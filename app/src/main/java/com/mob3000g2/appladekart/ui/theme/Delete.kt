package com.mob3000g2.appladekart.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mob3000g2.appladekart.R
import com.mob3000g2.appladekart.model.CarModel
import com.mob3000g2.appladekart.model.Company
import com.mob3000g2.appladekart.network.ElCarsApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Delete(navController: NavHostController) {

    val scope = rememberCoroutineScope()
    var companyName by rememberSaveable { mutableStateOf("") }
    var carModelName by rememberSaveable { mutableStateOf("") }
    var isCompanySelected by remember { mutableStateOf(false) }
    val context = LocalContext.current


    BottomMenuBar(navController = navController)
    Column (
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .padding(bottom = 70.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = (context.getString(R.string.app_admin_delete)), style = MaterialTheme.typography.headlineMedium)
        Text(context.getString(R.string.app_admin_delete_company_text))

            DropdownCompanyNameDelete {companyNames ->
                companyName = companyNames
                isCompanySelected = true
            }

        if (isCompanySelected) {
            DropdownCarModelDelete(companyName) {modelName ->
                carModelName = modelName
            }
        }

        Button(onClick = {
            scope.launch {
                deleteCompany(companyName)
            }
        }) {
            Text(context.getString(R.string.app_admin_delete_company))
        }

        Button(onClick = {
            scope.launch {
                deleteCarModel(companyName, carModelName)
            }
        }) {
            Text(context.getString(R.string.app_admin_delete_model))
        }
    }
}

@Composable
fun DropdownCompanyNameDelete(onCompanySelected: (String) -> Unit) {
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
fun DropdownCarModelDelete(
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

// Delete company
suspend fun deleteCompany (company: String) {
    try {
        ElCarsApi.retrofitService.deleteCompany(company)
        println("Company deleted successfully")
    } catch (e: Exception) {
        println("Error deleting company: $e")
    }
}

// Delete car model
suspend fun deleteCarModel(company: String, carModel: String) {
    try {
        ElCarsApi.retrofitService.deleteCarModel(company, carModel)
        println("Car model deleted successfully")
    } catch (e: Exception) {
        println("Error deleting car model: $e")
    }
}