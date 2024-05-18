package com.mob3000g2.appladekart.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mob3000g2.appladekart.R
import com.mob3000g2.appladekart.model.CarDetailsUpdate
import com.mob3000g2.appladekart.model.CarModel
import com.mob3000g2.appladekart.model.Company
import com.mob3000g2.appladekart.model.ModelVersion
import com.mob3000g2.appladekart.network.ElCarsApi
import kotlinx.coroutines.launch

data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,

    )

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Update(navController: NavHostController) {

    val scope = rememberCoroutineScope()
    var oldCompanyName by rememberSaveable { mutableStateOf("") }
    var companyName by rememberSaveable { mutableStateOf("") }
    var newCompanyName by rememberSaveable { mutableStateOf("") }
    var oldVersionName by rememberSaveable { mutableStateOf("") }
    var oldModelName by rememberSaveable { mutableStateOf("") }
    var isExposed by remember { mutableStateOf(false) }
    var newCarModel by remember { mutableStateOf("") }
    var newVersion by remember { mutableStateOf("") }
    var newRange by remember { mutableStateOf("") }
    var newBatteryCapacity by remember { mutableStateOf("") }
    var newChargingSpeed by remember { mutableStateOf("") }
    var isCompanySelected by remember { mutableStateOf(false) }
    var isModelSelected by remember { mutableStateOf(false) }
    val context = LocalContext.current


    BottomMenuBar(navController = navController)
    FlowRow(modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()
        .padding(bottom = 70.dp)
        .verticalScroll(rememberScrollState()),
    ) {

        Column(
            modifier = Modifier
                .width(300.dp)
        ) {
            Text(text = (context.getString(R.string.app_admin_update)), style = MaterialTheme.typography.headlineMedium)
            Text(text = (context.getString(R.string.app_admin_company)), style = MaterialTheme.typography.headlineSmall)

            Text(context.getString(R.string.app_admin_choose_company))

            DropdownCompanyNameUpdate { companyNames ->
                oldCompanyName = companyNames
            }

            OutlinedTextField(
                value = newCompanyName,
                onValueChange = { newCompanyName = it },
                label = { Text(context.getString(R.string.app_admin_new_name)) },
            )

            Button(onClick = {
                // Updates name of old company with new name
                scope.launch {
                    editCompany(oldCompanyName, newCompanyName)
                }
            }) {
                Text(context.getString(R.string.app_update))
            }
        }

            Column(
                modifier = Modifier
                    .width(300.dp)
            ) {
                Text(text = (context.getString(R.string.app_admin_car)), style = MaterialTheme.typography.headlineSmall)

                DropdownCompanyNameUpdate { companyNames ->
                    companyName = companyNames
                    isCompanySelected = true
                }

                if (isCompanySelected) {
                    DropdownCarModelUpdate(companyName) { modelName ->
                        oldModelName = modelName
                        isModelSelected = true
                    }
                }

                if (isCompanySelected && isModelSelected) {
                    DropdownModelVersionUpdate(companyName, oldModelName) { versionName ->
                        oldVersionName = versionName
                    }
                }

                OutlinedTextField(value = newCarModel, onValueChange = {
                    newCarModel = it
                }, label = { Text(context.getString(R.string.app_modell)) })

                OutlinedTextField(value = newVersion, onValueChange = {
                    newVersion = it
                }, label = { Text(context.getString(R.string.app_versjon)) })

                OutlinedTextField(value = newRange, onValueChange = {
                    newRange = it
                }, label = { Text(context.getString(R.string.app_range)) })

                OutlinedTextField(value = newBatteryCapacity, onValueChange = {
                    newBatteryCapacity = it
                }, label = { Text(context.getString(R.string.app_battery_capacity)) })

                OutlinedTextField(value = newChargingSpeed, onValueChange = {
                    newChargingSpeed = it
                }, label = { Text(context.getString(R.string.app_charge_speed)) }

                )

                Button(onClick = {
                    scope.launch {
                        editCarDetails(
                            companyName,
                            oldModelName,
                            newCarModel,
                            oldVersionName,
                            newVersion,
                            newRange.toInt(),
                            newBatteryCapacity.toInt(),
                            newChargingSpeed.toInt()
                        )
                    }
                }) {
                    Text(context.getString(R.string.app_update))
                }
            }
        }
}


@Composable
fun DropdownCompanyNameUpdate(onCompanySelected: (String) -> Unit) {
    val viewModel: LadekartViewModel = viewModel()
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    var carBrand by rememberSaveable { mutableStateOf(context.getString(R.string.app_merke)) }
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
    }

    Column(
        //modifier = Modifier.padding(horizontal = 10.dp),
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
                            onCompanySelected(company.name)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DropdownCarModelUpdate(
    company: String,
    onModelSelected: (String) -> Unit
) {
    val viewModel: LadekartViewModel = viewModel()
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    var carModels by rememberSaveable { mutableStateOf(context.getString(R.string.app_modell)) }
    var carModelList by rememberSaveable { mutableStateOf(listOf<CarModel>()) }
    var selectedModel by remember { mutableStateOf("") }
    var previousCompany by remember { mutableStateOf("") }

    // UI state values from ViewModel
    val menuUIStateListModel = viewModel.menuUIStateListModel.value
    val menuUIStateSelectedModel = viewModel.menuUIStateSelectedModel.value
    val menuUIStateSelectedPrevCompany = viewModel.menuUIStateSelectedPrevCompany.value
    var selectedViewCarModel by remember { mutableStateOf("") }
    var previousViewCompany by remember { mutableStateOf("") }

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
                            onModelSelected(carModel.name)
                        }
                    )
                }

            }
        }
    }
    // Set current company to previous
    previousCompany = company
}

@Composable
fun DropdownModelVersionUpdate(
    company: String,
    carModel: String,
    onVersionSelected: (String) -> Unit
) {
    val viewModel: LadekartViewModel = viewModel()
    val context = LocalContext.current
    var modelVersions by rememberSaveable { mutableStateOf(context.getString(R.string.app_versjon)) }
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
                                onVersionSelected(version)
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

// Edit company function
suspend fun editCompany(oldCompany: String, newCompany: String) {
    try {
        ElCarsApi.retrofitService.editCompany(oldCompany, newCompany)
        println("'$oldCompany' changed name to '$newCompany'")
    } catch (e: Exception) {
        println("Error adding company: $e")
    }
}

// Function for edit car model, version and specs
suspend fun editCarDetails(
    company: String,
    oldCarModel: String,
    newCarModel: String,
    oldVersion: String,
    newVersion: String,
    range: Int,
    batteryCapacity: Int,
    chargingSpeed: Int
) {
    try {
        val updatedDetails = CarDetailsUpdate(
            range_km = range,
            battery_capacity_kWh = batteryCapacity,
            charging_speed_kW = chargingSpeed
        )

        ElCarsApi.retrofitService.editCarDetails(
            company,
            oldCarModel,
            newCarModel,
            oldVersion,
            newVersion,
            updatedDetails
        )

        println("Car details updated successfully.")
    } catch (e: Exception) {
        println("Error updating car details: $e")
    }
}