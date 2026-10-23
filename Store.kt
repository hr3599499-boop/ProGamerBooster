package com.example.pgb

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Game(
    val name: String,
    val pkg: String?,
    val genre: String,
    val boosted: Boolean,
    val ping: Long?,
    val ip: String? = null,
    val port: Int? = null
)

data class Server(val id: String, val name: String, val ip: String, val port: Int)

class Store(ctx: Context) {
    private val p = ctx.getSharedPreferences("pgb", Context.MODE_PRIVATE)

    fun games(): MutableList<Game> {
        val a = JSONArray(p.getString("games", "[]") ?: "[]")
        return (0 until a.length()).map { i ->
            val o = a.getJSONObject(i)
            Game(
                o.getString("name"),
                if (o.isNull("pkg")) null else o.getString("pkg"),
                o.optString("genre"),
                o.optBoolean("boosted"),
                if (o.isNull("ping")) null else o.getLong("ping"),
                if (o.isNull("ip")) null else o.getString("ip"),
                if (o.isNull("port")) null else o.getInt("port")
            )
        }.toMutableList()
    }

    fun saveGames(list: List<Game>) {
        val a = JSONArray()
        list.forEach {
            a.put(
                JSONObject()
                    .put("name", it.name)
                    .put("pkg", it.pkg ?: JSONObject.NULL)
                    .put("genre", it.genre)
                    .put("boosted", it.boosted)
                    .put("ping", it.ping ?: JSONObject.NULL)
                    .put("ip", it.ip ?: JSONObject.NULL)
                    .put("port", it.port ?: JSONObject.NULL)
            )
        }
        p.edit().putString("games", a.toString()).apply()
    }

    fun updateGame(name: String, f: (Game) -> Game) {
        saveGames(games().map { if (it.name == name) f(it) else it })
    }

    fun servers(): MutableList<Server> {
        val a = JSONArray(p.getString("servers", "[]") ?: "[]")
        return (0 until a.length()).map { i ->
            val o = a.getJSONObject(i)
            Server(o.getString("id"), o.getString("name"), o.getString("ip"), o.getInt("port"))
        }.toMutableList()
    }

    fun saveServers(list: List<Server>) {
        val a = JSONArray()
        list.forEach {
            a.put(JSONObject().put("id", it.id).put("name", it.name).put("ip", it.ip).put("port", it.port))
        }
        p.edit().putString("servers", a.toString()).apply()
    }

    fun selectedServerId(): String? = p.getString("sel", null)

    fun selectServer(id: String?) {
        p.edit().putString("sel", id).apply()
    }

    fun selectedServer(): Server? {
        val id = selectedServerId()
        return servers().firstOrNull { it.id == id }
    }

    fun dns(): Pair<String, String>? {
        val a = p.getString("dns1", null)
        val b = p.getString("dns2", null)
        return if (a != null && b != null) a to b else null
    }

    fun saveDns(a: String, b: String) {
        p.edit().putString("dns1", a).putString("dns2", b).apply()
    }

    fun apiKey(): String? = p.getString("apikey", null)

    fun saveApiKey(k: String) {
        p.edit().putString("apikey", k).apply()
    }

    fun clearDns() {
        p.edit().remove("dns1").remove("dns2").apply()
    }
}
