package com.example.custodescommand.feature.rosters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.custodescommand.core.data.media.UnitPhotoStore
import com.example.custodescommand.core.data.repository.RosterRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RosterDeletionState {
    data object Idle : RosterDeletionState
    data class Deleting(val rosterId: String) : RosterDeletionState
    data class Deleted(val rosterId: String) : RosterDeletionState
    data class Failed(val rosterId: String, val message: String) : RosterDeletionState
}

class RosterDeletionViewModel(
    private val rosters: RosterRepository,
    private val photos: UnitPhotoStore
) : ViewModel() {
    private val _state = MutableStateFlow<RosterDeletionState>(RosterDeletionState.Idle)
    val state: StateFlow<RosterDeletionState> = _state.asStateFlow()

    fun deleteRoster(rosterId: String) {
        if (_state.value is RosterDeletionState.Deleting) return
        _state.value = RosterDeletionState.Deleting(rosterId)
        viewModelScope.launch {
            try {
                // Read photo IDs BEFORE deleting their owning roster-unit rows.
                val roster = requireNotNull(rosters.getRoster(rosterId)) {
                    "Roster no longer exists."
                }
                rosters.deleteRoster(rosterId) // Room FK CASCADE handles related rows.
                // File-system cleanup is best effort: deleting a roster must not fail
                // after Room already committed successfully.
                roster.units.forEach { unit ->
                    runCatching { photos.deletePhoto(unit.instanceId) }
                }
                _state.value = RosterDeletionState.Deleted(rosterId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = RosterDeletionState.Failed(
                    rosterId, error.message ?: "Could not delete this roster."
                )
            }
        }
    }

    fun acknowledge() { _state.value = RosterDeletionState.Idle }
}
