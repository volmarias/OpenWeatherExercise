package com.interviewing.openweatherexercise.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber


/**
 * Provides a logging interceptor for debugging
 * TODO: Separate Release and Debug
 */
@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun loggingOkHttpClient() =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor { message -> Timber.i(message) }
                    .also {
                        it.setLevel(
                            HttpLoggingInterceptor.Level.BODY
                        )
                    })
            .build()

}