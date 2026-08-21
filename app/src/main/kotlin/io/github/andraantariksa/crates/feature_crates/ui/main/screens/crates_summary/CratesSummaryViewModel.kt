package io.github.andraantariksa.crates.feature_crates.ui.main.screens.crates_summary

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.andraantariksa.crates.common.util.toCratesResult
import io.github.andraantariksa.crates.feature_crates.domain.repository.CratesIoRepository
import io.github.andraantariksa.crates.feature_crates.ui.main.screens.UIEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CratesSummaryViewModel(
    private val cratesIoRepository: CratesIoRepository
) : ViewModel() {
    var cratesSummaryState by mutableStateOf(CratesSummaryState())
        private set

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadCratesSummary()
    }

    private fun loadCratesSummary() = viewModelScope.launch {
        val result = cratesIoRepository.getCratesSummary()
        result.exceptionOrNull()?.let { android.util.Log.e("CratesSummaryVM", "load failed", it) }
        cratesSummaryState = cratesSummaryState.copy(cratesSummary = result.toCratesResult())
    }
}
