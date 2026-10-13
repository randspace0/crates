package io.github.andraantariksa.crates.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {
    @Test
    fun compactCounts() {
        assertEquals("999", formatCompact(999))
        assertEquals("1K", formatCompact(1_000))
        assertEquals("87.6K", formatCompact(87_612))
        assertEquals("267M", formatCompact(267_832_081))
        assertEquals("2.7B", formatCompact(2_712_000_000))
        assertEquals("2B", formatCompact(2_001_000_000))
    }
}
