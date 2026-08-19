package io.github.andraantariksa.crates.feature_crates.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.andraantariksa.crates.feature_crates.data.source.local.model.crates_summary.CratesSummaryEntity

@Dao
interface CratesSummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(entity: CratesSummaryEntity)

    @Query("SELECT * FROM crates_summary WHERE id = 1")
    suspend fun get(): CratesSummaryEntity?
}
