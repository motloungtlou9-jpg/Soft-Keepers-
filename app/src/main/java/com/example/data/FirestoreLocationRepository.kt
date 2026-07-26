package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * DriverLocationData model for Firestore synchronization.
 */
data class DriverLocationData(
    val driverId: String = "",
    val driverName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val heading: Float = 0f,
    val speed: Float = 0f,
    val rideId: String? = null,
    val isOnline: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Firestore repository handling real-time driver location synchronization.
 */
object FirestoreLocationRepository {
    private const val TAG = "FirestoreLocationRepo"
    private const val COLLECTION_DRIVERS = "driver_locations"
    private const val COLLECTION_RIDES = "active_rides"

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    /**
     * Publish or update driver's current GPS location in real-time in Firestore.
     */
    fun updateDriverLocation(
        driverId: String,
        driverName: String = "Driver",
        latitude: Double,
        longitude: Double,
        rideId: String? = null,
        isOnline: Boolean = true,
        heading: Float = 0f,
        speed: Float = 0f
    ) {
        try {
            val locationMap = hashMapOf(
                "driverId" to driverId,
                "driverName" to driverName,
                "latitude" to latitude,
                "longitude" to longitude,
                "heading" to heading,
                "speed" to speed,
                "rideId" to (rideId ?: ""),
                "isOnline" to isOnline,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection(COLLECTION_DRIVERS)
                .document(driverId)
                .set(locationMap, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Driver location updated successfully in Firestore for $driverId")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating driver location in Firestore", e)
                }

            // If attached to an active ride, sync driver location directly on the ride document as well
            if (!rideId.isNullOrEmpty()) {
                val rideUpdateMap = hashMapOf(
                    "driverLat" to latitude,
                    "driverLng" to longitude,
                    "lastUpdated" to System.currentTimeMillis()
                )
                firestore.collection(COLLECTION_RIDES)
                    .document(rideId)
                    .set(rideUpdateMap, SetOptions.merge())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore initialization or network exception", e)
        }
    }

    /**
     * Real-time listener for a specific driver's GPS location via Kotlin Flow callbackFlow.
     */
    fun observeDriverLocation(driverId: String): Flow<DriverLocationData?> = callbackFlow {
        if (driverId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listenerRegistration = try {
            firestore.collection(COLLECTION_DRIVERS)
                .document(driverId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen failed for driver location", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val lat = snapshot.getDouble("latitude") ?: 0.0
                        val lng = snapshot.getDouble("longitude") ?: 0.0
                        val name = snapshot.getString("driverName") ?: ""
                        val ride = snapshot.getString("rideId")
                        val online = snapshot.getBoolean("isOnline") ?: true
                        val ts = snapshot.getLong("timestamp") ?: System.currentTimeMillis()

                        val data = DriverLocationData(
                            driverId = driverId,
                            driverName = name,
                            latitude = lat,
                            longitude = lng,
                            rideId = ride,
                            isOnline = online,
                            timestamp = ts
                        )
                        trySend(data)
                    } else {
                        trySend(null)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching snapshot listener for $driverId", e)
            null
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }

    /**
     * Real-time listener for a specific ride's driver GPS location.
     */
    fun observeRideDriverLocation(rideId: String): Flow<Pair<Double, Double>?> = callbackFlow {
        if (rideId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listenerRegistration = try {
            firestore.collection(COLLECTION_RIDES)
                .document(rideId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen failed for ride driver location", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val lat = snapshot.getDouble("driverLat")
                        val lng = snapshot.getDouble("driverLng")
                        if (lat != null && lng != null) {
                            trySend(Pair(lat, lng))
                        } else {
                            trySend(null)
                        }
                    } else {
                        trySend(null)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching snapshot listener for ride $rideId", e)
            null
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }

    /**
     * Real-time listener for all active drivers from Firestore collection "driver_locations".
     */
    fun observeAllActiveDrivers(): Flow<List<DriverLocationData>> = callbackFlow {
        val listenerRegistration = try {
            firestore.collection(COLLECTION_DRIVERS)
                .addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen failed for active drivers", error)
                        return@addSnapshotListener
                    }

                    if (querySnapshot != null) {
                        val driversList = querySnapshot.documents.mapNotNull { doc ->
                            val driverId = doc.getString("driverId") ?: doc.id
                            val driverName = doc.getString("driverName") ?: "Driver"
                            val lat = doc.getDouble("latitude") ?: 0.0
                            val lng = doc.getDouble("longitude") ?: 0.0
                            val heading = doc.getDouble("heading")?.toFloat() ?: 0f
                            val speed = doc.getDouble("speed")?.toFloat() ?: 0f
                            val rideId = doc.getString("rideId")
                            val isOnline = doc.getBoolean("isOnline") ?: true
                            val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()

                            DriverLocationData(
                                driverId = driverId,
                                driverName = driverName,
                                latitude = lat,
                                longitude = lng,
                                heading = heading,
                                speed = speed,
                                rideId = rideId,
                                isOnline = isOnline,
                                timestamp = ts
                            )
                        }
                        trySend(driversList)
                    } else {
                        trySend(emptyList())
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error listening to all active drivers", e)
            trySend(emptyList())
            null
        }

        awaitClose {
            listenerRegistration?.remove()
        }
    }
}
