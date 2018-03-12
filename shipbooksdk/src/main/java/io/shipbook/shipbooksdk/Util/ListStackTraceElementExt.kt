package io.shipbook.shipbooksdk.Util

import org.json.JSONArray
import java.util.ArrayList
import io.shipbook.shipbooksdk.Models.StackTraceElement

/*
 *
 * Created by Elisha Sterngold on 07/03/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal fun JSONArray.toListStackTraceElement(): List<StackTraceElement> {
    val stackTrace = ArrayList<StackTraceElement>(this.length())
    (0 until this.length()).mapTo(stackTrace) { StackTraceElement.create(this.getJSONObject(it)) }
    return  stackTrace
}

internal fun Array<java.lang.StackTraceElement>.toInternal(): List<StackTraceElement> {
    val stackTrace : ArrayList<StackTraceElement> = arrayListOf()
    this.forEach {
        val stackTraceElement = StackTraceElement(it.className, it.methodName, it.fileName, it.lineNumber)
        stackTrace.add(stackTraceElement)
    }
    return stackTrace
}

internal fun List<StackTraceElement>.toJson(): JSONArray {
    val stackTraceArray = JSONArray()
    this.forEach {
        stackTraceArray.put(it.toJson())
    }
    return stackTraceArray
}