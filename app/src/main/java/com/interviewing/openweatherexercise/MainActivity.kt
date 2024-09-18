@file:OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)

package com.interviewing.openweatherexercise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.interviewing.openweatherexercise.common.model.Forecast
import com.interviewing.openweatherexercise.service.GeocodingService
import com.interviewing.openweatherexercise.service.GeocodingService.GeocodedLocation
import com.interviewing.openweatherexercise.service.WeatherService
import com.interviewing.openweatherexercise.ui.ForecastDetails
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
                        var topbarText by rememberSaveable { mutableStateOf("") }
                        var topbarExpanded by rememberSaveable { mutableStateOf(false) }

                        Box(Modifier.semantics { isTraversalGroup = true }) {
                            val searchViewModel: SearchViewModel = hiltViewModel()
                            LaunchedEffect(key1 = Unit) {
                                searchViewModel.initQuickSearch()
                            }
                            SearchBar(
                                modifier = Modifier.fillMaxWidth(),
                                inputField = {
                                    SearchBarDefaults.InputField(
                                        query = topbarText,
                                        onQueryChange = {
                                            topbarText = it
                                            searchViewModel.quickSearches.tryEmit(it)
                                        },
                                        onSearch = {
                                            topbarExpanded = false
                                            mainViewModel.fetchForecast(it)
                                        },
                                        expanded = topbarExpanded,
                                        onExpandedChange = { topbarExpanded = it },
                                        placeholder = { Text("City name, State") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Search,
                                                contentDescription = null
                                            )
                                        },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.MoreVert,
                                                contentDescription = null
                                            )
                                        },
                                    )
                                },
                                expanded = topbarExpanded,
                                onExpandedChange = {
                                    // TODO
                                }) {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(searchViewModel.searchResponses.value) {
                                        val fullNameText = listOfNotNull(
                                            it.name,
                                            it.state,
                                            it.country
                                        ).joinToString()

                                        Row(modifier = Modifier.fillMaxWidth().clickable {
                                            topbarExpanded = false
                                            topbarText = fullNameText
                                            mainViewModel.fetchForecast(it.lat, it.lon)
                                        }) {
                                            Text(fullNameText)
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->

                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        Column {
                            Text("Current state: ${mainViewModel.state}")
                            if (mainViewModel.state == MainWeatherViewModel.LoadingState.LOADED) {
                                ForecastDetails(mainViewModel.forecast.value!!)
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

    fun initQuickSearch() {
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
        LOADING,
        EMPTY,
        LOADED,
        ERROR
    }

    // TODO: Ought to be a tuple of forcast and geoinfo
    private var _forecast: MutableState<Forecast?> = mutableStateOf(null)
    val forecast: State<Forecast?> = _forecast

    var state by mutableStateOf(LoadingState.LOADING)

    fun fetchForecast(name: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Main) { state = LoadingState.LOADING }
                with(geocodingService.fetchCoordsForName(name)[0]) {
                    _forecast.value = _weatherService.weatherForLatLon(lat, lon)
                }
                withContext(Dispatchers.Main) {
                    state = forecast.value?.run { LoadingState.LOADED } ?: LoadingState.EMPTY
                }
            } catch (e: Exception) {
                Timber.e(e, "Unable to fetch weather")
                state = LoadingState.ERROR
            }
        }
    }

    fun fetchForecast(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Main) { state = LoadingState.LOADING }
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