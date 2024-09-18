package com.interviewing.openweatherexercise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.interviewing.openweatherexercise.common.model.Forecast
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

@Composable
fun ForecastDetails(forecast: Forecast, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        with(forecast) {
            LazyRow {
                items(weather) {
                    Row {
                        // TODO: Icon, for now just use desc.
                        Text(it.description)
                    }
                }
            }
            Text("Now: ${main.temp.roundToInt()}°, Min: ${forecast.main.tempMin.roundToInt()}°, Max: ${main.tempMax.roundToInt()}°")
            rain?.run {
                Text("Rain ${precipString(`1h`, `3h`)}")
            }
            snow?.run {
                Text("Snow ${precipString(`1h`, `3h`)}")
            }

            Text(
                "Sunrise: ${epochToLocalTime(sys.sunrise)}, Sunset: ${epochToLocalTime(sys.sunset)}"
            )
        }
    }
}

fun precipString(`1h`: Double?, `3h`: Double?): String {
    val l = mutableListOf<String>()
    `1h`?.let { l.add("over 1H: $it") }
    `3h`?.let { l.add("over 3H: $it") }
    return l.joinToString(separator = ", then")
}

// Going to just assume no incorrect date issues for now.
// Also, don't want to futz with datetimeformatter for now either.
fun epochToLocalTime(epoch: Long) =
    Instant.ofEpochSecond(epoch).atZone(ZoneId.systemDefault()).toLocalTime().withNano(0)
        .withSecond(0)

@Preview
@Composable
fun WeatherPreview() {
    val forecast = Forecast(
        coord = Forecast.Coord(40.7225, -74.0422),
        weather = listOf(Forecast.Weather("802", "Clouds", "scattered clouds", "03d")),
        main = Forecast.Main(
            temp = 297.0,
            feelsLike = 298.0,
            pressure = 1020.0,
            humidity = 65.0,
            tempMin = 296.0,
            tempMax = 299.0,
            seaLevel = 1020.0,
            grndLevel = 1019.0
        ),
        1000,
        wind = Forecast.Wind(5.0, 50, 0.0),
        clouds = Forecast.Clouds(40.0),
        rain = Forecast.Rain(`1h` = 1.0),
        snow = Forecast.Snow(`3h` = 3.0),
        dt = 1726607544,
        sys = Forecast.Sys(sunrise = 1726569556, sunset = 1726614114),
        name = "Jay Cee"
    )
    ForecastDetails(forecast, modifier = Modifier.background(Color.White))
}