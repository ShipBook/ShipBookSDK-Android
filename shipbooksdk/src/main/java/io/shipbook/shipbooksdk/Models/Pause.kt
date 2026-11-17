package io.shipbook.shipbooksdk.Models

import io.shipbook.shipbooksdk.InnerLog
import org.json.JSONObject

internal data class Pause(val appId: String, val sdkVersion: String, val until: Long): BaseObj {
    companion object {
        private val TAG = Pause::class.java.simpleName
        private const val MAX_SECONDS = 30L * 24 * 60 * 60
        private const val BACK_OFF_DEFAULT_SECONDS = 300L
        private const val BACK_OFF_MAX_SECONDS = 3600L

        fun create(json: JSONObject): Pause {
            val appId = json.getString("appId")
            val sdkVersion = json.getString("sdkVersion")
            val until = json.getLong("until")
            return Pause(appId, sdkVersion, until)
        }

        // null when the response doesn't refuse the SDK; only the Shipbook server sends x-shipbook-pause.
        fun pauseSeconds(statusCode: Int, pauseHeader: String?): Long? {
            if (statusCode < 400) return null
            val seconds = seconds(pauseHeader) ?: return null
            return minOf(seconds, MAX_SECONDS)
        }

        // For a response without a pause: null unless it says busy, which anything in front of the server can answer.
        fun backOffSeconds(statusCode: Int, retryAfterHeader: String?): Long? {
            if (statusCode != 429 && statusCode != 503) return null
            return minOf(seconds(retryAfterHeader) ?: BACK_OFF_DEFAULT_SECONDS, BACK_OFF_MAX_SECONDS)
        }

        private fun seconds(header: String?): Long? {
            val seconds = header?.trim()?.toLongOrNull() ?: return null
            return if (seconds > 0) seconds else null
        }

        // null for a record that can't be read or no longer applies; the caller deletes it, so it can never block a start.
        fun load(stored: String, appId: String, sdkVersion: String, now: Long): Pause? {
            try {
                val pause = create(JSONObject(stored))
                // More than the longest pause ahead means the device clock was wrong when it was saved.
                val applies = pause.appId == appId && pause.sdkVersion == sdkVersion && pause.until > now && pause.until <= now + MAX_SECONDS * 1000
                if (applies) return pause
            }
            catch (t: Throwable) {
                InnerLog.w(TAG, "saved pause is corrupt, deleting it", t)
            }
            return null
        }
    }

    override fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("appId", appId)
        json.put("sdkVersion", sdkVersion)
        json.put("until", until)
        return json
    }
}
