package com.lxmf.messenger.ui.screens.onboarding

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.lxmf.messenger.R

/**
 * State for the paged onboarding flow.
 */
@Immutable
data class OnboardingState(
    val currentPage: Int = 0,
    val displayName: String = "",
    val selectedInterfaces: Set<OnboardingInterfaceType> = setOf(OnboardingInterfaceType.AUTO),
    val notificationsEnabled: Boolean = false,
    val notificationsGranted: Boolean = false,
    val batteryOptimizationExempt: Boolean = false,
    val isSaving: Boolean = false,
    val isLoading: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val error: String? = null,
    val blePermissionsGranted: Boolean = false,
    val blePermissionsDenied: Boolean = false,
)

/**
 * Interface types that can be enabled during onboarding.
 * Simplified version of the full InterfaceConfig for user selection.
 */
enum class OnboardingInterfaceType(
    @StringRes val displayNameRes: Int,
    val displayName: String,
    @StringRes val descriptionRes: Int,
    val description: String,
    @StringRes val secondaryDescriptionRes: Int? = null,
    val secondaryDescription: String? = null,
) {
    AUTO(
        displayNameRes = R.string.onboarding_interface_auto_name,
        displayName = "Local WiFi",
        descriptionRes = R.string.onboarding_interface_auto_description,
        description = "Discover peers on your local network",
        secondaryDescriptionRes = R.string.onboarding_interface_auto_secondary,
        secondaryDescription = "No internet required",
    ),
    BLE(
        displayNameRes = R.string.onboarding_interface_ble_name,
        displayName = "Bluetooth LE",
        descriptionRes = R.string.onboarding_interface_ble_description,
        description = "Connect directly to nearby devices",
        secondaryDescriptionRes = R.string.onboarding_interface_ble_secondary,
        secondaryDescription = "Requires Bluetooth permissions",
    ),
    TCP(
        displayNameRes = R.string.onboarding_interface_tcp_name,
        displayName = "Internet (TCP)",
        descriptionRes = R.string.onboarding_interface_tcp_description,
        description = "Connect to the global Reticulum network",
        secondaryDescriptionRes = R.string.onboarding_interface_tcp_secondary,
        secondaryDescription = "Requires internet connection",
    ),
    RNODE(
        displayNameRes = R.string.onboarding_interface_rnode_name,
        displayName = "LoRa Radio",
        descriptionRes = R.string.onboarding_interface_rnode_description,
        description = "Long-range mesh via RNode hardware",
        secondaryDescriptionRes = R.string.onboarding_interface_rnode_secondary,
        secondaryDescription = "Requires external hardware - configure in Settings",
    ),
}

fun OnboardingInterfaceType.localizedDisplayName(context: Context): String =
    runCatching { context.getString(displayNameRes) }.getOrDefault(displayName)

fun OnboardingInterfaceType.localizedDescription(context: Context): String =
    runCatching { context.getString(descriptionRes) }.getOrDefault(description)

fun OnboardingInterfaceType.localizedSecondaryDescription(context: Context): String? =
    secondaryDescriptionRes?.let { resId ->
        runCatching { context.getString(resId) }.getOrDefault(secondaryDescription)
    } ?: secondaryDescription

/**
 * Total number of onboarding pages.
 */
const val ONBOARDING_PAGE_COUNT = 5
