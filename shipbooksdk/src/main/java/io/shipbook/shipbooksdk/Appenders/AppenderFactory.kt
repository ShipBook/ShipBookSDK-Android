package io.shipbook.shipbooksdk.Appenders

/*
 *
 * Created by Elisha Sterngold on 13/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal object AppenderFactory {
    fun create(type: String, name: String, config: Config?) : BaseAppender? {
        when (type) {
            "ConsoleAppender" -> return ConsoleAppender(name, config)
            "SBCloudAppender" -> return SBCloudAppender(name, config)
            else -> return null
        }
    }
}