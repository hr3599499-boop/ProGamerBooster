package com.example.pgb

data class CatItem(val name: String, val pkg: String?, val genre: String)

object Catalog {
    val games = listOf(
        CatItem("Call of Duty: Mobile", "com.activision.callofduty.shooter", "نبرد سلطنتی • شوتر آنلاین"),
        CatItem("PUBG Mobile", "com.tencent.ig", "نبرد سلطنتی • شوتر آنلاین"),
        CatItem("Free Fire", "com.dts.freefireth", "نبرد سلطنتی • شوتر آنلاین"),
        CatItem("Arena Breakout", null, "شوتر واقع‌گرایانه • آنلاین"),
        CatItem("Standoff 2", null, "شوتر • آنلاین"),
        CatItem("Critical Ops", null, "شوتر • آنلاین"),
        CatItem("Mobile Legends", "com.mobilelegends.mi", "موبا • پنج در پنج آنلاین"),
        CatItem("Honor of Kings", null, "موبا • پنج در پنج آنلاین"),
        CatItem("Wild Rift", "com.riotgames.league.wildrift", "موبا • پنج در پنج آنلاین"),
        CatItem("Genshin Impact", "com.miHoYo.GenshinImpact", "نقش‌آفرینی • جهان باز آنلاین"),
        CatItem("Honkai: Star Rail", "com.HoYoverse.hkrpgoversea", "نقش‌آفرینی • آنلاین"),
        CatItem("Zenless Zone Zero", "com.HoYoverse.Nap", "اکشن • آنلاین"),
        CatItem("Wuthering Waves", null, "نقش‌آفرینی • جهان باز آنلاین"),
        CatItem("Tower of Fantasy", null, "نقش‌آفرینی • جهان باز آنلاین"),
        CatItem("Black Desert Mobile", null, "نقش‌آفرینی • MMO"),
        CatItem("Lineage M", null, "نقش‌آفرینی • MMO"),
        CatItem("Ragnarok M: Eternal Love", null, "نقش‌آفرینی • MMO"),
        CatItem("Dead by Daylight Mobile", null, "ترسناک • چندنفره آنلاین"),
        CatItem("Asphalt 9: Legends", "com.gameloft.android.ANMP.GloftA9HM", "مسابقه‌ی ماشین • آنلاین"),
        CatItem("EA Sports FC Mobile", null, "ورزشی • فوتبال آنلاین"),
        CatItem("eFootball", null, "ورزشی • فوتبال آنلاین")
    )

    val dns = listOf(
        Triple("الکترو (Electro) - مخصوص بازی", "78.157.42.100", "78.157.42.101"),
        Triple("شکن", "178.22.122.100", "185.51.200.2"),
        Triple("Cloudflare", "1.1.1.1", "1.0.0.1"),
        Triple("Google", "8.8.8.8", "8.8.4.4"),
        Triple("Quad9", "9.9.9.9", "149.112.112.112"),
        Triple("OpenDNS", "208.67.222.222", "208.67.220.220"),
        Triple("AdGuard", "94.140.14.14", "94.140.15.15"),
        Triple("Comodo", "8.26.56.26", "8.20.247.20"),
        Triple("CleanBrowsing", "185.228.168.9", "185.228.169.9"),
        Triple("Yandex", "77.88.8.8", "77.88.8.1"),
        Triple("Level3", "4.2.2.1", "4.2.2.2"),
        Triple("Neustar", "156.154.70.1", "156.154.71.1"),
        Triple("Verisign", "64.6.64.6", "64.6.65.6")
    )

    val privateDns = listOf(
        "1dot1dot1dot1.cloudflare-dns.com",
        "dns.google",
        "dns.quad9.net",
        "dns.adguard-dns.com"
    )
}
