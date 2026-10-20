
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

import com.example.custodescommand.feature.wargear.WargearEditorViewModel
import com.example.custodescommand.feature.wargear.WargearEditorScreen
import com.example.custodescommand.feature.gamemode.GameModeViewModel
import com.example.custodescommand.feature.gamemode.GameModeScreen

import com.example.custodescommand.feature.media.UnitPhotosViewModel
import com.example.custodescommand.feature.rosters.RosterDeletionViewModel
import com.example.custodescommand.feature.rosters.RosterDeletionState
import com.example.custodescommand.feature.rosters.RosterCommandScreen

import com.example.custodescommand.feature.reference.CoreRulesViewModel
import com.example.custodescommand.feature.reference.CoreRulesScreen

private const val UNIT_PREFIX = "unit:"
private const val ROSTER_PREFIX = "roster:"
private const val SOURCE_PAGE_PREFIX = "source-page:"
private const val DETACHMENT_PREFIX = "detachment:"
private const val ROSTER_DETACHMENTS_PREFIX = "roster-detachments:"
private const val ROSTER_ENHANCEMENTS_PREFIX = "roster-enhancements:"
private const val WARGEAR_PREFIX = "wargear:"
private const val GAME_PREFIX = "game:"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustodesApp() {

    val application = LocalContext.current.applicationContext
            as CustodesApplication

    val container = application.container

    val unitPhotosFactory = remember(container) {
        viewModelFactory {
            initializer { UnitPhotosViewModel(container.unitPhotoStore) }
        }
    }
    val photoVm: UnitPhotosViewModel = viewModel(factory = unitPhotosFactory)
    val photoState by photoVm.state.collectAsStateWithLifecycle()
    val photoRevision by photoVm.revision.collectAsStateWithLifecycle()

    val deletionFactory = remember(container) {
        viewModelFactory {
            initializer {
                RosterDeletionViewModel(
                    rosters = container.rosterRepository,
                    photos = container.unitPhotoStore
                )
            }
        }
    }
    val deletionVm: RosterDeletionViewModel = viewModel(factory = deletionFactory)
    val deletionState by deletionVm.state.collectAsStateWithLifecycle()

    val coreRulesFactory = remember(container) {
        viewModelFactory {
            initializer { CoreRulesViewModel(container.coreRulesRepository) }
        }
    }
    val coreRulesVm: CoreRulesViewModel = viewModel(factory = coreRulesFactory)
    val coreRulesState by coreRulesVm.state.collectAsStateWithLifecycle()

    val gameModeFactory = remember(container) {
        viewModelFactory {
            initializer {
                GameModeViewModel(
                    rosters = container.rosterRepository,
                    references = container.referenceRepository,
                    details = container.unitDetailRepository,
                    detachments = container.detachmentRepository,
                    enhancements = container.rosterEnhancementRepository,
                    wargear = container.wargearRepository,
                    initializeData = container::initializeData
                )
            }
        }
    }

    val gameModeVm: GameModeViewModel = viewModel(factory = gameModeFactory)
    val gameModeState by gameModeVm.state.collectAsStateWithLifecycle()

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

    val wargearFactory = remember(container) {
        viewModelFactory {
            initializer {
                WargearEditorViewModel(
                    rosters = container.rosterRepository,
                    references = container.referenceRepository,
                    wargear = container.wargearRepository,
                    details = container.unitDetailRepository,
                    initializeData = container::initializeData
                )
            }
        }
    }
    val wargearVm: WargearEditorViewModel = viewModel(factory = wargearFactory)
    val wargearState by wargearVm.state.collectAsStateWithLifecycle()

    val enhancementFactory = remember(container) {
        viewModelFactory {
            initializer {
                RosterEnhancementsViewModel(
                    rosterRepository = container.rosterRepository,
                    referenceRepository = container.referenceRepository,
                    enhancements = container.rosterEnhancementRepository,
                    initializeData = container::initializeData,
                    wargear = container.wargearRepository,
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

    LaunchedEffect(deletionState) {
        val deleted = deletionState as? RosterDeletionState.Deleted
        if (deleted != null) {
            // If the deleted roster is currently in the stack, remove it.
            backStack.remove("$ROSTER_PREFIX${deleted.rosterId}")
            // If a nested route somehow remained, return to the roster list.
            backStack.clear()
            backStack.add(MainTab.Rosters.key)
            deletionVm.acknowledge()
        }
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
                    wargearRepository = container.wargearRepository,
                    unitPhotoStore = container.unitPhotoStore
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
                                },
                                onPlayRoster = { rosterId ->
                                    backStack.add("$GAME_PREFIX$rosterId")
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

                    route == MainTab.Rules.key -> NavEntry(route) {
                        CoreRulesScreen(
                            state = coreRulesState,
                            officialUrl = container.coreRulesRepository.officialPdfUrl
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

                            RosterCommandScreen(
                                rosterId = rosterId,
                                state = rosterEditorState,
                                photoState = photoState,
                                photoRevision = photoRevision,
                                photoFor = photoVm::photoFor,
                                deletionState = deletionState,
                                onAddUnit = rosterEditorViewModel::addUnit,
                                onRemoveUnit = rosterEditorViewModel::removeUnit,
                                onEquipUnit = { instanceId ->
                                    backStack.add("$WARGEAR_PREFIX$rosterId:$instanceId")
                                },
                                onOpenUnit = { unitId ->
                                    backStack.add("$UNIT_PREFIX$unitId")
                                },
                                onManageDetachments = {
                                    backStack.add("$ROSTER_DETACHMENTS_PREFIX$rosterId")
                                },
                                onManageEnhancements = {
                                    backStack.add("$ROSTER_ENHANCEMENTS_PREFIX$rosterId")
                                },
                                onOpenRules = {
                                    backStack.clear()
                                    backStack.add(MainTab.Rules.key)
                                },
                                onStartGame = {
                                    backStack.add("$GAME_PREFIX$rosterId")
                                },
                                onDeleteRoster = { deletionVm.deleteRoster(rosterId) },
                                onPhotoSelected = photoVm::import,
                                onRemovePhoto = photoVm::remove
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

                    route.startsWith(WARGEAR_PREFIX) -> NavEntry(route) {
                        val parts = route.removePrefix(WARGEAR_PREFIX).split(":", limit = 2)
                        require(parts.size == 2) { "Invalid wargear route" }
                        val rosterId = parts[0]
                        val instanceId = parts[1]
                        LaunchedEffect(rosterId, instanceId) { wargearVm.load(rosterId, instanceId) }
                        WargearEditorScreen(
                            state = wargearState,
                            onSelect = wargearVm::setSelected,
                            onSetVexillaBearer = wargearVm::setVexillaBearer,
                            onResize = wargearVm::resize,
                            onDismissError = wargearVm::dismissError,
                            onOpenSourcePage = { page -> backStack.add("$SOURCE_PAGE_PREFIX$page") }
                        )
                    }

                    route.startsWith(GAME_PREFIX) -> NavEntry(route) {
                        val rosterId = route.removePrefix(GAME_PREFIX)

                        LaunchedEffect(rosterId) {
                            gameModeVm.open(rosterId)
                        }

                        GameModeScreen(
                            rosterId = rosterId,
                            state = gameModeState,
                            onOpenDatasheet = { unitId ->
                                backStack.add("$UNIT_PREFIX$unitId")
                            },
                            onOpenDetachment = { detachmentId ->
                                backStack.add("$DETACHMENT_PREFIX$detachmentId")
                            },
                            onOpenSourcePage = { page ->
                                backStack.add("$SOURCE_PAGE_PREFIX$page")
                            },
                            onOpenCoreRules = {
                                // Core Rules is still a placeholder from the earlier lessons.
                                // Switch tabs, rather than stacking a root destination under Rosters.
                                backStack.clear()
                                backStack.add(MainTab.Rules.key)
                            }
                        )
                    }

                    else -> error("Unknown route: $route")
                }
            }
        )
    }
}
