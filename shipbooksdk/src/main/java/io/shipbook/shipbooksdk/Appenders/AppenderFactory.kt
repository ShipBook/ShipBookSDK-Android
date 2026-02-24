package io.shipbook.shipbooksdk.Appenders

/*
 *
 * Created by Elisha Sterngold on 13/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal object AppenderFactory {
    private val registry = mutableMapOf<String, (String, Config?) -> BaseAppender>()

    fun register(type: String, creator: (String, Config?) -> BaseAppender) {
        registry[type] = creator
    }

    fun create(type: String, name: String, config: Config?) : BaseAppender? {
        registry[type]?.let { return it(name, config) }
        when (type) {
            "ConsoleAppender" -> return ConsoleAppender(name, config)
            "SBCloudAppender", "SLCloudAppender" -> return SBCloudAppender(name, config)
            else -> return null
        }
    }
}