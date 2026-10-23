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
        col.addView(act.label("پینگ و شبکه", 18f))
        col.addView(act.label(HelpTexts.BOOST_NOTE, 13f))

        col.addView(act.label("نوع شبکه: ${ns.type}", 14f))
        col.addView(act.label("VPN / WireGuard: ${if (ns.vpn) "روشن" else "خاموش"}", 14f))
        col.addView(act.label("DNS فعلی گوشی: ${if (ns.dns.isEmpty()) "نامشخص" else ns.dns.joinToString(", ")}", 14f))
        col.addView(act.label("پینگ اتصال (به 1.1.1.1): ${ms(act.connPing)}", 14f))
        col.addView(act.label(HelpTexts.NET_NOTE, 13f))

        val sel = s.selectedServerId()
        val servers = s.servers()
        col.addView(
            act.label(
                if (servers.isEmpty()) {
                    "هنوز سروری اضافه نکرده‌ای. بدون سرور، پینگ اتصال به 1.1.1.1 را اندازه می‌گیرد."
                } else {
                    "روی سرور بزن تا برای پینگ و شروع بازی انتخاب شود. بدون انتخاب، 1.1.1.1 استفاده می‌شود."
                },
                13f
            )
        )

        servers.forEach { srv ->
            val r = act.hbox()
            val mark = if (srv.id == sel) "✓ " else ""
            val tv = act.label("$mark${srv.name}  ${srv.ip}:${srv.port}  ${ms(act.serverPings[srv.id])}", 14f)
            tv.setOnClickListener {
                s.selectServer(srv.id)
                act.show("boost")
            }
            r.addView(tv, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            r.addView(act.button(if (srv.id == sel) "✓ انتخاب شده" else "انتخاب") {
                s.selectServer(srv.id)
                act.show("boost")
            })
            r.addView(act.button("حذف") {
                s.saveServers(servers.filter { it.id != srv.id })
                if (sel == srv.id) s.selectServer(null)
                act.show("boost")
            })
            col.addView(r)
        }

        val nameIn = EditText(act).apply { hint = "نام سرور (مثلاً ایتالیا)" }
        val ipIn = EditText(act).apply { hint = "IP یا دامنه" }
        val portIn = EditText(act).apply {
            hint = "پورت (مثلاً 443)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        col.addView(nameIn)
        col.addView(ipIn)
        col.addView(portIn)

        col.addView(act.button("افزودن سرور") {
            val ip = ipIn.text.toString().trim()
            val port = portIn.text.toString().trim().toIntOrNull()
            if (ip.isEmpty() || port == null || port !in 1..65535) {
                act.toast("IP یا پورت درست نیست")
            } else {
                val name = nameIn.text.toString().trim().ifEmpty { ip }
                s.saveServers(servers + Server(System.currentTimeMillis().toString(), name, ip, port))
                act.show("boost")
            }
        })

        col.addView(act.button("تست پینگ‌ها") {
            act.toast("در حال اندازه‌گیری...")
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
