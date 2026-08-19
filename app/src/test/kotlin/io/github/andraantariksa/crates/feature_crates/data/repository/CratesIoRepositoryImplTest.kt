package io.github.andraantariksa.crates.feature_crates.data.repository

import com.google.common.truth.Truth.assertThat
import io.github.andraantariksa.crates.feature_crates.data.exception.NoCachedDataException
import io.github.andraantariksa.crates.feature_crates.data.exception.NoNetworkException
import io.github.andraantariksa.crates.feature_crates.data.source.local.CratesIoDataSourceLocal
import io.github.andraantariksa.crates.feature_crates.data.source.remote.CratesIoDataSourceRemote
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.summary.CratesSummary
import kotlinx.coroutines.runBlocking
import okio.IOException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.times
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CratesIoRepositoryImplTest {
    private lateinit var cratesIoDatasourceRemote: CratesIoDataSourceRemote
    private lateinit var cratesIoDatasourceLocal: CratesIoDataSourceLocal
    private lateinit var cratesIoRepositoryImpl: CratesIoRepositoryImpl

    @Before
    fun setUp() {
        cratesIoDatasourceRemote = mock()
        cratesIoDatasourceLocal = mock()
        cratesIoRepositoryImpl =
            CratesIoRepositoryImpl(
                cratesIoDatasourceRemote = cratesIoDatasourceRemote,
                cratesIoDatasourceLocal = cratesIoDatasourceLocal
            )
    }

    @Test
    fun `getCratesSummary should forward it to remote data source when online`() = runBlocking {
        whenever(cratesIoDatasourceRemote.getCratesSummary()).thenReturn(CratesSummary.EXAMPLE)

        val result = cratesIoRepositoryImpl.getCratesSummary()

        verify(cratesIoDatasourceRemote, times(1)).getCratesSummary()
        assertThat(result).isEqualTo(Result.success(CratesSummary.EXAMPLE))
    }

    @Test
    fun `getCratesSummary should return NoNetworkException when there is no network or cached data`() =
        runBlocking {
            whenever(cratesIoDatasourceRemote.getCratesSummary()).thenThrow(NoNetworkException())
            whenever(cratesIoDatasourceLocal.getCratesSummary()).thenThrow(NoCachedDataException())

            val result = cratesIoRepositoryImpl.getCratesSummary()

            verify(cratesIoDatasourceRemote, times(1)).getCratesSummary()
            verify(cratesIoDatasourceLocal, times(1)).getCratesSummary()
            assertThat(result).isEqualTo(Result.failure<CratesSummary>(NoNetworkException()))
        }

    @Test
    fun `getCratesSummary should return SocketTimeoutException when network failed or cached data`() =
        runBlocking {
            whenever(cratesIoDatasourceRemote.getCratesSummary()).thenAnswer { throw IOException() }
            whenever(cratesIoDatasourceLocal.getCratesSummary()).thenThrow(NoCachedDataException())

            val result = cratesIoRepositoryImpl.getCratesSummary()

            verify(cratesIoDatasourceRemote, times(1)).getCratesSummary()
            verify(cratesIoDatasourceLocal, times(1)).getCratesSummary()
            assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
        }
}