package com.example.cartonplan.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartonplan.model.AppSettings
import com.example.cartonplan.model.ThemeMode
import com.example.cartonplan.ui.CartonPlanViewModel

@Composable
fun SettingsScreen(
    viewModel: CartonPlanViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Title
        Column {
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Configure app defaults and appearance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Appearance Section
        SettingsCard(title = "APPEARANCE") {
            // Theme Mode
            Text(
                text = "Theme",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.values().forEach { mode ->
                    val isSelected = settings.themeMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateSettings(settings.copy(themeMode = mode)) },
                        label = {
                            Text(
                                text = when (mode) {
                                    ThemeMode.SYSTEM -> "System"
                                    ThemeMode.LIGHT -> "Light"
                                    ThemeMode.DARK -> "Dark"
                                }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Dynamic Color Toggle
            SettingsSwitchRow(
                title = "Dynamic Color",
                subtitle = "Use Material You colors on Android 12+",
                checked = settings.dynamicColor,
                onCheckedChange = { viewModel.updateSettings(settings.copy(dynamicColor = it)) }
            )
        }

        // Calculation Defaults
        SettingsCard(title = "CALCULATION DEFAULTS") {
            var itemsText by remember(settings.defaultItems) { mutableStateOf(settings.defaultItems.toString()) }
            var perCartonText by remember(settings.defaultItemsPerCarton) { mutableStateOf(settings.defaultItemsPerCarton.toString()) }
            var allowanceText by remember(settings.defaultPlanningAllowance) { mutableStateOf(settings.defaultPlanningAllowance) }
            var extraCartonsText by remember(settings.defaultExtraCartons) { mutableStateOf(settings.defaultExtraCartons.toString()) }

            OutlinedTextField(
                value = itemsText,
                onValueChange = {
                    itemsText = it
                    it.toIntOrNull()?.let { v ->
                        if (v in 1..10_000_000) viewModel.updateSettings(settings.copy(defaultItems = v))
                    }
                },
                label = { Text("Default Total Items") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = perCartonText,
                onValueChange = {
                    perCartonText = it
                    it.toIntOrNull()?.let { v ->
                        if (v in 1..1_000_000) viewModel.updateSettings(settings.copy(defaultItemsPerCarton = v))
                    }
                },
                label = { Text("Default Items Per Carton") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = allowanceText,
                onValueChange = {
                    allowanceText = it
                    viewModel.updateSettings(settings.copy(defaultPlanningAllowance = it.trim()))
                },
                label = { Text("Default Planning Allowance (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("%") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = extraCartonsText,
                onValueChange = {
                    extraCartonsText = it
                    it.toIntOrNull()?.let { v ->
                        if (v in 0..100) viewModel.updateSettings(settings.copy(defaultExtraCartons = v))
                    }
                },
                label = { Text("Default Extra Cartons") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Decimal places
            Text(
                text = "Decimal Places for Percentages",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0, 1, 2).forEach { places ->
                    val isSelected = settings.decimalPlaces == places
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateSettings(settings.copy(decimalPlaces = places)) },
                        label = { Text("$places decimals") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Display Options
        SettingsCard(title = "DISPLAY & SECTIONS") {
            SettingsSwitchRow(
                title = "Show Diagram",
                subtitle = "Display carton outline grid visual",
                checked = settings.showDiagram,
                onCheckedChange = { viewModel.updateSettings(settings.copy(showDiagram = it)) }
            )

            SettingsSwitchRow(
                title = "Show Formula Breakdown",
                subtitle = "Display dynamic step-by-step math card",
                checked = settings.showFormula,
                onCheckedChange = { viewModel.updateSettings(settings.copy(showFormula = it)) }
            )

            SettingsSwitchRow(
                title = "Show Carton Breakdown",
                subtitle = "Display expandable list of carton contents",
                checked = settings.showCartonBreakdown,
                onCheckedChange = { viewModel.updateSettings(settings.copy(showCartonBreakdown = it)) }
            )

            SettingsSwitchRow(
                title = "Show Capacity Utilization",
                subtitle = "Display utilization percentage in breakdown",
                checked = settings.showUtilization,
                onCheckedChange = { viewModel.updateSettings(settings.copy(showUtilization = it)) }
            )

            SettingsSwitchRow(
                title = "Show Planning Allowance",
                subtitle = "Enable planning allowance toggle on calculator",
                checked = settings.showPlanningAllowance,
                onCheckedChange = { viewModel.updateSettings(settings.copy(showPlanningAllowance = it)) }
            )
        }

        // App Preferences
        SettingsCard(title = "APP PREFERENCES") {
            SettingsSwitchRow(
                title = "Haptic Feedback",
                subtitle = "Light tactile feedback on button actions",
                checked = settings.hapticFeedback,
                onCheckedChange = { viewModel.updateSettings(settings.copy(hapticFeedback = it)) }
            )

            SettingsSwitchRow(
                title = "Confirm Delete",
                subtitle = "Ask for confirmation before deleting calculations",
                checked = settings.confirmDelete,
                onCheckedChange = { viewModel.updateSettings(settings.copy(confirmDelete = it)) }
            )
        }

        // Data & Reset Actions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "DATA MANAGEMENT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                OutlinedButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset All Settings to Defaults")
                }

                Button(
                    onClick = { showClearHistoryDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear All Calculation History")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Reset settings dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("Reset all preferences to default values?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSettings()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear history dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear All History") },
            text = { Text("Permanently delete all saved calculations from history?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics { contentDescription = "Toggle $title" }
        )
    }
}
