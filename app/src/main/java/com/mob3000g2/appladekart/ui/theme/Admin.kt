package com.mob3000g2.appladekart.ui.theme

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.mob3000g2.appladekart.AppLadeKartScreen
import com.mob3000g2.appladekart.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Admin(navController: NavHostController) {
    BottomMenuBar(navController = navController)
    AdminContent()
}

@Composable
fun BottomMenuBar(navController: NavHostController) {
    val context = LocalContext.current
    Scaffold(
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = { navController.navigate(AppLadeKartScreen.Start.name) }) {
                        Icon(Icons.Default.LocationOn, contentDescription = context.getString(R.string.app_name))
                    }
                    IconButton(onClick = { navController.navigate(AppLadeKartScreen.Admin.name) }) {
                        Icon(Icons.Default.Home, contentDescription = context.getString(R.string.app_admin_terminal))
                    }
                    IconButton(onClick = { navController.navigate(AppLadeKartScreen.Insert.name) }) {
                        Icon(Icons.Default.AddCircle, contentDescription = context.getString(R.string.app_admin_insert))
                    }
                    IconButton(onClick = { navController.navigate(AppLadeKartScreen.Update.name) }) {
                        Icon(Icons.Default.Edit, contentDescription = context.getString(R.string.app_admin_update))
                    }
                    IconButton(onClick = { navController.navigate(AppLadeKartScreen.Delete.name) }) {
                        Icon(Icons.Default.Delete, contentDescription = context.getString(R.string.app_admin_delete))
                    }
                    IconButton(onClick = { navController.navigate(AppLadeKartScreen.Create.name) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = context.getString(R.string.app_admin_create))
                    }
                }
            )
        },
    ) {

    }
}

@Composable
fun AdminContent() {
    val context = LocalContext.current
    Text(text = (context.getString(R.string.app_admin_welcome)), style = MaterialTheme.typography.headlineMedium)
}




