package io.shipbook.shipbooksdk.Models

import io.shipbook.shipbooksdk.Util.toJson
import io.shipbook.shipbooksdk.Util.toListStackTraceElement
import org.json.JSONObject
import java.util.*

/*
 *
 * Created by Elisha Sterngold on 04/03/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */
internal data class Exception (val name: String?,
                               val reason: String?,
                               val stackTrace: List<StackTraceElement>,
                                    //for base class
                               override var orderId: Int = 0,
                               override val time: Date = Date(),
                               override val threadInfo: BaseLog.ThreadInfo = BaseLog.ThreadInfo()) : BaseLog("exception") {

    companion object {
        fun create(json: JSONObject,
                   orderId: Int,
                   time: Date,
                   threadInfo: BaseLog.ThreadInfo): Exception {
            val name = json.optString("name")
            val reason = json.optString("reason")
            val stackTrace = json.getJSONArray("stackTrace").toListStackTraceElement()
            return Exception(name, reason, stackTrace, orderId, time, threadInfo)
        }
    }
    init {
        orderId = incrementOrderId(orderId)
    }

    override fun toJson(): JSONObject {
        val json = super.toJson()
        json.putOpt("name", name)
        json.putOpt("reason", reason)
        json.put("stackTrace", stackTrace.toJson())
        return json
    }
}