package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val shortLabel: String
) {
    data object Home : Screen("home", "Home & Browser", Icons.Default.Home, "Home")
    data object LiveData : Screen("live_data", "Live Telemetry", Icons.Default.FlightTakeoff, "Live Feed")
    data object History : Screen("history", "Round History", Icons.Default.History, "History")
    data object Analytics : Screen("analytics", "Statistical Analytics", Icons.Default.Analytics, "Analytics")
    data object Prediction : Screen("prediction", "Range Estimator", Icons.Default.Calculate, "Predict")
    data object Backtest : Screen("backtest", "Model Backtesting", Icons.Default.QueryStats, "Backtest")
    data object Browser : Screen("browser", "Browser", Icons.Default.Public, "Browser")

    companion object {
        val bottomNavItems: List<Screen>
            get() = listOf(
                Home,
                LiveData,
                History,
                Analytics,
                Prediction
            )
    }
}
