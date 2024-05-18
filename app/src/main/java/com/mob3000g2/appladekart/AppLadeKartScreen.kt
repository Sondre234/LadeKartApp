package com.mob3000g2.appladekart

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mob3000g2.appladekart.ui.theme.Admin
import com.mob3000g2.appladekart.ui.theme.AdminLogin
import com.mob3000g2.appladekart.ui.theme.CreateAdmin
import com.mob3000g2.appladekart.ui.theme.Delete
import com.mob3000g2.appladekart.ui.theme.Insert
import com.mob3000g2.appladekart.ui.theme.MapMenu
import com.mob3000g2.appladekart.ui.theme.MapScreen
import com.mob3000g2.appladekart.ui.theme.Update

/**
 * enum values that represent the screens in the app
 */
enum class AppLadeKartScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Login(title = R.string.app_admin_login),
    Admin(title = R.string.app_admin_terminal),
    Insert(title = R.string.app_admin_insert),
    Update(title = R.string.app_admin_update),
    Delete(title = R.string.app_admin_delete),
    Create(title = R.string.app_admin_create)
}

@Composable
fun AppLadeKart(
    navController: NavHostController = rememberNavController()
) {

    Scaffold() {
            innerPadding ->

        NavHost(
            navController = navController,
            startDestination = AppLadeKartScreen.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = AppLadeKartScreen.Start.name) {
                // add both Map and settings
                MapScreen(
                    modifier = Modifier.fillMaxSize()
                )
                MapMenu()
                AdminButton(navController = navController)
            }
            composable(route = AppLadeKartScreen.Login.name) {
                AdminLogin(navController = navController)
            }
             composable(route = AppLadeKartScreen.Admin.name) {
                 Admin(navController = navController)
             }
             composable(route = AppLadeKartScreen.Insert.name) {
                 Insert(navController = navController)
             }
             composable(route = AppLadeKartScreen.Update.name) {
                 Update(navController = navController)
             }
             composable(route = AppLadeKartScreen.Delete.name) {
                 Delete(navController = navController)
             }
             composable(route = AppLadeKartScreen.Create.name) {
                 CreateAdmin(navController = navController)
             }
        }
    }

}
@Composable
fun AdminButton(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 16.dp, top = 16.dp), // Adjust the top and end (right) padding as needed
        contentAlignment = Alignment.TopEnd // Align the content to the top right corner
    ) {
        
        Button(onClick = {
            
            navController.navigate(AppLadeKartScreen.Login.name)
        },
            
        modifier = Modifier
            .padding(end = 16.dp)
            .wrapContentWidth(Alignment.End)    ) {
            
            Text(text = "Admin")
        }

        }
    }
