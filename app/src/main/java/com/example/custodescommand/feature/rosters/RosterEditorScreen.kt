
package com.example.custodescommand.feature.rosters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.custodescommand.core.domain.model.UnitSummary

@Composable
fun RosterEditorScreen(
    rosterId: String,
    state: RosterEditorUiState,
    onAddUnit: (String, Int) -> Unit,
    onRemoveUnit: (String) -> Unit,
    onManageDetachments: () -> Unit,
    onManageEnhancements: () -> Unit,
    onDismissError: () -> Unit
) {
    var showUnitPicker by rememberSaveable(rosterId) {
        mutableStateOf(false)
    }

    if (state.rosterId != rosterId || state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val roster = state.roster

    if (roster == null) {
        Text(
            text = state.loadError ?: "Roster not found.",
            modifier = Modifier.padding(24.dp),
            color = MaterialTheme.colorScheme.error
        )
        return
    }

    val unitsById = state.availableUnits.associateBy {
        it.id
    }

    val pricesByInstance = state.points
        ?.lines
        ?.associateBy { it.rosterUnitId }
        .orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = roster.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {

                Text(
                    "Army Points",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = state.armyTotalPoints?.let { "$it / ${roster.pointsLimit} pts" }
                        ?: "Points unavailable",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text("Base units: ${state.points?.totalPoints ?: "?"} pts")
                Text("Enhancements: +${state.enhancementPoints} pts")
                state.armyRemainingPoints?.let { remaining ->
                    Text(
                        text = "Remaining: $remaining pts",
                        color = if (remaining < 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                Text("Extra wargear points not included yet.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }

        if (state.pricingError != null) {
            Text(
                text = "Pricing error: " +
                        state.pricingError,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (state.actionError != null) {
            Text(
                text = state.actionError,
                color = MaterialTheme.colorScheme.error
            )

            TextButton(onClick = onDismissError) {
                Text("Dismiss")
            }
        }

        Button(
            onClick = { showUnitPicker = true },
            enabled = !state.isSaving &&
                    state.availableUnits.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Add Unit")
        }

        OutlinedButton(
            onClick = onManageDetachments,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Manage Detachments")
        }

        OutlinedButton(
            onClick = onManageEnhancements,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Manage Enhancements") }

        if (roster.units.isEmpty()) {
            Text(
                text = "No units in this army yet.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = roster.units,
                    key = { it.instanceId }
                ) { rosterUnit ->

                    val name = unitsById[
                        rosterUnit.unitId
                    ]?.name ?: rosterUnit.unitId

                    val points = pricesByInstance[
                        rosterUnit.instanceId
                    ]?.points

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement =
                                    Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme
                                        .typography.titleMedium
                                )

                                Text(
                                    text = "${rosterUnit.modelCount} models",
                                    style = MaterialTheme
                                        .typography.bodySmall
                                )

                                Text(
                                    text = points?.let {
                                        "$it pts"
                                    } ?: "Cost unavailable",
                                    style = MaterialTheme
                                        .typography.bodyMedium
                                )
                            }

                            TextButton(
                                onClick = {
                                    onRemoveUnit(
                                        rosterUnit.instanceId
                                    )
                                },
                                enabled = !state.isSaving
                            ) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUnitPicker) {
        AddUnitDialog(
            units = state.availableUnits,
            isSaving = state.isSaving,
            onDismiss = {
                showUnitPicker = false
            },
            onConfirm = { unitId, modelCount ->
                onAddUnit(unitId, modelCount)
                showUnitPicker = false
            }
        )
    }
}


@Composable
private fun AddUnitDialog(
    units: List<UnitSummary>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var search by rememberSaveable {
        mutableStateOf("")
    }

    var selectedUnitId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var selectedSize by rememberSaveable {
        mutableIntStateOf(0)
    }

    val filteredUnits = units.filter {
        it.name.contains(search, ignoreCase = true) ||
                it.category.contains(search, ignoreCase = true)
    }

    val selectedUnit = units.firstOrNull {
        it.id == selectedUnitId
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Add Custodes Unit")
        },

        text = {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Search units") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                LazyColumn(
                    modifier = Modifier.heightIn(
                        max = 230.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = filteredUnits,
                        key = { it.id }
                    ) { unit ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedUnitId = unit.id
                                    selectedSize =
                                        unit.allowedModelCounts
                                            .minOrNull() ?: 0
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected =
                                    selectedUnitId == unit.id,
                                onClick = {
                                    selectedUnitId = unit.id
                                    selectedSize =
                                        unit.allowedModelCounts
                                            .minOrNull() ?: 0
                                }
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 6.dp)
                            ) {
                                Text(
                                    unit.name,
                                    style = MaterialTheme
                                        .typography.bodyMedium
                                )

                                Text(
                                    unit.category,
                                    style = MaterialTheme
                                        .typography.bodySmall
                                )
                            }
                        }
                    }
                }

                if (selectedUnit != null) {
                    HorizontalDivider()

                    Text(
                        "Select unit size",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        selectedUnit.allowedModelCounts
                            .sorted()
                            .forEach { count ->

                                FilterChip(
                                    selected =
                                        selectedSize == count,
                                    onClick = {
                                        selectedSize = count
                                    },
                                    label = {
                                        Text("$count models")
                                    }
                                )
                            }
                    }
                }
            }
        },

        confirmButton = {
            TextButton(
                enabled =
                    selectedUnit != null &&
                            selectedSize in
                            selectedUnit.allowedModelCounts &&
                            !isSaving,
                onClick = {
                    selectedUnitId?.let { unitId ->
                        onConfirm(
                            unitId,
                            selectedSize
                        )
                    }
                }
            ) {
                Text("Add Unit")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Cancel")
            }
        }
    )
}
