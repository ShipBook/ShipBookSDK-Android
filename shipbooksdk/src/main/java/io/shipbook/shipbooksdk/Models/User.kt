package io.shipbook.shipbooksdk.Models

import org.json.JSONObject

/*
 *
 * Created by Elisha Sterngold on 14/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal data class User(val userId:String,
                         val userName: String? = null,
                         val fullName: String? = null,
                         val email: String? = null,
                         val phoneNumber: String? = null,
                         val additionalInfo: Map<String,String>? = null): BaseObj {

    companion object {
        fun create(json: JSONObject) : User {
            val userId = json.optString("userId")
            val userName = json.optString("userName", null)
            val fullName = json.optString("fullName", null)
            val email = json.optString("email", null)
            val phoneNumber = json.optString("phoneNumber", null)
            var additionalInfo: MutableMap<String, String>? =  null
            val additionalInfoObject = json.optJSONObject("additionalInfo")
            if (additionalInfoObject != null) {
                additionalInfo = mutableMapOf()
                additionalInfoObject.keys().forEach {
                    additionalInfo.set(it, additionalInfoObject.optString(it))
                }
            }
            return User(userId, userName, fullName, email, phoneNumber, additionalInfo)
        }
    }

    override fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("userId", userId)
        json.putOpt("userName", userName)
        json.putOpt("fullName", fullName)
        json.putOpt("email", email)
        json.putOpt("phoneNumber", phoneNumber)
        if (additionalInfo != null) {
            val additionalInfoObject = JSONObject()
            additionalInfo.entries.forEach {
                additionalInfoObject.put(it.key, it.value)
            }
            json.putOpt("additionalInfo", additionalInfoObject)
        }

        return json
    }
}