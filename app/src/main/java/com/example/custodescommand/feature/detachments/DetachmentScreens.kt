package com.example.custodescommand.feature.detachments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.custodescommand.core.domain.model.*

private val parchmentGold = Color(0xFFD6B65F)
private val auramiteBurgundy = Color(0xFF312026)

@Composable
fun DetachmentListScreen(
    state: DetachmentsUiState,
    onOpen: (String) -> Unit
) {
    var search by rememberSaveable { mutableStateOf("") }
    val results = state.detachments.filter {
        it.name.contains(search, ignoreCase = true) ||
            it.dispositions.any { d -> d.contains(search, ignoreCase = true) }
    }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Detachments", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        OutlinedTextField(value = search, onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("Search name or disposition") })
        when {
            state.loading -> CircularProgressIndicator()
            state.error != null -> Text(state.error, color = MaterialTheme.colorScheme.error)
            results.isEmpty() -> Text("No detachments found")
            else -> LazyColumn(modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(results, key = { it.id }) { d ->
                    Card(onClick = { onOpen(d.id) }, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(d.name, style = MaterialTheme.typography.titleMedium)
                                Text(d.dispositions.joinToString(" • "), style = MaterialTheme.typography.bodySmall)
                                if (d.uniqueShieldHost) Text("UNIQUE: SHIELD HOST", color = parchmentGold,
                                    style = MaterialTheme.typography.labelSmall)
                            }
                            Text("${d.dpCost} DP", style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetachmentDetailScreen(
    requestedId: String,
    state: DetachmentsUiState,
    onOpenSourcePage: (Int) -> Unit
) {
    val detail = state.detail?.takeIf { it.summary.id == requestedId }
    if (detail == null) {
        Box(Modifier.fillMaxSize().padding(20.dp)) {
            if (state.loadingDetail || state.openId != requestedId) CircularProgressIndicator()
            else Text(state.error ?: "Detachment unavailable")
        }
        return
    }
    var filter by rememberSaveable(requestedId) { mutableStateOf("all") }
    var phaseFilter by rememberSaveable(requestedId) { mutableStateOf("All phases") }
    var phaseMenuOpen by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.fillMaxWidth().background(auramiteBurgundy).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("ADEPTUS CUSTODES · DETACHMENT", color = parchmentGold,
                style = MaterialTheme.typography.labelMedium)
            Text(detail.summary.name.uppercase(), color = Color(0xFFF4EFE7),
                style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("${detail.summary.dpCost} DP · ${detail.summary.dispositions.joinToString(" / ")}",
                color = Color(0xFFE2D6C4), style = MaterialTheme.typography.bodyMedium)
            if (detail.summary.uniqueShieldHost) Text("UNIQUE: SHIELD HOST", color = parchmentGold,
                style = MaterialTheme.typography.labelMedium)
        }
        Text("Detachment rule", style = MaterialTheme.typography.titleLarge)
        Text(detail.ruleTitle.uppercase(), style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary)
        Text(detail.ruleText)
        Text("Summary only — check the scanned rule for exact wording and restrictions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (detail.favouredKatah != null) {
            HorizontalDivider()
            Text("Favoured Ka'tah: ${detail.favouredKatah}", style = MaterialTheme.typography.titleMedium)
            detail.favouredKatahEffect?.let { Text(it) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = filter == "all", onClick = { filter = "all" }, label = { Text("All") })
            FilterChip(selected = filter == "stratagems", onClick = { filter = "stratagems" }, label = { Text("Stratagems") })
            FilterChip(selected = filter == "enhancements", onClick = { filter = "enhancements" }, label = { Text("Enhancements") })
        }
        if (filter == "all" || filter == "stratagems") {
            Text("Stratagems", style = MaterialTheme.typography.titleLarge)
            Box {
                OutlinedButton(onClick = { phaseMenuOpen = true }) { Text("Phase: $phaseFilter") }
                DropdownMenu(expanded = phaseMenuOpen, onDismissRequest = { phaseMenuOpen = false }) {
                    (listOf("All phases") + detail.stratagems.map { it.phase }.distinct().sorted())
                        .forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = {
                                phaseFilter = option
                                phaseMenuOpen = false
                            })
                        }
                }
            }
            detail.stratagems.filter { phaseFilter == "All phases" || it.phase == phaseFilter }
                .forEach { s ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(s.name.uppercase(), style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f))
                            Text("${s.cpCost} CP", color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold)
                        }
                        Text(s.phase, style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (s.effectText != null) Text(s.effectText)
                        else Text("Effect and full timing: consult source page ${s.sourcePage}.",
                            style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { onOpenSourcePage(s.sourcePage) }) {
                            Text("Read full stratagem in scan")
                        }
                    }
                }
            }
        }
        if (filter == "all" || filter == "enhancements") {
            Text("Enhancements", style = MaterialTheme.typography.titleLarge)
            detail.enhancements.forEach { e ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(e.name, style = MaterialTheme.typography.titleMedium)
                            Text(e.effectText ?: "Eligibility and full effect: see source scan.",
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("${e.points} pts", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        HorizontalDivider()
        Text("Original source pages", style = MaterialTheme.typography.titleMedium)
        detail.sourcePages.forEach { page ->
            OutlinedButton(onClick = { onOpenSourcePage(page) }, modifier = Modifier.fillMaxWidth()) {
                Text("Open photographed page $page")
            }
        }
        Text("Enhancements can now be assigned from a roster; full eligibility and army legality are still being verified.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun RosterDetachmentsScreen(
    rosterId: String,
    state: RosterDetachmentsUiState,
    onToggle: (String, Boolean) -> Unit,
    onOpenDetail: (String) -> Unit
) {
    if (state.rosterId != rosterId || state.loading) {
        Box(Modifier.fillMaxSize().padding(24.dp)) { CircularProgressIndicator() }
        return
    }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Roster Detachments", style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary)
        Text("DP used: ${state.usedDp}", style = MaterialTheme.typography.headlineSmall)
        Text("DP allowance and combined detachment restrictions are not validated yet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LazyColumn(modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.detachments, key = { it.id }) { d ->
                val isSelected = d.id in state.selectedIds
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isSelected, enabled = !state.saving,
                            onCheckedChange = { onToggle(d.id, it) })
                        Column(Modifier.weight(1f)) {
                            Text(d.name, style = MaterialTheme.typography.titleMedium)
                            Text("${d.dpCost} DP · ${d.dispositions.joinToString(" / ")}",
                                style = MaterialTheme.typography.bodySmall)
                            if (d.uniqueShieldHost) Text("UNIQUE: SHIELD HOST",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = { onOpenDetail(d.id) }) { Text("Rules") }
                    }
                }
            }
        }
    }
}
