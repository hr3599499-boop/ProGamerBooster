package com.example.pgb

import android.view.View

object HelpTab {
    fun build(act: MainActivity): View {
        val col = act.vbox()
        col.addView(act.label("راهنما", 18f))
        HelpTexts.sections.forEach { (title, body) ->
            col.addView(act.label(title, 15f))
            col.addView(act.label(body, 13f))
        }
        return act.scrollOf(col)
    }
}
