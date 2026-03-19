package com.lxmf.messenger.ui.screens.flasher.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lxmf.messenger.R
import com.lxmf.messenger.reticulum.flasher.FrequencyBand
import com.lxmf.messenger.reticulum.flasher.RNodeDeviceInfo
import com.lxmf.messenger.viewmodel.FlashResult

/**
 * Step 5: Flash Complete
 *
 * Shows the result of the flash operation and provides next actions:
 * - Success: Device info, "Flash Another", "Configure RNode", "Done"
 * - Failure: Error details, "Retry", "Done"
 * - Cancelled: Confirmation and options
 */
@Composable
fun FlashCompleteStep(
    result: FlashResult?,
    onFlashAnother: () -> Unit,
    onConfigureRNode: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val unknownStateLabel = stringResource(R.string.flash_complete_unknown_state)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        when (result) {
            is FlashResult.Success ->
                SuccessContent(
                    deviceInfo = result.deviceInfo,
                    onFlashAnother = onFlashAnother,
                    onConfigureRNode = onConfigureRNode,
                    onDone = onDone,
                )
            is FlashResult.Failure ->
                FailureContent(
                    error = result.error,
                    onFlashAnother = onFlashAnother,
                    onDone = onDone,
                )
            is FlashResult.Cancelled ->
                CancelledContent(
                    onFlashAnother = onFlashAnother,
                    onDone = onDone,
                )
            null -> {
                Text(unknownStateLabel)
            }
        }
    }
}

@Composable
private fun SuccessContent(
    deviceInfo: RNodeDeviceInfo?,
    onFlashAnother: () -> Unit,
    onConfigureRNode: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val flashSuccessfulLabel = stringResource(R.string.flash_complete_success_title)
    val flashSuccessfulMessage = stringResource(R.string.flash_complete_success_message)
    val deviceInformationLabel = stringResource(R.string.flash_complete_device_information)
    val boardLabel = stringResource(R.string.flash_complete_board)
    val firmwareLabel = stringResource(R.string.flash_complete_firmware)
    val bandLabel = stringResource(R.string.flash_complete_band)
    val configureRNodeLabel = stringResource(R.string.flash_complete_configure_rnode)
    val flashAnotherDeviceLabel = stringResource(R.string.flash_complete_flash_another)
    val doneLabel = stringResource(R.string.flash_complete_done)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Success icon
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = flashSuccessfulLabel,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = flashSuccessfulMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        // Device info card
        if (deviceInfo != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
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
                    deviceInfo.firmwareVersion?.let { version ->
                        DeviceInfoRow(firmwareLabel, "v$version")
                    }
                    val band = FrequencyBand.fromModelCode(deviceInfo.model)
                    if (band != FrequencyBand.UNKNOWN) {
                        DeviceInfoRow(bandLabel, band.displayName)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Button(
            onClick = onConfigureRNode,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(configureRNodeLabel)
        }

        OutlinedButton(
            onClick = onFlashAnother,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(flashAnotherDeviceLabel)
        }

        TextButton(
            onClick = onDone,
        ) {
            Text(doneLabel)
        }
    }
}

@Composable
private fun FailureContent(
    error: String,
    onFlashAnother: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val flashFailedLabel = stringResource(R.string.flash_complete_failure_title)
    val errorDetailsLabel = stringResource(R.string.flash_complete_error_details)
    val recoveryTipsLabel = stringResource(R.string.flash_complete_recovery_tips)
    val recoveryTipsMessage = stringResource(R.string.flash_complete_recovery_tips_message)
    val tryAgainLabel = stringResource(R.string.flash_complete_try_again)
    val doneLabel = stringResource(R.string.flash_complete_done)

    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Error icon
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error,
        )

        Text(
            text = flashFailedLabel,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
        )

        // Error details card
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = errorDetailsLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }

        // Recovery tips
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = recoveryTipsLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recoveryTipsMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Button(
            onClick = onFlashAnother,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(tryAgainLabel)
        }

        TextButton(
            onClick = onDone,
        ) {
            Text(doneLabel)
        }
    }
}

@Composable
private fun CancelledContent(
    onFlashAnother: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val flashCancelledLabel = stringResource(R.string.flash_complete_cancelled_title)
    val flashCancelledMessage = stringResource(R.string.flash_complete_cancelled_message)
    val flashCancelledWarning = stringResource(R.string.flash_complete_cancelled_warning)
    val tryAgainLabel = stringResource(R.string.flash_complete_try_again)
    val doneLabel = stringResource(R.string.flash_complete_done)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Cancel icon
        Icon(
            imageVector = Icons.Default.Cancel,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline,
        )

        Text(
            text = flashCancelledLabel,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = flashCancelledMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        // Warning card
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = flashCancelledWarning,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(16.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Button(
            onClick = onFlashAnother,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(tryAgainLabel)
        }

        TextButton(
            onClick = onDone,
        ) {
            Text(doneLabel)
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
