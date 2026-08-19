package io.github.andraantariksa.crates.feature_crates.data.source.remote.model

import com.google.common.truth.Truth.assertThat
import io.github.andraantariksa.crates.feature_crates.data.source.remote.CratesIoDataSourceRemoteImpl
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.detail.CrateDetail
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.summary.CratesSummary
import io.github.andraantariksa.crates.feature_crates.data.source.remote.service.CratesIoAPIService
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CratesIoDataSourceRemoteImplTest {
    private lateinit var cratesIoAPIService: CratesIoAPIService
    private lateinit var cratesIoDatasourceRemoteImpl: CratesIoDataSourceRemoteImpl

    @Before
    fun setUp() {
        cratesIoAPIService = mock()
        cratesIoDatasourceRemoteImpl =
            CratesIoDataSourceRemoteImpl(cratesIoAPIService = cratesIoAPIService)
    }

    @Test
    fun `getCratesSummary should forward to cratesIoAPIService getCratesSummary`() = runBlocking {
        whenever(cratesIoAPIService.getSummary()).thenReturn(CratesSummary.EXAMPLE)

        val result = cratesIoDatasourceRemoteImpl.getCratesSummary()

        verify(cratesIoAPIService, times(1)).getSummary()
        assertThat(result).isEqualTo(CratesSummary.EXAMPLE)
    }

    @Test
    fun `getCratesDetail should forward to cratesIoAPIService getCratesDetail`() = runBlocking {
        val crateDetail = CrateDetail.EXAMPLE
        val crateId = crateDetail.crate.id
        whenever(cratesIoAPIService.getCrateDetail(crateId)).thenReturn(crateDetail)

        val result = cratesIoDatasourceRemoteImpl.getCrateDetail(crateId)

        verify(cratesIoAPIService, times(1)).getCrateDetail(crateId)
        assertThat(result).isEqualTo(crateDetail)
    }
}