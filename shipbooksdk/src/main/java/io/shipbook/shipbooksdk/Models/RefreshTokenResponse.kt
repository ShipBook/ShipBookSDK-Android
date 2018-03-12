package io.shipbook.shipbooksdk.Models

import org.json.JSONObject

/*
 *
 * Created by Elisha Sterngold on 11/03/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal data class RefreshTokenResponse(val token: String): BaseObj {
    companion object {
        fun create(json: JSONObject): RefreshTokenResponse {
            val token = json.getString("token")
            return RefreshTokenResponse(token)
        }
    }

    override fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("token", token)
        return json
    }
}