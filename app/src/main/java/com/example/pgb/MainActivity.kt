package com.example.pgb

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout

class MainActivity : Activity() {
    companion object {
        const val REQ_VPN = 42
    }

    lateinit var store: Store
    var current = "games"
    val serverPings = HashMap<String, Long?>()
    var connPing: Long? = null
    var netState: NetState? = null

    private lateinit var content: FrameLayout
    private lateinit var netCb: ConnectivityManager.NetworkCallback
    private val main = Handler(Looper.getMainLooper())
    private val refreshRunnable = Runnable { refreshNetwork() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = Store(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(11, 18, 51))
        }
        content = FrameLayout(this)
        root.addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val tabs = listOf("games" to "بازی‌ها", "boost" to "پینگ", "dns" to "DNS", "help" to "راهنما")
        tabs.forEach { (key, title) ->
            val b = Button(this).apply {
                text = title
                setOnClickListener { show(key) }
            }
            nav.addView(b, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
        root.addView(nav)
        setContentView(root)

        netCb = NetMonitor.watch(this) {
            main.removeCallbacks(refreshRunnable)
            main.postDelayed(refreshRunnable, 800)
        }

        show("games")
        refreshNetwork()
    }

    override fun onDestroy() {
        NetMonitor.unwatch(this, netCb)
        super.onDestroy()
    }

    /** Re-reads network state and re-measures the connection ping (includes VPN/WireGuard path). */
    fun refreshNetwork() {
        netState = NetMonitor.state(this)
        Async.bg {
            val p = Net.tcpPing("1.1.1.1", 443)
            Async.ui {
                connPing = p
                if (current == "boost" || current == "games" || current == "dns") show(current)
            }
        }
    }

    fun show(key: String) {
        current = key
        val v = when (key) {
            "boost" -> BoostTab.build(this)
            "dns" -> DnsTab.build(this)
            "help" -> HelpTab.build(this)
            else -> GamesTab.build(this)
        }
        content.removeAllViews()
        content.addView(v)
    }

    /** Turns the DNS on for all apps. Android asks the user to allow the VPN the first time. */
    fun enableDns() {
        if (store.dns() == null) {
            toast("اول دو آدرس DNS را ذخیره کن")
            return
        }
        val prep = VpnService.prepare(this)
        if (prep != null) startActivityForResult(prep, REQ_VPN) else startDnsService()
    }

    fun disableDns() {
        startService(Intent(this, DnsVpnService::class.java).setAction(DnsVpnService.ACTION_STOP))
        main.postDelayed(refreshRunnable, 1000)
    }

    private fun startDnsService() {
        val d = store.dns() ?: return
        val i = Intent(this, DnsVpnService::class.java)
            .putExtra(DnsVpnService.EXTRA_D1, d.first)
            .putExtra(DnsVpnService.EXTRA_D2, d.second)
        startService(i)
        main.postDelayed(refreshRunnable, 1500)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_VPN && resultCode == RESULT_OK) startDnsService()
        if (requestCode == Scan.REQ_IMG && resultCode == RESULT_OK) Scan.onResult(this, data)
    }
}
