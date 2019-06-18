package io.shipbook.shipbooksdk.Appenders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import io.shipbook.shipbooksdk.BroadcastNames
import io.shipbook.shipbooksdk.InnerLog
import io.shipbook.shipbooksdk.Models.*

import io.shipbook.shipbooksdk.Networking.ConnectionClient.request
import io.shipbook.shipbooksdk.Networking.HttpMethod
import io.shipbook.shipbooksdk.Networking.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.timerTask

/*
 *
 * Created by Elisha Sterngold on 13/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */


internal class SBCloudAppender(name: String, config: Config?): BaseAppender(name, config) {
    // consts/
    private val TAG = SBCloudAppender::class.java.simpleName

    private val FILE_CLASS_SEPARATOR = ": "
    private val TOKEN = "token"
    private val NEW_LINE_SEPARATOR = "\n"

    @Volatile
    private var maxTime: Double = 3.0
    @Volatile
    private var maxFileSize: Int = 1048576
    @Volatile
    private var flushSeverity: Severity = Severity.Verbose
    @Volatile
    private var flushSize: Int = 40

    val broadcastReceiver =  object :  BroadcastReceiver() {
        override fun onReceive(contxt: Context, intent: Intent) {
            when(intent.action) {
                BroadcastNames.USER_CHANGE -> {
                    InnerLog.d(TAG, "received user change")
                    GlobalScope.launch(SessionManager.threadContext) {
                        val user = SessionManager.login?.user
                        user?.let {
                            saveToFile(it)
                            createTimer()
                        }
                    }
                }
                BroadcastNames.CONNECTED -> {
                    InnerLog.d(TAG, "received connected")
                    GlobalScope.launch(SessionManager.threadContext) { send() }
                }
            }
        }
    }

    // file names
    val file: File
    val tempFile: File

    private var flushQueue: Queue<BaseLog> = LinkedBlockingQueue()
    var hasLog = false


    @Volatile
    private var timer: Timer? = null
    @Volatile
    private var uploadingSavedData = false

    init {
        val context = SessionManager.appContext

        file = File(context?.filesDir, "CloudQueue.log")
        tempFile = File(context?.filesDir, "TempCloudQueue.log")

        val intentFilter = IntentFilter()
        intentFilter.addAction(BroadcastNames.USER_CHANGE)
        intentFilter.addAction(BroadcastNames.CONNECTED)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(broadcastReceiver, intentFilter)
    }

    @Suppress("unused")
    protected fun finalize() {
        InnerLog.d(TAG, "unregister broadcast receiver" )
        if (SessionManager.appContext == null) return
        LocalBroadcastManager.getInstance(SessionManager.appContext!!).unregisterReceiver(broadcastReceiver)
    }


    override fun update(config: Config?) {
        config?.apply {
            this["maxTime"]?.toDouble()?.let { maxTime = it }
            this["maxFileSize"]?.toInt()?.let { maxFileSize = it }
            this["flushSeverity"]?.let { flushSeverity = Severity.valueOf(it) }
            this["flushSize"]?.toInt()?.let { flushSize = it }
        }
    }

    fun saveToFile(obj: BaseObj) {
        try {
            if (file.length() > maxFileSize) {
                file.delete()
                hasLog = false
            }
            if (!hasLog) {
                if (SessionManager.token != null) {
                    val line = TOKEN + FILE_CLASS_SEPARATOR + SessionManager.token + NEW_LINE_SEPARATOR
                    file.appendText(line)
                } else SessionManager.login?.let { login ->
                    val prefix = login.javaClass.name + FILE_CLASS_SEPARATOR
                    val json = login.toJson().toString()
                    val line = prefix + json + NEW_LINE_SEPARATOR
                    file.appendText(line)
                }
            }
            var prefix = ""
            when(obj) {
                is BaseLog -> prefix = BaseLog::class.java.name + FILE_CLASS_SEPARATOR
                is User -> prefix = User::class.java.name + FILE_CLASS_SEPARATOR
            }

            val json = obj.toJson()
            val line = prefix + json + NEW_LINE_SEPARATOR
            file.appendText(line)
            hasLog = true
        }
        catch (e: Exception) {
            InnerLog.e(TAG, "save file got error", e)
        }

    }

    fun loadFromFile(file: File): List<SessionLogData> {
        val sessionsData : MutableList<SessionLogData> = mutableListOf()
        val loginName = Login::class.java.name
        val baseLogName = BaseLog::class.java.name
        val userName = User::class.java.name

        var sessionLogData: SessionLogData? = null
        try {
            val lines = file.readLines()
            lines.forEach { line ->
                val parts = line.split(FILE_CLASS_SEPARATOR, limit = 2)
                val className = parts[0]
                val classJson = parts[1]
                when (className) {
                    loginName -> {
                        val login = Login.create(JSONObject(classJson))
                        sessionLogData?.let { sessionsData.add(it) }
                        sessionLogData = SessionLogData(login = login)
                    }
                    TOKEN -> {
                        sessionLogData?.let { sessionsData.add(it) }
                        sessionLogData = SessionLogData(token = classJson)
                    }
                    baseLogName -> {
                        val log = BaseLog.create(JSONObject(classJson))
                        sessionLogData?.logs?.add(log)
                    }
                    userName -> {
                        val user = User.create(JSONObject(classJson))
                        sessionLogData?.user = user
                    }
                    else -> InnerLog.e(TAG, "no classname exists")
                }
            }
            sessionLogData?.let { sessionsData.add(it) }
        }
        catch (e: Exception) {
            InnerLog.e(TAG, "load from file failed", e)
        }
        return sessionsData
    }

    private fun saveFlushQueue() {
        flushQueue.forEach() { saveToFile(it)}
        flushQueue = LinkedBlockingQueue()
    }

    private fun createTimer() {
        if (timer != null) return
        InnerLog.d(TAG, "the current time $maxTime")
        timer = Timer(true)
        timer?.schedule(timerTask { GlobalScope.launch(SessionManager.threadContext) { timer = null; send() }}, (maxTime * 1000).toLong())
    }

    override fun push(log: BaseLog) {
        GlobalScope.launch(SessionManager.threadContext) {
            when (log) {
                is Message -> push(log)
                is BaseEvent -> push(log)
                is io.shipbook.shipbooksdk.Models.Exception -> push(log)
            }
        }
    }

    private fun push(log: Message) {
        if (flushSeverity.ordinal < log.severity.ordinal) {
            flushQueue.add(log)
            if (flushQueue.size > flushSize) flushQueue.remove()
        }
        else { // the info needs to be flushed and saved
            saveFlushQueue()
            saveToFile(log)
            createTimer()
        }
    }

    private fun push(event: BaseEvent) {
        flushQueue.add(event)
        if (flushQueue.size > flushSize) flushQueue.remove()
    }

    private fun push(exception: io.shipbook.shipbooksdk.Models.Exception) {
        saveFlushQueue()
        saveToFile(exception)
    }

    private suspend fun send() {
        timer?.let { it.cancel(); timer = null}
        if (uploadingSavedData) {
            InnerLog.d(TAG,"uploading saved data")
            createTimer()
        }

        if (!SessionManager.connected) {
            InnerLog.d(TAG, "not connected")
            return
        }


        if (!file.isFile) {
            InnerLog.d(TAG, "no file")
            return
        }

        uploadingSavedData = true

        try {
            tempFile.delete()
            file.renameTo(tempFile)
            hasLog = false
            val sessionsData = loadFromFile(tempFile)

            if (sessionsData.count() <= 0) {
                InnerLog.w(TAG,"empty session data eventhough loaded from file")
                uploadingSavedData = false
                return
            }

            // remove login that is current session and set token
            for (sessionData in sessionsData) {
                if (sessionData.login == SessionManager.login) {
                    sessionData.token = SessionManager.token
                    sessionData.login = null
                }
            }

            // should be with current time and not with the original time that has nothing to do with it
            val currentTime = Date()
            sessionsData.forEach() { it.login?.deviceTime = currentTime}
            try {
                val response = request("sessions/uploadSavedData", sessionsData, HttpMethod.POST)
                if (response.ok) tempFile.delete()
                else {
                    InnerLog.i(TAG, "server probably down")
                    concatTmpFile()
                }

                uploadingSavedData = false
            }
            catch (e: Exception) {
                InnerLog.e(TAG, "Had an error in send", e)
                uploadingSavedData = false
            }

        }
        catch (e: Exception) {
            InnerLog.e(TAG, "Had an error in send", e)
            uploadingSavedData = false
            return
        }

    }

    private fun concatTmpFile() {
        if (file.isFile) tempFile.appendText(file.readText())
        tempFile.renameTo(file)
    }
}