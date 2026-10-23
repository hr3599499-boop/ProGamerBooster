package com.example.pgb

import android.content.Context
import android.content.Intent
import android.net.Uri

object Launcher {
    /** True if the package is installed and has a launcher screen (needs the <queries> entry in the manifest). */
    fun isInstalled(ctx: Context, pkg: String?): Boolean {
        if (pkg == null) return false
        return ctx.packageManager.getLaunchIntentForPackage(pkg) != null
    }

    /** Opens the installed game. If it is not installed, opens its Google Play page. */
    fun launch(ctx: Context, g: Game) {
        val intent = g.pkg?.let { ctx.packageManager.getLaunchIntentForPackage(it) }
        if (intent != null) {
            ctx.startActivity(intent)
            return
        }
        val url = if (g.pkg != null) {
            "https://play.google.com/store/apps/details?id=${g.pkg}"
        } else {
            "https://play.google.com/store/search?q=${Uri.encode(g.name)}&c=apps"
        }
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
