package io.shipbook.shipbooksdk.Appenders

import android.util.Log
import io.shipbook.shipbooksdk.Models.BaseLog
import io.shipbook.shipbooksdk.Models.Message
import io.shipbook.shipbooksdk.Models.Severity

/*
 *
 * Created by Elisha Sterngold on 13/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal class ConsoleAppender(name: String, config: Config?): BaseAppender(name, config) {

    override fun update(config: Config?) {
    }

    override fun push(log: BaseLog) {
        when (log) {
            is Message -> {
                if (log.throwable == null) {
                    when (log.severity) { // looks like without throwable there is a native implementation
                        Severity.Error -> Log.e(log.tag, log.message)
                        Severity.Warning -> Log.w(log.tag, log.message)
                        Severity.Info -> Log.i(log.tag, log.message)
                        Severity.Debug -> Log.d(log.tag, log.message)
                        Severity.Verbose -> Log.v(log.tag, log.message)
                        Severity.Off -> Unit
                    }
                }
                else {
                    when (log.severity) {
                        Severity.Error -> Log.e(log.tag, log.message, log.throwable)
                        Severity.Warning -> Log.w(log.tag, log.message, log.throwable)
                        Severity.Info -> Log.i(log.tag, log.message, log.throwable)
                        Severity.Debug -> Log.d(log.tag, log.message, log.throwable)
                        Severity.Verbose -> Log.v(log.tag, log.message, log.throwable)
                        Severity.Off -> Unit
                    }
                }
            }
        }
    }
}