package com.mob3000g2.appladekart.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mob3000g2.appladekart.R
import com.mob3000g2.appladekart.model.CreateAdminAccount
import com.mob3000g2.appladekart.network.ElCarsApi.retrofitService
import kotlinx.coroutines.launch
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAdmin(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var isSuperAdmin by rememberSaveable { mutableStateOf(false) }
    var createdMessage by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    val icon = if (passwordVisibility)
        painterResource(id = com.google.android.material.R.drawable.design_ic_visibility)
    else
        painterResource(id = com.google.android.material.R.drawable.design_ic_visibility_off)

    BottomMenuBar(navController = navController)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(400.dp)
            .padding(bottom = 80.dp)
            .verticalScroll(rememberScrollState())
            .padding(20.dp, 65.dp, 20.dp, 20.dp)
            .fillMaxSize()
    ) {
        Text(text = (context.getString(R.string.app_admin_create)), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(context.getString(R.string.app_admin_email)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        /*
         * Code pattern for password visibilty icon is based on code from this video
         * https://www.youtube.com/watch?v=eNAhOqF83Kg
         */
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(context.getString(R.string.app_admin_password)) },
            singleLine = true,
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            trailingIcon = {
                           IconButton(onClick = {
                               passwordVisibility = !passwordVisibility
                           }) {
                               Icon (
                                   painter = icon,
                                   contentDescription = context.getString(R.string.app_admin_password)
                               )

                           }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isSuperAdmin,
                onCheckedChange = { isSuperAdmin = !isSuperAdmin }
            )

            Text(
                text = (context.getString(R.string.app_admin_super)),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Running createAdminAccount when clicking button
                scope.launch {
                    val response = createAdminAccount(email, password, isSuperAdmin)
                    if (response.isSuccessful) {
                        createdMessage = (context.getString(R.string.app_admin_create_success))
                    } else {
                        createdMessage = (context.getString(R.string.app_admin_create_error))
                    }
                }
                      },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(context.getString(R.string.app_admin_create_update))
        }
        if (createdMessage.isNotEmpty()) {
            Text(text = createdMessage)
        }
    }
}

// Function for creating admin account
suspend fun createAdminAccount(email: String, password: String, isSuperAdmin: Boolean): Response<Boolean> {
    try {
        val adminAccount = CreateAdminAccount(email, password, isSuperAdmin)
        return retrofitService.createAdminAccount(adminAccount)
    } catch (e: Exception) {
        throw e
    }
}