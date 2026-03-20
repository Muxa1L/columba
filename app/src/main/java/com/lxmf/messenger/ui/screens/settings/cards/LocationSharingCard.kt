@file:Suppress("TooManyFunctions") // Composable UI file with multiple small components

package com.lxmf.messenger.ui.screens.settings.cards

import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lxmf.messenger.R
import com.lxmf.messenger.data.model.EnrichedContact
import com.lxmf.messenger.service.SharingSession
import com.lxmf.messenger.ui.components.CollapsibleSettingsCard
import com.lxmf.messenger.ui.components.ProfileIcon
import com.lxmf.messenger.ui.model.SharingDuration
import kotlinx.coroutines.delay

/**
 * Settings card for managing location sharing preferences and active sessions.
 *
 * Features:
 * - Master toggle to enable/disable location sharing
 * - List of active sharing sessions with stop buttons
 * - Stop all sharing button
 * - Default duration picker
 * - Location precision picker
 * - Telemetry collector configuration
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocationSharingCard(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    activeSessions: List<SharingSession>,
    onStopSharing: (String) -> Unit,
    onStopAllSharing: () -> Unit,
    defaultDuration: String,
    onDefaultDurationChange: (String) -> Unit,
    locationPrecisionRadius: Int,
    onLocationPrecisionRadiusChange: (Int) -> Unit,
    // Telemetry collector props
    telemetryCollectorEnabled: Boolean,
    telemetryCollectorAddress: String?,
    telemetrySendIntervalSeconds: Int,
    lastTelemetrySendTime: Long?,
    isSendingTelemetry: Boolean,
    onTelemetryEnabledChange: (Boolean) -> Unit,
    onTelemetryCollectorAddressChange: (String?) -> Unit,
    onTelemetrySendIntervalChange: (Int) -> Unit,
    onTelemetrySendNow: () -> Unit,
    // Telemetry request props
    telemetryRequestEnabled: Boolean,
    telemetryRequestIntervalSeconds: Int,
    lastTelemetryRequestTime: Long?,
    isRequestingTelemetry: Boolean,
    onTelemetryRequestEnabledChange: (Boolean) -> Unit,
    onTelemetryRequestIntervalChange: (Int) -> Unit,
    onRequestTelemetryNow: () -> Unit,
    // Telemetry host mode props (acting as collector for others)
    telemetryHostModeEnabled: Boolean,
    onTelemetryHostModeEnabledChange: (Boolean) -> Unit,
    // Allowed requesters for host mode
    telemetryAllowedRequesters: Set<String>,
    contacts: List<EnrichedContact>,
    onTelemetryAllowedRequestersChange: (Set<String>) -> Unit,
    // Local identity for "Myself" option in host picker
    localDestinationHash: String? = null,
    localDisplayName: String = "Myself",
    localIconName: String? = null,
    localIconForegroundColor: String? = null,
    localIconBackgroundColor: String? = null,
    // Background location permission status
    hasForegroundLocationPermission: Boolean = false,
    hasBackgroundLocationPermission: Boolean = false,
    onBackgroundPermissionClick: () -> Unit = {},
) {
    var showDurationPicker by remember { mutableStateOf(false) }
    var showPrecisionPicker by remember { mutableStateOf(false) }
    val localizedSelfDisplayName = if (localDisplayName == "Myself") stringResource(R.string.settings_location_sharing_myself) else localDisplayName

    CollapsibleSettingsCard(
        title = stringResource(R.string.settings_location_sharing_title),
        icon = Icons.Default.LocationOn,
        isExpanded = isExpanded,
        onExpandedChange = onExpandedChange,
        headerAction = {
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
            )
        },
    ) {
        // Description
        Text(
            text = stringResource(R.string.settings_location_sharing_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Background location permission status indicator
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = onBackgroundPermissionClick,
                        role = Role.Button,
                    )
                    .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint =
                    if (hasBackgroundLocationPermission) {
                        MaterialTheme.colorScheme.primary
                    } else if (hasForegroundLocationPermission) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                modifier = Modifier.size(18.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text =
                        if (hasBackgroundLocationPermission) {
                            stringResource(R.string.settings_location_sharing_background_always)
                        } else if (hasForegroundLocationPermission) {
                            stringResource(R.string.settings_location_sharing_background_while_using)
                        } else {
                            stringResource(R.string.settings_location_sharing_background_not_granted)
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text =
                        if (hasBackgroundLocationPermission) {
                            stringResource(R.string.settings_location_sharing_background_change)
                        } else if (hasForegroundLocationPermission) {
                            stringResource(R.string.settings_location_sharing_background_enable)
                        } else {
                            stringResource(R.string.settings_location_sharing_background_grant)
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.common_change),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        // Active sessions section (only shown when enabled and there are sessions)
        if (enabled && activeSessions.isNotEmpty()) {
            ActiveSessionsSection(
                sessions = activeSessions,
                onStopSharing = onStopSharing,
                onStopAllSharing = onStopAllSharing,
            )
        }

        HorizontalDivider()

        // Default duration picker
        SettingsRow(
            label = stringResource(R.string.settings_location_sharing_default_duration),
            value = localizedDurationDisplayText(defaultDuration),
            onClick = { showDurationPicker = true },
        )

        // Location precision picker
        SettingsRow(
            label = stringResource(R.string.settings_location_sharing_location_precision),
            value = localizedPrecisionRadiusDisplayText(locationPrecisionRadius),
            onClick = { showPrecisionPicker = true },
        )

        // Telemetry Collector Section
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        TelemetryCollectorSection(
            enabled = telemetryCollectorEnabled,
            collectorAddress = telemetryCollectorAddress,
            sendIntervalSeconds = telemetrySendIntervalSeconds,
            lastSendTime = lastTelemetrySendTime,
            isSending = isSendingTelemetry,
            onEnabledChange = onTelemetryEnabledChange,
            onCollectorAddressChange = onTelemetryCollectorAddressChange,
            onSendIntervalChange = onTelemetrySendIntervalChange,
            onSendNow = onTelemetrySendNow,
            // Request props
            requestEnabled = telemetryRequestEnabled,
            requestIntervalSeconds = telemetryRequestIntervalSeconds,
            lastRequestTime = lastTelemetryRequestTime,
            isRequesting = isRequestingTelemetry,
            onRequestEnabledChange = onTelemetryRequestEnabledChange,
            onRequestIntervalChange = onTelemetryRequestIntervalChange,
            onRequestNow = onRequestTelemetryNow,
            // Host mode props
            hostModeEnabled = telemetryHostModeEnabled,
            onHostModeEnabledChange = onTelemetryHostModeEnabledChange,
            // Allowed requesters props
            allowedRequesters = telemetryAllowedRequesters,
            contacts = contacts,
            onAllowedRequestersChange = onTelemetryAllowedRequestersChange,
            // Local identity for "Myself" option
            localDestinationHash = localDestinationHash,
            localDisplayName = localizedSelfDisplayName,
            localIconName = localIconName,
            localIconForegroundColor = localIconForegroundColor,
            localIconBackgroundColor = localIconBackgroundColor,
        )
    }

    // Duration picker dialog
    if (showDurationPicker) {
        DurationPickerDialog(
            currentDuration = defaultDuration,
            onDurationSelected = {
                onDefaultDurationChange(it)
                showDurationPicker = false
            },
            onDismiss = { showDurationPicker = false },
        )
    }

    // Precision picker dialog
    if (showPrecisionPicker) {
        PrecisionRadiusPickerDialog(
            currentRadius = locationPrecisionRadius,
            onRadiusSelected = {
                onLocationPrecisionRadiusChange(it)
                showPrecisionPicker = false
            },
            onDismiss = { showPrecisionPicker = false },
        )
    }
}

@Composable
private fun ActiveSessionsSection(
    sessions: List<SharingSession>,
    onStopSharing: (String) -> Unit,
    onStopAllSharing: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_location_sharing_currently_sharing),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        sessions.forEach { session ->
            ActiveSessionRow(
                session = session,
                onStopSharing = { onStopSharing(session.destinationHash) },
            )
        }

        if (sessions.size > 1) {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = onStopAllSharing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_location_sharing_stop_all))
            }
        }
    }
}

@Composable
private fun ActiveSessionRow(
    session: SharingSession,
    onStopSharing: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = session.displayName,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = localizedFormatTimeRemaining(session.endTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = onStopSharing) {
            Text(stringResource(R.string.settings_location_sharing_stop))
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = stringResource(R.string.common_select),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DurationPickerDialog(
    currentDuration: String,
    onDurationSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_location_sharing_dialog_default_duration)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.settings_location_sharing_dialog_default_duration_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SharingDuration.entries.forEach { duration ->
                        FilterChip(
                            selected = currentDuration == duration.name,
                            onClick = { onDurationSelected(duration.name) },
                            label = { Text(localizedDurationDisplayText(duration.name)) },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_done))
            }
        },
    )
}

/**
 * Precision radius presets for the picker.
 */
private enum class PrecisionPreset(
    val radiusMeters: Int,
    val displayNameRes: Int,
    val descriptionRes: Int,
) {
    PRECISE(0, R.string.settings_location_sharing_precision_precise, R.string.settings_location_sharing_precision_exact_gps),
    NEIGHBORHOOD(100, R.string.settings_location_sharing_precision_neighborhood, R.string.settings_location_sharing_precision_neighborhood_radius),
    CITY(1000, R.string.settings_location_sharing_precision_city, R.string.settings_location_sharing_precision_city_radius),
    REGION(10000, R.string.settings_location_sharing_precision_region, R.string.settings_location_sharing_precision_region_radius),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrecisionRadiusPickerDialog(
    currentRadius: Int,
    onRadiusSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_location_sharing_dialog_location_precision)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.settings_location_sharing_dialog_location_precision_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))

                PrecisionPreset.entries.forEach { preset ->
                    PrecisionRadiusOption(
                        title = stringResource(preset.displayNameRes),
                        description = stringResource(preset.descriptionRes),
                        isSelected = currentRadius == preset.radiusMeters,
                        onClick = { onRadiusSelected(preset.radiusMeters) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_done))
            }
        },
    )
}

@Composable
private fun PrecisionRadiusOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
    )
}

/**
 * Format time remaining until sharing session ends.
 */
internal fun formatTimeRemaining(endTime: Long?): String {
    if (endTime == null) return "Until stopped"
    val remaining = endTime - System.currentTimeMillis()
    if (remaining <= 0) return "Expiring..."

    val minutes = remaining / 60_000
    val hours = minutes / 60

    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m remaining"
        else -> "${minutes}m remaining"
    }
}

/**
 * Get display text for a SharingDuration enum name.
 */
internal fun getDurationDisplayText(durationName: String): String =
    try {
        when (SharingDuration.valueOf(durationName)) {
            SharingDuration.FIFTEEN_MINUTES -> "15 min"
            SharingDuration.ONE_HOUR -> "1 hour"
            SharingDuration.FOUR_HOURS -> "4 hours"
            SharingDuration.UNTIL_MIDNIGHT -> "Until midnight"
            SharingDuration.INDEFINITE -> "Until I stop"
        }
    } catch (e: IllegalArgumentException) {
        "1 hour" // Default fallback
    }

/**
 * Get display text for a precision radius setting.
 */
internal fun getPrecisionRadiusDisplayText(radiusMeters: Int): String =
    when (radiusMeters) {
        0 -> "Precise"
        1000 -> "Neighborhood (~1km)"
        10000 -> "City (~10km)"
        100000 -> "Region (~100km)"
        else -> if (radiusMeters >= 1000) "${radiusMeters / 1000}km" else "${radiusMeters}m"
    }

@Composable
private fun localizedDurationDisplayText(durationName: String): String =
    when (durationName) {
        SharingDuration.FIFTEEN_MINUTES.name -> stringResource(R.string.settings_location_sharing_duration_fifteen_minutes)
        SharingDuration.ONE_HOUR.name -> stringResource(R.string.settings_location_sharing_duration_one_hour)
        SharingDuration.FOUR_HOURS.name -> stringResource(R.string.settings_location_sharing_duration_four_hours)
        SharingDuration.UNTIL_MIDNIGHT.name -> stringResource(R.string.settings_location_sharing_duration_until_midnight)
        SharingDuration.INDEFINITE.name -> stringResource(R.string.settings_location_sharing_duration_until_i_stop)
        else -> stringResource(R.string.settings_location_sharing_duration_fallback)
    }

@Composable
private fun localizedPrecisionRadiusDisplayText(radiusMeters: Int): String =
    when (radiusMeters) {
        0 -> stringResource(R.string.settings_location_sharing_precision_precise)
        1000 -> stringResource(R.string.settings_location_sharing_precision_neighborhood_display)
        10000 -> stringResource(R.string.settings_location_sharing_precision_city_display)
        100000 -> stringResource(R.string.settings_location_sharing_precision_region_display)
        else -> {
            if (radiusMeters >= 1000) {
                stringResource(R.string.settings_location_sharing_precision_km, radiusMeters / 1000)
            } else {
                stringResource(R.string.settings_location_sharing_precision_meters, radiusMeters)
            }
        }
    }

@Composable
private fun localizedFormatTimeRemaining(endTime: Long?): String {
    if (endTime == null) return stringResource(R.string.settings_location_sharing_until_stopped)
    val remaining = endTime - System.currentTimeMillis()
    if (remaining <= 0) return stringResource(R.string.settings_location_sharing_expiring)

    val minutes = remaining / 60_000
    val hours = minutes / 60

    return when {
        hours > 0 -> stringResource(R.string.settings_location_sharing_remaining_hours_minutes, hours, minutes % 60)
        else -> stringResource(R.string.settings_location_sharing_remaining_minutes, minutes)
    }
}

// =============================================================================
// Telemetry Collector Section
// =============================================================================

/**
 * Section for configuring telemetry collector integration.
 * Allows users to send location to a collector and receive locations from multiple peers.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TelemetryCollectorSection(
    enabled: Boolean,
    collectorAddress: String?,
    sendIntervalSeconds: Int,
    lastSendTime: Long?,
    isSending: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onCollectorAddressChange: (String?) -> Unit,
    onSendIntervalChange: (Int) -> Unit,
    onSendNow: () -> Unit,
    // Request props
    requestEnabled: Boolean,
    requestIntervalSeconds: Int,
    lastRequestTime: Long?,
    isRequesting: Boolean,
    onRequestEnabledChange: (Boolean) -> Unit,
    onRequestIntervalChange: (Int) -> Unit,
    onRequestNow: () -> Unit,
    // Host mode props (acting as collector for others)
    hostModeEnabled: Boolean,
    onHostModeEnabledChange: (Boolean) -> Unit,
    // Allowed requesters for host mode
    allowedRequesters: Set<String>,
    contacts: List<EnrichedContact>,
    onAllowedRequestersChange: (Set<String>) -> Unit,
    // Local identity for "Myself" option in host picker
    localDestinationHash: String? = null,
    localDisplayName: String = "Myself",
    localIconName: String? = null,
    localIconForegroundColor: String? = null,
    localIconBackgroundColor: String? = null,
) {
    var showAllowedRequestersDialog by remember { mutableStateOf(false) }
    var showContactPicker by remember { mutableStateOf(false) }

    // Check if "Myself" is selected as host
    val isSelfSelected =
        localDestinationHash != null &&
            collectorAddress.equals(localDestinationHash, ignoreCase = true)

    // Find the selected contact for display (null if "Myself" is selected)
    val selectedContact =
        if (isSelfSelected) {
            null
        } else {
            contacts.find {
                it.destinationHash.equals(collectorAddress, ignoreCase = true)
            }
        }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = stringResource(R.string.settings_location_sharing_group_tracker),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.settings_location_sharing_group_tracker),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }

        // Description
        Text(
            text = stringResource(R.string.settings_location_sharing_group_tracker_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Enable toggle
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = null,
                        indication = null,
                    ) { onEnabledChange(!enabled) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_location_sharing_share_with_group),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.settings_location_sharing_share_with_group_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = null,
            )
        }

        // Select from contacts
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.settings_location_sharing_group_host),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { showContactPicker = true }
                        .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (isSelfSelected) {
                    val hashBytes =
                        localDestinationHash
                            ?.chunked(2)
                            ?.mapNotNull { it.toIntOrNull(16)?.toByte() }
                            ?.toByteArray() ?: ByteArray(0)
                    ProfileIcon(
                        iconName = localIconName,
                        foregroundColor = localIconForegroundColor,
                        backgroundColor = localIconBackgroundColor,
                        size = 24.dp,
                        fallbackHash = hashBytes,
                    )
                } else if (selectedContact != null) {
                    val hashBytes =
                        selectedContact.destinationHash
                            .chunked(2)
                            .mapNotNull { it.toIntOrNull(16)?.toByte() }
                            .toByteArray()
                    ProfileIcon(
                        iconName = selectedContact.iconName,
                        foregroundColor = selectedContact.iconForegroundColor,
                        backgroundColor = selectedContact.iconBackgroundColor,
                        size = 24.dp,
                        fallbackHash = hashBytes,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text =
                        when {
                            isSelfSelected -> stringResource(R.string.settings_location_sharing_self_suffix, localDisplayName)
                            selectedContact != null -> selectedContact.displayName
                            collectorAddress != null -> collectorAddress
                            else -> stringResource(R.string.settings_location_sharing_select_contact)
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (isSelfSelected || selectedContact != null || collectorAddress != null) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Contact picker dialog
        if (showContactPicker) {
            GroupHostPickerDialog(
                contacts = contacts,
                selectedHash = collectorAddress,
                onContactSelected = { contact ->
                    onCollectorAddressChange(contact.destinationHash.lowercase())
                    showContactPicker = false
                },
                onUnset = {
                    onCollectorAddressChange(null)
                    showContactPicker = false
                },
                onDismiss = { showContactPicker = false },
                localDestinationHash = localDestinationHash,
                localDisplayName = localDisplayName,
                localIconName = localIconName,
                localIconForegroundColor = localIconForegroundColor,
                localIconBackgroundColor = localIconBackgroundColor,
                onSelfSelected = {
                    if (localDestinationHash != null) {
                        onCollectorAddressChange(localDestinationHash.lowercase())
                        showContactPicker = false
                    }
                },
            )
        }

        // Send interval chips
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.settings_location_sharing_send_every, localizedTelemetryIntervalDisplay(sendIntervalSeconds)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TelemetryIntervalChip(
                    label = stringResource(R.string.settings_interval_five_minutes_short),
                    selected = sendIntervalSeconds == 300,
                    enabled = enabled && collectorAddress != null,
                    onClick = { onSendIntervalChange(300) },
                )
                TelemetryIntervalChip(
                    label = stringResource(R.string.settings_interval_fifteen_minutes_short),
                    selected = sendIntervalSeconds == 900,
                    enabled = enabled && collectorAddress != null,
                    onClick = { onSendIntervalChange(900) },
                )
                TelemetryIntervalChip(
                    label = stringResource(R.string.settings_interval_thirty_minutes_short),
                    selected = sendIntervalSeconds == 1800,
                    enabled = enabled && collectorAddress != null,
                    onClick = { onSendIntervalChange(1800) },
                )
                TelemetryIntervalChip(
                    label = stringResource(R.string.settings_interval_one_hour_short),
                    selected = sendIntervalSeconds == 3600,
                    enabled = enabled && collectorAddress != null,
                    onClick = { onSendIntervalChange(3600) },
                )
            }
        }

        // Send Now button with last send time
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onSendNow,
                enabled = enabled && !isSending && collectorAddress != null,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                    ),
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_location_sharing_sending))
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_location_sharing_send_now))
                }
            }
            // Last send timestamp with periodic refresh
            if (lastSendTime != null) {
                var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(5_000)
                        currentTime = System.currentTimeMillis()
                    }
                }
                Text(
                    text = stringResource(R.string.settings_location_sharing_last_sent, localizedTelemetryRelativeTime(lastSendTime, currentTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Divider between send and receive sections
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        // Request toggle (receive from collector)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = null,
                        indication = null,
                        enabled = collectorAddress != null,
                    ) { onRequestEnabledChange(!requestEnabled) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_location_sharing_receive_locations),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color =
                        if (collectorAddress != null) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
                Text(
                    text = stringResource(R.string.settings_location_sharing_receive_locations_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = requestEnabled,
                onCheckedChange = null,
                enabled = collectorAddress != null,
            )
        }

        // Request interval chips
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.settings_location_sharing_request_every, localizedTelemetryIntervalDisplay(requestIntervalSeconds)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color =
                    if (requestEnabled && collectorAddress != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TelemetryIntervalChip(
                    label = stringResource(R.string.settings_interval_five_minutes_short),
                    selected = requestIntervalSeconds == 300,
                    enabled = requestEnabled && collectorAddress != null,
                    onClick = { onRequestIntervalChange(300) },
                )
                TelemetryIntervalChip(
                    label = stringResource(R.string.settings_interval_fifteen_minutes_short),
                    selected = requestIntervalSeconds == 900,
                    enabled = requestEnabled && collectorAddress != null,
                    onClick = { onRequestIntervalChange(900) },
                )
                TelemetryIntervalChip(
                    label = stringResource(R.string.settings_interval_thirty_minutes_short),
                    selected = requestIntervalSeconds == 1800,
                    enabled = requestEnabled && collectorAddress != null,
                    onClick = { onRequestIntervalChange(1800) },
                )
                TelemetryIntervalChip(
                    label = stringResource(R.string.settings_interval_one_hour_short),
                    selected = requestIntervalSeconds == 3600,
                    enabled = requestEnabled && collectorAddress != null,
                    onClick = { onRequestIntervalChange(3600) },
                )
            }
        }

        // Request Now button with last request time
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = {
                    android.util.Log.d("LocationSharingCard", "Request Now clicked! collectorAddress=$collectorAddress, isRequesting=$isRequesting")
                    onRequestNow()
                },
                enabled = !isRequesting && collectorAddress != null,
            ) {
                if (isRequesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_location_sharing_requesting))
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_location_sharing_request_now))
                }
            }
            // Last request timestamp with periodic refresh
            if (lastRequestTime != null) {
                var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(5_000)
                        currentTime = System.currentTimeMillis()
                    }
                }
                Text(
                    text = stringResource(R.string.settings_location_sharing_last_received, localizedTelemetryRelativeTime(lastRequestTime, currentTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Divider between receive and host sections
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        // Host mode toggle (act as collector for others)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = null,
                        indication = null,
                    ) { onHostModeEnabledChange(!hostModeEnabled) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_location_sharing_host_group),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.settings_location_sharing_host_group_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = hostModeEnabled,
                onCheckedChange = null,
            )
        }

        // Allowed requesters section (only visible when host mode is enabled)
        if (hostModeEnabled) {
            Spacer(modifier = Modifier.height(8.dp))

            AllowedRequestersSection(
                allowedRequesters = allowedRequesters,
                contacts = contacts,
                onEditClick = { showAllowedRequestersDialog = true },
            )
        }
    }

    // Allowed requesters dialog
    if (showAllowedRequestersDialog) {
        AllowedRequestersDialog(
            contacts = contacts,
            allowedRequesters = allowedRequesters,
            onDismiss = { showAllowedRequestersDialog = false },
            onConfirm = { selectedHashes ->
                onAllowedRequestersChange(selectedHashes)
                showAllowedRequestersDialog = false
            },
        )
    }
}

@Composable
private fun AllowedRequestersSection(
    allowedRequesters: Set<String>,
    contacts: List<EnrichedContact>,
    onEditClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Header row with Edit button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.settings_location_sharing_allowed_requesters),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.settings_location_sharing_edit_allowed_requesters),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Warning when no contacts are selected (blocks all)
        if (allowedRequesters.isEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(R.string.settings_location_sharing_no_contacts_selected),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            // Show count and list of selected contacts
            val selectedContacts = contacts.filter { it.destinationHash in allowedRequesters }
            val displayNames = selectedContacts.map { it.displayName }.take(3)
            val remaining = selectedContacts.size - displayNames.size

            val displayText =
                if (remaining > 0) {
                    stringResource(R.string.settings_location_sharing_more_contacts, displayNames.joinToString(", "), remaining)
                } else {
                    displayNames.joinToString(", ")
                }

            Text(
                text = displayText.ifEmpty { stringResource(R.string.settings_location_sharing_selected_count, allowedRequesters.size) },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.settings_location_sharing_allowed_requesters_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun AllowedRequestersDialog(
    contacts: List<EnrichedContact>,
    allowedRequesters: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
) {
    var selectedHashes by remember { mutableStateOf(allowedRequesters) }
    var searchQuery by remember { mutableStateOf("") }

    // Filter contacts by search query, deduplicate to prevent LazyColumn key crash (#542)
    val filteredContacts =
        remember(contacts, searchQuery) {
            val base =
                if (searchQuery.isBlank()) {
                    contacts
                } else {
                    contacts.filter { contact ->
                        contact.displayName.contains(searchQuery, ignoreCase = true) ||
                            contact.destinationHash.contains(searchQuery, ignoreCase = true)
                    }
                }
            base.distinctBy { it.destinationHash }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_location_sharing_select_allowed_requesters)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Description
                Text(
                    text = stringResource(R.string.settings_location_sharing_select_allowed_requesters_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.settings_location_sharing_search_contacts)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                // Contact list
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filteredContacts, key = { it.destinationHash }) { contact ->
                        ContactSelectionRow(
                            contact = contact,
                            isSelected = contact.destinationHash in selectedHashes,
                            onSelectionChange = { selected ->
                                selectedHashes =
                                    if (selected) {
                                        selectedHashes + contact.destinationHash
                                    } else {
                                        selectedHashes - contact.destinationHash
                                    }
                            },
                        )
                    }
                }

                // Show count
                if (selectedHashes.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.settings_location_sharing_contacts_selected, selectedHashes.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedHashes) }) {
                Text(stringResource(R.string.common_done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

@Composable
private fun ContactSelectionRow(
    contact: EnrichedContact,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
) {
    val hashBytes =
        contact.destinationHash
            .chunked(2)
            .mapNotNull { it.toIntOrNull(16)?.toByte() }
            .toByteArray()

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onSelectionChange(!isSelected) }
                .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileIcon(
            iconName = contact.iconName,
            foregroundColor = contact.iconForegroundColor,
            backgroundColor = contact.iconBackgroundColor,
            size = 40.dp,
            fallbackHash = hashBytes,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = contact.displayName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )

        Checkbox(
            checked = isSelected,
            onCheckedChange = onSelectionChange,
        )
    }
}

@Composable
private fun TelemetryIntervalChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(label) },
    )
}

@Composable
private fun localizedTelemetryRelativeTime(
    timestamp: Long,
    now: Long = System.currentTimeMillis(),
): String {
    val diff = now - timestamp

    return when {
        diff < 5_000 -> stringResource(R.string.common_relative_time_just_now)
        diff < 60_000 -> pluralStringResource(R.plurals.common_relative_time_seconds_ago, (diff / 1000).toInt(), (diff / 1000).toInt())
        diff < 120_000 -> stringResource(R.string.common_relative_time_one_minute)
        diff < 3600_000 -> pluralStringResource(R.plurals.common_relative_time_minutes_ago, (diff / 60_000).toInt(), (diff / 60_000).toInt())
        diff < 7200_000 -> stringResource(R.string.common_relative_time_one_hour)
        diff < 86400_000 -> pluralStringResource(R.plurals.common_relative_time_hours_ago, (diff / 3600_000).toInt(), (diff / 3600_000).toInt())
        else -> pluralStringResource(R.plurals.common_relative_time_days_ago, (diff / 86400_000).toInt(), (diff / 86400_000).toInt())
    }
}

@Composable
private fun localizedTelemetryIntervalDisplay(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return when {
        seconds < 60 -> stringResource(R.string.settings_interval_seconds_short, seconds)
        hours > 0 && minutes == 0 && secs == 0 -> stringResource(R.string.settings_interval_hours_short, hours)
        hours > 0 -> stringResource(R.string.settings_interval_hours_minutes_short, hours, minutes)
        secs == 0 -> stringResource(R.string.settings_interval_minutes_short, minutes)
        else -> stringResource(R.string.settings_interval_minutes_seconds_short, minutes, secs)
    }
}

/**
 * Dialog for selecting a contact as the group host/collector.
 */
@Composable
@Suppress("LongParameterList") // UI composable with display props
private fun GroupHostPickerDialog(
    contacts: List<EnrichedContact>,
    selectedHash: String?,
    onContactSelected: (EnrichedContact) -> Unit,
    onUnset: () -> Unit,
    onDismiss: () -> Unit,
    localDestinationHash: String? = null,
    localDisplayName: String = "Myself",
    localIconName: String? = null,
    localIconForegroundColor: String? = null,
    localIconBackgroundColor: String? = null,
    onSelfSelected: () -> Unit = {},
) {
    val isSelfSelected =
        localDestinationHash != null &&
            selectedHash.equals(localDestinationHash, ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_location_sharing_select_group_host)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Caution text
                Text(
                    text = stringResource(R.string.settings_location_sharing_select_group_host_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (contacts.isEmpty() && localDestinationHash == null) {
                    Text(
                        text = stringResource(R.string.settings_location_sharing_no_contacts),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 350.dp),
                    ) {
                        // "Myself" option at the top
                        if (localDestinationHash != null) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable(onClick = onSelfSelected)
                                            .padding(horizontal = 8.dp, vertical = 12.dp),
                                ) {
                                    val hashBytes =
                                        localDestinationHash
                                            .chunked(2)
                                            .mapNotNull { it.toIntOrNull(16)?.toByte() }
                                            .toByteArray()
                                    ProfileIcon(
                                        iconName = localIconName,
                                        foregroundColor = localIconForegroundColor,
                                        backgroundColor = localIconBackgroundColor,
                                        size = 40.dp,
                                        fallbackHash = hashBytes,
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = stringResource(R.string.settings_location_sharing_self_suffix, localDisplayName),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color =
                                            if (isSelfSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                        fontWeight = if (isSelfSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                HorizontalDivider()
                            }
                        }
                        // "None" option to unset the group host
                        if (selectedHash != null) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable(onClick = onUnset)
                                            .padding(horizontal = 8.dp, vertical = 12.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(40.dp),
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = stringResource(R.string.common_none),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                HorizontalDivider()
                            }
                        }
                        items(contacts.sortedBy { it.displayName.lowercase() }) { contact ->
                            GroupHostContactRow(
                                contact = contact,
                                isSelected = contact.destinationHash.equals(selectedHash, ignoreCase = true),
                                onClick = { onContactSelected(contact) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

/**
 * A clickable row displaying a contact for single selection.
 */
@Composable
private fun GroupHostContactRow(
    contact: EnrichedContact,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 12.dp),
    ) {
        val hashBytes =
            contact.destinationHash
                .chunked(2)
                .mapNotNull { it.toIntOrNull(16)?.toByte() }
                .toByteArray()

        ProfileIcon(
            iconName = contact.iconName,
            foregroundColor = contact.iconForegroundColor,
            backgroundColor = contact.iconBackgroundColor,
            size = 40.dp,
            fallbackHash = hashBytes,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = contact.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color =
                if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
    }
}
