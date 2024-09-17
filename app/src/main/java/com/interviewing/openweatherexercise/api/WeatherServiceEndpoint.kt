package com.interviewing.openweatherexercise.api

import com.interviewing.openweatherexercise.common.model.Forecast
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherServiceEndpoint {

    @GET("weather") // TODO: Add Interceptor to just apply appid to all
    fun weatherForLocation(@Query("lat") lat: Double, @Query("lon") lon: Double, @Query("appid") appid: String): Call<Forecast>

}