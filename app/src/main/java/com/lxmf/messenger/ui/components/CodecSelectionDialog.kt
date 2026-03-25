package com.lxmf.messenger.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.lxmf.messenger.R
import com.lxmf.messenger.service.ConversationLinkManager
import com.lxmf.messenger.ui.model.CodecProfile

/**
 * Dialog for selecting an audio codec profile before initiating a voice call.
 *
 * Displays all available codec profiles with their descriptions,
 * allowing the user to choose based on their network conditions.
 * Uses the generic QualitySelectionDialog for consistent UI with
 * other quality selection dialogs (e.g., image quality).
 *
 * @param recommendedProfile The recommended profile based on link speed (default: QUALITY_MEDIUM)
 * @param linkState Current link state for displaying path info (null to hide)
 * @param onDismiss Called when the dialog is dismissed without selection
 * @param onProfileSelected Called with the selected profile when user confirms
 */
@Composable
fun CodecSelectionDialog(
    recommendedProfile: CodecProfile = CodecProfile.DEFAULT,
    linkState: ConversationLinkManager.LinkState? = null,
    onDismiss: () -> Unit,
    onProfileSelected: (CodecProfile) -> Unit,
) {
    val context = LocalContext.current
    val title = stringResource(R.string.codec_selection_title)
    val subtitle = stringResource(R.string.codec_selection_subtitle)
    val confirmButtonText = stringResource(R.string.codec_selection_confirm)

    val options =
        CodecProfile.entries.map { profile ->
            QualityOption(
                value = profile,
                displayName = profile.localizedDisplayName(context),
                description = profile.localizedDescription(context),
                isExperimental = profile.isExperimental,
            )
        }

    QualitySelectionDialog(
        title = title,
        subtitle = subtitle,
        options = options,
        initialSelection = recommendedProfile,
        recommendedOption = recommendedProfile,
        linkState = linkState,
        confirmButtonText = confirmButtonText,
        onConfirm = onProfileSelected,
        onDismiss = onDismiss,
    )
}
