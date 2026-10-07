package com.example.cartonplan.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartonplan.model.CartonPlanResult
import com.example.cartonplan.ui.theme.PackagingGreen
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.sqrt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CartonVisualization(
    itemsPerCarton: Int,
    result: CartonPlanResult,
    modifier: Modifier = Modifier
) {
    val totalCartons = result.requiredCartons
    if (totalCartons <= 0 || itemsPerCarton <= 0) return

    val displayedCartonsCount = min(10, totalCartons)
    val remainingCartons = totalCartons - displayedCartonsCount

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CARTON VISUALIZATION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (totalCartons > 10) "Showing 1–10 of $totalCartons" else "$totalCartons cartons",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                    )
                    Text("Packed Item", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
                    )
                    Text("Empty Position", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // FlowRow of Carton Cards (max 10 cards)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                for (cartonIndex in 1..displayedCartonsCount) {
                    val isLastCarton = cartonIndex == totalCartons
                    val itemsInThisCarton = if (isLastCarton && result.partialCartons > 0) {
                        result.finalCartonItems
                    } else {
                        itemsPerCarton
                    }
                    val isFull = itemsInThisCarton == itemsPerCarton

                    CartonBoxCard(
                        cartonNumber = cartonIndex,
                        itemsPacked = itemsInThisCarton,
                        capacity = itemsPerCarton,
                        isFull = isFull,
                        modifier = Modifier.weight(1f)
                    )
                }

                // If more than 10 cartons, show omitted indicator
                if (remainingCartons > 0) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "+$remainingCartons",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "more cartons",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (result.partialCartons > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Final: ${result.finalCartonItems}/$itemsPerCarton",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartonBoxCard(
    cartonNumber: Int,
    itemsPacked: Int,
    capacity: Int,
    isFull: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = modifier
            .semantics {
                contentDescription = "Carton $cartonNumber: $itemsPacked of $capacity items. ${if (isFull) "Full" else "Partial"}"
            },
        colors = CardDefaults.cardColors(
            containerColor = surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CTN $cartonNumber",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isFull) "FULL" else "PARTIAL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = if (isFull) PackagingGreen else MaterialTheme.colorScheme.primary
                )
            }

            // Carton Graphic Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(64.dp)) {
                    val w = size.width
                    val h = size.height

                    // Draw outer carton border with carton flap detail
                    drawRoundRect(
                        color = outlineColor.copy(alpha = 0.6f),
                        topLeft = Offset(1f, 1f),
                        size = Size(w - 2f, h - 2f),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Top carton flap seam
                    drawLine(
                        color = outlineColor.copy(alpha = 0.4f),
                        start = Offset(4.dp.toPx(), 8.dp.toPx()),
                        end = Offset(w - 4.dp.toPx(), 8.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )

                    val contentTop = 12.dp.toPx()
                    val contentHeight = h - contentTop - 4.dp.toPx()
                    val contentLeft = 4.dp.toPx()
                    val contentWidth = w - 8.dp.toPx()

                    if (capacity in 1..48) {
                        // Draw individual item dots/slots in a grid
                        val columns = min(capacity, ceil(sqrt(capacity.toDouble() * 1.5)).toInt().coerceIn(2, 8))
                        val rows = ceil(capacity.toDouble() / columns).toInt()

                        val cellW = contentWidth / columns
                        val cellH = contentHeight / rows
                        val itemSize = min(cellW, cellH) * 0.75f

                        var count = 0
                        for (r in 0 until rows) {
                            for (c in 0 until columns) {
                                if (count >= capacity) break
                                val isFilled = count < itemsPacked
                                val cx = contentLeft + c * cellW + cellW / 2
                                val cy = contentTop + r * cellH + cellH / 2

                                if (isFilled) {
                                    // Filled item: solid rectangle with round corners
                                    drawRoundRect(
                                        color = primaryColor,
                                        topLeft = Offset(cx - itemSize / 2, cy - itemSize / 2),
                                        size = Size(itemSize, itemSize),
                                        cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                                    )
                                } else {
                                    // Empty position: stroked hollow square with dashed or distinct outline
                                    drawRoundRect(
                                        color = outlineColor,
                                        topLeft = Offset(cx - itemSize / 2, cy - itemSize / 2),
                                        size = Size(itemSize, itemSize),
                                        cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx()),
                                        style = Stroke(
                                            width = 1.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 2f))
                                        )
                                    )
                                }
                                count++
                            }
                        }
                    } else {
                        // For large capacities (> 48 items), draw simplified fill gauge
                        val fillRatio = if (capacity > 0) itemsPacked.toFloat() / capacity.toFloat() else 0f
                        val barHeight = contentHeight - 4.dp.toPx()
                        val barTop = contentTop + 2.dp.toPx()

                        // Background cavity
                        drawRoundRect(
                            color = outlineColor.copy(alpha = 0.2f),
                            topLeft = Offset(contentLeft + 2.dp.toPx(), barTop),
                            size = Size(contentWidth - 4.dp.toPx(), barHeight),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )

                        // Filled portion
                        val filledWidth = (contentWidth - 4.dp.toPx()) * fillRatio
                        if (filledWidth > 0) {
                            drawRoundRect(
                                color = if (isFull) PackagingGreen else primaryColor,
                                topLeft = Offset(contentLeft + 2.dp.toPx(), barTop),
                                size = Size(filledWidth, barHeight),
                                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                            )
                        }

                        // Hatch marks for technical warehouse feel
                        val hatchStep = (contentWidth - 4.dp.toPx()) / 4f
                        for (i in 1..3) {
                            val markX = contentLeft + 2.dp.toPx() + i * hatchStep
                            drawLine(
                                color = Color.White.copy(alpha = 0.4f),
                                start = Offset(markX, barTop),
                                end = Offset(markX, barTop + barHeight),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                }
            }

            Text(
                text = "$itemsPacked / $capacity",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
