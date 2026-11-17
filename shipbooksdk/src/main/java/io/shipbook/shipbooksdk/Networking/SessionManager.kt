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
import io.shipbook.shipbooksdk.Util.toStandardString
import java.util.Date


/*
 *
 * Created by Elisha Sterngold on 12/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

@SuppressLint("StaticFieldLeak")
@OptIn(DelicateCoroutinesApi::class)
internal object SessionManager {
    private const val SESSION_LINK_HEADER = "x-shipbook-session"
    private const val CONFIG_KEY = "config"
    private const val PAUSE_KEY = "pause"

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
    @Volatile
    var sessionLink: String? = null
    @Volatile
    var sessionUrl: String? = null

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

    @Volatile
    var isInLoginRequest = false

    // Set when loginSdk got a 4xx — integration is wrong (bad appId/appKey, app deleted, etc.). Retrying won't fix it; suppress further attempts in this process so we don't hammer the server.
    @Volatile
    private var loginFailedPermanently = false

    @Volatile
    var pausedUntil = 0L
    val paused: Boolean
        get() = System.currentTimeMillis() < pausedUntil

    // Unlike a pause it is never saved and drops nothing: the server is busy, it isn't refusing this app.
    @Volatile
    var backOffUntil = 0L
    val backingOff: Boolean
        get() = System.currentTimeMillis() < backOffUntil

    val requestsBlocked: Boolean
        get() = paused || backingOff

    val connected: Boolean
        get() {
            if (requestsBlocked) return false
            if (token != null) return true
            if (loginFailedPermanently) return false
            innerLogin()
            return false
        }

    fun login(application: Application, appId: String, appKey: String, completion: ((String)->Unit)?) {
        if (login?.appId == appId && this.appKey == appKey) {
            if (completion == null) return
            // The login answer takes the same lock, so each completion gets a session URL exactly once.
            val knownSessionUrl = synchronized(this) {
                sessionCompletion = completion
                sessionUrl
            }
            knownSessionUrl?.let { completion(it) }
            return
        }

        this.application = application
        this.appKey = appKey
        this.sessionCompletion = completion
        login = Login(appId, appKey)
        loginFailedPermanently = false
        // The login below is held back while paused or backing off; the previous app's session must not outlive it.
        token = null
        sessionLink = null
        sessionUrl = null
        val storedPause = StorageManager.getString(PAUSE_KEY)
        val pause = storedPause?.let { Pause.load(it, appId, BuildConfig.VERSION_NAME, System.currentTimeMillis()) }
        if (storedPause != null && pause == null) StorageManager.remove(PAUSE_KEY)
        pausedUntil = pause?.until ?: 0
        if (pause != null) InnerLog.w(TAG, "paused until ${Date(pause.until).toStandardString()} (from an earlier run); nothing will be sent")

        // Config loading is isolated from login setup so a bad config can never leave the SDK without a Login and unable to ever connect.
        var loadedCachedConfig = false
        val cachedConfig = StorageManager.getString(CONFIG_KEY)
        if (cachedConfig != null) {
            try {
                readConfig(cachedConfig)
                loadedCachedConfig = true
            }
            catch (t: Throwable) {
                // A config saved by another SDK version may no longer parse; treat it as absent instead of failing every start until app data is cleared.
                InnerLog.w(TAG, "cached config is corrupt, deleting it and falling back to the default config", t)
                StorageManager.remove(CONFIG_KEY)
            }
        }
        // The bundled config ships with this SDK; failing to read it is a bug to surface, not a state to handle.
        if (!loadedCachedConfig) readConfig(appContext!!.resources.openRawResource(R.raw.config).bufferedReader().use { it.readText() })

        innerLogin()
    }

    private fun innerLogin() {
        if (isInLoginRequest || login == null || loginFailedPermanently || requestsBlocked) return
        isInLoginRequest = true
        token = null
        sessionLink = null
        sessionUrl = null
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
                    sessionLink = loginResponse.sessionLink
                    val completion = synchronized(SessionManager) {
                        sessionUrl = loginResponse.sessionUrl
                        sessionCompletion
                    }
                    completion?.invoke(loginResponse.sessionUrl)
                    LogManager.config(loginResponse.config)
                    StorageManager.setString(CONFIG_KEY, loginResponse.config.toJson().toString())
                    InternalEventBus.emitSessionEvent(SessionEvent.Connected)
                }
                catch (e: Throwable) {
                    InnerLog.e(TAG, "There was a problem with the data", e)
                }
            }
            else if (response.statusCode in 400..499 && !requestsBlocked) {
                // 4xx on loginSdk = integration error (bad appId/appKey, app deleted). Retrying won't help — stop until the app is restarted or started with other credentials.
                loginFailedPermanently = true
                InnerLog.e(TAG, "loginSdk rejected with ${response.statusCode} — check appId/appKey. Will not retry until next app start.")
            }
            else {
                InnerLog.e(TAG, "The response not ok")
            }
        }

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
                if (response.statusCode in 400..499 && !requestsBlocked) {
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

    fun pause(appId: String, statusCode: Int, seconds: Long) {
        // The answer is for the app that sent the request; a start() with another appId may have replaced it since.
        if (appId != login?.appId) return
        val pause = Pause(appId, BuildConfig.VERSION_NAME, System.currentTimeMillis() + seconds * 1000)
        InnerLog.w(TAG, "paused until ${Date(pause.until).toStandardString()}: the server answered $statusCode with x-shipbook-pause $seconds")
        pausedUntil = pause.until
        token = null
        sessionLink = null
        sessionUrl = null
        StorageManager.setString(PAUSE_KEY, pause.toJson().toString())
    }

    fun backOff(statusCode: Int, seconds: Long) {
        backOffUntil = System.currentTimeMillis() + seconds * 1000
        InnerLog.w(TAG, "backing off until ${Date(backOffUntil).toStandardString()}: the server answered $statusCode")
    }

    // Empty until login completes and on silenced devices, so callers can always add it.
    fun getSessionHeaders(): Map<String, String> {
        val sessionLink = sessionLink ?: return emptyMap()
        return mapOf(SESSION_LINK_HEADER to sessionLink)
    }
}
