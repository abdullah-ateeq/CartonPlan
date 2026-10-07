package com.example.cartonplan.engine

import com.example.cartonplan.model.CartonPlanInput
import com.example.cartonplan.model.CartonPlanResult
import java.math.BigDecimal
import java.math.RoundingMode

object CartonPlanCalculator {

    const val MIN_TOTAL_ITEMS = 1
    const val MAX_TOTAL_ITEMS = 10_000_000

    const val MIN_ITEMS_PER_CARTON = 1
    const val MAX_ITEMS_PER_CARTON = 1_000_000

    val MIN_ALLOWANCE: BigDecimal = BigDecimal.ZERO
    val MAX_ALLOWANCE: BigDecimal = BigDecimal("50")

    const val MIN_EXTRA_CARTONS = 0
    const val MAX_EXTRA_CARTONS = 100

    fun calculatePlanningQuantity(
        totalItems: Int,
        allowancePercent: BigDecimal,
        enabled: Boolean
    ): Int {
        if (!enabled || totalItems <= 0) return totalItems
        if (allowancePercent.compareTo(BigDecimal.ZERO) <= 0) return totalItems

        // planningQuantity = ceiling(totalItems × (1 + allowancePercent / 100))
        val totalBD = BigDecimal(totalItems)
        val factor = BigDecimal.ONE.add(
            allowancePercent.divide(BigDecimal("100"), 10, RoundingMode.HALF_UP)
        )
        val rawQuantity = totalBD.multiply(factor)
        return rawQuantity.setScale(0, RoundingMode.CEILING).intValueExact()
    }

    fun calculateRequiredCartons(
        planningQuantity: Int,
        itemsPerCarton: Int
    ): Int {
        if (planningQuantity <= 0 || itemsPerCarton <= 0) return 0
        // Ceiling integer division: (planningQuantity + itemsPerCarton - 1) / itemsPerCarton
        val numerator = planningQuantity.toLong() + itemsPerCarton.toLong() - 1L
        return (numerator / itemsPerCarton.toLong()).toInt()
    }

    fun calculateFullCartons(
        planningQuantity: Int,
        itemsPerCarton: Int
    ): Int {
        if (planningQuantity <= 0 || itemsPerCarton <= 0) return 0
        val required = calculateRequiredCartons(planningQuantity, itemsPerCarton)
        return if (planningQuantity % itemsPerCarton == 0) {
            required
        } else {
            (required - 1).coerceAtLeast(0)
        }
    }

    fun calculateFinalCartonItems(
        planningQuantity: Int,
        itemsPerCarton: Int
    ): Int {
        if (planningQuantity <= 0 || itemsPerCarton <= 0) return 0
        val remainder = planningQuantity % itemsPerCarton
        return if (remainder == 0) {
            itemsPerCarton
        } else {
            remainder
        }
    }

    fun calculatePartialCartonCount(
        planningQuantity: Int,
        itemsPerCarton: Int
    ): Int {
        if (planningQuantity <= 0 || itemsPerCarton <= 0) return 0
        return if (planningQuantity % itemsPerCarton == 0) 0 else 1
    }

    fun calculateItemsPacked(
        requiredCartons: Int,
        itemsPerCarton: Int
    ): Long {
        if (requiredCartons <= 0 || itemsPerCarton <= 0) return 0L
        return requiredCartons.toLong() * itemsPerCarton.toLong()
    }

    fun calculateUnusedCapacity(
        itemsPacked: Long,
        planningQuantity: Int
    ): Long {
        if (itemsPacked <= 0L) return 0L
        return (itemsPacked - planningQuantity.toLong()).coerceAtLeast(0L)
    }

    fun calculateUtilization(
        planningQuantity: Int,
        itemsPacked: Long,
        scale: Int = 2
    ): BigDecimal {
        if (itemsPacked <= 0L || planningQuantity <= 0) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP)
        }
        val safeScale = scale.coerceIn(0, 4)
        return BigDecimal(planningQuantity)
            .multiply(BigDecimal("100"))
            .divide(BigDecimal(itemsPacked), safeScale + 4, RoundingMode.HALF_UP)
            .setScale(safeScale, RoundingMode.HALF_UP)
    }

    fun calculateFinalCartonFillPercent(
        finalCartonItems: Int,
        itemsPerCarton: Int,
        scale: Int = 2
    ): BigDecimal {
        if (itemsPerCarton <= 0 || finalCartonItems <= 0) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP)
        }
        val safeScale = scale.coerceIn(0, 4)
        return BigDecimal(finalCartonItems)
            .multiply(BigDecimal("100"))
            .divide(BigDecimal(itemsPerCarton), safeScale + 4, RoundingMode.HALF_UP)
            .setScale(safeScale, RoundingMode.HALF_UP)
    }

    fun calculatePlannedCartons(
        requiredCartons: Int,
        extraCartons: Int
    ): Int {
        val extra = extraCartons.coerceAtLeast(0)
        return requiredCartons + extra
    }

    fun calculate(
        input: CartonPlanInput,
        decimalPlaces: Int = 2
    ): CartonPlanResult {
        val planningQuantity = calculatePlanningQuantity(
            totalItems = input.totalItems,
            allowancePercent = input.planningAllowancePercent,
            enabled = input.includePlanningAllowance
        )

        val requiredCartons = calculateRequiredCartons(
            planningQuantity = planningQuantity,
            itemsPerCarton = input.itemsPerCarton
        )

        val extra = if (input.includeExtraCartons) input.extraCartons else 0
        val plannedCartons = calculatePlannedCartons(requiredCartons, extra)

        val fullCartons = calculateFullCartons(
            planningQuantity = planningQuantity,
            itemsPerCarton = input.itemsPerCarton
        )

        val partialCartons = calculatePartialCartonCount(
            planningQuantity = planningQuantity,
            itemsPerCarton = input.itemsPerCarton
        )

        val finalCartonItems = calculateFinalCartonItems(
            planningQuantity = planningQuantity,
            itemsPerCarton = input.itemsPerCarton
        )

        val itemsPacked = calculateItemsPacked(
            requiredCartons = requiredCartons,
            itemsPerCarton = input.itemsPerCarton
        )

        val unusedCapacity = calculateUnusedCapacity(
            itemsPacked = itemsPacked,
            planningQuantity = planningQuantity
        )

        val utilization = calculateUtilization(
            planningQuantity = planningQuantity,
            itemsPacked = itemsPacked,
            scale = decimalPlaces
        )

        val finalFill = calculateFinalCartonFillPercent(
            finalCartonItems = finalCartonItems,
            itemsPerCarton = input.itemsPerCarton,
            scale = decimalPlaces
        )

        val isFinalFull = partialCartons == 0 && requiredCartons > 0

        return CartonPlanResult(
            planningQuantity = planningQuantity,
            requiredCartons = requiredCartons,
            plannedCartons = plannedCartons,
            fullCartons = fullCartons,
            partialCartons = partialCartons,
            finalCartonItems = finalCartonItems,
            itemsPacked = itemsPacked,
            unusedCapacity = unusedCapacity,
            utilizationPercent = utilization,
            finalCartonFillPercent = finalFill,
            isFinalCartonFull = isFinalFull
        )
    }

    // Validation functions
    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val message: String) : ValidationResult()
    }

    fun validateTotalItems(value: String): ValidationResult {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Enter total items.")
        }
        val intValue = trimmed.toIntOrNull()
        if (intValue == null || intValue < MIN_TOTAL_ITEMS || intValue > MAX_TOTAL_ITEMS) {
            return ValidationResult.Invalid("Enter a whole number from 1 to 10,000,000.")
        }
        return ValidationResult.Valid
    }

    fun validateItemsPerCarton(value: String): ValidationResult {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Enter items per carton.")
        }
        val intValue = trimmed.toIntOrNull()
        if (intValue == null || intValue < MIN_ITEMS_PER_CARTON || intValue > MAX_ITEMS_PER_CARTON) {
            return ValidationResult.Invalid("Enter a whole number from 1 to 1,000,000.")
        }
        return ValidationResult.Valid
    }

    fun validateAllowance(value: String): ValidationResult {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Enter planning allowance.")
        }
        val bd = try {
            BigDecimal(trimmed)
        } catch (_: Exception) {
            null
        }
        if (bd == null || bd < MIN_ALLOWANCE || bd > MAX_ALLOWANCE) {
            return ValidationResult.Invalid("Allowance must be between 0% and 50%.")
        }
        return ValidationResult.Valid
    }

    fun validateExtraCartons(value: String): ValidationResult {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Valid // Treated as 0
        }
        val intValue = trimmed.toIntOrNull()
        if (intValue == null || intValue < MIN_EXTRA_CARTONS || intValue > MAX_EXTRA_CARTONS) {
            return ValidationResult.Invalid("Extra cartons must be between 0 and 100.")
        }
        return ValidationResult.Valid
    }
}
