package com.example.pgb

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.concurrent.Executors

object Async {
    private val pool = Executors.newFixedThreadPool(4)
    private val main = Handler(Looper.getMainLooper())
    fun bg(work: () -> Unit) { pool.execute { work() } }
    fun ui(work: () -> Unit) { main.post { work() } }
}

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

fun Context.vbox(): LinearLayout = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL
    setPadding(dp(16), dp(16), dp(16), dp(16))
}

fun Context.hbox(): LinearLayout = LinearLayout(this).apply {
    orientation = LinearLayout.HORIZONTAL
    gravity = Gravity.CENTER_VERTICAL
}

fun Context.label(s: String, size: Float = 14f): TextView = TextView(this).apply {
    text = s
    textSize = size
    setTextColor(Color.WHITE)
    setPadding(0, dp(4), 0, dp(4))
}

fun Context.button(s: String, onClick: () -> Unit): Button = Button(this).apply {
    text = s
    setOnClickListener { onClick() }
}

fun Context.scrollOf(child: View): ScrollView = ScrollView(this).apply { addView(child) }

fun Context.toast(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}

fun Context.copyText(s: String) {
    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("pgb", s))
    toast("کپی شد: $s")
}

fun ms(v: Long?): String = if (v == null) "—" else "$v ms"
