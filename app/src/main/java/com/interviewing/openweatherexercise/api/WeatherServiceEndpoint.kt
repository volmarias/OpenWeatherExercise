package com.interviewing.openweatherexercise.api

import com.interviewing.openweatherexercise.common.model.Forecast
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherServiceEndpoint {

    @GET("weather")
    fun weatherForLocation(@Query("lat") lat: Double, @Query("lon") lon: Double, @Query("units") units: String): Call<Forecast>

}