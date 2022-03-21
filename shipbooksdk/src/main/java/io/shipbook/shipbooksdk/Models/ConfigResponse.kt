package io.shipbook.shipbooksdk.Models

import io.shipbook.shipbooksdk.Appenders.Config
import org.json.JSONArray
import org.json.JSONObject

/*
 *
 * Created by Elisha Sterngold on 13/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal data class ConfigResponse(val appenders: List<AppenderResponse>,
                                   val loggers: List<LoggerResponse>,
                                   val eventLoggingDisabled: Boolean = false,
                                   val exceptionReportDisabled: Boolean = false): BaseObj {
    companion object {
        fun create(json: JSONObject): ConfigResponse {
            val appendersArray = json.getJSONArray("appenders")
            val appenders = ArrayList<AppenderResponse>(appendersArray.length())
            for (i in 0 until appendersArray.length()) {
                val appenderResponse = AppenderResponse.create(appendersArray.getJSONObject(i))
                appenders.add(appenderResponse)
            }

            val loggersArray = json.getJSONArray("loggers")
            val loggers = ArrayList<LoggerResponse>(loggersArray.length())
            for (i in 0 until loggersArray.length()) {
                val loggersResponse = LoggerResponse.create(loggersArray.getJSONObject(i))
                loggers.add(loggersResponse)
            }
            val eventLoggingDisabled = json.optBoolean("eventLoggingDisabled")
            val exceptionReportDisabled = json.optBoolean("exceptionReportDisabled")
            return ConfigResponse(appenders, loggers, eventLoggingDisabled, exceptionReportDisabled)
        }
    }

    override fun toJson(): JSONObject {
        val json = JSONObject()
        val appendersArray = JSONArray()
        appenders.forEach {
            appendersArray.put(it.toJson())
        }
        json.put("appenders", appendersArray)

        val loggersArray = JSONArray()
        loggers.forEach {
            loggersArray.put(it.toJson())
        }
        json.put("loggers", loggersArray)

        json.put("eventLoggingDisabled", eventLoggingDisabled)
        json.put("exceptionReportDisabled", exceptionReportDisabled)
        return json
    }

    data class AppenderResponse(val type: String, val name: String, val config: Config? = null): BaseObj {
        companion object {
            fun create(json: JSONObject): AppenderResponse {
                val type: String = json.optString("type")
                val name: String = json.optString("name")


                var config: MutableMap<String, Any>? = null
                if (json.has("config")) {
                    config = mutableMapOf()
                    val configObject = json.getJSONObject("config")
                    configObject.keys().forEach {
                        config.set(it, configObject.get(it))
                    }
                }

                return AppenderResponse(type, name, config)
            }
        }
        override fun toJson(): JSONObject {
            val json = JSONObject()
            json.put("type", type)
            json.put("name", name)
            config?.let { config ->
                val configObject = JSONObject()
                config.entries.forEach {
                    configObject.put(it.key, it.value)
                }
                json.put("config", configObject)
            }
            return json
        }
    }

    data class LoggerResponse(val name: String, val severity: Severity, val callStackSeverity: Severity = Severity.Off, val appenderRef: String): BaseObj {
        companion object {
            fun create(json: JSONObject): LoggerResponse {
                val name = json.optString("name")
                val severity = Severity.valueOf(json.optString("severity", Severity.Verbose.name))
                val callStackSeverity = if (json.has("callStackSeverity")) Severity.valueOf(json.optString("callStackSeverity", Severity.Verbose.name)) else Severity.Off
                val appenderRef = json.optString("appenderRef")
                return LoggerResponse(name, severity, callStackSeverity, appenderRef)
            }
        }
        override fun toJson(): JSONObject {
            val json = JSONObject()
            json.put("name", name)
            json.put("severity", severity.toString())
            json.put("callStackSeverity", callStackSeverity.toString())
            json.put("appenderRef", appenderRef)
            return json
        }
    }
}