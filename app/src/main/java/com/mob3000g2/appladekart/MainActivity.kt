package com.mob3000g2.appladekart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.mob3000g2.appladekart.ui.theme.AppLadeKartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppLadeKartTheme {
                Surface {
                    AppLadeKart()
                }
            }
        }
    }
}