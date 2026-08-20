package io.github.andraantariksa.crates.feature_crates.data.source.local

import io.github.andraantariksa.crates.feature_crates.data.exception.NoCachedDataException
import io.github.andraantariksa.crates.feature_crates.data.source.local.database.CratesDatabase
import io.github.andraantariksa.crates.feature_crates.data.source.local.model.crate_detail.CrateDetailEntity
import io.github.andraantariksa.crates.feature_crates.data.source.local.model.crates_summary.CratesSummaryEntity
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.detail.CrateDetail
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.summary.CratesSummary as CratesSummaryModel
import io.github.andraantariksa.crates.feature_crates.domain.entity.summary.CratesSummary

class CratesIoDataSourceLocalImpl(
    cratesDatabase: CratesDatabase,
) : CratesIoDataSourceLocal {
    private val cratesSummaryDao = cratesDatabase.crateSummaryDao()
    private val crateDetailDao = cratesDatabase.crateDetailDao()

    override suspend fun getCratesSummary(): CratesSummary =
        cratesSummaryDao.get()?.cratesSummary ?: throw NoCachedDataException()

    override suspend fun saveCratesSummary(cratesSummary: CratesSummary) {
        cratesSummaryDao.add(CratesSummaryEntity(cratesSummary = cratesSummary as CratesSummaryModel))
    }

    override suspend fun getCrateDetails(id: String): CrateDetail =
        crateDetailDao.get(id)?.crateDetail ?: throw NoCachedDataException()

    override suspend fun saveCrateDetails(id: String, crateDetail: CrateDetail) {
        crateDetailDao.add(CrateDetailEntity(id = id, crateDetail = crateDetail))
    }
}
