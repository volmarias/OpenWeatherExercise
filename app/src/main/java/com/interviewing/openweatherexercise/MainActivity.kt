package com.interviewing.openweatherexercise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.interviewing.openweatherexercise.common.model.Forecast
import com.interviewing.openweatherexercise.service.WeatherService
import com.interviewing.openweatherexercise.ui.theme.OpenWeatherExerciseTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenWeatherExerciseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // TODO: Add search bar to scaffold
                    Box(modifier = Modifier.padding(innerPadding)) {
                        val viewModel: MainWeatherViewModel = hiltViewModel()
                        LaunchedEffect(key1 = Unit) {
                            viewModel.fetchForecast(40.72250427983209, -74.0422344687394)
                        }
                        Column {
                            Text("Current state: ${viewModel.state}")
                            if (viewModel.state == MainWeatherViewModel.LoadingState.LOADED) {
                                val forecast = viewModel.forecast.value
                                // TODO: Remove nullity check
                                if (forecast != null) {
                                    Text(forecast.toString())
                                }
                            }
                        }
                    }
                }
            }
        }




    }
}

@HiltViewModel
class MainWeatherViewModel @Inject constructor(private val _weatherService: WeatherService) : ViewModel() {

    // TODO: Sealed class
    enum class LoadingState {
        LOADING,
        EMPTY,
        LOADED,
        ERROR
    }

    private var _forecast: MutableState<Forecast?> = mutableStateOf(null)
    val forecast: State<Forecast?> = _forecast

    var state by mutableStateOf(LoadingState.LOADING)

    fun fetchForecast(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                _forecast.value = _weatherService.weatherForLatLon(lat, lon)
                withContext(Dispatchers.Main) {
                    state = forecast.value?.run { LoadingState.LOADED } ?: LoadingState.EMPTY
                }
            } catch (e: Exception) {
                Timber.e(e, "Unable to fetch weather")
                state = LoadingState.ERROR
            }
        }
    }
}