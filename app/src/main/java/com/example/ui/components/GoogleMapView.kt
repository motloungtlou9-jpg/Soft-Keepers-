package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBluePrimary
import com.example.ui.theme.EmeraldAccent
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun GoogleMapView(
    pickupAddress: String = "Sandton City, Johannesburg",
    dropoffAddress: String = "OR Tambo Airport, Kempton Park",
    driverLat: Double = -26.15,
    driverLng: Double = 28.08,
    pickupLat: Double = -26.11,
    pickupLng: Double = 28.05,
    dropoffLat: Double = -26.13,
    dropoffLng: Double = 28.24,
    showDriver: Boolean = true,
    isTripStarted: Boolean = false,
    driverMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var useSdkMap by remember { mutableStateOf(true) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }

    val driverPos = LatLng(driverLat, driverLng)
    val pickupPos = LatLng(pickupLat, pickupLng)
    val dropoffPos = LatLng(dropoffLat, dropoffLng)

    // Calculate ETA and Distance
    val targetLat = if (isTripStarted) dropoffLat else pickupLat
    val targetLng = if (isTripStarted) dropoffLng else pickupLng

    val (rawEtaMins, distanceKm) = remember(driverLat, driverLng, targetLat, targetLng) {
        calculateEtaAndDistance(driverLat, driverLng, targetLat, targetLng)
    }

    val animatedEta by animateIntAsState(
        targetValue = rawEtaMins,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "etaAnimation"
    )

    val animatedDistance by animateFloatAsState(
        targetValue = distanceKm.toFloat(),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "distanceAnimation"
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            if (showDriver) driverPos else pickupPos,
            13f
        )
    }

    // Update camera position smoothly when driver moves
    LaunchedEffect(driverLat, driverLng) {
        if (showDriver) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(driverPos, 14f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE2E8F0))
            .testTag("google_map_container")
    ) {
        if (useSdkMap) {
            val uiSettings = remember {
                MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = true,
                    myLocationButtonEnabled = true
                )
            }
            val mapProperties = remember(mapType) {
                MapProperties(
                    mapType = mapType,
                    isMyLocationEnabled = false
                )
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings
            ) {
                // Pickup Location Marker
                Marker(
                    state = rememberMarkerState(position = pickupPos),
                    title = "Pickup Point",
                    snippet = pickupAddress,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )

                // Dropoff Location Marker
                Marker(
                    state = rememberMarkerState(position = dropoffPos),
                    title = "Destination",
                    snippet = dropoffAddress,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )

                // Live Vehicle GPS Marker
                if (showDriver) {
                    Marker(
                        state = rememberMarkerState(position = driverPos),
                        title = "Live Vehicle GPS",
                        snippet = "Taxi - Active Route",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )

                    // Route Polyline
                    Polyline(
                        points = listOf(pickupPos, driverPos, dropoffPos),
                        color = Color(0xFF10B981),
                        width = 12f
                    )
                } else {
                    Polyline(
                        points = listOf(pickupPos, dropoffPos),
                        color = Color(0xFF10B981),
                        width = 10f
                    )
                }
            }
        } else {
            // High-Contrast Vector Map Fallback View
            SimulatedMapView(
                pickupAddress = pickupAddress,
                dropoffAddress = dropoffAddress,
                driverLat = driverLat,
                driverLng = driverLng,
                pickupLat = pickupLat,
                pickupLng = pickupLng,
                dropoffLat = dropoffLat,
                dropoffLng = dropoffLng,
                showDriver = showDriver,
                driverMessage = driverMessage,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Overlay Map Header (GPS Signal & Mode Switch)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(DarkBluePrimary.copy(alpha = 0.92f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.GpsFixed,
                contentDescription = "GPS Active",
                tint = EmeraldAccent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (useSdkMap) "Google Maps GPS Live" else "Vector GPS Map",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Controls Floating Stack (Top Right)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mode Toggle Button (SDK vs Vector Map)
            FloatingActionButton(
                onClick = { useSdkMap = !useSdkMap },
                modifier = Modifier.size(38.dp).testTag("map_mode_toggle"),
                containerColor = DarkBluePrimary,
                contentColor = EmeraldAccent,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (useSdkMap) Icons.Default.Map else Icons.Default.Layers,
                    contentDescription = "Toggle Map Style",
                    modifier = Modifier.size(20.dp)
                )
            }

            if (useSdkMap) {
                // Map Type Toggle (Satellite vs Normal)
                FloatingActionButton(
                    onClick = {
                        mapType = if (mapType == MapType.NORMAL) MapType.SATELLITE else MapType.NORMAL
                    },
                    modifier = Modifier.size(38.dp),
                    containerColor = Color.White,
                    contentColor = DarkBluePrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Terrain,
                        contentDescription = "Satellite/Normal",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Recenter Camera Button
                FloatingActionButton(
                    onClick = {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            if (showDriver) driverPos else pickupPos,
                            14f
                        )
                    },
                    modifier = Modifier.size(38.dp),
                    containerColor = Color.White,
                    contentColor = DarkBluePrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Animated ETA Live Badge Overlay (Top Center)
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .shadow(6.dp, RoundedCornerShape(20.dp))
                .testTag("eta_badge"),
            color = DarkBluePrimary,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "ETA",
                    tint = EmeraldAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ETA: $animatedEta min${if (animatedEta > 1) "s" else ""} (${"%.1f".format(animatedDistance)} km)",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Driver Message Toast Overlay (Bottom Center)
        if (!driverMessage.isNullOrEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
                    .fillMaxWidth(0.9f)
                    .shadow(8.dp, RoundedCornerShape(12.dp)),
                color = DarkBluePrimary,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "Driver Message",
                        tint = EmeraldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "\"$driverMessage\"",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

fun calculateEtaAndDistance(
    driverLat: Double,
    driverLng: Double,
    destLat: Double,
    destLng: Double
): Pair<Int, Double> {
    val R = 6371.0
    val dLat = Math.toRadians(destLat - driverLat)
    val dLon = Math.toRadians(destLng - driverLng)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(driverLat)) * Math.cos(Math.toRadians(destLat)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    val distanceKm = R * c
    val etaMins = (distanceKm * 2.5).toInt().coerceAtLeast(1)
    return Pair(etaMins, distanceKm)
}

