package io.shipbook.shipbooksdk.Util

import io.shipbook.shipbooksdk.InnerLog
import java.text.SimpleDateFormat
import java.util.*

/*
 *
 * Created by Elisha Sterngold on 19/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

private val TAG = DateHelper::class.java.simpleName
private val sDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
object DateHelper {
    fun createDateStandard(string: String): Date? {
        try {
            return sDateFormat.parse(string)
        } catch (e: Exception) {
            InnerLog.e(TAG, "error in the parse", e)
            return null
        }
    }


}

fun Date.toStandardString(): String {
    return sDateFormat.format(this)
}