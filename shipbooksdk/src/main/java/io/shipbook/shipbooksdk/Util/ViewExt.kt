package io.shipbook.shipbooksdk.Util

import android.annotation.SuppressLint
import android.view.View
import io.shipbook.shipbooksdk.InnerLog


/*
 *
 * Created by Elisha Sterngold on 28/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

val View.onClickListener : View.OnClickListener?
        @SuppressLint("PrivateApi")
        get(){
            var retrievedListener: View.OnClickListener? = null
            val viewStr = "android.view.View"
            val lInfoStr = "android.view.View\$ListenerInfo"

            try {
                val listenerField = Class.forName(viewStr).getDeclaredField("mListenerInfo")
                var listenerInfo: Any? = null

                if (listenerField != null) {
                    listenerField.isAccessible = true
                    listenerInfo = listenerField.get(this)
                }

                val clickListenerField = Class.forName(lInfoStr).getDeclaredField("mOnClickListener")

                if (clickListenerField != null && listenerInfo != null) {
                    retrievedListener = clickListenerField.get(listenerInfo) as? View.OnClickListener
                }
            } catch (ex: NoSuchFieldException) {
                InnerLog.e("Reflection", "No Such Field.")
            } catch (ex: IllegalAccessException) {
                InnerLog.e("Reflection", "Illegal Access.")
            } catch (ex: ClassNotFoundException) {
                InnerLog.e("Reflection", "Class Not Found.")
            }

            return retrievedListener
        }