package io.shipbook.shipbooksdk.Events

import android.app.Fragment
import android.app.FragmentManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.view.View
import io.shipbook.shipbooksdk.InnerLog
import io.shipbook.shipbooksdk.LogManager
import io.shipbook.shipbooksdk.Models.FragmentEvent

/*
 *
 * Created by Elisha Sterngold on 27/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */
@RequiresApi(Build.VERSION_CODES.O)
internal object FragmentEventCallbacks : FragmentManager.FragmentLifecycleCallbacks() {
    private val TAG = FragmentEventCallbacks::class.java.simpleName

    private fun createEvent(event: String, fragment: Fragment) {
        val fragmentEvent = FragmentEvent(fragment.javaClass.name, event)
        InnerLog.v(TAG, "added fragment event: $fragmentEvent")
        LogManager.push(fragmentEvent)
    }

    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context?) {
        createEvent("onFragmentPreAttached", f)
    }


    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        createEvent("onFragmentAttached", f)
    }

    override fun onFragmentPreCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        createEvent("onFragmentPreCreated", f)
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        createEvent("onFragmentCreated", f)
    }

    override fun onFragmentActivityCreated(fm: FragmentManager, f: Fragment,
                                           savedInstanceState: Bundle?) {
        createEvent("onFragmentActivityCreated", f)
    }

    override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View,
                                       savedInstanceState: Bundle?) {
        createEvent("onFragmentViewCreated", f)
    }

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        createEvent("onFragmentStarted", f)
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        createEvent("onFragmentResumed", f)
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        createEvent("onFragmentPaused", f)
    }

    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        createEvent("onFragmentStopped", f)
    }

    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle?) {
        InnerLog.v(TAG, "onFragmentSaveInstanceState called")
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        createEvent("onFragmentViewDestroyed", f)
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        createEvent("onFragmentDestroyed", f)
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        createEvent("onFragmentDetached", f)
    }
}
