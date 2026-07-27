package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.FirestoreLocationRepository
import com.google.android.gms.location.*

/**
 * Foreground service using Google Play Services Location API to continuously
 * push live driver GPS coordinates to Firestore in real-time.
 */
class LocationUpdatesService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var driverId: String = "unknown_driver"
    private var driverName: String = "Driver"
    private var rideId: String? = null

    companion object {
        private const val TAG = "LocationUpdatesService"
        private const val CHANNEL_ID = "SoftKeeperLocationChannel"
        private const val NOTIFICATION_ID = 1001

        const val EXTRA_DRIVER_ID = "extra_driver_id"
        const val EXTRA_DRIVER_NAME = "extra_driver_name"
        const val EXTRA_RIDE_ID = "extra_ride_id"

        fun startService(context: Context, driverId: String, driverName: String, rideId: String? = null) {
            val intent = Intent(context, LocationUpdatesService::class.java).apply {
                putExtra(EXTRA_DRIVER_ID, driverId)
                putExtra(EXTRA_DRIVER_NAME, driverName)
                putExtra(EXTRA_RIDE_ID, rideId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, LocationUpdatesService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    Log.d(TAG, "Location update: ${location.latitude}, ${location.longitude}, heading: ${location.bearing}, speed: ${location.speed}")
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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            driverId = it.getStringExtra(EXTRA_DRIVER_ID) ?: driverId
            driverName = it.getStringExtra(EXTRA_DRIVER_NAME) ?: driverName
            rideId = it.getStringExtra(EXTRA_RIDE_ID)
        }

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        startLocationUpdates()

        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                4000L
            ).setMinUpdateIntervalMillis(2000L)
             .setWaitForAccurateLocation(false)
             .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "FusedLocationProviderClient updates requested successfully for driver: $driverId")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while requesting location updates", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception while requesting location updates", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Driver Live Location Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps driver GPS coordinates synced to Firestore in real-time"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Soft Keeper Live Tracking")
            .setContentText("Broadcasting live GPS coordinates to Firestore...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Log.d(TAG, "LocationUpdatesService destroyed and location updates removed")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing location updates on destroy", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
