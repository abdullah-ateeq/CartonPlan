package com.example.cartonplan.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartonplan.data.CartonPlanDataStore
import com.example.cartonplan.engine.CartonPlanCalculator
import com.example.cartonplan.model.AppSettings
import com.example.cartonplan.model.CartonPlanInput
import com.example.cartonplan.model.CartonPlanResult
import com.example.cartonplan.model.SavedCalculation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CalculatorUiState(
    val projectName: String = "",
    val totalItemsText: String = "250",
    val itemsPerCartonText: String = "40",
    val includePlanningAllowance: Boolean = false,
    val planningAllowanceText: String = "10",
    val includeExtraCartons: Boolean = false,
    val extraCartonsText: String = "0",
    val totalItemsError: String? = null,
    val itemsPerCartonError: String? = null,
    val allowanceError: String? = null,
    val extraCartonsError: String? = null,
    val result: CartonPlanResult? = null,
    val isValid: Boolean = true
)

class CartonPlanViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = CartonPlanDataStore(application)

    val settings: StateFlow<AppSettings> = dataStore.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val rawSavedCalculations: StateFlow<List<SavedCalculation>> = dataStore.savedCalculationsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredSavedCalculations: StateFlow<List<SavedCalculation>> = combine(
        rawSavedCalculations,
        _searchQuery
    ) { calculations, query ->
        val trimmedQuery = query.trim().lowercase(Locale.ROOT)
        val filtered = if (trimmedQuery.isEmpty()) {
            calculations
        } else {
            calculations.filter { it.projectName.lowercase(Locale.ROOT).contains(trimmedQuery) }
        }
        // Favorites appear first, then sorted by timestamp descending
        filtered.sortedWith(
            compareByDescending<SavedCalculation> { it.isFavorite }
                .thenByDescending { it.timestamp }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        // Initial calculation with default values
        recalculate(_uiState.value)
    }

    fun onProjectNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(projectName = name)
    }

    fun onTotalItemsChanged(text: String) {
        val newState = _uiState.value.copy(totalItemsText = text)
        validateAndRecalculate(newState)
    }

    fun onItemsPerCartonChanged(text: String) {
        val newState = _uiState.value.copy(itemsPerCartonText = text)
        validateAndRecalculate(newState)
    }

    fun onIncludePlanningAllowanceToggled(enabled: Boolean) {
        val newState = _uiState.value.copy(includePlanningAllowance = enabled)
        validateAndRecalculate(newState)
    }

    fun onPlanningAllowanceChanged(text: String) {
        val newState = _uiState.value.copy(planningAllowanceText = text)
        validateAndRecalculate(newState)
    }

    fun onIncludeExtraCartonsToggled(enabled: Boolean) {
        val newState = _uiState.value.copy(includeExtraCartons = enabled)
        validateAndRecalculate(newState)
    }

    fun onExtraCartonsChanged(text: String) {
        val newState = _uiState.value.copy(extraCartonsText = text)
        validateAndRecalculate(newState)
    }

    fun applyPreset(totalItems: Int, perCarton: Int) {
        val newState = _uiState.value.copy(
            totalItemsText = totalItems.toString(),
            itemsPerCartonText = perCarton.toString()
        )
        validateAndRecalculate(newState)
    }

    fun resetCalculator() {
        val currentSettings = settings.value
        val defaultItems = currentSettings.defaultItems
        val defaultPerCarton = currentSettings.defaultItemsPerCarton
        val defaultAllowance = currentSettings.defaultPlanningAllowance
        val defaultExtra = currentSettings.defaultExtraCartons

        val newState = CalculatorUiState(
            projectName = "",
            totalItemsText = defaultItems.toString(),
            itemsPerCartonText = defaultPerCarton.toString(),
            includePlanningAllowance = false,
            planningAllowanceText = if (defaultAllowance.isNotEmpty() && defaultAllowance != "0") defaultAllowance else "10",
            includeExtraCartons = false,
            extraCartonsText = defaultExtra.toString()
        )
        validateAndRecalculate(newState)
    }

    private fun validateAndRecalculate(state: CalculatorUiState) {
        val totalItemsValidation = CartonPlanCalculator.validateTotalItems(state.totalItemsText)
        val itemsPerCartonValidation = CartonPlanCalculator.validateItemsPerCarton(state.itemsPerCartonText)
        val allowanceValidation = if (state.includePlanningAllowance) {
            CartonPlanCalculator.validateAllowance(state.planningAllowanceText)
        } else {
            CartonPlanCalculator.ValidationResult.Valid
        }
        val extraCartonsValidation = if (state.includeExtraCartons) {
            CartonPlanCalculator.validateExtraCartons(state.extraCartonsText)
        } else {
            CartonPlanCalculator.ValidationResult.Valid
        }

        val totalItemsError = (totalItemsValidation as? CartonPlanCalculator.ValidationResult.Invalid)?.message
        val itemsPerCartonError = (itemsPerCartonValidation as? CartonPlanCalculator.ValidationResult.Invalid)?.message
        val allowanceError = (allowanceValidation as? CartonPlanCalculator.ValidationResult.Invalid)?.message
        val extraCartonsError = (extraCartonsValidation as? CartonPlanCalculator.ValidationResult.Invalid)?.message

        val isValid = totalItemsError == null &&
                itemsPerCartonError == null &&
                allowanceError == null &&
                extraCartonsError == null

        val validatedState = state.copy(
            totalItemsError = totalItemsError,
            itemsPerCartonError = itemsPerCartonError,
            allowanceError = allowanceError,
            extraCartonsError = extraCartonsError,
            isValid = isValid
        )

        recalculate(validatedState)
    }

    private fun recalculate(state: CalculatorUiState) {
        if (!state.isValid) {
            _uiState.value = state.copy(result = null)
            return
        }

        val totalItems = state.totalItemsText.trim().toIntOrNull()
        val itemsPerCarton = state.itemsPerCartonText.trim().toIntOrNull()

        if (totalItems == null || itemsPerCarton == null) {
            _uiState.value = state.copy(result = null)
            return
        }

        val allowancePercent = if (state.includePlanningAllowance) {
            try {
                BigDecimal(state.planningAllowanceText.trim())
            } catch (_: Exception) {
                BigDecimal.ZERO
            }
        } else {
            BigDecimal.ZERO
        }

        val extraCartons = if (state.includeExtraCartons) {
            state.extraCartonsText.trim().toIntOrNull() ?: 0
        } else {
            0
        }

        val input = CartonPlanInput(
            projectName = state.projectName,
            totalItems = totalItems,
            itemsPerCarton = itemsPerCarton,
            planningAllowancePercent = allowancePercent,
            includePlanningAllowance = state.includePlanningAllowance,
            extraCartons = extraCartons,
            includeExtraCartons = state.includeExtraCartons
        )

        val result = CartonPlanCalculator.calculate(
            input = input,
            decimalPlaces = settings.value.decimalPlaces
        )

        _uiState.value = state.copy(result = result)
    }

    fun saveCurrentCalculation() {
        val state = _uiState.value
        val result = state.result ?: return
        val totalItems = state.totalItemsText.trim().toIntOrNull() ?: return
        val itemsPerCarton = state.itemsPerCartonText.trim().toIntOrNull() ?: return
        val extra = if (state.includeExtraCartons) state.extraCartonsText.trim().toIntOrNull() ?: 0 else 0

        val projectName = if (state.projectName.trim().isEmpty()) {
            "Untitled Carton Plan"
        } else {
            state.projectName.trim()
        }

        val saved = SavedCalculation(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            projectName = projectName,
            totalItems = totalItems,
            itemsPerCarton = itemsPerCarton,
            includePlanningAllowance = state.includePlanningAllowance,
            planningAllowancePercent = state.planningAllowanceText.trim(),
            planningQuantity = result.planningQuantity,
            extraCartons = extra,
            includeExtraCartons = state.includeExtraCartons,
            requiredCartons = result.requiredCartons,
            plannedCartons = result.plannedCartons,
            fullCartons = result.fullCartons,
            partialCartons = result.partialCartons,
            finalCartonItems = result.finalCartonItems,
            itemsPacked = result.itemsPacked,
            unusedCapacity = result.unusedCapacity,
            utilizationPercent = result.utilizationPercent.toPlainString(),
            isFavorite = false
        )

        viewModelScope.launch {
            dataStore.saveCalculation(saved)
            _snackbarMessage.emit("Calculation saved")
        }
    }

    fun restoreCalculation(saved: SavedCalculation) {
        val newState = CalculatorUiState(
            projectName = saved.projectName,
            totalItemsText = saved.totalItems.toString(),
            itemsPerCartonText = saved.itemsPerCarton.toString(),
            includePlanningAllowance = saved.includePlanningAllowance,
            planningAllowanceText = saved.planningAllowancePercent,
            includeExtraCartons = saved.includeExtraCartons,
            extraCartonsText = saved.extraCartons.toString()
        )
        validateAndRecalculate(newState)
    }

    fun duplicateCalculation(saved: SavedCalculation) {
        val copy = saved.copy(
            id = UUID.randomUUID().toString(),
            projectName = "Copy of ${saved.projectName}",
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            dataStore.saveCalculation(copy)
            _snackbarMessage.emit("Duplicated calculation")
        }
    }

    fun toggleFavorite(saved: SavedCalculation) {
        viewModelScope.launch {
            dataStore.toggleFavorite(saved.id)
        }
    }

    fun deleteCalculation(saved: SavedCalculation) {
        viewModelScope.launch {
            dataStore.deleteCalculation(saved.id)
            _snackbarMessage.emit("Calculation deleted")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            dataStore.clearAllCalculations()
            _snackbarMessage.emit("History cleared")
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            dataStore.updateSettings(newSettings)
            // Re-trigger calculation with updated decimal places
            recalculate(_uiState.value)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            dataStore.resetSettings()
            resetCalculator()
            _snackbarMessage.emit("Settings reset to defaults")
        }
    }

    fun getResultSummaryText(): String {
        val state = _uiState.value
        val result = state.result ?: return "No valid calculation available."

        val name = if (state.projectName.trim().isNotEmpty()) state.projectName.trim() else "Untitled Carton Plan"

        return buildString {
            appendLine("CartonPlan")
            appendLine()
            appendLine("Project: $name")
            if (state.includePlanningAllowance) {
                appendLine("Order Items: ${state.totalItemsText.trim()}")
                appendLine("Planning Allowance: ${state.planningAllowanceText.trim()}%")
                appendLine("Planning Quantity: ${result.planningQuantity}")
            } else {
                appendLine("Items: ${result.planningQuantity}")
            }
            appendLine("Items per Carton: ${state.itemsPerCartonText.trim()}")
            appendLine("Cartons Required: ${result.requiredCartons}")
            if (state.includeExtraCartons && result.plannedCartons != result.requiredCartons) {
                appendLine("Extra Cartons: ${result.plannedCartons - result.requiredCartons}")
                appendLine("Planned Cartons: ${result.plannedCartons}")
            }
            appendLine("Full Cartons: ${result.fullCartons}")
            if (result.partialCartons > 0) {
                appendLine("Final Carton: ${result.finalCartonItems} / ${state.itemsPerCartonText.trim()} (${result.finalCartonFillPercent.toPlainString()}%)")
            } else {
                appendLine("Final Carton: Full (${result.finalCartonItems} / ${state.itemsPerCartonText.trim()})")
            }
            appendLine("Items Packed: ${result.itemsPacked}")
            appendLine("Unused Capacity: ${result.unusedCapacity}")
            appendLine("Utilization: ${result.utilizationPercent.toPlainString()}%")
        }.trimEnd()
    }
}
