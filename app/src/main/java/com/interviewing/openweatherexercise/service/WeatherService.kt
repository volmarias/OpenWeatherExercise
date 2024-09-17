package com.interviewing.openweatherexercise.service

import com.interviewing.openweatherexercise.common.model.Forecast

interface WeatherService {

    // Potentially added in future
    //    suspend fun weatherForName(name: String): Forecast

    suspend fun weatherForLatLon(lat: Double, lon: Double): Forecast?
}