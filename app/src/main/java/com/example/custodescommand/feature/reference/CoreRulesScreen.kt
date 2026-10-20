package com.example.custodescommand.feature.reference

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.custodescommand.core.rules.CoreRule
import com.example.custodescommand.core.rules.CoreRuleRepository

private val categories = listOf("All", "Weapon Abilities", "Core Abilities", "Phases", "Combat", "Movement", "Terrain", "Objectives", "Basics", "Advanced", "Stratagems", "Datasheets")
private val quickAccess = listOf("Assault", "Lethal Hits", "Devastating Wounds", "Feel No Pain", "Deep Strike", "Blast", "Cover")

/** Screen is search-first, not a reproduction of a many-page printed rulebook. */
@Composable
fun CoreRulesScreen(
    state: CoreRulesUiState,
    officialUrl: String
) {
    var search by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("All") }
    var selected by remember { mutableStateOf<CoreRule?>(null) }
    val context = LocalContext.current

    val matches = remember(state.rules, search, category) {
        val q = CoreRuleRepository.normalize(search)
        state.rules.filter { rule ->
            (category == "All" || rule.category == category) &&
                (q.isEmpty() || (listOf(rule.title, rule.number, rule.summary, rule.context)
                    + rule.aliases + rule.tags).any { CoreRuleRepository.normalize(it).contains(q) })
        }
    }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("CORE RULES · 11TH EDITION",
            modifier = Modifier.padding(start = 16.dp, top = 12.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Search rules, effects, numbers…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            categories.forEach { entry ->
                FilterChip(
                    selected = category == entry,
                    onClick = { category = entry },
                    label = { Text(entry) }
                )
            }
        }
        if (state.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        } else if (state.error != null) {
            Text(state.error, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (search.isBlank() && category == "All") {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("QUICK LOOKUP", style = MaterialTheme.typography.labelMedium)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                quickAccess.forEach { name ->
                                    val rule = state.rules.firstOrNull { it.title.equals(name, true) ||
                                        (name == "Cover" && it.title == "Benefit of Cover") }
                                    AssistChip(
                                        onClick = { if (rule != null) selected = rule },
                                        label = { Text(name) }
                                    )
                                }
                            }
                            Text("Compact summaries. For full adjudication, open the official PDF.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(officialUrl)))
                            }) { Text("Open free official Core Rules") }
                        }
                    }
                }
                item {
                    Text("${matches.size} matching rules", style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 4.dp))
                }
                if (matches.isEmpty()) item { Text("No matching rule. Try another phrase.") }
                items(matches, key = { it.id }) { rule ->
                    OutlinedCard(onClick = { selected = rule }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(rule.title, style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f))
                                Text("§${rule.number}", style = MaterialTheme.typography.labelSmall)
                            }
                            Text(rule.summary, style = MaterialTheme.typography.bodyMedium)
                            Text(rule.category, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
    selected?.let { rule ->
        RuleQuickPopup(rule = rule, officialUrl = officialUrl, onDismiss = { selected = null })
    }
}
