package io.shipbook.shipbooksdk

import android.util.Log
import io.shipbook.shipbooksdk.Models.Severity

/*
 *
 * Created by Elisha Sterngold on 14/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal object InnerLog {
    var enabled = false

    fun e(tag:String, msg: String, throwable: Throwable? = null) {
        message(tag, msg, Severity.Error, throwable)
    }

    fun w(tag:String, msg: String, throwable: Throwable? = null) {
        message(tag, msg, Severity.Warning, throwable)
    }

    fun i(tag:String, msg: String, throwable: Throwable? = null) {
        message(tag, msg, Severity.Info, throwable)
    }

    fun d(tag:String, msg: String, throwable: Throwable? = null) {
        message(tag, msg, Severity.Debug, throwable)
    }

    fun v(tag:String, msg: String, throwable: Throwable? = null) {
        message(tag, msg, Severity.Verbose, throwable)
    }

    fun message(tag:String, msg: String, severity: Severity, throwable: Throwable?) {
        if (!enabled) return
        val fullTag = "Shipbook-$tag"
        if (throwable != null) {
            when (severity) {
                Severity.Error -> Log.e(fullTag, msg, throwable)
                Severity.Warning -> Log.w(fullTag, msg, throwable)
                Severity.Info -> Log.i(fullTag, msg, throwable)
                Severity.Debug -> Log.d(fullTag, msg, throwable)
                Severity.Verbose -> Log.v(fullTag, msg, throwable)
                Severity.Off -> Unit
            }

        }
        else {
            when (severity) {
                Severity.Error -> Log.e(fullTag, msg)
                Severity.Warning -> Log.w(fullTag, msg)
                Severity.Info -> Log.i(fullTag, msg)
                Severity.Debug -> Log.d(fullTag, msg)
                Severity.Verbose -> Log.v(fullTag, msg)
                Severity.Off -> Unit
            }
        }
    }
}