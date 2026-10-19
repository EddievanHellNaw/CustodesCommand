package com.example.custodescommand.feature.enhancements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.custodescommand.core.domain.model.*

@Composable
fun RosterEnhancementsScreen(
    rosterId: String,
    state: RosterEnhancementsUiState,
    onAssign: (AvailableEnhancement, String) -> Unit,
    onRemove: (AssignedEnhancement) -> Unit,
    onDismissError: () -> Unit,
    onOpenSourcePage: (Int) -> Unit
) {
    var showPicker by rememberSaveable(rosterId) { mutableStateOf(false) }
    if (state.rosterId != rosterId || state.loading) {
        Box(Modifier.fillMaxSize().padding(24.dp)) { CircularProgressIndicator() }
        return
    }
    val roster = state.roster
    if (roster == null) {
        Text(state.error ?: "Roster not found", Modifier.padding(24.dp))
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Enhancements", style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary)
            Text(roster.name, style = MaterialTheme.typography.titleSmall)
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Army points", style = MaterialTheme.typography.titleMedium)
                    Text("Base units: ${state.baseUnitPoints?.let { "$it pts" } ?: "Unavailable"}")
                    Text("Enhancements: +${state.enhancementPoints} pts")
                    HorizontalDivider()
                    Text("Total: ${state.totalPoints?.let { "$it / ${roster.pointsLimit} pts" } ?: "Unavailable"}",
                        style = MaterialTheme.typography.titleLarge)
                    state.remainingPoints?.let { remainder ->
                        Text("Remaining: $remainder pts", color = if (remainder < 0)
                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                    }
                    Text("Additional wargear costs are not included yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Text("These are provisional assignments. Full enhancement eligibility and combined-detachment legality still require checking against the PDF scans.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            state.pricingError?.let {
                Text("Pricing error: $it", color = MaterialTheme.colorScheme.error)
            }
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onDismissError) { Text("Dismiss error") }
            }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.saving && roster.units.isNotEmpty() && state.options.isNotEmpty(),
                onClick = { showPicker = true }
            ) { Text("Assign enhancement") }
            if (state.options.isEmpty()) {
                Text("Select a detachment for this roster to access its enhancements.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        if (state.assignments.isEmpty()) {
            item { Text("No enhancements assigned.") }
        } else {
            items(state.assignments, key = { "${it.detachmentId}:${it.enhancementId}" }) { a ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(a.name, style = MaterialTheme.typography.titleMedium)
                        Text("+${a.points} pts · ${state.unitNames[a.unitId] ?: a.unitId}")
                        if (a.textStatus != "FULLY_VERIFIED") {
                            Text("Eligibility not fully verified", color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onOpenSourcePage(a.sourcePage) }) { Text("Source PDF") }
                            TextButton(onClick = { onRemove(a) }, enabled = !state.saving) { Text("Remove") }
                        }
                    }
                }
            }
        }
    }

    if (showPicker) {
        AssignEnhancementDialog(
            state = state,
            onDismiss = { showPicker = false },
            onConfirm = { enhancement, instanceId ->
                onAssign(enhancement, instanceId)
                showPicker = false
            }
        )
    }
}

@Composable
private fun AssignEnhancementDialog(
    state: RosterEnhancementsUiState,
    onDismiss: () -> Unit,
    onConfirm: (AvailableEnhancement, String) -> Unit
) {
    var enhancementKey by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedUnit by rememberSaveable { mutableStateOf<String?>(null) }
    val roster = state.roster ?: return
    val options = state.options
    val choice = options.firstOrNull { "${it.detachmentId}:${it.enhancementId}" == enhancementKey }
    val occupied = state.assignments.map { it.rosterUnitInstanceId }.toSet()
    val candidates = roster.units.filter { it.instanceId !in occupied }
    val candidateIds = candidates.map { it.instanceId }.toSet()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign enhancement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Choose enhancement", style = MaterialTheme.typography.titleSmall)
                LazyColumn(Modifier.heightIn(max = 175.dp)) {
                    items(options, key = { "${it.detachmentId}:${it.enhancementId}" }) { option ->
                        val key = "${option.detachmentId}:${option.enhancementId}"
                        Row(Modifier.fillMaxWidth().clickable { enhancementKey = key }.padding(vertical = 2.dp)) {
                            RadioButton(selected = enhancementKey == key, onClick = { enhancementKey = key })
                            Column(Modifier.padding(start = 5.dp)) {
                                Text(option.name, style = MaterialTheme.typography.bodyMedium)
                                Text("${option.points} pts · ${option.detachmentId}",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                HorizontalDivider()
                Text("Assign to roster unit", style = MaterialTheme.typography.titleSmall)
                LazyColumn(Modifier.heightIn(max = 165.dp)) {
                    items(candidates, key = { it.instanceId }) { unit ->
                        Row(Modifier.fillMaxWidth().clickable { selectedUnit = unit.instanceId }.padding(vertical = 2.dp)) {
                            RadioButton(selected = selectedUnit == unit.instanceId,
                                onClick = { selectedUnit = unit.instanceId })
                            Text("${state.unitNames[unit.unitId] ?: unit.unitId} · ${unit.modelCount} models",
                                modifier = Modifier.padding(top = 12.dp, start = 5.dp),
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Text("Assignment to a roster unit is a temporary stand-in for per-model enhancements. Check model eligibility before a game.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(
                enabled = choice != null && (selectedUnit?.let { it in candidateIds } == true) && !state.saving,
                onClick = {
                    val instance = selectedUnit
                    if (choice != null && instance != null) onConfirm(choice, instance)
                }
            ) { Text("Assign") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
