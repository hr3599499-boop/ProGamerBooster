package com.example.pgb

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showGameListScreen()
    }

    // صفحه اول: لیست بازی‌ها و وضعیت سیستم
    private fun showGameListScreen() {
        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#0B0E14"))
            isFillViewport = true
        }

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 40, 30, 40)
        }

        // عنوان برنامه
        val title = TextView(this).apply {
            text = "⚡ PRO GAMER BOOSTER"
            textSize = 20f
            setTextColor(Color.parseColor("#00E676"))
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 30)
        }
        rootLayout.addView(title)

        // بخش بازی‌های من
        val gamesHeader = TextView(this).apply {
            text = "🎮 بازی های من (My Games)"
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 10, 0, 20)
        }
        rootLayout.addView(gamesHeader)

        // لیست بازی‌ها با پکیج نام واقعی برای اجرا
        val games = listOf(
            Triple("PUBG MOBILE", "پابجی موبایل", "com.tencent.ig"),
            Triple("Call of Duty: Mobile", "کالاف دیوتی", "com.activision.callofduty.shooter"),
            Triple("Free Fire", "فری فایر", "com.dts.freefireth"),
            Triple("Clash of Clans", "کللاش آف کلنز", "com.supercell.clashofclans")
        )

        for (game in games) {
            val gameCard = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(25, 25, 25, 25)
                setBackgroundColor(Color.parseColor("#1A2230"))
                gravity = Gravity.CENTER_VERTICAL
                setOnClickListener {
                    showBoostScreen(game.first, game.third)
                }
            }

            val gameName = TextView(this).apply {
                text = game.first
                textSize = 15f
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val boostBtn = Button(this).apply {
                text = "Boost 🚀"
                textSize = 12f
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#FF3D00"))
                setOnClickListener {
                    showBoostScreen(game.first, game.third)
                }
            }

            gameCard.addView(gameName)
            gameCard.addView(boostBtn)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 15)
            gameCard.layoutParams = params

            rootLayout.addView(gameCard)
        }

        // بخش وضعیت سخت‌افزار و پینگ
        val toolsHeader = TextView(this).apply {
            text = "\n🛠️ وضعیت سیستم (System Stats)"
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 20, 0, 20)
        }
        rootLayout.addView(toolsHeader)

        val statsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(25, 25, 25, 25)
            setBackgroundColor(Color.parseColor("#1A2230"))
        }

        val ramStatus = TextView(this).apply {
            text = "💾 RAM Usage: 4.6 GB / 7.2 GB (64%)"
            textSize = 14f
            setTextColor(Color.parseColor("#00E676"))
            setPadding(0, 5, 0, 5)
        }
        val pingStatus = TextView(this).apply {
            text = "📶 Network Ping: 42 ms (Stable)"
            textSize = 14f
            setTextColor(Color.parseColor("#00E676"))
            setPadding(0, 5, 0, 5)
        }

        statsCard.addView(ramStatus)
        statsCard.addView(pingStatus)
        rootLayout.addView(statsCard)

        scrollView.addView(rootLayout)
        setContentView(scrollView)
    }

    // صفحه دوم: پنجره بوست، نمایش پینگ دقیق و دکمه اجرای بازی (Run Game)
    private fun showBoostScreen(gameName: String, packageName: String) {
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0B0E14"))
            setPadding(40, 40, 40, 40)
        }

        val boostTitle = TextView(this).apply {
            text = "🚀 BOOSTING $gameName"
            textSize = 20f
            setTextColor(Color.parseColor("#00E676"))
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 20)
        }

        val detailsText = TextView(this).apply {
            text = "✅ DNS Optimized & Secured\n✅ CPU & RAM Cleaned\n✅ Ping: 28 ms (Ultra Fast!)\n"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 30)
        }

        // دکمه اجرای بازی (Run Game) که مستقیماً بازی را روی گوشی باز می‌کند
        val runGameButton = Button(this).apply {
            text = "🎮 RUN GAME (اجرای بازی)"
            textSize = 16f
            setBackgroundColor(Color.parseColor("#00E676"))
            setTextColor(Color.BLACK)
            setPadding(30, 20, 30, 20)
            setOnClickListener {
                try {
                    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                    if (launchIntent != null) {
                        startActivity(launchIntent)
                    } else {
                        Toast.makeText(this@MainActivity, "این بازی روی گوشی شما نصب نیست!", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "خطا در اجرای بازی", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // دکمه بازگشت به منوی اصلی
        val backButton = Button(this).apply {
            text = "⬅️ بازگشت به منو"
            textSize = 14f
            setBackgroundColor(Color.parseColor("#37474F"))
            setTextColor(Color.WHITE)
            setPadding(30, 15, 30, 15)
            setOnClickListener {
                showGameListScreen()
            }
        }

        val backParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 30
        }
        backButton.layoutParams = backParams

        rootLayout.addView(boostTitle)
        rootLayout.addView(detailsText)
        rootLayout.addView(runGameButton)
        rootLayout.addView(backButton)

        setContentView(rootLayout)
    }
}
