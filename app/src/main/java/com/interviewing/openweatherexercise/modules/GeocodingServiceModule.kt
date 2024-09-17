package com.interviewing.openweatherexercise.modules

import com.interviewing.openweatherexercise.api.GeocodingEndpoint
import com.interviewing.openweatherexercise.service.GeocodingService
import com.interviewing.openweatherexercise.service.NetworkingGeocodingService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
class GeocodingServiceModule {

    @Provides
    fun endpoint(@Geocoding retrofit: Retrofit): GeocodingEndpoint = retrofit.create()

    @Provides
    fun geocodingService(endpoint: GeocodingEndpoint): GeocodingService = NetworkingGeocodingService(endpoint)
}

