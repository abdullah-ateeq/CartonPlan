package com.example.cartonplan.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartonplan.ui.theme.PackagingGreen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val itemsProgress = remember { Animatable(0f) }
    val carton1Progress = remember { Animatable(0f) }
    val carton2Progress = remember { Animatable(0f) }
    val countProgress = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Stage 1: Item shapes appear (0 - 300ms)
        itemsProgress.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing))

        // Stage 2: First carton appears & fills (300 - 650ms)
        carton1Progress.animateTo(1f, animationSpec = tween(350, easing = FastOutSlowInEasing))

        // Stage 3: Second carton appears & partially fills (650 - 950ms)
        carton2Progress.animateTo(0.6f, animationSpec = tween(300, easing = FastOutSlowInEasing))

        // Stage 4: Carton count & title fades in (950 - 1350ms)
        countProgress.animateTo(1f, animationSpec = tween(200, easing = FastOutSlowInEasing))
        titleAlpha.animateTo(1f, animationSpec = tween(250, easing = FastOutSlowInEasing))

        delay(150)
        onSplashFinished()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Canvas
            Box(
                modifier = Modifier.size(180.dp, 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val boxWidth = 72.dp.toPx()
                    val boxHeight = 72.dp.toPx()
                    val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())

                    // Carton 1 (Left)
                    if (carton1Progress.value > 0f) {
                        val c1Left = w / 2 - boxWidth - 10.dp.toPx()
                        val c1Top = h / 2 - boxHeight / 2

                        // Outline
                        drawRoundRect(
                            color = outlineColor,
                            topLeft = Offset(c1Left, c1Top),
                            size = Size(boxWidth, boxHeight),
                            cornerRadius = cornerRadius,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // Top flap line
                        drawLine(
                            color = outlineColor.copy(alpha = 0.5f),
                            start = Offset(c1Left + 6.dp.toPx(), c1Top + 10.dp.toPx()),
                            end = Offset(c1Left + boxWidth - 6.dp.toPx(), c1Top + 10.dp.toPx()),
                            strokeWidth = 1.5.dp.toPx()
                        )

                        // Fill (animated)
                        val fillHeight = (boxHeight - 16.dp.toPx()) * carton1Progress.value
                        drawRoundRect(
                            color = PackagingGreen.copy(alpha = 0.85f),
                            topLeft = Offset(c1Left + 6.dp.toPx(), c1Top + boxHeight - 6.dp.toPx() - fillHeight),
                            size = Size(boxWidth - 12.dp.toPx(), fillHeight),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    }

                    // Carton 2 (Right, partially filled)
                    if (carton2Progress.value > 0f) {
                        val c2Left = w / 2 + 10.dp.toPx()
                        val c2Top = h / 2 - boxHeight / 2

                        // Outline
                        drawRoundRect(
                            color = outlineColor,
                            topLeft = Offset(c2Left, c2Top),
                            size = Size(boxWidth, boxHeight),
                            cornerRadius = cornerRadius,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // Top flap line
                        drawLine(
                            color = outlineColor.copy(alpha = 0.5f),
                            start = Offset(c2Left + 6.dp.toPx(), c2Top + 10.dp.toPx()),
                            end = Offset(c2Left + boxWidth - 6.dp.toPx(), c2Top + 10.dp.toPx()),
                            strokeWidth = 1.5.dp.toPx()
                        )

                        // Fill (partially animated)
                        val fillHeight = (boxHeight - 16.dp.toPx()) * carton2Progress.value
                        drawRoundRect(
                            color = primaryColor.copy(alpha = 0.85f),
                            topLeft = Offset(c2Left + 6.dp.toPx(), c2Top + boxHeight - 6.dp.toPx() - fillHeight),
                            size = Size(boxWidth - 12.dp.toPx(), fillHeight),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Carton count badge
            if (countProgress.value > 0f) {
                Box(
                    modifier = Modifier
                        .alpha(countProgress.value)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "2 CARTONS REQUIRED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Title & Subtitle fade in
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(titleAlpha.value)
            ) {
                Text(
                    text = "CARTONPLAN",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Calculate cartons for your quantity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
