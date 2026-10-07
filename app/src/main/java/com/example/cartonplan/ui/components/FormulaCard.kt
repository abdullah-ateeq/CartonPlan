package com.example.cartonplan.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartonplan.model.CartonPlanResult
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun FormulaCard(
    totalItems: Int,
    itemsPerCarton: Int,
    includePlanningAllowance: Boolean,
    allowancePercent: String,
    includeExtraCartons: Boolean,
    extraCartons: Int,
    result: CartonPlanResult,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "HOW IT WAS CALCULATED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Step-by-step formula breakdown",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse calculation steps" else "Expand calculation steps",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Step 1: Planning Allowance (if enabled)
                    if (includePlanningAllowance) {
                        FormulaStep(
                            label = "Planning quantity:",
                            formula = "$totalItems × (1 + $allowancePercent ÷ 100)",
                            result = "= ${result.planningQuantity} items"
                        )
                    }

                    // Step 2: Cartons division & Ceiling
                    val rawDivision = try {
                        val n = BigDecimal(result.planningQuantity)
                        val d = BigDecimal(itemsPerCarton)
                        n.divide(d, 4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
                    } catch (_: Exception) {
                        "0"
                    }

                    FormulaStep(
                        label = "Cartons:",
                        formula = "${result.planningQuantity} ÷ $itemsPerCarton",
                        result = "= $rawDivision"
                    )

                    FormulaStep(
                        label = "Round up (ceiling):",
                        formula = "ceiling($rawDivision)",
                        result = "= ${result.requiredCartons} cartons"
                    )

                    // Step 3: Items Packed
                    FormulaStep(
                        label = "Items packed:",
                        formula = "${result.requiredCartons} × $itemsPerCarton",
                        result = "= ${result.itemsPacked}"
                    )

                    // Step 4: Unused Capacity
                    FormulaStep(
                        label = "Unused capacity:",
                        formula = "${result.itemsPacked} − ${result.planningQuantity}",
                        result = "= ${result.unusedCapacity}"
                    )

                    // Step 5: Utilization
                    FormulaStep(
                        label = "Utilization:",
                        formula = "${result.planningQuantity} ÷ ${result.itemsPacked} × 100",
                        result = "= ${result.utilizationPercent.toPlainString()}%"
                    )

                    // Step 6: Final carton contents
                    val finalModuloResult = if (result.partialCartons > 0) {
                        "${result.finalCartonItems} items"
                    } else {
                        "Full ($itemsPerCarton items)"
                    }
                    FormulaStep(
                        label = "Final carton:",
                        formula = "${result.planningQuantity} mod $itemsPerCarton",
                        result = "= $finalModuloResult"
                    )

                    // Step 7: Extra Cartons (if enabled)
                    if (includeExtraCartons && extraCartons > 0) {
                        FormulaStep(
                            label = "Planned cartons:",
                            formula = "${result.requiredCartons} + $extraCartons",
                            result = "= ${result.plannedCartons} cartons"
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun FormulaStep(
    label: String,
    formula: String,
    result: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formula,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = result,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
