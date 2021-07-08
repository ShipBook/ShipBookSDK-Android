package io.shipbook.shipbooksdk.Models

import org.json.JSONObject

/*
 *
 * Created by Elisha Sterngold on 11/03/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal data class RefreshToken(val token: String, val appKey: String): BaseObj {
    companion object {
        fun create(json: JSONObject): RefreshToken {
            val token = json.optString("token")
            val appKey = json.optString("appKey")
            return RefreshToken(token, appKey)
        }
    }

    override fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("token", token)
        json.put("appKey", appKey)
        return json
    }
}