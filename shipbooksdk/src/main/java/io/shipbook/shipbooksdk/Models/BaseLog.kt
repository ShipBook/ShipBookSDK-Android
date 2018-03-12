package io.shipbook.shipbooksdk.Models

import android.os.Looper
import io.shipbook.shipbooksdk.Util.DateHelper
import io.shipbook.shipbooksdk.Util.toStandardString
import org.json.JSONObject
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
*
* Created by Elisha Sterngold on 11/02/2018.
* Copyright © 2018 ShipBook Ltd. All rights reserved.
*
*/


internal abstract class BaseLog(val type: String,
                                open var orderId: Int = 0,
                                open val time: Date = Date(),
                                open val threadInfo: ThreadInfo = ThreadInfo()): BaseObj {
    companion object {
        var count = AtomicInteger(0)

        fun create(json: JSONObject): BaseLog {
            val type = json.getString("type")
            val orderId = json.getInt("orderId")
            val time = DateHelper.createDateStandard(json.getString("time"))!!
            val threadInfo = ThreadInfo.create(json.getJSONObject("threadInfo"))
            return when (type) {
                "message" -> Message.create(json, orderId, time, threadInfo)
                "activityEvent" -> ActivityEvent.create(json, orderId, time, threadInfo)
                "fragmentEvent" -> FragmentEvent.create(json, orderId, time, threadInfo)
                "configEvent" -> ConfigEvent.create(json, orderId, time, threadInfo)
                "actionEvent" -> ActionEvent.create(json, orderId, time, threadInfo)
                "exception" -> Exception.create(json, orderId, time, threadInfo)
                else -> throw Error("There doesn't exist this type")
            }
        }
    }

    init {
    }

    fun incrementOrderId(orderId: Int): Int {
        if (orderId == 0) { // not from serialized
            return count.incrementAndGet()
        }
        return orderId
    }

    override fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("type", type)
        json.put("orderId", orderId)
        json.put("time", time.toStandardString())
        json.put("threadInfo", threadInfo.toJson())
        return json
    }


    data class ThreadInfo(var threadName: String = Thread.currentThread().name,
                          var threadId: Long = Thread.currentThread().id,
                          var isMain: Boolean = Looper.myLooper() == Looper.getMainLooper() ): BaseObj {

        companion object {
            fun create(json: JSONObject): ThreadInfo {
                val threadName = json.getString("threadName")
                val threadId = json.getLong("threadId")
                val isMain = json.getBoolean("isMain")
                return  ThreadInfo(threadName, threadId, isMain)
            }
        }

        override fun toJson(): JSONObject {
            val json = JSONObject()
            json.put("threadName", threadName)
            json.put("threadId", threadId)
            json.put("isMain", isMain)
            return json
        }
    }
}