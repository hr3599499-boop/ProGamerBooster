package com.example.pgb

import android.content.Intent
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout

object DnsTab {

    fun build(act: MainActivity): View {
        val s = act.store
        val ns = act.netState ?: NetMonitor.state(act)
        val col = act.vbox()
        col.addView(act.label("DNS", 18f))
        col.addView(act.label(HelpTexts.DNS_NOTE, 13f))

        val saved = s.dns()
        val active = saved != null && ns.vpn && ns.dns.contains(saved.first)
        col.addView(act.label("وضعیت DNS: ${if (active) "روشن ✓" else "خاموش"}", 16f))
        col.addView(act.label("DNS فعلی گوشی: ${if (ns.dns.isEmpty()) "نامشخص" else ns.dns.joinToString(", ")}", 13f))

        val d1 = EditText(act).apply {
            hint = "DNS 1 (مثلاً 8.8.8.8)"
            setText(saved?.first ?: "")
        }
        val d2 = EditText(act).apply {
            hint = "DNS 2 (مثلاً 8.8.4.4)"
            setText(saved?.second ?: "")
        }
        col.addView(d1)
        col.addView(d2)

        col.addView(act.button("ذخیره و روشن کردن DNS") {
            val a = d1.text.toString().trim()
            val b = d2.text.toString().trim().ifEmpty { a }
            if (!Net.isIpv4(a) || !Net.isIpv4(b)) {
                act.toast("DNS 1 و DNS 2 باید IPv4 معتبر باشند")
            } else {
                s.saveDns(a, b)
                act.enableDns()
            }
        })
        col.addView(act.button("خاموش کردن DNS") {
            act.disableDns()
            act.toast("DNS خاموش شد")
        })
        col.addView(act.button("روش دستی") {
            act.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        })
        col.addView(act.button("پاک کردن ذخیره") {
            s.clearDns()
            act.show("dns")
        })

        col.addView(act.label("DNS خصوصی (تنظیمات گوشی > اینترنت و شبکه > DNS خصوصی > نام میزبان):", 14f))
        Catalog.privateDns.forEach { host ->
            col.addView(act.button(host) { act.copyText(host) })
        }

        col.addView(act.label("فهرست DNSها (روی آدرس بزن تا کپی شود):", 14f))
        Catalog.dns.forEach { (name, a, b) ->
            col.addView(act.label(name, 14f))
            val r = act.hbox()
            r.addView(act.button(a) { act.copyText(a) }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            r.addView(act.button(b) { act.copyText(b) }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            col.addView(r)
        }

        return act.scrollOf(col)
    }
}
