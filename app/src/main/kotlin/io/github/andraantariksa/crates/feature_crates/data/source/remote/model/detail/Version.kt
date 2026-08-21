package io.github.andraantariksa.crates.feature_crates.data.source.remote.model.detail


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Version(
    @Json(name = "audit_actions")
    val auditActions: List<AuditAction>,
    @Json(name = "crate")
    val crate: String,
    @Json(name = "crate_size")
    val crateSize: Int,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "dl_path")
    val dlPath: String,
    @Json(name = "downloads")
    val downloads: Long,
    @Json(name = "features")
    val features: Features,
    @Json(name = "id")
    val id: Int,
    @Json(name = "license")
    val license: String,
    @Json(name = "links")
    val links: LinksX,
    @Json(name = "num")
    val num: String,
    @Json(name = "published_by")
    val publishedBy: PublishedBy,
    @Json(name = "readme_path")
    val readmePath: String,
    @Json(name = "updated_at")
    val updatedAt: String,
    @Json(name = "yanked")
    val yanked: Boolean
)