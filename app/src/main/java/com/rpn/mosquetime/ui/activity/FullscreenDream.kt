package com.rpn.mosquetime.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.service.dreams.DreamService
import android.view.View
import android.widget.FrameLayout
import com.rpn.mosquetime.R

class FullscreenDream : DreamService() {
    var mContentView: FrameLayout? = null
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        setContentView(R.layout.activity_main)

        // find views
        mContentView = findViewById(R.id.main_browse_fragment)

        // hide the system navigation bar with the same flags as done in the Android clock app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mContentView?.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }

        // init battery info
        registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onDreamingStarted() {
        // start the clock
//        mContentView.resume()
    }

    override fun onDreamingStopped() {
        // stop the clock
//        mContentView.pause()
    }

    override fun onDetachedFromWindow() {
        // unregister battery receiver
        try {
            unregisterReceiver(mBatInfoReceiver)
        } catch (ignored: IllegalArgumentException) {
        }
    }

    private val mBatInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
//            mContentView.updateBattery(plugged, level)
        }
    }
}