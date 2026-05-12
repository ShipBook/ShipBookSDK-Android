package io.shipbook.shipbooksdk.Networking

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import io.shipbook.shipbooksdk.*
import io.shipbook.shipbooksdk.Events.EventManager
import io.shipbook.shipbooksdk.Models.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.net.URI


/*
 *
 * Created by Elisha Sterngold on 12/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

@SuppressLint("StaticFieldLeak")
@OptIn(DelicateCoroutinesApi::class)
internal object SessionManager {
    val threadContext = newSingleThreadContext("shipbook") // need this so that there is no problem of threading
                                                                 // kotlin will redo this implementation in the future and then we will need to change
                                                                 // the api. It might be that they will use the same api and then no changes will needed
                                                                 // see https://github.com/Kotlin/kotlinx.coroutines/issues/261

    private val TAG = SessionManager::class.java.simpleName
    var application: Application? = null
    val appContext: Context?
        get() = application?.applicationContext
    @Volatile
    var token: String? = null

    var sessionCompletion: ((String)->Unit)? = null

    private var appKey: String? = null
    private var _login: Login? = null
    var login: Login?
        get() {
            user?.let { _login?.user = user } // adding user to the login
            return _login
        }
        set(login) {
            _login = login
        }

    var user: User? = null
    var configFile: File? = null

    @Volatile
    var isInLoginRequest = false

    // Set when loginSdk got a 4xx — integration is wrong (bad appId/appKey, app deleted, etc.). Retrying won't fix it; suppress further attempts in this process so we don't hammer the server.
    @Volatile
    private var loginFailedPermanently = false

    val connected: Boolean
        get() {
            if (token != null) return true
            if (loginFailedPermanently) return false
            innerLogin()
            return false
        }

    fun login(application: Application, appId: String, appKey: String, completion: ((String)->Unit)?, userConfig: URI?) {
        try {
            this.application = application
            configFile = File(appContext?.filesDir, "config.json")
            when {
                configFile!!.isFile() && configFile!!.length() > 0 -> readConfig(configFile!!)
                userConfig != null -> {
                    val config = File(userConfig)
                    readConfig(config)
                }
                else -> readConfig(appContext!!.resources.openRawResource(R.raw.config))
            }

            this.appKey = appKey
            this.sessionCompletion = completion
            login = Login(appId, appKey)
            loginFailedPermanently = false
            innerLogin()
        }
        catch (t: Throwable) {
            InnerLog.e(TAG, "login file failed", t)
        }

    }

    private fun innerLogin() {
        if (isInLoginRequest || login == null || loginFailedPermanently) return
        isInLoginRequest = true
        token = null
        GlobalScope.launch(threadContext) {
            InnerLog.d(TAG, "current thread: ${Thread.currentThread().name}")
            val response = ConnectionClient.request("auth/loginSdk", login?.toJson().toString(), HttpMethod.POST)
            isInLoginRequest = false
            if (response.ok) {
                try {
                    if (response.data == null) throw Exception("No Data error")
                    val loginResponse = LoginResponse.create(response.data)
                    if (loginResponse.token.isEmpty()) { // this can happen if the account past the limits of logs
                        InnerLog.w(TAG, "Empty token: This can happen if the account past the limits of logs")
                        return@launch
                    };
                    token = loginResponse.token
                    sessionCompletion?.invoke(loginResponse.sessionUrl)
                    LogManager.config(loginResponse.config)
                    configFile!!.writeText(loginResponse.config.toJson().toString())
                    InternalEventBus.emitSessionEvent(SessionEvent.Connected)
                }
                catch (e: Throwable) {
                    InnerLog.e(TAG, "There was a problem with the data", e)
                }
            }
            else if (response.statusCode in 400..499) {
                // 4xx on loginSdk = integration error (bad appId/appKey, app deleted). Retrying won't help — stop until the app is restarted or logout/login is called.
                loginFailedPermanently = true
                InnerLog.e(TAG, "loginSdk rejected with ${response.statusCode} — check appId/appKey. Will not retry until next app start.")
            }
            else {
                InnerLog.e(TAG, "The response not ok")
            }
        }

    }

    private fun readConfig(input: InputStream) {
        val configString = input.bufferedReader().use { it.readText() }
        readConfig(configString)
    }
    private fun readConfig(file: File) {
        val configString = file.readText()
        readConfig(configString)
    }
    private fun readConfig(configString: String) {
        val config = ConfigResponse.create(JSONObject(configString))

        if (!config.exceptionReportDisabled) ExceptionManager.start()
        if (!config.eventLoggingDisabled) EventManager.start()

        LogManager.config(config)
    }

    fun registerUser(userId: String,
                     userName: String?,
                     fullName: String?,
                     email: String?,
                     phoneNumber: String?,
                     additionalInfo: Map<String, String>?) {
        user = User(userId, userName, fullName, email, phoneNumber, additionalInfo)
        if (login != null) GlobalScope.launch(threadContext) { InternalEventBus.emitSessionEvent(SessionEvent.UserChange) }
    }

    fun logout() {
        token = null
        user = null
        if (login != null) login = Login(login!!.appId, login!!.appKey)
        innerLogin()
    }

    suspend fun refreshToken(): Boolean {
        if (token == null || appKey == null )
            return false

        val refreshToken = RefreshToken(token!!, appKey!!)
        isInLoginRequest = true
        token = null
        val response = ConnectionClient.request("auth/refreshSdkToken", refreshToken.toJson().toString(), HttpMethod.POST)
        try {
            if (!response.ok) {
                if (response.statusCode in 400..499) {
                    // 4xx on refresh = integration error (app deleted, wrong appKey, bad JWT). Same as loginSdk failure — stop retrying until next app start instead of falling through to a fresh loginSdk that would 4xx too.
                    loginFailedPermanently = true
                    InnerLog.e(TAG, "refreshSdkToken rejected with ${response.statusCode} — check appId/appKey. Will not retry until next app start.")
                }
                return false
            }
            if (response.data == null) {
                InnerLog.e(TAG, "missing data on refresh — leaving token null so the next cycle falls back to a fresh loginSdk")
                return false
            }

            val refresh = RefreshTokenResponse.create(response.data)
            token = refresh.token
            return true
        }
        catch (e: Throwable) {
            InnerLog.e(TAG, "There was a problem with the data", e)
            return false
        }
        finally {
            isInLoginRequest = false
        }
    }
}