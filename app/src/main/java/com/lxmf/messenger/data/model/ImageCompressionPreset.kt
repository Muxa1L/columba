package com.lxmf.messenger.data.model

import androidx.annotation.StringRes
import com.lxmf.messenger.R

/**
 * Image compression presets for adaptive network-aware compression.
 * Presets are ordered from most aggressive compression (LOW) to least (ORIGINAL).
 *
 * @property displayName User-facing name for the preset
 * @property maxDimensionPx Maximum image dimension in pixels (width or height)
 * @property targetSizeBytes Target file size in bytes after compression
 * @property initialQuality Starting JPEG/WebP quality (0-100)
 * @property minQuality Minimum quality to try before giving up on target size
 * @property description Brief description for settings UI
 */
enum class ImageCompressionPreset(
    @StringRes val displayNameRes: Int,
    val displayName: String,
    val maxDimensionPx: Int,
    val targetSizeBytes: Long,
    val initialQuality: Int,
    val minQuality: Int,
    @StringRes val descriptionRes: Int,
    val description: String,
) {
    // 32KB target
    LOW(
        displayNameRes = R.string.settings_image_compression_preset_low,
        displayName = "Low",
        maxDimensionPx = 320,
        targetSizeBytes = 32 * 1024L,
        initialQuality = 60,
        minQuality = 30,
        descriptionRes = R.string.settings_image_compression_description_low,
        description = "32KB max - optimized for LoRa and BLE",
    ),

    // 128KB target
    MEDIUM(
        displayNameRes = R.string.settings_image_compression_preset_medium,
        displayName = "Medium",
        maxDimensionPx = 800,
        targetSizeBytes = 128 * 1024L,
        initialQuality = 75,
        minQuality = 40,
        descriptionRes = R.string.settings_image_compression_description_medium,
        description = "128KB max - balanced for mixed networks",
    ),

    // 512KB target
    HIGH(
        displayNameRes = R.string.settings_image_compression_preset_high,
        displayName = "High",
        maxDimensionPx = 2048,
        targetSizeBytes = 512 * 1024L,
        initialQuality = 90,
        minQuality = 50,
        descriptionRes = R.string.settings_image_compression_description_high,
        description = "512KB max - good quality for general use",
    ),

    // 25MB target
    ORIGINAL(
        displayNameRes = R.string.settings_image_compression_preset_original,
        displayName = "Original",
        // 8K resolution - exceeds Android Canvas limit if higher
        maxDimensionPx = 8192,
        targetSizeBytes = 25 * 1024 * 1024L,
        initialQuality = 95,
        minQuality = 90,
        descriptionRes = R.string.settings_image_compression_description_original,
        description = "25MB max - minimal compression for fast networks",
    ),

    // Default values (will be overridden by detection)
    AUTO(
        displayNameRes = R.string.settings_image_compression_preset_auto,
        displayName = "Auto",
        maxDimensionPx = 2048,
        targetSizeBytes = 512 * 1024L,
        initialQuality = 90,
        minQuality = 50,
        descriptionRes = R.string.settings_image_compression_description_auto,
        description = "Automatically select based on enabled interfaces",
    ),
    ;

    companion object {
        val DEFAULT = AUTO

        /**
         * Parse a preset from its name, falling back to DEFAULT if not found.
         */
        fun fromName(name: String): ImageCompressionPreset = entries.find { it.name == name } ?: DEFAULT
    }
}
