package com.lxmf.messenger.reticulum.flasher

import android.content.Context
import androidx.annotation.StringRes
import tech.torlando.columba.reticulum.R

/**
 * Represents a firmware source for the RNode flasher.
 *
 * Each GitHub-backed source maps directly to a GitHub owner/repo pair.
 * Custom allows the user to supply a local ZIP file or a direct download URL.
 */
sealed class FirmwareSource(
    /** Used as the cache subdirectory name under `firmware/`. */
    val id: String,
    val displayName: String,
    @StringRes val displayNameRes: Int,
    /** GitHub owner, or null for Custom. */
    val owner: String?,
    /** GitHub repo name, or null for Custom. */
    val repo: String?,
) {
    object Official : FirmwareSource(
        id = "official",
        displayName = "RNode Official",
        displayNameRes = R.string.firmware_source_official,
        owner = "markqvist",
        repo = "RNode_Firmware",
    )

    object MicroReticulum : FirmwareSource(
        id = "microreticulum",
        displayName = "microReticulum",
        displayNameRes = R.string.firmware_source_microreticulum,
        owner = "attermann",
        repo = "microReticulum_Firmware",
    )

    object CommunityEdition : FirmwareSource(
        id = "ce",
        displayName = "RNode Community Edition",
        displayNameRes = R.string.firmware_source_community_edition,
        owner = "liberatedsystems",
        repo = "RNode_Firmware_CE",
    )

    object Custom : FirmwareSource(
        id = "custom",
        displayName = "Custom",
        displayNameRes = R.string.firmware_source_custom,
        owner = null,
        repo = null,
    )

    fun localizedDisplayName(context: Context): String = context.getString(displayNameRes)
}
