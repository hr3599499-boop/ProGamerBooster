package com.example.pgb

import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout

object BoostTab {

    fun build(act: MainActivity): View {
        val s = act.store
        val ns = act.netState ?: NetMonitor.state(act)
        val col = act.vbox()
        col.addView(act.label("ظ¾غŒظ†ع¯ ظˆ ط´ط¨ع©ظ‡", 18f))
        col.addView(act.label(HelpTexts.BOOST_NOTE, 13f))

        col.addView(act.label("ظ†ظˆط¹ ط´ط¨ع©ظ‡: ${ns.type}", 14f))
        col.addView(act.label("VPN / WireGuard: ${if (ns.vpn) "ط±ظˆط´ظ†" else "ط®ط§ظ…ظˆط´"}", 14f))
        col.addView(act.label("DNS ظپط¹ظ„غŒ ع¯ظˆط´غŒ: ${if (ns.dns.isEmpty()) "ظ†ط§ظ…ط´ط®طµ" else ns.dns.joinToString(", ")}", 14f))
        col.addView(act.label("ظ¾غŒظ†ع¯ ط§طھطµط§ظ„ (ط¨ظ‡ 1.1.1.1): ${ms(act.connPing)}", 14f))
        col.addView(act.label(HelpTexts.NET_NOTE, 13f))

        val sel = s.selectedServerId()
        val servers = s.servers()
        col.addView(
            act.label(
                if (servers.isEmpty()) {
                    "ظ‡ظ†ظˆط² ط³ط±ظˆط±غŒ ط§ط¶ط§ظپظ‡ ظ†ع©ط±ط¯ظ‡â€Œط§غŒ. ط¨ط¯ظˆظ† ط³ط±ظˆط±طŒ ظ¾غŒظ†ع¯ ط§طھطµط§ظ„ ط¨ظ‡ 1.1.1.1 ط±ط§ ط§ظ†ط¯ط§ط²ظ‡ ظ…غŒâ€Œع¯غŒط±ط¯."
                } else {
                    "ط±ظˆغŒ ط³ط±ظˆط± ط¨ط²ظ† طھط§ ط¨ط±ط§غŒ ظ¾غŒظ†ع¯ ظˆ ط´ط±ظˆط¹ ط¨ط§ط²غŒ ط§ظ†طھط®ط§ط¨ ط´ظˆط¯. ط¨ط¯ظˆظ† ط§ظ†طھط®ط§ط¨طŒ 1.1.1.1 ط§ط³طھظپط§ط¯ظ‡ ظ…غŒâ€Œط´ظˆط¯."
                },
                13f
            )
        )

        servers.forEach { srv ->
            val r = act.hbox()
            val mark = if (srv.id == sel) "âœ“ " else ""
            val tv = act.label("$mark${srv.name}  ${srv.ip}:${srv.port}  ${ms(act.serverPings[srv.id])}", 14f)
            tv.setOnClickListener {
                s.selectServer(srv.id)
                act.show("boost")
            }
            r.addView(tv, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            r.addView(act.button(if (srv.id == sel) "âœ“ ط§ظ†طھط®ط§ط¨ ط´ط¯ظ‡" else "ط§ظ†طھط®ط§ط¨") {
                s.selectServer(srv.id)
                act.show("boost")
            })
            r.addView(act.button("ط­ط°ظپ") {
                s.saveServers(servers.filter { it.id != srv.id })
                if (sel == srv.id) s.selectServer(null)
                act.show("boost")
            })
            col.addView(r)
        }

        val nameIn = EditText(act).apply { hint = "ظ†ط§ظ… ط³ط±ظˆط± (ظ…ط«ظ„ط§ظ‹ ط§غŒطھط§ظ„غŒط§)" }
        val ipIn = EditText(act).apply { hint = "IP غŒط§ ط¯ط§ظ…ظ†ظ‡" }
        val portIn = EditText(act).apply {
            hint = "ظ¾ظˆط±طھ (ظ…ط«ظ„ط§ظ‹ 443)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        col.addView(nameIn)
        col.addView(ipIn)
        col.addView(portIn)

        col.addView(act.button("ط§ظپط²ظˆط¯ظ† ط³ط±ظˆط±") {
            val ip = ipIn.text.toString().trim()
            val port = portIn.text.toString().trim().toIntOrNull()
            if (ip.isEmpty() || port == null || port !in 1..65535) {
                act.toast("IP غŒط§ ظ¾ظˆط±طھ ط¯ط±ط³طھ ظ†غŒط³طھ")
            } else {
                val name = nameIn.text.toString().trim().ifEmpty { ip }
                s.saveServers(servers + Server(System.currentTimeMillis().toString(), name, ip, port))
                act.show("boost")
            }
        })

        col.addView(act.button("طھط³طھ ظ¾غŒظ†ع¯â€Œظ‡ط§") {
            act.toast("ط¯ط± ط­ط§ظ„ ط§ظ†ط¯ط§ط²ظ‡â€Œع¯غŒط±غŒ...")
            act.refreshNetwork()
            Async.bg {
                val res = s.servers().map { it.id to Net.tcpPing(it.ip, it.port) }
                Async.ui {
                    res.forEach { (id, p) -> act.serverPings[id] = p }
                    act.show("boost")
                }
            }
        })

        return act.scrollOf(col)
    }
}
