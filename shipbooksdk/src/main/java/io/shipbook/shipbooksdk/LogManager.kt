package io.shipbook.shipbooksdk

import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import io.shipbook.shipbooksdk.Appenders.AppenderFactory
import io.shipbook.shipbooksdk.Appenders.BaseAppender
import io.shipbook.shipbooksdk.Models.BaseLog
import io.shipbook.shipbooksdk.Models.ConfigResponse
import io.shipbook.shipbooksdk.Models.Message
import io.shipbook.shipbooksdk.Models.Severity
import io.shipbook.shipbooksdk.Networking.SessionManager

/*
 *
 * Created by Elisha Sterngold on 11/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal typealias AppenderMap = MutableMap<String, BaseAppender>
internal object LogManager {
    class Logger (val key: String, val severity: Severity, val callStackSeverity: Severity, val appender: BaseAppender)

    @Volatile
    var appenders: AppenderMap = mutableMapOf()
    @Volatile
    var loggers: MutableList<Logger> = mutableListOf()

    fun clear() {
        appenders = mutableMapOf()
        loggers = mutableListOf()
    }

    fun push(log: BaseLog){
        if (log is Message) { //is a message going according to tags
            val loggers = this.loggers //so that if something happens asynchronous it won't disturb
            val appenders = this.appenders //so that if something happens asynchronous it won't disturb
            val appenderNames = HashSet<String>()

            loggers.forEach() {
                if (log.tag!!.startsWith(it.key) && log.severity.ordinal <= it.severity.ordinal) appenderNames.add(it.appender.name)
            }

            appenderNames.forEach() { appenders[it]?.push(log) }
        }
        else { // isn't a message and therefor there isn't any tags
            val appenders = this.appenders //so that if something happens asynchronous it won't disturb
            appenders.forEach() { it.value.push(log) }
        }
    }

    fun getSeverity(tag: String): Severity {
        val loggers = this.loggers
        var severity = Severity.Off
        loggers.forEach() {
            if (tag.startsWith(it.key) && it.severity.ordinal > severity.ordinal) severity = it.severity
        }

        return severity
    }

    fun getCallStackSeverity(tag: String): Severity {
        val loggers = this.loggers
        var callStackSeverity = Severity.Off
        loggers.forEach() {
            if (tag.startsWith(it.key) && it.callStackSeverity.ordinal > callStackSeverity.ordinal) callStackSeverity = it.callStackSeverity
        }

        return callStackSeverity
    }

    fun config(config: ConfigResponse) {
        // appenders
        val appenders: AppenderMap = mutableMapOf()
        for (appender in config.appenders) {
            val origAppender = this.appenders[appender.name]
            if (origAppender != null) {
                origAppender.update(appender.config)
                appenders[appender.name] = origAppender
            }
            else {
                AppenderFactory.create(appender.type, appender.name, appender.config)?.let { appenders[appender.name] = it }
            }
        }

        // loggers
        val loggers: MutableList<Logger> = mutableListOf()
        for (logger in config.loggers) {
            appenders[logger.appenderRef]?.let {
                val tempLogger = Logger(logger.name,
                        logger.severity,
                        logger.callStackSeverity,
                        it
                        )
                loggers.add(tempLogger)
            }
        }

        this.appenders = appenders
        this.loggers = loggers

        LocalBroadcastManager.getInstance(SessionManager.appContext!!).sendBroadcast(Intent(BroadcastNames.CONFIG_CHANGE))
    }
}