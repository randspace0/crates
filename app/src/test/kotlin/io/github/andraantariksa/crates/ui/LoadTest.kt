package io.github.andraantariksa.crates.ui

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class LoadTest {
    @Test
    fun failuresBecomeFailed() = runBlocking {
        assertTrue(load { throw IOException("offline") } is Load.Failed)
        assertEquals(Load.Ready(1), load { 1 })
    }

    @Test(expected = CancellationException::class)
    fun cancellationIsNotAnError(): Unit = runBlocking {
        load { throw CancellationException() }
    }
}
