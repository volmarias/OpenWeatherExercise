@file:OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)

package com.interviewing.openweatherexercise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.interviewing.openweatherexercise.MainWeatherViewModel.LoadingState
import com.interviewing.openweatherexercise.common.model.Forecast
import com.interviewing.openweatherexercise.service.GeocodingService
import com.interviewing.openweatherexercise.service.GeocodingService.GeocodedLocation
import com.interviewing.openweatherexercise.service.WeatherService
import com.interviewing.openweatherexercise.ui.ForecastDetails
import com.interviewing.openweatherexercise.ui.WeatherSearchBar
import com.interviewing.openweatherexercise.ui.theme.OpenWeatherExerciseTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
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
                val mainViewModel: MainWeatherViewModel = hiltViewModel()

                Scaffold(
                    topBar = {
                        val searchViewModel: SearchViewModel = hiltViewModel()
                        WeatherSearchBar(
                            forecastViaString = { mainViewModel.fetchForecast(it) },
                            forecastViaLocation = { mainViewModel.fetchForecast(it) },
                            searchString = { searchViewModel.quickSearches.tryEmit(it) },
                            searchResultState = searchViewModel.searchResponses
                        )
                    }
                ) { innerPadding ->

                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        Column {
                            when (mainViewModel.state) {
                                LoadingState.START -> {}
                                LoadingState.LOADING -> Text("Please wait, loading...")
                                LoadingState.EMPTY -> Text("Could not find a forecast for this area? This should not happen.")
                                LoadingState.LOADED -> mainViewModel.areaForecast.value?.forecast?.let { ForecastDetails(it) }
                                LoadingState.ERROR -> Text("Unable to fetch a forecast for this area, please try again later.")
                            }
                        }
                    }
                }
            }
        }
    }
}

@HiltViewModel
class SearchViewModel @Inject constructor(private val geocodingService: GeocodingService) :
    ViewModel() {
    private var _searchResponses: MutableState<List<GeocodedLocation>> = mutableStateOf(listOf())
    val searchResponses: State<List<GeocodedLocation>> = _searchResponses

    private var quickSearchJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    val quickSearches: MutableStateFlow<String> = MutableStateFlow("")

    init {
        quickSearchJob = viewModelScope.launch {
            quickSearches
                .debounce(500)
                .buffer(onBufferOverflow = BufferOverflow.DROP_OLDEST)
                .distinctUntilChanged()
                .collect {
                    if (it.isEmpty()) {
                        withContext(Dispatchers.Main) { _searchResponses.value = listOf() }
                    } else {
                        try {
                            val results = geocodingService.fetchCoordsForName(it)
                            withContext(Dispatchers.Main) {
                                _searchResponses.value = results
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "unable to perform quick search")
                        }
                    }
                }
        }
    }
}

@HiltViewModel
class MainWeatherViewModel @Inject constructor(
    private val _weatherService: WeatherService,
    private val geocodingService: GeocodingService
) : ViewModel() {

    // TODO: Sealed class
    enum class LoadingState {
        START,
        LOADING,
        EMPTY,
        LOADED,
        ERROR
    }

    private var _areaForecast: MutableState<AreaForecast?> = mutableStateOf(null)
    val areaForecast: State<AreaForecast?> = _areaForecast

    var state by mutableStateOf(LoadingState.START)

    // TODO: For a real version, we would replace the user-entered string with the result in the
    // search bar as well.
    fun fetchForecast(name: String) {
        if (name.isNotBlank()) {
            fetchForecast { geocodingService.fetchCoordsForName(name)[0] }
        }
    }

    fun fetchForecast(area: GeocodedLocation) {
        fetchForecast { area }
    }

    private fun fetchForecast(areaProvider: suspend () -> GeocodedLocation) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Main) { state = LoadingState.LOADING }
                with(areaProvider()) {
                    val forecast = _weatherService.weatherForLatLon(lat, lon)
                    _areaForecast.value = AreaForecast(this, forecast)
                }
                withContext(Dispatchers.Main) {
                    state = areaForecast.value?.run { LoadingState.LOADED } ?: LoadingState.EMPTY
                }
            } catch (e: Exception) {
                Timber.e(e, "Unable to fetch weather")
                state = LoadingState.ERROR
            }
        }
    }
}

data class AreaForecast(val location: GeocodedLocation, val forecast: Forecast)