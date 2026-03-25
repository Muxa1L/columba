package com.lxmf.messenger.data.model

import android.content.Context
import androidx.annotation.StringRes
import com.lxmf.messenger.R

/**
 * Represents a community TCP server for Reticulum networking.
 *
 * @param name User-friendly name for the server
 * @param host Hostname or IP address
 * @param port TCP port number
 * @param isBootstrap When true, this server is recommended as a bootstrap interface.
 *                    Bootstrap interfaces auto-detach once sufficient discovered
 *                    interfaces are connected (RNS 1.1.0+ feature).
 */
data class TcpCommunityServer(
    val name: String,
    val host: String,
    val port: Int,
    @StringRes val displayNameRes: Int? = null,
    val isBootstrap: Boolean = false,
) {
    fun localizedDisplayName(context: Context): String =
        displayNameRes?.let { resId -> runCatching { context.getString(resId) }.getOrDefault(name) } ?: name
}

/**
 * List of known community TCP servers for Reticulum.
 *
 * Selected servers are marked as bootstrap candidates based on:
 * - Reputation in the community
 * - Long-term reliability
 * - Geographic distribution
 */
object TcpCommunityServers {
    val servers: List<TcpCommunityServer> =
        listOf(
            // Bootstrap servers: well-established, reliable nodes for initial network discovery
            TcpCommunityServer("Beleth RNS Hub", "rns.beleth.net", 4242, R.string.tcp_client_server_beleth_rns_hub, isBootstrap = true),
            TcpCommunityServer("Quad4 TCP Node 1", "rns.quad4.io", 4242, R.string.tcp_client_server_quad4_tcp_node_1, isBootstrap = true),
            TcpCommunityServer("FireZen", "firezen.com", 4242, R.string.tcp_client_server_firezen, isBootstrap = true),
            // Regular community servers
            TcpCommunityServer("g00n.cloud Hub", "dfw.us.g00n.cloud", 6969, R.string.tcp_client_server_g00n_cloud_hub),
            TcpCommunityServer("interloper node", "intr.cx", 4242, R.string.tcp_client_server_interloper_node),
            TcpCommunityServer(
                "interloper node (Tor)",
                "intrcxv4fa72e5ovler5dpfwsiyuo34tkcwfy5snzstxkhec75okowqd.onion",
                4242,
                R.string.tcp_client_server_interloper_node_tor,
            ),
            TcpCommunityServer("Jon's Node", "rns.jlamothe.net", 4242, R.string.tcp_client_server_jons_node),
            TcpCommunityServer("noDNS1", "202.61.243.41", 4965, R.string.tcp_client_server_nodns1),
            TcpCommunityServer("noDNS2", "193.26.158.230", 4965, R.string.tcp_client_server_nodns2),
            TcpCommunityServer("NomadNode SEAsia TCP", "rns.jaykayenn.net", 4242, R.string.tcp_client_server_nomadnode_seasia_tcp),
            TcpCommunityServer("0rbit-Net", "93.95.227.8", 49952, R.string.tcp_client_server_0rbit_net),
            TcpCommunityServer("Quad4 TCP Node 2", "rns2.quad4.io", 4242, R.string.tcp_client_server_quad4_tcp_node_2),
            TcpCommunityServer("Quortal TCP Node", "reticulum.qortal.link", 4242, R.string.tcp_client_server_quortal_tcp_node),
            TcpCommunityServer("R-Net TCP", "istanbul.reserve.network", 9034, R.string.tcp_client_server_r_net_tcp),
            TcpCommunityServer("RNS bnZ-NODE01", "node01.rns.bnz.se", 4242, R.string.tcp_client_server_rns_bnz_node01),
            TcpCommunityServer("RNS COMSEC-RD", "80.78.23.249", 4242, R.string.tcp_client_server_rns_comsec_rd),
            TcpCommunityServer("RNS HAM RADIO", "135.125.238.229", 4242, R.string.tcp_client_server_rns_ham_radio),
            TcpCommunityServer("RNS Testnet StoppedCold", "rns.stoppedcold.com", 4242, R.string.tcp_client_server_rns_testnet_stoppedcold),
            TcpCommunityServer("RNS_Transport_US-East", "45.77.109.86", 4965, R.string.tcp_client_server_rns_transport_us_east),
            TcpCommunityServer("SparkN0de", "aspark.uber.space", 44860, R.string.tcp_client_server_sparkn0de),
            TcpCommunityServer("Tidudanka.com", "reticulum.tidudanka.com", 37500, R.string.tcp_client_server_tidudanka),
        )

    /**
     * Get only servers marked as bootstrap candidates.
     */
    val bootstrapServers: List<TcpCommunityServer>
        get() = servers.filter { it.isBootstrap }
}
