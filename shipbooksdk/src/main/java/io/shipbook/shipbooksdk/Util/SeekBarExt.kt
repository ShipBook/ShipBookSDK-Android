package io.shipbook.shipbooksdk.Util

import android.annotation.SuppressLint
import android.widget.SeekBar
import io.shipbook.shipbooksdk.InnerLog

/*
 *
 * Created by Elisha Sterngold on 28/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal val SeekBar.onSeekBarChangeListener : SeekBar.OnSeekBarChangeListener?
    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    get() {

        var retrievedListener: SeekBar.OnSeekBarChangeListener? = null
        val viewStr = "android.widget.SeekBar"

        try {
            val listenerField = Class.forName(viewStr).getDeclaredField("mOnSeekBarChangeListener")
            listenerField.isAccessible = true
            retrievedListener = listenerField.get(this) as? SeekBar.OnSeekBarChangeListener
        } catch (ex: NoSuchFieldException) {
            InnerLog.e("Reflection", "No Such Field.")
        } catch (ex: IllegalAccessException) {
            InnerLog.e("Reflection", "Illegal Access.")
        } catch (ex: ClassNotFoundException) {
            InnerLog.e("Reflection", "Class Not Found.")
        }
        return retrievedListener
    }