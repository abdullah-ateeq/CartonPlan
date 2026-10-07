package com.example.cartonplan

import com.example.cartonplan.data.CalculationJsonCodec
import com.example.cartonplan.engine.CartonPlanCalculator
import com.example.cartonplan.model.AppSettings
import com.example.cartonplan.model.CartonPlanInput
import com.example.cartonplan.model.SavedCalculation
import com.example.cartonplan.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class CartonPlanUnitTest {

    // 1. Exact division
    @Test
    fun testExactDivision() {
        val input = CartonPlanInput(
            totalItems = 200,
            itemsPerCarton = 40
        )
        val result = CartonPlanCalculator.calculate(input)

        assertEquals(200, result.planningQuantity)
        assertEquals(5, result.requiredCartons)
        assertEquals(5, result.fullCartons)
        assertEquals(0, result.partialCartons)
        assertEquals(40, result.finalCartonItems)
        assertEquals(200L, result.itemsPacked)
        assertEquals(0L, result.unusedCapacity)
        assertEquals(BigDecimal("100.00"), result.utilizationPercent)
        assertTrue(result.isFinalCartonFull)
    }

    // 2. Partial carton
    @Test
    fun testPartialCarton() {
        val input = CartonPlanInput(
            totalItems = 250,
            itemsPerCarton = 40
        )
        val result = CartonPlanCalculator.calculate(input)

        assertEquals(250, result.planningQuantity)
        assertEquals(7, result.requiredCartons)
        assertEquals(6, result.fullCartons)
        assertEquals(1, result.partialCartons)
        assertEquals(10, result.finalCartonItems)
        assertEquals(280L, result.itemsPacked)
        assertEquals(30L, result.unusedCapacity)
        assertEquals(BigDecimal("89.29"), result.utilizationPercent)
        assertFalse(result.isFinalCartonFull)
    }

    // 3. One item
    @Test
    fun testOneItem() {
        val input = CartonPlanInput(
            totalItems = 1,
            itemsPerCarton = 10
        )
        val result = CartonPlanCalculator.calculate(input)

        assertEquals(1, result.planningQuantity)
        assertEquals(1, result.requiredCartons)
        assertEquals(0, result.fullCartons)
        assertEquals(1, result.partialCartons)
        assertEquals(1, result.finalCartonItems)
        assertEquals(10L, result.itemsPacked)
        assertEquals(9L, result.unusedCapacity)
        assertEquals(BigDecimal("10.00"), result.utilizationPercent)
    }

    // 4. One item per carton
    @Test
    fun testOneItemPerCarton() {
        val input = CartonPlanInput(
            totalItems = 15,
            itemsPerCarton = 1
        )
        val result = CartonPlanCalculator.calculate(input)

        assertEquals(15, result.planningQuantity)
        assertEquals(15, result.requiredCartons)
        assertEquals(15, result.fullCartons)
        assertEquals(0, result.partialCartons)
        assertEquals(1, result.finalCartonItems)
        assertEquals(15L, result.itemsPacked)
        assertEquals(0L, result.unusedCapacity)
        assertEquals(BigDecimal("100.00"), result.utilizationPercent)
    }

    // 5. Large quantities
    @Test
    fun testLargeQuantities() {
        val input1000 = CartonPlanInput(
            totalItems = 1000,
            itemsPerCarton = 50
        )
        val result1000 = CartonPlanCalculator.calculate(input1000)
        assertEquals(20, result1000.requiredCartons)
        assertEquals(20, result1000.fullCartons)
        assertEquals(0, result1000.partialCartons)
        assertEquals(1000L, result1000.itemsPacked)
        assertEquals(0L, result1000.unusedCapacity)
        assertEquals(BigDecimal("100.00"), result1000.utilizationPercent)

        // Extreme bounds test: 10,000,000 items, 1,000,000 per carton
        val inputExtreme = CartonPlanInput(
            totalItems = 10_000_000,
            itemsPerCarton = 1_000_000
        )
        val resultExtreme = CartonPlanCalculator.calculate(inputExtreme)
        assertEquals(10, resultExtreme.requiredCartons)
        assertEquals(10_000_000L, resultExtreme.itemsPacked)
        assertEquals(0L, resultExtreme.unusedCapacity)
    }

    // 6. Full carton count
    @Test
    fun testFullCartonCount() {
        assertEquals(0, CartonPlanCalculator.calculateFullCartons(1, 10))
        assertEquals(1, CartonPlanCalculator.calculateFullCartons(10, 10))
        assertEquals(1, CartonPlanCalculator.calculateFullCartons(11, 10))
        assertEquals(2, CartonPlanCalculator.calculateFullCartons(20, 10))
        assertEquals(6, CartonPlanCalculator.calculateFullCartons(250, 40))
        assertEquals(5, CartonPlanCalculator.calculateFullCartons(200, 40))
    }

    // 7. Partial carton count
    @Test
    fun testPartialCartonCount() {
        assertEquals(0, CartonPlanCalculator.calculatePartialCartonCount(200, 40))
        assertEquals(1, CartonPlanCalculator.calculatePartialCartonCount(250, 40))
        assertEquals(1, CartonPlanCalculator.calculatePartialCartonCount(1, 10))
        assertEquals(0, CartonPlanCalculator.calculatePartialCartonCount(10, 10))
    }

    // 8. Final carton quantity
    @Test
    fun testFinalCartonQuantity() {
        assertEquals(40, CartonPlanCalculator.calculateFinalCartonItems(200, 40))
        assertEquals(10, CartonPlanCalculator.calculateFinalCartonItems(250, 40))
        assertEquals(1, CartonPlanCalculator.calculateFinalCartonItems(1, 10))
        assertEquals(10, CartonPlanCalculator.calculateFinalCartonItems(10, 10))
    }

    // 9. Items packed
    @Test
    fun testItemsPacked() {
        assertEquals(280L, CartonPlanCalculator.calculateItemsPacked(7, 40))
        assertEquals(200L, CartonPlanCalculator.calculateItemsPacked(5, 40))
        assertEquals(10_000_000L, CartonPlanCalculator.calculateItemsPacked(10, 1_000_000))
    }

    // 10. Unused capacity
    @Test
    fun testUnusedCapacity() {
        assertEquals(30L, CartonPlanCalculator.calculateUnusedCapacity(280L, 250))
        assertEquals(0L, CartonPlanCalculator.calculateUnusedCapacity(200L, 200))
        assertEquals(9L, CartonPlanCalculator.calculateUnusedCapacity(10L, 1))
    }

    // 11. Utilization
    @Test
    fun testUtilization() {
        assertEquals(BigDecimal("89.29"), CartonPlanCalculator.calculateUtilization(250, 280L, 2))
        assertEquals(BigDecimal("100.00"), CartonPlanCalculator.calculateUtilization(200, 200L, 2))
        assertEquals(BigDecimal("10.00"), CartonPlanCalculator.calculateUtilization(1, 10L, 2))
    }

    // 12. Extra cartons
    @Test
    fun testExtraCartons() {
        val input = CartonPlanInput(
            totalItems = 250,
            itemsPerCarton = 40,
            extraCartons = 2,
            includeExtraCartons = true
        )
        val result = CartonPlanCalculator.calculate(input)
        assertEquals(7, result.requiredCartons)
        assertEquals(9, result.plannedCartons)
    }

    // 13. Planning allowance
    @Test
    fun testPlanningAllowance() {
        val input = CartonPlanInput(
            totalItems = 250,
            itemsPerCarton = 40,
            planningAllowancePercent = BigDecimal("10"),
            includePlanningAllowance = true
        )
        val result = CartonPlanCalculator.calculate(input)

        assertEquals(275, result.planningQuantity)
        assertEquals(7, result.requiredCartons)
        assertEquals(6, result.fullCartons)
        assertEquals(1, result.partialCartons)
        assertEquals(35, result.finalCartonItems)
        assertEquals(280L, result.itemsPacked)
        assertEquals(5L, result.unusedCapacity)
        assertEquals(BigDecimal("98.21"), result.utilizationPercent)
    }

    // 14. Zero allowance
    @Test
    fun testZeroAllowance() {
        val input = CartonPlanInput(
            totalItems = 250,
            itemsPerCarton = 40,
            planningAllowancePercent = BigDecimal.ZERO,
            includePlanningAllowance = true
        )
        val result = CartonPlanCalculator.calculate(input)
        assertEquals(250, result.planningQuantity)
        assertEquals(7, result.requiredCartons)
    }

    // 15. Decimal allowance
    @Test
    fun testDecimalAllowance() {
        val planningQty = CartonPlanCalculator.calculatePlanningQuantity(
            totalItems = 250,
            allowancePercent = BigDecimal("5.5"),
            enabled = true
        )
        // 250 * 1.055 = 263.75 -> ceiling = 264
        assertEquals(264, planningQty)
    }

    // 16. Invalid total items
    @Test
    fun testInvalidTotalItems() {
        assertTrue(CartonPlanCalculator.validateTotalItems("250") is CartonPlanCalculator.ValidationResult.Valid)

        val invalid0 = CartonPlanCalculator.validateTotalItems("0")
        assertTrue(invalid0 is CartonPlanCalculator.ValidationResult.Invalid)
        assertEquals("Enter a whole number from 1 to 10,000,000.", (invalid0 as CartonPlanCalculator.ValidationResult.Invalid).message)

        val invalidNegative = CartonPlanCalculator.validateTotalItems("-5")
        assertTrue(invalidNegative is CartonPlanCalculator.ValidationResult.Invalid)

        val invalidOver = CartonPlanCalculator.validateTotalItems("10000001")
        assertTrue(invalidOver is CartonPlanCalculator.ValidationResult.Invalid)

        val invalidEmpty = CartonPlanCalculator.validateTotalItems("")
        assertTrue(invalidEmpty is CartonPlanCalculator.ValidationResult.Invalid)
    }

    // 17. Invalid items per carton
    @Test
    fun testInvalidItemsPerCarton() {
        assertTrue(CartonPlanCalculator.validateItemsPerCarton("40") is CartonPlanCalculator.ValidationResult.Valid)

        val invalid0 = CartonPlanCalculator.validateItemsPerCarton("0")
        assertTrue(invalid0 is CartonPlanCalculator.ValidationResult.Invalid)
        assertEquals("Enter a whole number from 1 to 1,000,000.", (invalid0 as CartonPlanCalculator.ValidationResult.Invalid).message)

        val invalidOver = CartonPlanCalculator.validateItemsPerCarton("1000001")
        assertTrue(invalidOver is CartonPlanCalculator.ValidationResult.Invalid)
    }

    // 18. Invalid allowance
    @Test
    fun testInvalidAllowance() {
        assertTrue(CartonPlanCalculator.validateAllowance("10") is CartonPlanCalculator.ValidationResult.Valid)
        assertTrue(CartonPlanCalculator.validateAllowance("0") is CartonPlanCalculator.ValidationResult.Valid)
        assertTrue(CartonPlanCalculator.validateAllowance("50") is CartonPlanCalculator.ValidationResult.Valid)
        assertTrue(CartonPlanCalculator.validateAllowance("12.5") is CartonPlanCalculator.ValidationResult.Valid)

        val invalidOver = CartonPlanCalculator.validateAllowance("50.1")
        assertTrue(invalidOver is CartonPlanCalculator.ValidationResult.Invalid)
        assertEquals("Allowance must be between 0% and 50%.", (invalidOver as CartonPlanCalculator.ValidationResult.Invalid).message)

        val invalidUnder = CartonPlanCalculator.validateAllowance("-1")
        assertTrue(invalidUnder is CartonPlanCalculator.ValidationResult.Invalid)
    }

    // 19. Invalid extra cartons
    @Test
    fun testInvalidExtraCartons() {
        assertTrue(CartonPlanCalculator.validateExtraCartons("0") is CartonPlanCalculator.ValidationResult.Valid)
        assertTrue(CartonPlanCalculator.validateExtraCartons("100") is CartonPlanCalculator.ValidationResult.Valid)

        val invalidOver = CartonPlanCalculator.validateExtraCartons("101")
        assertTrue(invalidOver is CartonPlanCalculator.ValidationResult.Invalid)
        assertEquals("Extra cartons must be between 0 and 100.", (invalidOver as CartonPlanCalculator.ValidationResult.Invalid).message)

        val invalidUnder = CartonPlanCalculator.validateExtraCartons("-1")
        assertTrue(invalidUnder is CartonPlanCalculator.ValidationResult.Invalid)
    }

    // 20. BigDecimal precision
    @Test
    fun testBigDecimalPrecision() {
        val util0 = CartonPlanCalculator.calculateUtilization(250, 280L, 0)
        assertEquals(BigDecimal("89"), util0)

        val util1 = CartonPlanCalculator.calculateUtilization(250, 280L, 1)
        assertEquals(BigDecimal("89.3"), util1)

        val util2 = CartonPlanCalculator.calculateUtilization(250, 280L, 2)
        assertEquals(BigDecimal("89.29"), util2)
    }

    // 21. History serialization and deserialization
    @Test
    fun testHistorySerializationAndDeserialization() {
        val originalList = listOf(
            SavedCalculation(
                id = "calc-1",
                timestamp = 1696000000000L,
                projectName = "Shipping Batch",
                totalItems = 250,
                itemsPerCarton = 40,
                includePlanningAllowance = true,
                planningAllowancePercent = "10",
                planningQuantity = 275,
                extraCartons = 2,
                includeExtraCartons = true,
                requiredCartons = 7,
                plannedCartons = 9,
                fullCartons = 6,
                partialCartons = 1,
                finalCartonItems = 35,
                itemsPacked = 280L,
                unusedCapacity = 5L,
                utilizationPercent = "98.21",
                isFavorite = true
            ),
            SavedCalculation(
                id = "calc-2",
                timestamp = 1696000500000L,
                projectName = "Furniture Orders",
                totalItems = 1000,
                itemsPerCarton = 50,
                includePlanningAllowance = false,
                planningAllowancePercent = "0",
                planningQuantity = 1000,
                extraCartons = 0,
                includeExtraCartons = false,
                requiredCartons = 20,
                plannedCartons = 20,
                fullCartons = 20,
                partialCartons = 0,
                finalCartonItems = 50,
                itemsPacked = 1000L,
                unusedCapacity = 0L,
                utilizationPercent = "100.00",
                isFavorite = false
            )
        )

        val json = CalculationJsonCodec.listToJson(originalList)
        assertNotNull(json)
        assertTrue(json.startsWith("["))
        assertTrue(json.endsWith("]"))

        val deserialized = CalculationJsonCodec.listFromJson(json)
        assertEquals(2, deserialized.size)
        assertEquals("Shipping Batch", deserialized[0].projectName)
        assertEquals(250, deserialized[0].totalItems)
        assertEquals(40, deserialized[0].itemsPerCarton)
        assertTrue(deserialized[0].isFavorite)
        assertEquals("Furniture Orders", deserialized[1].projectName)
        assertFalse(deserialized[1].isFavorite)
    }

    // 22. Settings persistence defaults
    @Test
    fun testSettingsDefaults() {
        val defaultSettings = AppSettings()
        assertEquals(ThemeMode.SYSTEM, defaultSettings.themeMode)
        assertTrue(defaultSettings.dynamicColor)
        assertEquals(250, defaultSettings.defaultItems)
        assertEquals(40, defaultSettings.defaultItemsPerCarton)
        assertEquals("0", defaultSettings.defaultPlanningAllowance)
        assertEquals(0, defaultSettings.defaultExtraCartons)
        assertEquals(2, defaultSettings.decimalPlaces)
        assertTrue(defaultSettings.showDiagram)
        assertTrue(defaultSettings.showFormula)
        assertTrue(defaultSettings.showCartonBreakdown)
        assertTrue(defaultSettings.showUtilization)
        assertTrue(defaultSettings.showPlanningAllowance)
        assertTrue(defaultSettings.hapticFeedback)
        assertTrue(defaultSettings.confirmDelete)
    }

    // 23. Favorites priority sorting
    @Test
    fun testFavoritesSorting() {
        val item1 = SavedCalculation(
            id = "1",
            timestamp = 1000L,
            projectName = "Batch A",
            totalItems = 100,
            itemsPerCarton = 10,
            includePlanningAllowance = false,
            planningAllowancePercent = "0",
            planningQuantity = 100,
            extraCartons = 0,
            includeExtraCartons = false,
            requiredCartons = 10,
            plannedCartons = 10,
            fullCartons = 10,
            partialCartons = 0,
            finalCartonItems = 10,
            itemsPacked = 100L,
            unusedCapacity = 0L,
            utilizationPercent = "100.00",
            isFavorite = false
        )

        val item2 = item1.copy(id = "2", timestamp = 2000L, projectName = "Batch B", isFavorite = true)
        val item3 = item1.copy(id = "3", timestamp = 3000L, projectName = "Batch C", isFavorite = false)

        val list = listOf(item1, item2, item3)
        val sorted = list.sortedWith(
            compareByDescending<SavedCalculation> { it.isFavorite }
                .thenByDescending { it.timestamp }
        )

        assertEquals("Batch B", sorted[0].projectName) // Favorite first
        assertEquals("Batch C", sorted[1].projectName) // Non-favorite, newer
        assertEquals("Batch A", sorted[2].projectName) // Non-favorite, older
    }
}
