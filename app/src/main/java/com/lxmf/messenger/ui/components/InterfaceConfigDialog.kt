package com.lxmf.messenger.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.lxmf.messenger.R
import com.lxmf.messenger.reticulum.ble.model.BlePowerPreset
import com.lxmf.messenger.util.validation.ValidationConstants
import com.lxmf.messenger.viewmodel.InterfaceConfigState
import kotlinx.coroutines.launch

/**
 * Dialog for adding or editing a Reticulum network interface configuration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterfaceConfigDialog(
    configState: InterfaceConfigState,
    isEditing: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onConfigUpdate: (InterfaceConfigState) -> Unit,
) {
    val title = stringResource(if (isEditing) R.string.interface_config_edit_title else R.string.interface_config_add_title)
    val interfaceNameLabel = stringResource(R.string.interface_config_name_label)
    val interfaceNamePlaceholder = stringResource(R.string.interface_config_name_placeholder)
    val targetHostLabel = stringResource(R.string.interface_config_target_host_label)
    val targetHostPlaceholder = stringResource(R.string.interface_config_target_host_placeholder)
    val enabledLabel = stringResource(R.string.interface_config_enabled)
    val advancedOptionsLabel = stringResource(R.string.interface_config_advanced_options)
    val confirmLabel = stringResource(if (isEditing) R.string.interface_config_update else R.string.interface_config_add)
    val cancelLabel = stringResource(R.string.common_cancel)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Interface Name
                OutlinedTextField(
                    value = configState.name,
                    onValueChange = { newValue ->
                        // VALIDATION: Enforce interface name length limit
                        if (newValue.length <= ValidationConstants.MAX_INTERFACE_NAME_LENGTH) {
                            onConfigUpdate(configState.copy(name = newValue))
                        }
                    },
                    label = { Text(interfaceNameLabel) },
                    placeholder = { Text(interfaceNamePlaceholder) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = configState.nameError != null,
                    supportingText = {
                        val nameError = configState.nameError
                        if (nameError != null) {
                            Text(nameError)
                        } else {
                            Text("${configState.name.length}/${ValidationConstants.MAX_INTERFACE_NAME_LENGTH}")
                        }
                    },
                )

                // Interface Type Selector
                InterfaceTypeSelector(
                    selectedType = configState.type,
                    // Can't change type when editing
                    enabled = !isEditing,
                    onTypeChange = { onConfigUpdate(configState.copy(type = it)) },
                )

                // TCP Client Target Host (required field, shown by default)
                if (configState.type == "TCPClient") {
                    OutlinedTextField(
                        value = configState.targetHost,
                        onValueChange = { host ->
                            // Strip scheme prefixes and whitespace — only bare hostnames are valid
                            val cleaned =
                                host
                                    .trim()
                                    .removePrefix("http://")
                                    .removePrefix("https://")
                            onConfigUpdate(configState.copy(targetHost = cleaned))
                        },
                        label = { Text(targetHostLabel) },
                        placeholder = { Text(targetHostPlaceholder) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = configState.targetHostError != null,
                        supportingText = configState.targetHostError?.let { { Text(it) } },
                    )
                }

                // Enabled Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        enabledLabel,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Switch(
                        checked = configState.enabled,
                        onCheckedChange = { onConfigUpdate(configState.copy(enabled = it)) },
                    )
                }

                // Advanced Options (Expandable)
                var showAdvanced by remember { mutableStateOf(false) }

                Divider()

                OutlinedButton(
                    onClick = { showAdvanced = !showAdvanced },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = if (showAdvanced) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(advancedOptionsLabel)
                }

                AnimatedVisibility(visible = showAdvanced) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Type-specific configuration
                        when (configState.type) {
                            "AutoInterface" -> AutoInterfaceFields(configState, onConfigUpdate)
                            "TCPClient" -> TCPClientFields(configState, onConfigUpdate)
                            "TCPServer" -> TCPServerFields(configState, onConfigUpdate)
                            "AndroidBLE" -> AndroidBLEFields(configState, onConfigUpdate, scrollState)
                        }

                        Divider()

                        // Interface Mode
                        InterfaceModeSelector(
                            selectedMode = configState.mode,
                            onModeChange = { onConfigUpdate(configState.copy(mode = it)) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelLabel)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterfaceTypeSelector(
    selectedType: String,
    enabled: Boolean,
    onTypeChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val types =
        listOf(
            "AutoInterface" to stringResource(R.string.interface_config_type_auto_discovery),
            "TCPClient" to stringResource(R.string.interface_config_type_tcp_client),
            "TCPServer" to stringResource(R.string.interface_config_type_tcp_server),
            "AndroidBLE" to stringResource(R.string.interface_config_type_bluetooth_le),
        )
    val unknownLabel = stringResource(R.string.interface_config_unknown)
    val interfaceTypeLabel = stringResource(R.string.interface_config_type_label)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
    ) {
        OutlinedTextField(
            value = types.find { it.first == selectedType }?.second ?: unknownLabel,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(interfaceTypeLabel) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            types.forEach { (type, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onTypeChange(type)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun AutoInterfaceFields(
    configState: InterfaceConfigState,
    onConfigUpdate: (InterfaceConfigState) -> Unit,
) {
    val autoDiscoveryConfigurationLabel = stringResource(R.string.interface_config_auto_configuration)
    val groupIdLabel = stringResource(R.string.interface_config_group_id_label)
    val groupIdPlaceholder = stringResource(R.string.interface_config_group_id_placeholder)
    val discoveryPortLabel = stringResource(R.string.interface_config_discovery_port_label)
    val discoveryPortPlaceholder = stringResource(R.string.interface_config_discovery_port_placeholder)
    val dataPortLabel = stringResource(R.string.interface_config_data_port_label)
    val dataPortPlaceholder = stringResource(R.string.interface_config_data_port_placeholder)

    Text(
        autoDiscoveryConfigurationLabel,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )

    OutlinedTextField(
        value = configState.groupId,
        onValueChange = { onConfigUpdate(configState.copy(groupId = it)) },
        label = { Text(groupIdLabel) },
        placeholder = { Text(groupIdPlaceholder) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )

    DiscoveryScopeSelector(
        selectedScope = configState.discoveryScope,
        onScopeChange = { onConfigUpdate(configState.copy(discoveryScope = it)) },
    )

    OutlinedTextField(
        value = configState.discoveryPort,
        onValueChange = { onConfigUpdate(configState.copy(discoveryPort = it)) },
        label = { Text(discoveryPortLabel) },
        placeholder = { Text(discoveryPortPlaceholder) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = configState.discoveryPortError != null,
        supportingText = configState.discoveryPortError?.let { { Text(it) } },
    )

    OutlinedTextField(
        value = configState.dataPort,
        onValueChange = { onConfigUpdate(configState.copy(dataPort = it)) },
        label = { Text(dataPortLabel) },
        placeholder = { Text(dataPortPlaceholder) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = configState.dataPortError != null,
        supportingText = configState.dataPortError?.let { { Text(it) } },
    )
}

@Composable
fun TCPClientFields(
    configState: InterfaceConfigState,
    onConfigUpdate: (InterfaceConfigState) -> Unit,
) {
    val examplePort4242 = stringResource(R.string.interface_config_example_port_4242)
    val exampleLoopbackIp = stringResource(R.string.interface_config_example_loopback_ip)
    val examplePort9050 = stringResource(R.string.interface_config_example_port_9050)
    val tcpClientConfigurationLabel = stringResource(R.string.interface_config_tcp_client_configuration)
    val targetPortLabel = stringResource(R.string.interface_config_target_port_label)
    val networkNameLabel = stringResource(R.string.interface_config_network_name_label)
    val optionalLabel = stringResource(R.string.interface_config_optional)
    val networkNameHelp = stringResource(R.string.interface_config_network_name_help)
    val passphraseLabel = stringResource(R.string.interface_config_passphrase_label)
    val hidePassphrase = stringResource(R.string.interface_config_hide_passphrase)
    val showPassphrase = stringResource(R.string.interface_config_show_passphrase)
    val passphraseHelp = stringResource(R.string.interface_config_passphrase_help)
    val socksProxyLabel = stringResource(R.string.interface_config_socks_proxy_label)
    val socksProxyHelp = stringResource(R.string.interface_config_socks_proxy_help)
    val proxyHostLabel = stringResource(R.string.interface_config_proxy_host_label)
    val proxyHostHelp = stringResource(R.string.interface_config_proxy_host_help)
    val proxyPortLabel = stringResource(R.string.interface_config_proxy_port_label)
    val proxyPortHelp = stringResource(R.string.interface_config_proxy_port_help)

    Text(
        tcpClientConfigurationLabel,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )

    OutlinedTextField(
        value = configState.targetPort,
        onValueChange = { onConfigUpdate(configState.copy(targetPort = it)) },
        label = { Text(targetPortLabel) },
        placeholder = { Text(examplePort4242) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = configState.targetPortError != null,
        supportingText = configState.targetPortError?.let { { Text(it) } },
    )

    // Network Name (IFAC)
    OutlinedTextField(
        value = configState.networkName,
        onValueChange = { onConfigUpdate(configState.copy(networkName = it)) },
        label = { Text(networkNameLabel) },
        placeholder = { Text(optionalLabel) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        supportingText = {
            Text(
                networkNameHelp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )

    // Passphrase (IFAC)
    OutlinedTextField(
        value = configState.passphrase,
        onValueChange = { onConfigUpdate(configState.copy(passphrase = it)) },
        label = { Text(passphraseLabel) },
        placeholder = { Text(optionalLabel) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation =
            if (configState.passphraseVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
        trailingIcon = {
            IconButton(
                onClick = {
                    onConfigUpdate(configState.copy(passphraseVisible = !configState.passphraseVisible))
                },
            ) {
                Icon(
                    imageVector =
                        if (configState.passphraseVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                    contentDescription =
                        if (configState.passphraseVisible) {
                            hidePassphrase
                        } else {
                            showPassphrase
                        },
                )
            }
        },
        supportingText = {
            Text(
                passphraseHelp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )

    Divider()

    // SOCKS5 Proxy Toggle
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                socksProxyLabel,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                socksProxyHelp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = configState.socksProxyEnabled,
            onCheckedChange = { onConfigUpdate(configState.copy(socksProxyEnabled = it)) },
        )
    }

    AnimatedVisibility(visible = configState.socksProxyEnabled) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = configState.socksProxyHost,
                onValueChange = { host ->
                    onConfigUpdate(configState.copy(socksProxyHost = host.trim()))
                },
                label = { Text(proxyHostLabel) },
                placeholder = { Text(exampleLoopbackIp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = configState.socksProxyHostError != null,
                supportingText = {
                    val error = configState.socksProxyHostError
                    if (error != null) {
                        Text(error)
                    } else {
                        Text(
                            proxyHostHelp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )

            OutlinedTextField(
                value = configState.socksProxyPort,
                onValueChange = { onConfigUpdate(configState.copy(socksProxyPort = it)) },
                label = { Text(proxyPortLabel) },
                placeholder = { Text(examplePort9050) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = configState.socksProxyPortError != null,
                supportingText = {
                    val error = configState.socksProxyPortError
                    if (error != null) {
                        Text(error)
                    } else {
                        Text(
                            proxyPortHelp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScopeSelector(
    selectedScope: String,
    onScopeChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val scopes =
        listOf(
            "link" to stringResource(R.string.interface_config_scope_link),
            "admin" to stringResource(R.string.interface_config_scope_admin),
            "site" to stringResource(R.string.interface_config_scope_site),
            "organisation" to stringResource(R.string.interface_config_scope_organisation),
            "global" to stringResource(R.string.interface_config_scope_global),
        )
    val discoveryScopeLabel = stringResource(R.string.interface_config_discovery_scope_label)
    val defaultScopeLabel = stringResource(R.string.interface_config_scope_link)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = scopes.find { it.first == selectedScope }?.second ?: defaultScopeLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(discoveryScopeLabel) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            scopes.forEach { (scope, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onScopeChange(scope)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterfaceModeSelector(
    selectedMode: String,
    onModeChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val modes =
        listOf(
            "full" to stringResource(R.string.interface_config_mode_full),
            "gateway" to stringResource(R.string.interface_config_mode_gateway),
            "access_point" to stringResource(R.string.interface_config_mode_access_point),
            "roaming" to stringResource(R.string.interface_config_mode_roaming),
            "boundary" to stringResource(R.string.interface_config_mode_boundary),
        )
    val interfaceModeLabel = stringResource(R.string.interface_config_mode_label)
    val defaultModeLabel = stringResource(R.string.interface_config_mode_roaming)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = modes.find { it.first == selectedMode }?.second ?: defaultModeLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(interfaceModeLabel) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            modes.forEach { (mode, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onModeChange(mode)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun AndroidBLEFields(
    configState: InterfaceConfigState,
    onConfigUpdate: (InterfaceConfigState) -> Unit,
    scrollState: ScrollState? = null,
) {
    val exampleMaxConnections = stringResource(R.string.interface_config_example_max_connections)
    val coroutineScope = rememberCoroutineScope()
    val bleConfigurationLabel = stringResource(R.string.interface_config_ble_configuration)
    val deviceNameLabel = stringResource(R.string.interface_config_device_name_label)
    val deviceNamePlaceholder = stringResource(R.string.interface_config_device_name_placeholder)
    val deviceNameHelp = stringResource(R.string.interface_config_device_name_help)
    val maxConnectionsLabel = stringResource(R.string.interface_config_max_connections_label)
    val maxConnectionsHelp = stringResource(R.string.interface_config_max_connections_help)
    val powerProfileLabel = stringResource(R.string.interface_config_power_profile)
    val presets = listOf("performance", "balanced", "battery_saver", "custom")
    val labels = listOf(
        stringResource(R.string.interface_config_power_profile_performance),
        stringResource(R.string.interface_config_power_profile_balanced),
        stringResource(R.string.interface_config_power_profile_battery_saver),
        stringResource(R.string.interface_config_power_profile_custom),
    )
    val presetHelp = when (configState.blePowerPreset) {
        "performance" -> stringResource(R.string.interface_config_power_profile_performance_help)
        "balanced" -> stringResource(R.string.interface_config_power_profile_balanced_help)
        "battery_saver" -> stringResource(R.string.interface_config_power_profile_battery_saver_help)
        "custom" -> stringResource(R.string.interface_config_power_profile_custom_help)
        else -> ""
    }
    val activeScanSeconds = configState.bleDiscoveryIntervalMs.toLongOrNull()?.div(1000) ?: 5L
    val idleScanSeconds = configState.bleDiscoveryIntervalIdleMs.toLongOrNull()?.div(1000) ?: 30L
    val scanDurationSeconds = configState.bleScanDurationMs.toLongOrNull()?.div(1000) ?: 10L
    val advertisingRefreshSeconds = configState.bleAdvertisingRefreshIntervalMs.toLongOrNull()?.div(1000) ?: 60L

    Text(
        bleConfigurationLabel,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )

    OutlinedTextField(
        value = configState.deviceName,
        onValueChange = { newValue ->
            // VALIDATION: Enforce device name length limit
            if (newValue.length <= ValidationConstants.MAX_DEVICE_NAME_LENGTH) {
                onConfigUpdate(configState.copy(deviceName = newValue))
            }
        },
        label = { Text(deviceNameLabel) },
        placeholder = { Text(deviceNamePlaceholder) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = configState.deviceNameError != null,
        supportingText = {
            Column {
                configState.deviceNameError?.let { Text(it) }
                if (configState.deviceName.isNotEmpty()) {
                    Text(
                        stringResource(
                            R.string.interface_config_device_name_count,
                            configState.deviceName.length,
                            ValidationConstants.MAX_DEVICE_NAME_LENGTH,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    deviceNameHelp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )

    OutlinedTextField(
        value = configState.maxConnections,
        onValueChange = { onConfigUpdate(configState.copy(maxConnections = it)) },
        label = { Text(maxConnectionsLabel) },
        placeholder = { Text(exampleMaxConnections) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = configState.maxConnectionsError != null,
        supportingText = {
            Column {
                configState.maxConnectionsError?.let { Text(it) }
                Text(
                    maxConnectionsHelp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        powerProfileLabel,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )

    // Preset selector
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        presets.forEachIndexed { index, preset ->
            SegmentedButton(
                selected = configState.blePowerPreset == preset,
                onClick = {
                    val newState =
                        if (preset != "custom") {
                            val s = BlePowerPreset.getSettings(BlePowerPreset.fromString(preset))
                            configState.copy(
                                blePowerPreset = preset,
                                bleDiscoveryIntervalMs = s.discoveryIntervalMs.toString(),
                                bleDiscoveryIntervalIdleMs = s.discoveryIntervalIdleMs.toString(),
                                bleScanDurationMs = s.scanDurationMs.toString(),
                                bleAdvertisingRefreshIntervalMs = s.advertisingRefreshIntervalMs.toString(),
                            )
                        } else {
                            configState.copy(blePowerPreset = preset)
                        }
                    onConfigUpdate(newState)
                    if (preset == "custom" && scrollState != null) {
                        coroutineScope.launch {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }
                },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = presets.size),
                icon = {},
                label = { Text(labels[index], maxLines = 1, style = MaterialTheme.typography.labelSmall) },
            )
        }
    }

    // Helper text per preset
    Text(
        presetHelp,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    // Custom sliders (only enabled when preset is "custom")
    val isCustom = configState.blePowerPreset == "custom"

    Text(
        stringResource(R.string.interface_config_scan_interval_active, activeScanSeconds),
        style = MaterialTheme.typography.bodySmall,
    )
    Text(
        stringResource(R.string.interface_config_scan_interval_active_help),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Slider(
        value = (configState.bleDiscoveryIntervalMs.toFloatOrNull() ?: 5000f) / 1000f,
        onValueChange = { onConfigUpdate(configState.copy(bleDiscoveryIntervalMs = (it * 1000).toLong().toString())) },
        valueRange = 3f..30f,
        steps = 26,
        enabled = isCustom,
    )

    Text(
        stringResource(R.string.interface_config_scan_interval_idle, idleScanSeconds),
        style = MaterialTheme.typography.bodySmall,
    )
    Text(
        stringResource(R.string.interface_config_scan_interval_idle_help),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Slider(
        value = (configState.bleDiscoveryIntervalIdleMs.toFloatOrNull() ?: 30000f) / 1000f,
        onValueChange = { onConfigUpdate(configState.copy(bleDiscoveryIntervalIdleMs = (it * 1000).toLong().toString())) },
        valueRange = 15f..300f,
        steps = 56,
        enabled = isCustom,
    )

    Text(
        stringResource(R.string.interface_config_scan_duration, scanDurationSeconds),
        style = MaterialTheme.typography.bodySmall,
    )
    Text(
        stringResource(R.string.interface_config_scan_duration_help),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Slider(
        value = (configState.bleScanDurationMs.toFloatOrNull() ?: 10000f) / 1000f,
        onValueChange = { onConfigUpdate(configState.copy(bleScanDurationMs = (it * 1000).toLong().toString())) },
        valueRange = 3f..15f,
        steps = 11,
        enabled = isCustom,
    )

    // Warn when scan duration is close to or exceeds the active scan interval (high duty-cycle)
    val scanDuration = configState.bleScanDurationMs.toLongOrNull() ?: 10000L
    val activeInterval = configState.bleDiscoveryIntervalMs.toLongOrNull() ?: 5000L
    if (isCustom && scanDuration >= activeInterval) {
        Text(
            stringResource(R.string.interface_config_scan_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }

    Text(
        stringResource(R.string.interface_config_ad_refresh_interval, advertisingRefreshSeconds),
        style = MaterialTheme.typography.bodySmall,
    )
    Text(
        stringResource(R.string.interface_config_ad_refresh_interval_help),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Slider(
        value = (configState.bleAdvertisingRefreshIntervalMs.toFloatOrNull() ?: 60000f) / 1000f,
        onValueChange = { onConfigUpdate(configState.copy(bleAdvertisingRefreshIntervalMs = (it * 1000).toLong().toString())) },
        valueRange = 30f..300f,
        steps = 26,
        enabled = isCustom,
    )
}

@Composable
fun TCPServerFields(
    configState: InterfaceConfigState,
    onConfigUpdate: (InterfaceConfigState) -> Unit,
) {
    val exampleAnyAddress = stringResource(R.string.interface_config_example_any_address)
    val examplePort4242 = stringResource(R.string.interface_config_example_port_4242)
    val tcpServerConfigurationLabel = stringResource(R.string.interface_config_tcp_server_configuration)
    val tcpServerHelp = stringResource(R.string.interface_config_tcp_server_help)
    val listenIpLabel = stringResource(R.string.interface_config_listen_ip_label)
    val listenIpHelp = stringResource(R.string.interface_config_listen_ip_help)
    val listenPortLabel = stringResource(R.string.interface_config_listen_port_label)
    val listenPortHelp = stringResource(R.string.interface_config_listen_port_help)

    Text(
        tcpServerConfigurationLabel,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )

    Text(
        tcpServerHelp,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    OutlinedTextField(
        value = configState.listenIp,
        onValueChange = { ip ->
            val cleaned =
                ip
                    .trim()
                    .removePrefix("http://")
                    .removePrefix("https://")
            onConfigUpdate(configState.copy(listenIp = cleaned))
        },
        label = { Text(listenIpLabel) },
        placeholder = { Text(exampleAnyAddress) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = configState.listenIpError != null,
        supportingText = {
            Column {
                configState.listenIpError?.let { Text(it) }
                Text(
                    listenIpHelp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )

    OutlinedTextField(
        value = configState.listenPort,
        onValueChange = { onConfigUpdate(configState.copy(listenPort = it)) },
        label = { Text(listenPortLabel) },
        placeholder = { Text(examplePort4242) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = configState.listenPortError != null,
        supportingText = {
            Column {
                configState.listenPortError?.let { Text(it) }
                Text(
                    listenPortHelp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
