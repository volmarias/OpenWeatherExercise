@file:OptIn(ExperimentalMaterial3Api::class)

package com.interviewing.openweatherexercise.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.interviewing.openweatherexercise.service.GeocodingService.GeocodedLocation

@Composable
fun WeatherSearchBar(
    forecastViaString: (String) -> Unit,
    forecastViaLocation: (GeocodedLocation) -> Unit,
    searchString: (String) -> Unit,
    searchResultState: State<List<GeocodedLocation>>,
    modifier: Modifier = Modifier
) {
    var topbarText by rememberSaveable { mutableStateOf("") }
    var topbarExpanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier.semantics { isTraversalGroup = true }) {
        androidx.compose.material3.SearchBar(
            modifier = Modifier.fillMaxWidth(),
            inputField = {
                SearchBarDefaults.InputField(
                    query = topbarText,
                    onQueryChange = {
                        topbarText = it
                        searchString(it)
                    },
                    onSearch = {
                        topbarExpanded = false
                        forecastViaString(it)
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
                items(searchResultState.value) {
                    val fullNameText = listOfNotNull(
                        it.name,
                        it.state,
                        it.country
                    ).joinToString()

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            topbarExpanded = false
                            topbarText = fullNameText
                            forecastViaLocation(it)
                        }) {
                        Text(fullNameText)
                    }
                }
            }
        }
    }
}
