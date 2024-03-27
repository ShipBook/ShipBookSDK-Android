package io.shipbook.shipbooksdk.Events

import android.content.ComponentCallbacks2
import android.content.res.Configuration
import io.shipbook.shipbooksdk.InnerLog
import io.shipbook.shipbooksdk.LogManager
import io.shipbook.shipbooksdk.Models.ConfigEvent
import io.shipbook.shipbooksdk.Models.Orientation
import io.shipbook.shipbooksdk.Networking.SessionManager


/*
 *
 * Created by Elisha Sterngold on 22/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */


internal object EventManager {
    private val TAG = EventManager::class.java.simpleName
    init {

    }

    fun start() {
        SessionManager.application?.registerActivityLifecycleCallbacks(ActivityEventCallbacks)
        SessionManager.application?.registerComponentCallbacks(ComponentCallbacks())
        InnerLog.i(TAG, "Current configuration: ${SessionManager.appContext?.resources?.configuration}")
    }

    class ComponentCallbacks: ComponentCallbacks2 {
        override fun onLowMemory() {
            InnerLog.w(TAG, "low memory")
        }

        override fun onConfigurationChanged(newConfig: Configuration) {
            InnerLog.i(TAG, "configuration changed $newConfig")

            // Checks the orientation of the screen
            val orientation = when (newConfig.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> Orientation.Landscape
                Configuration.ORIENTATION_PORTRAIT -> Orientation.Portrait
                else -> Orientation.Undefined
            }

            val configEvent = ConfigEvent(orientation)
            InnerLog.v(TAG, "added config event: $configEvent")
            LogManager.push(configEvent)

        }

        override fun onTrimMemory(level: Int) {
            InnerLog.w(TAG, "trim memory on level: $level")
        }
    }
}