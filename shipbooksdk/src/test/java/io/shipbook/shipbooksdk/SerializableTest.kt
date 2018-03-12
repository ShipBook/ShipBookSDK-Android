//package io.shipbook.ShipBookSDK
//
//import io.shipbook.ShipBookSDK.Models.*
//import junit.framework.Assert.*
//import org.junit.Before
//import org.junit.Test
//
///*
// *
// * Created by Elisha Sterngold on 13/02/2018.
// * Copyright © 2018 ShipBook Ltd. All rights reserved.
// *
// */
//
//class SerializableTest {
//
//    @Before
//    fun setUp() {
//    }
//
//    @Test
//    fun baseLog() {
//        val message = Message("test", Severity.Error, "message1", false)
//        val baseLog: BaseLog = message
//
//        val serialized = JSON.stringify(BaseLogSerializer, baseLog)
//        val log = JSON.parse<Message>(serialized)
//        assertEquals(message, log)
//    }
//
//    @Test
//    fun appenderResponse() {
//        val appenderString = """
//            {
//              "type" : "ConsoleAppender",
//              "name" : "console",
//              "config": {
//                "pattern" : "test"
//              }
//            }
//            """
//        val appenderResponse = JSON.parse<ConfigResponse.AppenderResponse>(appenderString)
//        assertNotNull(appenderResponse)
//        assertEquals(appenderResponse.name, "console")
//        assertEquals(appenderResponse.type, "ConsoleAppender")
//        assertNotNull(appenderResponse.config)
//        assertEquals(appenderResponse.config!!["pattern"], "test")
//    }
//
//    @Test
//    fun loggerResponse() {
//        val loggerString = """
//            {
//                "name": "",
//                "severity": "Verbose",
//                "appenderRef": "console"
//            }
//        """
//        val loggerResponse = JSON.parse<ConfigResponse.LoggerResponse>(loggerString)
//        assertNotNull(loggerResponse)
//        assertEquals("" , loggerResponse.name)
//        assertEquals(Severity.Verbose, loggerResponse.severity)
//        assertEquals("console", loggerResponse.appenderRef)
//        assertEquals(Severity.Off, loggerResponse.callStackServerity)
//    }
//
//    @Test
//    fun configResponse() {
//        val configString = """
//            {
//                "appenders": [
//                {
//                    "type": "ConsoleAppender",
//                    "name": "console",
//                    "config": {
//                        "pattern": "message"
//                    }
//                }
//                ],
//                "loggers": [
//                {
//                    "name": "",
//                    "severity": "Verbose",
//                    "level": "Verbose",
//                    "appenderRef": "console"
//                }
//                ]
//            }
//        """
//        val configResponse = JSON.parse<ConfigResponse>(configString)
//        assertNotNull(configResponse)
//    }
//}