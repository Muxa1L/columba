package com.lxmf.messenger.ui.screens.tcpclient

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.lxmf.messenger.R
import com.lxmf.messenger.ui.components.WizardBottomBar
import com.lxmf.messenger.viewmodel.TcpClientWizardStep
import com.lxmf.messenger.viewmodel.TcpClientWizardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TcpClientWizardScreen(
    onNavigateBack: () -> Unit,
    onComplete: () -> Unit,
    interfaceId: Long? = null,
    // Initial values for creating from discovered interface
    initialHost: String? = null,
    initialPort: Int? = null,
    initialName: String? = null,
    viewModel: TcpClientWizardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val defaultConnectionName = stringResource(R.string.tcp_wizard_default_connection_name)

    // Load existing interface for editing, or set initial values from discovered interface
    LaunchedEffect(interfaceId, initialHost) {
        if (interfaceId != null) {
            viewModel.loadExistingInterface(interfaceId)
        } else if (initialHost != null) {
            viewModel.setInitialValues(
                host = initialHost,
                port = initialPort ?: 4242,
                name = initialName ?: defaultConnectionName,
            )
        }
    }

    // Handle system back button - go to previous step or exit wizard
    BackHandler {
        if (state.currentStep == TcpClientWizardStep.SERVER_SELECTION) {
            onNavigateBack()
        } else {
            viewModel.goToPreviousStep()
        }
    }

    // Handle save success
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            onComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (state.currentStep) {
                            TcpClientWizardStep.SERVER_SELECTION -> stringResource(R.string.tcp_wizard_choose_server)
                            TcpClientWizardStep.REVIEW_CONFIGURE -> stringResource(R.string.tcp_wizard_review_settings)
                        },
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.currentStep == TcpClientWizardStep.SERVER_SELECTION) {
                                onNavigateBack()
                            } else {
                                viewModel.goToPreviousStep()
                            }
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            WizardBottomBar(
                currentStepIndex = state.currentStep.ordinal,
                totalSteps = TcpClientWizardStep.entries.size,
                buttonText =
                    when (state.currentStep) {
                        TcpClientWizardStep.SERVER_SELECTION -> stringResource(R.string.common_next)
                        TcpClientWizardStep.REVIEW_CONFIGURE -> stringResource(R.string.common_save)
                    },
                canProceed = viewModel.canProceed(),
                isSaving = state.isSaving,
                onButtonClick = {
                    if (state.currentStep == TcpClientWizardStep.REVIEW_CONFIGURE) {
                        viewModel.saveConfiguration()
                    } else {
                        viewModel.goToNextStep()
                    }
                },
                modifier = Modifier.navigationBarsPadding(),
            )
        },
    ) { paddingValues ->
        AnimatedContent(
            targetState = state.currentStep,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                } else {
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                }
            },
            label = "tcp_wizard_step",
        ) { step ->
            when (step) {
                TcpClientWizardStep.SERVER_SELECTION -> ServerSelectionStep(viewModel)
                TcpClientWizardStep.REVIEW_CONFIGURE -> ReviewConfigureStep(viewModel)
            }
        }
    }

    // Error dialog
    state.saveError?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearSaveError() },
            title = { Text(stringResource(R.string.common_error)) },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSaveError() }) {
                    Text(stringResource(R.string.common_ok))
                }
            },
        )
    }
}
