package com.example.custodescommand.feature.rosters

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.custodescommand.core.domain.model.UnitSummary
import com.example.custodescommand.feature.media.MiniatureThumbnail
import com.example.custodescommand.feature.media.UnitPhotosUiState
import java.io.File

private val CustodesGold = Color(0xFFD6B65F)
private val CustodesRed = Color(0xFFF28C8C)

/**
 * A clean roster presentation. Only the editor ViewModel owns edits; this
 * composable receives immutable state and emits UI actions through callbacks.
 * Existing Navigation 3 destinations are deliberately kept in the app shell.
 */
@Composable
fun RosterCommandScreen(
    rosterId: String,
    state: RosterEditorUiState,
    photoState: UnitPhotosUiState,
    photoRevision: Long,
    photoFor: (String) -> File?,
    deletionState: RosterDeletionState,
    onAddUnit: (String, Int) -> Unit,
    onRemoveUnit: (String) -> Unit,
    onEquipUnit: (String) -> Unit,
    onOpenUnit: (String) -> Unit,
    onManageDetachments: () -> Unit,
    onManageEnhancements: () -> Unit,
    onOpenRules: () -> Unit,
    onStartGame: () -> Unit,
    onDeleteRoster: () -> Unit,
    onPhotoSelected: (String, Uri) -> Unit,
    onRemovePhoto: (String) -> Unit
) {
    var showAddDialog by rememberSaveable(rosterId) { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable(rosterId) { mutableStateOf(false) }
    var removeInstanceId by rememberSaveable(rosterId) { mutableStateOf<String?>(null) }
    var photoTargetId by rememberSaveable(rosterId) { mutableStateOf<String?>(null) }
    var activeTab by rememberSaveable(rosterId) { mutableStateOf("Overview") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val instanceId = photoTargetId
        if (uri != null && instanceId != null) onPhotoSelected(instanceId, uri)
        photoTargetId = null
    }

    // Alternative document-provider route for devices/cloud photo providers that
    // do not serve Photo Picker URIs reliably. Import happens immediately.
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        val instanceId = photoTargetId
        if (uri != null && instanceId != null) onPhotoSelected(instanceId, uri)
        photoTargetId = null
    }

    if (state.rosterId != rosterId || state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val roster = state.roster
    if (roster == null) {
        Text(
            text = state.loadError ?: "Roster not found.",
            modifier = Modifier.padding(20.dp),
            color = MaterialTheme.colorScheme.error
        )
        return
    }

    val definitions = remember(state.availableUnits) {
        state.availableUnits.associateBy { it.id }
    }
    val baseCosts = remember(state.points) {
        state.points?.lines?.associateBy { it.rosterUnitId }.orEmpty()
    }
    val isDeleting = deletionState is RosterDeletionState.Deleting &&
        deletionState.rosterId == rosterId

    Column(
        Modifier.fillMaxSize().padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = roster.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CustodesGold
                )
                Text("ADEPTUS CUSTODES", style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = onStartGame) { Text("Play") }
            TextButton(onClick = { showDeleteDialog = true }) {
                Text("Delete", color = CustodesRed)
            }
        }

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Overview", "Units", "Detachments", "Enhancements", "Rules")
                .forEach { tab ->
                    FilterChip(
                        selected = activeTab == tab,
                        onClick = {
                            when (tab) {
                                "Detachments" -> onManageDetachments()
                                "Enhancements" -> onManageEnhancements()
                                "Rules" -> onOpenRules()
                                else -> activeTab = tab
                            }
                        },
                        label = { Text(tab) }
                    )
                }
        }

        if (state.actionError != null || state.pricingError != null || photoState.errorMessage != null) {
            Text(
                state.actionError ?: state.pricingError ?: photoState.errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            if (activeTab == "Overview") {
                item { RosterPointsPanel(state) }
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Units", style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif)
                    Text(
                        "${roster.units.size} squads",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            if (roster.units.isEmpty()) {
                item {
                    Text("This roster is empty. Add a unit to get started.")
                }
            }

            items(roster.units, key = { it.instanceId }) { rosterUnit ->
                val instanceId = rosterUnit.instanceId
                val photo = photoFor(instanceId)
                val busy = instanceId in photoState.busyUnitIds
                Card(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            MiniatureThumbnail(
                                photo = photo,
                                photoRevision = photoRevision,
                                modifier = Modifier.size(86.dp)
                            )
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    definitions[rosterUnit.unitId]?.name ?: rosterUnit.unitId,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text("${rosterUnit.modelCount} models", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    baseCosts[instanceId]?.points?.let { "$it base pts" }
                                        ?: "Points unavailable",
                                    color = CustodesGold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { onEquipUnit(instanceId) }, modifier = Modifier.weight(1f)) {
                                Text("Equip", maxLines = 1)
                            }
                            TextButton(onClick = { onOpenUnit(rosterUnit.unitId) }, modifier = Modifier.weight(1f)) {
                                Text("Details", maxLines = 1)
                            }
                            TextButton(onClick = { removeInstanceId = instanceId }, modifier = Modifier.weight(1f)) {
                                Text("Remove", maxLines = 1, color = CustodesRed)
                            }
                        }
                        HorizontalDivider()
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    photoTargetId = instanceId
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                enabled = !busy
                            ) {
                                Text(if (photo == null) "Add mini photo" else "Change photo")
                            }
                            TextButton(
                                onClick = {
                                    photoTargetId = instanceId
                                    documentPicker.launch(arrayOf("image/*"))
                                },
                                enabled = !busy
                            ) { Text("Browse files") }
                            if (photo != null) {
                                TextButton(onClick = { onRemovePhoto(instanceId) }, enabled = !busy) {
                                    Text("Clear photo")
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { showAddDialog = true },
            enabled = !state.isSaving && !isDeleting,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("+ Add Unit")
        }
    }

    if (showAddDialog) {
        RosterAddUnitDialog(
            units = state.availableUnits,
            onDismiss = { showAddDialog = false },
            onAdd = { unitId, count ->
                onAddUnit(unitId, count)
                showAddDialog = false
            }
        )
    }
    if (removeInstanceId != null) {
        AlertDialog(
            onDismissRequest = { removeInstanceId = null },
            title = { Text("Remove this squad?") },
            text = { Text("Its equipment and assigned enhancements will also be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    removeInstanceId?.let(onRemoveUnit)
                    removeInstanceId = null
                }) { Text("Remove", color = CustodesRed) }
            },
            dismissButton = { TextButton(onClick = { removeInstanceId = null }) { Text("Cancel") } }
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = { Text("Permanently delete ${roster.name}?") },
            text = {
                val failure = deletionState as? RosterDeletionState.Failed
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This deletes the saved army, its squads and their private mini photos. This cannot be undone.")
                    if (failure?.rosterId == rosterId) {
                        Text(failure.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDeleteRoster, enabled = !isDeleting) {
                    Text(if (isDeleting) "Deleting…" else "Delete roster", color = CustodesRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }, enabled = !isDeleting) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RosterPointsPanel(state: RosterEditorUiState) {
    val roster = state.roster ?: return
    val points = state.armyTotalPoints
    val remaining = state.armyRemainingPoints
    Card(
        border = BorderStroke(1.dp, CustodesGold),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ARMY POINTS", style = MaterialTheme.typography.titleSmall, color = CustodesGold)
            Text(
                text = points?.let { "$it / ${roster.pointsLimit} pts" }
                    ?: "Points unavailable",
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif)
            )
            if (points != null) {
                LinearProgressIndicator(
                    progress = { (points.toFloat() / roster.pointsLimit.coerceAtLeast(1)).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = CustodesGold
                )
            }
            HorizontalDivider()
            SummaryRow("Base units", state.points?.totalPoints?.let { "$it pts" } ?: "Unknown")
            SummaryRow("Enhancements", "+${state.enhancementPoints} pts")
            SummaryRow("Wargear", "+${state.wargearPoints} pts")
            SummaryRow("Remaining", remaining?.let { "$it pts" } ?: "Unknown")
        }
    }
}

@Composable
private fun SummaryRow(name: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(name, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RosterAddUnitDialog(
    units: List<UnitSummary>,
    onDismiss: () -> Unit,
    onAdd: (String, Int) -> Unit
) {
    var search by rememberSaveable { mutableStateOf("") }
    var selectedUnitId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCount by rememberSaveable { mutableIntStateOf(0) }
    val selected = units.firstOrNull { it.id == selectedUnitId }
    val filtered = units.filter {
        it.name.contains(search, ignoreCase = true) || it.category.contains(search, ignoreCase = true)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a unit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Search datasheets") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(Modifier.heightIn(max = 240.dp)) {
                    items(filtered, key = { it.id }) { unit ->
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                selectedUnitId = unit.id
                                selectedCount = unit.allowedModelCounts.minOrNull() ?: 0
                            }.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedUnitId == unit.id,
                                onClick = {
                                    selectedUnitId = unit.id
                                    selectedCount = unit.allowedModelCounts.minOrNull() ?: 0
                                }
                            )
                            Text(unit.name, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (selected != null) {
                    Text("Unit size", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        selected.allowedModelCounts.sorted().forEach { count ->
                            FilterChip(
                                selected = selectedCount == count,
                                onClick = { selectedCount = count },
                                label = { Text("$count") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selected != null && selectedCount in selected.allowedModelCounts,
                onClick = { selected?.let { onAdd(it.id, selectedCount) } }
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
