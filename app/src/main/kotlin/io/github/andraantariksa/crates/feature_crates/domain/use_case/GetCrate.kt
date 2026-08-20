package io.github.andraantariksa.crates.feature_crates.domain.use_case

import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.detail.CrateDetail
import io.github.andraantariksa.crates.feature_crates.domain.repository.CratesIoRepository

class GetCrate(
    private val repository: CratesIoRepository
) {
    suspend operator fun invoke(id: String): Result<CrateDetail> = repository.getCrateDetails(id)
}
