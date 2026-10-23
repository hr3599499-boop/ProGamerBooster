package com.example.pgb

import java.net.InetSocketAddress
import java.net.Socket

object Net {
    /** Best of 3 TCP handshakes to host:port, in ms. Null if unreachable. */
    fun tcpPing(host: String, port: Int): Long? {
        var best: Long? = null
        repeat(3) {
            try {
                Socket().use { s ->
                    val t0 = System.nanoTime()
                    s.connect(InetSocketAddress(host, port), 3000)
                    val ms = (System.nanoTime() - t0) / 1_000_000
                    best = best?.let { minOf(it, ms) } ?: ms
                }
            } catch (_: Exception) {
            }
        }
        return best
    }

    fun isIpv4(v: String): Boolean {
        val part = "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)"
        return Regex("$part(\\.$part){3}").matches(v)
    }
}
