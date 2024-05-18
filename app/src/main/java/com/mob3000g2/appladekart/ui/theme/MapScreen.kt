package com.mob3000g2.appladekart.ui.theme

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.mob3000g2.appladekart.R
import com.mob3000g2.appladekart.model.CalculationResult
import com.mob3000g2.appladekart.model.MapStations
import com.mob3000g2.appladekart.model.UserMarkSelect
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val TAG = "MyMap"

@Composable
fun MapScreen(
    viewModel: LadekartViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var circleState by rememberSaveable { mutableStateOf<LatLng?>(null) }
    var markerState: StationsResponseState by remember { mutableStateOf(StationsResponseState()) }
    val mapUIState = viewModel.mapUIState.value
    val carSliceMapMark by viewModel.userCar.observeAsState()
    val menuUIStateSeasonChoiceSettings = viewModel.menuUIStateSettingsSeasonChoice.value
    var seasonChoice by remember { mutableStateOf(true) }
    val selMark by viewModel.userMarks.observeAsState()
    val selectedStationsMark = viewModel.userMarks.observeAsState().value.orEmpty()
    var seasonDeviation = 1.0
    val rangeDeviation = 0.75

    when(mapUIState) {
        is MapUIState.Success -> {
            markerState = mapUIState.stationsState
            Log.d(TAG, "API responded")
        }
        else -> {
            Log.d(TAG, "API is not working/awake yet")
        }
    }

    // Handling UI state of the season choice setting
    when(menuUIStateSeasonChoiceSettings) {
        is MenuUIStateSeasonChoiceSettings.Success -> {
            seasonChoice = menuUIStateSeasonChoiceSettings.seasonChoiceSettings
            if (seasonChoice) seasonDeviation = 1.0 else seasonDeviation = 0.8
        }
        else -> {}
    }


    var showInfoWindow by remember {
        mutableStateOf(true)
    }
    val context = LocalContext.current
    val center = LatLng(59.911491, 10.757933)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 10f)
    }


    // Calculate the range left after getting to the station
    fun getRangeMarker(rangeMC: Double, distanceMC: Double): Double {
        return rangeMC - distanceMC
    }

    // Calculate the charged range
    fun getAddRange(charMinC: Double, maxChargeM: Double, charSpeedC: Double, battCapC: Double, maxRangeC: Double, distanceMC: Double, rangeMC: Double): Double {
        val rangeLeft = rangeMC - distanceMC
        val rangeKmPerKwh = maxRangeC / battCapC
        val newRange = if (maxChargeM > charSpeedC) {
            (charSpeedC * (charMinC / 60)) * rangeKmPerKwh
        } else {
            (maxChargeM * (charMinC / 60)) * rangeKmPerKwh
        }
        val maxAllowedCharged = maxRangeC - rangeLeft
        return if (maxAllowedCharged > newRange) newRange else newRange - maxAllowedCharged
    }

    // Extract the charging capacity from the string given from the NOBIL API
    fun getCharCap(charCapString: String): Double? {
        val regex = """([\d,.]+)\s*kW""".toRegex()
        val matchResult = regex.find(charCapString)
        return matchResult?.groups?.get(1)?.value?.replace(',', '.')?.toDoubleOrNull()
    }

    // Calculate distance between the starting point and the selected station
    // Using Haversine formula
    fun mapPointDistanceCalculator(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371 // Earth radius in km
        val dLat = (lat2 - lat1) * PI / 180.0
        val dLng = (lng2 - lng1) * PI / 180.0
        val a = sin(dLat / 2).pow(2) + cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    // Handling UI state of selected stations
    fun handleStationClick(station: MapStations) {
        val isSelected = viewModel.userMarks.value?.any { it.id == station.id } ?: false

        if (isSelected) {
            viewModel.deleteUserMarkSelect(station.id)
        } else {
            val carSlice = viewModel.userCar.value
            if (carSlice != null && carSlice.type.isNotEmpty() && carSlice.version.isNotEmpty() &&
                carSlice.maxRange.isNotEmpty() && carSlice.range.isNotEmpty() && carSlice.battCap.isNotEmpty() &&
                carSlice.charSpeed.isNotEmpty() && carSlice.lat.isNotEmpty() && carSlice.lng.isNotEmpty() &&
                carSlice.charMIN.isNotEmpty()) {

                val maxChargeM = getCharCap(station.maxChargingCapacity) ?: 0.0
                val selectedStations = viewModel.userMarks.value.orEmpty()

                val calculationResult = if (selectedStations.isEmpty()) {
                    val distanceMC = mapPointDistanceCalculator(carSlice.lat.toDouble(), carSlice.lng.toDouble(), station.latLng.lat, station.latLng.lng)
                    val addRangeMC = getAddRange(carSlice.charMIN.toDouble(), maxChargeM, carSlice.charSpeed.toDouble(), carSlice.battCap.toDouble(), carSlice.maxRange.toDouble(), distanceMC, carSlice.range.toDouble())
                    CalculationResult(
                        distanceM = distanceMC,
                        addRangeM = addRangeMC,
                        rangeM = addRangeMC + getRangeMarker(carSlice.range.toDouble(), distanceMC),
                        checkRange = carSlice.range.toDouble()
                    )
                } else {
                    val lastStation = selectedStations.last()
                    val distanceMC = mapPointDistanceCalculator(lastStation.lat, lastStation.lng, station.latLng.lat, station.latLng.lng)
                    val addRangeMC = getAddRange(carSlice.charMIN.toDouble(), maxChargeM, carSlice.charSpeed.toDouble(), carSlice.battCap.toDouble(), carSlice.maxRange.toDouble(), distanceMC, lastStation.range)
                    CalculationResult(
                        distanceM = distanceMC,
                        addRangeM = addRangeMC,
                        rangeM = addRangeMC + getRangeMarker(lastStation.range, distanceMC),
                        checkRange = lastStation.range
                    )
                }
                // Checks if the station is out of range or not
                if (calculationResult.distanceM <= (calculationResult.checkRange * rangeDeviation)) {
                    viewModel.addUserMarkSelect(UserMarkSelect(
                        id = station.id,
                        lat = station.latLng.lat,
                        lng = station.latLng.lng,
                        distance = calculationResult.distanceM,
                        range = calculationResult.rangeM,
                        maxCharge = maxChargeM,
                        addedRange = calculationResult.addRangeM
                    ))
                }
                Log.d(TAG, "Stations selected: "+selMark)
            }
        }
    }

    // Handling UI state of location of CarSlice
    fun handleMapClick(latLng: LatLng) {
        val carSliceMap = viewModel.userCar.value
        if (carSliceMap != null && carSliceMap.type.isNotEmpty() && carSliceMap.version.isNotEmpty() &&
            carSliceMap.maxRange.isNotEmpty() && carSliceMap.range.isNotEmpty()) {

            viewModel.resetUserMark()
            circleState = latLng
            val latitude = latLng.latitude
            val longitude = latLng.longitude
            viewModel.updateLatLng(latitude.toString(), longitude.toString())
        }
    }


    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng -> handleMapClick(latLng) }
    ) {
        if (circleState != null && carSliceMapMark != null) {
            Marker(
                state = MarkerState(circleState!!),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            )
            Circle(
                center = circleState!!,
                radius = (((carSliceMapMark!!.range.toDouble()*1000)*rangeDeviation)*seasonDeviation),
                fillColor = Color.Red.copy(alpha = 0.35f),
                strokeColor = Color.Red.copy(alpha = 0.8f),
                strokeWidth = 2f
            )
        }
        markerState.response?.forEach { station ->
            val isSelected = selectedStationsMark.any { it.id == station.id }

            MarkerInfoWindowContent(
                state = MarkerState(position = LatLng(station.latLng.lat, station.latLng.lng)),
                onClick = {
                    handleStationClick(station = station)
                    if (showInfoWindow) {
                        it.showInfoWindow()
                        showInfoWindow = false
                    } else {
                        it.hideInfoWindow()
                        showInfoWindow = true
                    }
                    return@MarkerInfoWindowContent false
                },
                icon = if (isSelected) BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE) else BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                title = station.id+"",
                onInfoWindowClick = {
                    it.hideInfoWindow()
                    showInfoWindow = true
                }
            ) {
                Column (
                    modifier = Modifier.background(color = MaterialTheme.colorScheme.background,)
                ) {
                    Text(text = station.name, fontWeight = FontWeight.Bold);
                    station.description?.let { it1 -> Text(text = it1) }
                    Text(text = station.address)
                    Text(text = context.getString(R.string.app_kontakttype)+" "+station.connector)
                    Text(text = context.getString(R.string.app_ladekapasitet)+" "+station.maxChargingCapacity)
                }
            }
            if (isSelected) {
                val selectedStation = selectedStationsMark.firstOrNull { it.id == station.id }

                selectedStation?.let {
                    Circle(
                        center = LatLng(it.lat, it.lng),
                        radius = ((it.range * 1000) * rangeDeviation) * seasonDeviation,
                        fillColor = Color.Red.copy(alpha = 0.35f),
                        strokeColor = Color.Red.copy(alpha = 0.8f),
                        strokeWidth = 2f
                    )
                }
            }

        }
    }
    trackMapInteraction(cameraPositionState = cameraPositionState, viewModel = viewModel)
}

/*
 * The MapTracking have been taken from this tutorial here:
 * https://medium.com/@shrirampasrija/oncameraidlelistener-oncameramovestartedlistener-for-googlemaps-jetpack-compose-923460273e
 * As the Maps Composable Library don't have any default IdleListener as the Web Google Maps Api has
 */
@Composable
fun trackMapInteraction(
    cameraPositionState: CameraPositionState,
    viewModel: LadekartViewModel
) {
    // store the initial position of the camera
    var initialCameraPosition by remember { mutableStateOf(cameraPositionState.projection?.visibleRegion?.latLngBounds) }
    
    // called when the camera just starts moving
    val onMapCameraMoveStart: (LatLngBounds?) -> Unit = {
        // store the camera's position when map started moving
        initialCameraPosition = it
    }
    
    // Called when the map camera stops moving
    val onMapCameraIdle: (LatLngBounds?) -> Unit = { newBounds ->

        val latLng = newBounds

        initialCameraPosition = newBounds
        onIdle(viewModel, latLng)
        Log.d(TAG, ""+latLng)
    }

    LaunchedEffect(key1 = cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) onMapCameraMoveStart(cameraPositionState.projection?.visibleRegion?.latLngBounds)
        else onMapCameraIdle(cameraPositionState.projection?.visibleRegion?.latLngBounds)
    }
}

// Gets the charging stations from the API by ne (northeast) and sw (southwest)
// coordinates
// Working?
fun onIdle(ladekartViewModel: LadekartViewModel, bounds: LatLngBounds?) {
    bounds?.let {
        val latitude = "(" + it.northeast.latitude + ", " + it.northeast.longitude + ")"
        val longitude = "(" + it.southwest.latitude + ", " + it.southwest.longitude + ")"
        ladekartViewModel.getChargerStation(latitude,longitude)
    }
}


@Preview
@Composable
fun MapScreenPreview(){
    MapScreen(
        modifier = Modifier.fillMaxSize()
    )
}