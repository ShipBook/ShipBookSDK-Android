//package io.shipbook.ShipBookSDK
//
//import android.app.Fragment
//import android.content.Context
//import android.os.Bundle
//import android.support.v4.app.FragmentManager
//import android.view.View
//
//
///*
// *
// * Created by Elisha Sterngold on 22/02/2018.
// * Copyright © 2018 ShipBook Ltd. All rights reserved.
// *
// */
//class EventFragmentLifecycleCallbacks : FragmentManager.FragmentLifecycleCallbacks() {
//
//    override fun onFragmentPreAttached(fm: FragmentManager?, f: android.support.v4.app.Fragment?, context: Context?) {
//        super.onFragmentPreAttached(fm, f, context)
//    }
//
//
//    override fun onFragmentAttached(fm: FragmentManager, f: android.support.v4.app.Fragment, context: Context) {
//        super.onFragmentAttached(fm, f, context)
//    }
//
//    fun onFragmentPreCreated(fm: FragmentManager, f: android.support.v4.app.Fragment,
//                             savedInstanceState: Bundle) {
//    }
//
//    /**
//     * Called after the fragment has returned from the FragmentManager's call to
//     * [Fragment.onCreate]. This will only happen once for any given
//     * fragment instance, though the fragment may be attached and detached multiple times.
//     *
//     * @param fm Host FragmentManager
//     * @param f Fragment changing state
//     * @param savedInstanceState Saved instance bundle from a previous instance
//     */
//    fun onFragmentCreated(fm: FragmentManager, f: android.support.v4.app.Fragment, savedInstanceState: Bundle) {}
//
//    /**
//     * Called after the fragment has returned from the FragmentManager's call to
//     * [Fragment.onActivityCreated]. This will only happen once for any given
//     * fragment instance, though the fragment may be attached and detached multiple times.
//     *
//     * @param fm Host FragmentManager
//     * @param f Fragment changing state
//     * @param savedInstanceState Saved instance bundle from a previous instance
//     */
//    fun onFragmentActivityCreated(fm: FragmentManager, f: android.support.v4.app.Fragment,
//                                  savedInstanceState: Bundle) {
//    }
//
//    /**
//     * Called after the fragment has returned a non-null view from the FragmentManager's
//     * request to [Fragment.onCreateView].
//     *
//     * @param fm Host FragmentManager
//     * @param f Fragment that created and owns the view
//     * @param v View returned by the fragment
//     * @param savedInstanceState Saved instance bundle from a previous instance
//     */
//    fun onFragmentViewCreated(fm: FragmentManager, f: android.support.v4.app.Fragment, v: View,
//                              savedInstanceState: Bundle) {
//    }
//
//    /**
//     * Called after the fragment has returned from the FragmentManager's call to
//     * [Fragment.onStart].
//     *
//     * @param fm Host FragmentManager
//     * @param f Fragment changing state
//     */
//    fun onFragmentStarted(fm: FragmentManager, f: android.support.v4.app.Fragment) {}
//
//    /**
//     * Called after the fragment has returned from the FragmentManager's call to
//     * [Fragment.onResume].
//     *
//     * @param fm Host FragmentManager
//     * @param f Fragment changing state
//     */
//    fun onFragmentResumed(fm: FragmentManager, f: android.support.v4.app.Fragment) {}
//
//    /**
//     * Called after the fragment has returned from the FragmentManager's call to
//     * [Fragment.onPause].
//     *
//     * @param fm Host FragmentManager
//     * @param f Fragment changing state
//     */
//    fun onFragmentPaused(fm: FragmentManager, f: android.support.v4.app.Fragment) {}
//
//    /**
//     * Called after the fragment has returned from the FragmentManager's call to
//     * [Fragment.onStop].
//     *
//     * @param fm Host FragmentManager
//     * @param f Fragment changing state
//     */
//    fun onFragmentStopped(fm: FragmentManager, f: android.support.v4.app.Fragment) {}
//
//    /**
//     * Called after the fragment has returned from the FragmentManager's call to
//     * [Fragment.onSaveInstanceState].
//     *
//     * @param fm Host FragmentManager
//     * @param f Fragment changing state
//     * @param outState Saved state bundle for the fragment
//     */
//    fun onFragmentSaveInstanceState(fm: FragmentManager, f: android.support.v4.app.Fragment, outState: Bundle) {}
//
//    /**
//     * Called after the fragment has returned from the FragmentManager's call to
//     * [Fragment.onDestroyView].
//     *
//     * @param fm Host FragmentManager
//     * @param f Fragment changing state
//     */
//    fun onFragmentViewDestroyed(fm: FragmentManager, f: android.support.v4.app.Fragment) {}
//
//    /**
//     * Called after the fragment has returned from the FragmentManager's call to
//     * [Fragment.onDestroy].
//     *
//     * @param fm Host FragmentManager
//     * @param f Fragment changing state
//     */
//    fun onFragmentDestroyed(fm: FragmentManager, f: android.support.v4.app.Fragment) {}
//
//    /**
//     * Called after the fragment has returned from the FragmentManager's call to
//     * [Fragment.onDetach].
//     *
//     * @param fm Host FragmentManager
//     * @param f Fragment changing state
//     */
//    fun onFragmentDetached(fm: FragmentManager, f: android.support.v4.app.Fragment) {}
//
//
//}