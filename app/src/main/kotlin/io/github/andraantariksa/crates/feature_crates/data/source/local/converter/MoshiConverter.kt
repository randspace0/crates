package io.github.andraantariksa.crates.feature_crates.data.source.local.converter

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.detail.CrateDetail
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.summary.CratesSummary

class MoshiConverter {
    private val moshi = Moshi.Builder().build()
    private val cratesSummaryAdapter = moshi.adapter(CratesSummary::class.java)
    private val crateDetailAdapter = moshi.adapter(CrateDetail::class.java)

    @TypeConverter
    fun stringToCratesSummary(string: String): CratesSummary {
        return cratesSummaryAdapter.fromJson(string) ?: throw IllegalStateException()
    }

    @TypeConverter
    fun cratesSummaryToString(cratesSummary: CratesSummary): String {
        return cratesSummaryAdapter.toJson(cratesSummary)
    }

    @TypeConverter
    fun stringToCrateDetail(string: String): CrateDetail {
        return crateDetailAdapter.fromJson(string) ?: throw IllegalStateException()
    }

    @TypeConverter
    fun crateDetailToString(crateDetail: CrateDetail): String {
        return crateDetailAdapter.toJson(crateDetail)
    }
}