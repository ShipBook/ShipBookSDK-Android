package io.shipbook.shipbooksdk.Models

import org.json.JSONArray
import org.json.JSONObject


/*
 *
 * Created by Elisha Sterngold on 14/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal data class SessionLogData(var token: String? = null,
                                   var login: Login? = null,
                                   var user: User? = null,
                                   var logs: MutableList<BaseLog> = mutableListOf()): BaseObj {
    companion object {
        fun create(json: JSONObject): SessionLogData {
            val token = json.optString("token")
            val login = if (json.has("login")) Login.create(json.getJSONObject("login")) else null
            val user = if (json.has("user")) User.create(json.getJSONObject("user")) else null
            val logs: MutableList<BaseLog> = mutableListOf()
            val logsArray = json.getJSONArray("logs")
            for (i in 0..(logsArray.length() - 1)) {
                val log = BaseLog.create(logsArray.getJSONObject(i))
                logs.add(log)
            }
            return SessionLogData(token, login, user, logs)
        }
    }

    override fun toJson(): JSONObject {
        val json = JSONObject()
        json.putOpt("token", token)
        login?.let { json.put("login", it.toJson())}
        user?.let { json.put("user", it.toJson()) }
        val logsArray = JSONArray()
        logs.forEach { logsArray.put(it.toJson()) }
        json.put("logs", logsArray)
        return json
    }
}