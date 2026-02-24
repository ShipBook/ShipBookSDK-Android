package io.shipbook.shipbooksdk

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.shipbook.shipbooksdk.Appenders.AppenderFactory
import io.shipbook.shipbooksdk.Appenders.BaseAppender
import io.shipbook.shipbooksdk.Appenders.Config
import io.shipbook.shipbooksdk.Models.*
import io.shipbook.shipbooksdk.Networking.SessionManager
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogManagerTest {

    class TestAppender(name: String, config: Config?) : BaseAppender(name, config) {
        val logs = mutableListOf<BaseLog>()
        val messages: List<Message> get() = logs.filterIsInstance<Message>()

        override fun update(config: Config?) {}
        override fun push(log: BaseLog) { logs.add(log) }
        fun clear() { logs.clear() }
    }

    @Before
    fun setUp() {
        val application = ApplicationProvider.getApplicationContext<Context>() as Application
        SessionManager.application = application
        LogManager.clear()
    }

    @After
    fun tearDown() {
        LogManager.clear()
    }

    private fun setupTestAppender(
        appenderName: String = "test",
        module: String = "",
        severity: Severity = Severity.Verbose,
        callStackSeverity: Severity = Severity.Off
    ): TestAppender {
        val testAppender = TestAppender(appenderName, null)
        LogManager.appenders[appenderName] = testAppender
        LogManager.loggers.add(
            LogManager.Logger(module, severity, callStackSeverity, testAppender)
        )
        return testAppender
    }

    @Test
    fun pushMessageReceived() {
        val testAppender = setupTestAppender()
        val message = Message(Severity.Error, "test message", "TestTag")
        LogManager.push(message)

        assertEquals(1, testAppender.logs.size)
        assertEquals(1, testAppender.messages.size)
        val received = testAppender.messages[0]
        assertEquals("test message", received.message)
        assertEquals(Severity.Error, received.severity)
        assertEquals("TestTag", received.tag)
    }

    @Test
    fun severityFiltering() {
        val testAppender = setupTestAppender(severity = Severity.Warning)

        LogManager.push(Message(Severity.Error, "error", "Tag"))
        LogManager.push(Message(Severity.Warning, "warning", "Tag"))
        LogManager.push(Message(Severity.Info, "info", "Tag"))
        LogManager.push(Message(Severity.Debug, "debug", "Tag"))
        LogManager.push(Message(Severity.Verbose, "verbose", "Tag"))

        assertEquals(2, testAppender.messages.size)
        assertEquals("error", testAppender.messages[0].message)
        assertEquals("warning", testAppender.messages[1].message)
    }

    @Test
    fun tagBasedRouting() {
        val appender1 = TestAppender("app1", null)
        val appender2 = TestAppender("app2", null)
        LogManager.appenders["app1"] = appender1
        LogManager.appenders["app2"] = appender2
        LogManager.loggers.add(LogManager.Logger("com.example.ui", Severity.Verbose, Severity.Off, appender1))
        LogManager.loggers.add(LogManager.Logger("com.example.data", Severity.Verbose, Severity.Off, appender2))

        LogManager.push(Message(Severity.Info, "ui message", "com.example.ui.MainActivity"))
        LogManager.push(Message(Severity.Info, "data message", "com.example.data.Repository"))

        assertEquals(1, appender1.messages.size)
        assertEquals("ui message", appender1.messages[0].message)
        assertEquals(1, appender2.messages.size)
        assertEquals("data message", appender2.messages[0].message)
    }

    @Test
    fun allSeverityLevels() {
        val testAppender = setupTestAppender()
        val severities = listOf(Severity.Error, Severity.Warning, Severity.Info, Severity.Debug, Severity.Verbose)

        severities.forEach { sev ->
            LogManager.push(Message(sev, "${sev.identifier} message", "Tag"))
        }

        assertEquals(5, testAppender.messages.size)
        severities.forEachIndexed { index, sev ->
            assertEquals(sev, testAppender.messages[index].severity)
        }
    }

    @Test
    fun throwableException() {
        val testAppender = setupTestAppender()
        val throwable = RuntimeException("test error")
        val message = Message(Severity.Error, "error with throwable", "Tag", throwable = throwable)
        LogManager.push(message)

        assertEquals(1, testAppender.messages.size)
        val received = testAppender.messages[0]
        assertNotNull(received.exception)
        assertEquals("java.lang.RuntimeException", received.exception?.name)
        assertEquals("test error", received.exception?.reason)
    }

    @Test
    fun nonMessageLog() {
        val testAppender = setupTestAppender()
        val screenEvent = ScreenEvent("TestScreen")
        LogManager.push(screenEvent)

        assertEquals(1, testAppender.logs.size)
        assertEquals(0, testAppender.messages.size)
        assertTrue(testAppender.logs[0] is ScreenEvent)
    }

    @Test
    fun registerCustomAppender() {
        AppenderFactory.register("TestAppender") { name, config ->
            TestAppender(name, config)
        }

        val configString = """
            {
                "appenders": [
                    { "type": "TestAppender", "name": "customTest" }
                ],
                "loggers": [
                    { "name": "", "severity": "Verbose", "appenderRef": "customTest" }
                ]
            }
        """
        val configResponse = ConfigResponse.create(JSONObject(configString))
        LogManager.config(configResponse)

        assertTrue(LogManager.appenders["customTest"] is TestAppender)

        val message = Message(Severity.Info, "custom appender test", "Tag")
        LogManager.push(message)

        val testAppender = LogManager.appenders["customTest"] as TestAppender
        assertEquals(1, testAppender.messages.size)
        assertEquals("custom appender test", testAppender.messages[0].message)
    }

    @Test
    fun logStaticMethod() {
        val testAppender = setupTestAppender()
        Log.message("TestTag", "static log test", Severity.Error)

        assertEquals(1, testAppender.messages.size)
        assertEquals("static log test", testAppender.messages[0].message)
        assertEquals(Severity.Error, testAppender.messages[0].severity)
        assertEquals("TestTag", testAppender.messages[0].tag)
    }

    @Test
    fun logStaticMethodWithThrowable() {
        val testAppender = setupTestAppender()
        val error = IllegalArgumentException("bad argument")
        Log.message("TestTag", "error log", Severity.Error, error)

        assertEquals(1, testAppender.messages.size)
        val received = testAppender.messages[0]
        assertNotNull(received.exception)
        assertEquals("java.lang.IllegalArgumentException", received.exception?.name)
        assertEquals("bad argument", received.exception?.reason)
    }

    @Test
    fun logStaticSeverityFiltering() {
        val testAppender = setupTestAppender(severity = Severity.Error)

        Log.message("TestTag", "should pass", Severity.Error)
        Log.message("TestTag", "should not pass", Severity.Info)

        assertEquals(1, testAppender.messages.size)
        assertEquals("should pass", testAppender.messages[0].message)
    }
}
