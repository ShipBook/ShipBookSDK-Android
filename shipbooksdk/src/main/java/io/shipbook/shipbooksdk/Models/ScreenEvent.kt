package io.shipbook.shipbooksdk.Models

import org.json.JSONObject
import java.util.*

/*
 *
 * Created by Elisha Sterngold on 07/10/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */
internal data class ScreenEvent(val name: String,
                                //for base class
                                override var orderId: Int = 0,
                                override val time: Date = Date(),
                                override val threadInfo: ThreadInfo = ThreadInfo()) : BaseEvent("screenEvent") {

    companion object {
        fun create(json: JSONObject,
                   orderId: Int,
                   time: Date,
                   threadInfo: ThreadInfo): ScreenEvent {
            val name = json.getString("name")
            return ScreenEvent(name, orderId, time, threadInfo)
        }
    }
    init {
        orderId = incrementOrderId(orderId)
    }

    override fun toJson(): JSONObject {
        val json = super.toJson()
        json.put("name", name)
        return json
    }
}