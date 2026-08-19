package io.github.andraantariksa.crates.feature_crates.data.source.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.andraantariksa.crates.feature_crates.data.source.local.converter.MoshiConverter
import io.github.andraantariksa.crates.feature_crates.data.source.local.dao.CookieDao
import io.github.andraantariksa.crates.feature_crates.data.source.local.dao.CrateDetailDao
import io.github.andraantariksa.crates.feature_crates.data.source.local.dao.CratesSummaryDao
import io.github.andraantariksa.crates.feature_crates.data.source.local.dao.UserDao
import io.github.andraantariksa.crates.feature_crates.data.source.local.model.cookie.CookieEntity
import io.github.andraantariksa.crates.feature_crates.data.source.local.model.crate_detail.CrateDetailEntity
import io.github.andraantariksa.crates.feature_crates.data.source.local.model.crates_summary.CratesSummaryEntity
import io.github.andraantariksa.crates.feature_crates.data.source.local.model.user.MyUserDBEntity

@Database(
    entities = [CratesSummaryEntity::class, CrateDetailEntity::class, CookieEntity::class, MyUserDBEntity::class],
    version = 2
)
@TypeConverters(MoshiConverter::class)
abstract class CratesDatabase : RoomDatabase() {
    abstract fun crateSummaryDao(): CratesSummaryDao
    abstract fun crateDetailDao(): CrateDetailDao
    abstract fun cookieDao(): CookieDao
    abstract fun userDao(): UserDao

    companion object {
        const val NAME = "crates"
    }
}