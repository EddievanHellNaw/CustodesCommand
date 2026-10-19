
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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

import com.example.custodescommand.CustodesApplication
import com.example.custodescommand.feature.datasheets.DatasheetsViewModel
import com.example.custodescommand.feature.rosters.RostersViewModel
import com.example.custodescommand.feature.rosters.RostersScreen

import androidx.compose.runtime.LaunchedEffect
import com.example.custodescommand.feature.rosters.RosterEditorViewModel
import com.example.custodescommand.feature.rosters.RosterEditorScreen

import com.example.custodescommand.feature.datasheets.UnitDetailViewModel
import com.example.custodescommand.feature.datasheets.UnitDetailScreen
import com.example.custodescommand.feature.datasheets.SourcePageScreen

import com.example.custodescommand.feature.detachments.*
import com.example.custodescommand.feature.enhancements.RosterEnhancementsViewModel
import com.example.custodescommand.feature.enhancements.RosterEnhancementsScreen

private const val UNIT_PREFIX = "unit:"
private const val ROSTER_PREFIX = "roster:"
private const val SOURCE_PAGE_PREFIX = "source-page:"
private const val DETACHMENT_PREFIX = "detachment:"
private const val ROSTER_DETACHMENTS_PREFIX = "roster-detachments:"
private const val ROSTER_ENHANCEMENTS_PREFIX = "roster-enhancements:"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustodesApp() {

    val application = LocalContext.current.applicationContext
            as CustodesApplication

    val container = application.container

    val detailFactory = remember(container) {
        viewModelFactory {
            initializer {
                UnitDetailViewModel(
                    repository = container.unitDetailRepository,
                    initializeData = container::initializeData
                )
            }
        }
    }
    val detailViewModel: UnitDetailViewModel = viewModel(factory = detailFactory)
    val detailState by detailViewModel.state.collectAsStateWithLifecycle()

    val enhancementFactory = remember(container) {
        viewModelFactory {
            initializer {
                RosterEnhancementsViewModel(
                    rosterRepository = container.rosterRepository,
                    referenceRepository = container.referenceRepository,
                    enhancements = container.rosterEnhancementRepository,
                    initializeData = container::initializeData
                )
            }
        }
    }
    val enhancementVm: RosterEnhancementsViewModel = viewModel(factory = enhancementFactory)
    val enhancementState by enhancementVm.state.collectAsStateWithLifecycle()

    val detachmentsFactory = remember(container) {
        viewModelFactory {
            initializer {
                DetachmentsViewModel(
                    repository = container.detachmentRepository,
                    initializeData = container::initializeData
                )
            }
        }
    }
    val detachmentVm: DetachmentsViewModel = viewModel(factory = detachmentsFactory)
    val detachmentsState by detachmentVm.state.collectAsStateWithLifecycle()

    val rosterDetachmentsFactory = remember(container) {
        viewModelFactory {
            initializer {
                RosterDetachmentsViewModel(
                    repository = container.detachmentRepository,
                    initializeData = container::initializeData
                )
            }
        }
    }
    val rosterDetachmentsVm: RosterDetachmentsViewModel =
        viewModel(factory = rosterDetachmentsFactory)
    val rosterDetachmentsState by
    rosterDetachmentsVm.state.collectAsStateWithLifecycle()

    val datasheetsFactory = remember(container) {
        viewModelFactory {
            initializer {
                DatasheetsViewModel(
                    repository = container.referenceRepository,
                    initializeData = container::initializeData
                )
            }
        }
    }

    val rostersFactory = remember(container) {
        viewModelFactory {
            initializer {
                RostersViewModel(
                    repository = container.rosterRepository,
                    initializeData = container::initializeData
                )
            }
        }
    }

    val datasheetsViewModel: DatasheetsViewModel = viewModel(
        factory = datasheetsFactory
    )

    val rostersViewModel: RostersViewModel = viewModel(
        factory = rostersFactory
    )

    val datasheetsState by
    datasheetsViewModel.uiState.collectAsStateWithLifecycle()

    val rostersState by
    rostersViewModel.uiState.collectAsStateWithLifecycle()

    val backStack = rememberSaveable(
        saver = listSaver<SnapshotStateList<String>, String>(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        mutableStateListOf(MainTab.Rosters.key)
    }


    val rosterEditorFactory = remember(container) {
        viewModelFactory {
            initializer {
                RosterEditorViewModel(
                    rosterRepository =
                        container.rosterRepository,
                    referenceRepository =
                        container.referenceRepository,
                    initializeData =
                        container::initializeData,
                    enhancementRepository = container.rosterEnhancementRepository,
                )
            }
        }
    }

    val rosterEditorViewModel: RosterEditorViewModel =
        viewModel(
            factory = rosterEditorFactory
        )

    val rosterEditorState by
    rosterEditorViewModel.uiState
        .collectAsStateWithLifecycle()


    val currentRoute = backStack.last()
    val selectedTab = backStack.first()


    val screenTitle = when {

        currentRoute.startsWith(ROSTER_PREFIX) ->
            "Roster Editor"

        currentRoute.startsWith(UNIT_PREFIX) ->
            "Datasheet"

        else ->
            MainTab.entries
                .firstOrNull {
                    it.key == currentRoute
                }
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
                            RostersScreen(
                                state = rostersState,
                                onCreateRoster =
                                    rostersViewModel::createRoster,
                                onDismissError =
                                    rostersViewModel::dismissError,
                                onOpenRoster = { rosterId ->
                                    backStack.add(
                                        "$ROSTER_PREFIX$rosterId"
                                    )
                                }
                            )
                        }




                    route == MainTab.Datasheets.key ->
                        NavEntry(route) {
                            DatasheetListScreen(
                                state = datasheetsState,
                                onOpenUnit = { unitId ->
                                    backStack.add("$UNIT_PREFIX$unitId")
                                }
                            )
                        }

                    route == MainTab.Detachments.key -> NavEntry(route) {
                        DetachmentListScreen(
                            state = detachmentsState,
                            onOpen = { id -> backStack.add("$DETACHMENT_PREFIX$id") }
                        )
                    }

                    route == MainTab.Rules.key ->
                        NavEntry(route) {
                            SectionPlaceholder(
                                title = "Core Rules",
                                description = "A searchable reference for Warhammer 40,000."
                            )
                        }

                    route.startsWith(UNIT_PREFIX) -> NavEntry(route) {
                        val unitId = route.removePrefix(UNIT_PREFIX)
                        LaunchedEffect(unitId) { detailViewModel.open(unitId) }
                        UnitDetailScreen(
                            requestedUnitId = unitId,
                            state = detailState,
                            onOpenSourcePage = { page ->
                                backStack.add("$SOURCE_PAGE_PREFIX$page")
                            }
                        )
                    }

                    route.startsWith(ROSTER_PREFIX) ->
                        NavEntry(route) {

                            val rosterId =
                                route.removePrefix(ROSTER_PREFIX)

                            LaunchedEffect(rosterId) {
                                rosterEditorViewModel.loadRoster(rosterId)
                            }

                            RosterEditorScreen(
                                rosterId = rosterId,
                                state = rosterEditorState,
                                onAddUnit =
                                    rosterEditorViewModel::addUnit,
                                onRemoveUnit =
                                    rosterEditorViewModel::removeUnit,
                                onManageDetachments = {
                                    backStack.add("$ROSTER_DETACHMENTS_PREFIX$rosterId")
                                },
                                onManageEnhancements = {
                                    backStack.add("$ROSTER_ENHANCEMENTS_PREFIX$rosterId")
                                },
                                onDismissError =
                                    rosterEditorViewModel::dismissError
                            )
                        }

                    route.startsWith(SOURCE_PAGE_PREFIX) -> NavEntry(route) {
                        val page = route.removePrefix(SOURCE_PAGE_PREFIX).toIntOrNull()
                        if (page != null && page in 2..30) {
                            SourcePageScreen(page)
                        } else {
                            Text("Source page unavailable")
                        }
                    }

                    route.startsWith(DETACHMENT_PREFIX) -> NavEntry(route) {
                        val detachmentId = route.removePrefix(DETACHMENT_PREFIX)
                        LaunchedEffect(detachmentId) { detachmentVm.open(detachmentId) }
                        DetachmentDetailScreen(
                            requestedId = detachmentId,
                            state = detachmentsState,
                            onOpenSourcePage = { page ->
                                backStack.add("$SOURCE_PAGE_PREFIX$page")
                            }
                        )
                    }

                    route.startsWith(ROSTER_DETACHMENTS_PREFIX) -> NavEntry(route) {
                        val rosterId = route.removePrefix(ROSTER_DETACHMENTS_PREFIX)
                        LaunchedEffect(rosterId) { rosterDetachmentsVm.loadRoster(rosterId) }
                        RosterDetachmentsScreen(
                            rosterId = rosterId,
                            state = rosterDetachmentsState,
                            onToggle = rosterDetachmentsVm::setSelected,
                            onOpenDetail = { id -> backStack.add("$DETACHMENT_PREFIX$id") }
                        )
                    }

                    route.startsWith(ROSTER_ENHANCEMENTS_PREFIX) -> NavEntry(route) {
                        val rosterId = route.removePrefix(ROSTER_ENHANCEMENTS_PREFIX)
                        LaunchedEffect(rosterId) { enhancementVm.load(rosterId) }
                        RosterEnhancementsScreen(
                            rosterId = rosterId,
                            state = enhancementState,
                            onAssign = enhancementVm::assign,
                            onRemove = enhancementVm::remove,
                            onDismissError = enhancementVm::dismissError,
                            onOpenSourcePage = { page ->
                                backStack.add("$SOURCE_PAGE_PREFIX$page")
                            }
                        )
                    }

                    else -> error("Unknown route: $route")
                }
            }
        )
    }
}
