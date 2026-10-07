package com.example.cartonplan.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cartonplan.model.AppSettings
import com.example.cartonplan.model.SavedCalculation
import com.example.cartonplan.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "carton_plan_prefs")

class CartonPlanDataStore(private val context: Context) {

    companion object {
        const val MAX_SAVED_CALCULATIONS = 50

        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_DEFAULT_ITEMS = intPreferencesKey("default_items")
        private val KEY_DEFAULT_ITEMS_PER_CARTON = intPreferencesKey("default_items_per_carton")
        private val KEY_DEFAULT_ALLOWANCE = stringPreferencesKey("default_allowance")
        private val KEY_DEFAULT_EXTRA_CARTONS = intPreferencesKey("default_extra_cartons")
        private val KEY_DECIMAL_PLACES = intPreferencesKey("decimal_places")
        private val KEY_SHOW_DIAGRAM = booleanPreferencesKey("show_diagram")
        private val KEY_SHOW_FORMULA = booleanPreferencesKey("show_formula")
        private val KEY_SHOW_CARTON_BREAKDOWN = booleanPreferencesKey("show_carton_breakdown")
        private val KEY_SHOW_UTILIZATION = booleanPreferencesKey("show_utilization")
        private val KEY_SHOW_PLANNING_ALLOWANCE = booleanPreferencesKey("show_planning_allowance")
        private val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        private val KEY_CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
        private val KEY_SAVED_CALCULATIONS = stringPreferencesKey("saved_calculations_json")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val themeModeStr = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeModeStr)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }

        AppSettings(
            themeMode = themeMode,
            dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: true,
            defaultItems = prefs[KEY_DEFAULT_ITEMS] ?: 250,
            defaultItemsPerCarton = prefs[KEY_DEFAULT_ITEMS_PER_CARTON] ?: 40,
            defaultPlanningAllowance = prefs[KEY_DEFAULT_ALLOWANCE] ?: "0",
            defaultExtraCartons = prefs[KEY_DEFAULT_EXTRA_CARTONS] ?: 0,
            decimalPlaces = prefs[KEY_DECIMAL_PLACES] ?: 2,
            showDiagram = prefs[KEY_SHOW_DIAGRAM] ?: true,
            showFormula = prefs[KEY_SHOW_FORMULA] ?: true,
            showCartonBreakdown = prefs[KEY_SHOW_CARTON_BREAKDOWN] ?: true,
            showUtilization = prefs[KEY_SHOW_UTILIZATION] ?: true,
            showPlanningAllowance = prefs[KEY_SHOW_PLANNING_ALLOWANCE] ?: true,
            hapticFeedback = prefs[KEY_HAPTIC_FEEDBACK] ?: true,
            confirmDelete = prefs[KEY_CONFIRM_DELETE] ?: true
        )
    }

    val savedCalculationsFlow: Flow<List<SavedCalculation>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_SAVED_CALCULATIONS] ?: "[]"
        CalculationJsonCodec.listFromJson(json)
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = settings.themeMode.name
            prefs[KEY_DYNAMIC_COLOR] = settings.dynamicColor
            prefs[KEY_DEFAULT_ITEMS] = settings.defaultItems
            prefs[KEY_DEFAULT_ITEMS_PER_CARTON] = settings.defaultItemsPerCarton
            prefs[KEY_DEFAULT_ALLOWANCE] = settings.defaultPlanningAllowance
            prefs[KEY_DEFAULT_EXTRA_CARTONS] = settings.defaultExtraCartons
            prefs[KEY_DECIMAL_PLACES] = settings.decimalPlaces
            prefs[KEY_SHOW_DIAGRAM] = settings.showDiagram
            prefs[KEY_SHOW_FORMULA] = settings.showFormula
            prefs[KEY_SHOW_CARTON_BREAKDOWN] = settings.showCartonBreakdown
            prefs[KEY_SHOW_UTILIZATION] = settings.showUtilization
            prefs[KEY_SHOW_PLANNING_ALLOWANCE] = settings.showPlanningAllowance
            prefs[KEY_HAPTIC_FEEDBACK] = settings.hapticFeedback
            prefs[KEY_CONFIRM_DELETE] = settings.confirmDelete
        }
    }

    suspend fun resetSettings() {
        updateSettings(AppSettings())
    }

    suspend fun saveCalculation(calculation: SavedCalculation) {
        context.dataStore.edit { prefs ->
            val json = prefs[KEY_SAVED_CALCULATIONS] ?: "[]"
            val currentList = CalculationJsonCodec.listFromJson(json).toMutableList()

            // Remove existing if matching id
            currentList.removeAll { it.id == calculation.id }

            // Add new at front
            currentList.add(0, calculation)

            // Keep max 50 saved calculations - remove oldest (at the end)
            while (currentList.size > MAX_SAVED_CALCULATIONS) {
                currentList.removeAt(currentList.lastIndex)
            }

            prefs[KEY_SAVED_CALCULATIONS] = CalculationJsonCodec.listToJson(currentList)
        }
    }

    suspend fun deleteCalculation(id: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[KEY_SAVED_CALCULATIONS] ?: "[]"
            val currentList = CalculationJsonCodec.listFromJson(json).toMutableList()
            currentList.removeAll { it.id == id }
            prefs[KEY_SAVED_CALCULATIONS] = CalculationJsonCodec.listToJson(currentList)
        }
    }

    suspend fun toggleFavorite(id: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[KEY_SAVED_CALCULATIONS] ?: "[]"
            val currentList = CalculationJsonCodec.listFromJson(json).map {
                if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
            }
            prefs[KEY_SAVED_CALCULATIONS] = CalculationJsonCodec.listToJson(currentList)
        }
    }

    suspend fun clearAllCalculations() {
        context.dataStore.edit { prefs ->
            prefs[KEY_SAVED_CALCULATIONS] = "[]"
        }
    }
}
