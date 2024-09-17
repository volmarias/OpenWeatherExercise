package com.interviewing.openweatherexercise.service

import android.location.Location
import com.interviewing.openweatherexercise.api.GeocodingEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import timber.log.Timber

class NetworkingGeocodingService(private val geocodingEndpoint: GeocodingEndpoint) : GeocodingService {
    // TODO: I should probably return something more useful, like a list of possible responses.
    override suspend fun fetchCoordsForName(query: String, limit: Int): Location {
        return withContext(Dispatchers.IO) {
            Timber.i("Requesting coordinate for $query")
            with (geocodingEndpoint.directQuery(query, limit = limit).execute()) {
                if (isSuccessful && body()?.isNotEmpty() == true) {
                    body()!![0].let {
                        // TODO: Return multiple results list
                        Location("Geocoding").apply {
                            latitude = it.lat
                            longitude = it.lon
                        }
                    }
                } else {
                    throw IOException("Could not geolocate $query")
                }
            }
        }
    }

    override suspend fun fetchNameForCoords(lat: Double, lon: Double, limit: Int): String {
        return withContext(Dispatchers.IO) {
            Timber.i("Requesting name for $lat, $lon")
            with (geocodingEndpoint.reverseQuery(lat = lat, lon = lon, limit = limit).execute()) {
                if (isSuccessful && body()?.isNotEmpty() == true) {
                    body()!![0].run { "${name},${state},${country}" }
                } else {
                    throw IOException("Could not find location for $lat, $lon")
                }
            }
        }
    }
}