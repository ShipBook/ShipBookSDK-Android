package io.shipbook.shipbooksdk.Models

import org.json.JSONObject
import java.util.*

/*
 *
 * Created by Elisha Sterngold on 27/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal data class FragmentEvent(val name: String,
                                  val event: String,
        //for base class
                                  override var orderId: Int = 0,
                                  override val time: Date = Date(),
                                  override val threadInfo: ThreadInfo = ThreadInfo()) : BaseEvent("fragmentEvent") {

    companion object {
        fun create(json: JSONObject,
                   orderId: Int,
                   time: Date,
                   threadInfo: ThreadInfo): FragmentEvent {
            val name = json.optString("name")
            val event = json.optString("event")
            return FragmentEvent(name, event, orderId, time, threadInfo)
        }
    }
    init {
        orderId = incrementOrderId(orderId)
    }

    override fun toJson(): JSONObject {
        val json = super.toJson()
        json.put("name", name)
        json.put("event", event)
        return json
    }
}