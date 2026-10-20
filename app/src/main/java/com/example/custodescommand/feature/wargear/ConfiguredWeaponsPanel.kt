package com.example.custodescommand.feature.wargear

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.custodescommand.core.domain.model.ConfiguredWeaponsResult
import com.example.custodescommand.core.domain.model.EquippedWeapon
import com.example.custodescommand.feature.reference.WeaponAbilityChips

/** Reusable on the equipment editor and a future active-roster Game Mode screen. */
@Composable
fun ConfiguredWeaponsPanel(result: ConfiguredWeaponsResult?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("EQUIPPED WEAPON PROFILES", style = MaterialTheme.typography.titleMedium)
        if (result == null) {
            Text("Profile lookup is not available yet.", style = MaterialTheme.typography.bodySmall)
            return@Column
        }
        if (!result.supported) {
            Text("Default loadout not verified for this datasheet. Consult the photographed source page.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall)
            return@Column
        }
        Text("The entries below describe the selected models, not every weapon option on the datasheet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        result.weapons.groupBy { it.modelIndex }.forEach { (model, weapons) ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Model $model", style = MaterialTheme.typography.titleSmall)
                    weapons.forEach { EquippedWeaponRow(it) }
                }
            }
        }
        result.warnings.forEach { warning ->
            Text(warning, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun EquippedWeaponRow(weapon: EquippedWeapon) {
    val profile = weapon.profile
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text("${if (weapon.quantity > 1) "${weapon.quantity} × " else ""}${weapon.name}",
            style = MaterialTheme.typography.bodyMedium)
        if (profile == null) {
            Text("Profile awaiting verified transcription", color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Range ${profile.range}", style = MaterialTheme.typography.labelSmall)
                Text("A ${profile.attacks}", style = MaterialTheme.typography.labelSmall)
                Text("${if (profile.type.name == "RANGED") "BS" else "WS"} ${profile.skill}",
                    style = MaterialTheme.typography.labelSmall)
                Text("S ${profile.strength}", style = MaterialTheme.typography.labelSmall)
                Text("AP ${profile.armourPenetration}", style = MaterialTheme.typography.labelSmall)
                Text("D ${profile.damage}", style = MaterialTheme.typography.labelSmall)
            }
            if (profile.abilities.isNotEmpty()) {
                WeaponAbilityChips(abilities = profile.abilities)
            }
        }
    }
}
