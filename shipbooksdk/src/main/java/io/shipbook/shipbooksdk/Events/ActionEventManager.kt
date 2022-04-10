package io.shipbook.shipbooksdk.Events

import android.text.Editable
import android.text.InputType
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

/*
 *
 * Created by Elisha Sterngold on 28/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal object ActionEventManager {
    private val TAG = ActionEventManager::class.java.simpleName
    var ignoreViews = emptySet<Int>()
    private fun createActionEvent(action: String, title: String, view: View) {
        val sender = view.javaClass.simpleName
        val actionEvent = ActionEvent(action, sender, title, "")
        InnerLog.v(TAG, "added action event: $actionEvent")
        LogManager.push(actionEvent)

    }

    private fun registerView(view: View) {
        if (ignoreViews.contains(view.id)) return
        when (view) {
            is ViewGroup -> registerViews(view)
            is CompoundButton -> {
                val onCheckedChangeListener = view.onCheckedChangeListener
                view.setOnCheckedChangeListener {  buttonView, isChecked ->
                    createActionEvent("onCheckedChange", view.text.toString(), view)
                    onCheckedChangeListener?.onCheckedChanged(buttonView, isChecked)
                }
            }
            is Button -> {
                val onClickListener = view.onClickListener
                view.setOnClickListener {
                    createActionEvent("onClick", view.text.toString(), it)
                    onClickListener?.onClick(view)
                }
            }
            is EditText -> {
                view.addTextChangedListener(object: TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (view.inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                            InnerLog.d("actionEvent", "is password")
                        }
                        else createActionEvent("textChanged", s.toString(), view) //TODO: need to check that it isn't passing the password info
                    }
                })

            }
            is SeekBar -> {
                val onSeekBarChangeListener = view.onSeekBarChangeListener
                view.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        createActionEvent("progessChanged", "", seekBar)
                        onSeekBarChangeListener?.onProgressChanged(seekBar, progress, fromUser)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {
                        onSeekBarChangeListener?.onStartTrackingTouch(seekBar)
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        onSeekBarChangeListener?.onStopTrackingTouch(seekBar)
                    }

                })

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