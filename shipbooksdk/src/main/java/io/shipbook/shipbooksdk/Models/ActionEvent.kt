package io.shipbook.shipbooksdk.Models

import org.json.JSONObject
import java.util.*

/*
 *
 * Created by Elisha Sterngold on 28/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */
internal data class ActionEvent(val action: String,
                                val sender: String,
                                val senderTitle: String,
                                val target: String,
                                //for base class
                                override var orderId: Int = 0,
                                override val time: Date = Date(),
                                override val threadInfo: ThreadInfo = ThreadInfo()) : BaseEvent("actionEvent") {

    companion object {
        fun create(json: JSONObject,
                   orderId: Int,
                   time: Date,
                   threadInfo: ThreadInfo): ActionEvent {
            val action = json.optString("action")
            val sender = json.optString("sender")
            val senderTitle = json.optString("senderTitle")
            val target = json.optString("target")
            return ActionEvent(action, sender, senderTitle, target, orderId, time, threadInfo)
        }
    }
    init {
        orderId = incrementOrderId(orderId)
    }

    override fun toJson(): JSONObject {
        val json = super.toJson()
        json.put("action", action)
        json.put("sender", sender)
        json.put("senderTitle", senderTitle)
        json.put("target", target)
        return json
    }
}