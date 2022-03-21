package io.shipbook.shipbooksdk.Appenders

import io.shipbook.shipbooksdk.Models.BaseLog

/*
 *
 * Created by Elisha Sterngold on 13/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal typealias Config = Map<String, Any>
internal abstract class BaseAppender(val name: String, val config: Config?) {
    abstract fun update(config: Config?)
    abstract fun push(log: BaseLog)
}