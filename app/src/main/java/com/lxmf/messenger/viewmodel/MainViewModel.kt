package com.lxmf.messenger.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lxmf.messenger.R
import com.lxmf.messenger.reticulum.model.DestinationType
import com.lxmf.messenger.reticulum.model.Direction
import com.lxmf.messenger.reticulum.model.Identity
import com.lxmf.messenger.reticulum.model.InterfaceConfig
import com.lxmf.messenger.reticulum.model.LogLevel
import com.lxmf.messenger.reticulum.model.NetworkStatus
import com.lxmf.messenger.reticulum.model.ReticulumConfig
import com.lxmf.messenger.reticulum.protocol.ReticulumProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main ViewModel for the Columba application.
 * Demonstrates integration with the Reticulum abstraction layer.
 */
@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val reticulumProtocol: ReticulumProtocol,
        @ApplicationContext private val context: Context? = null,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
        val uiState: StateFlow<UiState> = _uiState.asStateFlow()

        private val _networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.SHUTDOWN)
        val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

        private var currentIdentity: Identity? = null

        private fun string(
            resId: Int,
            fallback: String,
            vararg args: Any,
        ): String =
            runCatching {
                if (args.isEmpty()) {
                    context?.getString(resId)?.takeIf { it.isNotBlank() } ?: fallback
                } else {
                    context?.getString(resId, *args)?.takeIf { it.isNotBlank() } ?: fallback.format(*args)
                }
            }.getOrElse {
                if (args.isEmpty()) fallback else fallback.format(*args)
            }

        fun initializeReticulum() {
            viewModelScope.launch {
                _uiState.value = UiState.Loading(string(R.string.main_viewmodel_initializing_reticulum, "Initializing Reticulum..."))

                val config =
                    ReticulumConfig(
                        storagePath = "/tmp/columba",
                        enabledInterfaces = listOf(InterfaceConfig.AutoInterface()),
                        logLevel = LogLevel.INFO,
                    )

                reticulumProtocol.initialize(config)
                    .onSuccess {
                        _networkStatus.value = reticulumProtocol.networkStatus.value
                        _uiState.value = UiState.Success(string(R.string.main_viewmodel_reticulum_initialized, "Reticulum initialized successfully"))
                    }
                    .onFailure { error ->
                        _uiState.value =
                            UiState.Error(
                                string(
                                    R.string.main_viewmodel_failed_initialize,
                                    "Failed to initialize: %s",
                                    error.message ?: string(R.string.identity_screen_unknown_error, "Unknown error"),
                                ),
                            )
                    }
            }
        }

        fun createIdentity() {
            viewModelScope.launch {
                _uiState.value = UiState.Loading(string(R.string.main_viewmodel_creating_identity, "Creating identity..."))

                reticulumProtocol.createIdentity()
                    .onSuccess { identity ->
                        currentIdentity = identity
                        val hexHash = identity.hash.joinToString("") { "%02x".format(it) }
                        _uiState.value = UiState.Success(string(R.string.main_viewmodel_identity_created, "Identity created!\nHash: %s", hexHash))
                    }
                    .onFailure { error ->
                        _uiState.value =
                            UiState.Error(
                                string(
                                    R.string.main_viewmodel_failed_create_identity,
                                    "Failed to create identity: %s",
                                    error.message ?: string(R.string.identity_screen_unknown_error, "Unknown error"),
                                ),
                            )
                    }
            }
        }

        fun testSendPacket() {
            viewModelScope.launch {
                val identity = currentIdentity
                if (identity == null) {
                    _uiState.value = UiState.Error(string(R.string.main_viewmodel_create_identity_first, "Please create an identity first"))
                    return@launch
                }

                _uiState.value =
                    UiState.Loading(
                        string(
                            R.string.main_viewmodel_creating_destination_and_packet,
                            "Creating destination and sending packet...",
                        ),
                    )

                // Create a test destination
                reticulumProtocol.createDestination(
                    identity = identity,
                    direction = Direction.OUT,
                    type = DestinationType.SINGLE,
                    appName = "columba.test",
                    aspects = listOf("test"),
                ).onSuccess { destination ->
                    // Send a test packet
                    reticulumProtocol.sendPacket(
                        destination = destination,
                        data = "Hello, Reticulum!".toByteArray(),
                    ).onSuccess { receipt ->
                        _uiState.value =
                            UiState.Success(
                                string(
                                    R.string.main_viewmodel_packet_sent,
                                    "Packet sent!\nDelivered: %s\nReceipt hash: %s...",
                                    receipt.delivered,
                                    receipt.hash.take(8).joinToString("") { "%02x".format(it) },
                                ),
                            )
                    }.onFailure { error ->
                        _uiState.value =
                            UiState.Error(
                                string(
                                    R.string.main_viewmodel_failed_send_packet,
                                    "Failed to send packet: %s",
                                    error.message ?: string(R.string.identity_screen_unknown_error, "Unknown error"),
                                ),
                            )
                    }
                }.onFailure { error ->
                    _uiState.value =
                        UiState.Error(
                            string(
                                R.string.main_viewmodel_failed_create_destination,
                                "Failed to create destination: %s",
                                error.message ?: string(R.string.identity_screen_unknown_error, "Unknown error"),
                            ),
                        )
                }
            }
        }

        fun getNetworkStatusColor(): Long {
            return when (networkStatus.value) {
                is NetworkStatus.READY -> 0xFF4CAF50 // Green
                is NetworkStatus.INITIALIZING -> 0xFFFFC107 // Amber
                is NetworkStatus.CONNECTING -> 0xFF2196F3 // Blue
                is NetworkStatus.ERROR -> 0xFFF44336 // Red
                NetworkStatus.SHUTDOWN -> 0xFF9E9E9E // Gray
            }
        }

        fun getNetworkStatusLabel(): String =
            when (val status = networkStatus.value) {
                is NetworkStatus.READY -> string(R.string.service_notification_status_ready, "READY")
                is NetworkStatus.INITIALIZING -> string(R.string.service_notification_status_initializing, "INITIALIZING")
                is NetworkStatus.CONNECTING -> string(R.string.service_notification_status_connecting, "CONNECTING")
                is NetworkStatus.ERROR -> string(R.string.announce_stream_error_prefix, "ERROR: %s", status.message)
                NetworkStatus.SHUTDOWN -> string(R.string.service_notification_status_disconnected, "SHUTDOWN")
            }
    }

sealed class UiState {
    object Initial : UiState()

    data class Loading(val message: String) : UiState()

    data class Success(val message: String) : UiState()

    data class Error(val message: String) : UiState()
}
