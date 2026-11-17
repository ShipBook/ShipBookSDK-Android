package io.shipbook.shipbooksdk

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.shipbook.shipbooksdk.Models.Login
import io.shipbook.shipbooksdk.Models.Pause
import io.shipbook.shipbooksdk.Networking.ConnectionClient
import io.shipbook.shipbooksdk.Networking.SessionManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketException
import java.util.Date
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

@RunWith(AndroidJUnit4::class)
class ConnectionClientTest {
    private val productionUrl = ConnectionClient.baseUrl
    private val requests = CopyOnWriteArrayList<Map<String, String>>()
    private val server = ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))

    @Volatile
    private var status = 200
    @Volatile
    private var headers = mapOf<String, String>()
    @Volatile
    private var body = "{}"

    @Before
    fun setUp() {
        thread(isDaemon = true) {
            while (true) {
                val socket = try { server.accept() } catch (e: SocketException) { return@thread }
                socket.use {
                    val reader = it.getInputStream().bufferedReader()
                    val lines = generateSequence { reader.readLine() }.drop(1).takeWhile { line -> line.isNotEmpty() }
                    requests.add(lines.associate { line -> line.substringBefore(":").lowercase() to line.substringAfter(":").trim() })
                    val headerLines = headers.map { (name, value) -> "$name: $value\r\n" }.joinToString("")
                    val head = "HTTP/1.1 $status Status\r\n${headerLines}Content-Length: ${body.toByteArray().size}\r\nConnection: close\r\n\r\n"
                    it.getOutputStream().write((head + body).toByteArray())
                }
            }
        }
        ConnectionClient.baseUrl = "http://127.0.0.1:${server.localPort}/"
        SessionManager.application = ApplicationProvider.getApplicationContext<Context>() as Application
        StorageManager.remove("pause")
    }

    @After
    fun tearDown() {
        runBlocking(SessionManager.threadContext) {}
        server.close()
        ConnectionClient.baseUrl = productionUrl
        SessionManager.login = null
        SessionManager.token = null
        SessionManager.sessionLink = null
        SessionManager.sessionUrl = null
        SessionManager.pausedUntil = 0
        SessionManager.backOffUntil = 0
        StorageManager.remove("pause")
    }

    private fun start(appId: String) {
        SessionManager.login = Login(appId, "appKey", "android", "app", "udid", Date(), Date(), "14", "1.0", 1,
            BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE, "", "", "", "en", false, false, null)
        SessionManager.token = "token"
        SessionManager.sessionLink = "link"
        SessionManager.sessionUrl = "url"
    }

    private fun request() = runBlocking { ConnectionClient.request("ping", null) }

    private fun assertEndsIn(seconds: Long, until: Long, before: Long) {
        assertTrue("$until is not $seconds seconds ahead", until in (before + seconds * 1000)..(System.currentTimeMillis() + seconds * 1000))
    }

    @Test
    fun everyRequestIdentifiesTheSdkAndTheApp() {
        start("app1")

        assertTrue(request().ok)

        assertEquals("android/${BuildConfig.VERSION_NAME}", requests.single()["x-shipbook-sdk"])
        assertEquals("app1", requests.single()["x-shipbook-app-id"])
    }

    @Test
    fun beforeStartOnlyTheSdkIsIdentified() {
        assertTrue(request().ok)

        assertEquals("android/${BuildConfig.VERSION_NAME}", requests.single()["x-shipbook-sdk"])
        assertFalse(requests.single().containsKey("x-shipbook-app-id"))
    }

    @Test
    fun pauseHeaderOnA401PausesSavesItAndLaterRequestsNeverReachTheServer() {
        start("app1")
        status = 401
        headers = mapOf("x-shipbook-pause" to "120", "Retry-After" to "120")
        body = "{\"name\":\"AppNotFound\",\"message\":\"unknown app\"}"
        val before = System.currentTimeMillis()

        val response = request()

        assertEquals(401, response.statusCode)
        assertTrue(SessionManager.paused)
        assertFalse(SessionManager.backingOff)
        assertEndsIn(120, SessionManager.pausedUntil, before)
        assertNull(SessionManager.token)
        assertNull(SessionManager.sessionLink)
        assertNull(SessionManager.sessionUrl)
        assertEquals(Pause("app1", BuildConfig.VERSION_NAME, SessionManager.pausedUntil), Pause.load(StorageManager.getString("pause")!!, "app1", BuildConfig.VERSION_NAME, before))

        val local = request()

        assertFalse(local.ok)
        assertEquals(429, local.statusCode)
        assertEquals(1, requests.size)
    }

    @Test
    fun pauseHeaderOnA429PausesInsteadOfBackingOff() {
        start("app1")
        status = 429
        headers = mapOf("x-shipbook-pause" to "60", "Retry-After" to "60")
        val before = System.currentTimeMillis()

        request()

        assertEndsIn(60, SessionManager.pausedUntil, before)
        assertEquals(0L, SessionManager.backOffUntil)
        assertNull(SessionManager.token)
        assertNotNull(StorageManager.getString("pause"))
    }

    @Test
    fun retryAfterAloneOnA401ChangesNothing() {
        start("app1")
        status = 401
        headers = mapOf("Retry-After" to "120")
        body = "{\"name\":\"InvalidToken\",\"message\":\"bad token\"}"

        assertEquals(401, request().statusCode)
        request()

        assertFalse(SessionManager.paused)
        assertFalse(SessionManager.backingOff)
        assertEquals("token", SessionManager.token)
        assertNull(StorageManager.getString("pause"))
        assertEquals(2, requests.size)
    }

    @Test
    fun busyAnswerBacksOffForRetryAfterKeepsTheSessionAndSavesNothing() {
        listOf(429, 503).forEach {
            start("app1")
            SessionManager.backOffUntil = 0
            requests.clear()
            status = it
            headers = mapOf("Retry-After" to "120")
            val before = System.currentTimeMillis()

            assertEquals(it, request().statusCode)

            assertTrue("status $it", SessionManager.backingOff)
            assertEndsIn(120, SessionManager.backOffUntil, before)
            assertFalse("status $it", SessionManager.paused)
            assertEquals("token", SessionManager.token)
            assertEquals("link", SessionManager.sessionLink)
            assertNull("status $it", StorageManager.getString("pause"))

            val local = request()

            assertFalse(local.ok)
            assertEquals(429, local.statusCode)
            assertEquals(1, requests.size)
        }
    }

    @Test
    fun backOffDefaultsToFiveMinutes() {
        listOf(mapOf(), mapOf("Retry-After" to "soon")).forEach {
            start("app1")
            SessionManager.backOffUntil = 0
            status = 503
            headers = it
            val before = System.currentTimeMillis()

            request()

            assertEndsIn(300, SessionManager.backOffUntil, before)
        }
    }

    @Test
    fun backOffIsCappedAtAnHour() {
        start("app1")
        status = 429
        headers = mapOf("Retry-After" to "86400")
        val before = System.currentTimeMillis()

        request()

        assertEndsIn(3600, SessionManager.backOffUntil, before)
    }

    @Test
    fun signalsAreReadWhenTheBodyIsNotJsonOrMissing() {
        listOf("<html>Service unavailable</html>", "").forEach {
            start("app1")
            SessionManager.pausedUntil = 0
            SessionManager.backOffUntil = 0
            body = it
            status = 401
            headers = mapOf("x-shipbook-pause" to "60")

            request()

            assertTrue("pause with body '$it'", SessionManager.paused)

            start("app1")
            SessionManager.pausedUntil = 0
            status = 503
            headers = mapOf()

            request()

            assertTrue("back-off with body '$it'", SessionManager.backingOff)
        }
    }

    @Test
    fun signalsOnSuccessAreIgnored() {
        start("app1")
        headers = mapOf("x-shipbook-pause" to "120", "Retry-After" to "120")

        assertTrue(request().ok)

        assertFalse(SessionManager.requestsBlocked)
    }

    @Test
    fun pauseForAnAppThatAnotherStartReplacedIsIgnored() {
        start("app2")

        SessionManager.pause("app1", 401, 60)

        assertFalse(SessionManager.paused)
        assertEquals("token", SessionManager.token)
        assertNull(StorageManager.getString("pause"))
    }

    @Test
    fun whilePausedOrBackingOffConnectedIsFalseAndStartsNoLogin() {
        start("app1")
        SessionManager.token = null
        SessionManager.pausedUntil = System.currentTimeMillis() + 60_000

        assertFalse(SessionManager.connected)
        assertFalse(SessionManager.isInLoginRequest)

        SessionManager.pausedUntil = 0
        SessionManager.backOffUntil = System.currentTimeMillis() + 60_000

        assertFalse(SessionManager.connected)
        assertFalse(SessionManager.isInLoginRequest)
    }

    @Test
    fun whileBackingOffAValidTokenDoesNotMeanConnected() {
        start("app1")
        SessionManager.backOffUntil = System.currentTimeMillis() + 60_000

        assertFalse(SessionManager.connected)

        SessionManager.backOffUntil = System.currentTimeMillis() - 1

        assertTrue(SessionManager.connected)
    }

    @Test
    fun afterPauseAndBackOffEndConnectedStartsALogin() {
        start("app1")
        SessionManager.token = null
        SessionManager.pausedUntil = System.currentTimeMillis() - 1
        SessionManager.backOffUntil = System.currentTimeMillis() - 1
        val shipbookThread = CountDownLatch(1)
        SessionManager.threadContext.executor.execute { shipbookThread.await() }

        assertFalse(SessionManager.connected)

        assertTrue(SessionManager.isInLoginRequest)
        shipbookThread.countDown()
    }
}
