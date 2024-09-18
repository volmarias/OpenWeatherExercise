@file:OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)

package com.interviewing.openweatherexercise

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
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
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.gson.Gson
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

    private lateinit var locationProvider: FusedLocationProviderClient
    private lateinit var locationPermissionRequest: ActivityResultLauncher<String>

    private val mainViewModel: MainWeatherViewModel by viewModels()
    private val searchViewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        locationProvider = LocationServices.getFusedLocationProviderClient(this)
        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                requestUserLocation()
            } else {
                // Currently, do nothing. The user remains on the search bar.
                // In a real version, this would probably remove the location option at some point.
            }
        }

        // This is a very hacky way, I'd use an actual data store if I had time. Oh well.
        val defaultSearchTarget = getSharedPreferences("", 0).getString("default_search_result", null)
        defaultSearchTarget?.let {
            val location = Gson().fromJson(it, GeocodedLocation::class.java)
            mainViewModel.fetchForecast(location)
            searchViewModel.searchText.value = location.displayString()
        } ?: {
            searchViewModel.expanded.value = true
        }


        setContent {
            OpenWeatherExerciseTheme {
                Scaffold(
                    topBar = {
                        WeatherSearchBar(
                            textProvider = { searchViewModel.searchText },
                            expandedProvider = { searchViewModel.expanded },
                            forecastViaString = { mainViewModel.fetchForecast(it) },
                            forecastViaResult = { mainViewModel.fetchForecast(it) },
                            searchString = { searchViewModel.quickSearches.tryEmit(it) },
                            searchResultState = searchViewModel.searchResponses,
                            requestCurrentLocation = {
                                requestUserLocation()
                            }
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
                                LoadingState.LOADED ->
                                    mainViewModel.areaForecast.value?.forecast?.let {
                                        ForecastDetails(it)
                                    }

                                LoadingState.ERROR -> Text("Unable to fetch a forecast for this area, please try again later.")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val areaforecast = mainViewModel.areaForecast.value
        // This is a very hacky way, I'd use an actual data store if I had time. Oh well.
        if (mainViewModel.state == LoadingState.LOADED && areaforecast != null) {
            getSharedPreferences("", 0)
                .edit()
                .putString("default_search_result", Gson().toJson(areaforecast.location))
                .apply()
        }
    }

    private fun requestUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
            == PERMISSION_GRANTED
        ) {
            fetchLocationTask(
                successCallback = {
                    mainViewModel.fetchForecast(it)
                    searchViewModel.expanded.value = false
                                  },
                failureCallback = { mainViewModel.fetchForecastFailure(it) }
            )
        } else {
            locationPermissionRequest.launch(ACCESS_COARSE_LOCATION)
        }
    }

    @RequiresPermission(ACCESS_COARSE_LOCATION)
    private fun fetchLocationTask(
        successCallback: (Location) -> Unit,
        failureCallback: (Exception) -> Unit
    ) {
        // Make the compiler happy.
        locationProvider.getCurrentLocation(
            CurrentLocationRequest.Builder()
                .setPriority(PRIORITY_HIGH_ACCURACY)
                .setGranularity(Granularity.GRANULARITY_COARSE)
                .setDurationMillis(5000)
                .setMaxUpdateAgeMillis(60 * 1000)
                .build(),
            null
        ).addOnSuccessListener(this) {
            successCallback(it)
        }.addOnFailureListener(this) {
            failureCallback(it)
        }
    }
}

@HiltViewModel
class SearchViewModel @Inject constructor(private val geocodingService: GeocodingService) :
    ViewModel() {
    private var _searchResponses: MutableState<List<GeocodedLocation>> = mutableStateOf(listOf())
    val searchResponses: State<List<GeocodedLocation>> = _searchResponses

    val searchText = mutableStateOf("")
    val expanded = mutableStateOf(false)

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
        ERROR,
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

    fun fetchForecast(location: Location) {
        fetchForecast {
            geocodingService.fetchNameForCoords(
                location.latitude,
                location.longitude
            )[0]
        }
    }

    fun fetchForecastFailure(e: Exception) {
        Timber.e(e, "Unable to fetch weather")
        state = LoadingState.ERROR
    }

    private fun fetchForecast(areaProvider: suspend () -> GeocodedLocation) {
        viewModelScope.launch {
            try {
                state = LoadingState.LOADING
                withContext(Dispatchers.IO) {
                    val area = areaProvider()
                    val forecast = _weatherService.weatherForLatLon(area.lat, area.lon)
                    withContext(Dispatchers.Main) {
                        _areaForecast.value = AreaForecast(area, forecast)
                    }
                }
                state = areaForecast.value?.run { LoadingState.LOADED } ?: LoadingState.EMPTY
            } catch (e: Exception) {
                fetchForecastFailure(e)
            }
        }
    }
}

data class AreaForecast(val location: GeocodedLocation, val forecast: Forecast)