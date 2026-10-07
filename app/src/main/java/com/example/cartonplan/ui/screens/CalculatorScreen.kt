package com.example.cartonplan.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartonplan.ui.CartonPlanViewModel
import com.example.cartonplan.ui.components.CartonBreakdownCard
import com.example.cartonplan.ui.components.CartonCapacityCard
import com.example.cartonplan.ui.components.CartonVisualization
import com.example.cartonplan.ui.components.FormulaCard
import com.example.cartonplan.ui.components.HapticFeedbackHelper
import com.example.cartonplan.ui.components.PresetChips
import com.example.cartonplan.ui.components.PrimaryResultCard
import com.example.cartonplan.ui.components.ResultBreakdownCard

@Composable
fun CalculatorScreen(
    viewModel: CartonPlanViewModel,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp

        if (isWide) {
            // Adaptive Two-Column Layout for tablets / wide landscape
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left Column: Inputs & Controls
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HeaderSection()

                    InputsSection(
                        uiState = uiState,
                        settingsShowAllowance = settings.showPlanningAllowance,
                        onProjectNameChanged = viewModel::onProjectNameChanged,
                        onTotalItemsChanged = {
                            HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                            viewModel.onTotalItemsChanged(it)
                        },
                        onItemsPerCartonChanged = {
                            HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                            viewModel.onItemsPerCartonChanged(it)
                        },
                        onPlanningAllowanceChanged = viewModel::onPlanningAllowanceChanged,
                        onIncludePlanningAllowanceToggled = {
                            HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                            viewModel.onIncludePlanningAllowanceToggled(it)
                        },
                        onExtraCartonsChanged = viewModel::onExtraCartonsChanged,
                        onIncludeExtraCartonsToggled = {
                            HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                            viewModel.onIncludeExtraCartonsToggled(it)
                        },
                        onPresetSelected = { total, perCarton ->
                            HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                            viewModel.applyPreset(total, perCarton)
                        }
                    )

                    ActionButtons(
                        canSave = uiState.isValid && uiState.result != null,
                        onSave = {
                            HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                            viewModel.saveCurrentCalculation()
                        },
                        onCopy = {
                            HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                            copyToClipboard(context, viewModel.getResultSummaryText())
                            onShowSnackbar("Result copied")
                        },
                        onShare = {
                            HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                            shareResult(context, viewModel.getResultSummaryText())
                        },
                        onReset = {
                            HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                            viewModel.resetCalculator()
                        }
                    )

                    DisclaimerSection()
                }

                // Right Column: Results & Visualizations
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ResultsSection(
                        uiState = uiState,
                        settings = settings
                    )
                }
            }
        } else {
            // Single-Column Layout for portrait phones
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderSection()

                InputsSection(
                    uiState = uiState,
                    settingsShowAllowance = settings.showPlanningAllowance,
                    onProjectNameChanged = viewModel::onProjectNameChanged,
                    onTotalItemsChanged = {
                        HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                        viewModel.onTotalItemsChanged(it)
                    },
                    onItemsPerCartonChanged = {
                        HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                        viewModel.onItemsPerCartonChanged(it)
                    },
                    onPlanningAllowanceChanged = viewModel::onPlanningAllowanceChanged,
                    onIncludePlanningAllowanceToggled = {
                        HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                        viewModel.onIncludePlanningAllowanceToggled(it)
                    },
                    onExtraCartonsChanged = viewModel::onExtraCartonsChanged,
                    onIncludeExtraCartonsToggled = {
                        HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                        viewModel.onIncludeExtraCartonsToggled(it)
                    },
                    onPresetSelected = { total, perCarton ->
                        HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                        viewModel.applyPreset(total, perCarton)
                    }
                )

                ResultsSection(
                    uiState = uiState,
                    settings = settings
                )

                ActionButtons(
                    canSave = uiState.isValid && uiState.result != null,
                    onSave = {
                        HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                        viewModel.saveCurrentCalculation()
                    },
                    onCopy = {
                        HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                        copyToClipboard(context, viewModel.getResultSummaryText())
                        onShowSnackbar("Result copied")
                    },
                    onShare = {
                        HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                        shareResult(context, viewModel.getResultSummaryText())
                    },
                    onReset = {
                        HapticFeedbackHelper.performLightHaptic(haptic, settings.hapticFeedback)
                        viewModel.resetCalculator()
                    }
                )

                DisclaimerSection()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "CARTONPLAN",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Calculate how many cartons you need.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InputsSection(
    uiState: com.example.cartonplan.ui.CalculatorUiState,
    settingsShowAllowance: Boolean,
    onProjectNameChanged: (String) -> Unit,
    onTotalItemsChanged: (String) -> Unit,
    onItemsPerCartonChanged: (String) -> Unit,
    onPlanningAllowanceChanged: (String) -> Unit,
    onIncludePlanningAllowanceToggled: (Boolean) -> Unit,
    onExtraCartonsChanged: (String) -> Unit,
    onIncludeExtraCartonsToggled: (Boolean) -> Unit,
    onPresetSelected: (Int, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Optional Project Name
            OutlinedTextField(
                value = uiState.projectName,
                onValueChange = onProjectNameChanged,
                label = { Text("Project / Reference Name (Optional)") },
                placeholder = { Text("Untitled Carton Plan") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Project reference name input" }
            )

            // Total Items (Required)
            OutlinedTextField(
                value = uiState.totalItemsText,
                onValueChange = onTotalItemsChanged,
                label = { Text("Total Items") },
                placeholder = { Text("e.g. 250") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = uiState.totalItemsError != null,
                supportingText = {
                    uiState.totalItemsError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    } ?: Text("Enter a whole number from 1 to 10,000,000.")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "${uiState.totalItemsText} total items" }
            )

            // Items Per Carton (Required)
            OutlinedTextField(
                value = uiState.itemsPerCartonText,
                onValueChange = onItemsPerCartonChanged,
                label = { Text("Items Per Carton") },
                placeholder = { Text("e.g. 40") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = uiState.itemsPerCartonError != null,
                supportingText = {
                    uiState.itemsPerCartonError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    } ?: Text("Enter a whole number from 1 to 1,000,000.")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "${uiState.itemsPerCartonText} items per carton" }
            )

            // Quick Example Presets
            PresetChips(
                selectedTotal = uiState.totalItemsText,
                selectedPerCarton = uiState.itemsPerCartonText,
                onPresetSelected = onPresetSelected
            )

            // Planning Allowance Toggle (if enabled in settings)
            if (settingsShowAllowance) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PLANNING ALLOWANCE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Additional items allowance (0–50%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.includePlanningAllowance,
                                onCheckedChange = onIncludePlanningAllowanceToggled,
                                modifier = Modifier.semantics {
                                    contentDescription = "Toggle planning allowance"
                                }
                            )
                        }

                        if (uiState.includePlanningAllowance) {
                            OutlinedTextField(
                                value = uiState.planningAllowanceText,
                                onValueChange = onPlanningAllowanceChanged,
                                label = { Text("Allowance Percentage (%)") },
                                placeholder = { Text("e.g. 10") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                suffix = { Text("%") },
                                isError = uiState.allowanceError != null,
                                supportingText = {
                                    uiState.allowanceError?.let {
                                        Text(text = it, color = MaterialTheme.colorScheme.error)
                                    } ?: Text("Allowance must be between 0% and 50%.")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { contentDescription = "${uiState.planningAllowanceText} percent allowance" }
                            )
                        }
                    }
                }
            }

            // Include Extra Cartons Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "INCLUDE EXTRA CARTONS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Buffer / spare cartons (0–100)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.includeExtraCartons,
                            onCheckedChange = onIncludeExtraCartonsToggled,
                            modifier = Modifier.semantics {
                                contentDescription = "Toggle extra cartons"
                            }
                        )
                    }

                    if (uiState.includeExtraCartons) {
                        OutlinedTextField(
                            value = uiState.extraCartonsText,
                            onValueChange = onExtraCartonsChanged,
                            label = { Text("Extra Cartons") },
                            placeholder = { Text("e.g. 2") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = uiState.extraCartonsError != null,
                            supportingText = {
                                uiState.extraCartonsError?.let {
                                    Text(text = it, color = MaterialTheme.colorScheme.error)
                                } ?: Text("Extra cartons must be between 0 and 100.")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "${uiState.extraCartonsText} extra cartons" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsSection(
    uiState: com.example.cartonplan.ui.CalculatorUiState,
    settings: com.example.cartonplan.model.AppSettings
) {
    val result = uiState.result
    val itemsPerCarton = uiState.itemsPerCartonText.toIntOrNull() ?: 1
    val orderItems = uiState.totalItemsText.toIntOrNull() ?: 0
    val extraCartons = if (uiState.includeExtraCartons) uiState.extraCartonsText.toIntOrNull() ?: 0 else 0

    if (result != null && uiState.isValid) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Primary Result Card (Visually Dominant)
            PrimaryResultCard(
                itemsPerCarton = itemsPerCarton,
                includePlanningAllowance = uiState.includePlanningAllowance,
                includeExtraCartons = uiState.includeExtraCartons,
                result = result
            )

            // 2. Result Breakdown
            ResultBreakdownCard(
                orderItems = orderItems,
                itemsPerCarton = itemsPerCarton,
                includePlanningAllowance = uiState.includePlanningAllowance,
                allowancePercent = uiState.planningAllowanceText,
                includeExtraCartons = uiState.includeExtraCartons,
                extraCartons = extraCartons,
                showUtilization = settings.showUtilization,
                result = result
            )

            // 3. Carton Capacity & Final Carton Card
            CartonCapacityCard(
                itemsPerCarton = itemsPerCarton,
                result = result
            )

            // 4. Carton Visualization (Canvas)
            if (settings.showDiagram) {
                CartonVisualization(
                    itemsPerCarton = itemsPerCarton,
                    result = result
                )
            }

            // 5. Expandable Carton Breakdown
            if (settings.showCartonBreakdown) {
                CartonBreakdownCard(
                    itemsPerCarton = itemsPerCarton,
                    result = result
                )
            }

            // 6. Dynamic Formula Section
            if (settings.showFormula) {
                FormulaCard(
                    totalItems = orderItems,
                    itemsPerCarton = itemsPerCarton,
                    includePlanningAllowance = uiState.includePlanningAllowance,
                    allowancePercent = uiState.planningAllowanceText,
                    includeExtraCartons = uiState.includeExtraCartons,
                    extraCartons = extraCartons,
                    result = result
                )
            }
        }
    } else {
        // Incomplete / invalid prompt
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Enter the required quantities.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    canSave: Boolean,
    onSave: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSave,
                enabled = canSave,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Save calculation" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save")
            }

            OutlinedButton(
                onClick = onCopy,
                enabled = canSave,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Copy result" }
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onShare,
                enabled = canSave,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Share result" }
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share")
            }

            TextButton(
                onClick = onReset,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Reset calculator" }
            ) {
                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset")
            }
        }
    }
}

@Composable
private fun DisclaimerSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = "Planning estimate only. Actual carton requirements may vary because of packing arrangement, product shape, protection materials, and carton specifications.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            lineHeight = 15.sp
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("CartonPlan Result", text)
    clipboard.setPrimaryClip(clip)
}

private fun shareResult(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share CartonPlan Result")
    context.startActivity(shareIntent)
}
