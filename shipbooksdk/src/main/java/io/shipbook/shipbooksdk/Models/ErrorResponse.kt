package io.shipbook.shipbooksdk.Models

import org.json.JSONObject

/*
 *
 * Created by Elisha Sterngold on 13/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */


internal data class ErrorResponse (val name: String, val message: String, val status: Int?) {
    companion object {
        fun create(json: JSONObject): ErrorResponse {
            val name = json.getString("name")
            val message = json.getString("message")
            val status = json.optInt("status")
            return ErrorResponse(name, message, status)
        }
    }
}