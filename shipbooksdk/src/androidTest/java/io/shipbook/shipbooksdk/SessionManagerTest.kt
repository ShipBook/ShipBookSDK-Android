package io.shipbook.shipbooksdk

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.shipbook.shipbooksdk.Models.ConfigResponse
import io.shipbook.shipbooksdk.Networking.SessionManager
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SessionManagerTest {
    private lateinit var application: Application
    private lateinit var configFile: File

    private val customConfig = """
        {
            "appenders": [ { "type": "ConsoleAppender", "name": "cachedConsole" } ],
            "loggers": [ { "name": "", "severity": "Verbose", "appenderRef": "cachedConsole" } ]
        }
    """

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext<Context>() as Application
        SessionManager.application = application
        configFile = File(application.filesDir, "config.json")
        configFile.delete()
        File(application.filesDir, "config.json.tmp").delete()
        LogManager.clear()
    }

    @After
    fun tearDown() {
        LogManager.clear()
        SessionManager.login = null
        SessionManager.token = null
        configFile.delete()
    }

    @Test
    fun corruptCachedConfigIsDiscardedAndLoginStillHappens() {
        configFile.writeBytes(ByteArray(300) { 0xFF.toByte() })

        SessionManager.login(application, "appId", "appKey", null, null)

        assertNotNull("login must be set up even when the cached config is unreadable", SessionManager.login)
        assertFalse("corrupt cache must be deleted so the next start doesn't hit it again", configFile.exists())
        assertTrue("bundled config must be loaded as the fallback", LogManager.appenders.containsKey("cloud"))
        assertTrue(LogManager.appenders.containsKey("console"))
    }

    @Test
    fun validCachedConfigIsUsed() {
        configFile.writeText(customConfig)

        SessionManager.login(application, "appId", "appKey", null, null)

        assertNotNull(SessionManager.login)
        assertTrue(configFile.exists())
        assertTrue(LogManager.appenders.containsKey("cachedConsole"))
        assertFalse(LogManager.appenders.containsKey("cloud"))
    }

    @Test
    fun missingCachedConfigFallsBackToBundledConfig() {
        SessionManager.login(application, "appId", "appKey", null, null)

        assertNotNull(SessionManager.login)
        assertTrue(LogManager.appenders.containsKey("cloud"))
    }

    @Test
    fun writeConfigAtomicallyProducesParsableFileAndNoTempLeftover() {
        SessionManager.configFile = configFile
        configFile.writeText("stale")
        val config = ConfigResponse.create(JSONObject(customConfig))

        SessionManager.writeConfigAtomically(config)

        val written = ConfigResponse.create(JSONObject(configFile.readText()))
        assertEquals(1, written.appenders.size)
        assertEquals("cachedConsole", written.appenders[0].name)
        assertFalse(File(application.filesDir, "config.json.tmp").exists())
    }
}
