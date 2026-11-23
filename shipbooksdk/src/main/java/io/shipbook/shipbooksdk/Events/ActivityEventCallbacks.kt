package io.shipbook.shipbooksdk.Events

import android.app.Activity
import android.app.Application
import android.os.Bundle
import io.shipbook.shipbooksdk.InnerLog
import io.shipbook.shipbooksdk.LogManager
import io.shipbook.shipbooksdk.Models.ActivityEvent
import android.view.ViewGroup


/*
 *
 * Created by Elisha Sterngold on 27/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal object  ActivityEventCallbacks : Application.ActivityLifecycleCallbacks {
    private val TAG = ActivityEventCallbacks::class.java.simpleName
    private fun createEvent(event: String, activity: Activity) {
        var title = ""
        activity.title?.let { title = it.toString() }
        val activityEvent = ActivityEvent(activity.javaClass.name, event, title)
        InnerLog.v(TAG, "added activity event: $activityEvent")
        LogManager.push(activityEvent)
    }

    override fun onActivityPaused(activity: Activity) {
        createEvent("onActivityPaused", activity)
    }

    override fun onActivityResumed(activity: Activity) {
        createEvent("onActivityResumed", activity)
    }

    override fun onActivityStarted(activity: Activity) {
        createEvent("onActivityStarted", activity)
        val content: ViewGroup? = activity.findViewById(android.R.id.content)
        content?.let {
            ActionEventManager.registerViews(it)
        } ?: run {
            InnerLog.w(TAG, "content view is null")
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        createEvent("onActivityDestroyed", activity)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        InnerLog.v(TAG, "onActivitySaveInstanceState called")
    }

    override fun onActivityStopped(activity: Activity) {
        createEvent("onActivityStopped", activity)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        createEvent("onActivityCreated", activity)
    }
}
