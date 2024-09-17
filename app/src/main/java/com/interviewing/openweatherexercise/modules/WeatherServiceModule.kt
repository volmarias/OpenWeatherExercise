package com.interviewing.openweatherexercise.modules

import com.interviewing.openweatherexercise.api.WeatherServiceEndpoint
import com.interviewing.openweatherexercise.service.NetworkingWeatherService
import com.interviewing.openweatherexercise.service.WeatherService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
class WeatherServiceModule {

    @Provides
    fun endpoint(@Weather retrofit: Retrofit): WeatherServiceEndpoint = retrofit.create()

    @Provides
    fun weatherService(endpoint: WeatherServiceEndpoint): WeatherService =
        NetworkingWeatherService(endpoint)
}

