package com.lxmf.messenger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lxmf.messenger.R
import com.lxmf.messenger.viewmodel.MainViewModel
import com.lxmf.messenger.viewmodel.UiState

/**
 * Main screen of the Columba application.
 * Demonstrates integration with Reticulum protocol through the abstraction layer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val title = stringResource(R.string.main_screen_title)
    val networkStatusDescription = stringResource(R.string.main_screen_network_status_cd)
    val heading = stringResource(R.string.main_screen_heading)
    val intro = stringResource(R.string.main_screen_intro)
    val networkStatusTitle = stringResource(R.string.main_screen_network_status_title)
    val initializeReticulumLabel = stringResource(R.string.main_screen_initialize_reticulum)
    val createIdentityLabel = stringResource(R.string.main_screen_create_identity)
    val testSendPacketLabel = stringResource(R.string.main_screen_test_send_packet)
    val readyLabel = stringResource(R.string.main_screen_ready)
    val statusLabel = stringResource(R.string.main_screen_status_label)
    val successLabel = stringResource(R.string.main_screen_success)
    val initialHint = stringResource(R.string.main_screen_initial_hint)
    val footer = stringResource(R.string.main_screen_footer)
    val networkStatusLabel = viewModel.getNetworkStatusLabel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = networkStatusDescription,
                        tint = Color(viewModel.getNetworkStatusColor()),
                        modifier = Modifier.padding(end = 16.dp),
                    )
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Text(
                text = heading,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 32.dp),
            )

            Text(
                text = intro,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Network Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                ) {
                    Text(
                        text = networkStatusTitle,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = networkStatusLabel,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            // Action Buttons
            Button(
                onClick = { viewModel.initializeReticulum() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(initializeReticulumLabel)
            }

            Button(
                onClick = { viewModel.createIdentity() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(createIdentityLabel)
            }

            Button(
                onClick = { viewModel.testSendPacket() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(testSendPacketLabel)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status/Result Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    when (uiState) {
                        is UiState.Error ->
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            )
                        is UiState.Success ->
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            )
                        else -> CardDefaults.cardColors()
                    },
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                ) {
                    Text(
                        text =
                            when (uiState) {
                                is UiState.Initial -> readyLabel
                                is UiState.Loading -> statusLabel
                                is UiState.Success -> successLabel
                                is UiState.Error -> stringResource(R.string.common_error)
                            },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    when (val state = uiState) {
                        is UiState.Initial -> {
                            Text(initialHint)
                        }
                        is UiState.Loading -> {
                            CircularProgressIndicator(
                                modifier =
                                    Modifier
                                        .padding(8.dp)
                                        .size(24.dp),
                            )
                            Text(state.message)
                        }
                        is UiState.Success -> {
                            Text(state.message)
                        }
                        is UiState.Error -> {
                            Text(state.message)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Text(
                text = footer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
