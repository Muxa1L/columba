package com.lxmf.messenger.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lxmf.messenger.R
import com.lxmf.messenger.data.model.InterfaceType
import com.lxmf.messenger.data.repository.Announce
import com.lxmf.messenger.ui.theme.MeshConnected
import com.lxmf.messenger.ui.theme.MeshLimited
import com.lxmf.messenger.ui.theme.MeshOffline
import com.lxmf.messenger.util.formatTimeSince
import kotlinx.coroutines.delay

/**
 * Shared peer card component used by both AnnounceStreamScreen and SavedPeersScreen.
 */
@OptIn(ExperimentalFoundationApi::class)
@androidx.compose.runtime.Stable
@Composable
fun PeerCard(
    announce: Announce,
    onClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onLongPress: () -> Unit = {},
    badgeContent: @Composable () -> Unit = { NodeTypeBadge(nodeType = announce.nodeType) },
    showFavoriteToggle: Boolean = true,
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    },
                ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Box {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(end = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Profile icon (with identicon fallback)
                ProfileIcon(
                    iconName = announce.iconName,
                    foregroundColor = announce.iconForegroundColor,
                    backgroundColor = announce.iconBackgroundColor,
                    size = 56.dp,
                    fallbackHash = announce.publicKey,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )

                // Peer information
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // Peer name
                    Text(
                        text = announce.peerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    // Destination hash (abbreviated) - use remember to avoid recalculating
                    val abbreviatedHash =
                        remember(announce.destinationHash) {
                            formatHashString(announce.destinationHash)
                        }
                    Text(
                        text = abbreviatedHash,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Time since last seen - update adaptively based on how recent it is
                    var timeSinceText by remember { mutableStateOf(formatTimeSince(context, announce.lastSeenTimestamp)) }

                    LaunchedEffect(announce.lastSeenTimestamp) {
                        // Update immediately when timestamp changes
                        timeSinceText = formatTimeSince(context, announce.lastSeenTimestamp)

                        // Adaptive update frequency: more frequent for recent times, less for old ones
                        while (true) {
                            val now = System.currentTimeMillis()
                            val ageMinutes = (now - announce.lastSeenTimestamp) / (60 * 1000)

                            // Update frequency based on age:
                            // - < 1 minute: every second (very fresh, shows seconds)
                            // - < 1 hour: every 30 seconds (fresh data)
                            // - < 24 hours: every minute (still relevant)
                            // - >= 24 hours: every 2 minutes (less critical)
                            val delayMs =
                                when {
                                    ageMinutes < 1 -> 1_000L // 1 second
                                    ageMinutes < 60 -> 30_000L // 30 seconds
                                    ageMinutes < 1440 -> 60_000L // 1 minute
                                    else -> 120_000L // 2 minutes
                                }

                            delay(delayMs)
                            timeSinceText = formatTimeSince(context, announce.lastSeenTimestamp)
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = timeSinceText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Badge and signal strength indicator
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.CenterVertically),
                ) {
                    badgeContent()
                    SignalStrengthIndicator(hops = announce.hops)
                }
            }

            // Star button and interface type icon overlay column
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .padding(end = 4.dp, top = 4.dp, bottom = 16.dp),
                contentAlignment = Alignment.TopEnd,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxHeight(),
                ) {
                    StarToggleButton(
                        isStarred = announce.isFavorite || !showFavoriteToggle,
                        onClick = onFavoriteClick,
                    )
                    InterfaceTypeIcon(interfaceType = announce.receivingInterfaceType)
                }
            }
        }
    }
}

@Composable
fun SignalStrengthIndicator(
    hops: Int,
    modifier: Modifier = Modifier,
) {
    // Use remember to avoid recalculating on every recomposition
    val hopsKey = remember(hops) { hops }
    val (strength, color, _) =
        remember(hopsKey) {
            when {
                hopsKey <= 1 -> Triple(3, MeshConnected, "Excellent")
                hopsKey <= 3 -> Triple(2, MeshLimited, "Good")
                else -> Triple(1, MeshOffline, "Weak")
            }
        }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Signal bars
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            for (i in 1..3) {
                Box(
                    modifier =
                        Modifier
                            .width(6.dp)
                            .height((8 + i * 6).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (i <= strength) color else Color.LightGray.copy(alpha = 0.3f)),
                )
            }
        }

        // Hop count
        Text(
            text = pluralStringResource(R.plurals.peer_card_hops, hops, hops),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
        )
    }
}

/**
 * Displays an icon representing the network interface type through which an announce was received.
 * Returns early (renders nothing) for unknown or null interface types.
 */
@Composable
fun InterfaceTypeIcon(
    interfaceType: String?,
    modifier: Modifier = Modifier,
) {
    val type =
        interfaceType?.let {
            runCatching { InterfaceType.valueOf(it) }.getOrNull()
        } ?: return

    if (type == InterfaceType.UNKNOWN) return

    val (icon, contentDescription) =
        when (type) {
            InterfaceType.AUTO_INTERFACE -> Icons.Default.Wifi to stringResource(R.string.peer_card_interface_wifi)
            InterfaceType.TCP_CLIENT -> Icons.Default.Public to stringResource(R.string.peer_card_interface_internet)
            InterfaceType.ANDROID_BLE -> Icons.Default.Bluetooth to stringResource(R.string.peer_card_interface_bluetooth)
            InterfaceType.RNODE -> ImageVector.vectorResource(com.composables.icons.lucide.R.drawable.lucide_ic_antenna) to stringResource(R.string.peer_card_interface_lora_rnode)
            InterfaceType.UNKNOWN -> return
        }

    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier.size(18.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun NodeTypeBadge(nodeType: String) {
    val (text, color) =
        when (nodeType) {
            "NODE" -> stringResource(R.string.announce_stream_node_type_node) to MaterialTheme.colorScheme.tertiary
            "PEER" -> stringResource(R.string.announce_stream_node_type_peer) to MaterialTheme.colorScheme.primary
            "PROPAGATION_NODE" -> stringResource(R.string.announce_stream_node_type_relay) to MaterialTheme.colorScheme.secondary
            "PHONE" -> stringResource(R.string.peer_card_node_type_phone) to MaterialTheme.colorScheme.error
            else -> stringResource(R.string.announce_stream_node_type_node) to MaterialTheme.colorScheme.tertiary
        }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun OtherBadge() {
    val color = MaterialTheme.colorScheme.outline
    val label = stringResource(R.string.peer_card_other)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

/**
 * Format a destination hash string for display.
 */
fun formatHashString(hashString: String): String = hashString
