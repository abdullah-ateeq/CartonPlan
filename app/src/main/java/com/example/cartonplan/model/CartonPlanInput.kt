package com.example.cartonplan.model

import java.math.BigDecimal

/**
 * User inputs for a carton calculation.
 */
data class CartonPlanInput(
    val projectName: String = "",
    val totalItems: Int = 250,
    val itemsPerCarton: Int = 40,
    val planningAllowancePercent: BigDecimal = BigDecimal.ZERO,
    val includePlanningAllowance: Boolean = false,
    val extraCartons: Int = 0,
    val includeExtraCartons: Boolean = false
)
