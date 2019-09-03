package io.shipbook.shipbooksdk

import android.app.Application
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.shipbook.shipbooksdk.Appenders.SBCloudAppender
import io.shipbook.shipbooksdk.Models.*
import io.shipbook.shipbooksdk.Networking.SessionManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/*
 *
 * Created by Elisha Sterngold on 14/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

@RunWith(AndroidJUnit4::class)
class AppenderTest {
    @Test
    fun cloudAppender() {
        val application = InstrumentationRegistry.getTargetContext().applicationContext as Application //InstrumentationRegistry.getInstrumentation().newApplication(TestApplication::class.java.classLoader,"TestApplication", InstrumentationRegistry.getContext())
        SessionManager.application = application

        val cloudAppender = SBCloudAppender("cloud", null)
        cloudAppender.file.delete()
        val token = "testing"
        SessionManager.token = token
        val logs1 = ArrayList<BaseLog>()
        logs1.add(Message(Severity.Verbose, "message1"))
        logs1.add(Message(Severity.Debug, "message2"))
        logs1.add(Message(Severity.Info, "message3"))
        logs1.add(Message(Severity.Warning, "message4"))
        logs1.add(Message(Severity.Error, "message5"))

        logs1.forEach { log -> cloudAppender.saveToFile(log)}

        cloudAppender.hasLog = false //doing like that it starts a new load
        SessionManager.token = null
        SessionManager.login = Login("59f196837211df173c430c51", "5a4b2e7f82416460bd2afc1cdb002e743ac25ce9")
        val logs2 = ArrayList<BaseLog>()
        logs2.add(Message(Severity.Info, "message1", "test2"))
        logs2.add(Message(Severity.Info, "message2", "test2"))
        logs2.add(Message(Severity.Info, "message3", "test2"))
        logs2.add(Message(Severity.Info, "message4", "test2"))
        logs2.add(Message(Severity.Info, "message5", "test2"))

        logs2.forEach { log -> cloudAppender.saveToFile(log)}

        // assertEquals("io.shipbook.shipbook.test", appContext.packageName)
        val sessionsData = cloudAppender.loadFromFile(cloudAppender.file)
        assertEquals(2, sessionsData.size)
        val sessionLogData1 = sessionsData[0]
        assertNull(sessionLogData1.login)
        assertEquals(token, sessionLogData1.token)
        assertEquals(sessionLogData1.logs, logs1)
        val sessionLogData2 = sessionsData[1]
        assertNull(sessionLogData2.token)
        assertEquals(SessionManager.login, sessionLogData2.login)
        assertEquals(sessionLogData2.logs, logs2)

////        val serialized = JSON.stringify(SessionLogData::class.serializer().list, sessionsData)
////        val sessionsDataRet = JSON.parse<List<SessionLogData>>(serialized)
//        assertEquals(sessionsData, sessionsDataRet)

    }
}