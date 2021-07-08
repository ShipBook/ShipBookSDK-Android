package io.shipbook.shipbooksdk.Models

import org.json.JSONObject

/*
 *
 * Created by Elisha Sterngold on 13/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal data class LoginResponse(val token: String, val config: ConfigResponse, val sessionUrl: String): BaseObj {

    companion object {
        fun create(json: JSONObject): LoginResponse {
            val token = json.optString("token")
            val config = ConfigResponse.create(json.getJSONObject("config"))
            val sessionUrl = json.optString("sessionUrl")
            return LoginResponse(token, config, sessionUrl)
        }
    }
    override fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("token", token)
        json.put("config", config.toJson())
        json.put("sessionUrl", sessionUrl)
        return json
    }
}