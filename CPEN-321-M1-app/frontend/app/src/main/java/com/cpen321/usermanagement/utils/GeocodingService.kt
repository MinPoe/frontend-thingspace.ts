package com.cpen321.usermanagement.utils

import android.util.Log
import com.cpen321.usermanagement.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import org.json.JSONObject

object GeocodingService {
    private const val TAG = "GeocodingService"
    
    // API key - replace with your actual key
    private val API_KEY = "AIzaSyDQ6b01Z3Dd2L9Lk1PRKQe2ApWcWLUQuIs"
    
    suspend fun getCoordinatesForCity(cityName: String): Pair<Double, Double> {
        return withContext(Dispatchers.IO) {
            try {
                // First try to use hardcoded coordinates from CityExtractor
                val hardcodedCoords = CityExtractor.getCityCoordinates(cityName)
                if (hardcodedCoords != Pair(49.2827, -123.1207) || cityName.contains("Vancouver", ignoreCase = true)) {
                    Log.d(TAG, "Using hardcoded coordinates for $cityName: $hardcodedCoords")
                    return@withContext hardcodedCoords
                }
                
                // Fallback to Google Geocoding API
                val encodedCity = java.net.URLEncoder.encode(cityName, "UTF-8")
                val url = "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedCity&key=$API_KEY"
                
                val response = URL(url).readText()
                val json = JSONObject(response)
                
                if (json.getString("status") == "OK") {
                    val results = json.getJSONArray("results")
                    if (results.length() > 0) {
                        val location = results.getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                        
                        val lat = location.getDouble("lat")
                        val lng = location.getDouble("lng")
                        
                        Log.d(TAG, "Found coordinates for $cityName: ($lat, $lng)")
                        return@withContext Pair(lat, lng)
                    }
                }
                
                Log.w(TAG, "No coordinates found for $cityName, using default")
                Pair(49.2827, -123.1207) // Default to Vancouver
                
            } catch (e: Exception) {
                Log.e(TAG, "Error geocoding $cityName", e)
                // Fallback to hardcoded coordinates
                return@withContext CityExtractor.getCityCoordinates(cityName)
            }
        }
    }
}
