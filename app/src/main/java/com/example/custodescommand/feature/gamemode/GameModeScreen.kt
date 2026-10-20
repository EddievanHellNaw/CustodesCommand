package com.example.custodescommand.feature.gamemode

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.custodescommand.core.domain.model.*
import com.example.custodescommand.feature.wargear.ConfiguredWeaponsPanel

private val gameTabs = listOf("Units", "Stratagems", "Enhancements", "References")

/** Read-only: every callback opens another reference, never changes the roster. */
@Composable
fun GameModeScreen(
    rosterId: String,
    state: GameModeUiState,
    onOpenDatasheet: (String) -> Unit,
    onOpenDetachment: (String) -> Unit,
    onOpenSourcePage: (Int) -> Unit,
    onOpenCoreRules: () -> Unit
) {
    if (state.rosterId != rosterId || state.loading) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val game = state.game
    if (game == null) {
        Text(state.error ?: "Roster unavailable", Modifier.padding(20.dp),
            color = MaterialTheme.colorScheme.error)
        return
    }

    var selectedTab by rememberSaveable(rosterId) { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        GameSummary(game)
        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 8.dp) {
            gameTabs.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index },
                    text = { Text(title) })
            }
        }
        when (selectedTab) {
            0 -> UnitsTab(game, Modifier.weight(1f), onOpenDatasheet, onOpenSourcePage)
            1 -> StratagemsTab(game, Modifier.weight(1f), onOpenSourcePage)
            2 -> EnhancementsTab(game, Modifier.weight(1f), onOpenSourcePage)
            else -> ReferencesTab(game, Modifier.weight(1f), onOpenDetachment,
                onOpenSourcePage, onOpenCoreRules)
        }
    }
}

@Composable
private fun GameSummary(game: GameModeSnapshot) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("GAME MODE · READ-ONLY", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary)
        Text(game.rosterName, style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(game.points.totalPoints?.let { "$it / ${game.points.pointsLimit} pts" }
                ?: "Points unavailable", style = MaterialTheme.typography.titleMedium)
            Text(game.dpUsed?.let { "$it DP used" } ?: "DP unknown",
                style = MaterialTheme.typography.bodyMedium)
        }
        Text("${game.units.size} squads · ${game.detachments.size} detachments · " +
            "${game.enhancements.size} enhancements", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (game.points.remainingPoints != null && game.points.remainingPoints!! < 0) {
            Text("Army exceeds the saved points limit.",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun UnitsTab(
    game: GameModeSnapshot,
    modifier: Modifier,
    onOpenDatasheet: (String) -> Unit,
    onOpenSourcePage: (Int) -> Unit
) {
    var expanded by rememberSaveable(game.rosterId) { mutableStateOf<String?>(null) }
    LazyColumn(modifier, contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (game.units.isEmpty()) item { Text("This roster has no squads yet.") }
        items(game.units, key = { it.rosterUnit.instanceId }) { unit ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(unit.name, style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary)
                    Text("${unit.rosterUnit.modelCount} models · " +
                        "Base: ${unit.basePoints?.let { "$it pts" } ?: "unknown"} · " +
                        "Wargear: +${unit.wargearPoints} pts",
                        style = MaterialTheme.typography.bodySmall)
                    if (unit.enhancements.isNotEmpty()) {
                        Text("Enhancement: ${unit.enhancements.joinToString { it.name }}",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    if (unit.playStatus == "LEGENDS") {
                        Text("LEGENDS — confirm event eligibility", color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium)
                    }
                    if (unit.profileStatus != null && unit.profileStatus != "VERIFIED") {
                        Text("Some profile text may be incomplete — check the source scan.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = { onOpenDatasheet(unit.rosterUnit.unitId) }) {
                            Text("Datasheet")
                        }
                        OutlinedButton(onClick = {
                            expanded = if (expanded == unit.rosterUnit.instanceId) null
                                else unit.rosterUnit.instanceId
                        }) { Text(if (expanded == unit.rosterUnit.instanceId) "Hide weapons" else "Equipped weapons") }
                        unit.sourcePage?.let { page ->
                            TextButton(onClick = { onOpenSourcePage(page) }) { Text("Source p$page") }
                        }
                    }
                    if (expanded == unit.rosterUnit.instanceId) {
                        HorizontalDivider()
                        ConfiguredWeaponsPanel(unit.configuredWeapons)
                    }
                }
            }
        }
    }
}

@Composable
private fun StratagemsTab(
    game: GameModeSnapshot,
    modifier: Modifier,
    onOpenSourcePage: (Int) -> Unit
) {
    var chosenPhase by rememberSaveable(game.rosterId) { mutableStateOf(GamePhase.ALL.name) }
    val phase = GamePhase.valueOf(chosenPhase)
    val matches = game.stratagems.filter { GamePhaseFilter.matches(it.rule.phase, phase) }
    Column(modifier.padding(top = 6.dp)) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GamePhase.entries.forEach { value ->
                FilterChip(selected = phase == value,
                    onClick = { chosenPhase = value.name }, label = { Text(value.label) })
            }
        }
        Text("A phase filter is not an eligibility check; read WHEN, TARGET and EFFECT in the source.",
            Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (game.detachments.isEmpty()) item { Text("No detachments are selected for this roster.") }
            else if (matches.isEmpty()) item { Text("No indexed stratagems match this phase.") }
            items(matches, key = { "${it.detachmentId}:${it.rule.id}" }) { entry ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(entry.rule.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            Text("${entry.rule.cpCost} CP", color = MaterialTheme.colorScheme.primary)
                        }
                        Text(entry.detachmentName + " · " + entry.rule.phase,
                            style = MaterialTheme.typography.bodySmall)
                        entry.rule.whenText?.let { Text("WHEN: $it", style = MaterialTheme.typography.bodySmall) }
                        entry.rule.targetText?.let { Text("TARGET: $it", style = MaterialTheme.typography.bodySmall) }
                        entry.rule.effectText?.let { Text("EFFECT: $it", style = MaterialTheme.typography.bodySmall) }
                        if (entry.rule.textStatus != "VERIFIED") {
                            Text("Index/partial text — use the original page for the full rule.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { onOpenSourcePage(entry.rule.sourcePage) }) {
                            Text("Open stratagem source · p${entry.rule.sourcePage}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancementsTab(
    game: GameModeSnapshot,
    modifier: Modifier,
    onOpenSourcePage: (Int) -> Unit
) {
    LazyColumn(modifier, contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (game.enhancements.isEmpty()) item {
            Text("No enhancements assigned in this roster.")
        }
        items(game.enhancements,
            key = { "${it.assignment.detachmentId}:${it.assignment.enhancementId}" }) { entry ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(entry.assignment.name, Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium)
                        Text("+${entry.assignment.points} pts", color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Assigned to ${entry.targetUnitName}", style = MaterialTheme.typography.bodyMedium)
                    if (entry.assignment.textStatus != "VERIFIED") {
                        Text("Effect and model eligibility require source verification.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { onOpenSourcePage(entry.assignment.sourcePage) }) {
                        Text("Source p${entry.assignment.sourcePage}")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferencesTab(
    game: GameModeSnapshot,
    modifier: Modifier,
    onOpenDetachment: (String) -> Unit,
    onOpenSourcePage: (Int) -> Unit,
    onOpenCoreRules: () -> Unit
) {
    LazyColumn(modifier, contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            OutlinedButton(onClick = onOpenCoreRules, modifier = Modifier.fillMaxWidth()) {
                Text("Core Rules section")
            }
            Text("Core Rules content is still being built; this button opens the existing reference tab.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(game.detachments, key = { it.summary.id }) { d ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(d.summary.name, style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary)
                    Text("${d.summary.dpCost} DP · ${d.ruleTitle}",
                        style = MaterialTheme.typography.bodyMedium)
                    Text(d.ruleText, style = MaterialTheme.typography.bodySmall)
                    if (d.ruleTextStatus != "VERIFIED") {
                        Text("Summary only — original page controls exact wording.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = { onOpenDetachment(d.summary.id) }) {
                            Text("Detachment details")
                        }
                        d.sourcePages.distinct().forEach { page ->
                            TextButton(onClick = { onOpenSourcePage(page) }) { Text("Page $page") }
                        }
                    }
                }
            }
        }
        item {
            HorizontalDivider()
            Text("Validation and source notes", style = MaterialTheme.typography.titleMedium)
            game.warnings.forEach {
                Text("• $it", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
