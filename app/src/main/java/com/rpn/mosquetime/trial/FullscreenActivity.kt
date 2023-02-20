package com.rpn.mosquetime.trial
/*

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import com.rpn.mosquetime.trial.FullscreenActivity
import android.app.UiModeManager
import android.annotation.SuppressLint
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultCallback
import android.app.Activity
import android.app.Dialog
import android.content.*
import android.os.Bundle
import android.widget.Toast
import android.os.BatteryManager
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.Window
import androidx.activity.result.ActivityResult

class FullscreenActivity : AppCompatActivity() {
    private val me = this
    var mSharedPref: SharedPreferences? = null
    var uiModeManager: UiModeManager? = null
    private val mHideHandler = Handler()
    private var mContentView: FsClockView? = null
    private val mHidePart2Runnable = Runnable {
        mContentView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )
    }
    private var mControlsView: View? = null
    private val mShowPart2Runnable = Runnable {
        val actionBar = supportActionBar
        actionBar?.show()
        mControlsView!!.visibility = View.VISIBLE
    }
    private var mVisible = false
    private val mHideRunnable = Runnable { hide() }
    var settingsActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            mContentView.loadSettings(me)
        }
        hide()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)
        uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager

        // init settings
        mSharedPref = getSharedPreferences(SettingsActivity.SHARED_PREF_DOMAIN, MODE_PRIVATE)

        // find views
        mControlsView = findViewById(R.id.fullscreen_content_controls)
        mContentView = findViewById(R.id.fullscreen_fsclock_view)
        mContentView.mActivity = this
        if (uiModeManager!!.currentModeType != Configuration.UI_MODE_TYPE_TELEVISION) {
            // we do not enable the onTouch event on TVs because this intersects with the onKeyDown event
            mContentView.setOnClickListener(View.OnClickListener { me.toggle() })
        }

        // initial event state update
        mContentView.updateEventView()
    }

    public override fun onPause() {
        super.onPause()

        // stop the clock
        mContentView.pause()

        // unregister receiver
        unregisterReceiver(mBatInfoReceiver)
    }

    public override fun onResume() {
        super.onResume()

        // init battery info
        registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // start the clock
        mContentView.resume()
        incrementStartedCounter()
        showDialogReview()

        // show TV keys info
        if (uiModeManager!!.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) {
            val tvHintShown = mSharedPref!!.getInt("tv-hint-shown", 0)
            if (tvHintShown == 0) {
                // increment counter
                val editor = mSharedPref!!.edit()
                editor.putInt("tv-hint-shown", tvHintShown + 1)
                editor.apply()
                // show info
                Toast.makeText(this, getString(R.string.tv_settings_note), Toast.LENGTH_LONG).show()
            }
        }
    }

    private val mBatInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            mContentView.updateBattery(plugged, level)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        var handled = false
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_SETTINGS -> {
                openSettings(null)
                handled = true
            }
        }
        return handled || super.onKeyDown(keyCode, event)
    }

    override fun onRequestPermissionsResult(
        callbackId: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(callbackId, permissions, grantResults)
        var i = 0
        for (p in permissions) {
            if (p == Manifest.permission.READ_CALENDAR) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    mContentView.updateEventView()
                }
            }
            i++
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        hide()
        //delayedHide(100);
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        val actionBar = supportActionBar
        actionBar?.hide()
        mControlsView!!.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @SuppressLint("InlinedApi")
    private fun show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    fun openSettings(v: View?) {
        val i = Intent(this, SettingsActivity::class.java)
        settingsActivityResultLauncher.launch(i)
    }

    private fun incrementStartedCounter() {
        val oldStartedValue = mSharedPref!!.getInt("started", 0)
        Log.i("STARTED", Integer.toString(oldStartedValue))
        val editor = mSharedPref!!.edit()
        editor.putInt("started", oldStartedValue + 1)
        editor.apply()
    }

    private fun showDialogReview() {
        if (mSharedPref!!.getInt("started", 0) % 14 == 0
            && mSharedPref!!.getInt("ad-other-apps-shown", 0) < 1
        ) {
            // increment counter
            val editor = mSharedPref!!.edit()
            editor.putInt("ad-other-apps-shown", mSharedPref!!.getInt("ad-other-apps-shown", 0) + 1)
            editor.apply()

            // show ad "other apps"
            val ad = Dialog(this)
            ad.requestWindowFeature(Window.FEATURE_NO_TITLE)
            ad.setContentView(R.layout.dialog_review)
            ad.setCancelable(true)
            ad.findViewById<View>(R.id.buttonReviewNow).setOnClickListener {
                openPlayStore(me, packageName)
                ad.hide()
            }
            ad.findViewById<View>(R.id.linearLayoutRateStars).setOnClickListener {
                openPlayStore(me, packageName)
                ad.hide()
            }
            ad.show()
        }
    }

    companion object {
        private const val UI_ANIMATION_DELAY = 300
        fun openPlayStore(activity: Activity, appId: String) {
            try {
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$appId")
                    )
                )
            } catch (ignored: ActivityNotFoundException) {
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appId")
                    )
                )
            }
        }
    }
}*/
