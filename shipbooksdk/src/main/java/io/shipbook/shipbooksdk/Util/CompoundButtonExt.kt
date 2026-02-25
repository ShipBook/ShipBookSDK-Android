package io.shipbook.shipbooksdk.Util

import android.annotation.SuppressLint
import android.widget.CompoundButton
import io.shipbook.shipbooksdk.InnerLog

/*
 *
 * Created by Elisha Sterngold on 28/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal val CompoundButton.onCheckedChangeListener : CompoundButton.OnCheckedChangeListener?
    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    get() {

        var retrievedListener: CompoundButton.OnCheckedChangeListener? = null
        val viewStr = "android.widget.CompoundButton"

        try {
            val listenerField = Class.forName(viewStr).getDeclaredField("mOnCheckedChangeListener")
            listenerField.isAccessible = true
            retrievedListener = listenerField.get(this) as? CompoundButton.OnCheckedChangeListener
        } catch (ex: NoSuchFieldException) {
            InnerLog.e("Reflection", "No Such Field.")
        } catch (ex: IllegalAccessException) {
            InnerLog.e("Reflection", "Illegal Access.")
        } catch (ex: ClassNotFoundException) {
            InnerLog.e("Reflection", "Class Not Found.")
        }
        return retrievedListener
    }