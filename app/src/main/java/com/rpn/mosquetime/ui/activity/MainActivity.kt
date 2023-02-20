package com.rpn.mosquetime.ui.activity

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.rpn.mosquetime.R
import com.rpn.mosquetime.ui.fragment.MainFragment
import com.rpn.mosquetime.ui.viewmodel.MainViewModel
import com.rpn.mosquetime.utils.toast
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent


/**
 * Loads [MainFragment].
 */


//SCREENLAYOUT_SIZE_LARGE 15 - SrceenSize 854 x 480 fwvga
//SCREENLAYOUT_SIZE_NORMAL 15  - SrceenSize 1776 x 1080 nexus 5
//SCREENLAYOUT_SIZE_LARGE 15 - SrceenSize 2153 x 1080 - samsung m31, TV
//SCREENLAYOUT_SIZE_XLARGE 15 - SrceenSize 1952 x 1536 - Nexus 9, Pixel C TAB
class MainActivity : FragmentActivity(), KoinComponent {
    val mainViewModel by inject<MainViewModel>()
    val TAG = "MainActivity"
    var uiModeManager: UiModeManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
//        screenType(this)

        uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager

        if (uiModeManager!!.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) {
            //Keep screen on Television
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }else{
            //Show Full screen on Mobile
            hideSystemUI()
            window.decorView
                .setOnSystemUiVisibilityChangeListener { visibility ->
                    if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                        hideSystemUI()
                    }
                }
        }

        createNotificationChannel()
        verifyPermissions(this)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(MainFragment.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }


    fun screenType(ctx: Context) {
        Log.d(TAG, "Device Name: ${getDeviceName()} ")
        when (ctx.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
            Configuration.SCREENLAYOUT_SIZE_NORMAL -> {
                Log.d(TAG, "SCREENLAYOUT_SIZE_NORMAL ${Configuration.SCREENLAYOUT_SIZE_MASK}")
            }
            Configuration.SCREENLAYOUT_SIZE_SMALL -> {
                Log.d(TAG, "SCREENLAYOUT_SIZE_SMALL ${Configuration.SCREENLAYOUT_SIZE_MASK}")
            }
            Configuration.SCREENLAYOUT_SIZE_LARGE -> {
                Log.d(TAG, "SCREENLAYOUT_SIZE_LARGE ${Configuration.SCREENLAYOUT_SIZE_MASK}")
            }
            Configuration.SCREENLAYOUT_SIZE_MASK -> {
                Log.d(TAG, "SCREENLAYOUT_SIZE_MASK ${Configuration.SCREENLAYOUT_SIZE_MASK}")
            }
            Configuration.SCREENLAYOUT_SIZE_UNDEFINED -> {
                Log.d(TAG, "SCREENLAYOUT_SIZE_UNDEFINED ${Configuration.SCREENLAYOUT_SIZE_MASK}")
            }
            Configuration.SCREENLAYOUT_SIZE_XLARGE -> {
                Log.d(TAG, "SCREENLAYOUT_SIZE_XLARGE ${Configuration.SCREENLAYOUT_SIZE_MASK}")
            }
        }
    }

    fun getDeviceName(): String? {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else capitalize(manufacturer) + " " + model
    }

    private fun capitalize(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true
        var phrase = ""
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += c.uppercaseChar()
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase += c
        }

        return phrase
    }

    fun verifyPermissions(context: Activity): Boolean {

        // This will return the current Status
        val permissionExternalMemory =
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        if (permissionExternalMemory != PackageManager.PERMISSION_GRANTED) {
            val STORAGE_PERMISSIONS = arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            // If permission not granted then ask for permission real time.
            ActivityCompat.requestPermissions(context, STORAGE_PERMISSIONS, 1)
            return false
        }
        return true
    }


    fun hideSystemUI() {
        val UI_OPTIONS = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                )


//        val actionBar: ActionBar = getSupportActionBar()
//        if (actionBar != null) actionBar.hide()
        window.decorView.systemUiVisibility = UI_OPTIONS
    }

}