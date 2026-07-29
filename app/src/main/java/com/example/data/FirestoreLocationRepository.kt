package com.example.data

import android.util.Log
import com.example.model.*
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

    /**
     * Synchronize a ride request or update to Firestore active_rides collection.
     */
    fun syncRideToFirestore(ride: Ride) {
        try {
            val map = hashMapOf(
                "id" to ride.id,
                "passengerId" to ride.passengerId,
                "passengerName" to ride.passengerName,
                "passengerPhone" to ride.passengerPhone,
                "pickupAddress" to ride.pickupAddress,
                "dropoffAddress" to ride.dropoffAddress,
                "pickupLat" to ride.pickupLat,
                "pickupLng" to ride.pickupLng,
                "dropoffLat" to ride.dropoffLat,
                "dropoffLng" to ride.dropoffLng,
                "fare" to ride.fare,
                "offeredFare" to (ride.offeredFare ?: 0.0),
                "driverId" to (ride.driverId ?: ""),
                "driverName" to (ride.driverName ?: ""),
                "driverPhone" to (ride.driverPhone ?: ""),
                "vehicleInfo" to (ride.vehicleInfo ?: ""),
                "vehicleRegistration" to (ride.vehicleRegistration ?: ""),
                "driverLat" to ride.driverLat,
                "driverLng" to ride.driverLng,
                "status" to ride.status.name,
                "quickMessage" to (ride.quickMessage ?: ""),
                "createdAt" to ride.createdAt
            )
            firestore.collection(COLLECTION_RIDES)
                .document(ride.id)
                .set(map, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Ride synced to Firestore successfully: ${ride.id}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error syncing ride to Firestore", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception syncing ride to Firestore", e)
        }
    }

    /**
     * Update ride lifecycle status and metadata in Firestore.
     */
    fun updateRideStatusInFirestore(
        rideId: String,
        status: RideStatus,
        driverId: String? = null,
        driverName: String? = null,
        offeredFare: Double? = null,
        quickMessage: String? = null
    ) {
        try {
            val updates = mutableMapOf<String, Any>(
                "status" to status.name
            )
            driverId?.let { updates["driverId"] = it }
            driverName?.let { updates["driverName"] = it }
            offeredFare?.let { updates["offeredFare"] = it }
            quickMessage?.let { updates["quickMessage"] = it }

            firestore.collection(COLLECTION_RIDES)
                .document(rideId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Ride status updated in Firestore for $rideId to ${status.name}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating ride status in Firestore", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception updating ride status in Firestore", e)
        }
    }

    /**
     * Real-time listener for a specific ride's lifecycle and state updates.
     */
    fun observeRide(rideId: String): Flow<Ride?> = callbackFlow {
        if (rideId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_RIDES)
            .document(rideId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for ride $rideId", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val id = snapshot.getString("id") ?: rideId
                    val passId = snapshot.getString("passengerId") ?: ""
                    val passName = snapshot.getString("passengerName") ?: ""
                    val passPhone = snapshot.getString("passengerPhone") ?: ""
                    val pickupAddr = snapshot.getString("pickupAddress") ?: ""
                    val dropoffAddr = snapshot.getString("dropoffAddress") ?: ""
                    val pickupLat = snapshot.getDouble("pickupLat") ?: 0.0
                    val pickupLng = snapshot.getDouble("pickupLng") ?: 0.0
                    val dropoffLat = snapshot.getDouble("dropoffLat") ?: 0.0
                    val dropoffLng = snapshot.getDouble("dropoffLng") ?: 0.0
                    val fare = snapshot.getDouble("fare") ?: 0.0
                    val offeredFare = snapshot.getDouble("offeredFare")?.takeIf { it > 0.0 }
                    val drvId = snapshot.getString("driverId").takeIf { !it.isNullOrBlank() }
                    val drvName = snapshot.getString("driverName").takeIf { !it.isNullOrBlank() }
                    val drvPhone = snapshot.getString("driverPhone").takeIf { !it.isNullOrBlank() }
                    val vehInfo = snapshot.getString("vehicleInfo")?.takeIf { !it.isNullOrBlank() }
                    val vehReg = snapshot.getString("vehicleRegistration")?.takeIf { !it.isNullOrBlank() }
                    val drvLat = snapshot.getDouble("driverLat") ?: (pickupLat - 0.015)
                    val drvLng = snapshot.getDouble("driverLng") ?: (pickupLng - 0.010)
                    val statusStr = snapshot.getString("status") ?: "WAITING"
                    val rideStatus = try { RideStatus.valueOf(statusStr) } catch (e: Exception) { RideStatus.WAITING }
                    val qMsg = snapshot.getString("quickMessage")?.takeIf { !it.isNullOrBlank() }
                    val createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis()

                    val ride = Ride(
                        id = id,
                        passengerId = passId,
                        passengerName = passName,
                        passengerPhone = passPhone,
                        pickupAddress = pickupAddr,
                        dropoffAddress = dropoffAddr,
                        pickupLat = pickupLat,
                        pickupLng = pickupLng,
                        dropoffLat = dropoffLat,
                        dropoffLng = dropoffLng,
                        fare = fare,
                        offeredFare = offeredFare,
                        driverId = drvId,
                        driverName = drvName,
                        driverPhone = drvPhone,
                        vehicleInfo = vehInfo,
                        vehicleRegistration = vehReg,
                        driverLat = drvLat,
                        driverLng = drvLng,
                        status = rideStatus,
                        quickMessage = qMsg,
                        createdAt = createdAt
                    )
                    trySend(ride)
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            listener.remove()
        }
    }
}
