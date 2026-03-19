package com.lxmf.messenger.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lxmf.messenger.R

/**
 * Material 3 confirmation dialog for adding a contact from a scanned QR code or deep link.
 *
 * @param destinationHash The LXMF destination hash (32 hex characters)
 * @param onDismiss Callback when dialog is dismissed/cancelled
 * @param onConfirm Callback when user confirms addition with optional nickname
 */
@Composable
fun AddContactConfirmationDialog(
    destinationHash: String,
    onDismiss: () -> Unit,
    onConfirm: (nickname: String?) -> Unit,
) {
    var nickname by remember { mutableStateOf("") }
    var showFullHash by remember { mutableStateOf(false) }
    val view = LocalView.current
    val addContactContentDescription = stringResource(R.string.add_contact_content_description)
    val title = stringResource(R.string.add_contact_title)
    val destinationHashLabel = stringResource(R.string.add_contact_destination_hash)
    val showLessLabel = stringResource(R.string.common_show_less)
    val showFullLabel = stringResource(R.string.common_show_full)
    val nicknameLabel = stringResource(R.string.add_contact_nickname_label)
    val nicknamePlaceholder = stringResource(R.string.add_contact_nickname_placeholder)
    val helperText = stringResource(R.string.add_contact_helper)
    val addContactLabel = stringResource(R.string.add_contact_content_description)
    val cancelLabel = stringResource(R.string.common_cancel)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = addContactContentDescription,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Destination hash display
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = destinationHashLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Hash display with expand/collapse
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                        ) {
                            Text(
                                text =
                                    if (showFullHash) {
                                        destinationHash
                                    } else {
                                        // Show first 8 and last 8 characters
                                        "${destinationHash.take(8)}...${destinationHash.takeLast(8)}"
                                    },
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            // Toggle button
                            TextButton(
                                onClick = { showFullHash = !showFullHash },
                                modifier = Modifier.align(Alignment.End),
                            ) {
                                Text(
                                    text = if (showFullHash) showLessLabel else showFullLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }

                // Optional nickname field
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text(nicknameLabel) },
                    placeholder = { Text(nicknamePlaceholder) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                )

                // Helper text
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    onConfirm(nickname.trim().ifBlank { null })
                },
            ) {
                Text(addContactLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelLabel)
            }
        },
    )
}
