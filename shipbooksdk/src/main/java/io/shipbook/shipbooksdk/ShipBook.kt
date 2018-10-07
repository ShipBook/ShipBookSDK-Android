package io.shipbook.shipbooksdk

import android.app.Application
import io.shipbook.shipbooksdk.Models.ScreenEvent
import io.shipbook.shipbooksdk.Networking.ConnectionClient

import io.shipbook.shipbooksdk.Networking.SessionManager
import java.net.URI


/*
 *
 * Created by Elisha Sterngold on 25/01/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

class ShipBook {
    companion object {
        @JvmStatic
        @JvmOverloads
        fun start(application: Application, appId: String, appKey: String, uri: URI? = null) {
            SessionManager.login(application, appId, appKey, uri)
        }

        @JvmStatic
        fun enableInnerLog(enable: Boolean) {
            InnerLog.enabled = enable
        }

        @JvmStatic
        fun setConnectionUrl(url: String) {
            ConnectionClient.baseUrl = url
        }

        @JvmStatic
        fun getLogger(tag: String): Log {
            return Log(tag)
        }

        @JvmStatic
        @JvmOverloads
        fun registerUser(userId: String,
                         userName: String? = null,
                         fullName: String? = null,
                         email: String? = null,
                         phoneNumber: String? = null,
                         additionalInfo: Map<String, String> = mutableMapOf()) {
            SessionManager.registerUser(userId, userName, fullName, email, phoneNumber, additionalInfo)
        }

        @JvmStatic
        fun logout() {
            SessionManager.logout()
        }

        @JvmStatic
        fun screen(name: String) {
            val event = ScreenEvent(name)
            LogManager.push(event)
        }
    }
}
