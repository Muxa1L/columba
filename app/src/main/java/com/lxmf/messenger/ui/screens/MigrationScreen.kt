package com.lxmf.messenger.ui.screens

import android.Manifest
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.hilt.navigation.compose.hiltViewModel
import com.lxmf.messenger.R
import com.lxmf.messenger.migration.ExportResult
import com.lxmf.messenger.migration.MigrationPreview
import com.lxmf.messenger.viewmodel.MigrationUiState
import com.lxmf.messenger.viewmodel.MigrationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Migration screen for exporting and importing app data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrationScreen(
    onNavigateBack: () -> Unit,
    onImportComplete: () -> Unit = {},
    viewModel: MigrationViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val resources = context.resources
    val uiState by viewModel.uiState.collectAsState()
    val exportProgress by viewModel.exportProgress.collectAsState()
    val importProgress by viewModel.importProgress.collectAsState()
    val exportPreview by viewModel.exportPreview.collectAsState()
    val includeAttachments by viewModel.includeAttachments.collectAsState()

    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var pendingImportPassword by remember { mutableStateOf<String?>(null) }
    var showExportPasswordDialog by remember { mutableStateOf(false) }
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    var pendingImportComplete by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val exportSavedSuccessfully = stringResource(R.string.migration_screen_export_saved_successfully)

    // Notification permission launcher for Android 13+
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            // Permission result received, now complete the import flow
            showNotificationPermissionDialog = false
            if (pendingImportComplete) {
                pendingImportComplete = false
                onImportComplete()
            }
        }

    // SAF file save launcher for data export
    val exportSaveLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        ) { destinationUri: Uri? ->
            destinationUri?.let {
                viewModel.saveExportToFile(context.contentResolver, it)
            }
        }

    // Check if notification permission is needed (Android 13+ with notifications enabled in settings)
    fun needsNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PermissionChecker.PERMISSION_GRANTED

    // File picker for import
    val importLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let {
                viewModel.previewImport(it)
            }
        }

    // Handle state changes
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is MigrationUiState.ExportComplete -> {
                val timestamp =
                    SimpleDateFormat(
                        "yyyy-MM-dd_HHmmss",
                        Locale.US,
                    ).format(Date())
                exportSaveLauncher.launch("columba_export_$timestamp.columba")
                viewModel.onExportSaveDialogLaunched()
            }
            is MigrationUiState.ExportSaved -> {
                snackbarHostState.showSnackbar(exportSavedSuccessfully)
                viewModel.resetState()
            }
            is MigrationUiState.ImportPreview -> {
                pendingImportUri = state.fileUri
                pendingImportPassword = state.password
                showImportConfirmDialog = true
            }
            is MigrationUiState.PasswordRequired, is MigrationUiState.WrongPassword -> {
                // Handled by dialogs below
            }
            is MigrationUiState.ImportComplete -> {
                snackbarHostState.showSnackbar(
                    resources.getString(
                        R.string.migration_screen_import_complete_snackbar,
                        formatMigrationSummary(
                            resources,
                            listOf(
                                R.plurals.migration_screen_identities_count to state.result.identitiesImported,
                                R.plurals.migration_screen_messages_count to state.result.messagesImported,
                                R.plurals.migration_screen_announces_count to state.result.announcesImported,
                                R.plurals.migration_screen_interfaces_count to state.result.interfacesImported,
                            ),
                        ),
                    ),
                )
            }
            is MigrationUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.migration_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.migration_screen_navigate_back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Export Section
            ExportSection(
                exportPreview = exportPreview,
                uiState = uiState,
                exportProgress = exportProgress,
                includeAttachments = includeAttachments,
                onIncludeAttachmentsChange = { viewModel.setIncludeAttachments(it) },
                onExport = { showExportPasswordDialog = true },
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Import Section
            ImportSection(
                uiState = uiState,
                importProgress = importProgress,
                onSelectFile = { importLauncher.launch("*/*") },
                onImportComplete = {
                    // After import, check if notification permission is needed
                    if (needsNotificationPermission()) {
                        pendingImportComplete = true
                        showNotificationPermissionDialog = true
                    } else {
                        onImportComplete()
                    }
                },
            )

            // Bottom spacer for navigation bar
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Export Password Dialog
    if (showExportPasswordDialog) {
        PasswordDialog(
            title = stringResource(R.string.migration_screen_encrypt_export_title),
            description = stringResource(R.string.migration_screen_encrypt_export_description),
            isConfirmMode = true,
            isWrongPassword = false,
            onConfirm = { password ->
                showExportPasswordDialog = false
                viewModel.exportData(password)
            },
            onDismiss = {
                showExportPasswordDialog = false
            },
        )
    }

    // Import Password Dialog (encrypted file detected)
    val currentState = uiState
    if (currentState is MigrationUiState.PasswordRequired || currentState is MigrationUiState.WrongPassword) {
        val fileUri = when (currentState) {
            is MigrationUiState.PasswordRequired -> currentState.fileUri
            is MigrationUiState.WrongPassword -> currentState.fileUri
            else -> null
        }
        if (fileUri != null) {
            PasswordDialog(
                title = stringResource(R.string.migration_screen_encrypted_backup_title),
                description = stringResource(R.string.migration_screen_encrypted_backup_description),
                isConfirmMode = false,
                isWrongPassword = currentState is MigrationUiState.WrongPassword,
                onConfirm = { password ->
                    viewModel.previewImport(fileUri, password)
                },
                onDismiss = {
                    viewModel.resetState()
                },
            )
        }
    }

    // Import Confirmation Dialog
    if (showImportConfirmDialog && uiState is MigrationUiState.ImportPreview) {
        val preview = (uiState as MigrationUiState.ImportPreview).preview
        ImportConfirmDialog(
            preview = preview,
            onConfirm = {
                showImportConfirmDialog = false
                pendingImportUri?.let { viewModel.importData(it, pendingImportPassword) }
            },
            onDismiss = {
                showImportConfirmDialog = false
                viewModel.resetState()
            },
        )
    }

    // Blocking dialog while service restarts after import
    if (uiState is MigrationUiState.RestartingService) {
        RestartingServiceDialog()
    }

    // Notification permission dialog after import (Android 13+)
    if (showNotificationPermissionDialog) {
        NotificationPermissionDialog(
            onConfirm = {
                showNotificationPermissionDialog = false
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
            onDismiss = {
                // User declined, proceed without notifications
                showNotificationPermissionDialog = false
                if (pendingImportComplete) {
                    pendingImportComplete = false
                    onImportComplete()
                }
            },
        )
    }
}

@Composable
private fun ExportSection(
    exportPreview: ExportResult?,
    uiState: MigrationUiState,
    exportProgress: Float,
    includeAttachments: Boolean,
    onIncludeAttachmentsChange: (Boolean) -> Unit,
    onExport: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Upload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.migration_screen_export_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                stringResource(R.string.migration_screen_export_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            // Preview stats
            when (exportPreview) {
                is ExportResult.Success -> {
                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                stringResource(R.string.migration_screen_export_data_to_export),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(pluralStringResource(R.plurals.migration_screen_identities_count, exportPreview.identityCount, exportPreview.identityCount))
                            Text(pluralStringResource(R.plurals.migration_screen_messages_count, exportPreview.messageCount, exportPreview.messageCount))
                            Text(pluralStringResource(R.plurals.migration_screen_contacts_count, exportPreview.contactCount, exportPreview.contactCount))
                            Text(pluralStringResource(R.plurals.migration_screen_announces_count, exportPreview.announceCount, exportPreview.announceCount))
                            Text(pluralStringResource(R.plurals.migration_screen_interfaces_count, exportPreview.interfaceCount, exportPreview.interfaceCount))
                            Text(pluralStringResource(R.plurals.migration_screen_custom_themes_count, exportPreview.customThemeCount, exportPreview.customThemeCount))
                        }
                    }
                }
                is ExportResult.Error -> {
                    Text(
                        stringResource(R.string.migration_screen_preview_load_failed, exportPreview.message),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                null -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }

            // Export progress
            when (uiState) {
                is MigrationUiState.Exporting -> {
                    Column {
                        LinearProgressIndicator(
                            progress = { exportProgress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.migration_screen_progress_percent, (exportProgress * 100).toInt()),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                is MigrationUiState.ExportComplete -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.migration_screen_export_complete))
                    }
                }
                else -> {}
            }

            // Include attachments checkbox
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled =
                                uiState !is MigrationUiState.Exporting &&
                                    uiState !is MigrationUiState.Importing,
                        ) {
                            onIncludeAttachmentsChange(!includeAttachments)
                        },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = includeAttachments,
                    // Parent Row handles clicks
                    onCheckedChange = null,
                    enabled =
                        uiState !is MigrationUiState.Exporting &&
                            uiState !is MigrationUiState.Importing,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.migration_screen_include_attachments),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (!includeAttachments) {
                        Text(
                            stringResource(R.string.migration_screen_include_attachments_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Button(
                onClick = onExport,
                modifier = Modifier.fillMaxWidth(),
                enabled =
                    uiState !is MigrationUiState.Exporting &&
                        uiState !is MigrationUiState.Importing,
            ) {
                if (uiState is MigrationUiState.Exporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(stringResource(R.string.migration_screen_export_all_data))
            }
        }
    }
}

@Composable
private fun ImportSection(
    uiState: MigrationUiState,
    importProgress: Float,
    onSelectFile: () -> Unit,
    onImportComplete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.migration_screen_import_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                stringResource(R.string.migration_screen_import_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )

            // Import progress
            when (uiState) {
                is MigrationUiState.Importing -> {
                    Column {
                        LinearProgressIndicator(
                            progress = { importProgress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.migration_screen_importing_progress, (importProgress * 100).toInt()),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                is MigrationUiState.ImportComplete -> {
                    // Trigger the callback to navigate away after import and restart complete
                    var hasCalledImportComplete by remember { mutableStateOf(false) }
                    LaunchedEffect(uiState) {
                        if (!hasCalledImportComplete) {
                            hasCalledImportComplete = true
                            onImportComplete()
                        }
                    }
                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    stringResource(R.string.migration_screen_import_complete_title),
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    formatMigrationSummary(
                                        LocalContext.current.resources,
                                        listOf(
                                            R.plurals.migration_screen_identities_count to uiState.result.identitiesImported,
                                            R.plurals.migration_screen_messages_count to uiState.result.messagesImported,
                                            R.plurals.migration_screen_contacts_count to uiState.result.contactsImported,
                                            R.plurals.migration_screen_announces_count to uiState.result.announcesImported,
                                            R.plurals.migration_screen_interfaces_count to uiState.result.interfacesImported,
                                            R.plurals.migration_screen_themes_count to uiState.result.customThemesImported,
                                        ),
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
                is MigrationUiState.Error -> {
                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                uiState.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }
                else -> {}
            }

            OutlinedButton(
                onClick = onSelectFile,
                modifier = Modifier.fillMaxWidth(),
                enabled =
                    uiState !is MigrationUiState.Exporting &&
                        uiState !is MigrationUiState.Importing &&
                        uiState !is MigrationUiState.RestartingService,
            ) {
                if (uiState is MigrationUiState.Importing ||
                    uiState is MigrationUiState.Loading
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(stringResource(R.string.migration_screen_select_migration_file))
            }
        }
    }
}

@Composable
private fun ImportConfirmDialog(
    preview: MigrationPreview,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val datePattern = stringResource(R.string.migration_screen_import_confirm_date_pattern)
    val dateFormat = remember(datePattern) { SimpleDateFormat(datePattern, Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.migration_screen_import_confirm_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(R.string.migration_screen_import_confirm_created_on),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    dateFormat.format(Date(preview.exportedAt)),
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    stringResource(R.string.migration_screen_import_confirm_data_to_import),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(pluralStringResource(R.plurals.migration_screen_identities_count, preview.identityCount, preview.identityCount))
                        if (preview.identityNames.isNotEmpty()) {
                            Text(
                                preview.identityNames.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(pluralStringResource(R.plurals.migration_screen_conversations_count, preview.conversationCount, preview.conversationCount))
                        Text(pluralStringResource(R.plurals.migration_screen_messages_count, preview.messageCount, preview.messageCount))
                        Text(pluralStringResource(R.plurals.migration_screen_contacts_count, preview.contactCount, preview.contactCount))
                        Text(pluralStringResource(R.plurals.migration_screen_announces_count, preview.announceCount, preview.announceCount))
                        Text(pluralStringResource(R.plurals.migration_screen_interfaces_count, preview.interfaceCount, preview.interfaceCount))
                        Text(pluralStringResource(R.plurals.migration_screen_custom_themes_count, preview.customThemeCount, preview.customThemeCount))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    stringResource(R.string.migration_screen_import_confirm_skip_existing),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.migration_screen_import))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.migration_screen_cancel))
            }
        },
    )
}

private fun formatMigrationSummary(
    resources: Resources,
    items: List<Pair<Int, Int>>,
): String =
    items.joinToString(separator = ", ") { (resId, count) ->
        resources.getQuantityString(resId, count, count)
    }

/**
 * Blocking dialog shown while service restarts after import.
 * Similar to ApplyChangesDialog in InterfaceManagementScreen.
 */
@Composable
private fun RestartingServiceDialog() {
    AlertDialog(
        onDismissRequest = { /* Cannot dismiss - blocking */ },
        icon = {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        },
        title = { Text(stringResource(R.string.migration_screen_restarting_service_title)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(R.string.migration_screen_restarting_service_message),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.migration_screen_restarting_service_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { /* No buttons - blocking */ },
    )
}

/**
 * Dialog prompting for notification permission after backup restore.
 * On Android 13+, notification permission isn't restored with backup data,
 * so we need to request it explicitly.
 */
@Composable
private fun NotificationPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
        },
        title = { Text(stringResource(R.string.migration_screen_enable_notifications_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(R.string.migration_screen_enable_notifications_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.migration_screen_enable_notifications_permission),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.migration_screen_enable))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.migration_screen_not_now))
            }
        },
    )
}

/**
 * Dialog for entering (or creating + confirming) a password for export encryption / import decryption.
 *
 * @param title Dialog title
 * @param description Explanatory text shown below the title
 * @param isConfirmMode If true, shows a second "confirm password" field (used during export)
 * @param isWrongPassword If true, shows an error message (used during import retry)
 * @param onConfirm Called with the validated password
 * @param onDismiss Called when the dialog is cancelled
 */
@Composable
internal fun PasswordDialog(
    title: String,
    description: String,
    isConfirmMode: Boolean,
    isWrongPassword: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val minLength = com.lxmf.messenger.migration.MigrationCrypto.MIN_PASSWORD_LENGTH
    val incorrectPasswordText = stringResource(R.string.migration_screen_incorrect_password)
    val passwordTooShortText = stringResource(R.string.migration_screen_password_too_short, minLength)
    val passwordsDoNotMatchText = stringResource(R.string.migration_screen_passwords_do_not_match)
    val passwordLabel = stringResource(R.string.migration_screen_password_label)
    val confirmPasswordLabel = stringResource(R.string.migration_screen_confirm_password_label)
    val hideLabel = stringResource(R.string.migration_screen_hide)
    val showLabel = stringResource(R.string.migration_screen_show)
    val confirmActionLabel = if (isConfirmMode) stringResource(R.string.migration_screen_export) else stringResource(R.string.migration_screen_unlock)
    val cancelLabel = stringResource(R.string.migration_screen_cancel)
    var errorMessage by remember(incorrectPasswordText, isWrongPassword) {
        mutableStateOf<String?>(if (isWrongPassword) incorrectPasswordText else null)
    }

    fun validate(): Boolean {
        if (password.length < minLength) {
            errorMessage = passwordTooShortText
            return false
        }
        if (isConfirmMode && password != confirmPassword) {
            errorMessage = passwordsDoNotMatchText
            return false
        }
        errorMessage = null
        return true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text(passwordLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation =
                        if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(if (passwordVisible) hideLabel else showLabel)
                        }
                    },
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (isConfirmMode) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text(confirmPasswordLabel) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation =
                            if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        isError = errorMessage != null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (errorMessage != null) {
                    Text(
                        errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validate()) {
                        onConfirm(password)
                    }
                },
                enabled = password.isNotEmpty(),
            ) {
                Text(confirmActionLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelLabel)
            }
        },
    )
}
