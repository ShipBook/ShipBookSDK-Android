package io.shipbook.shipbooksdk

import android.content.Context
import android.content.SharedPreferences

// The SDK's own preferences file, never the app's default one, so its keys can't collide with the app's.
internal object Storage {
    private const val NAME = "io.shipbook.shipbooksdk"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    fun getString(key: String): String? = prefs.getString(key, null)

    fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}
