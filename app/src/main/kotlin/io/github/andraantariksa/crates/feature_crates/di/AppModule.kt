package io.github.andraantariksa.crates.feature_crates.di

import androidx.room.Room
import io.github.andraantariksa.crates.feature_crates.data.repository.CookieRepositoryImpl
import io.github.andraantariksa.crates.feature_crates.data.repository.CratesIoRepositoryImpl
import io.github.andraantariksa.crates.feature_crates.data.repository.UserRepositoryImpl
import io.github.andraantariksa.crates.feature_crates.data.source.local.CratesIoDataSourceLocal
import io.github.andraantariksa.crates.feature_crates.data.source.local.CratesIoDataSourceLocalImpl
import io.github.andraantariksa.crates.feature_crates.data.source.local.UserDataSourceLocal
import io.github.andraantariksa.crates.feature_crates.data.source.local.UserDataSourceLocalImpl
import io.github.andraantariksa.crates.feature_crates.data.source.local.database.CratesDatabase
import io.github.andraantariksa.crates.feature_crates.data.source.remote.CratesIoDataSourceRemote
import io.github.andraantariksa.crates.feature_crates.data.source.remote.CratesIoDataSourceRemoteImpl
import io.github.andraantariksa.crates.feature_crates.data.source.remote.service.ConnectivityInterceptor
import io.github.andraantariksa.crates.feature_crates.data.source.remote.service.CratesIoAPIService
import io.github.andraantariksa.crates.feature_crates.domain.repository.CookieRepository
import io.github.andraantariksa.crates.feature_crates.domain.repository.CratesIoRepository
import io.github.andraantariksa.crates.feature_crates.domain.repository.UserRepository
import io.github.andraantariksa.crates.feature_crates.ui.crate.CrateViewModel
import io.github.andraantariksa.crates.feature_crates.ui.main.UserViewModel
import io.github.andraantariksa.crates.feature_crates.ui.main.screens.crates_summary.CratesSummaryViewModel
import io.github.andraantariksa.crates.feature_crates.ui.sign_in.screen.SignInViewModel
import io.github.andraantariksa.crates.feature_crates.util.PersistedCookieJar
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


val appModule = module {
    single {
        Room
            .databaseBuilder(get(), CratesDatabase::class.java, CratesDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    single<CookieRepository> {
        CookieRepositoryImpl(get())
    }

    single<CratesIoRepository> {
        CratesIoRepositoryImpl(
            cratesIoDatasourceLocal = get(),
            cratesIoDatasourceRemote = get()
        )
    }

    single<UserDataSourceLocal> {
        UserDataSourceLocalImpl(cratesDatabase = get())
    }

    single<CratesIoDataSourceLocal> {
        CratesIoDataSourceLocalImpl(cratesDatabase = get())
    }

    single {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(ConnectivityInterceptor(get()))
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "crates (https://github.com/randspace0/crates)")
                        .build()
                )
            }
            .cookieJar(PersistedCookieJar(cookieRepository = get()))
            .build()

        Retrofit.Builder().client(okHttpClient).baseUrl("https://crates.io/api/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create()).build()
            .create(CratesIoAPIService::class.java)
    }

    single<CratesIoDataSourceRemote> {
        CratesIoDataSourceRemoteImpl(get())
    }

    single<UserRepository> {
        UserRepositoryImpl(
            cratesIoDataSourceRemote = get(),
            userDataSourceLocal = get()
        )
    }

    viewModel { CrateViewModel(get()) }
    viewModel { SignInViewModel(get(), get()) }
    viewModel { CratesSummaryViewModel(get()) }
    viewModel { UserViewModel(get()) }
}
