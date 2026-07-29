package com.example.ui.screens.passenger

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.example.R
import com.example.data.SoftKeeperRepository
import com.example.model.Ride
import com.example.model.RideStatus
import com.example.ui.components.GoogleMapView
import com.example.ui.components.LocationPermissionHandler
import com.example.ui.theme.DarkBlueBackground
import com.example.ui.theme.DarkBluePrimary
import com.example.ui.theme.EmeraldAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerHomeScreen(
    onLogout: () -> Unit
) {
    val currentUser by SoftKeeperRepository.currentUser.collectAsState()
    val activeRide by SoftKeeperRepository.activeRide.collectAsState()
    val allRides by SoftKeeperRepository.allRides.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Request, 1: My Trips, 2: Complaints, 3: Profile

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_soft_keeper_logo_1784959811892),
                            contentDescription = "Logo",
                            modifier = Modifier.size(32.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Soft Keeper", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Safe. Simple. Reliable.", color = EmeraldAccent, fontSize = 11.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBluePrimary),
                actions = {
                    IconButton(onClick = { selectedTab = 3 }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkBluePrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("passenger_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                    label = { Text("Request") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBluePrimary,
                        selectedTextColor = EmeraldAccent,
                        indicatorColor = EmeraldAccent,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("My Trips") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBluePrimary,
                        selectedTextColor = EmeraldAccent,
                        indicatorColor = EmeraldAccent,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.ReportProblem, contentDescription = null) },
                    label = { Text("Complaints") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBluePrimary,
                        selectedTextColor = EmeraldAccent,
                        indicatorColor = EmeraldAccent,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBluePrimary,
                        selectedTextColor = EmeraldAccent,
                        indicatorColor = EmeraldAccent,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )
            }
        },
        containerColor = DarkBlueBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LocationPermissionHandler()
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> RequestTransportTab(activeRide)
                        1 -> MyTripsTab(allRides.filter { it.passengerId == currentUser?.id })
                        2 -> ComplaintsTab(allRides.filter { it.passengerId == currentUser?.id })
                        3 -> PassengerProfileTab(onLogout = onLogout)
                    }
                }
            }
        }
    }
}

@Composable
fun RequestTransportTab(activeRide: Ride?) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val currentUser by SoftKeeperRepository.currentUser.collectAsState()
    var pickup by remember { mutableStateOf("Sandton City Mall, Johannesburg") }
    var dropoff by remember { mutableStateOf("OR Tambo Airport, Kempton Park") }
    var estimatedFare by remember { mutableDoubleStateOf(28.50) }
    var seatsNeeded by remember { mutableIntStateOf(1) }
    var ratingDialogOpen by remember { mutableStateOf(false) }
    var userRating by remember { mutableFloatStateOf(5.0f) }
    var showTopUpDialog by remember { mutableStateOf(false) }
    var locationWarningDismissed by remember { mutableStateOf(false) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = isGranted
        if (isGranted) {
            SoftKeeperRepository.toggleLocationPermission(true)
            fetchRealtimeGpsLocation(fusedLocationClient) { lat, lng, addr ->
                pickup = addr
                SoftKeeperRepository.updateUserLocationCoordinates(lat, lng)
            }
        } else {
            SoftKeeperRepository.toggleLocationPermission(false)
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && currentUser?.isLocationEnabled == true) {
            fetchRealtimeGpsLocation(fusedLocationClient) { lat, lng, addr ->
                pickup = addr
                SoftKeeperRepository.updateUserLocationCoordinates(lat, lng)
            }
        }
    }

    LaunchedEffect(activeRide?.status) {
        if (activeRide?.status == RideStatus.TRIP_COMPLETED && activeRide?.rating == null) {
            ratingDialogOpen = true
        }
    }

    if (activeRide != null && activeRide.status != RideStatus.TRIP_COMPLETED && activeRide.status != RideStatus.CANCELLED) {
        // Active Live Ride View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    when (activeRide.status) {
                                        RideStatus.DRIVER_OFFERED_FARE -> Color(0xFFF59E0B)
                                        RideStatus.ACCEPTED -> Color(0xFF38BDF8)
                                        else -> EmeraldAccent
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (activeRide.status) {
                                RideStatus.WAITING -> "Searching for Drivers..."
                                RideStatus.DRIVER_OFFERED_FARE -> "Driver Offer Received"
                                RideStatus.ACCEPTED -> "Fare Approved - Awaiting Driver"
                                RideStatus.DRIVER_ON_THE_WAY -> "Driver On The Way!"
                                RideStatus.DRIVER_ARRIVED -> "Driver Arrived at Pickup!"
                                RideStatus.TRIP_STARTED -> "Trip in Progress"
                                else -> "Active Trip"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    if (activeRide.status == RideStatus.DRIVER_OFFERED_FARE) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = Color(0xFF312E81),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFFFDE047))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Driver ${activeRide.driverName} Offered a Fare!", color = Color(0xFFFDE047), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Text("${activeRide.vehicleInfo} • ${activeRide.vehicleRegistration}", color = Color(0xFFCBD5E1), fontSize = 12.sp)

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Offered Fare:", color = Color.White, fontSize = 13.sp)
                                    Text("R ${"%.2f".format(activeRide.offeredFare ?: activeRide.fare)}", color = EmeraldAccent, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { SoftKeeperRepository.passengerApproveFareOffer(activeRide.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).testTag("approve_fare_button")
                                    ) {
                                        Text("✅ Accept Offer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { SoftKeeperRepository.passengerRejectFareOffer(activeRide.id) },
                                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).testTag("pass_fare_button")
                                    ) {
                                        Text("❌ Pass", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else if (activeRide.driverName != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.img_driver_avatar_1784959837433),
                                contentDescription = "Driver Avatar",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(activeRide.driverName ?: "Assigned Driver", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("${activeRide.vehicleInfo} (${activeRide.vehicleRegistration})", color = Color(0xFF94A3B8), fontSize = 13.sp)
                                Text("Tel: ${activeRide.driverPhone}", color = EmeraldAccent, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Searching for nearby drivers...", color = Color(0xFF94A3B8), fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Live Map View
            GoogleMapView(
                pickupAddress = activeRide.pickupAddress,
                dropoffAddress = activeRide.dropoffAddress,
                pickupLat = activeRide.pickupLat,
                pickupLng = activeRide.pickupLng,
                dropoffLat = activeRide.dropoffLat,
                dropoffLng = activeRide.dropoffLng,
                driverLat = activeRide.driverLat,
                driverLng = activeRide.driverLng,
                showDriver = activeRide.driverId != null,
                isTripStarted = activeRide.status == RideStatus.TRIP_STARTED,
                driverMessage = activeRide.quickMessage,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel Ride Button
            Button(
                onClick = { SoftKeeperRepository.cancelRide(activeRide.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("cancel_ride_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel Transport Request", fontWeight = FontWeight.Bold)
            }
        }
    } else {
        // Request Transport Form
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Account Money Card
            Surface(
                color = DarkBluePrimary,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Account Money", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "R ${"%.2f".format(currentUser?.accountBalance ?: 250.0)}",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { showTopUpDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Top Up", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Location Services Status Warning Banner
            if (!hasLocationPermission || currentUser?.isLocationEnabled == false) {
                Surface(
                    color = Color(0xFF7F1D1D),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOff, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Google Play Services GPS Location: OFF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Please enable location permissions so drivers can pinpoint your exact GPS starting point on the map.",
                            color = Color(0xFFFECACA),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp).testTag("enable_location_button")
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Request GPS Location Permission", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Surface(
                    color = Color(0xFF064E3B),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, EmeraldAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("GPS Location Active", color = Color(0xFFD1FAE5), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                if (currentUser?.currentLat != null && currentUser?.currentLng != null) {
                                    Text("Lat: ${"%.4f".format(currentUser?.currentLat)}, Lng: ${"%.4f".format(currentUser?.currentLng)}", color = Color(0xFFA7F3D0), fontSize = 11.sp)
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    fetchRealtimeGpsLocation(fusedLocationClient) { lat, lng, addr ->
                                        pickup = addr
                                        SoftKeeperRepository.updateUserLocationCoordinates(lat, lng)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh GPS", tint = EmeraldAccent, modifier = Modifier.size(18.dp))
                            }
                            TextButton(onClick = { SoftKeeperRepository.toggleLocationPermission(false) }) {
                                Text("Turn Off", color = Color(0xFF9CA3AF), fontSize = 11.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text("Request Transport", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Your starting point is auto-detected via GPS. Input your destination point below:", color = Color(0xFF94A3B8), fontSize = 12.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Starting Point (Auto Detected)
                    OutlinedTextField(
                        value = pickup,
                        onValueChange = { pickup = it },
                        label = { Text("Starting Point (Pickup Location)") },
                        leadingIcon = { Icon(Icons.Default.MyLocation, contentDescription = null, tint = EmeraldAccent) },
                        trailingIcon = {
                            Surface(
                                color = EmeraldAccent.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clip(RoundedCornerShape(6.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.GpsFixed, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("GPS AUTO", color = EmeraldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("pickup_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Destination Point
                    OutlinedTextField(
                        value = dropoff,
                        onValueChange = { dropoff = it },
                        label = { Text("Destination Point (Where are you heading?)") },
                        placeholder = { Text("e.g. OR Tambo Airport, Rosebank Mall, Soweto") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444)) },
                        modifier = Modifier.fillMaxWidth().testTag("dropoff_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Seats Required:", color = Color.White, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (seatsNeeded > 1) seatsNeeded-- }) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = EmeraldAccent)
                            }
                            Text("$seatsNeeded", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            IconButton(onClick = { if (seatsNeeded < 4) seatsNeeded++ }) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = EmeraldAccent)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estimated Fare:", color = Color(0xFF94A3B8), fontSize = 14.sp)
                        Text("R ${"%.2f".format(estimatedFare * seatsNeeded)}", color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Map Preview
            GoogleMapView(
                pickupAddress = pickup,
                dropoffAddress = dropoff,
                showDriver = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (currentUser?.isLocationEnabled == false) {
                        // Automatically enable location when requesting so drivers can find passenger
                        SoftKeeperRepository.toggleLocationPermission()
                    }
                    SoftKeeperRepository.requestTransport(
                        pickupAddress = pickup,
                        dropoffAddress = dropoff,
                        pickupLat = -26.1076,
                        pickupLng = 28.0567,
                        dropoffLat = -26.1367,
                        dropoffLng = 28.2411,
                        fare = estimatedFare * seatsNeeded
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("request_ride_now_button"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Transport Request", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }

    if (showTopUpDialog) {
        val topUpInstructions by SoftKeeperRepository.passengerTopUpInstructions.collectAsState()

        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            containerColor = Color(0xFF1E293B),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCBD5E1),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = EmeraldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Top Up Account Money")
                }
            },
            text = {
                Column {
                    // Admin Official Payment Instructions Banner
                    Surface(
                        color = DarkBluePrimary,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("👑", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Official Admin Top-Up Instructions:", color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(topUpInstructions, color = Color.White, fontSize = 12.sp, lineHeight = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Select an amount to instantly add to your wallet:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                SoftKeeperRepository.topUpAccountBalance(50.0)
                                showTopUpDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBluePrimary, contentColor = EmeraldAccent),
                            border = BorderStroke(1.dp, EmeraldAccent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("R 50", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                SoftKeeperRepository.topUpAccountBalance(100.0)
                                showTopUpDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBluePrimary, contentColor = EmeraldAccent),
                            border = BorderStroke(1.dp, EmeraldAccent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("R 100", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                SoftKeeperRepository.topUpAccountBalance(250.0)
                                showTopUpDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("R 250", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTopUpDialog = false }) {
                    Text("Close", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    if (ratingDialogOpen && activeRide != null) {
        AlertDialog(
            onDismissRequest = { ratingDialogOpen = false },
            title = { Text("Rate Your Driver", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("How was your trip with ${activeRide.driverName}?", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        (1..5).forEach { star ->
                            IconButton(onClick = { userRating = star.toFloat() }) {
                                Icon(
                                    imageVector = if (star <= userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star $star",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        SoftKeeperRepository.rateDriver(activeRide.id, userRating)
                        ratingDialogOpen = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary)
                ) {
                    Text("Submit Rating", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun MyTripsTab(passengerRides: List<Ride>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("My Ride History", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("View past transport requests and receipts", color = Color(0xFF94A3B8), fontSize = 13.sp)

        Spacer(modifier = Modifier.height(16.dp))

        if (passengerRides.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No past rides found.", color = Color(0xFF94A3B8))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(passengerRides) { ride ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ride #${ride.id.takeLast(6)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "$${"%.2f".format(ride.fare)}",
                                    color = EmeraldAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(ride.pickupAddress, color = Color(0xFFE2E8F0), fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(ride.dropoffAddress, color = Color(0xFFE2E8F0), fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Driver: ${ride.driverName ?: "N/A"}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                Surface(
                                    color = if (ride.status == RideStatus.TRIP_COMPLETED) EmeraldAccent.copy(alpha = 0.2f) else Color(0xFF334155),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = ride.status.name,
                                        color = if (ride.status == RideStatus.TRIP_COMPLETED) EmeraldAccent else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComplaintsTab(passengerRides: List<Ride>) {
    var selectedRideId by remember { mutableStateOf(passengerRides.firstOrNull()?.id ?: "") }
    var category by remember { mutableStateOf("Unsafe driving") }
    var details by remember { mutableStateOf("") }
    var submittedAlert by remember { mutableStateOf(false) }

    val categories = listOf("Unsafe driving", "Late arrival", "Wrong behaviour", "Vehicle issue", "Other")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Submit Complaint", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Report issues directly to Soft Keeper admin", color = Color(0xFF94A3B8), fontSize = 13.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Category:", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                categories.forEach { cat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = category == cat,
                            onClick = { category = cat },
                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldAccent)
                        )
                        Text(cat, color = Color.White, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Complaint Details") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("complaint_details_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (details.isNotBlank() && selectedRideId.isNotBlank()) {
                            SoftKeeperRepository.submitComplaint(selectedRideId, category, details)
                            submittedAlert = true
                            details = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_complaint_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit to Admin", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (submittedAlert) {
        AlertDialog(
            onDismissRequest = { submittedAlert = false },
            title = { Text("Complaint Submitted") },
            text = { Text("Platform admin has received your complaint and will review it immediately.") },
            confirmButton = {
                Button(onClick = { submittedAlert = false }) { Text("OK") }
            }
        )
    }
}

@Composable
fun PassengerProfileTab(onLogout: () -> Unit) {
    val user by SoftKeeperRepository.currentUser.collectAsState()
    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var savedMsg by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Profile & Settings", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Manage your account details & Crown verification", color = Color(0xFF94A3B8), fontSize = 13.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Crown Verification Status Banner
        if (user?.isCrownVerified == true) {
            Surface(
                color = Color(0xFF1E1B4B),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👑", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("CROWN VERIFIED", color = Color(0xFFFDE047), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = Color(0xFFF59E0B).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                Text("Face-to-Face", color = Color(0xFFFDE047), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Met Admin in person. Full network trust active.", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    }
                }
            }
        } else {
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF475569)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👻", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("GHOST STATUS (Unverified)", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("You are an unverified ghost. Contact Admin to meet face-to-face and claim your Crown 👑.", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.img_soft_keeper_logo_1784959811892),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(80.dp).clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        SoftKeeperRepository.updateProfile(name, email, phone)
                        savedMsg = true
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("logout_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", fontWeight = FontWeight.Bold)
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchRealtimeGpsLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationFetched: (Double, Double, String) -> Unit
) {
    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                val addr = "GPS Location (Lat: ${"%.4f".format(loc.latitude)}, Lng: ${"%.4f".format(loc.longitude)})"
                onLocationFetched(loc.latitude, loc.longitude, addr)
            } else {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { currLoc ->
                        if (currLoc != null) {
                            val addr = "GPS Location (Lat: ${"%.4f".format(currLoc.latitude)}, Lng: ${"%.4f".format(currLoc.longitude)})"
                            onLocationFetched(currLoc.latitude, currLoc.longitude, addr)
                        } else {
                            val defaultLat = -26.1076
                            val defaultLng = 28.0567
                            onLocationFetched(defaultLat, defaultLng, "GPS Location (Lat: -26.1076, Lng: 28.0567)")
                        }
                    }
                    .addOnFailureListener {
                        onLocationFetched(-26.1076, 28.0567, "GPS Location (Lat: -26.1076, Lng: 28.0567)")
                    }
            }
        }.addOnFailureListener {
            onLocationFetched(-26.1076, 28.0567, "GPS Location (Lat: -26.1076, Lng: 28.0567)")
        }
    } catch (e: Exception) {
        onLocationFetched(-26.1076, 28.0567, "GPS Location (Lat: -26.1076, Lng: 28.0567)")
    }
}
