package io.shipbook.shipbooksdk

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.shipbook.shipbooksdk.Networking.SessionManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionManagerTest {
    private lateinit var application: Application

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
        Storage.init(application)
        Storage.remove("config")
        LogManager.clear()
    }

    @After
    fun tearDown() {
        LogManager.clear()
        SessionManager.login = null
        SessionManager.token = null
        SessionManager.sessionLink = null
        Storage.remove("config")
    }

    @Test
    fun corruptCachedConfigIsDiscardedAndLoginStillHappens() {
        Storage.setString("config", "not json")

        SessionManager.login(application, "appId", "appKey", null, null)

        assertNotNull("login must be set up even when the cached config is unreadable", SessionManager.login)
        assertNull("corrupt cache must be deleted so the next start doesn't hit it again", Storage.getString("config"))
        assertTrue("bundled config must be loaded as the fallback", LogManager.appenders.containsKey("cloud"))
        assertTrue(LogManager.appenders.containsKey("console"))
    }

    @Test
    fun validCachedConfigIsUsed() {
        Storage.setString("config", customConfig)

        SessionManager.login(application, "appId", "appKey", null, null)

        assertNotNull(SessionManager.login)
        assertEquals(customConfig, Storage.getString("config"))
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
    fun sessionHeadersCarryTheSessionLink() {
        SessionManager.sessionLink = "1:698ad6e6595b3f6a7590b24f:66f1c2a9e4b0d3f8a1b2c3d4"

        assertEquals(mapOf("x-shipbook-session" to "1:698ad6e6595b3f6a7590b24f:66f1c2a9e4b0d3f8a1b2c3d4"), ShipBook.getSessionHeaders())
    }

    @Test
    fun sessionHeadersAreEmptyBeforeLogin() {
        assertEquals(emptyMap<String, String>(), ShipBook.getSessionHeaders())
    }
}
