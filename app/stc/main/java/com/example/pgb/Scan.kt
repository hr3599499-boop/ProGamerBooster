package com.example.pgb

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.text.InputType
import android.util.Base64
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.view.ViewGroup
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Reads game names from phone screenshots using Claude (needs the user's own API key). */
object Scan {
    const val REQ_IMG = 77
    private const val MODEL = "claude-haiku-5-5"
    private const val PROMPT =
        "این عکس صفحه‌ی برنامه‌های گوشی است. فقط اسم بازی‌های ویدیویی را که در عکس می‌بینی پیدا کن " +
        "و فقط یک آرایه‌ی JSON از رشته‌ها برگردان، مثلاً [\"PUBG Mobile\", \"Free Fire\"]. " +
        "برنامه‌های غیربازی را نیاور و هیچ توضیحی ننویس."

    fun pickImages(act: MainActivity) {
        if (act.store.apiKey() == null) {
            askKey(act) { pickImages(act) }
            return
        }
        val i = Intent(Intent.ACTION_GET_CONTENT)
            .setType("image/*")
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        act.startActivityForResult(i, REQ_IMG)
    }

    fun askKey(act: MainActivity, then: () -> Unit) {
        val input = EditText(act).apply {
            hint = "کلید API (sk-ant-...)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setText(act.store.apiKey() ?: "")
        }
        AlertDialog.Builder(act)
            .setTitle("کلید Claude")
            .setMessage("برای خواندن بازی از عکس، کلید API خودت را وارد کن. کلید فقط داخل همین گوشی ذخیره می‌شود. هر خواندن هزینه‌ی کوچکی دارد.")
            .setView(input)
            .setPositiveButton("ذخیره") { _, _ ->
                val k = input.text.toString().trim()
                if (k.isNotEmpty()) {
                    act.store.saveApiKey(k)
                    then()
                }
            }
            .setNegativeButton("لغو", null)
            .show()
    }

    fun onResult(act: MainActivity, data: Intent?) {
        val uris = mutableListOf<Uri>()
        data?.clipData?.let { clip ->
            for (i in 0 until clip.itemCount) uris.add(clip.getItemAt(i).uri)
        }
        if (uris.isEmpty()) data?.data?.let { uris.add(it) }
        if (uris.isEmpty()) return

        act.toast("در حال خواندن بازی‌ها...")
        Async.bg {
            val found = linkedSetOf<String>()
            var err: String? = null
            for (u in uris) {
                try {
                    found.addAll(readGames(act, u))
                } catch (e: Exception) {
                    err = e.message
                }
            }
            Async.ui {
                if (found.isEmpty()) {
                    act.toast("بازی‌ای پیدا نشد" + (err?.let { ": $it" } ?: ""))
                } else {
                    chooseDialog(act, found.toList())
                }
            }
        }
    }

    private fun readGames(act: MainActivity, uri: Uri): List<String> {
        val bytes = act.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw Exception("عکس خوانده نشد")
        if (bytes.size > 4_500_000) throw Exception("عکس خیلی بزرگ است")
        val mime = act.contentResolver.getType(uri) ?: "image/jpeg"
        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

        val body = JSONObject()
            .put("model", MODEL)
            .put("max_tokens", 1000)
            .put(
                "messages",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put(
                            "content",
                            JSONArray()
                                .put(
                                    JSONObject()
                                        .put("type", "image")
                                        .put(
                                            "source",
                                            JSONObject()
                                                .put("type", "base64")
                                                .put("media_type", mime)
                                                .put("data", b64)
                                        )
                                )
                                .put(JSONObject().put("type", "text").put("text", PROMPT))
                        )
                )
            )

        val conn = URL("https://api.anthropic.com/v1/messages").openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 15000
        conn.readTimeout = 60000
        conn.doOutput = true
        conn.setRequestProperty("content-type", "application/json")
        conn.setRequestProperty("x-api-key", act.store.apiKey() ?: "")
        conn.setRequestProperty("anthropic-version", "2023-06-01")
        conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
        if (code !in 200..299) throw Exception("خطای API کد $code")

        val content = JSONObject(text).getJSONArray("content")
        val reply = (0 until content.length())
            .map { content.getJSONObject(it) }
            .filter { it.optString("type") == "text" }
            .joinToString("") { it.getString("text") }

        val m = Regex("\\[.*\\]", RegexOption.DOT_MATCHES_ALL).find(reply) ?: return emptyList()
        val arr = JSONArray(m.value)
        return (0 until arr.length()).map { arr.getString(it).trim() }.filter { it.isNotEmpty() }
    }

    private fun chooseDialog(act: MainActivity, names: List<String>) {
        val have = act.store.games().map { it.name }.toSet()
        val items = names.filter { it !in have }
        if (items.isEmpty()) {
            act.toast("همه‌ی این بازی‌ها قبلاً در لیست هستند")
            return
        }
        val list = ListView(act).apply {
            choiceMode = AbsListView.CHOICE_MODE_MULTIPLE
            adapter = ArrayAdapter(act, android.R.layout.simple_list_item_multiple_choice, items)
        }
        items.indices.forEach { list.setItemChecked(it, true) }
        val box = act.vbox()
        box.addView(list, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, act.dp(360)))

        AlertDialog.Builder(act)
            .setTitle("بازی‌های پیدا شده")
            .setView(box)
            .setPositiveButton("افزودن") { _, _ ->
                val games = act.store.games()
                val checked = list.checkedItemPositions
                items.forEachIndexed { i, name ->
                    if (checked.get(i)) {
                        val cat = Catalog.games.firstOrNull {
                            it.name.equals(name, true) || name.contains(it.name, true) || it.name.contains(name, true)
                        }
                        games.add(Game(name, cat?.pkg, cat?.genre ?: "بازی دلخواه", false, null))
                    }
                }
                act.store.saveGames(games)
                act.show("games")
            }
            .setNegativeButton("لغو", null)
            .show()
    }
}
