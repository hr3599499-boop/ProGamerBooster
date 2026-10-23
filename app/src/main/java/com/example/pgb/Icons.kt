package com.example.pgb

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

object Icons {
    /** Real app icon for installed games; a colored tile with initials otherwise. */
    fun view(ctx: Context, g: Game, sizeDp: Int): View {
        val size = ctx.dp(sizeDp)
        val real = g.pkg?.let { p ->
            try {
                ctx.packageManager.getApplicationIcon(p)
            } catch (_: Exception) {
                null
            }
        }
        if (real != null) {
            return ImageView(ctx).apply {
                setImageDrawable(real)
                layoutParams = LinearLayout.LayoutParams(size, size)
            }
        }
        val hue = (g.name.hashCode() and 0x7fffffff) % 360
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = ctx.dp(12).toFloat()
            setColor(Color.HSVToColor(floatArrayOf(hue.toFloat(), 0.6f, 0.45f)))
        }
        val initials = g.name.split(" ").filter { it.isNotEmpty() }.take(2)
            .joinToString("") { it.first().uppercase() }
        return TextView(ctx).apply {
            text = initials
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = bg
            layoutParams = LinearLayout.LayoutParams(size, size)
        }
    }
}
