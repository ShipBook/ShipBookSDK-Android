package io.shipbook.shipbooksdk

import io.shipbook.shipbooksdk.Models.Pause
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PauseTest {
    private val now = 1_800_000_000_000L
    private val hour = 60 * 60 * 1000L
    private val thirtyDays = 30 * 24 * hour
    private val unusableSeconds = listOf(null, "", " ", "abc", "1.5", "12s", "0", "-30", "Mon, 09 Nov 2026 07:28:00 GMT", "99999999999999999999")

    @Test
    fun pauseHeaderPausesOnAnyErrorStatus() {
        listOf(400, 401, 403, 429, 500, 503).forEach {
            assertEquals("status $it", 120L, Pause.pauseSeconds(it, "120"))
        }
        assertEquals(120L, Pause.pauseSeconds(401, " 120 "))
    }

    @Test
    fun pauseIsCappedAtThirtyDays() {
        assertEquals(2592000L, Pause.pauseSeconds(401, "2592000"))
        assertEquals(2592000L, Pause.pauseSeconds(401, "2592001"))
        assertEquals(2592000L, Pause.pauseSeconds(429, "99999999"))
    }

    @Test
    fun unusablePauseHeaderCountsAsAbsent() {
        unusableSeconds.forEach {
            assertNull("x-shipbook-pause '$it' on 401", Pause.pauseSeconds(401, it))
            assertNull("x-shipbook-pause '$it' on 429", Pause.pauseSeconds(429, it))
        }
    }

    @Test
    fun responsesBelow400NeverPause() {
        listOf(200, 204, 304, 399).forEach {
            assertNull("status $it", Pause.pauseSeconds(it, "120"))
        }
    }

    @Test
    fun busyStatusesBackOffForRetryAfter() {
        assertEquals(120L, Pause.backOffSeconds(429, "120"))
        assertEquals(120L, Pause.backOffSeconds(503, " 120 "))
    }

    @Test
    fun backOffDefaultsToFiveMinutes() {
        unusableSeconds.forEach {
            assertEquals("Retry-After '$it' on 429", 300L, Pause.backOffSeconds(429, it))
            assertEquals("Retry-After '$it' on 503", 300L, Pause.backOffSeconds(503, it))
        }
    }

    @Test
    fun backOffIsCappedAtAnHour() {
        assertEquals(3600L, Pause.backOffSeconds(429, "3600"))
        assertEquals(3600L, Pause.backOffSeconds(429, "3601"))
        assertEquals(3600L, Pause.backOffSeconds(503, "2592000"))
    }

    @Test
    fun otherStatusesNeverBackOff() {
        listOf(200, 400, 401, 403, 404, 500, 502, 504).forEach {
            assertNull("status $it", Pause.backOffSeconds(it, "120"))
            assertNull("status $it", Pause.backOffSeconds(it, null))
        }
    }

    @Test
    fun savedRecordIsLoadedWhileItStillApplies() {
        val pause = Pause("app1", "1.10.0", now + hour)

        assertEquals(pause, Pause.load(pause.toJson().toString(), "app1", "1.10.0", now))
    }

    @Test
    fun savedRecordHasTheAgreedShape() {
        val json = JSONObject(Pause("app1", "1.10.0", now + hour).toJson().toString())

        assertEquals(3, json.length())
        assertEquals("app1", json.getString("appId"))
        assertEquals("1.10.0", json.getString("sdkVersion"))
        assertEquals(now + hour, json.getLong("until"))
    }

    @Test
    fun recordOfAnotherAppDoesNotApply() {
        assertNull(Pause.load(stored(now + hour), "app2", "1.10.0", now))
    }

    @Test
    fun recordOfAnotherSdkVersionDoesNotApply() {
        assertNull(Pause.load(stored(now + hour), "app1", "1.10.1", now))
    }

    @Test
    fun recordThatEndedDoesNotApply() {
        listOf(now, now - 1, 0L).forEach {
            assertNull("until $it", Pause.load(stored(it), "app1", "1.10.0", now))
        }
    }

    @Test
    fun recordMoreThanThirtyDaysAheadDoesNotApply() {
        assertEquals(now + thirtyDays, Pause.load(stored(now + thirtyDays), "app1", "1.10.0", now)!!.until)
        listOf(now + thirtyDays + 1, now + 365 * 24 * hour).forEach {
            assertNull("until $it", Pause.load(stored(it), "app1", "1.10.0", now))
        }
    }

    @Test
    fun corruptRecordIsTreatedAsAbsent() {
        val corrupt = listOf(
            String(ByteArray(300) { 0xFF.toByte() }),
            "",
            "{\"appId\":\"app1\",\"sdkVer",
            "{\"appId\":\"app1\",\"sdkVersion\":\"1.10.0\"}",
            "{\"appId\":\"app1\",\"sdkVersion\":\"1.10.0\",\"until\":\"later\"}",
            "[]"
        )
        corrupt.forEach {
            assertNull(it, Pause.load(it, "app1", "1.10.0", now))
        }
    }

    private fun stored(until: Long) = Pause("app1", "1.10.0", until).toJson().toString()
}
