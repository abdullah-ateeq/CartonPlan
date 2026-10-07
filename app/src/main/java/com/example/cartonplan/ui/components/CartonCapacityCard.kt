package com.example.cartonplan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartonplan.model.CartonPlanResult
import com.example.cartonplan.ui.theme.PackagingGreen
import java.math.BigDecimal

@Composable
fun CartonCapacityCard(
    itemsPerCarton: Int,
    result: CartonPlanResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CARTON CAPACITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$itemsPerCarton items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "FINAL CARTON",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    val finalDescription = if (result.isFinalCartonFull) {
                        "Full (${result.finalCartonItems} / $itemsPerCarton)"
                    } else {
                        "${result.finalCartonItems} / $itemsPerCarton items (${result.finalCartonFillPercent.toPlainString()}%)"
                    }
                    Text(
                        text = "${result.finalCartonItems} / $itemsPerCarton",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (result.isFinalCartonFull) PackagingGreen else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Visual Capacity Bar
            val fillFraction = if (itemsPerCarton > 0) {
                (result.finalCartonItems.toFloat() / itemsPerCarton.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Final carton fill: ${result.finalCartonItems} of $itemsPerCarton items, ${result.finalCartonFillPercent.toPlainString()} percent full"
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(9.dp)
                        )
                ) {
                    // Filled capacity
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = fillFraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                if (result.isFinalCartonFull) PackagingGreen else MaterialTheme.colorScheme.primary
                            )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Filled: ${result.finalCartonItems} (${result.finalCartonFillPercent.toPlainString()}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val emptySlots = (itemsPerCarton - result.finalCartonItems).coerceAtLeast(0)
                    Text(
                        text = if (emptySlots == 0) "Status: Full" else "Empty: $emptySlots slots",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
