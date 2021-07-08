package io.shipbook.shipbooksdk.Models

import org.json.JSONObject
import java.util.*

/*
 *
 * Created by Elisha Sterngold on 26/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal enum class Orientation {
    Undefined, Landscape, Portrait
}
internal data class ConfigEvent(val orientation: Orientation,
                            //for base class
                            override var orderId: Int = 0,
                            override val time: Date = Date(),
                            override val threadInfo: ThreadInfo = ThreadInfo()): BaseEvent("configEvent") {
    companion object {
        fun create(json: JSONObject,
                   orderId: Int,
                   time: Date,
                   threadInfo: ThreadInfo): ConfigEvent {
            val orientation = Orientation.valueOf(json.optString("orientation"))
            return ConfigEvent(orientation, orderId, time, threadInfo)
        }
    }

    init {
        orderId = incrementOrderId(orderId)
    }

    override fun toJson(): JSONObject {
        val json = super.toJson()
        json.put("orientation", orientation)
        return json
    }

}