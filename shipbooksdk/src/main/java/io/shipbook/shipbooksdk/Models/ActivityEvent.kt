package io.shipbook.shipbooksdk.Models

import org.json.JSONObject
import java.util.*

/*
 *
 * Created by Elisha Sterngold on 25/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */
internal data class ActivityEvent(val name: String,
                                  val event: String,
                                  val title: String,
                                  //for base class
                                  override var orderId: Int = 0,
                                  override val time: Date = Date(),
                                  override val threadInfo: ThreadInfo = ThreadInfo()) : BaseEvent("activityEvent") {

    companion object {
        fun create(json: JSONObject,
                   orderId: Int,
                   time: Date,
                   threadInfo: ThreadInfo): ActivityEvent {
            val name = json.getString("name")
            val event = json.getString("event")
            val title = json.getString("title")
            return ActivityEvent(name, event, title, orderId, time, threadInfo)
        }
    }
    init {
        orderId = incrementOrderId(orderId)
    }

    override fun toJson(): JSONObject {
        val json = super.toJson()
        json.put("name", name)
        json.put("event", event)
        json.put("title", title)
        return json
    }
}