package io.shipbook.shipbooksdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import io.shipbook.shipbooksdk.Models.Message
import io.shipbook.shipbooksdk.Models.Severity
import io.shipbook.shipbooksdk.Networking.SessionManager
import io.shipbook.shipbooksdk.Util.toInternal
import java.util.*
import kotlin.concurrent.schedule

/*
 *
 * Created by Elisha Sterngold on 11/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

class Log(val tag: String)  {
    private val TAG = Log::class.java.simpleName
    @Volatile
    private var severity = LogManager.getSeverity(tag)
    @Volatile
    private var callStackSeverity = LogManager.getCallStackSeverity(tag)

    val broadcastReceiver =  object :  BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            InnerLog.d(TAG, "got receiver configChange for tag $tag")
            severity = LogManager.getSeverity(tag)
            callStackSeverity = LogManager.getCallStackSeverity(tag)
        }
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun e(tag:String, msg: String, throwable: Throwable? = null) {
            message(tag, msg, Severity.Error, throwable)
        }

        @JvmStatic
        @JvmOverloads
        fun w(tag:String, msg: String, throwable: Throwable? = null) {
            message(tag, msg, Severity.Warning, throwable)
        }

        @JvmStatic
        @JvmOverloads
        fun i(tag:String, msg: String, throwable: Throwable? = null) {
            message(tag, msg, Severity.Info, throwable)
        }

        @JvmStatic
        @JvmOverloads
        fun d(tag:String, msg: String, throwable: Throwable? = null) {
            message(tag, msg, Severity.Debug, throwable)
        }

        @JvmStatic
        @JvmOverloads
        fun v(tag:String, msg: String, throwable: Throwable? = null) {
            message(tag, msg, Severity.Verbose, throwable)
        }

        @JvmStatic
        @JvmOverloads
        fun message(tag:String, msg: String, severity: Severity, throwable: Throwable? = null) {
            Log(tag).message(msg, severity, throwable)
        }
    }

    init {
        InnerLog.d(TAG, "register broadcast receiver" )
        if (SessionManager.appContext != null) addBroadcastReceiver()
        else Timer().schedule(0) { //for the case that the application has a getLogger then it will be initialized before the start
            addBroadcastReceiver()
        }

    }

    protected fun finalize() {
        InnerLog.d(TAG, "unregister broadcast receiver" )
        LocalBroadcastManager.getInstance(SessionManager.appContext).unregisterReceiver(broadcastReceiver)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun addBroadcastReceiver() {
        LocalBroadcastManager.getInstance(SessionManager.appContext)
                .registerReceiver(broadcastReceiver, IntentFilter(BroadcastNames.CONFIG_CHANGE))
    }


    @JvmOverloads
    fun e(msg: String, throwable: Throwable? = null) {
        message(msg, Severity.Error, throwable)
    }

    @JvmOverloads
    fun w(msg: String, throwable: Throwable? = null) {
        message(msg, Severity.Warning, throwable)
    }

    @JvmOverloads
    fun i(msg: String, throwable: Throwable? = null) {
        message(msg, Severity.Info, throwable)
    }

    @JvmOverloads
    fun d(msg: String, throwable: Throwable? = null) {
        message(msg, Severity.Debug, throwable)
    }

    @JvmOverloads
    fun v(msg: String, throwable: Throwable? = null) {
        message(msg, Severity.Verbose, throwable)
    }

    @JvmOverloads
    fun message(msg: String, severity: Severity, throwable: Throwable? = null) {
        if (severity.ordinal > this.severity.ordinal) return
        val stackTrace = if (severity.ordinal <= callStackSeverity.ordinal) Thread.currentThread().stackTrace.toInternal() else null
        LogManager.push(Message(tag, severity, msg, stackTrace, throwable))
    }
}