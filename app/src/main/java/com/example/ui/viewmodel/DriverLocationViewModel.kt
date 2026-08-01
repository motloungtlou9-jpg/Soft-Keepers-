package com.example.ui.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirestoreLocationRepository
import com.example.service.LocationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel that tracks the driver's real-time position using LocationService and streams updates
 * to UI state, while syncing location updates to Firestore.
 */
class DriverLocationViewModel(application: Application) : AndroidViewModel(application) {

    private val locationService = LocationService(application)

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private var trackingJob: Job? = null
    private var driverId: String = "driver_default"
    private var driverName: String = "Driver"
    private var rideId: String? = null

    fun setDriverInfo(id: String, name: String, currentRideId: String? = null) {
        driverId = id
        driverName = name
        rideId = currentRideId
    }

    fun startTracking() {
        if (_isTracking.value) return
        _isTracking.value = true

        trackingJob = viewModelScope.launch {
            locationService.requestLocationUpdates()
                .catch { e ->
                    // Handle error if needed
                }
                .collect { location ->
                    _currentLocation.value = location
                    // Stream/sync real-time location to repository / Firestore
                    FirestoreLocationRepository.updateDriverLocation(
                        driverId = driverId,
                        driverName = driverName,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        rideId = rideId,
                        isOnline = true,
                        heading = location.bearing,
                        speed = location.speed
                    )
                }
        }
    }

    fun stopTracking() {
        _isTracking.value = false
        trackingJob?.cancel()
        trackingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking()
    }
}
