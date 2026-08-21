package io.github.andraantariksa.crates.feature_crates.data.source.remote.model.summary


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.github.andraantariksa.crates.feature_crates.domain.entity.crate.Crate

@JsonClass(generateAdapter = true)
data class MostDownloaded(
    @Json(name = "badges")
    val badges: Any?,
    @Json(name = "categories")
    val categories: Any?,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "description")
    override val description: String,
    @Json(name = "documentation")
    val documentation: String?,
    @Json(name = "downloads")
    val downloads: Long,
    @Json(name = "exact_match")
    val exactMatch: Boolean,
    @Json(name = "homepage")
    val homepage: String?,
    @Json(name = "id")
    override val id: String,
    @Json(name = "keywords")
    val keywords: Any?,
    @Json(name = "links")
    val links: Links,
    @Json(name = "max_stable_version")
    val maxStableVersion: String,
    @Json(name = "max_version")
    override val maxVersion: String,
    @Json(name = "name")
    override val name: String,
    @Json(name = "newest_version")
    val newestVersion: String,
    @Json(name = "recent_downloads")
    val recentDownloads: Long,
    @Json(name = "repository")
    val repository: String,
    @Json(name = "updated_at")
    val updatedAt: String,
    @Json(name = "versions")
    override val versions: String?
): Crate(
    name = name,
    versions = versions,
    description = description,
    id = id,
    maxVersion = maxVersion,
)