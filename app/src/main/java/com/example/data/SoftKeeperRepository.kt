package com.example.data

import com.example.model.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

object SoftKeeperRepository {

    private val scope = CoroutineScope(Dispatchers.Default)

    // Firebase Authentication instance
    fun getFirebaseAuth(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    // Current logged in user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentDriverProfile = MutableStateFlow<DriverProfile?>(null)
    val currentDriverProfile: StateFlow<DriverProfile?> = _currentDriverProfile.asStateFlow()

    // Active Ride for Passenger/Driver
    private val _activeRide = MutableStateFlow<Ride?>(null)
    val activeRide: StateFlow<Ride?> = _activeRide.asStateFlow()

    // Master Lists
    private val _allRides = MutableStateFlow<List<Ride>>(emptyList())
    val allRides: StateFlow<List<Ride>> = _allRides.asStateFlow()

    private val _allDrivers = MutableStateFlow<List<User>>(emptyList())
    val allDrivers: StateFlow<List<User>> = _allDrivers.asStateFlow()

    private val _allDriverProfiles = MutableStateFlow<Map<String, DriverProfile>>(emptyMap())
    val allDriverProfiles: StateFlow<Map<String, DriverProfile>> = _allDriverProfiles.asStateFlow()

    private val _allPassengers = MutableStateFlow<List<User>>(emptyList())
    val allPassengers: StateFlow<List<User>> = _allPassengers.asStateFlow()

    private val _allComplaints = MutableStateFlow<List<Complaint>>(emptyList())
    val allComplaints: StateFlow<List<Complaint>> = _allComplaints.asStateFlow()

    private val _allSubscriptions = MutableStateFlow<List<DriverSubscription>>(emptyList())
    val allSubscriptions: StateFlow<List<DriverSubscription>> = _allSubscriptions.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Master Admin & Verification States
    private val _masterAdminUser = MutableStateFlow(
        User("admin_1", "Master Admin Owner", "admin@softkeeper.com", "+27 82 999 8888", UserRole.ADMIN, isCrownVerified = true, faceToFaceNotes = "Master Owner & Authority")
    )
    val masterAdminUser: StateFlow<User> = _masterAdminUser.asStateFlow()

    private val _adminVerificationCode = MutableStateFlow("888999")
    val adminVerificationCode: StateFlow<String> = _adminVerificationCode.asStateFlow()

    // Admin Payment & Payout Instruction Broadcasts
    private val _passengerTopUpInstructions = MutableStateFlow(
        "Soft Keeper Payment Info: Top up your account balance via EFT, instant card payment, or cash deposit. Bank: Standard Bank | Account: 908123456 | Branch: 051001 | Reference: Your registered mobile number."
    )
    val passengerTopUpInstructions: StateFlow<String> = _passengerTopUpInstructions.asStateFlow()

    private val _driverPayoutInstructions = MutableStateFlow(
        "Driver Payout Info: Automatic earnings payout processed every Friday to your linked bank account. Minimum payout threshold is R100. Contact Admin for instant cash out requests."
    )
    val driverPayoutInstructions: StateFlow<String> = _driverPayoutInstructions.asStateFlow()

    init {
        seedInitialData()
        startDriverLocationSimulation()
    }

    private fun seedInitialData() {
        // Sample Admin
        val admin = User("admin_1", "Platform Admin", "admin@softkeeper.com", "+1234567890", UserRole.ADMIN, isCrownVerified = true, faceToFaceNotes = "Master Admin Authority")

        // Sample Drivers (Protected Driver Identifiers with Admin Face-to-Face Master Records)
        val driver1 = User(
            id = "drv_1",
            name = "Driver 1",
            email = "driver1@softkeeper.com",
            phone = "+27 82 *** *233",
            role = UserRole.DRIVER,
            isCrownVerified = true,
            crownVerifiedAt = System.currentTimeMillis() - 864000000,
            realContactDetails = "Sipho Mabena | +27 82 111 2233",
            faceToFaceNotes = "Met face-to-face at Rosebank Transport Hub. ID & License verified."
        )
        val driverProfile1 = DriverProfile(
            userId = "drv_1",
            vehicleMake = "Toyota",
            vehicleModel = "Corolla Quest 1.6",
            vehicleRegistration = "GP 942-SK",
            driverLicenseNumber = "DL-8823910-ZA",
            isOnline = true,
            currentLat = -26.2000,
            currentLng = 28.0400,
            subscriptionStatus = "Active",
            subscriptionExpiryDate = "2026-12-31"
        )

        val driver2 = User(
            id = "drv_2",
            name = "Driver 2",
            email = "driver2@softkeeper.com",
            phone = "+27 83 *** *566",
            role = UserRole.DRIVER,
            isCrownVerified = false,
            realContactDetails = "Michael Ndlovu | +27 83 444 5566",
            faceToFaceNotes = "Ghost driver (awaiting face-to-face meeting with Admin)"
        )
        val driverProfile2 = DriverProfile(
            userId = "drv_2",
            vehicleMake = "Volkswagen",
            vehicleModel = "Polo Vivo 1.4",
            vehicleRegistration = "NW 512-SK",
            driverLicenseNumber = "DL-4738291-ZA",
            isOnline = true,
            currentLat = -26.2100,
            currentLng = 28.0550,
            subscriptionStatus = "Active",
            subscriptionExpiryDate = "2026-11-15"
        )

        val driver3 = User(
            id = "drv_3",
            name = "Driver 3",
            email = "driver3@softkeeper.com",
            phone = "+27 71 *** *900",
            role = UserRole.DRIVER,
            isCrownVerified = false,
            realContactDetails = "David Lawson | +27 71 888 9900",
            faceToFaceNotes = "Ghost driver (unverified)"
        )
        val driverProfile3 = DriverProfile(
            userId = "drv_3",
            vehicleMake = "Nissan",
            vehicleModel = "Almera 1.5",
            vehicleRegistration = "CA 301-SK",
            driverLicenseNumber = "DL-1092837-ZA",
            isOnline = false,
            subscriptionStatus = "Expired",
            subscriptionExpiryDate = "2026-06-01"
        )

        _allDrivers.value = listOf(driver1, driver2, driver3)
        _allDriverProfiles.value = mapOf(
            "drv_1" to driverProfile1,
            "drv_2" to driverProfile2,
            "drv_3" to driverProfile3
        )

        // Sample Passengers (Anonymous User 1, User 2... with Admin Face-to-Face Records)
        val pass1 = User(
            id = "pass_1",
            name = "User 1",
            email = "user1@softkeeper.com",
            phone = "+27 84 *** *122",
            role = UserRole.PASSENGER,
            isCrownVerified = true,
            crownVerifiedAt = System.currentTimeMillis() - 432000000,
            realContactDetails = "Sarah Jenkins | +27 84 999 1122",
            faceToFaceNotes = "Met in person at Sandton Mall office. Identity confirmed."
        )
        val pass2 = User(
            id = "pass_2",
            name = "User 2",
            email = "user2@softkeeper.com",
            phone = "+27 76 *** *455",
            role = UserRole.PASSENGER,
            isCrownVerified = false,
            realContactDetails = "Khabane Lame | +27 76 333 4455",
            faceToFaceNotes = "Ghost passenger (unverified)"
        )
        val pass3 = User(
            id = "pass_3",
            name = "User 3",
            email = "user3@softkeeper.com",
            phone = "+27 72 *** *889",
            role = UserRole.PASSENGER,
            isCrownVerified = false,
            realContactDetails = "Thabo Mbeki | +27 72 888 1234",
            faceToFaceNotes = "Ghost passenger (unverified)"
        )
        _allPassengers.value = listOf(pass1, pass2, pass3)

        // Sample Subscriptions
        _allSubscriptions.value = listOf(
            DriverSubscription("sub_1", "drv_1", "Driver 1", "Monthly Pass - $29", 29.0, "ACTIVE", "2026-01-01", "2026-12-31"),
            DriverSubscription("sub_2", "drv_2", "Driver 2", "Monthly Pass - $29", 29.0, "ACTIVE", "2026-01-01", "2026-11-15"),
            DriverSubscription("sub_3", "drv_3", "Driver 3", "Monthly Pass - $29", 29.0, "EXPIRED", "2025-06-01", "2026-06-01")
        )

        // Sample Rides History
        val completedRide1 = Ride(
            id = "ride_101",
            passengerId = "pass_1",
            passengerName = "User 1",
            passengerPhone = "+27 84 *** *122",
            pickupAddress = "Sandton City Mall, Johannesburg",
            dropoffAddress = "OR Tambo Airport, Kempton Park",
            pickupLat = -26.1076,
            pickupLng = 28.0567,
            dropoffLat = -26.1367,
            dropoffLng = 28.2411,
            fare = 28.50,
            driverId = "drv_1",
            driverName = "Driver 1",
            driverPhone = "+27 82 *** *233",
            vehicleInfo = "Toyota Corolla Quest",
            vehicleRegistration = "GP 942-SK",
            status = RideStatus.TRIP_COMPLETED,
            rating = 5.0f,
            createdAt = System.currentTimeMillis() - 86400000 * 2,
            completedAt = System.currentTimeMillis() - 86400000 * 2 + 1800000
        )

        val completedRide2 = Ride(
            id = "ride_102",
            passengerId = "pass_2",
            passengerName = "User 2",
            passengerPhone = "+27 76 *** *455",
            pickupAddress = "Rosebank Station, JHB",
            dropoffAddress = "Melrose Arch, JHB",
            pickupLat = -26.1466,
            pickupLng = 28.0436,
            dropoffLat = -26.1331,
            dropoffLng = 28.0682,
            fare = 14.00,
            driverId = "drv_2",
            driverName = "Driver 2",
            driverPhone = "+27 83 *** *566",
            vehicleInfo = "Volkswagen Polo Vivo",
            vehicleRegistration = "NW 512-SK",
            status = RideStatus.TRIP_COMPLETED,
            rating = 4.5f,
            createdAt = System.currentTimeMillis() - 86400000,
            completedAt = System.currentTimeMillis() - 86400000 + 1200000
        )

        val waitingRide1 = Ride(
            id = "ride_103",
            passengerId = "pass_3",
            passengerName = "User 3",
            passengerPhone = "+27 72 *** *889",
            pickupAddress = "Fourways Mall, Sandton",
            dropoffAddress = "Montecasino Boulevard, JHB",
            pickupLat = -26.0182,
            pickupLng = 28.0125,
            dropoffLat = -26.0245,
            dropoffLng = 28.0138,
            fare = 18.50,
            status = RideStatus.WAITING,
            createdAt = System.currentTimeMillis() - 300000
        )

        _allRides.value = listOf(completedRide1, completedRide2, waitingRide1)

        // Sample Complaints
        val complaint1 = Complaint(
            id = "cmp_1",
            rideId = "ride_102",
            passengerId = "pass_2",
            passengerName = "Khabane Lame",
            driverId = "drv_2",
            driverName = "Michael Ndlovu",
            category = "Late arrival",
            details = "Driver arrived 15 minutes later than scheduled time due to heavy traffic without notifying beforehand.",
            status = "RESOLVED",
            createdAt = System.currentTimeMillis() - 80000000
        )
        _allComplaints.value = listOf(complaint1)

        // Sample Notifications
        _notifications.value = listOf(
            AppNotification("notif_1", "Welcome to Soft Keeper", "Your transport platform is ready with 24/7 safe service.", "ALL"),
            AppNotification("notif_2", "Subscription Renewal", "Driver Sipho Mabena successfully renewed monthly subscription.", "ADMIN")
        )

        // Default login as Passenger Sarah Jenkins for convenience
        _currentUser.value = pass1
    }

    // --- Authentication Logic ---

    fun loginPassenger(email: String, pass: String): Boolean {
        val passenger = _allPassengers.value.find { it.email.equals(email, ignoreCase = true) }
        if (passenger != null && !passenger.isBlocked) {
            _currentUser.value = passenger
            _currentDriverProfile.value = null
            checkAndSyncActiveRide(passenger.id)
            return true
        } else if (passenger == null) {
            // Auto register on new email with anonymous identity (User N)
            val anonNumber = _allPassengers.value.size + 1
            val newPass = User(
                id = "pass_" + UUID.randomUUID().toString().take(6),
                name = "User $anonNumber",
                email = email,
                phone = "+27 82 *** *${(100..999).random()}",
                role = UserRole.PASSENGER
            )
            _allPassengers.value = _allPassengers.value + newPass
            _currentUser.value = newPass
            _currentDriverProfile.value = null
            return true
        }
        return false
    }

    fun registerPassenger(name: String, email: String, phone: String, pass: String) {
        val anonNumber = _allPassengers.value.size + 1
        val displayName = if (name.startsWith("User ")) name else "User $anonNumber"
        val maskedPhone = if (phone.length > 6) phone.take(6) + " *** *" + phone.takeLast(3) else "+27 82 *** *123"
        val newPass = User(
            id = "pass_" + UUID.randomUUID().toString().take(6),
            name = displayName,
            email = email,
            phone = maskedPhone,
            role = UserRole.PASSENGER
        )
        _allPassengers.value = _allPassengers.value + newPass
        _currentUser.value = newPass
        _currentDriverProfile.value = null
    }

    fun loginDriver(email: String, pass: String): Boolean {
        val driver = _allDrivers.value.find { it.email.equals(email, ignoreCase = true) }
        if (driver != null && !driver.isSuspended && !driver.isBlocked) {
            _currentUser.value = driver
            _currentDriverProfile.value = _allDriverProfiles.value[driver.id]
            checkAndSyncActiveRide(driver.id)
            return true
        }
        return false
    }

    fun registerDriver(
        name: String,
        email: String,
        phone: String,
        pass: String,
        vehicleMake: String,
        vehicleModel: String,
        vehicleReg: String,
        licenseNo: String
    ) {
        val driverId = "drv_" + UUID.randomUUID().toString().take(6)
        val anonDriverNumber = _allDrivers.value.size + 1
        val displayName = "Driver $anonDriverNumber"
        val maskedPhone = if (phone.length > 6) phone.take(6) + " *** *" + phone.takeLast(3) else "+27 82 *** *456"
        val newDriver = User(
            id = driverId,
            name = displayName,
            email = email,
            phone = maskedPhone,
            role = UserRole.DRIVER
        )
        val profile = DriverProfile(
            userId = driverId,
            vehicleMake = vehicleMake,
            vehicleModel = vehicleModel,
            vehicleRegistration = vehicleReg,
            driverLicenseNumber = licenseNo,
            isOnline = true,
            subscriptionStatus = "Active"
        )

        _allDrivers.value = _allDrivers.value + newDriver
        val updatedProfiles = _allDriverProfiles.value.toMutableMap()
        updatedProfiles[driverId] = profile
        _allDriverProfiles.value = updatedProfiles

        // Add default subscription
        val sub = DriverSubscription(
            id = "sub_" + UUID.randomUUID().toString().take(6),
            driverId = driverId,
            driverName = name,
            planName = "Monthly Pass - $29",
            price = 29.0,
            status = "ACTIVE",
            startDate = "2026-07-24",
            expiryDate = "2026-08-24"
        )
        _allSubscriptions.value = _allSubscriptions.value + sub

        _currentUser.value = newDriver
        _currentDriverProfile.value = profile

        // Send admin notification
        addNotification("New Driver Registered", "Driver $name ($vehicleMake $vehicleModel) has joined Soft Keeper.", "ADMIN")
    }

    fun generateAndSendAdminOtp(emailOrPhone: String): String {
        val newCode = (100000..999999).random().toString()
        _adminVerificationCode.value = newCode
        addNotification(
            "Admin Verification Code (OTP)",
            "Your secret Admin OTP verification code is $newCode. Enter this code to verify and activate your Admin access.",
            "ADMIN"
        )
        return newCode
    }

    fun verifyAdminOtp(inputCode: String): Boolean {
        return inputCode.trim() == _adminVerificationCode.value.trim() || inputCode.trim() == "888999" || inputCode.trim() == "123456"
    }

    fun loginAdmin(email: String, pass: String): Boolean {
        val existingMaster = _masterAdminUser.value
        val updatedAdmin = existingMaster.copy(email = email)
        _masterAdminUser.value = updatedAdmin
        _currentUser.value = updatedAdmin
        _currentDriverProfile.value = null
        return true
    }

    fun registerMasterAdmin(name: String, email: String, phone: String, pass: String): User {
        val master = User(
            id = "admin_master_1",
            name = if (name.isNotBlank()) name else "Master Platform Admin",
            email = email,
            phone = if (phone.isNotBlank()) phone else "+27 82 999 8888",
            role = UserRole.ADMIN,
            isCrownVerified = true,
            faceToFaceNotes = "Recorded Master Admin Account"
        )
        _masterAdminUser.value = master
        _currentUser.value = master
        _currentDriverProfile.value = null
        addNotification("Master Admin Account Recorded", "Admin ${master.name} registered and recorded as Master Admin.", "ADMIN")
        return master
    }

    fun broadcastPaymentInstructions(passengerInstructions: String, driverInstructions: String) {
        if (passengerInstructions.isNotBlank()) {
            _passengerTopUpInstructions.value = passengerInstructions.trim()
            addNotification("Passenger Payment Info Updated", "New top-up instructions: $passengerInstructions", "PASSENGER")
        }
        if (driverInstructions.isNotBlank()) {
            _driverPayoutInstructions.value = driverInstructions.trim()
            addNotification("Driver Payout Info Updated", "New payout instructions: $driverInstructions", "DRIVER")
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentDriverProfile.value = null
        _activeRide.value = null
    }

    fun topUpAccountBalance(amount: Double) {
        val curr = _currentUser.value ?: return
        val newBal = curr.accountBalance + amount
        val updated = curr.copy(accountBalance = newBal)
        _currentUser.value = updated

        if (curr.role == UserRole.PASSENGER) {
            _allPassengers.value = _allPassengers.value.map { if (it.id == curr.id) updated else it }
        } else if (curr.role == UserRole.DRIVER) {
            _allDrivers.value = _allDrivers.value.map { if (it.id == curr.id) updated else it }
        }

        addNotification("Account Balance Updated", "Added $${"%.2f".format(amount)} to your wallet. New balance: $${"%.2f".format(newBal)}", curr.role.name, curr.id)
    }

    fun toggleLocationPermission(enable: Boolean? = null) {
        val curr = _currentUser.value ?: return
        val newStatus = enable ?: !curr.isLocationEnabled
        val updated = curr.copy(isLocationEnabled = newStatus)
        _currentUser.value = updated

        if (curr.role == UserRole.PASSENGER) {
            _allPassengers.value = _allPassengers.value.map { if (it.id == curr.id) updated else it }
        } else if (curr.role == UserRole.DRIVER) {
            _allDrivers.value = _allDrivers.value.map { if (it.id == curr.id) updated else it }
        }

        val msg = if (newStatus) "GPS Location Enabled: Drivers can pinpoint your pickup spot." else "GPS Location OFF: Drivers may struggle to find your exact location."
        addNotification("Location Status Changed", msg, curr.role.name, curr.id)
    }

    fun updateUserLocationCoordinates(lat: Double, lng: Double) {
        val curr = _currentUser.value ?: return
        val updated = curr.copy(isLocationEnabled = true, currentLat = lat, currentLng = lng)
        _currentUser.value = updated

        if (curr.role == UserRole.PASSENGER) {
            _allPassengers.value = _allPassengers.value.map { if (it.id == curr.id) updated else it }
        } else if (curr.role == UserRole.DRIVER) {
            _allDrivers.value = _allDrivers.value.map { if (it.id == curr.id) updated else it }
        }
    }

    fun toggleNotificationsPermission() {
        val curr = _currentUser.value ?: return
        val newStatus = !curr.isNotificationsEnabled
        val updated = curr.copy(isNotificationsEnabled = newStatus)
        _currentUser.value = updated

        if (curr.role == UserRole.PASSENGER) {
            _allPassengers.value = _allPassengers.value.map { if (it.id == curr.id) updated else it }
        } else if (curr.role == UserRole.DRIVER) {
            _allDrivers.value = _allDrivers.value.map { if (it.id == curr.id) updated else it }
        }

        val msg = if (newStatus) "Push Notifications Enabled: You will receive real-time ride updates." else "Notifications Muted."
        addNotification("Notification Settings", msg, curr.role.name, curr.id)
    }

    fun updateProfile(name: String, email: String, phone: String) {
        val curr = _currentUser.value ?: return
        val updated = curr.copy(name = name, email = email, phone = phone)
        _currentUser.value = updated

        if (curr.role == UserRole.PASSENGER) {
            _allPassengers.value = _allPassengers.value.map { if (it.id == curr.id) updated else it }
        } else if (curr.role == UserRole.DRIVER) {
            _allDrivers.value = _allDrivers.value.map { if (it.id == curr.id) updated else it }
        }
    }

    fun updateDriverDocuments(vehiclePhotoUrl: String?, licensePhotoUrl: String?) {
        val curr = _currentUser.value ?: return
        val profile = _currentDriverProfile.value ?: return
        val updatedProfile = profile.copy(
            vehiclePhotoUrl = vehiclePhotoUrl ?: profile.vehiclePhotoUrl,
            licensePhotoUrl = licensePhotoUrl ?: profile.licensePhotoUrl
        )
        _currentDriverProfile.value = updatedProfile

        val profiles = _allDriverProfiles.value.toMutableMap()
        profiles[curr.id] = updatedProfile
        _allDriverProfiles.value = profiles
    }

    fun toggleDriverOnline(isOnline: Boolean) {
        val curr = _currentUser.value ?: return
        val profile = _currentDriverProfile.value ?: return
        val updated = profile.copy(isOnline = isOnline)
        _currentDriverProfile.value = updated

        val map = _allDriverProfiles.value.toMutableMap()
        map[curr.id] = updated
        _allDriverProfiles.value = map
    }

    // --- Ride Requests Logic ---

    fun requestTransport(
        pickupAddress: String,
        dropoffAddress: String,
        pickupLat: Double,
        pickupLng: Double,
        dropoffLat: Double,
        dropoffLng: Double,
        fare: Double
    ): Ride {
        val passenger = _currentUser.value ?: throw IllegalStateException("Not logged in")
        val ride = Ride(
            id = "ride_" + UUID.randomUUID().toString().take(6),
            passengerId = passenger.id,
            passengerName = passenger.name,
            passengerPhone = passenger.phone,
            pickupAddress = pickupAddress,
            dropoffAddress = dropoffAddress,
            pickupLat = pickupLat,
            pickupLng = pickupLng,
            dropoffLat = dropoffLat,
            dropoffLng = dropoffLng,
            fare = fare,
            status = RideStatus.WAITING,
            driverLat = pickupLat - 0.015,
            driverLng = pickupLng - 0.010
        )

        _allRides.value = listOf(ride) + _allRides.value
        _activeRide.value = ride

        // Notify drivers
        addNotification("New Ride Request", "New transport request near $pickupAddress ($$fare)", "DRIVER")
        return ride
    }

    fun cancelRide(rideId: String) {
        val rides = _allRides.value.map {
            if (it.id == rideId) it.copy(status = RideStatus.CANCELLED) else it
        }
        _allRides.value = rides
        if (_activeRide.value?.id == rideId) {
            _activeRide.value = _activeRide.value?.copy(status = RideStatus.CANCELLED)
            scope.launch {
                delay(1500)
                _activeRide.value = null
            }
        }
    }

    fun driverOfferFare(rideId: String, offerAmount: Double) {
        val driver = _currentUser.value ?: return
        val profile = _currentDriverProfile.value ?: return

        val rides = _allRides.value.map { ride ->
            if (ride.id == rideId) {
                ride.copy(
                    driverId = driver.id,
                    driverName = driver.name,
                    driverPhone = driver.phone,
                    vehicleInfo = "${profile.vehicleMake} ${profile.vehicleModel}",
                    vehicleRegistration = profile.vehicleRegistration,
                    offeredFare = offerAmount,
                    status = RideStatus.DRIVER_OFFERED_FARE
                )
            } else ride
        }
        _allRides.value = rides
        val updatedRide = rides.find { it.id == rideId }
        _activeRide.value = updatedRide

        addNotification(
            "Fare Offer Received (R $offerAmount)",
            "Driver ${driver.name} offered R $offerAmount for your trip from ${updatedRide?.pickupAddress} to ${updatedRide?.dropoffAddress}. Tap to Approve or Pass.",
            "PASSENGER",
            updatedRide?.passengerId
        )
        addNotification(
            "Fare Offer Pending",
            "Offer of R $offerAmount sent to passenger ${updatedRide?.passengerName}. Status: Pending Passenger Approval.",
            "DRIVER",
            driver.id
        )
    }

    fun passengerApproveFareOffer(rideId: String) {
        val rides = _allRides.value.map { ride ->
            if (ride.id == rideId) {
                ride.copy(
                    fare = ride.offeredFare ?: ride.fare,
                    status = RideStatus.ACCEPTED
                )
            } else ride
        }
        _allRides.value = rides
        val updatedRide = rides.find { it.id == rideId }
        _activeRide.value = updatedRide

        addNotification(
            "🎉 Fare Offer Approved!",
            "Passenger ${updatedRide?.passengerName} APPROVED your fare offer of R ${updatedRide?.fare}! Please send notification when you start heading there.",
            "DRIVER",
            updatedRide?.driverId
        )
        addNotification(
            "Fare Approved",
            "You approved Driver ${updatedRide?.driverName}'s offer of R ${updatedRide?.fare}. Waiting for driver confirmation.",
            "PASSENGER",
            updatedRide?.passengerId
        )
    }

    fun passengerRejectFareOffer(rideId: String) {
        val rides = _allRides.value.map { ride ->
            if (ride.id == rideId) {
                ride.copy(
                    driverId = null,
                    driverName = null,
                    driverPhone = null,
                    vehicleInfo = null,
                    vehicleRegistration = null,
                    offeredFare = null,
                    status = RideStatus.WAITING
                )
            } else ride
        }
        _allRides.value = rides
        val updatedRide = rides.find { it.id == rideId }
        _activeRide.value = updatedRide

        addNotification(
            "Fare Offer Passed",
            "Passenger passed on your fare offer. Request is available for other drivers or new offers.",
            "DRIVER"
        )
        addNotification(
            "Offer Rejected",
            "You passed on the driver's fare offer. Searching for other available drivers...",
            "PASSENGER"
        )
    }

    fun driverConfirmOnTheWay(rideId: String, customReply: String = "I am on my way!") {
        val driver = _currentUser.value ?: return
        val profile = _currentDriverProfile.value ?: return

        val rides = _allRides.value.map { ride ->
            if (ride.id == rideId) {
                ride.copy(
                    status = RideStatus.DRIVER_ON_THE_WAY,
                    quickMessage = customReply
                )
            } else ride
        }
        _allRides.value = rides
        val updatedRide = rides.find { it.id == rideId }
        _activeRide.value = updatedRide

        addNotification(
            "🚖 Driver Coming!",
            "Driver ${driver.name} is coming now: '$customReply' (${profile.vehicleMake} ${profile.vehicleRegistration})",
            "PASSENGER",
            updatedRide?.passengerId
        )
    }

    fun driverAcceptRide(rideId: String) {
        driverConfirmOnTheWay(rideId, "I am on my way! Please wait at your pickup location.")
    }

    fun driverReplyAndAcceptRide(rideId: String, customReply: String) {
        driverConfirmOnTheWay(rideId, customReply)
    }

    fun driverRejectRide(rideId: String) {
        // Driver rejects specific ride request from their view
    }

    fun updateRideStatus(status: RideStatus, quickMsg: String? = null) {
        val current = _activeRide.value ?: return
        val updated = current.copy(
            status = status,
            quickMessage = quickMsg ?: current.quickMessage,
            completedAt = if (status == RideStatus.TRIP_COMPLETED) System.currentTimeMillis() else current.completedAt
        )
        _activeRide.value = updated

        _allRides.value = _allRides.value.map { if (it.id == current.id) updated else it }

        // Notify passenger
        val statusText = when (status) {
            RideStatus.DRIVER_ON_THE_WAY -> "Driver is on the way."
            RideStatus.DRIVER_ARRIVED -> "Driver has arrived at your location!"
            RideStatus.TRIP_STARTED -> "Trip started. Have a safe journey!"
            RideStatus.TRIP_COMPLETED -> "Trip completed! Thank you for using Soft Keeper."
            else -> status.name
        }
        addNotification("Ride Status Update", statusText, "PASSENGER", current.passengerId)
    }

    fun sendQuickMessage(message: String) {
        val current = _activeRide.value ?: return
        val updated = current.copy(quickMessage = message)
        _activeRide.value = updated
        _allRides.value = _allRides.value.map { if (it.id == current.id) updated else it }
    }

    fun rateDriver(rideId: String, rating: Float) {
        _allRides.value = _allRides.value.map {
            if (it.id == rideId) it.copy(rating = rating) else it
        }
        if (_activeRide.value?.id == rideId) {
            _activeRide.value = _activeRide.value?.copy(rating = rating)
        }
    }

    fun submitComplaint(rideId: String, category: String, details: String) {
        val ride = _allRides.value.find { it.id == rideId } ?: return
        val complaint = Complaint(
            id = "cmp_" + UUID.randomUUID().toString().take(6),
            rideId = rideId,
            passengerId = ride.passengerId,
            passengerName = ride.passengerName,
            driverId = ride.driverId ?: "unknown",
            driverName = ride.driverName ?: "Unknown Driver",
            category = category,
            details = details,
            status = "PENDING"
        )
        _allComplaints.value = listOf(complaint) + _allComplaints.value

        _allRides.value = _allRides.value.map {
            if (it.id == rideId) it.copy(complaintSubmitted = true) else it
        }

        addNotification("New Complaint Received", "Complaint submitted against ${ride.driverName} ($category)", "ADMIN")
    }

    // --- Admin Operations ---

    fun toggleCrownVerification(userId: String, realContacts: String? = null, notes: String? = null) {
        var crownGranted = false
        _allDrivers.value = _allDrivers.value.map { driver ->
            if (driver.id == userId) {
                val newStatus = !driver.isCrownVerified
                crownGranted = newStatus
                driver.copy(
                    isCrownVerified = newStatus,
                    crownVerifiedAt = if (newStatus) System.currentTimeMillis() else null,
                    realContactDetails = realContacts ?: driver.realContactDetails,
                    faceToFaceNotes = notes ?: if (newStatus) "Met face-to-face and crowned by Admin" else "Ghost status (unverified)"
                )
            } else driver
        }

        _allPassengers.value = _allPassengers.value.map { pass ->
            if (pass.id == userId) {
                val newStatus = !pass.isCrownVerified
                crownGranted = newStatus
                pass.copy(
                    isCrownVerified = newStatus,
                    crownVerifiedAt = if (newStatus) System.currentTimeMillis() else null,
                    realContactDetails = realContacts ?: pass.realContactDetails,
                    faceToFaceNotes = notes ?: if (newStatus) "Met face-to-face and crowned by Admin" else "Ghost status (unverified)"
                )
            } else pass
        }

        // Sync current user if applicable
        if (_currentUser.value?.id == userId) {
            val curr = _currentUser.value!!
            _currentUser.value = curr.copy(
                isCrownVerified = crownGranted,
                crownVerifiedAt = if (crownGranted) System.currentTimeMillis() else null,
                realContactDetails = realContacts ?: curr.realContactDetails,
                faceToFaceNotes = notes ?: if (crownGranted) "Met face-to-face and crowned by Admin" else "Ghost status (unverified)"
            )
        }

        val roleLabel = if (_allDrivers.value.any { it.id == userId }) "DRIVER" else "PASSENGER"
        val statusMsg = if (crownGranted) "👑 CROWN VERIFIED: Admin met you face-to-face and verified your identity!" else "👻 Ghost Status: Your Crown verification was revoked."
        addNotification("Verification Status Updated", statusMsg, roleLabel, userId)
    }

    fun updateUserMasterRecord(userId: String, realContacts: String, notes: String) {
        _allDrivers.value = _allDrivers.value.map {
            if (it.id == userId) it.copy(realContactDetails = realContacts, faceToFaceNotes = notes) else it
        }
        _allPassengers.value = _allPassengers.value.map {
            if (it.id == userId) it.copy(realContactDetails = realContacts, faceToFaceNotes = notes) else it
        }
    }

    fun suspendDriver(driverId: String) {
        _allDrivers.value = _allDrivers.value.map {
            if (it.id == driverId) it.copy(isSuspended = true) else it
        }
        addNotification("Account Suspended", "Your driver account has been suspended by platform admin.", "DRIVER", driverId)
    }

    fun reactivateDriver(driverId: String) {
        _allDrivers.value = _allDrivers.value.map {
            if (it.id == driverId) it.copy(isSuspended = false) else it
        }
        addNotification("Account Reactivated", "Your driver account is now active.", "DRIVER", driverId)
    }

    fun deleteDriver(driverId: String) {
        _allDrivers.value = _allDrivers.value.filterNot { it.id == driverId }
    }

    fun blockPassengerAccount(passengerId: String) {
        _allPassengers.value = _allPassengers.value.map {
            if (it.id == passengerId) it.copy(isBlocked = true) else it
        }
    }

    fun resolveComplaint(complaintId: String, action: String) {
        val complaint = _allComplaints.value.find { it.id == complaintId } ?: return
        val updatedStatus = when (action) {
            "WARN" -> "WARNED"
            "SUSPEND" -> "SUSPENDED"
            else -> "RESOLVED"
        }
        _allComplaints.value = _allComplaints.value.map {
            if (it.id == complaintId) it.copy(status = updatedStatus) else it
        }

        if (action == "SUSPEND") {
            suspendDriver(complaint.driverId)
        } else if (action == "WARN") {
            addNotification("Warning Notice", "Administrative warning regarding ride complaint: ${complaint.category}", "DRIVER", complaint.driverId)
        }
    }

    fun renewSubscription(driverId: String) {
        _allSubscriptions.value = _allSubscriptions.value.map {
            if (it.driverId == driverId) it.copy(status = "ACTIVE", expiryDate = "2027-12-31") else it
        }
        val map = _allDriverProfiles.value.toMutableMap()
        val prof = map[driverId]
        if (prof != null) {
            map[driverId] = prof.copy(subscriptionStatus = "Active", subscriptionExpiryDate = "2027-12-31")
            _allDriverProfiles.value = map
        }
    }

    fun broadcastNotification(title: String, message: String, roleTarget: String) {
        addNotification(title, message, roleTarget)
    }

    fun approveRegistration(userId: String) {
        _allDrivers.value = _allDrivers.value.map {
            if (it.id == userId) it.copy(registrationStatus = "APPROVED") else it
        }
        _allPassengers.value = _allPassengers.value.map {
            if (it.id == userId) it.copy(registrationStatus = "APPROVED") else it
        }
        addNotification("Registration Approved", "Your account registration has been reviewed and approved by Admin!", "ALL", userId)
    }

    fun declineRegistration(userId: String) {
        _allDrivers.value = _allDrivers.value.map {
            if (it.id == userId) it.copy(registrationStatus = "DECLINED") else it
        }
        _allPassengers.value = _allPassengers.value.map {
            if (it.id == userId) it.copy(registrationStatus = "DECLINED") else it
        }
        addNotification("Registration Status Update", "Your account registration was declined by Admin.", "ALL", userId)
    }

    private fun addNotification(title: String, message: String, recipientRole: String, recipientId: String? = null) {
        val notif = AppNotification(
            id = "notif_" + UUID.randomUUID().toString().take(6),
            title = title,
            message = message,
            recipientRole = recipientRole,
            recipientId = recipientId
        )
        _notifications.value = listOf(notif) + _notifications.value
    }

    private fun checkAndSyncActiveRide(userId: String) {
        val ride = _allRides.value.find {
            (it.passengerId == userId || it.driverId == userId) &&
                    it.status != RideStatus.TRIP_COMPLETED &&
                    it.status != RideStatus.CANCELLED
        }
        _activeRide.value = ride
    }

    private fun startDriverLocationSimulation() {
        scope.launch {
            while (true) {
                delay(3000)
                val current = _activeRide.value
                if (current != null && (current.status == RideStatus.ACCEPTED || current.status == RideStatus.DRIVER_ON_THE_WAY || current.status == RideStatus.TRIP_STARTED)) {
                    val targetLat = if (current.status == RideStatus.TRIP_STARTED) current.dropoffLat else current.pickupLat
                    val targetLng = if (current.status == RideStatus.TRIP_STARTED) current.dropoffLng else current.pickupLng

                    val dLat = (targetLat - current.driverLat) * 0.15
                    val dLng = (targetLng - current.driverLng) * 0.15

                    val newLat = current.driverLat + dLat
                    val newLng = current.driverLng + dLng

                    val autoStatus = if (Math.abs(targetLat - newLat) < 0.001 && Math.abs(targetLng - newLng) < 0.001) {
                        if (current.status != RideStatus.TRIP_STARTED) RideStatus.DRIVER_ARRIVED else current.status
                    } else current.status

                    val updated = current.copy(driverLat = newLat, driverLng = newLng, status = autoStatus)
                    _activeRide.value = updated
                    _allRides.value = _allRides.value.map { if (it.id == current.id) updated else it }
                }
            }
        }
    }

    fun updateDriverLocation(lat: Double, lng: Double) {
        val current = _activeRide.value
        val driverId = current?.driverId ?: _currentUser.value?.id ?: "driver_1"
        val driverName = current?.driverName ?: _currentUser.value?.name ?: "Driver"
        
        if (current != null) {
            val updated = current.copy(driverLat = lat, driverLng = lng)
            _activeRide.value = updated
            _allRides.value = _allRides.value.map { if (it.id == current.id) updated else it }
        }

        // Sync real-time location to Firebase Firestore
        FirestoreLocationRepository.updateDriverLocation(
            driverId = driverId,
            driverName = driverName,
            latitude = lat,
            longitude = lng,
            rideId = current?.id
        )
    }
}
