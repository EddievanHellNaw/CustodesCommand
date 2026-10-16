
package com.example.custodescommand.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.custodescommand.navigation.MainTab
import com.example.custodescommand.feature.common.SectionPlaceholder
import com.example.custodescommand.feature.datasheets.DatasheetListScreen
import com.example.custodescommand.feature.datasheets.DatasheetDetailScreen

private const val UNIT_PREFIX = "unit:"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustodesApp() {

    val backStack = rememberSaveable(
        saver = listSaver<SnapshotStateList<String>, String>(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        mutableStateListOf(MainTab.Rosters.key)
    }

    val currentRoute = backStack.last()
    val selectedTab = backStack.first()

    val screenTitle = if (currentRoute.startsWith(UNIT_PREFIX)) {
        "Datasheet"
    } else {
        MainTab.entries
            .firstOrNull { it.key == currentRoute }
            ?.label ?: "Custodes Command"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    if (backStack.size > 1) {
                        IconButton(
                            onClick = {
                                backStack.removeAt(backStack.lastIndex)
                            }
                        ) {
                            Text(
                                text = "←",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            )
        },

        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->

                    NavigationBarItem(
                        selected = selectedTab == tab.key,

                        onClick = {
                            if (
                                backStack.size > 1 ||
                                selectedTab != tab.key
                            ) {
                                backStack.clear()
                                backStack.add(tab.key)
                            }
                        },

                        icon = { Text(tab.symbol) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->

        NavDisplay(
            modifier = Modifier.padding(innerPadding),
            backStack = backStack,

            onBack = {
                if (backStack.size > 1) {
                    backStack.removeAt(backStack.lastIndex)
                }
            },

            entryProvider = { route ->

                when {
                    route == MainTab.Rosters.key ->
                        NavEntry(route) {
                            SectionPlaceholder(
                                title = "My Rosters",
                                description = "Your saved Custodes armies will appear here."
                            )
                        }

                    route == MainTab.Datasheets.key ->
                        NavEntry(route) {
                            DatasheetListScreen(
                                onOpenUnit = { unitId ->
                                    backStack.add("$UNIT_PREFIX$unitId")
                                }
                            )
                        }

                    route == MainTab.Detachments.key ->
                        NavEntry(route) {
                            SectionPlaceholder(
                                title = "Detachments",
                                description = "Detachment rules, stratagems and enhancements."
                            )
                        }

                    route == MainTab.Rules.key ->
                        NavEntry(route) {
                            SectionPlaceholder(
                                title = "Core Rules",
                                description = "A searchable reference for Warhammer 40,000."
                            )
                        }

                    route.startsWith(UNIT_PREFIX) ->
                        NavEntry(route) {
                            DatasheetDetailScreen(
                                unitId = route.removePrefix(UNIT_PREFIX)
                            )
                        }

                    else -> error("Unknown route: $route")
                }
            }
        )
    }
}
