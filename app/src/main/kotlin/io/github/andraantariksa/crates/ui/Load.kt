package io.github.andraantariksa.crates.ui

import android.text.format.DateUtils
import java.text.NumberFormat
import java.time.Instant
import java.util.Locale

sealed interface Load<out T> {
    data object Loading : Load<Nothing>
    data class Ready<T>(val data: T) : Load<T>
    data class Failed(val error: Throwable) : Load<Nothing>
}

suspend fun <T> load(block: suspend () -> T): Load<T> =
    runCatching { block() }.fold({ Load.Ready(it) }, { Load.Failed(it) })

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
