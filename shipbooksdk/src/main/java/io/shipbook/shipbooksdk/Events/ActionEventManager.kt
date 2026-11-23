package io.shipbook.shipbooksdk.Events

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.shipbook.shipbooksdk.InnerLog
import io.shipbook.shipbooksdk.LogManager
import io.shipbook.shipbooksdk.Models.ActionEvent
import io.shipbook.shipbooksdk.Util.onCheckedChangeListener
import io.shipbook.shipbooksdk.Util.onClickListener
import io.shipbook.shipbooksdk.Util.onSeekBarChangeListener
import io.shipbook.shipbooksdk.Util.views
import java.util.Collections
import java.util.WeakHashMap

/*
 *
 * Created by Elisha Sterngold on 28/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal object ActionEventManager {
    private val TAG = ActionEventManager::class.java.simpleName
    var ignoreViews = emptySet<Int>()
    // A text watcher can't be read back from its view, so the fields that have one are remembered here.
    private val watchedTexts = Collections.newSetFromMap(WeakHashMap<EditText, Boolean>())

    private fun createActionEvent(action: String, title: String, view: View) {
        val sender = view.javaClass.simpleName
        val actionEvent = ActionEvent(action, sender, title, "")
        InnerLog.v(TAG, "added action event: $actionEvent")
        LogManager.push(actionEvent)

    }

    // Views are registered on every activity start, so each wrapper is a class of its own: a view that already carries one is left alone.
    private class ClickListener(val wrapped: View.OnClickListener?) : View.OnClickListener {
        override fun onClick(view: View) {
            createActionEvent("onClick", (view as Button).text.toString(), view)
            wrapped?.onClick(view)
        }
    }

    private class CheckedChangeListener(val wrapped: CompoundButton.OnCheckedChangeListener?) : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            createActionEvent("onCheckedChange", buttonView.text.toString(), buttonView)
            wrapped?.onCheckedChanged(buttonView, isChecked)
        }
    }

    private class SeekBarChangeListener(val wrapped: SeekBar.OnSeekBarChangeListener?) : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser) createActionEvent("progressChanged", "", seekBar)
            wrapped?.onProgressChanged(seekBar, progress, fromUser)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            wrapped?.onStartTrackingTouch(seekBar)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            wrapped?.onStopTrackingTouch(seekBar)
        }
    }

    private fun registerView(view: View) {
        if (ignoreViews.contains(view.id)) return
        when (view) {
            is ViewGroup -> registerViews(view)
            is CompoundButton -> {
                val onCheckedChangeListener = view.onCheckedChangeListener
                if (onCheckedChangeListener is CheckedChangeListener) return
                view.setOnCheckedChangeListener(CheckedChangeListener(onCheckedChangeListener))
            }
            is Button -> {
                val onClickListener = view.onClickListener
                if (onClickListener is ClickListener) return
                view.setOnClickListener(ClickListener(onClickListener))
            }
            is EditText -> {
                if (!watchedTexts.add(view)) return
                view.addTextChangedListener(object: TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        // The text stays on the device: it can be a name, a card number or a password.
                        createActionEvent("textChanged", "", view)
                    }
                })

            }
            is SeekBar -> {
                val onSeekBarChangeListener = view.onSeekBarChangeListener
                if (onSeekBarChangeListener is SeekBarChangeListener) return
                view.setOnSeekBarChangeListener(SeekBarChangeListener(onSeekBarChangeListener))
            }
            is Spinner -> {
                val onItemSelectedListener = view.onItemSelectedListener
                view.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>) {
                        onItemSelectedListener?.onNothingSelected(parent)
                    }

                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        createActionEvent("itemSelected", "", parent)
                        onItemSelectedListener?.onItemSelected(parent, view, position, id)
                    }
                }
            }
        }
    }

    fun registerViews(parent: ViewGroup) {
//        parent.setOnHierarchyChangeListener(object: ViewGroup.OnHierarchyChangeListener { // TODO: use the getOnHierarchChangeListener
//            override fun onChildViewRemoved(parent: View, child: View) {
//
//            }
//
//            override fun onChildViewAdded(parent: View, child: View) {
//                registerView(child)
//            }
//        })

        parent.views.forEach {view -> registerView(view) }
    }
}