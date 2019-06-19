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

/**
 * The class that create the logs.
 *
 * There are two ways that you can call this class:
 * 1. Getting this class from `ShipBook.getLogger`
 * 2. Calling static functions. This is not recommended and the caveats are listed below. The advantage is that it is then the default android api.
 *
 * As mentioned, working with this static logger isn’t ideal:
 * * Performance is slower, especially in cases where the log is closed
 * * The log’s information is less detailed. Ideally, you should create a logger for each class.
 * * The Log name can have a name collision with a local Log class.
 *
 * @property tag The tag of the log
 */
class Log(val tag: String)  {
    private val TAG = Log::class.java.simpleName
    @Volatile
    private var severity = LogManager.getSeverity(tag)
    @Volatile
    private var callStackSeverity = LogManager.getCallStackSeverity(tag)

    private var counter = 0

    private val broadcastReceiver =  object :  BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            InnerLog.d(TAG, "got receiver configChange for tag $tag")
            severity = LogManager.getSeverity(tag)
            callStackSeverity = LogManager.getCallStackSeverity(tag)
        }
    }

    companion object {
        /**
         * Error message
         * @param tag The tag.
         * @param msg The message.
         * @param throwable If there is a log throwable.
         */
        @JvmStatic
        @JvmOverloads
        fun e(tag:String, msg: String, throwable: Throwable? = null) {
            message(tag, msg, Severity.Error, throwable)
        }

        /**
         * Warning message
         * @param tag The tag.
         * @param msg The message.
         * @param throwable If there is a log throwable.
         */
        @JvmStatic
        @JvmOverloads
        fun w(tag:String, msg: String, throwable: Throwable? = null) {
            message(tag, msg, Severity.Warning, throwable)
        }

        /**
         * Information message
         * @param tag The tag.
         * @param msg The message.
         * @param throwable If there is a log throwable.
         */
        @JvmStatic
        @JvmOverloads
        fun i(tag:String, msg: String, throwable: Throwable? = null) {
            message(tag, msg, Severity.Info, throwable)
        }

        /**
         * Debug message
         * @param tag The tag.
         * @param msg The message.
         * @param throwable If there is a log throwable.
         */
        @JvmStatic
        @JvmOverloads
        fun d(tag:String, msg: String, throwable: Throwable? = null) {
            message(tag, msg, Severity.Debug, throwable)
        }

        /**
         * Verbose message
         * @param tag The tag.
         * @param msg The message.
         * @param throwable If there is a log throwable.
         */
        @JvmStatic
        @JvmOverloads
        fun v(tag:String, msg: String, throwable: Throwable? = null) {
            message(tag, msg, Severity.Verbose, throwable)
        }

        /**
         * General message
         * @param tag The tag.
         * @param msg The message.
         * @param severity The log severity of the message.
         * @param throwable If there is a log throwable.
         * @param function the function name
         * @param fileName the fileName
         * @param lineNumber the line number
         * @param className the class name
         */
        @JvmStatic
        @JvmOverloads
        fun message(tag:String,
                    msg: String,
                    severity: Severity,
                    throwable: Throwable? = null,
                    function: String? = null,
                    fileName: String? = null,
                    lineNumber: Int? = null,
                    className: String? = null) {
            Log(tag).message(msg, severity, throwable, function, fileName, lineNumber, className)
        }
    }

    init {
        InnerLog.d(TAG, "register broadcast receiver" )
        addBroadcastReceiver()
    }

    /**
     * the finalize function
     */
    protected fun finalize() {
        InnerLog.d(TAG, "unregister broadcast receiver" )
        if (SessionManager.appContext != null)
            LocalBroadcastManager.getInstance(SessionManager.appContext!!).unregisterReceiver(broadcastReceiver)
    }

    private fun addBroadcastReceiver() {
        InnerLog.d(TAG, "add broadcast receiver with counter" + counter )
        if (SessionManager.appContext != null && counter > 0) LocalBroadcastManager.getInstance(SessionManager.appContext!!)
               .registerReceiver(broadcastReceiver, IntentFilter(BroadcastNames.CONFIG_CHANGE))
        else Timer().schedule(0) { //for the case that the application has a getLogger then it will be initialized before the start
            counter++
            if (counter < 5) addBroadcastReceiver()
            else InnerLog.d(TAG, "counter bigger than 5" )
        }
    }


    /**
     * Error message
     * @param msg The message.
     * @param throwable If there is a log throwable.
     */
    @JvmOverloads
    fun e(msg: String, throwable: Throwable? = null) {
        message(msg, Severity.Error, throwable)
    }

    /**
     * Warning message
     * @param msg The message.
     * @param throwable If there is a log throwable.
     */
    @JvmOverloads
    fun w(msg: String, throwable: Throwable? = null) {
        message(msg, Severity.Warning, throwable)
    }

    /**
     * Information message
     * @param msg The message.
     * @param throwable If there is a log throwable.
     */
    @JvmOverloads
    fun i(msg: String, throwable: Throwable? = null) {
        message(msg, Severity.Info, throwable)
    }

    /**
     * Debug message
     * @param msg The message.
     * @param throwable If there is a log throwable.
     */
    @JvmOverloads
    fun d(msg: String, throwable: Throwable? = null) {
        message(msg, Severity.Debug, throwable)
    }

    /**
     * Verbose message
     * @param msg The message.
     * @param throwable If there is a log throwable.
     */
    @JvmOverloads
    fun v(msg: String, throwable: Throwable? = null) {
        message(msg, Severity.Verbose, throwable)
    }

    /**
     * General message
     * @param msg The message.
     * @param severity The log severity of the message.
     * @param throwable If there is a log throwable.
     * @param function the function name
     * @param fileName the fileName
     * @param lineNumber the line number
     * @param className the class name

     */
    @JvmOverloads
    fun message(msg: String,
                severity: Severity,
                throwable: Throwable? = null,
                function: String? = null,
                fileName: String? = null,
                lineNumber: Int? = null,
                className: String? = null) {
        if (severity.ordinal > this.severity.ordinal) return
        val stackTrace = if (severity.ordinal <= callStackSeverity.ordinal) Throwable().stackTrace.toInternal() else null
        LogManager.push(Message(tag, severity, msg, stackTrace, throwable, function, fileName, lineNumber, className))
    }
}