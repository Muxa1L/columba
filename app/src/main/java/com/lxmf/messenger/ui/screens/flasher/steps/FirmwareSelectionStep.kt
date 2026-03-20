package com.lxmf.messenger.ui.screens.flasher.steps

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lxmf.messenger.R
import com.lxmf.messenger.reticulum.flasher.FirmwarePackage
import com.lxmf.messenger.reticulum.flasher.FirmwareSource
import com.lxmf.messenger.reticulum.flasher.FrequencyBand
import com.lxmf.messenger.reticulum.flasher.RNodeBoard

/**
 * Step 3: Firmware Selection
 *
 * Allows selection of board type, frequency band, and firmware version.
 * Shows cached firmware and option to download new versions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirmwareSelectionStep(
    availableFirmwareSources: List<FirmwareSource>?,
    selectedFirmwareSource: FirmwareSource,
    customFirmwareUri: Uri?,
    customFirmwareUrl: String,
    selectedBoard: RNodeBoard?,
    selectedBand: FrequencyBand,
    bandExplicitlySelected: Boolean,
    availableFirmware: List<FirmwarePackage>,
    selectedFirmware: FirmwarePackage?,
    availableVersions: List<String>,
    selectedVersion: String?,
    isDownloading: Boolean,
    downloadProgress: Int,
    downloadError: String?,
    useManualSelection: Boolean,
    onFirmwareSourceSelected: (FirmwareSource) -> Unit,
    onCustomUrlChanged: (String) -> Unit,
    onPickFile: () -> Unit,
    onBoardSelected: (RNodeBoard) -> Unit,
    onBandSelected: (FrequencyBand) -> Unit,
    onFirmwareSelected: (FirmwarePackage) -> Unit,
    onDownloadFirmware: (String) -> Unit,
    onProvisionOnly: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val title = stringResource(R.string.firmware_selection_title)
    val subtitle = stringResource(R.string.firmware_selection_subtitle)
    val microReticulumTitle = stringResource(R.string.firmware_selection_about_microreticulum_title)
    val microReticulumMessage = stringResource(R.string.firmware_selection_about_microreticulum_message)
    val alreadyFlashedLabel = stringResource(R.string.firmware_selection_already_flashed)
    val alreadyFlashedMessage = stringResource(R.string.firmware_selection_already_flashed_message)
    val provisionOnlyLabel = stringResource(R.string.firmware_selection_provision_only)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
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

        // Firmware source selection
        FirmwareSourceCard(
            selectedSource = selectedFirmwareSource,
            availableSources = availableFirmwareSources,
            onSourceSelected = onFirmwareSourceSelected,
        )

        // microReticulum info note
        if (selectedFirmwareSource is FirmwareSource.MicroReticulum) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = microReticulumTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = microReticulumMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Board selection (only if manual selection enabled)
        if (useManualSelection) {
            BoardSelectionCard(
                selectedBoard = selectedBoard,
                onBoardSelected = onBoardSelected,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Frequency band selection
        FrequencyBandCard(
            selectedBand = selectedBand,
            bandExplicitlySelected = bandExplicitlySelected,
            onBandSelected = onBandSelected,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Download error
        if (downloadError != null) {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
            ) {
                Text(
                    text = downloadError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        // Download progress
        if (isDownloading) {
            DownloadProgressCard(progress = downloadProgress)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Custom firmware input (URL or local file)
        if (selectedFirmwareSource == FirmwareSource.Custom) {
            CustomFirmwareCard(
                customFirmwareUri = customFirmwareUri,
                customFirmwareUrl = customFirmwareUrl,
                onCustomUrlChanged = onCustomUrlChanged,
                onPickFile = onPickFile,
            )
        } else {
            // Version selection / cached firmware
            FirmwareVersionCard(
                selectedBoard = selectedBoard,
                availableFirmware = availableFirmware,
                selectedFirmware = selectedFirmware,
                availableVersions = availableVersions,
                selectedVersion = selectedVersion,
                isDownloading = isDownloading,
                onFirmwareSelected = onFirmwareSelected,
                onDownloadFirmware = onDownloadFirmware,
            )
        }

        // Provision only option (skip flashing)
        if (selectedBoard != null) {
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = alreadyFlashedLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alreadyFlashedMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    androidx.compose.material3.OutlinedButton(
                        onClick = onProvisionOnly,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(provisionOnlyLabel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoardSelectionCard(
    selectedBoard: RNodeBoard?,
    onBoardSelected: (RNodeBoard) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val boardTypeLabel = stringResource(R.string.firmware_selection_board_type)
    val selectBoardLabel = stringResource(R.string.firmware_selection_select_board)

    // Filter to flashable boards
    val boards =
        RNodeBoard.entries.filter {
            it != RNodeBoard.UNKNOWN && it.platform != com.lxmf.messenger.reticulum.flasher.RNodePlatform.AVR
        }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = boardTypeLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                OutlinedTextField(
                    value = selectedBoard?.displayName ?: selectBoardLabel,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    boards.forEach { board ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(board.displayName)
                                    Text(
                                        text = board.platform.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                            onClick = {
                                onBoardSelected(board)
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FrequencyBandCard(
    selectedBand: FrequencyBand,
    bandExplicitlySelected: Boolean,
    onBandSelected: (FrequencyBand) -> Unit,
    modifier: Modifier = Modifier,
) {
    val frequencyBandLabel = stringResource(R.string.firmware_selection_frequency_band)
    val requiredLabel = stringResource(R.string.firmware_selection_required)
    val frequencyRequiredHelp = stringResource(R.string.firmware_selection_frequency_required_help)
    val frequencyHelp = stringResource(R.string.firmware_selection_frequency_help)

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (!bandExplicitlySelected) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RadioButtonChecked,
                    contentDescription = null,
                    tint =
                        if (!bandExplicitlySelected) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = frequencyBandLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                if (!bandExplicitlySelected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = requiredLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Only show as "selected" if user has explicitly confirmed
                FilterChip(
                    selected = bandExplicitlySelected && selectedBand == FrequencyBand.BAND_868_915,
                    onClick = { onBandSelected(FrequencyBand.BAND_868_915) },
                    label = { Text(stringResource(R.string.firmware_selection_band_868_915)) },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                )
                FilterChip(
                    selected = bandExplicitlySelected && selectedBand == FrequencyBand.BAND_433,
                    onClick = { onBandSelected(FrequencyBand.BAND_433) },
                    label = { Text(stringResource(R.string.firmware_selection_band_433)) },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                )
            }

            Text(
                text =
                    if (!bandExplicitlySelected) {
                        frequencyRequiredHelp
                    } else {
                        frequencyHelp
                    },
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (!bandExplicitlySelected) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun DownloadProgressCard(
    progress: Int,
    modifier: Modifier = Modifier,
) {
    val downloadingFirmwareLabel = stringResource(R.string.firmware_selection_downloading_firmware)

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = downloadingFirmwareLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.common_percent_value, progress),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FirmwareVersionCard(
    selectedBoard: RNodeBoard?,
    availableFirmware: List<FirmwarePackage>,
    selectedFirmware: FirmwarePackage?,
    availableVersions: List<String>,
    selectedVersion: String?,
    isDownloading: Boolean,
    onFirmwareSelected: (FirmwarePackage) -> Unit,
    onDownloadFirmware: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firmwareVersionLabel = stringResource(R.string.firmware_selection_version)
    val selectBoardFirstLabel = stringResource(R.string.firmware_selection_select_board_first)
    val noFirmwareAvailableLabel = stringResource(R.string.firmware_selection_no_firmware_available)
    val cachedFirmwareLabel = stringResource(R.string.firmware_selection_cached_firmware)
    val availableDownloadLabel = stringResource(R.string.firmware_selection_available_download)
    val selectVersionLabel = stringResource(R.string.firmware_selection_select_version)
    val selectVersionContinueLabel = stringResource(R.string.firmware_selection_select_version_continue)

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = firmwareVersionLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            if (selectedBoard == null) {
                Text(
                    text = selectBoardFirstLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (availableFirmware.isEmpty() && availableVersions.isEmpty()) {
                Text(
                    text = noFirmwareAvailableLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                // Show cached firmware
                if (availableFirmware.isNotEmpty()) {
                    Text(
                        text = cachedFirmwareLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    availableFirmware.forEach { firmware ->
                        FilterChip(
                            selected = selectedFirmware == firmware,
                            onClick = { onFirmwareSelected(firmware) },
                            label = {
                                Text(stringResource(R.string.firmware_selection_version_value, firmware.version))
                            },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Show available versions for download
                if (availableVersions.isNotEmpty()) {
                    Text(
                        text = availableDownloadLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                    ) {
                        OutlinedTextField(
                            value = selectedVersion ?: selectVersionLabel,
                            onValueChange = {},
                            readOnly = true,
                            enabled = !isDownloading,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            availableVersions.forEach { version ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.firmware_selection_version_value, version)) },
                                    onClick = {
                                        onDownloadFirmware(version)
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Show selected firmware info OR selected version for download
            when {
                selectedFirmware != null -> {
                    val selectedSummaryLabel = stringResource(
                        R.string.firmware_selection_selected_summary,
                        selectedFirmware.board.displayName,
                        selectedFirmware.version,
                    )
                    val selectedBandPlatformLabel = stringResource(
                        R.string.firmware_selection_selected_band_platform,
                        selectedFirmware.frequencyBand.displayName,
                        selectedFirmware.platform.name,
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = selectedSummaryLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = selectedBandPlatformLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                selectedVersion != null && selectedBoard != null -> {
                    val willDownloadLabel = stringResource(R.string.firmware_selection_will_download, selectedVersion)
                    val willDownloadHelp = stringResource(R.string.firmware_selection_will_download_help)

                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            ),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = willDownloadLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                            Text(
                                text = willDownloadHelp,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
                selectedBoard != null && availableFirmware.isNotEmpty() || availableVersions.isNotEmpty() -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = selectVersionContinueLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun FirmwareSourceCard(
    selectedSource: FirmwareSource,
    availableSources: List<FirmwareSource>?,
    onSourceSelected: (FirmwareSource) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firmwareSourceLabel = stringResource(R.string.firmware_selection_source)

    val sources =
        availableSources ?: listOf(
            FirmwareSource.Official,
            FirmwareSource.MicroReticulum,
            FirmwareSource.CommunityEdition,
            FirmwareSource.Custom,
        )

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = firmwareSourceLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                sources.forEach { source ->
                    FilterChip(
                        selected = selectedSource == source,
                        onClick = { onSourceSelected(source) },
                        label = { Text(source.displayName) },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomFirmwareCard(
    customFirmwareUri: Uri?,
    customFirmwareUrl: String,
    onCustomUrlChanged: (String) -> Unit,
    onPickFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val customFirmwareLabel = stringResource(R.string.firmware_selection_custom)
    val downloadFromUrlLabel = stringResource(R.string.firmware_selection_download_from_url)
    val firmwareUrlLabel = stringResource(R.string.firmware_selection_url_label)
    val firmwareUrlPlaceholder = stringResource(R.string.firmware_selection_url_placeholder)
    val downloadOnStartLabel = stringResource(R.string.firmware_selection_download_on_start)
    val pickLocalFileLabel = stringResource(R.string.firmware_selection_pick_local_file)
    val pickZipFileLabel = stringResource(R.string.firmware_selection_pick_zip_file)
    val customFirmwareFallback = stringResource(R.string.firmware_selection_custom_firmware_fallback)

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = customFirmwareLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // URL input
            Text(
                text = downloadFromUrlLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customFirmwareUrl,
                onValueChange = onCustomUrlChanged,
                label = { Text(firmwareUrlLabel) },
                placeholder = { Text(firmwareUrlPlaceholder) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (customFirmwareUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        ),
                ) {
                    Text(
                        text = downloadOnStartLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pickLocalFileLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))

            androidx.compose.material3.OutlinedButton(
                onClick = onPickFile,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(pickZipFileLabel)
            }

            if (customFirmwareUri != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                ) {
                    Text(
                        text = stringResource(
                            R.string.firmware_selection_file_selected,
                            customFirmwareUri.lastPathSegment ?: customFirmwareFallback,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}
