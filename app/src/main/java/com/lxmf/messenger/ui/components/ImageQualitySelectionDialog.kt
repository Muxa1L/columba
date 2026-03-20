package com.lxmf.messenger.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.lxmf.messenger.R
import com.lxmf.messenger.data.model.ImageCompressionPreset
import com.lxmf.messenger.service.ConversationLinkManager

@Composable
private fun localizedImageCompressionPresetName(preset: ImageCompressionPreset): String =
    when (preset) {
        ImageCompressionPreset.LOW -> stringResource(R.string.settings_image_compression_preset_low)
        ImageCompressionPreset.MEDIUM -> stringResource(R.string.settings_image_compression_preset_medium)
        ImageCompressionPreset.HIGH -> stringResource(R.string.settings_image_compression_preset_high)
        ImageCompressionPreset.ORIGINAL -> stringResource(R.string.settings_image_compression_preset_original)
        ImageCompressionPreset.AUTO -> stringResource(R.string.settings_image_compression_preset_auto)
    }

@Composable
private fun localizedImageCompressionPresetDescription(preset: ImageCompressionPreset): String =
    when (preset) {
        ImageCompressionPreset.LOW -> stringResource(R.string.settings_image_compression_description_low)
        ImageCompressionPreset.MEDIUM -> stringResource(R.string.settings_image_compression_description_medium)
        ImageCompressionPreset.HIGH -> stringResource(R.string.settings_image_compression_description_high)
        ImageCompressionPreset.ORIGINAL -> stringResource(R.string.settings_image_compression_description_original)
        ImageCompressionPreset.AUTO -> stringResource(R.string.settings_image_compression_description_auto)
    }

/**
 * Dialog for selecting image quality/compression level before sending.
 *
 * Shows all available presets with:
 * - Recommended preset highlighted based on link state
 * - Estimated transfer times for each option
 * - Path information (hops, rate, etc.)
 *
 * Uses the generic QualitySelectionDialog for consistent UI with
 * other quality selection dialogs (e.g., voice call codec).
 *
 * @param recommendedPreset The preset recommended based on network speed
 * @param linkState The current link state with speed measurements (null if no link)
 * @param transferTimeEstimates Map of preset to estimated transfer time string
 * @param onSelect Called when user selects a preset
 * @param onDismiss Called when dialog is dismissed
 */
@Composable
fun ImageQualitySelectionDialog(
    recommendedPreset: ImageCompressionPreset,
    linkState: ConversationLinkManager.LinkState?,
    transferTimeEstimates: Map<ImageCompressionPreset, String?>,
    onSelect: (ImageCompressionPreset) -> Unit,
    onDismiss: () -> Unit,
    imageCount: Int = 1,
) {
    // Show presets in order: LOW, MEDIUM, HIGH, ORIGINAL (skip AUTO)
    val presets =
        listOf(
            ImageCompressionPreset.LOW,
            ImageCompressionPreset.MEDIUM,
            ImageCompressionPreset.HIGH,
            ImageCompressionPreset.ORIGINAL,
        )

    val options =
        presets.map { preset ->
            QualityOption(
                value = preset,
                displayName = localizedImageCompressionPresetName(preset),
                description = localizedImageCompressionPresetDescription(preset),
            )
        }

    val title =
        if (imageCount > 1) {
            pluralStringResource(R.plurals.image_quality_send_images_title, imageCount, imageCount)
        } else {
            stringResource(R.string.image_quality_choose_title)
        }

    val confirmText =
        if (imageCount > 1) {
            pluralStringResource(R.plurals.image_quality_send_images_action, imageCount, imageCount)
        } else {
            stringResource(R.string.image_quality_send_action)
        }

    // For multi-image shares, append " each" to transfer time estimates so the user
    // understands the time shown is per-image, not total.
    val displayEstimates =
        if (imageCount > 1) {
            transferTimeEstimates.mapValues { (_, time) ->
                time?.let { estimate -> stringResource(R.string.image_quality_transfer_time_each, estimate) }
            }
        } else {
            transferTimeEstimates
        }

    QualitySelectionDialog(
        title = title,
        options = options,
        initialSelection = recommendedPreset,
        recommendedOption = recommendedPreset,
        linkState = linkState,
        transferTimeEstimates = displayEstimates,
        confirmButtonText = confirmText,
        onConfirm = onSelect,
        onDismiss = onDismiss,
    )
}
