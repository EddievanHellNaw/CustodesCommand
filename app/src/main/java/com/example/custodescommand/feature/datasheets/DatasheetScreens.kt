
package com.example.custodescommand.feature.datasheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.custodescommand.core.data.SampleCustodesCatalog

@Composable
fun DatasheetListScreen(
    onOpenUnit: (String) -> Unit
) {
    var query by rememberSaveable {
        mutableStateOf("")
    }

    val filteredUnits = SampleCustodesCatalog.units.filter {
        it.name.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Unit Datasheets",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search units") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = filteredUnits,
                key = { it.id }
            ) { unit ->

                Card(
                    onClick = { onOpenUnit(unit.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = unit.name,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = unit.category,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DatasheetDetailScreen(
    unitId: String
) {
    val unit = SampleCustodesCatalog.units.firstOrNull {
        it.id == unitId
    }

    if (unit == null) {
        Text("Datasheet not found.")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = unit.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = unit.category,
            style = MaterialTheme.typography.titleSmall
        )

        Text(
            text = "Unit sizes: " +
                    unit.allowedModelCounts.sorted().joinToString(", ")
        )

        HorizontalDivider()

        Text(
            text = "Characteristics",
            style = MaterialTheme.typography.titleLarge
        )

        val stats = unit.characteristics

        if (stats == null) {
            Text("Profile awaiting transcription.")
        } else {
            Text(
                "M ${stats.movementInches}\"   " +
                        "T ${stats.toughness}   " +
                        "Sv ${stats.armourSave}"
            )
            Text(
                "W ${stats.wounds}   " +
                        "Ld ${stats.leadership}   " +
                        "OC ${stats.objectiveControl}"
            )
            Text(
                "Invulnerable save: " +
                        (stats.invulnerableSave ?: "None listed")
            )
        }

        HorizontalDivider()

        Text(
            text = "Weapons",
            style = MaterialTheme.typography.titleLarge
        )

        if (unit.weapons.isEmpty()) {
            Text("Weapon profiles awaiting transcription.")
        }

        unit.weapons.forEach { weapon ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        weapon.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "${weapon.type} | Range ${weapon.range}"
                    )

                    Text(
                        "A ${weapon.attacks}  " +
                                "Skill ${weapon.skill}  " +
                                "S ${weapon.strength}"
                    )

                    Text(
                        "AP ${weapon.armourPenetration}  " +
                                "D ${weapon.damage}"
                    )

                    if (weapon.abilities.isNotEmpty()) {
                        Text(
                            weapon.abilities.joinToString(", ")
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        Text(
            text = "Source",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            unit.source?.let {
                "${it.document}, page ${it.page ?: "N/A"}"
            } ?: "Source not recorded"
        )
    }
}
