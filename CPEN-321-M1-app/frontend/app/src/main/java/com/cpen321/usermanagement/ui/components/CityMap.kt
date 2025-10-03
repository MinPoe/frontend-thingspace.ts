package com.cpen321.usermanagement.ui.components

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.cpen321.usermanagement.utils.CityExtractor
import com.cpen321.usermanagement.utils.GeocodingService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.CameraPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CityMap(
    bio: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cityName = CityExtractor.extractCityFromBio(bio)
    
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var cityLatLng by remember { mutableStateOf<LatLng?>(null) }
    
    LaunchedEffect(cityName) {
        try {
            // Reset states when city changes
            isLoading = true
            error = null
            
            // Get coordinates first in coroutine context
            val coordinates = GeocodingService.getCoordinatesForCity(cityName)
            cityLatLng = LatLng(coordinates.first, coordinates.second)
            isLoading = false
        } catch (e: Exception) {
            error = "Failed to get coordinates: ${e.message}"
            isLoading = false
        }
    }
    
    Card(
        modifier = modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if (isLoading) {
            Text(
                text = "Loading map...",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        } else if (error != null) {
            Text(
                text = "Error: $error",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else if (cityLatLng != null) {
            // Display city name and coordinates
            Text(
                text = "Showing map for: $cityName\nCoordinates: ${cityLatLng?.latitude}, ${cityLatLng?.longitude}",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
                // Display the actual map using AndroidView
                AndroidView(
                    factory = { context ->
                        MapView(context).apply {
                            onCreate(null)
                            onResume()
                            onStart()
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { mapView ->
                    mapView.getMapAsync { googleMap ->
                        try {
                            // Set up the map
                            googleMap.uiSettings.isZoomControlsEnabled = true
                            googleMap.uiSettings.isCompassEnabled = true
                            
                            // Use the coordinates we obtained
                            val cameraPosition = CameraPosition.Builder()
                                .target(cityLatLng!!)
                                .zoom(10f)
                                .build()
                            
                            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                            
                            // Add marker
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(cityLatLng!!)
                                    .title(cityName)
                            )
                            
                            // Move camera to city
                            googleMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(cityLatLng!!, 10f)
                            )
                        } catch (e: Exception) {
                            error = "Failed to load map: ${e.message}"
                        }
                    }
                }
            )
        } else {
            Text(
                text = "No location found for: $cityName",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}