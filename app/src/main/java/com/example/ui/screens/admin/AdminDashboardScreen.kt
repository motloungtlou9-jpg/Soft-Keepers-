package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DriverLocationData
import com.example.data.FirestoreLocationRepository
import com.example.data.SoftKeeperRepository
import com.example.model.*
import com.example.ui.components.GoogleMapView
import com.example.ui.theme.DarkBlueBackground
import com.example.ui.theme.DarkBluePrimary
import com.example.ui.theme.EmeraldAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit
) {
    val allDrivers by SoftKeeperRepository.allDrivers.collectAsState()
    val allPassengers by SoftKeeperRepository.allPassengers.collectAsState()
    val allComplaints by SoftKeeperRepository.allComplaints.collectAsState()
    val allSubscriptions by SoftKeeperRepository.allSubscriptions.collectAsState()
    val allRides by SoftKeeperRepository.allRides.collectAsState()
    val notifications by SoftKeeperRepository.notifications.collectAsState()

    val pendingCount = (allDrivers + allPassengers).count { it.registrationStatus == "PENDING" }

    var selectedTab by remember { mutableIntStateOf(0) }
    // 0: Overview, 1: Live Ops (Firestore), 2: Approvals, 3: Drivers, 4: Passengers, 5: Broadcast, 6: Payments & Complaints

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
                            Text("Soft Keeper Admin", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Master Authority Console", color = EmeraldAccent, fontSize = 11.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBluePrimary),
                actions = {
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("admin_logout_btn")) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkBluePrimary,
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) }, label = { Text("Overview") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = DarkBluePrimary, selectedTextColor = EmeraldAccent, indicatorColor = EmeraldAccent)
                )
                NavigationBarItem(
                    selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.GpsFixed, contentDescription = null) }, label = { Text("Live Ops") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = DarkBluePrimary, selectedTextColor = EmeraldAccent, indicatorColor = EmeraldAccent)
                )
                NavigationBarItem(
                    selected = selectedTab == 2, onClick = { selectedTab = 2 },
                    icon = {
                        BadgedBox(badge = {
                            if (pendingCount > 0) {
                                Badge(containerColor = Color(0xFFF59E0B)) { Text("$pendingCount") }
                            }
                        }) {
                            Icon(Icons.Default.PendingActions, contentDescription = null)
                        }
                    },
                    label = { Text("Approvals") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = DarkBluePrimary, selectedTextColor = EmeraldAccent, indicatorColor = EmeraldAccent)
                )
                NavigationBarItem(
                    selected = selectedTab == 3, onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) }, label = { Text("Drivers") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = DarkBluePrimary, selectedTextColor = EmeraldAccent, indicatorColor = EmeraldAccent)
                )
                NavigationBarItem(
                    selected = selectedTab == 4, onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.People, contentDescription = null) }, label = { Text("Passengers") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = DarkBluePrimary, selectedTextColor = EmeraldAccent, indicatorColor = EmeraldAccent)
                )
                NavigationBarItem(
                    selected = selectedTab == 5, onClick = { selectedTab = 5 },
                    icon = { Icon(Icons.Default.Campaign, contentDescription = null) }, label = { Text("Broadcast") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = DarkBluePrimary, selectedTextColor = EmeraldAccent, indicatorColor = EmeraldAccent)
                )
                NavigationBarItem(
                    selected = selectedTab == 6, onClick = { selectedTab = 6 },
                    icon = { Icon(Icons.Default.Payments, contentDescription = null) }, label = { Text("Payments") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = DarkBluePrimary, selectedTextColor = EmeraldAccent, indicatorColor = EmeraldAccent)
                )
            }
        },
        containerColor = DarkBlueBackground
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                0 -> AdminOverviewTab(allDrivers, allPassengers, allComplaints, allRides.size, allRides.sumOf { it.fare })
                1 -> AdminLiveOpsTab(allRides)
                2 -> AdminApprovalsTab(allDrivers, allPassengers, notifications)
                3 -> AdminDriversTab(allDrivers)
                4 -> AdminPassengersTab(allPassengers)
                5 -> AdminBroadcastTab()
                6 -> AdminPaymentsAndComplaintsTab(allComplaints, allSubscriptions)
            }
        }
    }
}

@Composable
fun AdminOverviewTab(
    drivers: List<User>,
    passengers: List<User>,
    complaints: List<Complaint>,
    totalRides: Int,
    totalRevenue: Double
) {
    val crownedDrivers = drivers.count { it.isCrownVerified }
    val ghostDrivers = drivers.count { !it.isCrownVerified }
    val crownedPassengers = passengers.count { it.isCrownVerified }
    val ghostPassengers = passengers.count { !it.isCrownVerified }
    val totalCrowned = crownedDrivers + crownedPassengers
    val totalGhosts = ghostDrivers + ghostPassengers

    val pendingDrivers = drivers.count { it.registrationStatus == "PENDING" }
    val pendingPassengers = passengers.count { it.registrationStatus == "PENDING" }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Admin Authority Console", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Master authority & face-to-face identity system", color = Color(0xFF94A3B8), fontSize = 13.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Admin Authority Crown Banner
        Surface(
            color = Color(0xFF1E1B4B),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👑", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Admin Crown Authority", color = Color(0xFFFDE047), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Only Admin holds real contacts & face-to-face verification", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Without your Crown 👑, users and drivers remain Ghosts 👻 on the network. You alone verify identities face-to-face.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("👑 Crowned (Verified)", "$totalCrowned", Color(0xFFF59E0B), Modifier.weight(1f))
            StatCard("👻 Ghosts (Unverified)", "$totalGhosts", Color(0xFF94A3B8), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Pending Driver Regs", "$pendingDrivers", Color(0xFFFDE047), Modifier.weight(1f))
            StatCard("Pending Pass Regs", "$pendingPassengers", Color(0xFF38BDF8), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Registered Drivers", "${drivers.size}", EmeraldAccent, Modifier.weight(1f))
            StatCard("Passengers", "${passengers.size}", Color(0xFF3B82F6), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Total Trips", "$totalRides", Color(0xFFEC4899), Modifier.weight(1f))
            StatCard("Revenue", "R${"%.2f".format(totalRevenue)}", EmeraldAccent, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(colors = CardDefaults.cardColors(containerColor = DarkBluePrimary), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Pending Complaints", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${complaints.count { it.status == "PENDING" }} unresolved complaint reports requiring review", color = Color(0xFF94A3B8), fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = DarkBluePrimary), shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = Color(0xFF94A3B8), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = accentColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminLiveOpsTab(allRides: List<Ride>) {
    val activeDriversFromFirestore by remember {
        FirestoreLocationRepository.observeAllActiveDrivers()
    }.collectAsState(initial = emptyList())

    val activeRides = allRides.filter {
        it.status != RideStatus.TRIP_COMPLETED && it.status != RideStatus.CANCELLED
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Firestore Real-Time Live Operations", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Live active driver GPS locations & transport requests synced via Firebase", color = Color(0xFF94A3B8), fontSize = 13.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Section 1: Active Drivers from Firestore
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GpsFixed, contentDescription = null, tint = EmeraldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Active Drivers (Firestore)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Surface(
                        color = EmeraldAccent.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "${activeDriversFromFirestore.size} Transmitting",
                            color = EmeraldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (activeDriversFromFirestore.isEmpty()) {
                    Text("No drivers currently transmitting GPS to Firestore.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        activeDriversFromFirestore.forEach { driver ->
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(driver.driverName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("ID: ${driver.driverId}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        Text("Lat: ${"%.5f".format(driver.latitude)} | Lng: ${"%.5f".format(driver.longitude)}", color = EmeraldAccent, fontSize = 11.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Surface(
                                            color = if (driver.isOnline) EmeraldAccent.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                if (driver.isOnline) "ONLINE" else "OFFLINE",
                                                color = if (driver.isOnline) EmeraldAccent else Color.Red,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (!driver.rideId.isNullOrEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Ride: ${driver.rideId}", color = Color(0xFF38BDF8), fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Current Transport Requests
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Current Transport Requests", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "${activeRides.size} Active Requests",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (activeRides.isEmpty()) {
                    Text("No current active transport requests.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        activeRides.forEach { ride ->
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Ride ID: ${ride.id}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Surface(
                                            color = when (ride.status) {
                                                RideStatus.WAITING -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                                RideStatus.ACCEPTED, RideStatus.DRIVER_ON_THE_WAY, RideStatus.TRIP_STARTED -> EmeraldAccent.copy(alpha = 0.2f)
                                                else -> Color(0xFF64748B).copy(alpha = 0.2f)
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                ride.status.name,
                                                color = when (ride.status) {
                                                    RideStatus.WAITING -> Color(0xFFF59E0B)
                                                    RideStatus.ACCEPTED, RideStatus.DRIVER_ON_THE_WAY, RideStatus.TRIP_STARTED -> EmeraldAccent
                                                    else -> Color(0xFF94A3B8)
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Passenger: ${ride.passengerName} (${ride.passengerPhone})", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                                    Text("Driver: ${ride.driverName ?: "Unassigned"}", color = EmeraldAccent, fontSize = 12.sp)
                                    Text("Pickup: ${ride.pickupAddress}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                    Text("Dropoff: ${ride.dropoffAddress}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                    Text("Fare: R${"%.2f".format(ride.fare)}", color = Color(0xFFFDE047), fontWeight = FontWeight.Bold, fontSize = 13.sp)

                                    Spacer(modifier = Modifier.height(10.dp))
                                    GoogleMapView(
                                        pickupAddress = ride.pickupAddress,
                                        dropoffAddress = ride.dropoffAddress,
                                        driverLat = ride.driverLat,
                                        driverLng = ride.driverLng,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(8.dp))
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
fun AdminApprovalsTab(
    drivers: List<User>,
    passengers: List<User>,
    notifications: List<AppNotification>
) {
    val pendingDrivers = drivers.filter { it.registrationStatus == "PENDING" }
    val pendingPassengers = passengers.filter { it.registrationStatus == "PENDING" }
    val totalPending = pendingDrivers.size + pendingPassengers.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Registration Approvals & Notifications", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Review new registration applications, approve or decline users", color = Color(0xFF94A3B8), fontSize = 13.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Pending Counter Banner
        Surface(
            color = if (totalPending > 0) Color(0xFF78350F) else Color(0xFF064E3B),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, if (totalPending > 0) Color(0xFFF59E0B) else EmeraldAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (totalPending > 0) Icons.Default.PendingActions else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (totalPending > 0) Color(0xFFFDE047) else EmeraldAccent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (totalPending > 0) "$totalPending Registration Applications Pending" else "All Registrations Up To Date",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (totalPending > 0) "Immediate admin action required to approve or decline" else "No pending registration reviews required",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Pending Driver Applications
        if (pendingDrivers.isNotEmpty()) {
            Text("Pending Driver Registrations (${pendingDrivers.size})", color = EmeraldAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            pendingDrivers.forEach { driver ->
                PendingUserCard(user = driver, isDriver = true)
                Spacer(modifier = Modifier.height(10.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section: Pending Passenger Applications
        if (pendingPassengers.isNotEmpty()) {
            Text("Pending Passenger Registrations (${pendingPassengers.size})", color = Color(0xFF38BDF8), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            pendingPassengers.forEach { pass ->
                PendingUserCard(user = pass, isDriver = false)
                Spacer(modifier = Modifier.height(10.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text("All Registered Drivers & Passengers Status", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Quickly review or update approval status for any account:", color = Color(0xFF94A3B8), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(10.dp))

        (drivers + passengers).forEach { user ->
            UserApprovalListItem(user = user)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Registration Notifications History
        Text("Registration Notifications Log", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        val regNotifs = notifications.filter { it.recipientRole == "ADMIN" || it.title.contains("Registration", ignoreCase = true) }
        if (regNotifs.isEmpty()) {
            Text("No registration notification logs.", color = Color(0xFF94A3B8), fontSize = 13.sp)
        } else {
            regNotifs.forEach { notif ->
                Surface(
                    color = DarkBluePrimary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(notif.title, color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(notif.message, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PendingUserCard(user: User, isDriver: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDriver) Icons.Default.DirectionsCar else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isDriver) EmeraldAccent else Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(user.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(user.email, color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
                Surface(
                    color = Color(0xFF78350F),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("PENDING", color = Color(0xFFFDE047), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Phone: ${user.phone}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
            Text("Role: ${if (isDriver) "Driver" else "Passenger"}", color = EmeraldAccent, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { SoftKeeperRepository.approveRegistration(user.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary),
                    modifier = Modifier.weight(1f).testTag("approve_user_${user.id}")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { SoftKeeperRepository.declineRegistration(user.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.weight(1f).testTag("decline_user_${user.id}")
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Decline", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun UserApprovalListItem(user: User) {
    Surface(
        color = DarkBluePrimary,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${user.email} | Status: ${user.registrationStatus}", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (user.registrationStatus != "APPROVED") {
                    Button(
                        onClick = { SoftKeeperRepository.approveRegistration(user.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) { Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
                if (user.registrationStatus != "DECLINED") {
                    Button(
                        onClick = { SoftKeeperRepository.declineRegistration(user.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) { Text("Decline", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun AdminBroadcastTab() {
    var subjectInput by remember { mutableStateOf("") }
    var bodyInput by remember { mutableStateOf("") }
    var targetRole by remember { mutableStateOf("ALL") } // ALL, DRIVER, PASSENGER
    var broadcastSuccessNotice by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Broadcast Announcement Console", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Message everyone or send targeted notifications to drivers and passengers", color = Color(0xFF94A3B8), fontSize = 13.sp)

        Spacer(modifier = Modifier.height(16.dp))

        if (broadcastSuccessNotice != null) {
            Surface(
                color = Color(0xFF064E3B),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, EmeraldAccent),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(broadcastSuccessNotice!!, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Compose Message to Everyone", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))

                // Target Selector Chips
                Text("Recipient Group:", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = targetRole == "ALL",
                        onClick = { targetRole = "ALL" },
                        label = { Text("📢 Everyone (All Users)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldAccent, selectedLabelColor = DarkBluePrimary, labelColor = Color.White)
                    )
                    FilterChip(
                        selected = targetRole == "DRIVER",
                        onClick = { targetRole = "DRIVER" },
                        label = { Text("🚗 Drivers Only", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF38BDF8), selectedLabelColor = DarkBluePrimary, labelColor = Color.White)
                    )
                    FilterChip(
                        selected = targetRole == "PASSENGER",
                        onClick = { targetRole = "PASSENGER" },
                        label = { Text("👤 Passengers Only", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFF59E0B), selectedLabelColor = DarkBluePrimary, labelColor = Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = subjectInput,
                    onValueChange = { subjectInput = it },
                    label = { Text("Broadcast Subject / Title") },
                    placeholder = { Text("e.g. System Announcement / Holiday Promotion") },
                    modifier = Modifier.fillMaxWidth().testTag("broadcast_subject_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bodyInput,
                    onValueChange = { bodyInput = it },
                    label = { Text("Message Body") },
                    placeholder = { Text("Type broadcast message content here to deliver to everyone...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp).testTag("broadcast_body_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (subjectInput.isNotBlank() && bodyInput.isNotBlank()) {
                            SoftKeeperRepository.broadcastNotification(subjectInput, bodyInput, targetRole)
                            broadcastSuccessNotice = "Message broadcasted successfully to target group ($targetRole)!"
                            subjectInput = ""
                            bodyInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("send_broadcast_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = subjectInput.isNotBlank() && bodyInput.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Broadcast Message to Everyone", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun AdminDriversTab(drivers: List<User>) {
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") } // ALL, CROWNED, GHOSTS, PENDING
    var editingUser by remember { mutableStateOf<User?>(null) }

    val filtered = drivers.filter { driver ->
        val matchesSearch = driver.name.contains(searchQuery, ignoreCase = true) ||
                driver.email.contains(searchQuery, ignoreCase = true) ||
                (driver.realContactDetails?.contains(searchQuery, ignoreCase = true) == true)

        val matchesFilter = when (filterType) {
            "CROWNED" -> driver.isCrownVerified
            "GHOSTS" -> !driver.isCrownVerified
            "PENDING" -> driver.registrationStatus == "PENDING"
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Registered Drivers Directory", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Text("View driver details, crown verification, and master records", color = Color(0xFF94A3B8), fontSize = 12.sp)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search driver name, email or contact...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldAccent) },
            modifier = Modifier.fillMaxWidth().testTag("search_driver_input"),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = filterType == "ALL",
                onClick = { filterType = "ALL" },
                label = { Text("All (${drivers.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldAccent, selectedLabelColor = DarkBluePrimary, labelColor = Color.White)
            )
            FilterChip(
                selected = filterType == "CROWNED",
                onClick = { filterType = "CROWNED" },
                label = { Text("👑 Crowned (${drivers.count { it.isCrownVerified }})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFF59E0B), selectedLabelColor = DarkBluePrimary, labelColor = Color.White)
            )
            FilterChip(
                selected = filterType == "GHOSTS",
                onClick = { filterType = "GHOSTS" },
                label = { Text("👻 Ghosts (${drivers.count { !it.isCrownVerified }})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF64748B), selectedLabelColor = Color.White, labelColor = Color.White)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filtered) { driver ->
                var showRealDetails by remember { mutableStateOf(false) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    border = if (driver.isCrownVerified) BorderStroke(1.dp, Color(0xFFF59E0B)) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(driver.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (driver.isCrownVerified) {
                                        Surface(color = Color(0xFFF59E0B).copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text("👑", fontSize = 12.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("CROWNED", color = Color(0xFFFDE047), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Surface(color = Color(0xFF64748B).copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text("👻", fontSize = 12.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("GHOST", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                Text(driver.email, color = Color(0xFF94A3B8), fontSize = 13.sp)
                                Text("App Phone: ${driver.phone}", color = EmeraldAccent, fontSize = 12.sp)
                                Text("Reg Status: ${driver.registrationStatus}", color = Color(0xFF38BDF8), fontSize = 11.sp)
                            }

                            Surface(
                                color = if (driver.isSuspended) Color(0xFFEF4444).copy(alpha = 0.2f) else EmeraldAccent.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (driver.isSuspended) "SUSPENDED" else "ACTIVE",
                                    color = if (driver.isSuspended) Color(0xFFEF4444) else EmeraldAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Admin Master Details Box
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🔒 Admin Master Record", color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    TextButton(onClick = { showRealDetails = !showRealDetails }) {
                                        Text(if (showRealDetails) "Hide" else "Reveal Contact", fontSize = 11.sp, color = Color(0xFF38BDF8))
                                    }
                                }
                                if (showRealDetails) {
                                    Text("Real Contact: ${driver.realContactDetails ?: "Not logged"}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Face-to-Face Notes: ${driver.faceToFaceNotes ?: "None"}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                                } else {
                                    Text("Hidden (Admin Master Authority Only)", color = Color(0xFF64748B), fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Crown Toggle & Admin Actions
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { SoftKeeperRepository.toggleCrownVerification(driver.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (driver.isCrownVerified) Color(0xFF334155) else Color(0xFFF59E0B),
                                    contentColor = if (driver.isCrownVerified) Color.White else DarkBluePrimary
                                ),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Text(if (driver.isCrownVerified) "👻 Revoke Crown" else "👑 Bestow Crown", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { editingUser = driver },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Edit Master", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            if (driver.isSuspended) {
                                Button(
                                    onClick = { SoftKeeperRepository.reactivateDriver(driver.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary),
                                    modifier = Modifier.weight(1f)
                                ) { Text("Reactivate", fontSize = 12.sp) }
                            } else {
                                Button(
                                    onClick = { SoftKeeperRepository.suspendDriver(driver.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    modifier = Modifier.weight(1f)
                                ) { Text("Suspend", fontSize = 12.sp) }
                            }

                            Button(
                                onClick = { SoftKeeperRepository.deleteDriver(driver.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                modifier = Modifier.weight(1f)
                            ) { Text("Delete", fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }

    if (editingUser != null) {
        MasterRecordDialog(user = editingUser!!, onDismiss = { editingUser = null })
    }
}

@Composable
fun AdminPassengersTab(passengers: List<User>) {
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") }
    var editingUser by remember { mutableStateOf<User?>(null) }

    val filtered = passengers.filter { pass ->
        val matchesSearch = pass.name.contains(searchQuery, ignoreCase = true) ||
                pass.email.contains(searchQuery, ignoreCase = true) ||
                (pass.realContactDetails?.contains(searchQuery, ignoreCase = true) == true)

        val matchesFilter = when (filterType) {
            "CROWNED" -> pass.isCrownVerified
            "GHOSTS" -> !pass.isCrownVerified
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Registered Passengers Directory", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("View passenger details, wallet balance, and crown verification", color = Color(0xFF94A3B8), fontSize = 12.sp)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search passenger name, email or contact...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldAccent) },
            modifier = Modifier.fillMaxWidth().testTag("search_passenger_input"),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = filterType == "ALL",
                onClick = { filterType = "ALL" },
                label = { Text("All (${passengers.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldAccent, selectedLabelColor = DarkBluePrimary, labelColor = Color.White)
            )
            FilterChip(
                selected = filterType == "CROWNED",
                onClick = { filterType = "CROWNED" },
                label = { Text("👑 Crowned (${passengers.count { it.isCrownVerified }})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFF59E0B), selectedLabelColor = DarkBluePrimary, labelColor = Color.White)
            )
            FilterChip(
                selected = filterType == "GHOSTS",
                onClick = { filterType = "GHOSTS" },
                label = { Text("👻 Ghosts (${passengers.count { !it.isCrownVerified }})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF64748B), selectedLabelColor = Color.White, labelColor = Color.White)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filtered) { pass ->
                var showRealDetails by remember { mutableStateOf(false) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    border = if (pass.isCrownVerified) BorderStroke(1.dp, Color(0xFFF59E0B)) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(pass.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (pass.isCrownVerified) {
                                        Surface(color = Color(0xFFF59E0B).copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text("👑", fontSize = 12.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("CROWNED", color = Color(0xFFFDE047), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Surface(color = Color(0xFF64748B).copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text("👻", fontSize = 12.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("GHOST", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                Text(pass.email, color = Color(0xFF94A3B8), fontSize = 13.sp)
                                Text("App Phone: ${pass.phone}", color = EmeraldAccent, fontSize = 12.sp)
                                Text("Wallet Balance: R${"%.2f".format(pass.accountBalance)}", color = Color(0xFFFDE047), fontSize = 12.sp)
                            }

                            if (pass.isBlocked) {
                                Surface(color = Color(0xFFEF4444).copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                    Text("BLOCKED", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Master Record Box
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🔒 Admin Master Record", color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    TextButton(onClick = { showRealDetails = !showRealDetails }) {
                                        Text(if (showRealDetails) "Hide" else "Reveal Contact", fontSize = 11.sp, color = Color(0xFF38BDF8))
                                    }
                                }
                                if (showRealDetails) {
                                    Text("Real Contact: ${pass.realContactDetails ?: "Not logged"}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Face-to-Face Notes: ${pass.faceToFaceNotes ?: "None"}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                                } else {
                                    Text("Hidden (Admin Master Authority Only)", color = Color(0xFF64748B), fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { SoftKeeperRepository.toggleCrownVerification(pass.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (pass.isCrownVerified) Color(0xFF334155) else Color(0xFFF59E0B),
                                    contentColor = if (pass.isCrownVerified) Color.White else DarkBluePrimary
                                ),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Text(if (pass.isCrownVerified) "👻 Revoke Crown" else "👑 Bestow Crown", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { editingUser = pass },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Edit Master", fontSize = 12.sp)
                            }

                            if (!pass.isBlocked) {
                                Button(
                                    onClick = { SoftKeeperRepository.blockPassengerAccount(pass.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    modifier = Modifier.weight(1f)
                                ) { Text("Block", fontSize = 12.sp) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingUser != null) {
        MasterRecordDialog(user = editingUser!!, onDismiss = { editingUser = null })
    }
}

@Composable
fun MasterRecordDialog(user: User, onDismiss: () -> Unit) {
    var realContactInput by remember { mutableStateOf(user.realContactDetails ?: "") }
    var notesInput by remember { mutableStateOf(user.faceToFaceNotes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        titleContentColor = Color.White,
        textContentColor = Color(0xFFCBD5E1),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👑", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Master Record: ${user.name}")
            }
        },
        text = {
            Column {
                Text("As sole Admin authority, log real contacts and face-to-face meeting notes:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = realContactInput,
                    onValueChange = { realContactInput = it },
                    label = { Text("Real Name & Direct Contact") },
                    placeholder = { Text("e.g. Sipho Mabena | +27 82 111 2233") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Face-to-Face Meeting Notes") },
                    placeholder = { Text("e.g. Met at Sandton station, checked ID and vehicle license") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    SoftKeeperRepository.updateUserMasterRecord(user.id, realContactInput, notesInput)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary)
            ) {
                Text("Save Master Record", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
fun AdminPaymentsAndComplaintsTab(
    complaints: List<Complaint>,
    subscriptions: List<DriverSubscription>
) {
    val currentPassengerInstructions by SoftKeeperRepository.passengerTopUpInstructions.collectAsState()
    val currentDriverInstructions by SoftKeeperRepository.driverPayoutInstructions.collectAsState()

    var passInput by remember(currentPassengerInstructions) { mutableStateOf(currentPassengerInstructions) }
    var driverInput by remember(currentDriverInstructions) { mutableStateOf(currentDriverInstructions) }
    var broadcastNotice by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Payments & Complaints Console", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Manage top-up instructions, driver subscriptions, and rider complaints", color = Color(0xFF94A3B8), fontSize = 13.sp)

        Spacer(modifier = Modifier.height(16.dp))

        if (broadcastNotice != null) {
            Surface(
                color = Color(0xFF064E3B),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, EmeraldAccent),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(broadcastNotice!!, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Passenger Payment Instructions
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = EmeraldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Passenger Top-Up Payment Instructions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("This message will be sent to all passengers in their top-up wallet area:", color = Color(0xFF94A3B8), fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = passInput,
                    onValueChange = { passInput = it },
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldAccent, unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Driver Payout Instructions
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Driver Payout Instructions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("This message explains to drivers how they get paid and request withdrawals:", color = Color(0xFF94A3B8), fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = driverInput,
                    onValueChange = { driverInput = it },
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8), unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                SoftKeeperRepository.broadcastPaymentInstructions(passInput, driverInput)
                broadcastNotice = "Payment & payout instructions updated and broadcast to all users!"
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Broadcast Payment Instructions to All Users", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Complaints Section
        Text("Unresolved Complaints (${complaints.count { it.status == "PENDING" }})", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        if (complaints.isEmpty()) {
            Text("No complaints submitted.", color = Color(0xFF94A3B8), fontSize = 13.sp)
        } else {
            complaints.forEach { cmp ->
                Card(colors = CardDefaults.cardColors(containerColor = DarkBluePrimary), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Category: ${cmp.category}", color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Passenger: ${cmp.passengerName} -> Driver: ${cmp.driverName}", color = Color.White, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("\"${cmp.details}\"", color = Color(0xFFCBD5E1), fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { SoftKeeperRepository.resolveComplaint(cmp.id, "WARN") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                modifier = Modifier.weight(1f)
                            ) { Text("Warn Driver", fontSize = 11.sp) }

                            Button(
                                onClick = { SoftKeeperRepository.resolveComplaint(cmp.id, "SUSPEND") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                modifier = Modifier.weight(1f)
                            ) { Text("Suspend Driver", fontSize = 11.sp) }
                        }
                    }
                }
            }
        }
    }
}
