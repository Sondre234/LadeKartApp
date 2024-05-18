package com.mob3000g2.appladekart.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mob3000g2.appladekart.R
import com.mob3000g2.appladekart.model.NewSpecs
import com.mob3000g2.appladekart.network.ElCarsApi
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Insert(navController: NavHostController) {

    val scope = rememberCoroutineScope()
    var selskap by remember { mutableStateOf(TextFieldValue("")) }
    var bilmodell by remember { mutableStateOf(TextFieldValue("")) }
    var versjon by remember { mutableStateOf(TextFieldValue("")) }
    var rekkevidde by remember { mutableStateOf(TextFieldValue("")) }
    var batterikapasitet by remember { mutableStateOf(TextFieldValue("")) }
    var ladefart by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current

    BottomMenuBar(navController = navController)
    Column(modifier = Modifier
        .padding(16.dp)
        .width(500.dp)
        .padding(bottom = 70.dp)
        .verticalScroll(rememberScrollState())
        .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = (context.getString(R.string.app_admin_insert)), style = MaterialTheme.typography.headlineMedium)
        // Selskap
        Text(context.getString(R.string.app_admin_company), style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = selskap,
            onValueChange = { selskap = it },
            label = { Text(context.getString(R.string.app_admin_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Bil
        Text(context.getString(R.string.app_admin_car), style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = bilmodell,
            onValueChange = { bilmodell = it },
            label = { Text(context.getString(R.string.app_modell)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = versjon,
            onValueChange = { versjon = it },
            label = { Text(context.getString(R.string.app_versjon)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = rekkevidde,
            onValueChange = { rekkevidde = it },
            label = { Text(context.getString(R.string.app_range)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = batterikapasitet,
            onValueChange = { batterikapasitet = it },
            label = { Text(context.getString(R.string.app_battery_capacity)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = ladefart,
            onValueChange = { ladefart = it },
            label = { Text(context.getString(R.string.app_charge_speed)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Submit button
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                      scope.launch {
                          val companyName = selskap.text
                          val carModel = bilmodell.text
                          val version = versjon.text
                          val range = rekkevidde.text
                          val batteryCapacity = batterikapasitet.text
                          val chargingSpeed = ladefart.text

                          if (companyName.isNotEmpty() && carModel.isNotEmpty() && version.isNotEmpty() && range.isNotEmpty() && batteryCapacity.isNotEmpty() && chargingSpeed.isNotEmpty()) {
                              addCar(companyName, carModel, version, range, batteryCapacity, chargingSpeed)
                          } else if (companyName.isNotEmpty()) {
                              addCompany(companyName)
                          } else {
                              println("Not every field was filled in.")
                          }
                      }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(context.getString(R.string.app_add))
            }
        }
    }
}

// Function for adding company
suspend fun addCompany(companyName: String) {
        try {
            ElCarsApi.retrofitService.setCompany(companyName)
            println("Company '$companyName' added successfully.")
        } catch (e: Exception) {
            println("Error adding company: $e")
        }
    }


// Function for adding car model, version and specs
suspend fun addCar(
    company: String,
    carModel: String,
    modelVersion: String,
    range: String,
    batteryCapacity: String,
    chargingSpeed: String
) {
    val newSpecs = NewSpecs(
        company = company,
        carModel = carModel,
        modelVersion = modelVersion,
        range = range.toInt(),
        batteryCapacity = batteryCapacity.toInt(),
        chargingSpeed = chargingSpeed.toInt()
    )

    try {
        ElCarsApi.retrofitService.setCar(company, carModel, modelVersion, newSpecs)
        println("Model '$carModel' '$modelVersion' '$newSpecs' added successfully.")
    } catch (e: Exception) {
        println("Error adding car: $e")
    }
}