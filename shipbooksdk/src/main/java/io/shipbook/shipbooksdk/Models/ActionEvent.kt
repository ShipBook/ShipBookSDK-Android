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
            val action = json.getString("action")
            val sender = json.getString("sender")
            val senderTitle = json.getString("senderTitle")
            val target = json.getString("target")
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