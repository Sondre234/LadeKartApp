package com.mob3000g2.appladekart.ui.theme

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mob3000g2.appladekart.R
import com.mob3000g2.appladekart.model.CarModel
import com.mob3000g2.appladekart.model.Company
import com.mob3000g2.appladekart.model.ModelVersion
import com.mob3000g2.appladekart.model.VersionSpecs


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapMenu() {
    val density = LocalDensity.current.density
    val xPixel = 700
    val xDp = (xPixel / density).dp

    val context = LocalContext.current

    var isMenuVisible by remember { mutableStateOf(true) }

    if (isMenuVisible) {

        Column (modifier = Modifier
            .width(xDp)
            .verticalScroll(rememberScrollState())
            .background(
                //if (isSystemInDarkTheme()) Color.DarkGray else Color.White,
                color = colorScheme.background,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp, 65.dp, 20.dp, 20.dp)
            ,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            FirstText()
            DropdownCompanyName()
            SecondText()
            Slider()
            ThirdText()
            ChargeSlider()
            SeasonDecider()
            InfoButton()
        }

    }

    Button(
        onClick = {
            isMenuVisible = !isMenuVisible // Toggle menu visibility
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Text(if (isMenuVisible) context.getString(R.string.app_menu) else context.getString(R.string.app_menu))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownCompanyName() {
    val viewModel: LadekartViewModel = viewModel()
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    var carBrand by rememberSaveable { mutableStateOf("") }
    var companyList by rememberSaveable { mutableStateOf(listOf<Company>()) }
    var selectedCompany by remember { mutableStateOf("") }

    // UI state values from ViewModel
    val menuUIStateListCompany = viewModel.menuUIStateListCompany.value
    val menuUIStateSelectedCompany = viewModel.menuUIStateSelectedCompany.value
    var selectedViewCompany by remember { mutableStateOf("") }
    val carSlice by viewModel.userCar.observeAsState()

    // Handling UI states of company list
    when(menuUIStateListCompany) {
        is MenuUIStateListCompany.Success -> {
            companyList = menuUIStateListCompany.companyListState
        }
        else -> {}
    }

    // Handling UI states of selected company
    when(menuUIStateSelectedCompany) {
        is MenuUIStateSelectedCompany.Success -> {
            selectedViewCompany = menuUIStateSelectedCompany.companySelectedState
            if (selectedCompany != selectedViewCompany) {
                selectedCompany = selectedViewCompany
            }
        }
        else -> {}
    }

    // Fetching all companies
    LaunchedEffect(companyList) {
        viewModel.getCompaniesList()
        //viewModel.getSelectedCompany()
    }

    Column(
        modifier = Modifier.padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { isExpanded = it }) {

            OutlinedTextField(
                value = if (selectedCompany != "") selectedCompany else carBrand,
                label = { Text(context.getString(R.string.app_merke)) },
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                // Handles getting the company list when it gets forgotten
                if (companyList.isEmpty()) {
                    viewModel.getCompaniesList()
                }
                // Updating selectedCompany when an item is clicked
                for (company in companyList) {
                    DropdownMenuItem(
                        text = { Text(text = company.name) },
                        onClick = {
                            isExpanded = false
                            carBrand = company.name
                            selectedCompany = company.name
                            if (company.name != carSlice?.car) {
                                viewModel.insertSelectedCompany(selectedCompany)
                                viewModel.resetCarSlice()
                                viewModel.resetUserMark()
                                viewModel.insertSelectedModel("")
                                viewModel.insertSelectedVersion("")
                                viewModel.addCarSlice(selectedCompany)
                            }

                        }
                    )
                }
            }
        }
        // Rendering second dropdown based on the selected option
        if (selectedCompany != "") {
            DropdownCarModel(selectedCompany)
        }
    }
}

@Composable
fun DropdownCarModel(company: String) {
    val viewModel: LadekartViewModel = viewModel()
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    var carModels by rememberSaveable { mutableStateOf("") }
    var carModelList by rememberSaveable { mutableStateOf(listOf<CarModel>()) }
    var selectedModel by remember { mutableStateOf("") }
    var previousCompany by remember { mutableStateOf("") }

    // UI state values from ViewModel
    val menuUIStateListModel = viewModel.menuUIStateListModel.value
    val menuUIStateSelectedModel = viewModel.menuUIStateSelectedModel.value
    val menuUIStateSelectedPrevCompany = viewModel.menuUIStateSelectedPrevCompany.value
    var selectedViewCarModel by remember { mutableStateOf("") }
    var previousViewCompany by remember { mutableStateOf("") }
    val carSlice by viewModel.userCar.observeAsState()

    // Resetting dropdown menu if another company is selected
    if ( company != previousCompany) {
        carModels = context.getString(R.string.app_modell)
    }

    // Handling UI states of the car model list
    when(menuUIStateListModel) {
        is MenuUIStateListModel.Success -> {
            carModelList = menuUIStateListModel.modelListState
        }
        else -> {}
    }

    // Handling UI states of previously selected company
    when(menuUIStateSelectedPrevCompany) {
        is MenuUIStateSelectedPrevCompany.Success -> {
            previousViewCompany = menuUIStateSelectedPrevCompany.companyPrevSelectedState
            if (previousCompany != previousViewCompany) {
                previousViewCompany = previousCompany
            }
        }
        else -> {}
    }

    // Handling UI states of selected car model
    when(menuUIStateSelectedModel) {
        is MenuUIStateSelectedModel.Success -> {
            selectedViewCarModel = menuUIStateSelectedModel.modelSelectedState
            if (selectedModel != selectedViewCarModel) {
                selectedModel = selectedViewCarModel
            }
        }
        else -> {}
    }

    // Fetching car models of selected company
    LaunchedEffect(company) {
        viewModel.getCarModelList(company)
        //viewModel.getSelectedModel()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { isExpanded = it }) {

            OutlinedTextField(
                value = if (selectedModel != "") selectedModel else carModels,
                label = { Text(context.getString(R.string.app_modell)) },
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                // Handles getting the model list when it gets forgotten
                if (carModelList.isEmpty()) {
                    viewModel.getCarModelList(company)
                }

                // Populating second dropdown menu with car models of selected company
                for (carModel in carModelList) {
                    DropdownMenuItem(
                        text = { Text(text = carModel.name) },
                        onClick = {
                            isExpanded = false
                            carModels = carModel.name
                            selectedModel = carModel.name
                            if (carModel.name != carSlice?.type) {
                                viewModel.insertSelectedModel(selectedModel)
                                viewModel.insertSelectedVersion("")
                                viewModel.resetCarSliceType()
                                viewModel.resetCarSliceVersion()
                                viewModel.resetCarSliceInfo()
                                viewModel.resetCarSliceMaxRange()
                                viewModel.resetCarSliceRange()
                                viewModel.resetCarSliceCharMIN()
                                viewModel.resetUserMark()
                                viewModel.updateType(selectedModel)
                            }
                        }
                    )
                }

            }
        }
        // Rendering third dropdown based on the selected options
        if (selectedModel != "") {
            DropdownModelVersion(company, selectedModel)
        }
    }
    // Set current company to previous
    previousCompany = company
}

@Composable
fun DropdownModelVersion(company: String, carModel: String) {
    val viewModel: LadekartViewModel = viewModel()
    val context = LocalContext.current
    var modelVersions by rememberSaveable { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    var selectedVersion by remember { mutableStateOf("") }
    var modelVersionList by rememberSaveable { mutableStateOf(listOf<ModelVersion>()) }
    var previousCarModel by remember { mutableStateOf("") }

    // UI state values from ViewModel
    val menuUIStateListVersion = viewModel.menuUIStateListVersion.value
    val menuUIStateSelectedVersion = viewModel.menuUIStateSelectedVersion.value
    val menuUIStateSelectedPrevModel = viewModel.menuUIStateSelectedPrevModel.value
    var selectedViewVersion by remember { mutableStateOf("") }
    var previousViewCarModel by remember { mutableStateOf("") }
    val carSlice by viewModel.userCar.observeAsState()
    val menuUIStateListVersionSpec = viewModel.menuUIStateListVersionSpec.value
    var versionSpecs by remember { mutableStateOf(listOf<VersionSpecs>()) }
    val menuUIStateBatteryLevelSettings = viewModel.menuUIStateSettingsBatteryLevel.value
    var batteryLevel by remember { mutableStateOf(80f) }
    val menuUIStateChargingTimeSettings = viewModel.menuUIStateSettingsChargingTime.value
    var chargingTime by remember { mutableStateOf(30f) }

    // Handling UI state of the battery level setting
    when(menuUIStateBatteryLevelSettings) {
        is MenuUIStateBatteryLevelSettings.Success -> {
            batteryLevel = menuUIStateBatteryLevelSettings.batteryLevelSettings
        }
        else -> {}
    }

    // Handling UI state of the charging time setting
    when(menuUIStateChargingTimeSettings) {
        is MenuUIStateChargingTimeSettings.Success -> {
            chargingTime = menuUIStateChargingTimeSettings.chargingTimeSettings
        }
        else -> {}
    }


    // Handling UI states of the version list specs
    when(menuUIStateListVersionSpec) {
        is MenuUIStateListVersionSpec.Success -> {
            versionSpecs = menuUIStateListVersionSpec.versionSpecListState

            val selectedSpecs = versionSpecs.getOrNull(0)
            if (selectedSpecs != null) {
                val checkCar = selectedSpecs.company
                val checkModel = selectedSpecs.carModel
                val checkVersion = selectedSpecs.modelVersion
                val newMaxRange = selectedSpecs.details.range.toString()
                val newRange = (selectedSpecs.details.range * (batteryLevel.toInt().toDouble()/100)).toString()
                val newBatteryCapacity = selectedSpecs.details.batteryCapacity.toString()
                val newChargingSpeed = selectedSpecs.details.chargingSpeed.toString()
                val newCharMIN = chargingTime.toInt().toString()

                // Updates CarSlice info specs if values don't exists and are not the same
                if (!viewModel.isSameInfo(checkCar, checkModel, checkVersion, newMaxRange, newRange, newBatteryCapacity, newChargingSpeed, newCharMIN)) {
                    viewModel.updateInfo(newBatteryCapacity, newChargingSpeed)
                    viewModel.updateMaxRange(newMaxRange)
                    viewModel.updateRange(newRange)
                    viewModel.updateCharMIN(newCharMIN)
                }
            }
        }
        else -> {}
    }

    LaunchedEffect(selectedVersion) {
        if (selectedVersion.isNotEmpty()) {
            viewModel.getVersionSpecsList(company, carModel, selectedVersion)
        }
    }

    // Resetting dropdown menu if another car model is selected
    if (carModel != previousCarModel) {
        modelVersions = context.getString(R.string.app_versjon)
    }

    // Fetching versions of selected company and model
    LaunchedEffect(company, carModel) {
        viewModel.getModelVersionsList(company, carModel)
    }

    // Handling UI states of the version list
    when(menuUIStateListVersion) {
        is MenuUIStateListVersion.Success -> {
            modelVersionList = menuUIStateListVersion.versionListState
        }
        else -> {}
    }

    // Handling UI states for previously selected model
    when(menuUIStateSelectedPrevModel) {
        is MenuUIStateSelectedPrevModel.Success -> {
            previousViewCarModel = menuUIStateSelectedPrevModel.modelPrevSelectedState
            if (previousCarModel != previousViewCarModel) {
                previousViewCarModel = previousCarModel
            }
        }
        else -> {}
    }

    // Handling UI states of selected version
    when(menuUIStateSelectedVersion) {
        is MenuUIStateSelectedVersion.Success -> {
            selectedViewVersion = menuUIStateSelectedVersion.versionSelectedState
            if (selectedVersion != selectedViewVersion) {
                selectedVersion = selectedViewVersion
            }
        }
        else -> {}
    }




    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { isExpanded = it }) {

            OutlinedTextField(
                value = if (selectedVersion != "") selectedVersion else modelVersions,
                label = { Text(context.getString(R.string.app_versjon)) },
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                // Handles getting the version list when it gets forgotten
                if (modelVersionList.isEmpty()) {
                    viewModel.getModelVersionsList(company, carModel)
                }

                // Populating third dropdown menu with versions of selected model
                for (modelVersion in modelVersionList) {
                    for (version in modelVersion.versions) {
                        DropdownMenuItem(
                            text = { Text(text = version) },
                            onClick = {
                                isExpanded = false
                                modelVersions = version
                                selectedVersion = version
                                if (version != carSlice?.version) {
                                    viewModel.insertSelectedVersion(selectedVersion)
                                    viewModel.resetCarSliceVersion()
                                    viewModel.resetCarSliceInfo()
                                    viewModel.resetCarSliceMaxRange()
                                    viewModel.resetCarSliceRange()
                                    viewModel.resetCarSliceCharMIN()
                                    viewModel.resetUserMark()
                                    viewModel.updateVersion(selectedVersion)

                                }
                            }
                        )
                    }
                }
            }
        }
    }
    // Set current car model to previous
    previousCarModel = carModel
}



@Composable
fun Slider() {
    val viewModel: LadekartViewModel = viewModel()
    val context = LocalContext.current
    val menuUIStateListVersionSpec = viewModel.menuUIStateListVersionSpec.value
    var versionSpecs by remember { mutableStateOf(listOf<VersionSpecs>()) }
    val carSlice by viewModel.userCar.observeAsState()
    var sliderPosition by rememberSaveable {
        mutableStateOf(80f)
    }
    val menuUIStateBatteryLevelSettings = viewModel.menuUIStateSettingsBatteryLevel.value
    var batteryLevel by remember { mutableStateOf(80f) }

    // Handling UI state of the battery level setting
    when(menuUIStateBatteryLevelSettings) {
        is MenuUIStateBatteryLevelSettings.Success -> {
            batteryLevel = menuUIStateBatteryLevelSettings.batteryLevelSettings
            // Checks if sliderPosition is like the Battery level in the viewModel
            if (sliderPosition != batteryLevel) {
                sliderPosition = batteryLevel
            }

            val selectedSpecs = versionSpecs.getOrNull(0)
            if (selectedSpecs != null) {
                viewModel.updateRange((selectedSpecs.details.range * (batteryLevel.toInt().toDouble()/100)).toString())
            }
            Log.d("MyMap", ""+carSlice)
        }
        else -> {}
    }

    // Handling UI states for getting version list specs
    when(menuUIStateListVersionSpec) {
        is MenuUIStateListVersionSpec.Success -> {
            versionSpecs = menuUIStateListVersionSpec.versionSpecListState
        }
        else -> {}
    }




    Column(

        //edit layout and padding
        modifier = Modifier.padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = " ${sliderPosition.toInt()}"+context.getString(R.string.app_percent))
        
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.material3.Slider(
                modifier = Modifier.fillMaxWidth(),
                value = sliderPosition,
                onValueChange = {
                    sliderPosition = it
                    viewModel.insertBatteryLevelMenu(sliderPosition)
                },
                // set the highest and lowest value of the slider
                valueRange = 20f..100f,
                // the number of possible selections on the slider
                // 1 for each percent point
                steps = 80

            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 0.dp, 0.dp, 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = context.getString(R.string.app_20_percent))
                Text(text = context.getString(R.string.app_100_percent))
            }
        }
    }

    


}

@Composable
fun FirstText() {
    val context = LocalContext.current
    Text(text = context.getString(R.string.app_choose_car))

}

@Composable
fun SecondText() {
    val context = LocalContext.current
    Text(text = context.getString(R.string.app_set_charging))
}

@Composable
fun ThirdText() {
    val context = LocalContext.current
    Text(text = context.getString(R.string.app_charging_station_time))
}

@Composable
fun ChargeSlider() {
    val viewModel: LadekartViewModel = viewModel()
    val menuUIStateListVersionSpec = viewModel.menuUIStateListVersionSpec.value
    var versionSpecs by remember { mutableStateOf(listOf<VersionSpecs>()) }
    val context = LocalContext.current
    var sliderPosition by rememberSaveable {
        mutableStateOf(30f)
    }
    val menuUIStateChargingTimeSettings = viewModel.menuUIStateSettingsChargingTime.value
    var chargingTime by remember { mutableStateOf(30f) }

    // Handling UI state of the charging time setting
    when(menuUIStateChargingTimeSettings) {
        is MenuUIStateChargingTimeSettings.Success -> {
            chargingTime = menuUIStateChargingTimeSettings.chargingTimeSettings
            // Checks if sliderPosition is like the Battery level in the viewModel
            if (sliderPosition != chargingTime) {
                sliderPosition = chargingTime
            }

            val selectedSpecs = versionSpecs.getOrNull(0)
            if (selectedSpecs != null) {
                viewModel.updateCharMIN(chargingTime.toInt().toString())
            }
        }
        else -> {}
    }

    // Handling UI states for getting version list specs
    when(menuUIStateListVersionSpec) {
        is MenuUIStateListVersionSpec.Success -> {
            versionSpecs = menuUIStateListVersionSpec.versionSpecListState
        }
        else -> {}
    }

    Column(

        //edit layout and padding
        modifier = Modifier.padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = " ${sliderPosition.toInt()} "+context.getString(R.string.app_min))

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.material3.Slider(
                modifier = Modifier.fillMaxWidth(),
                value = sliderPosition,
                onValueChange = {
                    sliderPosition = it
                    viewModel.insertChargingTimeMenu(sliderPosition)
                },
                // set the highest and lowest value of the slider
                valueRange = 5f..120f,
                // the number of possible selections on the slider
                // up to 2 hours in minutes
                steps = 115
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 0.dp, 0.dp, 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = context.getString(R.string.app_min_min))
                Text(text = context.getString(R.string.app_max_min))
            }
        }
    }


}



@Composable
fun SeasonDecider() {
    val viewModel: LadekartViewModel = viewModel()
    val context = LocalContext.current
    var checkedValue by rememberSaveable { mutableStateOf(true) }
    var text = if (checkedValue) {
        context.getString(R.string.app_summer)
    } else {
        context.getString(R.string.app_winter)
    }
    val menuUIStateSeasonChoiceSettings = viewModel.menuUIStateSettingsSeasonChoice.value
    var seasonChoice by remember { mutableStateOf(true) }

    // Handling UI state of the season choice setting
    when(menuUIStateSeasonChoiceSettings) {
        is MenuUIStateSeasonChoiceSettings.Success -> {
            seasonChoice = menuUIStateSeasonChoiceSettings.seasonChoiceSettings
            // Checks if sliderPosition is like the Battery level in the viewModel
            if (checkedValue != seasonChoice) {
                checkedValue = seasonChoice
                if (checkedValue) text = context.getString(R.string.app_summer) else text = context.getString(R.string.app_winter)
            }
        }
        else -> {}
    }

    Column {
        Switch(
            checked = checkedValue,
            onCheckedChange = {
                checkedValue = it
                viewModel.insertSeasonChoiceMenu(checkedValue)
            }
        )

        Text(text = text)
    }
}



@Composable
fun InfoButton() {
    val context = LocalContext.current
    var isTextVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isTextVisible) {
            Text(
                text = context.getString(R.string.app_info_text),
                modifier = Modifier.clickable {
                    // Clicking the text will hide the text and reveal the button again
                    isTextVisible = !isTextVisible
                }
            )
        } else {
            Button(
                onClick = {
                    isTextVisible = !isTextVisible
                }
            ) {
                Text(context.getString(R.string.app_info))
            }
        }
    }
}



