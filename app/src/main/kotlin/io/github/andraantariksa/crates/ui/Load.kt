package io.github.andraantariksa.crates.ui

import android.text.format.DateUtils
import androidx.compose.ui.platform.UriHandler
import kotlinx.coroutines.CancellationException
import java.text.NumberFormat
import java.time.Instant
import java.util.Locale

sealed interface Load<out T> {
    data object Loading : Load<Nothing>
    data class Ready<T>(val data: T) : Load<T>
    data class Failed(val error: Throwable) : Load<Nothing>
}

/** Like runCatching, but lets cancellation through so a cancelled load never shows as an error. */
suspend fun <T> load(block: suspend () -> T): Load<T> =
    try {
        Load.Ready(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Load.Failed(e)
    }

/** Opens http(s) links from crate metadata or READMEs; other schemes and missing browsers are ignored. */
fun UriHandler.openWeb(url: String) {
    if (!url.startsWith("https://") && !url.startsWith("http://")) return
    runCatching { openUri(url) }
}

fun formatCount(n: Long): String = NumberFormat.getIntegerInstance().format(n)

/** 2_700_000_000 -> "2.7B", like the crates.io download badges. */
fun formatCompact(n: Long): String {
    val units = listOf(1_000_000_000L to "B", 1_000_000L to "M", 1_000L to "K")
    val (size, suffix) = units.firstOrNull { n >= it.first } ?: return n.toString()
    val value = n.toDouble() / size
    return if (value >= 100) "${value.toLong()}$suffix"
    else String.format(Locale.US, "%.1f", value).removeSuffix(".0") + suffix
}

fun relativeTime(iso: String, now: Long = System.currentTimeMillis()): String =
    runCatching {
        DateUtils.getRelativeTimeSpanString(Instant.parse(iso).toEpochMilli(), now, DateUtils.MINUTE_IN_MILLIS)
            .toString()
    }.getOrDefault(iso)
