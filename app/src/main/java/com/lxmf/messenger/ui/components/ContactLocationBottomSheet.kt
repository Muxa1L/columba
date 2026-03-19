package com.lxmf.messenger.ui.components

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lxmf.messenger.R
import com.lxmf.messenger.viewmodel.ContactMarker
import com.lxmf.messenger.viewmodel.MarkerState

/**
 * Bottom sheet displayed when tapping a contact's location marker on the map.
 *
 * Shows:
 * - Contact's identicon avatar and display name
 * - Last updated time ("Updated 30s ago")
 * - Distance and direction from user ("450m southeast")
 * - Directions button → opens external maps app
 * - Message button → navigates to conversation
 *
 * @param marker The contact marker that was tapped
 * @param userLocation The user's current location (for distance calculation)
 * @param onDismiss Callback when the bottom sheet is dismissed
 * @param onSendMessage Callback to navigate to the conversation with this contact
 * @param sheetState The state of the bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactLocationBottomSheet(
    marker: ContactMarker,
    userLocation: Location?,
    onDismiss: () -> Unit,
    onSendMessage: () -> Unit,
    onRemoveMarker: () -> Unit = {},
    sheetState: SheetState,
) {
    val context = LocalContext.current
    val isStale = marker.state != MarkerState.FRESH
    val directionsLabel = stringResource(R.string.common_directions)
    val messageLabel = stringResource(R.string.common_message)
    val removeFromMapLabel = stringResource(R.string.contact_location_remove_from_map)
    val noMapsApplicationLabel = stringResource(R.string.contact_location_no_maps_app)
    val locationUnknownLabel = stringResource(R.string.contact_location_unknown)
    val northLabel = stringResource(R.string.contact_location_direction_north)
    val northeastLabel = stringResource(R.string.contact_location_direction_northeast)
    val eastLabel = stringResource(R.string.contact_location_direction_east)
    val southeastLabel = stringResource(R.string.contact_location_direction_southeast)
    val southLabel = stringResource(R.string.contact_location_direction_south)
    val southwestLabel = stringResource(R.string.contact_location_direction_southwest)
    val westLabel = stringResource(R.string.contact_location_direction_west)
    val northwestLabel = stringResource(R.string.contact_location_direction_northwest)
    val updatedJustNowLabel = stringResource(R.string.contact_location_updated_just_now)
    val distanceText =
        formatDistanceAndDirection(
            userLocation = userLocation,
            markerLat = marker.latitude,
            markerLng = marker.longitude,
            locationUnknown = locationUnknownLabel,
            metersFormatter = { distance -> context.getString(R.string.contact_location_distance_meters, distance) },
            kilometersFormatter = { distance -> context.getString(R.string.contact_location_distance_kilometers, distance) },
            directionFormatter = { bearing ->
                bearingToDirection(
                    bearing = bearing,
                    north = northLabel,
                    northeast = northeastLabel,
                    east = eastLabel,
                    southeast = southeastLabel,
                    south = southLabel,
                    southwest = southwestLabel,
                    west = westLabel,
                    northwest = northwestLabel,
                )
            },
            distanceWithDirectionFormatter = { distance, direction ->
                context.getString(R.string.contact_location_distance_with_direction, distance, direction)
            },
        )
    val updatedText =
        formatUpdatedTime(
            timestamp = marker.timestamp,
            justNow = updatedJustNowLabel,
            secondsFormatter = { seconds -> context.getString(R.string.contact_location_updated_seconds, seconds) },
            minutesFormatter = { minutes -> context.getString(R.string.contact_location_updated_minutes, minutes) },
            hoursFormatter = { hours -> context.getString(R.string.contact_location_updated_hours, hours) },
            daysFormatter = { days -> context.getString(R.string.contact_location_updated_days, days) },
        )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0) },
        modifier = Modifier.systemBarsPadding(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
        ) {
            // Avatar, name, and last updated
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Profile icon (dimmed for stale locations)
                Box {
                    ProfileIcon(
                        iconName = marker.iconName,
                        foregroundColor = marker.iconForegroundColor,
                        backgroundColor = marker.iconBackgroundColor,
                        size = 48.dp,
                        fallbackHash = marker.publicKey ?: hexStringToByteArray(marker.destinationHash),
                        modifier = if (isStale) Modifier.alpha(0.6f) else Modifier,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = marker.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        // Stale badge
                        if (isStale) {
                            Spacer(modifier = Modifier.width(8.dp))
                            StaleLocationBadge(marker.state)
                        }
                    }
                    Text(
                        text = updatedText,
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            if (isStale) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Distance and direction
            Text(
                text = distanceText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { openDirectionsInMaps(context, marker.latitude, marker.longitude, noMapsApplicationLabel) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(directionsLabel)
                }

                Button(
                    onClick = onSendMessage,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(messageLabel)
                }
            }

            // Remove marker button (only for stale/expired markers)
            if (isStale) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onRemoveMarker,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        removeFromMapLabel,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Calculate and format the distance and direction from user to marker.
 *
 * @param userLocation The user's current location, or null if unavailable
 * @param markerLat Marker latitude
 * @param markerLng Marker longitude
 * @return Formatted string like "450m southeast" or "Location unknown"
 */
internal fun formatDistanceAndDirection(
    userLocation: Location?,
    markerLat: Double,
    markerLng: Double,
    locationUnknown: String = "Location unknown",
    metersFormatter: (Int) -> String = { distance -> "${distance}m" },
    kilometersFormatter: (Double) -> String = { distance -> "%.1fkm".format(distance) },
    directionFormatter: (Float) -> String = { bearing -> bearingToDirection(bearing) },
    distanceWithDirectionFormatter: (String, String) -> String = { distance, direction -> "$distance $direction" },
): String {
    if (userLocation == null) return locationUnknown

    val results = FloatArray(2)
    Location.distanceBetween(
        userLocation.latitude,
        userLocation.longitude,
        markerLat,
        markerLng,
        results,
    )
    val distance = results[0]
    val bearing = results[1]

    val distanceText =
        when {
            distance < 1000 -> metersFormatter(distance.toInt())
            else -> kilometersFormatter(distance / 1000.0)
        }

    val direction = directionFormatter(bearing)
    return distanceWithDirectionFormatter(distanceText, direction)
}

/**
 * Convert a bearing angle to a cardinal/intercardinal direction.
 *
 * @param bearing Bearing in degrees (0-360)
 * @return Direction string like "north", "southeast", etc.
 */
internal fun bearingToDirection(
    bearing: Float,
    north: String = "north",
    northeast: String = "northeast",
    east: String = "east",
    southeast: String = "southeast",
    south: String = "south",
    southwest: String = "southwest",
    west: String = "west",
    northwest: String = "northwest",
): String {
    val normalized = (bearing + 360) % 360
    return when {
        normalized < 22.5 || normalized >= 337.5 -> north
        normalized < 67.5 -> northeast
        normalized < 112.5 -> east
        normalized < 157.5 -> southeast
        normalized < 202.5 -> south
        normalized < 247.5 -> southwest
        normalized < 292.5 -> west
        else -> northwest
    }
}

/**
 * Format the timestamp as a relative time string.
 *
 * @param timestamp Timestamp in milliseconds since epoch
 * @return Formatted string like "Updated just now", "Updated 30s ago", "Updated 5m ago"
 */
internal fun formatUpdatedTime(
    timestamp: Long,
    justNow: String = "Updated just now",
    secondsFormatter: (Long) -> String = { seconds -> "Updated ${seconds}s ago" },
    minutesFormatter: (Long) -> String = { minutes -> "Updated ${minutes}m ago" },
    hoursFormatter: (Long) -> String = { hours -> "Updated ${hours}h ago" },
    daysFormatter: (Long) -> String = { days -> "Updated ${days}d ago" },
): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 10_000 -> justNow
        diff < 60_000 -> secondsFormatter(diff / 1000)
        diff < 3600_000 -> minutesFormatter(diff / 60_000)
        diff < 86400_000 -> hoursFormatter(diff / 3600_000)
        else -> daysFormatter(diff / 86400_000)
    }
}

/**
 * Open directions to a location in an external maps app.
 *
 * Tries Google Maps navigation first, falls back to generic geo URI.
 *
 * @param context Android context
 * @param lat Destination latitude
 * @param lng Destination longitude
 */
internal fun openDirectionsInMaps(
    context: Context,
    lat: Double,
    lng: Double,
    noMapsApplicationLabel: String = "No maps application found",
) {
    try {
        // Try Google Maps navigation first (walking mode)
        val googleMapsUri = Uri.parse("google.navigation:q=$lat,$lng&mode=w")
        val googleIntent = Intent(Intent.ACTION_VIEW, googleMapsUri)

        if (googleIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(googleIntent)
        } else {
            // Fallback to generic geo URI
            val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
            val geoIntent = Intent(Intent.ACTION_VIEW, geoUri)
            context.startActivity(geoIntent)
        }
    } catch (_: android.content.ActivityNotFoundException) {
        android.widget.Toast
            .makeText(context, noMapsApplicationLabel, android.widget.Toast.LENGTH_SHORT)
            .show()
    }
}

/**
 * Badge indicating the location is stale or expired.
 *
 * @param state The marker state (STALE or EXPIRED_GRACE_PERIOD)
 */
@Composable
private fun StaleLocationBadge(state: MarkerState) {
    if (state == MarkerState.FRESH) return

    val (text, color) =
        when (state) {
            MarkerState.STALE -> stringResource(R.string.contact_location_stale) to MaterialTheme.colorScheme.outline
            MarkerState.EXPIRED_GRACE_PERIOD -> stringResource(R.string.contact_location_last_known) to MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            else -> return
        }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

/**
 * Convert a hex string to a byte array.
 *
 * @param hex Hex string (with or without spaces/colons)
 * @return ByteArray
 */
private fun hexStringToByteArray(hex: String): ByteArray {
    val cleanHex = hex.replace(" ", "").replace(":", "")
    val len = cleanHex.length
    if (len == 0 || len % 2 != 0) return ByteArray(0)

    val data = ByteArray(len / 2)
    var i = 0
    while (i < len) {
        data[i / 2] = ((Character.digit(cleanHex[i], 16) shl 4) + Character.digit(cleanHex[i + 1], 16)).toByte()
        i += 2
    }
    return data
}
