package com.interviewing.openweatherexercise.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber


/**
 * Provides a logging interceptor for debugging
 */
@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    private fun apiKeyInterceptor() = Interceptor { chain ->
        val original = chain.request()
        chain.proceed(
            original.newBuilder().url(
                original.url.newBuilder()
                    .addQueryParameter("appid", "4577ed43ce33d6db3df4a9dc21a00a31").build()
            ).build()
        )
    }


    // TODO: In a real version, this would live in release / debug targets, rather than just here.
    @Provides
    fun okHttpClient() =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor { message -> Timber.i(message) }
                    .also { it.setLevel(HttpLoggingInterceptor.Level.BODY) })
            .addInterceptor(apiKeyInterceptor())
            .build()

}
