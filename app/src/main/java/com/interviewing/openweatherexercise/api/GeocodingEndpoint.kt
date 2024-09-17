package com.interviewing.openweatherexercise.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service for using OpenWeather's geocoding
 */
interface GeocodingEndpoint {

    // Lookup by query, in the form of {city name},{state name},{country code}
    @GET("direct")
    fun directQuery(@Query("q") query: String, @Query("limit") limit: Int = 5): Call<Array<GeocodingResponse>>

    // Lookup by zip code, in form of {zip code},{country code}
    // TODO if there's time.
//    @GET("zip")
//    fun zipcodeQuery(@Query("zip") zip: String)

    @GET("reverse")
    fun reverseQuery(@Query("lat") lat: Double, @Query("lon") lon: Double, limit: Int = 5): Call<Array<GeocodingResponse>>

    companion object {
        data class GeocodingResponse(
            val name: String,
            val localNames: Map<String, String>,
            val lat: Double,
            val lon: Double,
            val country: String,
            val state: String?
        )
    }

}