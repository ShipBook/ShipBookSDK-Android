package io.shipbook.shipbooksdk.Models

import org.json.JSONObject

/*
 *
 * Created by Elisha Sterngold on 13/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal data class LoginResponse(val token: String, val config: ConfigResponse, val sessionUrl: String, val sessionLink: String? = null): BaseObj {

    companion object {
        fun create(json: JSONObject): LoginResponse {
            val token = json.optString("token")
            val config = ConfigResponse.create(json.getJSONObject("config"))
            val sessionUrl = json.optString("sessionUrl")
            val sessionLink = if (json.isNull("sessionLink")) null else json.getString("sessionLink") // absent when the device is silenced
            return LoginResponse(token, config, sessionUrl, sessionLink)
        }
    }
    override fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("token", token)
        json.put("config", config.toJson())
        json.put("sessionUrl", sessionUrl)
        json.put("sessionLink", sessionLink)
        return json
    }
}