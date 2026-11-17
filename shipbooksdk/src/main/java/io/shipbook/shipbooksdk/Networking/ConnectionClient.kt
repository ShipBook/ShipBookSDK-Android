package io.shipbook.shipbooksdk.Networking

import io.shipbook.shipbooksdk.BuildConfig
import io.shipbook.shipbooksdk.InnerLog
import io.shipbook.shipbooksdk.Models.BaseObj
import io.shipbook.shipbooksdk.Models.ErrorResponse
import io.shipbook.shipbooksdk.Models.Pause
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/*
 *
 * Created by Elisha Sterngold on 11/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

private val TAG = ConnectionClient::class.java.simpleName

internal enum class HttpMethod(val value: String) {
    GET("GET"), POST("POST"), DELETE("DELETE"), PUT("PUT")
}


internal class ResponseData (val ok: Boolean, val statusCode: Int = -1, data: String = "") {
    val data: JSONObject?
    val error: ErrorResponse?

    init {
        this.data = if (data.isNotBlank()) JSONObject(data) else null
        error = if (this.data != null && !ok) ErrorResponse.create(this.data)
            else null
    }
}


internal object ConnectionClient {
    var baseUrl = "https://api.shipbook.io/v2/"

    suspend fun request(url: String, data: List<BaseObj>, method:HttpMethod = HttpMethod.GET): ResponseData  {
        val jsonArray = JSONArray()
        data.forEach { jsonArray.put(it.toJson ()) }

        return request(url, jsonArray.toString(), method)
    }

    @Suppress("NOTHING_TO_INLINE")
    suspend inline fun request(url: String, data: BaseObj, method:HttpMethod = HttpMethod.GET): ResponseData  {
        return request(url, data.toJson().toString(), method)
    }

    @Suppress("RedundantSuspendModifier")
    suspend fun request(url: String, data: String?, method:HttpMethod = HttpMethod.GET) : ResponseData {
        if (SessionManager.requestsBlocked) return ResponseData(false, 429)

        val urlString = "$baseUrl$url"
        InnerLog.d(TAG, "the url: $urlString")
        val obj = URL(urlString)
        val appId = SessionManager.login?.appId
        try {
            with(obj.openConnection() as HttpURLConnection) {
                requestMethod = method.value
                InnerLog.i(TAG, "Sending '${method.value}' request to URL : $url")

                SessionManager.token?.let {
                    setRequestProperty("Authorization", "Bearer $it")
                }
                setRequestProperty("x-shipbook-sdk", "android/${BuildConfig.VERSION_NAME}")
                appId?.let { setRequestProperty("x-shipbook-app-id", it) }

                data?.let {
                    InnerLog.d(TAG, "Sending output : $data")
                    setRequestProperty("Content-Type", "application/json")
                    outputStream.bufferedWriter().use { it.write(data) }
                }


                InnerLog.i(TAG, "Response Code : $responseCode")
                val ok = responseCode in 200..299

                // Read before the body: a layer in front of the server may answer with a body that isn't JSON, or with none.
                val pauseSeconds = Pause.pauseSeconds(responseCode, getHeaderField("x-shipbook-pause"))
                if (pauseSeconds != null) SessionManager.pause(appId!!, responseCode, pauseSeconds)
                else Pause.backOffSeconds(responseCode, getHeaderField("Retry-After"))?.let { SessionManager.backOff(responseCode, it) }

                val stream = if (!ok) errorStream else inputStream
                val responseDataString = stream.bufferedReader().use { it.readText() }

                InnerLog.d(TAG, "The response $responseDataString")
                val responseData =  ResponseData(ok, responseCode, responseDataString)
                if (!ok && responseCode == 401 && responseData.error?.name == "TokenExpired") {
                    if (SessionManager.refreshToken()) {
                        return request(url, data, method)
                    }
                }
                return responseData
            }
        } catch (e: Exception) {
            InnerLog.e(TAG, "connection error", e)
            return ResponseData(false)
        }
    }
}