package com.interviewing.openweatherexercise.modules

import com.interviewing.openweatherexercise.api.GeocodingEndpoint
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

}

