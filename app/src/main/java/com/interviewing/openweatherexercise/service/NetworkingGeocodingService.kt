package com.interviewing.openweatherexercise.service

import android.util.LruCache
import com.interviewing.openweatherexercise.api.GeocodingEndpoint
import com.interviewing.openweatherexercise.service.GeocodingService.GeocodedLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import timber.log.Timber

class NetworkingGeocodingService(private val geocodingEndpoint: GeocodingEndpoint) :
    GeocodingService {

    // TODO: In real code, this would be disk access based.
    private val nameLruCache: LruCache<String, List<GeocodedLocation>> = LruCache(32)
    private val coordsLruCache: LruCache<Pair<Double, Double>, List<GeocodedLocation>> =
        LruCache(32)

    override suspend fun fetchCoordsForName(query: String, limit: Int): List<GeocodedLocation> {
        return withContext(Dispatchers.IO) {
            Timber.i("Requesting coordinate for $query")
            nameLruCache.get(query) ?: with(
                geocodingEndpoint.directQuery(query, limit = limit).execute()
            ) {
                if (isSuccessful && body() != null) {
                    body()!!.map { it.toGeocodedLocation() }.also { nameLruCache.put(query, it) }
                } else {
                    throw IOException("Could not geolocate $query")
                }
            }
        }
    }

    override suspend fun fetchNameForCoords(
        lat: Double,
        lon: Double,
        limit: Int
    ): List<GeocodedLocation> {
        return withContext(Dispatchers.IO) {
            Timber.i("Requesting name for $lat, $lon")
            coordsLruCache.get(Pair(lat, lon)) ?: with(
                geocodingEndpoint.reverseQuery(
                    lat = lat,
                    lon = lon,
                    limit = limit
                ).execute()
            ) {
                if (isSuccessful && body()?.isNotEmpty() == true) {
                    body()!!.map { it.toGeocodedLocation() }
                        .also { coordsLruCache.put(Pair(lat, lon), it) }
                } else {
                    throw IOException("Could not find location for $lat, $lon")
                }
            }
        }
    }
}

fun GeocodingEndpoint.Companion.GeocodingResponse.toGeocodedLocation() =
    GeocodedLocation(
        lat = lat,
        lon = lon,
        name = name,
        country = country,
        state = state
    )