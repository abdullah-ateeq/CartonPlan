package com.example.cartonplan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cartonplan.navigation.AppNavigation
import com.example.cartonplan.ui.CartonPlanViewModel
import com.example.cartonplan.ui.theme.CartonPlanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CartonPlanViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()

            CartonPlanTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}