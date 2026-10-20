
package com.example.custodescommand.feature.rosters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp


@Composable
fun RostersScreen(
    state: RostersUiState,
    onCreateRoster: (String, Int) -> Unit,
    onDismissError: () -> Unit,
    onOpenRoster: (String) -> Unit,
    onPlayRoster: (String) -> Unit
) {
    var showCreateDialog by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "My Rosters",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Button(
            onClick = { showCreateDialog = true },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create New Roster")
        }

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error
            )

            TextButton(onClick = onDismissError) {
                Text("Dismiss error")
            }
        }

        when {
            state.isLoading -> {
                CircularProgressIndicator()
            }

            state.rosters.isEmpty() -> {
                Text(
                    text = "No saved rosters yet. Create your first army!",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> {
                LazyColumn(
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = state.rosters,
                        key = { it.id }
                    ) { roster ->
                        Card(
                            onClick = {
                                onOpenRoster(roster.id)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = roster.name,
                                    style = MaterialTheme
                                        .typography.titleLarge
                                )

                                Text(
                                    text = "Points limit: ${roster.pointsLimit}",
                                    style = MaterialTheme
                                        .typography.bodyMedium
                                )

                                TextButton(onClick = { onPlayRoster(roster.id) }) {
                                    Text("Play / Game Mode")
                                }

                                Text(
                                    text = "Tap to manage this army",
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

    if (showCreateDialog) {
        CreateRosterDialog(
            isSaving = state.isSaving,
            onDismiss = {
                showCreateDialog = false
            },
            onConfirm = { name, points ->
                onCreateRoster(name, points)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CreateRosterDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by rememberSaveable {
        mutableStateOf("")
    }

    var pointsInput by rememberSaveable {
        mutableStateOf("2000")
    }

    val points = pointsInput.toIntOrNull()

    val isValid =
        name.isNotBlank() &&
                points != null &&
                points > 0

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },

        title = {
            Text("New Army Roster")
        },

        text = {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Roster name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pointsInput,
                    onValueChange = { pointsInput = it },
                    label = { Text("Points limit") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },

        confirmButton = {
            TextButton(
                enabled = isValid && !isSaving,
                onClick = {
                    points?.let {
                        onConfirm(name.trim(), it)
                    }
                }
            ) {
                Text("Create")
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
