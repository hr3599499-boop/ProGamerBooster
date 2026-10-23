package com.example.pgb

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities

data class NetState(val type: String, val vpn: Boolean, val dns: List<String>)

object NetMonitor {

    /** What the phone is using right now: network type, whether a VPN/WireGuard is active, and the DNS in use. */
    fun state(ctx: Context): NetState {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return NetState("بدون اینترنت", false, emptyList())
        val caps: NetworkCapabilities? = cm.getNetworkCapabilities(net)
        val lp: LinkProperties? = cm.getLinkProperties(net)
        val type = when {
            caps == null -> "نامشخص"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "موبایل"
            else -> "دیگر"
        }
        val vpn = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        val dns = lp?.dnsServers?.mapNotNull { it.hostAddress } ?: emptyList()
        return NetState(type, vpn, dns)
    }

    /** Calls onChange whenever the default network changes (VPN/WireGuard on or off, Wi-Fi, DNS). */
    fun watch(ctx: Context, onChange: () -> Unit): ConnectivityManager.NetworkCallback {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                onChange()
            }

            override fun onLinkPropertiesChanged(network: Network, lp: LinkProperties) {
                onChange()
            }

            override fun onLost(network: Network) {
                onChange()
            }
        }
        cm.registerDefaultNetworkCallback(cb)
        return cb
    }

    fun unwatch(ctx: Context, cb: ConnectivityManager.NetworkCallback) {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.unregisterNetworkCallback(cb)
    }
}
