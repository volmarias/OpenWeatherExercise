package com.interviewing.openweatherexercise.service

import android.location.Location

interface GeocodingService {

    /**
     * Retrieves a Location for a given name query
     */
    suspend fun fetchCoordsForName(queryName: String, limit: Int = 5): Location

    // If there's time
    // suspend fun fetchCoordsForZip(queryName: String, limit: Int = 5): Location

    /**
     * Retrieves a Name for a given lat/lon pair
     */
    suspend fun fetchNameForCoords(lat: Double, lon: Double, limit: Int = 5): String
}