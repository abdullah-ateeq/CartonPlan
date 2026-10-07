package com.example.cartonplan.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartonplan.model.CartonPlanResult

@Composable
fun ResultBreakdownCard(
    orderItems: Int,
    itemsPerCarton: Int,
    includePlanningAllowance: Boolean,
    allowancePercent: String,
    includeExtraCartons: Boolean,
    extraCartons: Int,
    showUtilization: Boolean,
    result: CartonPlanResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "RESULT BREAKDOWN",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            // Items breakdown
            if (includePlanningAllowance) {
                BreakdownRow(label = "ORDER ITEMS", value = "$orderItems items")
                BreakdownRow(label = "PLANNING ALLOWANCE", value = "$allowancePercent%")
                BreakdownRow(label = "PLANNING QUANTITY", value = "${result.planningQuantity} items", isHighlight = true)
            } else {
                BreakdownRow(label = "TOTAL ITEMS", value = "$orderItems items")
            }

            BreakdownRow(label = "ITEMS PER CARTON", value = "$itemsPerCarton items")
            BreakdownRow(label = "CARTONS REQUIRED", value = "${result.requiredCartons}", isHighlight = true)

            // Extra cartons if enabled
            if (includeExtraCartons && extraCartons > 0) {
                BreakdownRow(label = "EXTRA CARTONS", value = "$extraCartons")
                BreakdownRow(label = "PLANNED CARTONS", value = "${result.plannedCartons}", isHighlight = true)
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            BreakdownRow(label = "ITEMS PACKED", value = "${result.itemsPacked} items")
            BreakdownRow(
                label = "UNUSED CAPACITY",
                value = "${result.unusedCapacity} item positions",
                subtext = "Available positions across cartons"
            )

            if (showUtilization) {
                BreakdownRow(
                    label = "CARTON UTILIZATION",
                    value = "${result.utilizationPercent.toPlainString()}%",
                    subtext = "Item Capacity Utilization",
                    isHighlight = true
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            BreakdownRow(label = "FULL CARTONS", value = "${result.fullCartons}")
            val finalCartonText = if (result.isFinalCartonFull) {
                "Full (${result.finalCartonItems} / $itemsPerCarton)"
            } else {
                "${result.finalCartonItems} / $itemsPerCarton (${result.finalCartonFillPercent.toPlainString()}%)"
            }
            BreakdownRow(label = "FINAL CARTON", value = finalCartonText)
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    subtext: String? = null,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (subtext != null) {
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
