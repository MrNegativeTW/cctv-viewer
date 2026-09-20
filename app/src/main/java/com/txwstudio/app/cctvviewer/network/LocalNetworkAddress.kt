package com.txwstudio.app.cctvviewer.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.Inet4Address

object LocalNetworkAddress {
    @Suppress("DEPRECATION")
    fun find(context: Context): String? {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return null
        val networks = (listOfNotNull(manager.activeNetwork) + manager.allNetworks).distinct()
        return networks.firstNotNullOfOrNull { network ->
            val capabilities = manager.getNetworkCapabilities(network) ?: return@firstNotNullOfOrNull null
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                    !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
            ) return@firstNotNullOfOrNull null
            manager.getLinkProperties(network)?.linkAddresses?.map { it.address }
                ?.filterIsInstance<Inet4Address>()
                ?.firstOrNull { !it.isLoopbackAddress && !it.isAnyLocalAddress }?.hostAddress
        }
    }
}
