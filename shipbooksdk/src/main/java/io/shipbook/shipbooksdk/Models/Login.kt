package io.shipbook.shipbooksdk.Models

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.os.Build
import android.provider.Settings
import io.shipbook.shipbooksdk.Networking.SessionManager
import io.shipbook.shipbooksdk.ShipBook
import io.shipbook.shipbooksdk.Util.DateHelper
import io.shipbook.shipbooksdk.Util.toStandardString
import org.json.JSONObject
import java.util.*

/**
*
* Created by Elisha Sterngold on 12/02/2018.
* Copyright © 2018 ShipBook Ltd. All rights reserved.
*
*/

@SuppressLint("HardwareIds")
internal data class Login(
        val appId: String,
        val appKey: String,
        val os: String = "android",
        //    val bundleIdentifier: String
        val appName: String = SessionManager.appContext!!.applicationInfo.processName,
        val udid: String = Settings.Secure.getString(SessionManager.appContext!!.contentResolver, Settings.Secure.ANDROID_ID) ?: "test_device",
        val time: Date = Date(),
        var deviceTime: Date = time,
        val osVersion: String = Build.VERSION.RELEASE ?: "", // "" for testing
        var appVersion: String = "",
        var appVersionCode: Int = -1,
        val sdkVersion: String = "1.7.1",
        val sdkVersionCode: Int = 1,
        val manufacturer: String = Build.MANUFACTURER ?: "", // "" for testing
        val deviceModel: String = Build.MODEL ?: "", // "" for testing
        val deviceName: String = Build.DEVICE ?: "", // "" for testing
        //    var advertisementId: String? = nil
        val language: String = Locale.getDefault().language,
        val isDebug: Boolean = (0 != (SessionManager.appContext!!.applicationInfo.flags.and(ApplicationInfo.FLAG_DEBUGGABLE))),
        val isObfuscated: Boolean = !ShipBook::class.java.name.equals("io.shipbook.shipbooksdk.ShipBook"),
        var user: User? = null

        ): BaseObj {
    init {
        if (appVersion == "") {
            val context = SessionManager.appContext!!
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            appVersion = pInfo.versionName ?: "" // in test it is null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) appVersionCode = pInfo.longVersionCode.toInt()
            else {
                @Suppress("DEPRECATION")
                appVersionCode = pInfo.versionCode
            }

        }
    }

    companion object {
        fun create(json: JSONObject): Login {
            val appId = json.optString("appId")
            val appKey = json.optString("appKey")
            val os = json.optString("os")
            //    val bundleIdentifier: String
            val appName = json.optString("appName")
            val udid = json.optString("udid")
            val time = DateHelper.createDateStandard(json.optString("time"))!!
            val deviceTime = DateHelper.createDateStandard(json.optString("deviceTime"))!!
            val osVersion = json.optString("osVersion")
            val appVersion = json.optString("appVersion")
            val appVersionCode = json.getInt("appVersionCode")
            val sdkVersion = json.optString("sdkVersion")
            val sdkVersionCode = json.getInt("sdkVersionCode")
            val manufacturer = json.optString("manufacturer")
            val deviceModel = json.optString("deviceModel")
            val deviceName = json.optString("deviceName")
            //    var advertisementId: String? = nil
            val language = json.optString("language")
            val isDebug = json.getBoolean("isDebug")
            val isObfuscated = json.getBoolean("isObfuscated")
            val user = if (json.has("user")) User.create(json.optJSONObject("user")) else null
            return Login(appId, appKey, os, appName, udid, time, deviceTime, osVersion, appVersion, appVersionCode, sdkVersion, sdkVersionCode, manufacturer, deviceModel, deviceName, language, isDebug, isObfuscated, user)
        }
    }

    override fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("appId", appId)
        json.put("appKey", appKey)
        json.put("os", os)
        json.put("appName", appName)
        json.put("udid", udid)
        json.put("time", time.toStandardString())
        json.put("deviceTime", deviceTime.toStandardString())
        json.put("osVersion", osVersion)
        json.put("appVersion", appVersion)
        json.put("appVersionCode", appVersionCode)
        json.put("sdkVersion", sdkVersion)
        json.put("sdkVersionCode", sdkVersionCode)
        json.put("manufacturer", manufacturer)
        json.put("deviceModel", deviceModel)
        json.put("deviceName", deviceName)
        json.put("language", language)
        json.put("isDebug", isDebug)
        json.put("isObfuscated", isObfuscated)
        json.putOpt("user", user?.toJson())
        return json
    }
}
