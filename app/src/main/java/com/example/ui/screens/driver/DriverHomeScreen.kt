package com.example.ui.screens.driver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun DriverHomeScreen(
    onLogout: () -> Unit
) {
    val currentDriver by SoftKeeperRepository.currentUser.collectAsState()
    val driverProfile by SoftKeeperRepository.currentDriverProfile.collectAsState()
    val activeRide by SoftKeeperRepository.activeRide.collectAsState()
    val allRides by SoftKeeperRepository.allRides.collectAsState()
    val notifications by SoftKeeperRepository.notifications.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Requests, 1: Documents, 2: Earnings, 3: Profile
    var showNotificationsSheet by remember { mutableStateOf(false) }

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
                            Text("Driver Portal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${driverProfile?.vehicleMake ?: "Vehicle"} • ${driverProfile?.vehicleRegistration ?: ""}", color = EmeraldAccent, fontSize = 11.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBluePrimary),
                actions = {
                    // Notifications Icon with Badge
                    IconButton(onClick = { showNotificationsSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (notifications.isNotEmpty()) {
                                    Badge(containerColor = EmeraldAccent, contentColor = DarkBluePrimary) {
                                        Text("${notifications.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                        Text(
                            text = if (driverProfile?.isOnline == true) "ONLINE" else "OFFLINE",
                            color = if (driverProfile?.isOnline == true) EmeraldAccent else Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = driverProfile?.isOnline ?: true,
                            onCheckedChange = { SoftKeeperRepository.toggleDriverOnline(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = DarkBluePrimary, checkedTrackColor = EmeraldAccent)
                        )
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
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                    label = { Text("Requests") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = DarkBluePrimary, selectedTextColor = EmeraldAccent, indicatorColor = EmeraldAccent)
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.FolderSpecial, contentDescription = null) },
                    label = { Text("Documents") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = DarkBluePrimary, selectedTextColor = EmeraldAccent, indicatorColor = EmeraldAccent)
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    label = { Text("Earnings") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = DarkBluePrimary, selectedTextColor = EmeraldAccent, indicatorColor = EmeraldAccent)
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = DarkBluePrimary, selectedTextColor = EmeraldAccent, indicatorColor = EmeraldAccent)
                )
            }
        },
        containerColor = DarkBlueBackground
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                0 -> DriverRequestsTab(
                    activeRide = activeRide,
                    waitingRides = allRides.filter {
                        it.status == RideStatus.WAITING ||
                        (it.status == RideStatus.DRIVER_OFFERED_FARE && it.driverId == currentDriver?.id) ||
                        (it.status == RideStatus.ACCEPTED && it.driverId == currentDriver?.id)
                    }
                )
                1 -> DriverDocumentsTab()
                2 -> DriverEarningsTab(allRides.filter { it.driverId == currentDriver?.id && it.status == RideStatus.TRIP_COMPLETED })
                3 -> DriverProfileTab(onLogout)
            }
        }
    }

    if (showNotificationsSheet) {
        AlertDialog(
            onDismissRequest = { showNotificationsSheet = false },
            containerColor = Color(0xFF1E293B),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCBD5E1),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = EmeraldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Driver Notification Area")
                }
            },
            text = {
                Column {
                    // Toggle Receive Notifications on Phone
                    Surface(
                        color = DarkBluePrimary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Receive Notifications on Phone", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Real-time audio & pop-up alerts for new passenger trip requests.", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                            Switch(
                                checked = currentDriver?.isNotificationsEnabled ?: true,
                                onCheckedChange = { SoftKeeperRepository.toggleNotificationsPermission() },
                                colors = SwitchDefaults.colors(checkedThumbColor = DarkBluePrimary, checkedTrackColor = EmeraldAccent)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Recent Alerts & Updates", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (notifications.isEmpty()) {
                        Text("No notifications right now.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 240.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(notifications) { notif ->
                                Surface(
                                    color = Color(0xFF0F172A),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(notif.title, color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(notif.message, color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsSheet = false }) {
                    Text("Close", color = EmeraldAccent)
                }
            }
        )
    }
}

@Composable
fun DriverRequestsTab(activeRide: Ride?, waitingRides: List<Ride>) {
    LocationPermissionHandler()
    if (activeRide != null && activeRide.status != RideStatus.TRIP_COMPLETED && activeRide.status != RideStatus.CANCELLED) {
        // Active Driver Navigation View
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Passenger: ${activeRide.passengerName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Tel: ${activeRide.passengerPhone}", color = EmeraldAccent, fontSize = 13.sp)
                    Text("Pickup: ${activeRide.pickupAddress}", color = Color(0xFFE2E8F0), fontSize = 13.sp)
                    Text("Dropoff: ${activeRide.dropoffAddress}", color = Color(0xFFE2E8F0), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fare: $${"%.2f".format(activeRide.fare)}", color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Messages Row
            Text("Send Quick Message to Passenger:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { SoftKeeperRepository.sendQuickMessage("I'm coming.") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.weight(1f).testTag("quick_msg_coming")
                ) { Text("I'm coming", fontSize = 11.sp) }

                Button(
                    onClick = { SoftKeeperRepository.sendQuickMessage("I've arrived.") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.weight(1f).testTag("quick_msg_arrived")
                ) { Text("I've arrived", fontSize = 11.sp) }

                Button(
                    onClick = { SoftKeeperRepository.sendQuickMessage("Running late.") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.weight(1f).testTag("quick_msg_late")
                ) { Text("Running late", fontSize = 11.sp) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Map View
            GoogleMapView(
                pickupAddress = activeRide.pickupAddress,
                dropoffAddress = activeRide.dropoffAddress,
                driverLat = activeRide.driverLat,
                driverLng = activeRide.driverLng,
                showDriver = true,
                driverMessage = activeRide.quickMessage,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Driver Ride Progression Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                if (activeRide.status == RideStatus.ACCEPTED || activeRide.status == RideStatus.DRIVER_ON_THE_WAY) {
                    Button(
                        onClick = { SoftKeeperRepository.updateRideStatus(RideStatus.DRIVER_ARRIVED, "I've arrived at pickup location") },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("status_arrived_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) { Text("I've Arrived", fontWeight = FontWeight.Bold) }
                }

                if (activeRide.status == RideStatus.DRIVER_ARRIVED) {
                    Button(
                        onClick = { SoftKeeperRepository.updateRideStatus(RideStatus.TRIP_STARTED, "Trip started") },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("status_start_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary)
                    ) { Text("Start Trip", fontWeight = FontWeight.Bold) }
                }

                if (activeRide.status == RideStatus.TRIP_STARTED) {
                    Button(
                        onClick = { SoftKeeperRepository.updateRideStatus(RideStatus.TRIP_COMPLETED, "Trip completed") },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("status_complete_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary)
                    ) { Text("Complete Trip", fontWeight = FontWeight.Bold) }
                }
            }
        }
    } else {
        // List of Available Requests & Fare Negotiation
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Available Transport Requests", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Protected Identity Mode: Passengers listed safely. Review starting & destination points.", color = EmeraldAccent, fontSize = 12.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(16.dp))

            if (waitingRides.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No active requests right now.", color = Color(0xFF94A3B8))
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(waitingRides) { ride ->
                        DriverRideRequestCard(ride = ride)
                    }
                }
            }
        }
    }
}

@Composable
fun DriverRideRequestCard(ride: Ride) {
    var fareInputText by remember(ride.id, ride.fare, ride.offeredFare) {
        mutableStateOf("%.0f".format(if (ride.offeredFare != null) ride.offeredFare else ride.fare))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = EmeraldAccent.copy(alpha = 0.2f),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Protected Passenger",
                            tint = EmeraldAccent,
                            modifier = Modifier.padding(6.dp).size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(ride.passengerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                // Status Badge
                Surface(
                    color = when (ride.status) {
                        RideStatus.DRIVER_OFFERED_FARE -> Color(0xFFD97706)
                        RideStatus.ACCEPTED -> Color(0xFF059669)
                        else -> DarkBlueBackground
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (ride.status) {
                            RideStatus.DRIVER_OFFERED_FARE -> "⏳ PENDING APPROVAL"
                            RideStatus.ACCEPTED -> "🎉 FARE APPROVED!"
                            else -> "R ${"%.2f".format(ride.fare)}"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real-time GPS starting point & Destination Point
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("STARTING POINT (Real-time GPS): ", color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(ride.pickupAddress, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DESTINATION POINT: ", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(ride.dropoffAddress, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (ride.status) {
                RideStatus.WAITING -> {
                    Text("Input the fare money you want to charge:", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fareInputText,
                            onValueChange = { fareInputText = it },
                            label = { Text("Fare Amount (R)") },
                            prefix = { Text("R ", color = EmeraldAccent, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldAccent,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Button(
                            onClick = {
                                val offerVal = fareInputText.toDoubleOrNull() ?: ride.fare
                                SoftKeeperRepository.driverOfferFare(ride.id, offerVal)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(54.dp).testTag("send_fare_offer_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Send Offer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Quick Fare Options:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(50, 100, 150, 200, 250).forEach { preset ->
                            OutlinedButton(
                                onClick = { fareInputText = preset.toString() },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1)),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("R$preset", fontSize = 10.sp)
                            }
                        }
                    }
                }

                RideStatus.DRIVER_OFFERED_FARE -> {
                    Surface(
                        color = Color(0xFF78350F),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFFDE047), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fare Offer Sent: R ${"%.2f".format(ride.offeredFare ?: ride.fare)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Pending Passenger Approval. Passenger will approve or pass on this offer shortly...", color = Color(0xFFFEF08A), fontSize = 11.sp)
                        }
                    }
                }

                RideStatus.ACCEPTED -> {
                    Surface(
                        color = Color(0xFF064E3B),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, EmeraldAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Passenger Approved Fare (R ${"%.2f".format(ride.fare)})!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Send notification to passenger that you are coming now:", color = Color(0xFFA7F3D0), fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { SoftKeeperRepository.driverConfirmOnTheWay(ride.id, "I am coming now!") },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("🚀 Send 'I'm coming!'", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { SoftKeeperRepository.driverConfirmOnTheWay(ride.id, "On my way! ETA 5 mins.") },
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("⏱️ 'ETA 5 mins'", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun DriverDocumentsTab() {
    val profile by SoftKeeperRepository.currentDriverProfile.collectAsState()
    var updatedMsg by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Driver & Vehicle Documents", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Uploaded credentials for platform compliance", color = Color(0xFF94A3B8), fontSize = 13.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = DarkBluePrimary), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Driver's License", color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("License No: ${profile?.driverLicenseNumber}", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Vehicle Information", color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${profile?.vehicleMake} ${profile?.vehicleModel}", color = Color.White, fontSize = 14.sp)
                Text("Registration: ${profile?.vehicleRegistration}", color = Color(0xFF94A3B8), fontSize = 13.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(id = R.drawable.img_vehicle_placeholder_1784959849639),
                    contentDescription = "Vehicle Photo",
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { updatedMsg = true },
                    modifier = Modifier.fillMaxWidth().testTag("upload_docs_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = DarkBluePrimary)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Re-Upload Vehicle Photo & License", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (updatedMsg) {
        AlertDialog(
            onDismissRequest = { updatedMsg = false },
            title = { Text("Documents Updated") },
            text = { Text("Vehicle photo and license verification updated successfully.") },
            confirmButton = { Button(onClick = { updatedMsg = false }) { Text("OK") } }
        )
    }
}

@Composable
fun DriverEarningsTab(completedRides: List<Ride>) {
    val totalEarnings = completedRides.sumOf { it.fare }
    val payoutInstructions by SoftKeeperRepository.driverPayoutInstructions.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Earnings & Trip History", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Overview of your completed rides & payout instructions", color = Color(0xFF94A3B8), fontSize = 13.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Official Admin Payout Instructions Banner
        Surface(
            color = DarkBluePrimary,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Official Admin Payout Instructions:", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(payoutInstructions, color = Color.White, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = DarkBluePrimary), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total Earnings", color = Color(0xFF94A3B8), fontSize = 14.sp)
                Text("$${"%.2f".format(totalEarnings)}", color = EmeraldAccent, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text("${completedRides.size} Trips Completed", color = Color.White, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(completedRides) { ride ->
                Card(colors = CardDefaults.cardColors(containerColor = DarkBluePrimary), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(ride.passengerName, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${ride.pickupAddress.take(20)}... -> ${ride.dropoffAddress.take(20)}...", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Text("$${"%.2f".format(ride.fare)}", color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DriverProfileTab(onLogout: () -> Unit) {
    val user by SoftKeeperRepository.currentUser.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Driver Profile", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Personal settings & Crown verification status", color = Color(0xFF94A3B8), fontSize = 13.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Driver Crown Banner
        if (user?.isCrownVerified == true) {
            Surface(
                color = Color(0xFF1E1B4B),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
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
                            Text("CROWN VERIFIED DRIVER", color = Color(0xFFFDE047), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Met Admin face-to-face. Displayed to passengers as Crowned.", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    }
                }
            }
        } else {
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👻", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("GHOST DRIVER (Unverified)", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("You are an unverified ghost driver. Contact Admin for face-to-face verification to receive your Crown 👑.", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = DarkBluePrimary), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.img_driver_avatar_1784959837433),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(80.dp).clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(user?.name ?: "", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(user?.email ?: "", color = Color(0xFF94A3B8), fontSize = 14.sp)
                Text(user?.phone ?: "", color = EmeraldAccent, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("driver_logout_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", fontWeight = FontWeight.Bold)
        }
    }
}
