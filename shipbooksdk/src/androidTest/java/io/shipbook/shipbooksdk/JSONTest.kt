package io.shipbook.shipbooksdk

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.shipbook.shipbooksdk.Models.*
import io.shipbook.shipbooksdk.Networking.SessionManager
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/*
 *
 * Created by Elisha Sterngold on 21/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

@RunWith(AndroidJUnit4::class)
class JSONTest {
    @Before
    fun setUp() {
        val application = ApplicationProvider.getApplicationContext<Context>() as Application
        SessionManager.application = application
    }

    @Test
    fun message() {
        val message = Message(Severity.Error, "message1")
        val baseLog = BaseLog.create(message.toJson()) as Message
        assertEquals(message, baseLog)
    }

    @Test
    fun appenderResponse() {
        val appenderString = """
            {
              "type" : "ConsoleAppender",
              "name" : "console",
              "config": {
                "pattern" : "test"
              }
            }
            """
        val appenderResponse = ConfigResponse.AppenderResponse.create(JSONObject(appenderString))
        assertNotNull(appenderResponse)
        assertEquals(appenderResponse.name, "console")
        assertEquals(appenderResponse.type, "ConsoleAppender")
        assertNotNull(appenderResponse.config)
        assertEquals(appenderResponse.config!!["pattern"], "test")


        val appenderResponse2 = ConfigResponse.AppenderResponse.create(appenderResponse.toJson())
        assertEquals(appenderResponse, appenderResponse2)
    }

    @Test
    fun appenderResponseNoConfig() {
        val appenderString = """
            {
              "type" : "ConsoleAppender",
              "name" : "console"
            }
            """
        val appenderResponse = ConfigResponse.AppenderResponse.create(JSONObject(appenderString))
        assertNotNull(appenderResponse)
        assertEquals(appenderResponse.name, "console")
        assertEquals(appenderResponse.type, "ConsoleAppender")
        assertNull(appenderResponse.config)

        val appenderResponse2 = ConfigResponse.AppenderResponse.create(appenderResponse.toJson())
        assertEquals(appenderResponse, appenderResponse2)
    }

    @Test
    fun loggerResponse() {
        val loggerString = """
            {
                "name": "",
                "severity": "Verbose",
                "appenderRef": "console"
            }
        """
        val loggerResponse = ConfigResponse.LoggerResponse.create(JSONObject(loggerString))
        assertNotNull(loggerResponse)
        assertEquals("" , loggerResponse.name)
        assertEquals(Severity.Verbose, loggerResponse.severity)
        assertEquals("console", loggerResponse.appenderRef)
        assertEquals(Severity.Off, loggerResponse.callStackSeverity)
        val loggerResponse2 = ConfigResponse.LoggerResponse.create(loggerResponse.toJson())
        assertEquals(loggerResponse, loggerResponse2)
    }

    @Test
    fun configResponse() {
        val configString = """
            {
                "appenders": [
                {
                    "type": "ConsoleAppender",
                    "name": "console",
                    "config": {
                        "pattern": "message"
                    }
                }
                ],
                "loggers": [
                {
                    "name": "",
                    "severity": "Verbose",
                    "level": "Verbose",
                    "appenderRef": "console"
                }
                ]
            }
        """
        val configResponse = ConfigResponse.create(JSONObject(configString))
        assertEquals(1, configResponse.appenders.size)
        assertEquals(1, configResponse.loggers.size)
        val configResponse2 = ConfigResponse.create(configResponse.toJson())
        assertEquals(configResponse, configResponse2)
    }

    @Test
    fun loginResponse() {
        val configString = """
            {
                "appenders": [
                {
                    "type": "ConsoleAppender",
                    "name": "console",
                    "config": {
                        "pattern": "message"
                    }
                }
                ],
                "loggers": [
                {
                    "name": "",
                    "severity": "Verbose",
                    "level": "Verbose",
                    "appenderRef": "console"
                }
                ]
            }
        """
        val configResponse = ConfigResponse.create(JSONObject(configString))
        val loginResponse = LoginResponse("testToken", configResponse, "")
        val loginResponse2 = LoginResponse.create(loginResponse.toJson())
        assertEquals(loginResponse, loginResponse2)

    }

    @Test
    fun login() {
        val login = Login("idTest", "appTest")
        val login2 = Login.create(login.toJson())
        assertEquals(login, login2)
    }

    @Test
    fun user() {
        val user = User("test", email = "test@test.com")
        val user2 = User.create(user.toJson())
        assertEquals(user, user2)
    }

    @Test
    fun userWithAdditional() {
        val user = User("test", email = "test@test.com", additionalInfo = mapOf("locataion" to "oslo"))
        val user2 = User.create(user.toJson())
        assertEquals(user, user2)
    }



    @Test
    fun sessionLogData() {
        val message = Message(Severity.Error, "message1")
        //val login = Login("idTest", "appTest")
        val login = Login("idTest", "appTest")
        val sessionLogData = SessionLogData("test", login)
        sessionLogData.logs.add(message)


        val sessionLogData2 = SessionLogData.create(sessionLogData.toJson())
        assertEquals(sessionLogData, sessionLogData2)

    }

    @Test
    fun activityEvent() {
        val activityEvent = ActivityEvent("nameTest", "eventTest", "titleTest")
        val baseLog = BaseLog.create(activityEvent.toJson()) as ActivityEvent
        assertEquals(activityEvent, baseLog)

    }

    @Test
    fun screenEvent() {
        val screenEvent = ScreenEvent("test")
        val baseLog = BaseLog.create(screenEvent.toJson()) as ScreenEvent
        assertEquals(screenEvent, baseLog)
    }


    @Test
    fun configEvent() {
        val configEvent = ConfigEvent(Orientation.Landscape)
        val baseLog = BaseLog.create(configEvent.toJson()) as ConfigEvent
        assertEquals(configEvent, baseLog)
    }

    @Test
    fun exception() {
        val stackTrace = Thread.currentThread().stackTrace
        val stackTraceList : ArrayList<StackTraceElement> = arrayListOf()
        stackTrace.forEach {
            val stackTraceElement = StackTraceElement(it.className, it.methodName, it.fileName, it.lineNumber)
            stackTraceList.add(stackTraceElement)
        }

        val exception = Exception("nameTest", "reasonTest", stackTraceList)
        val baseLog = BaseLog.create(exception.toJson()) as Exception
        assertEquals(exception, baseLog)
    }
}