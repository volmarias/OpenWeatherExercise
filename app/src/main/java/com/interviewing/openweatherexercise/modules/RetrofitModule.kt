package com.interviewing.openweatherexercise.modules

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
class RetrofitModule {

    @Provides
    @Weather
    fun getWeather(gson: Gson, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Geocoding
    fun getGeocodingRetrofit(gson: Gson, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://api.openweathermap.org/geo/1.0/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}

@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Qualifier
annotation class Geocoding

@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Qualifier
annotation class Weather

