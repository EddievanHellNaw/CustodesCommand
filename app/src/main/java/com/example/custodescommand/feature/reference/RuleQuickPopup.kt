package com.example.custodescommand.feature.reference

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.custodescommand.core.rules.CoreRule
import com.example.custodescommand.core.rules.CoreRuleRepository

/** Short, quickly dismissible reference; source opens the full official PDF. */
@Composable
fun RuleQuickPopup(
    rule: CoreRule?,
    unknownLabel: String? = null,
    officialUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(rule?.title ?: unknownLabel ?: "Ability not indexed",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary)
                rule?.let {
                    Text("11th edition · §${it.number}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(rule?.summary ?: "This ability is not in the local Core Rules quick-reference. For faction-specific effects, consult that unit’s codex.",
                    style = MaterialTheme.typography.bodyLarge)
                if (!rule?.context.isNullOrBlank()) {
                    HorizontalDivider()
                    Text(rule!!.context, style = MaterialTheme.typography.bodyMedium)
                }
                Text("Quick summary only — use the official source for exact timing and wording.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        dismissButton = {
            TextButton(onClick = {
                val url = rule?.sourceUrl?.ifBlank { officialUrl } ?: officialUrl
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }) {
                Text("Official rules PDF")
            }
        }
    )
}

/**
 * Drop-in replacement for weapon.abilities.joinToString(...).
 * Parameterized abilities such as "BLAST 2" and "ANTI-VEHICLE 4+" resolve to the base rule.
 * When no rule matches, the tap still opens a source-linked explanation, not a dead button.
 */
@Composable
fun WeaponAbilityChips(
    abilities: List<String>,
    modifier: Modifier = Modifier
) {
    if (abilities.isEmpty()) return
    val context = LocalContext.current
    val repo = remember(context.applicationContext) {
        CoreRuleRepository.from(context.applicationContext)
    }
    var selected by remember { mutableStateOf<String?>(null) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            abilities.forEach { label ->
                AssistChip(
                    onClick = { selected = label },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.heightIn(min = 32.dp)
                )
            }
        }
    }
    selected?.let { raw ->
        val found = remember(raw) { repo.forAbility(raw) }
        RuleQuickPopup(
            rule = found,
            unknownLabel = raw,
            officialUrl = repo.officialPdfUrl,
            onDismiss = { selected = null }
        )
    }
}
