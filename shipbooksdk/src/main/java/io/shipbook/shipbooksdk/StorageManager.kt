package io.shipbook.shipbooksdk

import android.content.Context
import android.content.SharedPreferences
import io.shipbook.shipbooksdk.Networking.SessionManager

// The SDK's own preferences file, never the app's default one, so its keys can't collide with the app's.
internal object StorageManager {
    private const val NAME = "io.shipbook.shipbooksdk"
    // Android caches the instance per name, so asking for it on each call is cheap.
    private val prefs: SharedPreferences
        get() = SessionManager.appContext!!.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun getString(key: String): String? = prefs.getString(key, null)

    fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}
