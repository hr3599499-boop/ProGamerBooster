package com.example.pgb

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build

object Specs {
    fun lines(ctx: Context): List<Pair<String, String>> {
        val out = mutableListOf<Pair<String, String>>()
        out.add("مدل" to "${Build.MANUFACTURER} ${Build.MODEL}")
        out.add("اندروید" to Build.VERSION.RELEASE)
        out.add("تعداد هسته‌ها" to Runtime.getRuntime().availableProcessors().toString())

        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        out.add("RAM کل" to String.format("%.1f GB", mi.totalMem / 1073741824.0))
        out.add("RAM آزاد" to String.format("%.1f GB", mi.availMem / 1073741824.0))

        val bat = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val lvl = bat?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = bat?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        out.add("باتری" to if (lvl >= 0 && scale > 0) "${lvl * 100 / scale}%" else "نامشخص")

        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        val net = when {
            caps == null -> "بدون اینترنت"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "موبایل"
            else -> "دیگر"
        }
        out.add("نوع شبکه" to net)
        return out
    }
}
