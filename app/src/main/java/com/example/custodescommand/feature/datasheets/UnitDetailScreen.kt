package com.example.custodescommand.feature.datasheets

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.custodescommand.core.domain.model.*
import com.example.custodescommand.feature.reference.WeaponAbilityChips

private val headerGold = Color(0xFFD6B65F)
private val headerBurgundy = Color(0xFF312026)
private val bookIvory = Color(0xFFF1EADB)

@Composable
fun UnitDetailScreen(
    requestedUnitId: String,
    state: UnitDetailUiState,
    onOpenSourcePage: (Int) -> Unit
) {
    if (state.requestedUnitId != requestedUnitId || state.loading) {
        Box(Modifier.fillMaxSize().padding(20.dp)) { CircularProgressIndicator() }
        return
    }
    val unit = state.unit
    if (unit == null) {
        Text(state.error ?: "Datasheet not found", Modifier.padding(20.dp))
        return
    }
    DatasheetPage(unit, onOpenSourcePage)
}

@Composable
private fun DatasheetPage(unit: DetailedUnit, onOpenSourcePage: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().background(headerBurgundy).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text("ADEPTUS CUSTODES  •  ${unit.category.uppercase()}",
                color = headerGold, style = MaterialTheme.typography.labelMedium)
            Text(unit.name.uppercase(), color = bookIvory,
                style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("${unit.allowedModelCounts.sorted().joinToString(" / ")} models",
                color = bookIvory, style = MaterialTheme.typography.bodyMedium)
            if (unit.playStatus == "LEGENDS") {
                Text("LEGENDS — check event restrictions", color = headerGold,
                    style = MaterialTheme.typography.labelMedium)
            }
        }

        if (unit.profileStatus != "FULLY_VERIFIED") {
            Text("Partial structured transcription: check the original scanned datasheet during play.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall)
        }

        UnitCharacteristicStrip(unit.characteristics)

        // Keep each weapon category together in one horizontally scrollable matrix.
        WeaponMatrix("RANGED WEAPONS", unit.weapons.filter { it.type == WeaponType.RANGED }, "BS")
        WeaponMatrix("MELEE WEAPONS", unit.weapons.filter { it.type == WeaponType.MELEE }, "WS")

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 720.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AbilityPanel(unit)
                        CodexReferencePanel(unit)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CompositionPanel(unit)
                        PricePanel(unit)
                    }
                }
            } else {
                // A Box overlays children. In portrait we need a vertical layout
                // or the ability, composition and points text will draw on top of itself.
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AbilityPanel(unit)
                    CodexReferencePanel(unit)
                    CompositionPanel(unit)
                    PricePanel(unit)
                }
            }
        }

        unit.sourcePage?.let { page ->
            OutlinedButton(
                onClick = { onOpenSourcePage(page) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("View original datasheet · PDF page $page") }
        } ?: Text("No scanned datasheet supplied for this unit.",
            style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SectionTitle(title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title.uppercase(), color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold)
        HorizontalDivider()
    }
}

@Composable
private fun UnitCharacteristicStrip(stats: UnitCharacteristics?) {
    SectionTitle("Characteristics")
    if (stats == null) {
        Text("Characteristics not yet transcribed; consult source scan.")
        return
    }
    // Horizontal scrolling prevents six stats becoming unreadably small on narrow phones.
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        StatBox("M", "${stats.movementInches}\"")
        StatBox("T", stats.toughness.toString())
        StatBox("Sv", stats.armourSave)
        StatBox("W", stats.wounds.toString())
        StatBox("Ld", stats.leadership)
        StatBox("OC", stats.objectiveControl.toString())
        stats.invulnerableSave?.let { StatBox("Inv", it) }
    }
}

@Composable
private fun StatBox(label: String, value: String) {
    Column(
        modifier = Modifier.width(58.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 10.dp, horizontal = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WeaponMatrix(title: String, weapons: List<WeaponProfile>, skillLabel: String) {
    SectionTitle(title)
    if (weapons.isEmpty()) {
        Text("No $title transcribed; consult the scanned datasheet.",
            style = MaterialTheme.typography.bodySmall)
        return
    }
    val nameWidth = 196.dp
    val rangeWidth = 70.dp
    val numericWidth = 50.dp
    val tableWidth = nameWidth + rangeWidth + numericWidth * 5
    Column(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        Row(Modifier.width(tableWidth).background(MaterialTheme.colorScheme.surfaceVariant)) {
            MatrixCell("WEAPON", nameWidth, header = true)
            MatrixCell("RANGE", rangeWidth, header = true)
            listOf("A", skillLabel, "S", "AP", "D").forEach {
                MatrixCell(it, numericWidth, header = true)
            }
        }
        weapons.forEachIndexed { index, weapon ->
            Row(Modifier.width(tableWidth)
                .background(if (index % 2 == 0) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))) {
                MatrixCell(weapon.name, nameWidth, emphasis = true)
                MatrixCell(weapon.range, rangeWidth)
                MatrixCell(weapon.attacks, numericWidth)
                MatrixCell(weapon.skill, numericWidth)
                MatrixCell(weapon.strength, numericWidth)
                MatrixCell(weapon.armourPenetration.toString(), numericWidth)
                MatrixCell(weapon.damage, numericWidth)
            }
            if (weapon.abilities.isNotEmpty()) {
                WeaponAbilityChips(
                    abilities = weapon.abilities,
                    modifier = Modifier.width(tableWidth)
                        .padding(start = 8.dp, top = 2.dp, bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MatrixCell(value: String, width: Dp, header: Boolean = false, emphasis: Boolean = false) {
    Box(
        modifier = Modifier.width(width).heightIn(min = 42.dp).padding(horizontal = 5.dp, vertical = 9.dp),
        contentAlignment = if (header || !emphasis) Alignment.Center else Alignment.CenterStart
    ) {
        Text(
            text = value,
            style = if (header) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
            fontWeight = if (header || emphasis) FontWeight.Bold else FontWeight.Normal,
            maxLines = if (header) 1 else 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AbilityPanel(unit: DetailedUnit) {
    SectionTitle("Datasheet abilities")
    if (unit.abilities.isEmpty()) Text("Ability text not fully transcribed; use source scan.",
        style = MaterialTheme.typography.bodySmall)
    unit.abilities.forEach { ability ->
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(ability.name, color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(ability.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
    if (unit.keywords.isNotEmpty()) {
        SectionTitle("Keywords")
        Text(unit.keywords.joinToString("  •  "), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CodexReferencePanel(unit: DetailedUnit) {
    val index = unit.codexSections.find { it.kind == "ABILITY_INDEX" }
    val summary = unit.codexSections.find { it.kind == "ABILITY_SUMMARY" }
    val missing = unit.codexSections.find { it.kind == "SOURCE_WARNING" }
    if (index == null && summary == null && missing == null) return
    SectionTitle("Codex source index")
    index?.let {
        Text("Listed abilities: ${it.text}", style = MaterialTheme.typography.bodyMedium)
    }
    summary?.let {
        Text(it.text, style = MaterialTheme.typography.bodyMedium)
        Text("Summary only — consult photographed datasheet for the complete effect.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    missing?.let {
        Text(it.text, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun CompositionPanel(unit: DetailedUnit) {
    SectionTitle("Unit composition and wargear")
    Text("Allowed roster sizes: ${unit.allowedModelCounts.sorted().joinToString(" / ")}")
    val composition = unit.codexSections.find { it.kind == "COMPOSITION" }
    val defaultEquipment = unit.codexSections.find { it.kind == "DEFAULT_EQUIPMENT" }
    if (composition != null) {
        Text("Printed composition", style = MaterialTheme.typography.titleSmall)
        Text(composition.text, style = MaterialTheme.typography.bodyMedium)
    }
    if (defaultEquipment != null) {
        Text("Default equipment", style = MaterialTheme.typography.titleSmall)
        Text(defaultEquipment.text, style = MaterialTheme.typography.bodyMedium)
    }
    if (unit.wargearOptions.isEmpty()) Text("Wargear transcription pending; consult scan.",
        style = MaterialTheme.typography.bodySmall)
    else unit.wargearOptions.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
    if (unit.eligibleBodyguardNames.isNotEmpty()) {
        Text("Leader — eligible bodyguards", style = MaterialTheme.typography.titleSmall)
        unit.eligibleBodyguardNames.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun PricePanel(unit: DetailedUnit) {
    SectionTitle("Point costs")
    unit.pointTiers.forEach { tier ->
        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${tier.modelCount} models · units ${tier.fromOccurrence}–${tier.throughOccurrence ?: "+"}",
                modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            Text("${tier.points} pts", style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold)
        }
    }
    if (unit.extraWargearCosts.isNotEmpty()) {
        HorizontalDivider()
        unit.extraWargearCosts.forEach { Text("${it.name}: +${it.pointsPerItem} pts each",
            style = MaterialTheme.typography.bodySmall) }
        Text("Additional wargear points are not yet in the roster calculator.",
            style = MaterialTheme.typography.labelSmall)
    }
}
