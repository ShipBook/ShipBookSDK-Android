package io.shipbook.shipbooksdk.Models

import org.json.JSONObject

/*
 *
 * Created by Elisha Sterngold on 13/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal data class LoginResponse(val token: String, val config: ConfigResponse): BaseObj {

    companion object {
        fun create(json: JSONObject): LoginResponse {
            val token = json.getString("token")
            val config = ConfigResponse.create(json.getJSONObject("config"))
            return LoginResponse(token, config)
        }
    }
    override fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("token", token)
        json.put("config", config.toJson())
        return json
    }
}