package io.shipbook.shipbooksdk

import android.app.Application
import io.shipbook.shipbooksdk.Events.ActionEventManager
import io.shipbook.shipbooksdk.Models.Message
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

/**
 * The main class of the Shipbook SDK - A remote logging platform
 */
class ShipBook {
    companion object {
        /**
         * Starts the shipbook SDK should be called in `application(_:didFinishLaunchingWithOptions:)`
         * @param appId The app id. You get it from https://console.shipbook.io.
         * @param appKey The app key. You get it from https://console.shipbook.io.
         * @param completion a completion of start callback so that you'll be able to get the sessionUrl if needed for 3rd party integration
         * @param uri The url of the server. By default it goes to the Shipboook production server.
         */
        @JvmStatic
        @JvmOverloads
        fun start(application: Application, appId: String, appKey: String, completion: ((String)->Unit)? = null, uri: URI? = null) {
            SessionManager.login(application, appId, appKey, completion, uri)
        }

        /**
         * Open the inner log. This function is for in the case that the SDK doesn't work and you need to understand why.
         * @param enable if the innerlog should be enabled
         */
        @JvmStatic
        fun enableInnerLog(enable: Boolean) {
            InnerLog.enabled = enable
        }

        /**
         * Change the url of the server.
         * @param url: The url of the server. By default it goes to the Shipboook production server.
         */
        @JvmStatic
        fun setConnectionUrl(url: String) {
            ConnectionClient.baseUrl = url
        }

        /**
         * Create a Log class
         * @param tag The tag that the log class will use.
         */
        @JvmStatic
        fun getLogger(tag: String): Log {
            return Log(tag)
        }

        /**
         * Register the user. This is to connect the user to this session.
         *
         * The best practice is to set registerUser before ShipBook.start. It will also work after this point however, it will require an additional api request.
         *
         * Be sure that you have concent from the user to save this information. In any case you can save only the userId and like
         * this will be able to find in the console the logs for this user
         *
         * @param userId The user id in your app/system.
         * @param userName The user name in you system. This is the parameter that the user logs in to you app/system.
         * @param fullName The full name of the user.
         * @param email The email of the user.
         * @param phoneNumber The phone number of the user.
         * @param additionalInfo Additional info of the user.
         *
         */
        @JvmStatic
        @JvmOverloads
        fun registerUser(userId: String,
                         userName: String? = null,
                         fullName: String? = null,
                         email: String? = null,
                         phoneNumber: String? = null,
                         additionalInfo: Map<String, String>? = null) {
            SessionManager.registerUser(userId, userName, fullName, email, phoneNumber, additionalInfo)
        }

        /**
         * Logout the user. This will create after it a new session where the user isn't connected to it.
         */
        @JvmStatic
        fun logout() {
            SessionManager.logout()
        }

        /**
         * Ignore Views
         *
         * These views will be ignored for action events. This does that if there is private information in a view it won't log the information.
         *
         * @param ids The ids of the screens to ignore.
         */
        @JvmStatic
        fun ignoreViews(vararg ids: Int) {
            ActionEventManager.ignoreViews = ids.toSet()
        }

        /**
         * Entered in a new screen.
         *
         * This will help you connect the logs to wich screen is open.
         * The best practice is to add this code to viewWillAppear in the view controller.
         *
         * @param name The name of the new screen.
         */

        @JvmStatic
        fun screen(name: String) {
            val event = ScreenEvent(name)
            LogManager.push(event)
        }

        /**
         * Adding wrapper class
         *
         * In the case that you have a wrapper class and you want that the logs will not right there
         * class and line number
         *
         * @param name The name of the wrapper class.
         */

        @JvmStatic
        fun addWrapperClass(name: String) {
            Message.addIgnoreClass(name)
        }

    }
}
