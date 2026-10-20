package com.example.custodescommand.feature.reference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.custodescommand.core.rules.CoreRule
import com.example.custodescommand.core.rules.CoreRuleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CoreRulesUiState(
    val loading: Boolean = true,
    val rules: List<CoreRule> = emptyList(),
    val error: String? = null
)

class CoreRulesViewModel(private val repository: CoreRuleRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(CoreRulesUiState())
    val state: StateFlow<CoreRulesUiState> = mutableState.asStateFlow()
    init {
        viewModelScope.launch {
            try {
                val items = withContext(Dispatchers.IO) { repository.entries }
                mutableState.value = CoreRulesUiState(loading = false, rules = items)
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                mutableState.value = CoreRulesUiState(
                    loading = false, error = ex.message ?: "Unable to load bundled core rules"
                )
            }
        }
    }
}
