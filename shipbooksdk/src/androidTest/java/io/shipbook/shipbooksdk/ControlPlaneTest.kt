package io.shipbook.shipbooksdk

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.shipbook.shipbooksdk.Models.Exception
import io.shipbook.shipbooksdk.Models.Message
import io.shipbook.shipbooksdk.Models.Pause
import io.shipbook.shipbooksdk.Models.Severity
import io.shipbook.shipbooksdk.Networking.ConnectionClient
import io.shipbook.shipbooksdk.Networking.HttpMethod
import io.shipbook.shipbooksdk.Networking.SessionManager
import io.shipbook.shipbooksdk.Util.toInternal
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread

// The SDK talks to a server inside the test, so nothing here reaches the network.
@RunWith(AndroidJUnit4::class)
class ControlPlaneTest {
    private class Answer(val status: Int, val headers: Map<String, String> = mapOf(), val body: String = "{}")
    private class Request(val path: String, val headers: Map<String, String>, val body: String)

    private val login = "auth/loginSdk"
    private val upload = "sessions/uploadSavedData"
    private val sessionUrl = "https://console.shipbook.io/session"
    private val loggedIn = Answer(200, body = """
        {
            "token": "token1", "sessionUrl": "$sessionUrl", "sessionLink": "1:app:session",
            "config": {
                "appenders": [ { "type": "SBCloudAppender", "name": "cloud", "config": { "maxTime": 0.2, "flushSeverity": "Verbose" } } ],
                "loggers": [ { "name": "", "severity": "Verbose", "appenderRef": "cloud" } ]
            }
        }
    """)
    private val refusal = mapOf("x-shipbook-pause" to "120", "Retry-After" to "120")

    private val productionUrl = ConnectionClient.baseUrl
    private val server = ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))
    private val requests = CopyOnWriteArrayList<Request>()
    private val answers = ConcurrentHashMap<String, Answer>()
    private lateinit var application: Application
    private lateinit var cloudFile: File

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext<Context>() as Application
        SessionManager.application = application
        SessionManager.login = null
        cloudFile = File(application.filesDir, "CloudQueue.log")
        deleteFiles()
        LogManager.clear()
        ConnectionClient.baseUrl = "http://127.0.0.1:${server.localPort}/"
        thread(isDaemon = true) {
            while (true) {
                val socket = try { server.accept() } catch (e: SocketException) { return@thread }
                socket.use {
                    val reader = it.getInputStream().bufferedReader(Charsets.ISO_8859_1)
                    val path = (reader.readLine() ?: return@use).split(" ")[1].removePrefix("/")
                    val lines = generateSequence { reader.readLine() }.takeWhile { line -> line.isNotEmpty() }
                    val headers = lines.associate { line -> line.substringBefore(":").lowercase() to line.substringAfter(":").trim() }
                    val body = CharArray(headers["content-length"]?.toInt() ?: 0)
                    var read = 0
                    while (read < body.size) read += reader.read(body, read, body.size - read)
                    requests.add(Request(path, headers, String(body)))
                    val answer = answers.getValue(path)
                    val headerLines = answer.headers.map { (name, value) -> "$name: $value\r\n" }.joinToString("")
                    val head = "HTTP/1.1 ${answer.status} Status\r\n${headerLines}Content-Length: ${answer.body.toByteArray().size}\r\nConnection: close\r\n\r\n"
                    it.getOutputStream().write((head + answer.body).toByteArray())
                }
            }
        }
    }

    @After
    fun tearDown() {
        settle()
        LogManager.clear()
        server.close()
        ConnectionClient.baseUrl = productionUrl
        SessionManager.login = null
        SessionManager.token = null
        SessionManager.sessionLink = null
        SessionManager.sessionUrl = null
        SessionManager.sessionCompletion = null
        SessionManager.pausedUntil = 0
        SessionManager.backOffUntil = 0
        deleteFiles()
    }

    private fun deleteFiles() {
        StorageManager.remove("pause")
        StorageManager.remove("config")
        listOf("CloudQueue.log", "TempCloudQueue.log").forEach { File(application.filesDir, it).delete() }
    }

    private fun start(appKey: String = "appKey", completion: ((String) -> Unit)? = null) {
        SessionManager.login(application, "appId", appKey, completion)
    }

    private fun settle() = runBlocking(SessionManager.threadContext) {}

    private fun count(path: String) = requests.count { it.path == path }

    private fun awaitRequests(path: String, count: Int) {
        val deadline = System.currentTimeMillis() + 10_000
        while (count(path) < count && System.currentTimeMillis() < deadline) Thread.sleep(20)
        settle()
        assertEquals("requests to $path", count, count(path))
    }

    private fun log(message: String) {
        LogManager.push(Message(Severity.Error, message, "tag"))
        settle()
    }

    private fun crash(reason: String) {
        LogManager.push(Exception(IllegalStateException::class.java.name, reason, IllegalStateException(reason).stackTrace.toInternal()))
        settle()
    }

    @Test
    fun everyRequestIdentifiesTheSdkAndTheApp() {
        answers[login] = loggedIn
        answers[upload] = Answer(200)

        start()
        awaitRequests(login, 1)
        log("first")
        awaitRequests(upload, 1)

        requests.forEach {
            assertEquals(it.path, "android/${BuildConfig.VERSION_NAME}", it.headers["x-shipbook-sdk"])
            assertEquals(it.path, "appId", it.headers["x-shipbook-app-id"])
        }
        assertTrue(requests.last().body.contains("first"))
    }

    @Test
    fun refusedLoginPausesDropsLogsAndLogsInAgainWhenThePauseEnds() {
        answers[login] = Answer(401, refusal, """{"name":"AppNotFound","message":"unknown app"}""")

        start()
        awaitRequests(login, 1)

        assertTrue(SessionManager.paused)
        assertFalse(SessionManager.backingOff)
        val saved = Pause.load(StorageManager.getString("pause")!!, "appId", BuildConfig.VERSION_NAME, System.currentTimeMillis())
        assertEquals(SessionManager.pausedUntil, saved!!.until)

        log("dropped")
        crash("dropped")
        assertFalse(SessionManager.connected)
        settle()
        assertEquals(429, runBlocking { ConnectionClient.request(login, "{}", HttpMethod.POST) }.statusCode)
        assertFalse(cloudFile.exists())
        assertEquals(1, requests.size)

        SessionManager.pausedUntil = 0
        assertFalse(SessionManager.connected)
        awaitRequests(login, 2)
    }

    @Test
    fun busyLoginBacksOffKeepsLogsSavesNothingAndLogsInAgainAfterwards() {
        answers[login] = Answer(429, mapOf("Retry-After" to "120"), """{"name":"TooManyRequests","message":"busy"}""")

        start()
        awaitRequests(login, 1)

        assertTrue(SessionManager.backingOff)
        assertFalse(SessionManager.paused)
        assertNull(StorageManager.getString("pause"))

        log("kept log")
        crash("kept crash")
        assertFalse(SessionManager.connected)
        settle()
        assertEquals(429, runBlocking { ConnectionClient.request(login, "{}", HttpMethod.POST) }.statusCode)
        assertTrue(cloudFile.readText().contains("kept log"))
        assertTrue(cloudFile.readText().contains("kept crash"))
        assertEquals(1, requests.size)

        SessionManager.backOffUntil = 0
        assertFalse(SessionManager.connected)
        awaitRequests(login, 2)
    }

    @Test
    fun rejectedLoginWithOnlyRetryAfterIsNotRetriedEvenByARepeatedStart() {
        answers[login] = Answer(401, mapOf("Retry-After" to "120"), """{"name":"InvalidAppKey","message":"wrong key"}""")

        start()
        awaitRequests(login, 1)

        assertFalse(SessionManager.requestsBlocked)
        assertNull(StorageManager.getString("pause"))

        assertFalse(SessionManager.connected)
        start()
        settle()
        assertEquals(1, requests.size)
    }

    @Test
    fun savedPauseStartsPausedAndNothingIsSent() {
        answers[login] = loggedIn
        StorageManager.setString("pause", Pause("appId", BuildConfig.VERSION_NAME, System.currentTimeMillis() + 60_000).toJson().toString())

        start()
        settle()

        assertTrue(SessionManager.paused)
        assertFalse(SessionManager.connected)
        settle()
        assertNotNull("the pause must outlive the start that found it", StorageManager.getString("pause"))
        assertEquals(0, requests.size)
    }

    @Test
    fun savedPauseFromAWrongClockIsDeletedAndLoginHappens() {
        answers[login] = loggedIn
        StorageManager.setString("pause", Pause("appId", BuildConfig.VERSION_NAME, System.currentTimeMillis() + 31L * 24 * 60 * 60 * 1000).toJson().toString())

        start()
        awaitRequests(login, 1)

        assertFalse(SessionManager.paused)
        assertNull(StorageManager.getString("pause"))
        assertEquals("token1", SessionManager.token)
    }

    @Test
    fun repeatedStartKeepsTheSessionAndOnlyAGivenCompletionReplacesTheEarlierOne() {
        answers[login] = loggedIn
        val first = CopyOnWriteArrayList<String>()
        val second = CopyOnWriteArrayList<String>()

        start { first.add(it) }
        awaitRequests(login, 1)
        assertEquals(listOf(sessionUrl), first)

        start()
        start { second.add(it) }
        assertEquals("called at once with the known session url", listOf(sessionUrl), second)
        start()
        settle()
        assertEquals(1, requests.size)
        assertEquals("token1", SessionManager.token)

        SessionManager.logout()
        awaitRequests(login, 2)
        assertEquals(listOf(sessionUrl), first)
        assertEquals(listOf(sessionUrl, sessionUrl), second)
    }

    @Test
    fun startWithAnotherAppKeyStartsOver() {
        answers[login] = loggedIn

        start()
        awaitRequests(login, 1)
        start("otherKey")
        awaitRequests(login, 2)

        assertTrue(requests.last().body.contains("otherKey"))
    }

    @Test
    fun busyUploadLosesNothing() {
        answers[login] = loggedIn
        answers[upload] = Answer(429)

        start()
        awaitRequests(login, 1)
        log("before the busy answer")
        awaitRequests(upload, 1)

        assertTrue(SessionManager.backingOff)
        assertFalse(SessionManager.paused)
        assertEquals("token1", SessionManager.token)
        assertNull(StorageManager.getString("pause"))

        log("while backing off")
        Thread.sleep(600)
        settle()
        assertEquals(1, count(upload))

        answers[upload] = Answer(200)
        SessionManager.backOffUntil = 0
        log("after the back-off")
        awaitRequests(upload, 2)

        val sent = requests.last().body
        assertTrue(sent.contains("before the busy answer"))
        assertTrue(sent.contains("while backing off"))
        assertTrue(sent.contains("after the back-off"))
        assertEquals(1, count(login))
    }

    @Test
    fun refusedUploadKeepsWhatWasSavedAndDropsNewLogs() {
        answers[login] = loggedIn
        answers[upload] = Answer(429, refusal)

        start()
        awaitRequests(login, 1)
        log("saved before the pause")
        awaitRequests(upload, 1)

        assertTrue(SessionManager.paused)
        assertNull(SessionManager.token)
        assertNotNull(StorageManager.getString("pause"))

        log("dropped")
        Thread.sleep(600)
        settle()

        assertTrue(cloudFile.readText().contains("saved before the pause"))
        assertFalse(cloudFile.readText().contains("dropped"))
        assertEquals(2, requests.size)

        answers[upload] = Answer(200)
        SessionManager.pausedUntil = 0
        log("after the pause")
        awaitRequests(login, 2)
        awaitRequests(upload, 2)

        val sent = requests.last().body
        assertTrue(sent.contains("saved before the pause"))
        assertTrue(sent.contains("after the pause"))
        assertFalse(sent.contains("dropped"))
    }

    @Test
    fun startWithOtherCredentialsHeldBackByABackOffLogsInWhenItEnds() {
        answers[login] = loggedIn

        start()
        awaitRequests(login, 1)
        SessionManager.backOff(503, 120)
        start("otherKey")
        settle()

        assertNull("the previous app's token must not be used for the new one", SessionManager.token)
        assertEquals(1, requests.size)

        SessionManager.backOffUntil = 0
        assertFalse(SessionManager.connected)
        awaitRequests(login, 2)
        assertTrue(requests.last().body.contains("otherKey"))
    }
}
