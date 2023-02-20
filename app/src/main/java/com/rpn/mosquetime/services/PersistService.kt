package com.rpn.mosquetime.services


import android.R.string
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.ActivityManager.RunningTaskInfo
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager
import android.preference.PreferenceManager
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class PersistService : Service() {
    private var mWakeLock: PowerManager.WakeLock? = null
    val TAG = "PersistService"
    override fun onCreate() {
        super.onCreate()
        stopTask = false

        // Optional: Screen Always On Mode!
        // Screen will never switch off this way

        val pm = getSystemService(POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "a_tag")
        mWakeLock?.acquire()

        // Start your (polling) task
        val task: TimerTask = object : TimerTask() {
            override fun run() {

                // If you wish to stop the task/polling
                if (stopTask) {
                    cancel()
                }

                // The first in the list of RunningTasks is always the foreground task.
                val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
                val tasks: List<RunningAppProcessInfo> = activityManager.getRunningAppProcesses()
                val foregroundTaskPackageNameTest = tasks[0].processName

                val foregroundTaskInfo: RunningTaskInfo = activityManager.getRunningTasks(1).get(0)
                val foregroundTaskPackageName = foregroundTaskInfo.topActivity!!.packageName

                Log.d(
                    TAG,
                    "run: foregroundTaskPackageName $foregroundTaskPackageName foregroundTaskPackageNameTest $foregroundTaskPackageNameTest"
                )

                // Check foreground app: If it is not in the foreground... bring it!
                if (foregroundTaskPackageName != YOUR_APP_PACKAGE_NAME) {
                    val LaunchIntent = packageManager.getLaunchIntentForPackage(
                        YOUR_APP_PACKAGE_NAME
                    )
                    startActivity(LaunchIntent)
                }
            }
        }
        val timer = Timer()
        timer.scheduleAtFixedRate(task, 0, INTERVAL.toLong())
    }

    override fun onDestroy() {
        stopTask = true
        if (mWakeLock != null) mWakeLock!!.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        private const val INTERVAL = 3000 // poll every 3 secs
        private const val YOUR_APP_PACKAGE_NAME = "com.rpn.mosquetime"
        private var stopTask = false
    }
}

class Timer_Service : Service() {
    private val mHandler = Handler()
    val TAG = "Timer_Service"
    var calendar: Calendar? = null
    var simpleDateFormat: SimpleDateFormat? = null
    var strDate: String? = null
    var date_current: Date? = null
    var date_diff: Date? = null
    var mpref: SharedPreferences? = null
    var mEditor: SharedPreferences.Editor? = null
    private var mTimer: Timer? = null
    var intent: Intent? = null
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mpref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        mEditor = mpref?.edit()
        calendar = Calendar.getInstance()
        simpleDateFormat = SimpleDateFormat("HH:mm:ss")
        mTimer = Timer()
        mTimer!!.scheduleAtFixedRate(TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL)
        intent = Intent(str_receiver)

        Log.d(TAG, "onCreate: Time at service")
    }

    internal inner class TimeDisplayTimerTask : TimerTask() {
        override fun run() {
            mHandler.post {
                calendar = Calendar.getInstance()
                simpleDateFormat = SimpleDateFormat("HH:mm:ss")
                strDate = simpleDateFormat!!.format(calendar?.getTime())
                Log.d(TAG, strDate.toString())
//                twoDatesBetweenTime()

                fn_update(strDate.toString())
            }
        }
    }

    fun twoDatesBetweenTime(): String {
        try {
            date_current = simpleDateFormat!!.parse(strDate)
        } catch (e: Exception) {
        }
        try {
            date_diff = simpleDateFormat!!.parse(mpref!!.getString("data", ""))
        } catch (e: Exception) {
        }
        try {
            val diff = date_current!!.time - date_diff!!.time
            val int_hours = Integer.valueOf(mpref!!.getString("hours", ""))
            val int_timer = TimeUnit.HOURS.toMillis(int_hours.toLong())
            val long_hours = int_timer - diff
            val diffSeconds2 = long_hours / 1000 % 60
            val diffMinutes2 = long_hours / (60 * 1000) % 60
            val diffHours2 = long_hours / (60 * 60 * 1000) % 24
            if (long_hours > 0) {
                val str_testing = "$diffHours2:$diffMinutes2:$diffSeconds2"
                Log.d(TAG, str_testing)
                fn_update(str_testing)
            } else {
                mEditor!!.putBoolean("finish", true).commit()
                mTimer!!.cancel()
            }
        } catch (e: Exception) {
            mTimer!!.cancel()
            mTimer!!.purge()
        }
        return ""
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("Service finish", "Finish")
    }

    private fun fn_update(str_time: String) {
        intent!!.putExtra("time", str_time)
        sendBroadcast(intent)
    }

    companion object {
        var str_receiver = "com.countdowntimerservice.receiver"
        const val NOTIFY_INTERVAL: Long = 1000
    }
}