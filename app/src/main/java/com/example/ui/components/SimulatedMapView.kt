package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBluePrimary
import com.example.ui.theme.EmeraldAccent

@Composable
fun SimulatedMapView(
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
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 36f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE2E8F0))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
            .testTag("simulated_map_container")
    ) {
        // Render Canvas Map with Streets, Blocks, Route & Pins
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Background Terrain
            drawRect(color = Color(0xFFEFEFEF))

            // City Blocks / Parks
            drawRoundRect(
                color = Color(0xFFD1FAE5),
                topLeft = Offset(w * 0.05f, h * 0.1f),
                size = Size(w * 0.25f, h * 0.3f),
                cornerRadius = CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(w * 0.65f, h * 0.05f),
                size = Size(w * 0.3f, h * 0.25f),
                cornerRadius = CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = Color(0xFFE0E7FF),
                topLeft = Offset(w * 0.1f, h * 0.6f),
                size = Size(w * 0.35f, h * 0.3f),
                cornerRadius = CornerRadius(16f, 16f)
            )

            // Grid Roads (Main Avenues & Highways)
            val roadColor = Color.White
            val mainRoadColor = Color(0xFFCBD5E1)

            // Horizontal Roads
            drawRect(color = roadColor, topLeft = Offset(0f, h * 0.25f), size = Size(w, 28f))
            drawRect(color = roadColor, topLeft = Offset(0f, h * 0.55f), size = Size(w, 36f))
            drawRect(color = roadColor, topLeft = Offset(0f, h * 0.8f), size = Size(w, 24f))

            // Vertical Roads
            drawRect(color = roadColor, topLeft = Offset(w * 0.35f, 0f), size = Size(32f, h))
            drawRect(color = roadColor, topLeft = Offset(w * 0.7f, 0f), size = Size(28f, h))

            // Main Highway diagonal arc
            val highwayPath = Path().apply {
                moveTo(0f, h * 0.9f)
                cubicTo(w * 0.3f, h * 0.7f, w * 0.6f, h * 0.4f, w, h * 0.15f)
            }
            drawPath(
                path = highwayPath,
                color = mainRoadColor,
                style = Stroke(width = 42f)
            )
            drawPath(
                path = highwayPath,
                color = Color(0xFFFDE047), // Yellow center line
                style = Stroke(width = 6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f)))
            )

            // Calculate mapped screen coordinates for Pins
            // Normalize offsets inside canvas bounds
            val pickupScreen = Offset(w * 0.35f, h * 0.35f)
            val dropoffScreen = Offset(w * 0.78f, h * 0.72f)

            // Route Path Line (Emerald Green Glowing Route)
            val routePath = Path().apply {
                moveTo(pickupScreen.x, pickupScreen.y)
                lineTo(w * 0.35f, h * 0.55f)
                lineTo(w * 0.70f, h * 0.55f)
                lineTo(dropoffScreen.x, dropoffScreen.y)
            }

            drawPath(
                path = routePath,
                color = Color(0x3310B981),
                style = Stroke(width = 24f)
            )
            drawPath(
                path = routePath,
                color = EmeraldAccent,
                style = Stroke(width = 10f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(25f, 15f)))
            )

            // Pickup Marker Pulse
            drawCircle(
                color = Color(0x4410B981),
                radius = pulseRadius,
                center = pickupScreen
            )
            drawCircle(
                color = EmeraldAccent,
                radius = 12f,
                center = pickupScreen
            )

            // Dropoff Marker
            drawCircle(
                color = Color(0x44EF4444),
                radius = 16f,
                center = dropoffScreen
            )
            drawCircle(
                color = Color(0xFFEF4444),
                radius = 10f,
                center = dropoffScreen
            )

            // Driver Car Marker (Dynamic interpolated position)
            if (showDriver) {
                // Compute driver interpolated position
                val driverScreen = Offset(w * 0.48f, h * 0.55f)

                drawCircle(
                    color = Color(0x550F2042),
                    radius = 24f,
                    center = driverScreen
                )
                drawCircle(
                    color = DarkBluePrimary,
                    radius = 16f,
                    center = driverScreen
                )
            }
        }

        // Overlay Map Status Header (GPS Signal & Location Badge)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(DarkBluePrimary.copy(alpha = 0.9f))
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
                text = "GPS Live Tracking",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Map Control Floating Buttons (Right Side)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { /* Recenter map */ },
                modifier = Modifier.size(36.dp),
                containerColor = Color.White,
                contentColor = DarkBluePrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter", modifier = Modifier.size(18.dp))
            }
            FloatingActionButton(
                onClick = { /* Zoom in */ },
                modifier = Modifier.size(36.dp),
                containerColor = Color.White,
                contentColor = DarkBluePrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom in", modifier = Modifier.size(18.dp))
            }
        }

        // Driver Toast Message Overlay (if available)
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
                        contentDescription = "Quick Message",
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
