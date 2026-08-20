package io.github.andraantariksa.crates.feature_crates.data.source.remote.model

import com.google.common.truth.Truth.assertThat
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.summary.JustUpdatedCrate
import io.github.andraantariksa.crates.feature_crates.domain.entity.crate.Crate
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.summary.MostDownloaded
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.summary.MostRecentlyDownloaded
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.summary.NewCrate
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ModelTest {
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun everyCrateLikeModel_isInheritFromCrate() {
        assertThat(Crate::class.java.isAssignableFrom(JustUpdatedCrate::class.java)).isTrue()
        assertThat(Crate::class.java.isAssignableFrom(MostDownloaded::class.java)).isTrue()
        assertThat(Crate::class.java.isAssignableFrom(MostRecentlyDownloaded::class.java)).isTrue()
        assertThat(Crate::class.java.isAssignableFrom(NewCrate::class.java)).isTrue()
    }
}