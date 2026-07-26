package com.example.model

enum class UserRole {
    PASSENGER,
    DRIVER,
    ADMIN
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val profilePhotoUrl: String? = null,
    val isSuspended: Boolean = false,
    val isBlocked: Boolean = false,
    val isCrownVerified: Boolean = false, // True if Admin met user face-to-face and bestowed a Crown 👑
    val crownVerifiedAt: Long? = null,
    val realContactDetails: String? = null, // Admin master records
    val faceToFaceNotes: String? = null,
    val accountBalance: Double = 250.00, // Passenger wallet balance
    val isLocationEnabled: Boolean = true, // Location state for ride requests
    val currentLat: Double? = null,
    val currentLng: Double? = null,
    val isNotificationsEnabled: Boolean = true, // Push notification switch
    val registrationStatus: String = "APPROVED", // "APPROVED", "PENDING", "DECLINED"
    val createdAt: Long = System.currentTimeMillis()
)

data class DriverProfile(
    val userId: String,
    val vehicleMake: String,
    val vehicleModel: String,
    val vehicleRegistration: String,
    val driverLicenseNumber: String,
    val licensePhotoUrl: String? = null,
    val vehiclePhotoUrl: String? = null,
    val isOnline: Boolean = true,
    val currentLat: Double = -26.2041, // Default Johannesburg / city center
    val currentLng: Double = 28.0473,
    val subscriptionStatus: String = "Active", // Active, Expired, Pending
    val subscriptionExpiryDate: String = "2026-12-31"
)

enum class RideStatus {
    WAITING,
    DRIVER_OFFERED_FARE,
    ACCEPTED,
    DRIVER_ON_THE_WAY,
    DRIVER_ARRIVED,
    TRIP_STARTED,
    TRIP_COMPLETED,
    CANCELLED
}

data class Ride(
    val id: String,
    val passengerId: String,
    val passengerName: String,
    val passengerPhone: String,
    val pickupAddress: String,
    val dropoffAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropoffLat: Double,
    val dropoffLng: Double,
    val fare: Double,
    val offeredFare: Double? = null,
    val driverId: String? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val vehicleInfo: String? = null,
    val vehicleRegistration: String? = null,
    val driverLat: Double = pickupLat - 0.012,
    val driverLng: Double = pickupLng - 0.008,
    val status: RideStatus = RideStatus.WAITING,
    val quickMessage: String? = null,
    val rating: Float? = null,
    val complaintSubmitted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

data class Complaint(
    val id: String,
    val rideId: String,
    val passengerId: String,
    val passengerName: String,
    val driverId: String,
    val driverName: String,
    val category: String, // "Unsafe driving", "Late arrival", "Wrong behaviour", "Vehicle issue", "Other"
    val details: String,
    val status: String = "PENDING", // PENDING, RESOLVED, WARNED, SUSPENDED
    val createdAt: Long = System.currentTimeMillis()
)

data class DriverSubscription(
    val id: String,
    val driverId: String,
    val driverName: String,
    val planName: String, // "Monthly Pass - $29", "Annual VIP - $290"
    val price: Double,
    val status: String, // ACTIVE, EXPIRED, PENDING
    val startDate: String,
    val expiryDate: String
)

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val recipientRole: String, // ALL, PASSENGER, DRIVER, ADMIN
    val recipientId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
