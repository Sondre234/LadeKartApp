package com.mob3000g2.appladekart.ui.theme

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mob3000g2.appladekart.AppLadeKartScreen
import com.mob3000g2.appladekart.R
import com.mob3000g2.appladekart.model.AdminAccount
import com.mob3000g2.appladekart.network.ElCarsApi.retrofitService
import kotlinx.coroutines.launch


/*val density = LocalDensity.current.density
val xPixel = 700
val xDp = (xPixel / density).dp

Column (modifier = Modifier
.width(xDp)
.verticalScroll(rememberScrollState())
.background(Color.White)
.padding(20.dp, 65.dp, 20.dp, 20.dp)
,

horizontalAlignment = Alignment.CenterHorizontally,
verticalArrangement = Arrangement.Center
) {

}*/

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AdminLogin(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }

    val icon = if (passwordVisibility)
        painterResource(id = com.google.android.material.R.drawable.design_ic_visibility)
    else
        painterResource(id = com.google.android.material.R.drawable.design_ic_visibility_off)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = (context.getString(R.string.app_login)))})
        },
        content = {
            Column(
                /*modifier = Modifier
                    .width(400.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp, 65.dp, 20.dp, 20.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally*/
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp, 65.dp, 20.dp, 20.dp)
                    .verticalScroll(rememberScrollState())
                    .width(400.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    singleLine = true,
                    label = { Text(context.getString(R.string.app_admin_email)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                /*
                 * Code pattern for password visibilty icon is based on code from this video
                 * https://www.youtube.com/watch?v=eNAhOqF83Kg
                 */
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    label = { Text(context.getString(R.string.app_admin_password)) },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),

                    ) {


                    Button(

                        onClick = {
                            scope.launch {
                                val token = loginAdmin(email, password)
                                if (token) {
                                    navController.navigate(AppLadeKartScreen.Admin.name)
                                } else {
                                    Log.e("AdminLogin", "Invalid credentials")
                                    errorMessage = (context.getString(R.string.app_admin_login_error))
                                }
                            }
                        }
                    ) {
                        Text(context.getString(R.string.app_login))
                    }
                }
                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = Color.Red)
                }
            }
        }
    )
}

// Function for logon to admin pages
suspend fun loginAdmin(email: String, password: String): Boolean {
    try {
        val loginCredentials = AdminAccount(email, password)
        val response = retrofitService.adminLogin(loginCredentials)
        println("Response code: " + response.code())

        // If response is success
        return if (response.code() == 200) {
            true
        }
        else {
            // If invalid username or password
            if (response.code() == 401) {
                println("Invalid username or password")
                false
            } else {
                false
            }
            false
        }
    } catch (e: Exception) {
        println("Error logging in admin account: $e")
        return false
    }
}