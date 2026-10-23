package com.example.pgb

import android.app.AlertDialog
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView

object GamesTab {

    fun build(act: MainActivity): View {
        val col = act.vbox()
        col.addView(act.label("بازی‌های من", 18f))
        col.addView(act.label(HelpTexts.GAMES_NOTE, 13f))
        col.addView(act.button("افزودن بازی") { addDialog(act) })
        col.addView(act.button("تشخیص بازی‌های نصب‌شده") { detectInstalled(act) })
        col.addView(act.button("خواندن بازی‌ها از اسکرین‌شات") { Scan.pickImages(act) })
        col.addView(act.button("کلید Claude (برای خواندن از عکس)") { Scan.askKey(act) {} })

        val games = act.store.games()
        if (games.isEmpty()) col.addView(act.label("هنوز بازی‌ای اضافه نکرده‌ای."))
        games.forEach { col.addView(row(act, it)) }
        return act.scrollOf(col)
    }

    private fun row(act: MainActivity, g: Game): View {
        val r = act.hbox()
        r.addView(Icons.view(act, g, 44))

        val info = LinearLayout(act).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(act.dp(10), 0, act.dp(6), 0)
            setOnClickListener { details(act, g) }
        }
        val status = if (Launcher.isInstalled(act, g.pkg)) "نصب شده" else "نصب نشده"
        info.addView(act.label(g.name, 15f))
        info.addView(act.label("${g.genre} • $status", 12f))
        r.addView(info, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        if (g.boosted) r.addView(act.label("پینگ: ${ms(g.ping)}", 13f))
        r.addView(act.button("جزئیات") { details(act, g) })
        r.addView(act.button(if (g.boosted) "فعال ✓" else "شروع") { boost(act, g) })
        r.addView(act.button("×") {
            act.store.saveGames(act.store.games().filter { it.name != g.name })
            act.show("games")
        })
        return r
    }

    private fun detectInstalled(act: MainActivity) {
        val games = act.store.games()
        val have = games.map { it.name }.toMutableSet()
        var added = 0
        Catalog.games.forEach { c ->
            if (c.name !in have && Launcher.isInstalled(act, c.pkg)) {
                games.add(Game(c.name, c.pkg, c.genre, false, null))
                have.add(c.name)
                added++
            }
        }
        act.store.saveGames(games)
        act.toast(if (added == 0) "بازی نصب‌شده‌ی جدیدی از فهرست پیدا نشد" else "$added بازی نصب‌شده اضافه شد")
        act.show("games")
    }

    private fun addDialog(act: MainActivity) {
        val have = act.store.games().map { it.name }.toSet()
        val avail = Catalog.games.filter { it.name !in have }

        val list = ListView(act).apply {
            choiceMode = AbsListView.CHOICE_MODE_MULTIPLE
            adapter = ArrayAdapter(
                act,
                android.R.layout.simple_list_item_multiple_choice,
                avail.map { c ->
                    val st = if (Launcher.isInstalled(act, c.pkg)) " ✓ نصب شده" else ""
                    "${c.name} — ${c.genre}$st"
                }
            )
        }
        val custom = EditText(act).apply { hint = "نام بازی دلخواه (اختیاری)" }
        val box = act.vbox()
        box.addView(custom)
        box.addView(list, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, act.dp(360)))

        AlertDialog.Builder(act)
            .setTitle("افزودن بازی")
            .setView(box)
            .setPositiveButton("افزودن") { _, _ ->
                val games = act.store.games()
                val checked = list.checkedItemPositions
                avail.forEachIndexed { i, c ->
                    if (checked.get(i)) games.add(Game(c.name, c.pkg, c.genre, false, null))
                }
                val cn = custom.text.toString().trim()
                if (cn.isNotEmpty() && games.none { it.name == cn }) {
                    games.add(Game(cn, null, "بازی دلخواه", false, null))
                }
                act.store.saveGames(games)
                act.show("games")
            }
            .setNegativeButton("لغو", null)
            .show()
    }

    /** Each game has its own server. If it is not set, the selected server (or 1.1.1.1) is used. */
    private fun boost(act: MainActivity, g: Game) {
        Async.bg {
            val gip = g.ip
            val gport = g.port
            val sel = act.store.selectedServer()
            val ping: Long?
            val target: String
            if (gip != null && gport != null) {
                ping = Net.tcpPing(gip, gport)
                target = "سرور ${g.name}"
            } else if (sel != null) {
                ping = Net.tcpPing(sel.ip, sel.port)
                target = sel.name
            } else {
                ping = Net.tcpPing("1.1.1.1", 443)
                target = "1.1.1.1"
            }
            Async.ui {
                act.store.updateGame(g.name) { it.copy(boosted = true, ping = ping) }
                act.toast("پینگ تا $target: ${ms(ping)}")
                Launcher.launch(act, g)
                act.show("games")
            }
        }
    }

    private fun setGameServer(act: MainActivity, g: Game, onDone: () -> Unit) {
        val ipIn = EditText(act).apply {
            hint = "IP یا دامنه‌ی سرور این بازی"
            setText(g.ip ?: "")
        }
        val portIn = EditText(act).apply {
            hint = "پورت (مثلاً 443)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(g.port?.toString() ?: "")
        }
        val box = act.vbox()
        box.addView(ipIn)
        box.addView(portIn)

        AlertDialog.Builder(act)
            .setTitle("سرور ${g.name}")
            .setView(box)
            .setPositiveButton("ذخیره") { _, _ ->
                val ip = ipIn.text.toString().trim()
                val port = portIn.text.toString().trim().toIntOrNull()
                if (ip.isEmpty() || port == null || port !in 1..65535) {
                    act.toast("IP یا پورت درست نیست")
                } else {
                    act.store.updateGame(g.name) { it.copy(ip = ip, port = port) }
                    act.toast("سرور ${g.name} ذخیره شد")
                    onDone()
                    act.show("games")
                }
            }
            .setNegativeButton("لغو", null)
            .show()
    }

    private fun details(act: MainActivity, g: Game) {
        var dlg: AlertDialog? = null
        val box = act.vbox()
        val ns = NetMonitor.state(act)
        box.addView(act.label("شبکه: ${ns.type} • VPN/WireGuard: ${if (ns.vpn) "روشن" else "خاموش"}"))
        box.addView(act.label("DNS فعلی: ${if (ns.dns.isEmpty()) "نامشخص" else ns.dns.joinToString(", ")}"))

        val connTv = act.label("پینگ اتصال (به 1.1.1.1): در حال اندازه‌گیری...")
        box.addView(connTv)
        if (g.boosted) box.addView(act.label("پینگ هنگام شروع: ${ms(g.ping)}"))

        val gip = g.ip
        val gport = g.port
        val hasGameServer = gip != null && gport != null
        val gameTv = act.label(
            if (hasGameServer) "سرور این بازی ($gip:$gport): در حال اندازه‌گیری..." else "سرور این بازی: تنظیم نشده"
        )
        box.addView(gameTv)
        box.addView(act.button(if (hasGameServer) "تغییر سرور این بازی" else "تنظیم سرور این بازی") {
            setGameServer(act, g) { dlg?.dismiss() }
        })
        if (hasGameServer) {
            box.addView(act.button("حذف سرور این بازی") {
                act.store.updateGame(g.name) { it.copy(ip = null, port = null) }
                dlg?.dismiss()
                act.show("games")
            })
        }

        val servers = act.store.servers()
        val srvTvs = servers.map { srv ->
            act.label("${srv.name} (${srv.ip}:${srv.port}): در حال اندازه‌گیری...").also { box.addView(it) }
        }
        if (servers.isEmpty()) {
            box.addView(act.label("برای پینگ سرور دلخواه، در تب پینگ سرور اضافه کن."))
        }

        box.addView(act.label("مشخصات دستگاه", 16f))
        Specs.lines(act).forEach { (k, v) -> box.addView(act.label("$k: $v")) }

        dlg = AlertDialog.Builder(act)
            .setTitle(g.name)
            .setView(act.scrollOf(box))
            .setPositiveButton("باز کردن بازی") { _, _ -> Launcher.launch(act, g) }
            .setNegativeButton("بستن", null)
            .show()

        Async.bg {
            val conn = Net.tcpPing("1.1.1.1", 443)
            val gp = if (gip != null && gport != null) Net.tcpPing(gip, gport) else null
            val srvPings = servers.map { Net.tcpPing(it.ip, it.port) }
            Async.ui {
                connTv.text = "پینگ اتصال (به 1.1.1.1): ${ms(conn)}"
                if (gip != null && gport != null) {
                    gameTv.text = "سرور این بازی ($gip:$gport): ${ms(gp)}"
                }
                srvTvs.forEachIndexed { i, tv ->
                    tv.text = "${servers[i].name} (${servers[i].ip}:${servers[i].port}): ${ms(srvPings[i])}"
                }
            }
        }
    }
}
