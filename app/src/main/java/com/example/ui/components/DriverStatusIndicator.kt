package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class DriverStatus {
    ACTIVE,
    IDLE,
    OFFLINE
}

@Composable
fun DriverStatusIndicator(
    status: DriverStatus,
    modifier: Modifier = Modifier,
    speed: Float? = null // speed in m/s or km/h
) {
    val (statusText, badgeColor) = when (status) {
        DriverStatus.ACTIVE -> "Active" to Color(0xFF2E7D32) // Green
        DriverStatus.IDLE -> "Idle" to Color(0xFFEF6C00) // Orange/Amber
        DriverStatus.OFFLINE -> "Offline" to Color(0xFF757575) // Gray
    }

    // Pulsing animation for active state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (status == DriverStatus.ACTIVE) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        modifier = modifier
            .testTag("driver_status_indicator"),
        shape = RoundedCornerShape(16.dp),
        color = badgeColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .scale(if (status == DriverStatus.ACTIVE) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(badgeColor)
            )

            Column {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = badgeColor,
                    fontSize = 13.sp
                )
                if (status == DriverStatus.ACTIVE && speed != null && speed > 0f) {
                    Text(
                        text = "${String.format("%.1f", speed * 3.6)} km/h",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
