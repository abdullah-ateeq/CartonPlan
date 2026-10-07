package com.example.cartonplan.model

import java.math.BigDecimal

/**
 * Calculated outputs for a carton plan.
 */
data class CartonPlanResult(
    val planningQuantity: Int,
    val requiredCartons: Int,
    val plannedCartons: Int,
    val fullCartons: Int,
    val partialCartons: Int,
    val finalCartonItems: Int,
    val itemsPacked: Long,
    val unusedCapacity: Long,
    val utilizationPercent: BigDecimal,
    val finalCartonFillPercent: BigDecimal,
    val isFinalCartonFull: Boolean
)
