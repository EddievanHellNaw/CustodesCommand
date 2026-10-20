package com.example.custodescommand.feature.media

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.custodescommand.core.data.media.UnitPhotoStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class UnitPhotosUiState(
    val busyUnitIds: Set<String> = emptySet(),
    val errorMessage: String? = null
)

class UnitPhotosViewModel(private val store: UnitPhotoStore) : ViewModel() {
    val revision: StateFlow<Long> = store.revision
    private val _state = MutableStateFlow(UnitPhotosUiState())
    val state: StateFlow<UnitPhotosUiState> = _state.asStateFlow()

    fun photoFor(instanceId: String): File? = store.findPhoto(instanceId)

    fun import(instanceId: String, uri: Uri) = execute(instanceId) {
        store.importPhoto(instanceId, uri)
    }

    fun remove(instanceId: String) = execute(instanceId) {
        store.deletePhoto(instanceId)
    }

    private fun execute(id: String, action: suspend () -> Unit) {
        if (id in _state.value.busyUnitIds) return
        _state.update { it.copy(busyUnitIds = it.busyUnitIds + id, errorMessage = null) }
        viewModelScope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                _state.update { it.copy(errorMessage = exception.message ?: "Unable to save photo.") }
            } finally {
                _state.update { it.copy(busyUnitIds = it.busyUnitIds - id) }
            }
        }
    }

    fun dismissError() = _state.update { it.copy(errorMessage = null) }
}
