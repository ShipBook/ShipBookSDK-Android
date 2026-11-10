package io.shipbook.shipbooksdk

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.shipbook.shipbooksdk.Models.Exception
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExceptionManagerTest {

    class OrderActivity {
        fun onCreate(): Nothing = throw NullPointerException("Attempt to invoke virtual method on a null object reference")
        fun onResume(): Nothing = throw NullPointerException()
    }

    private val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
    private var forwarded: Throwable? = null
    private val appender = LogManagerTest.TestAppender("test", null)

    @Before
    fun setUp() {
        LogManager.clear()
        LogManager.appenders["test"] = appender
        Thread.setDefaultUncaughtExceptionHandler { _, throwable -> forwarded = throwable }
        ExceptionManager.start()
    }

    @After
    fun tearDown() {
        Thread.setDefaultUncaughtExceptionHandler(originalHandler)
        LogManager.clear()
    }

    private fun crash(throwable: Throwable): Exception {
        Thread.getDefaultUncaughtExceptionHandler()!!.uncaughtException(Thread.currentThread(), throwable)
        assertEquals(1, appender.logs.size)
        return appender.logs[0] as Exception
    }

    private fun activityThreadWrapper(message: String, cause: Throwable) = RuntimeException(message, cause).apply {
        stackTrace = arrayOf(
            java.lang.StackTraceElement("android.app.ActivityThread", "performLaunchActivity", "ActivityThread.java", 3782),
            java.lang.StackTraceElement("com.android.internal.os.ZygoteInit", "main", "ZygoteInit.java", 1034)
        )
    }

    @Test
    fun lifecycleCrashReportsRootCauseWithAppFrames() {
        val npe = runCatching { OrderActivity().onCreate() }.exceptionOrNull()!!
        val wrapper = activityThreadWrapper("Unable to start activity ComponentInfo{com.example.shop/com.example.shop.OrderActivity}: $npe", npe)

        val exception = crash(wrapper)

        assertEquals("java.lang.NullPointerException", exception.name)
        assertEquals("Attempt to invoke virtual method on a null object reference", exception.reason)
        assertEquals(OrderActivity::class.java.name, exception.stackTrace[0].declaringClass)
        assertEquals("onCreate", exception.stackTrace[0].methodName)
        assertEquals("ExceptionManagerTest.kt", exception.stackTrace[0].fileName)
        assertEquals(npe.stackTrace.size, exception.stackTrace.size)
        assertSame(wrapper, forwarded)
    }

    @Test
    fun messageLessRootCauseKeepsWrapperMessage() {
        val npe = runCatching { OrderActivity().onResume() }.exceptionOrNull()!!
        val wrapper = activityThreadWrapper("Unable to resume activity ComponentInfo{com.example.shop/com.example.shop.OrderActivity}: $npe", npe)

        val exception = crash(wrapper)

        assertEquals("java.lang.NullPointerException", exception.name)
        assertEquals(wrapper.message, exception.reason)
        assertEquals("onResume", exception.stackTrace[0].methodName)
    }

    @Test
    fun crashWithoutCauseReportsItself() {
        val npe = runCatching { OrderActivity().onCreate() }.exceptionOrNull()!!

        val exception = crash(npe)

        assertEquals("java.lang.NullPointerException", exception.name)
        assertEquals(npe.message, exception.reason)
        assertEquals("onCreate", exception.stackTrace[0].methodName)
    }

    @Test
    fun causeCycleStopsAtLastUnvisitedThrowable() {
        val outer = IllegalStateException("outer")
        val inner = IllegalArgumentException("inner")
        outer.initCause(inner)
        inner.initCause(outer)

        val exception = crash(outer)

        assertEquals("java.lang.IllegalArgumentException", exception.name)
        assertEquals("inner", exception.reason)
    }
}
