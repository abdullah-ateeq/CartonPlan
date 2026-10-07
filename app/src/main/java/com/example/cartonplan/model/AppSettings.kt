package com.example.cartonplan.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultItems: Int = 250,
    val defaultItemsPerCarton: Int = 40,
    val defaultPlanningAllowance: String = "0",
    val defaultExtraCartons: Int = 0,
    val decimalPlaces: Int = 2,
    val showDiagram: Boolean = true,
    val showFormula: Boolean = true,
    val showCartonBreakdown: Boolean = true,
    val showUtilization: Boolean = true,
    val showPlanningAllowance: Boolean = true,
    val hapticFeedback: Boolean = true,
    val confirmDelete: Boolean = true
)
