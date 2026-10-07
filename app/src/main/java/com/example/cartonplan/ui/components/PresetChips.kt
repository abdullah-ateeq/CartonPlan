package com.example.cartonplan.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PresetOption(
    val label: String,
    val totalItems: Int,
    val itemsPerCarton: Int
)

val DEFAULT_PRESETS = listOf(
    PresetOption("50 (10/ctn)", 50, 10),
    PresetOption("100 (20/ctn)", 100, 20),
    PresetOption("250 (40/ctn)", 250, 40),
    PresetOption("1000 (50/ctn)", 1000, 50)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetChips(
    selectedTotal: String,
    selectedPerCarton: String,
    onPresetSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "QUICK EXAMPLES",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DEFAULT_PRESETS.forEach { preset ->
                val isSelected = selectedTotal == preset.totalItems.toString() &&
                        selectedPerCarton == preset.itemsPerCarton.toString()

                FilterChip(
                    selected = isSelected,
                    onClick = { onPresetSelected(preset.totalItems, preset.itemsPerCarton) },
                    label = {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}
