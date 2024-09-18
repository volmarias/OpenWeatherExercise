package com.interviewing.openweatherexercise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.interviewing.openweatherexercise.common.model.Forecast
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

@Composable
fun ForecastDetails(forecast: Forecast, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        with(forecast) {

            LazyRow(Modifier
                .scrollable(rememberScrollableState { it }, orientation = Orientation.Horizontal, enabled = false)
                .background(Color.Cyan)
                .fillMaxWidth()
            ) {
                item {
                    Text(main.temp.roundToInt().toString(), style = MaterialTheme.typography.headlineLarge)
                }
                itemsIndexed(weather) { index, it ->
                    AsyncImage(
                        model = "https://openweathermap.org/img/wn/${it.icon}@${if (index == 0) "2x" else "1x"}.png",
                        contentDescription = it.description,
                        // TODO: Relevant placeholders, etc.
                    )
                }
            }
            Text("Min: ${forecast.main.tempMin.roundToInt()}°, Max: ${main.tempMax.roundToInt()}°")
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
        weather = listOf(
            Forecast.Weather("802", "Clouds", "scattered clouds", "03d"),
            Forecast.Weather("804", "Clouds", "overcast clouds", "04d")
        ),
        main = Forecast.Main(
            temp = 22.0,
            feelsLike = 23.0,
            pressure = 1020.0,
            humidity = 65.0,
            tempMin = 21.0,
            tempMax = 24.0,
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