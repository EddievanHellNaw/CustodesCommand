
package com.example.custodescommand.feature.datasheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DatasheetListScreen(
    onOpenUnit: (String) -> Unit
) {
    var query by rememberSaveable {
        mutableStateOf("")
    }

    val filteredUnits = sampleUnits.filter { unit ->
        unit.name.contains(query, ignoreCase = true) ||
                unit.category.contains(query, ignoreCase = true)
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

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search units") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredUnits.isEmpty()) {
            Text("No units found.")
        } else {
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
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
    val unit = sampleUnits.firstOrNull {
        it.id == unitId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (unit == null) {
            Text("Datasheet not found.")
            return@Column
        }

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
            text = "Full characteristics, weapons and abilities will be imported in a later milestone.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
