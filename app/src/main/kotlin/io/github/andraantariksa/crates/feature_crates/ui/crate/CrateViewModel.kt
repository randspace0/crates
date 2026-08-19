package io.github.andraantariksa.crates.feature_crates.ui.crate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.andraantariksa.crates.common.util.CratesResult
import io.github.andraantariksa.crates.common.util.toCratesResult
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.detail.CrateDetail
import io.github.andraantariksa.crates.feature_crates.domain.repository.CratesIoRepository
import kotlinx.coroutines.launch

class CrateViewModel(
    private val cratesIoRepository: CratesIoRepository
) : ViewModel() {
    var crateDetail by mutableStateOf<CratesResult<CrateDetail>>(CratesResult.Loading())
        private set

    fun load(crateId: String?) {
        if (crateId == null) {
            crateDetail = CratesResult.Error(IllegalArgumentException("Missing crate id"))
            return
        }
        viewModelScope.launch {
            crateDetail = cratesIoRepository.getCrateDetails(crateId).toCratesResult()
        }
    }
}
