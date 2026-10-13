package io.github.andraantariksa.crates

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.squareup.moshi.Moshi
import io.github.andraantariksa.crates.data.CratesApi
import io.github.andraantariksa.crates.ui.crate.CrateViewModel
import io.github.andraantariksa.crates.ui.home.HomeViewModel
import io.github.andraantariksa.crates.ui.search.SearchViewModel
import io.github.andraantariksa.crates.ui.theme.ThemePreference
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.IOException

val appModule = module {
    single {
        OkHttpClient.Builder()
            .cache(Cache(File(androidContext().cacheDir, "http"), 50L * 1024 * 1024))
            // crates.io asks API clients to identify themselves.
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "crates-android/${BuildConfig.VERSION_NAME}")
                        .build()
                )
            }
            // Offline: fall back to whatever the HTTP cache still holds.
            .addInterceptor { chain ->
                try {
                    chain.proceed(chain.request())
                } catch (e: IOException) {
                    val cached = chain.proceed(
                        chain.request().newBuilder().cacheControl(CacheControl.FORCE_CACHE).build()
                    )
                    if (cached.code == 504) throw e.also { cached.close() }
                    cached
                }
            }
            .build()
    }
    single { Moshi.Builder().build() }
    single<CratesApi> {
        Retrofit.Builder()
            .baseUrl("https://crates.io/api/")
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
            .create(CratesApi::class.java)
    }
    single { ThemePreference(androidContext()) }

    viewModelOf(::HomeViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::CrateViewModel)
}

class CratesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val koin = startKoin {
            androidContext(this@CratesApplication)
            modules(appModule)
        }.koin
        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components { add(OkHttpNetworkFetcherFactory(callFactory = { koin.get<OkHttpClient>() })) }
                .build()
        }
    }
}
