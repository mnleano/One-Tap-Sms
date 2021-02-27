package com.june.onetapsms.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class UssdService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d(TAG, "onAccessibilityEvent")
        try {
            //Get the source
            val nodeInfo = event.source
            val text = event.text.toString()
            val codes = Codes.codes()
            if (event.className == "android.app.AlertDialog" && Codes.index < codes.size) {
                Log.d(TAG, "onAccessibilityEvent: code=${codes[Codes.index]}\nmenu=$text")
                val nodeInput = nodeInfo.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                val bundle = Bundle()
                bundle.putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    codes[Codes.index]
                )
                nodeInput.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)
                nodeInput.refresh()
                val list = nodeInfo.findAccessibilityNodeInfosByText("Send")
                for (node in list) { node.performAction(AccessibilityNodeInfo.ACTION_CLICK) }
                Codes.index++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInterrupt() {}
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected")
        try {
            val info = AccessibilityServiceInfo()
            info.flags = AccessibilityServiceInfo.DEFAULT
            info.packageNames = arrayOf("com.android.phone")
            info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            serviceInfo = info
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        var TAG = "UssdService"
    }
}