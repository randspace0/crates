package io.github.andraantariksa.crates.feature_crates.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.andraantariksa.crates.feature_crates.data.source.local.model.crate_detail.CrateDetailEntity

@Dao
interface CrateDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(entity: CrateDetailEntity)

    @Query("SELECT * FROM crate_detail WHERE id = :id")
    suspend fun get(id: String): CrateDetailEntity?
}
