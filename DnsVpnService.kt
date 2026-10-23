package com.example.pgb

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Sends only DNS queries (UDP port 53) to the chosen DNS servers.
 * Every other connection (games included) keeps using the normal route.
 */
class DnsVpnService : VpnService() {

    companion object {
        const val EXTRA_D1 = "d1"
        const val EXTRA_D2 = "d2"
        const val ACTION_STOP = "stop"
    }

    private class Query(val srcIp: ByteArray, val dstIp: ByteArray, val srcPort: Int, val payload: ByteArray)

    @Volatile private var running = false
    private var tun: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopDns()
            stopSelf()
            return START_NOT_STICKY
        }
        val d1 = intent?.getStringExtra(EXTRA_D1)
        val d2 = intent?.getStringExtra(EXTRA_D2) ?: d1
        if (d1 == null || d2 == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        startDns(listOf(d1, d2))
        return START_NOT_STICKY
    }

    override fun onRevoke() {
        stopDns()
        super.onRevoke()
    }

    override fun onDestroy() {
        stopDns()
        super.onDestroy()
    }

    private fun startDns(servers: List<String>) {
        stopDns()
        val builder = Builder()
            .setSession("Pro Gamer Booster DNS")
            .addAddress("10.77.0.2", 32)
            .setMtu(1500)
        servers.forEach { ip ->
            builder.addDnsServer(ip)
            builder.addRoute(ip, 32)
        }
        val fd = builder.establish() ?: return
        tun = fd
        running = true
        Thread { relay(fd, servers) }.start()
    }

    private fun stopDns() {
        running = false
        try {
            tun?.close()
        } catch (_: Exception) {
        }
        tun = null
    }

    private fun relay(fd: ParcelFileDescriptor, servers: List<String>) {
        val input = FileInputStream(fd.fileDescriptor)
        val output = FileOutputStream(fd.fileDescriptor)
        val sock = DatagramSocket()
        protect(sock) // our own DNS requests must not loop back into the tunnel
        sock.soTimeout = 2000
        val buf = ByteArray(32767)
        try {
            while (running) {
                val n = input.read(buf)
                if (n <= 0) break
                val q = parseDnsQuery(buf, n, servers) ?: continue
                try {
                    val upstream = InetAddress.getByAddress(q.dstIp)
                    sock.send(DatagramPacket(q.payload, q.payload.size, upstream, 53))
                    val rbuf = ByteArray(1500)
                    val rp = DatagramPacket(rbuf, rbuf.size)
                    sock.receive(rp)
                    output.write(buildReply(q, rbuf.copyOf(rp.length)))
                } catch (_: Exception) {
                    // timeout or network error: the phone retries the query itself
                }
            }
        } catch (_: Exception) {
        } finally {
            sock.close()
        }
    }

    private fun parseDnsQuery(b: ByteArray, n: Int, servers: List<String>): Query? {
        if (n < 28) return null
        if ((b[0].toInt() shr 4) != 4) return null // IPv4 only
        val ihl = (b[0].toInt() and 0x0f) * 4
        if (b[9].toInt() != 17) return null // UDP only
        val dstIp = b.copyOfRange(16, 20)
        val dst = InetAddress.getByAddress(dstIp).hostAddress ?: return null
        if (dst !in servers) return null
        if (n < ihl + 8) return null
        val srcPort = ((b[ihl].toInt() and 0xff) shl 8) or (b[ihl + 1].toInt() and 0xff)
        val dstPort = ((b[ihl + 2].toInt() and 0xff) shl 8) or (b[ihl + 3].toInt() and 0xff)
        if (dstPort != 53) return null
        val udpLen = ((b[ihl + 4].toInt() and 0xff) shl 8) or (b[ihl + 5].toInt() and 0xff)
        val payloadLen = udpLen - 8
        if (payloadLen <= 0 || ihl + 8 + payloadLen > n) return null
        val payload = b.copyOfRange(ihl + 8, ihl + 8 + payloadLen)
        return Query(b.copyOfRange(12, 16), dstIp, srcPort, payload)
    }

    /** Builds an IPv4 + UDP packet that looks like it came from the DNS server back to the phone. */
    private fun buildReply(q: Query, resp: ByteArray): ByteArray {
        val total = 20 + 8 + resp.size
        val p = ByteArray(total)
        p[0] = 0x45
        p[2] = (total shr 8).toByte()
        p[3] = total.toByte()
        p[6] = 0x40 // don't fragment
        p[8] = 64 // ttl
        p[9] = 17 // UDP
        System.arraycopy(q.dstIp, 0, p, 12, 4) // source = DNS server
        System.arraycopy(q.srcIp, 0, p, 16, 4) // destination = phone
        val c = checksum(p, 0, 20)
        p[10] = (c shr 8).toByte()
        p[11] = c.toByte()
        p[20] = 0
        p[21] = 53
        p[22] = (q.srcPort shr 8).toByte()
        p[23] = q.srcPort.toByte()
        val ul = 8 + resp.size
        p[24] = (ul shr 8).toByte()
        p[25] = ul.toByte()
        // bytes 26-27 stay 0: the UDP checksum is optional over IPv4
        System.arraycopy(resp, 0, p, 28, resp.size)
        return p
    }

    private fun checksum(b: ByteArray, off: Int, len: Int): Int {
        var sum = 0L
        var i = off
        while (i < off + len) {
            val hi = b[i].toInt() and 0xff
            val lo = if (i + 1 < off + len) b[i + 1].toInt() and 0xff else 0
            sum += ((hi shl 8) or lo).toLong()
            i += 2
        }
        while (sum shr 16 != 0L) sum = (sum and 0xffffL) + (sum shr 16)
        return (sum.inv() and 0xffffL).toInt()
    }
}
