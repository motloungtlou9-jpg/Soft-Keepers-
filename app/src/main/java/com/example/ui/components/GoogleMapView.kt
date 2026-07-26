package com.example.ui.components

import androidx.compose.animation.*
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
    driverMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var useSdkMap by remember { mutableStateOf(true) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }

    val driverPos = LatLng(driverLat, driverLng)
    val pickupPos = LatLng(pickupLat, pickupLng)
    val dropoffPos = LatLng(dropoffLat, dropoffLng)

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
