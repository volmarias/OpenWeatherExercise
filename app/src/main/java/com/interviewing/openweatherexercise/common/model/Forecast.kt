package com.interviewing.openweatherexercise.common.model

/**
 * Response content from the API. If I had more time, I would document this thoroughly.
 */
data class Forecast (
    val coord: Coord,
    val weather: List<Weather>,
    val main: Main,
    val visibility: Int,
    val wind: Wind,
    val clouds: Clouds,
    val rain: Rain?,
    val snow: Snow?,
    val dt: Long,
    val sys: Sys,
    val name: String // Deprecated, I know.
) {
    data class Coord(val lat: Double, val lon: Double)
    data class Weather(val id: String, val main: String, val description: String, val icon: String)
    data class Main(val temp: Double, val feelsLike: Double, val pressure: Double, val humidity: Double, val tempMin: Double, val tempMax: Double, val seaLevel: Double, val grndLevel: Double)
    data class Wind(val speed: Double, val deg: Int, val gust: Double)
    data class Clouds(val all: Double)
    data class Rain(val `1h`: Double? = null, val `3h`: Double? = null)
    data class Snow(val `1h`: Double? = null, val `3h`: Double? = null)
    data class Sys(val country: String? = null, val sunrise: Long, val sunset: Long)

}