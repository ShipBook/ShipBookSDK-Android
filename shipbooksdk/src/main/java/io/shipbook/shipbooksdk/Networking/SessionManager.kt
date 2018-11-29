package io.shipbook.shipbooksdk.Networking

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import io.shipbook.shipbooksdk.*
import io.shipbook.shipbooksdk.Events.EventManager
import io.shipbook.shipbooksdk.Models.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.net.URI
import kotlin.coroutines.CoroutineContext


/*
 *
 * Created by Elisha Sterngold on 12/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

@SuppressLint("StaticFieldLeak")
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

    val connected: Boolean
        get() {
            if (token != null) return true
            innerLogin()
            return false
        }

    fun login(application: Application, appId: String, appKey: String, userConfig: URI?) {
        this.application = application
        configFile = File(appContext?.filesDir, "config.json")
        when {
            configFile!!.isFile() -> readConfig(configFile!!)
            userConfig != null -> {
                val config = File(userConfig)
                readConfig(config)
            }
            else -> readConfig(appContext!!.resources.openRawResource(R.raw.config))
        }

        this.appKey = appKey
        login = Login(appId, appKey)
        innerLogin()
    }

    private fun innerLogin() {
        if (isInLoginRequest || login == null) return
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
                    token = loginResponse.token
                    LogManager.config(loginResponse.config)
                    configFile!!.writeText(loginResponse.config.toJson().toString())
                    LocalBroadcastManager.getInstance(appContext).sendBroadcast(Intent(BroadcastNames.CONNECTED))
                }
                catch (e: Throwable) {
                    InnerLog.e(TAG, "There was a problem with the data", e)
                }
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
                     additionalInfo: Map<String, String>) {
        user = User(userId, userName, fullName, email, phoneNumber, additionalInfo)
        if (login != null) LocalBroadcastManager.getInstance(appContext).sendBroadcast(Intent(BroadcastNames.USER_CHANGE))
    }

    fun logout() {
        this.token = null
        this.user = null
    }

    suspend fun refreshToken(): Boolean {
        if (token == null || appKey == null )
            return false

        val refreshToken = RefreshToken(token!!, appKey!!)
        isInLoginRequest = true
        token = null
        val response = ConnectionClient.request("auth/refreshSdkToken", refreshToken.toJson().toString(), HttpMethod.POST)
        try {
            if (!response.ok) return false
            if (response.data == null) {
                InnerLog.e(TAG, "missing data")
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