package com.example.cartonplan.model

import java.math.BigDecimal

data class SavedCalculation(
    val id: String,
    val timestamp: Long,
    val projectName: String,
    val totalItems: Int,
    val itemsPerCarton: Int,
    val includePlanningAllowance: Boolean,
    val planningAllowancePercent: String,
    val planningQuantity: Int,
    val extraCartons: Int,
    val includeExtraCartons: Boolean,
    val requiredCartons: Int,
    val plannedCartons: Int,
    val fullCartons: Int,
    val partialCartons: Int,
    val finalCartonItems: Int,
    val itemsPacked: Long,
    val unusedCapacity: Long,
    val utilizationPercent: String,
    val isFavorite: Boolean = false
) {
    fun toInput(): CartonPlanInput {
        val allowance = try {
            BigDecimal(planningAllowancePercent)
        } catch (_: Exception) {
            BigDecimal.ZERO
        }
        return CartonPlanInput(
            projectName = projectName,
            totalItems = totalItems,
            itemsPerCarton = itemsPerCarton,
            planningAllowancePercent = allowance,
            includePlanningAllowance = includePlanningAllowance,
            extraCartons = extraCartons,
            includeExtraCartons = includeExtraCartons
        )
    }
}
