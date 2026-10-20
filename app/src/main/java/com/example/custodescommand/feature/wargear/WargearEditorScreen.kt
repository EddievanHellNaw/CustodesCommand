package com.example.custodescommand.feature.wargear

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.custodescommand.core.domain.model.WargearOption

/**
 * Vexilla is a single unit-wide choice with one bearer: never one checkbox per model.
 * Other weapon options remain attached to their selected physical model slot.
 */
@Composable
fun WargearEditorScreen(
    state: WargearEditorUiState,
    onSelect: (String, Int, Boolean) -> Unit,
    onSetVexillaBearer: (Int?) -> Unit,
    onResize: (Int) -> Unit,
    onDismissError: () -> Unit,
    onOpenSourcePage: (Int) -> Unit
) {
    if (state.isLoading) {
        Box(Modifier.fillMaxSize().padding(24.dp)) { CircularProgressIndicator() }
        return
    }
    val squad = state.unit
    if (squad == null) {
        Text(state.error ?: "Squad not found.", Modifier.padding(24.dp))
        return
    }
    val vexilla = state.options.firstOrNull { it.optionId == "vexilla" && it.maxPerUnit == 1 }
    val perModel = state.options.filterNot { it.optionId == "vexilla" && it.maxPerUnit == 1 }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SQUAD EQUIPMENT", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
                Text(state.unitName, style = MaterialTheme.typography.headlineSmall)
                Text("${squad.modelCount} models · Additional wargear: +${state.extraPoints} pts")
                Text("Selections are associated with models; squad-wide limits apply across the entire unit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (state.error != null) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(state.error, color = MaterialTheme.colorScheme.onErrorContainer)
                        TextButton(onClick = onDismissError) { Text("Dismiss") }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Squad size", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.allowedSizes.forEach { count ->
                        FilterChip(selected = squad.modelCount == count,
                            onClick = { onResize(count) }, enabled = !state.isSaving,
                            label = { Text("$count models") })
                    }
                }
            }
        }
        if (vexilla != null) {
            item {
                val bearer = state.selections.firstOrNull { it.optionId == "vexilla" }?.modelIndex
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("UNIT-WIDE VEXILLA", style = MaterialTheme.typography.titleMedium)
                        Text("Only ONE model in this squad may carry a Vexilla. Choose its bearer:",
                            style = MaterialTheme.typography.bodySmall)
                        Row(Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = bearer == null, onClick = { onSetVexillaBearer(null) },
                                enabled = !state.isSaving, label = { Text("None") })
                            for (model in 1..squad.modelCount) {
                                FilterChip(selected = bearer == model,
                                    onClick = { onSetVexillaBearer(model) },
                                    enabled = !state.isSaving,
                                    label = { Text("Model $model") })
                            }
                        }
                        Text("Selected: ${bearer?.let { "Model $it" } ?: "No Vexilla"} · Maximum 1 per unit",
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        if (perModel.isEmpty() && vexilla == null) {
            item { Text("No selectable equipment has been transcribed for this unit. " +
                "Use its original scanned datasheet for other options.") }
        }
        if (perModel.isNotEmpty()) {
            items(squad.modelCount) { modelOffset ->
                val model = modelOffset + 1
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("MODEL $model — WEAPONS", style = MaterialTheme.typography.titleMedium)
                        perModel.forEach { option: WargearOption ->
                            val selected = state.selections.any {
                                it.modelIndex == model && it.optionId == option.optionId
                            }
                            Row(Modifier.fillMaxWidth().toggleable(
                                value = selected, enabled = !state.isSaving, role = Role.Checkbox,
                                onValueChange = { onSelect(option.optionId, model, it) }
                            ).padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Checkbox(checked = selected, onCheckedChange = null)
                                Column(Modifier.weight(1f)) {
                                    Text(option.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(if (option.pointsPerSelection > 0) "+${option.pointsPerSelection} pts" else "No additional points",
                                        style = MaterialTheme.typography.labelSmall)
                                    if (option.sourceStatus == "PRICE_ONLY") {
                                        Text("Eligibility not fully verified", color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item { HorizontalDivider() }
        item { ConfiguredWeaponsPanel(state.configuredWeapons) }
        if (state.provisionalCount > 0) {
            item { Text("${state.provisionalCount} selection(s) have verified prices but incomplete eligibility rules.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
        }
        state.sourcePage?.let { page ->
            item {
                OutlinedButton(onClick = { onOpenSourcePage(page) }, modifier = Modifier.fillMaxWidth()) {
                    Text("View original datasheet — PDF page $page")
                }
            }
        }
    }
}
