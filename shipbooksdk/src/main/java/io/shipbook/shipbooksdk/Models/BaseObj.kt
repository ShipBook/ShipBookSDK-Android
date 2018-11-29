package io.shipbook.shipbooksdk.Models

import org.json.JSONObject

/*
 *
 * Created by Elisha Sterngold on 19/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal interface BaseObj {
    fun toJson(): JSONObject
}
