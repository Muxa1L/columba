package com.lxmf.messenger.ui.screens.flasher.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lxmf.messenger.R
import com.lxmf.messenger.reticulum.flasher.FrequencyBand
import com.lxmf.messenger.reticulum.flasher.RNodeBoard
import com.lxmf.messenger.reticulum.flasher.RNodeDeviceInfo

/**
 * Step 2: Device Detection
 *
 * Shows the detection process and results.
 * If detection fails, allows manual board selection.
 */
@Composable
fun DeviceDetectionStep(
    isDetecting: Boolean,
    detectedInfo: RNodeDeviceInfo?,
    detectionError: String?,
    detectionMessage: String,
    onManualSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(R.string.device_detection_title)
    val subtitle = stringResource(R.string.device_detection_subtitle)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            isDetecting -> DetectingState(message = detectionMessage)
            detectedInfo != null && detectedInfo.board != RNodeBoard.UNKNOWN ->
                DetectedState(deviceInfo = detectedInfo)
            detectedInfo != null && detectedInfo.board == RNodeBoard.UNKNOWN ->
                UnknownBoardState(
                    deviceInfo = detectedInfo,
                    onManualSelection = onManualSelection,
                )
            detectionError != null ->
                ErrorState(
                    error = detectionError,
                    onManualSelection = onManualSelection,
                )
        }
    }
}

@Composable
private fun DetectingState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                strokeWidth = 4.dp,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DetectedState(
    deviceInfo: RNodeDeviceInfo,
    modifier: Modifier = Modifier,
) {
    val deviceDetectedLabel = stringResource(R.string.device_detection_device_detected)
    val deviceInformationLabel = stringResource(R.string.device_detection_device_information)
    val boardLabel = stringResource(R.string.flash_complete_board)
    val platformLabel = stringResource(R.string.device_detection_platform)
    val mcuLabel = stringResource(R.string.device_detection_mcu)
    val firmwareLabel = stringResource(R.string.flash_complete_firmware)
    val bandLabel = stringResource(R.string.device_detection_frequency_band)
    val serialLabel = stringResource(R.string.device_detection_serial)
    val statusLabel = stringResource(R.string.interface_stats_status)
    val provisionedLabel = stringResource(R.string.device_detection_provisioned)
    val notProvisionedLabel = stringResource(R.string.device_detection_not_provisioned)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Success card
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = deviceDetectedLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = deviceInfo.board.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        // Device details card
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.DeveloperBoard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = deviceInformationLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                DeviceInfoRow(boardLabel, deviceInfo.board.displayName)
                DeviceInfoRow(platformLabel, deviceInfo.platform.name)
                DeviceInfoRow(mcuLabel, deviceInfo.mcu.name)

                deviceInfo.firmwareVersion?.let { version ->
                    DeviceInfoRow(firmwareLabel, stringResource(R.string.firmware_selection_version_value, version))
                }

                val band = FrequencyBand.fromModelCode(deviceInfo.model)
                if (band != FrequencyBand.UNKNOWN) {
                    DeviceInfoRow(bandLabel, band.displayName)
                }

                deviceInfo.serialNumber?.let { serial ->
                    DeviceInfoRow(serialLabel, "#$serial")
                }

                DeviceInfoRow(
                    statusLabel,
                    if (deviceInfo.isProvisioned) provisionedLabel else notProvisionedLabel,
                )
            }
        }
    }
}

@Composable
private fun DeviceInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun UnknownBoardState(
    deviceInfo: RNodeDeviceInfo,
    onManualSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectBoardTypeLabel = stringResource(R.string.device_detection_select_board_type)
    val selectBoardTypeMessage = stringResource(R.string.device_detection_select_board_message)
    val continueManualLabel = stringResource(R.string.device_detection_continue_manual)
    val deviceDetectedLabel = stringResource(R.string.device_detection_device_detected)
    val unknownBoardLabel = stringResource(R.string.device_detection_unknown_board_message)
    val deviceInformationLabel = stringResource(R.string.device_detection_device_information)
    val platformLabel = stringResource(R.string.device_detection_platform)
    val mcuLabel = stringResource(R.string.device_detection_mcu)
    val firmwareLabel = stringResource(R.string.flash_complete_firmware)
    val statusLabel = stringResource(R.string.interface_stats_status)
    val notProvisionedLabel = stringResource(R.string.device_detection_not_provisioned)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Warning card - device detected but board unknown
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.DeveloperBoard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = deviceDetectedLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Text(
                        text = unknownBoardLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }

        // Device details card
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.DeveloperBoard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = deviceInformationLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                DeviceInfoRow(platformLabel, deviceInfo.platform.name)
                DeviceInfoRow(mcuLabel, deviceInfo.mcu.name)

                deviceInfo.firmwareVersion?.let { version ->
                    DeviceInfoRow(firmwareLabel, stringResource(R.string.firmware_selection_version_value, version))
                }

                DeviceInfoRow(statusLabel, notProvisionedLabel)
            }
        }

        // Manual selection option
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectBoardTypeLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Text(
                    text = selectBoardTypeMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(
                    onClick = onManualSelection,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(continueManualLabel)
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onManualSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val detectionFailedLabel = stringResource(R.string.device_detection_failed)
    val manualSelectionLabel = stringResource(R.string.device_detection_manual_selection)
    val manualSelectionMessage = stringResource(R.string.device_detection_manual_selection_message)
    val continueManualLabel = stringResource(R.string.device_detection_continue_manual)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Error card
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = detectionFailedLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }

        // Manual selection option
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = manualSelectionLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Text(
                    text = manualSelectionMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(
                    onClick = onManualSelection,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(continueManualLabel)
                }
            }
        }
    }
}
