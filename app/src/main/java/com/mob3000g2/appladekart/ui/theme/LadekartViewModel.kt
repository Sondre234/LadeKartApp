package com.mob3000g2.appladekart.ui.theme

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mob3000g2.appladekart.model.CarModel
import com.mob3000g2.appladekart.model.CarSlice
import com.mob3000g2.appladekart.model.ChargerRequestBody
import com.mob3000g2.appladekart.model.Company
import com.mob3000g2.appladekart.model.MapStations
import com.mob3000g2.appladekart.model.ModelVersion
import com.mob3000g2.appladekart.model.UserMarkSelect
import com.mob3000g2.appladekart.model.VersionSpecs
import com.mob3000g2.appladekart.network.ElCarsApi
import com.mob3000g2.appladekart.network.ElCarsApi2
import com.mob3000g2.appladekart.network.MapStationsApi
import kotlinx.coroutines.launch

private const val TAG = "MyMap"

// The viewModel is based on examples and videos from other people.
// So the sealed interface method that is being used here was really easy to use
// but in the end made it less readable, but still fit the UC of how I want the app
// to get/use/change data for the UI State
// So when it comes to the Car and Markers setting that the user can change,
// we ended up using _userCar & _userMarks methode for the viewModel part,
// and we did not change out the rest as everything works as it should

// Map UI State
sealed interface MapUIState {
    data class Success(val stationsState: StationsResponseState): MapUIState
    object Error : MapUIState
    object Loading : MapUIState
}

data class StationsResponseState(
    val response: List<MapStations>? = null,
    val error: Throwable? = null
)


// Menu Selected UI States
sealed interface MenuUIStateSelectedCompany {
    data class Success(val companySelectedState: String): MenuUIStateSelectedCompany
    object Error : MenuUIStateSelectedCompany
    object Loading : MenuUIStateSelectedCompany
}

sealed interface MenuUIStateSelectedModel {
    data class Success(val modelSelectedState: String): MenuUIStateSelectedModel
    object Error : MenuUIStateSelectedModel
    object Loading : MenuUIStateSelectedModel
}

sealed interface MenuUIStateSelectedVersion {
    data class Success(val versionSelectedState: String): MenuUIStateSelectedVersion
    object Error : MenuUIStateSelectedVersion
    object Loading : MenuUIStateSelectedVersion
}


// Menu Previous Selected UI States
sealed interface MenuUIStateSelectedPrevCompany {
    data class Success(val companyPrevSelectedState: String): MenuUIStateSelectedPrevCompany
    object Error : MenuUIStateSelectedPrevCompany
    object Loading : MenuUIStateSelectedPrevCompany
}

sealed interface MenuUIStateSelectedPrevModel {
    data class Success(val modelPrevSelectedState: String): MenuUIStateSelectedPrevModel
    object Error : MenuUIStateSelectedPrevModel
    object Loading : MenuUIStateSelectedPrevModel
}


// Menu List UI State
sealed interface MenuUIStateListCompany {
    data class Success(val companyListState: List<Company>): MenuUIStateListCompany
    object Error : MenuUIStateListCompany
    object Loading : MenuUIStateListCompany
}

sealed interface MenuUIStateListModel {
    data class Success(val modelListState: List<CarModel>): MenuUIStateListModel
    object Error : MenuUIStateListModel
    object Loading : MenuUIStateListModel
}

sealed interface MenuUIStateListVersion {
    data class Success(val versionListState: List<ModelVersion>): MenuUIStateListVersion
    object Error : MenuUIStateListVersion
    object Loading : MenuUIStateListVersion
}

sealed interface MenuUIStateListVersionSpec {
    data class Success(val versionSpecListState: List<VersionSpecs>): MenuUIStateListVersionSpec
    object Error : MenuUIStateListVersionSpec
    object Loading : MenuUIStateListVersionSpec
}


// Menu Settings UI State
sealed interface MenuUIStateBatteryLevelSettings {
    data class Success(val batteryLevelSettings: Float): MenuUIStateBatteryLevelSettings
    object Error : MenuUIStateBatteryLevelSettings
    object Loading : MenuUIStateBatteryLevelSettings
}

sealed interface MenuUIStateChargingTimeSettings {
    data class Success(val chargingTimeSettings: Float): MenuUIStateChargingTimeSettings
    object Error : MenuUIStateChargingTimeSettings
    object Loading : MenuUIStateChargingTimeSettings
}

sealed interface MenuUIStateSeasonChoiceSettings {
    data class Success(val seasonChoiceSettings: Boolean): MenuUIStateSeasonChoiceSettings
    object Error : MenuUIStateSeasonChoiceSettings
    object Loading : MenuUIStateSeasonChoiceSettings
}

// ViewModel for Map, Calculations and Menu
class LadekartViewModel : ViewModel() {
    // map UI state
    var mapUIState: MutableState<MapUIState> = mutableStateOf(MapUIState.Loading)
        private set


    // menu list UI state
    var menuUIStateListCompany: MutableState<MenuUIStateListCompany> = mutableStateOf(MenuUIStateListCompany.Loading)
        private set

    var menuUIStateListModel: MutableState<MenuUIStateListModel> = mutableStateOf(MenuUIStateListModel.Loading)
        private set

    var menuUIStateListVersion: MutableState<MenuUIStateListVersion> = mutableStateOf(MenuUIStateListVersion.Loading)
        private set

    var menuUIStateListVersionSpec: MutableState<MenuUIStateListVersionSpec> = mutableStateOf(MenuUIStateListVersionSpec.Loading)
        private set


    // menu selected UI state
    var menuUIStateSelectedCompany: MutableState<MenuUIStateSelectedCompany> = mutableStateOf(MenuUIStateSelectedCompany.Loading)
        private set

    var menuUIStateSelectedModel: MutableState<MenuUIStateSelectedModel> = mutableStateOf(MenuUIStateSelectedModel.Loading)
        private set

    var menuUIStateSelectedVersion: MutableState<MenuUIStateSelectedVersion> = mutableStateOf(MenuUIStateSelectedVersion.Loading)
        private set


    // menu previous selected UI state
    var menuUIStateSelectedPrevCompany: MutableState<MenuUIStateSelectedPrevCompany> = mutableStateOf(MenuUIStateSelectedPrevCompany.Loading)
        private set

    var menuUIStateSelectedPrevModel: MutableState<MenuUIStateSelectedPrevModel> = mutableStateOf(MenuUIStateSelectedPrevModel.Loading)
        private set


    // menu settings UI state
    var menuUIStateSettingsBatteryLevel: MutableState<MenuUIStateBatteryLevelSettings> = mutableStateOf(MenuUIStateBatteryLevelSettings.Loading)
        private set

    var menuUIStateSettingsChargingTime: MutableState<MenuUIStateChargingTimeSettings> = mutableStateOf(MenuUIStateChargingTimeSettings.Loading)
        private set

    var menuUIStateSettingsSeasonChoice: MutableState<MenuUIStateSeasonChoiceSettings> = mutableStateOf(MenuUIStateSeasonChoiceSettings.Loading)
        private set


    // Store information for callculating distance
    private val _userCar = MutableLiveData<CarSlice>()
    val userCar: LiveData<CarSlice> = _userCar

    private val _userMarks = MutableLiveData<List<UserMarkSelect>>(emptyList())
    val userMarks: LiveData<List<UserMarkSelect>> = _userMarks


    // Api service calls
    private val apiService = MapStationsApi.retrofitService
    private val elApiService = ElCarsApi.retrofitService
    private val elApiService2 = ElCarsApi2.retrofitService2


    // Initial call to get the map charging stations
    init {
        getChargerStation("("+60.232481164098054.toString()+", "+12.076292374999985.toString()+")", "("+59.587366886749464.toString()+", "+9.439573624999985.toString()+")")
    }


    // Selected var for menu
    var selectedCompany by mutableStateOf("")
    var selectedModel by mutableStateOf("")
    var selectedVersion by mutableStateOf("")


    // List var for menu
    var companyList by mutableStateOf(listOf<Company>())
    var carModelList by mutableStateOf(listOf<CarModel>())
    var versionList by mutableStateOf(listOf<ModelVersion>())
    var versionSpecList by mutableStateOf(listOf<VersionSpecs>())


    // Menu settings var
    var batteryLevel by mutableStateOf(80f)
    var chargingTime by mutableStateOf(30f)
    var seasonChoice by mutableStateOf(true)


    // Get the charging stations and sends them to the map
    fun getChargerStation(ne: String, sw: String) {
        viewModelScope.launch {
            try {
                val requestBody = ChargerRequestBody(ChargerRequestBody.Bounds(ne, sw))
                val stationsList: List<MapStations> = apiService.getChargerJson(requestBody)

                mapUIState.value = MapUIState.Success(StationsResponseState(response = stationsList))
            } catch (e: Exception) {
                mapUIState.value = MapUIState.Error
            }
        }
    }


    // Get car lists info from api
    fun getCompaniesList() {
        viewModelScope.launch {
            menuUIStateListCompany.value = MenuUIStateListCompany.Loading
            try {
                val companiesStringList = elApiService.getCompanies()

                val companiesList = companiesStringList.map { Company.fromString(it) }

                companyList = companiesList
                menuUIStateListCompany.value = MenuUIStateListCompany.Success(companyList)
            } catch (e: Exception) {
                menuUIStateListCompany.value = MenuUIStateListCompany.Error
            }
        }
    }

    fun getCarModelList(company: String) {
        viewModelScope.launch {
            try {
                var modelsStringList = elApiService.getCarModels(company)

                val modelList = modelsStringList.map { CarModel.fromString(it) }

                carModelList = modelList
                menuUIStateListModel.value = MenuUIStateListModel.Success(carModelList)
            } catch (e: Exception) {
                menuUIStateListModel.value = MenuUIStateListModel.Error
            }
        }
    }

    fun getModelVersionsList(company: String, carModel: String) {
        viewModelScope.launch {
            try {
                versionList = elApiService2.getModelVersions(company, carModel)
                menuUIStateListVersion.value = MenuUIStateListVersion.Success(versionList)
            } catch (e: Exception) {
                menuUIStateListVersion.value = MenuUIStateListVersion.Error
            }
        }
    }

    fun getVersionSpecsList(company: String, carModel: String, modelVersion: String) {
        viewModelScope.launch {
            try {
                versionSpecList = elApiService2.getVersionSpecs(company, carModel, modelVersion)
                menuUIStateListVersionSpec.value = MenuUIStateListVersionSpec.Success(versionSpecList)
            } catch (e: Exception) {
                menuUIStateListVersionSpec.value = MenuUIStateListVersionSpec.Error
            }
        }
    }


    // Insert Selected
    fun insertSelectedCompany(company: String) {
        viewModelScope.launch {
            selectedCompany = company
            menuUIStateSelectedCompany.value = MenuUIStateSelectedCompany.Success(companySelectedState = company)
        }
    }

    fun insertSelectedModel(carModel: String) {
        viewModelScope.launch {
            selectedModel = carModel
            menuUIStateSelectedModel.value = MenuUIStateSelectedModel.Success(modelSelectedState = carModel)
        }
    }

    fun insertSelectedVersion(carVersion: String) {
        viewModelScope.launch {
            selectedVersion = carVersion
            menuUIStateSelectedVersion.value = MenuUIStateSelectedVersion.Success(versionSelectedState = carVersion)
        }
    }


    // Insert Menu Settings
    fun insertBatteryLevelMenu(level: Float) {
        viewModelScope.launch {
            batteryLevel = level
            menuUIStateSettingsBatteryLevel.value = MenuUIStateBatteryLevelSettings.Success(batteryLevelSettings = level)
        }
    }

    fun insertChargingTimeMenu(time: Float) {
        viewModelScope.launch {
            chargingTime = time
            menuUIStateSettingsChargingTime.value = MenuUIStateChargingTimeSettings.Success(chargingTimeSettings = time)
        }
    }

    fun insertSeasonChoiceMenu(season: Boolean) {
        viewModelScope.launch {
            seasonChoice = season
            menuUIStateSettingsSeasonChoice.value = MenuUIStateSeasonChoiceSettings.Success(seasonChoiceSettings = season)
        }
    }


    // CarSlice Add, Update & Reset
    fun addCarSlice(car: String) {
        _userCar.value = CarSlice(car = car)
    }

    fun updateType(type: String) {
        _userCar.value?.let {
            _userCar.value = it.copy(type = type)
        }
    }

    fun updateVersion(version: String) {
        _userCar.value?.let {
            _userCar.value = it.copy(version = version)
        }
    }

    fun updateInfo(battCap: String, charSpeed: String) {
        _userCar.value?.let {
            _userCar.value = it.copy(battCap = battCap, charSpeed = charSpeed)
        }
    }

    fun updateRange(range: String) {
        _userCar.value?.let {
            _userCar.value = it.copy(range = range)
        }
    }

    fun updateLatLng(lat: String, lng: String) {
        _userCar.value?.let {
            _userCar.value = it.copy(lat = lat, lng = lng)
        }
    }

    fun updateCharMIN(charMIN: String) {
        _userCar.value?.let {
            _userCar.value = it.copy(charMIN = charMIN)
        }
    }

    fun updateMaxRange(maxRange: String) {
        _userCar.value?.let {
            _userCar.value = it.copy(maxRange = maxRange)
        }
    }

    fun resetCarSlice() {
        _userCar.value = CarSlice()
    }

    fun resetCarSliceType() {
        _userCar.value?.let {
            _userCar.value = it.copy(type = "")
        }
    }

    fun resetCarSliceVersion() {
        _userCar.value?.let {
            _userCar.value = it.copy(version = "")
        }
    }

    fun resetCarSliceInfo() {
        _userCar.value?.let {
            _userCar.value = it.copy(battCap = "", charSpeed = "")
        }
    }

    fun resetCarSliceMaxRange() {
        _userCar.value?.let {
            _userCar.value = it.copy(maxRange = "")
        }
    }

    fun resetCarSliceRange() {
        _userCar.value?.let {
            _userCar.value = it.copy(range = "")
        }
    }

    fun resetCarSliceCharMIN() {
        _userCar.value?.let {
            _userCar.value = it.copy(charMIN = "")
        }
    }

    // CarSlice Info update check if already same info
    fun isSameInfo(newCar: String, newModel: String, newVersion: String, newMaxRange: String, newRange: String, newBatteryCapacity: String, newChargingSpeed: String, newCharMIN: String): Boolean {

        val currentCar = _userCar.value?.car
        val currentModel = _userCar.value?.type
        val currentVersion = _userCar.value?.version
        val currentMaxRange = _userCar.value?.maxRange
        val currentRange = _userCar.value?.range
        val currentBatteryCapacity = _userCar.value?.battCap
        val currentChargingSpeed = _userCar.value?.charSpeed
        val currentCharMIN = _userCar.value?.charMIN

        return newCar == currentCar && newModel == currentModel && newVersion == currentVersion && newMaxRange == currentMaxRange && newRange == currentRange && newBatteryCapacity == currentBatteryCapacity && newChargingSpeed == currentChargingSpeed && newCharMIN == currentCharMIN
    }

    // UserMarkSelect Add, Update, Delete & Reset
    fun addUserMarkSelect(markSelect: UserMarkSelect) {
        _userMarks.value = _userMarks.value.orEmpty() + markSelect
    }

    fun deleteUserMarkSelect(id: String) {
        _userMarks.value = _userMarks.value?.filter { it.id != id }
    }

    fun resetUserMark() {
        _userMarks.value = emptyList()
    }
}