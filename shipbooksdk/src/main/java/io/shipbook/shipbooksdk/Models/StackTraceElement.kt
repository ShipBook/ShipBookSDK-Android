package io.shipbook.shipbooksdk.Models

import org.json.JSONObject

/*
 *
 * Created by Elisha Sterngold on 04/03/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal data class StackTraceElement (val declaringClass: String,
                                       val methodName: String,
                                       val fileName: String?,
                                       val lineNumber: Int): BaseObj {
    companion object {
        fun create(json: JSONObject): StackTraceElement {
            val declaringClass = json.getString("declaringClass")
            val methodName = json.getString("methodName")
            val fileName = json.optString("fileName")
            val lineNumber = json.getInt("lineNumber")
            return StackTraceElement(declaringClass, methodName, fileName, lineNumber)
        }
    }

    override fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("declaringClass", declaringClass)
        json.put("methodName", methodName)
        json.putOpt("fileName", fileName)
        json.put("lineNumber", lineNumber)
        return json
    }
}