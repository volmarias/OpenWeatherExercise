package com.interviewing.openweatherexercise.service

import com.interviewing.openweatherexercise.api.WeatherServiceEndpoint
import com.interviewing.openweatherexercise.common.model.Forecast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import timber.log.Timber

/**
 * Provides network-based weather service requests.
 */
class NetworkingWeatherService(private val weatherServiceEndpoint: WeatherServiceEndpoint) :
    WeatherService {


    override suspend fun weatherForLatLon(lat: Double, lon: Double): Forecast? {
        return withContext(Dispatchers.IO) {
            Timber.i("Requesting weather for $lat, $lon")
            with(weatherServiceEndpoint.weatherForLocation(lat, lon, "<snipped>").execute()) {
                if (isSuccessful) {
                    body()
                } else {
                    throw IOException("Failed to fetch forecast for $lat, $lon")
                }
            }
        }
    }
}