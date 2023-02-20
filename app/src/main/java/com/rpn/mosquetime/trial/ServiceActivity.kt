package com.rpn.mosquetime.trial

/*

import android.content.*
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rpn.mosquetime.R
import com.rpn.mosquetime.services.Timer_Service
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var btn_start: Button? = null
    private var btn_cancel: Button? = null
    private var tv_timer: TextView? = null
    var date_time: String? = null
    var calendar: Calendar? = null
    var simpleDateFormat: SimpleDateFormat? = null
    var et_hours: EditText? = null
    var mpref: SharedPreferences? = null
    var mEditor: SharedPreferences.Editor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        listener()
    }

    private fun init() {
        btn_start = findViewById<View>(R.id.btn_timer) as Button
        tv_timer = findViewById<View>(R.id.tv_timer) as TextView
        et_hours = findViewById<View>(R.id.et_hours) as EditText
        btn_cancel = findViewById<View>(R.id.btn_cancel) as Button
        mpref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        mEditor = mpref.edit()
        try {
            val str_value = mpref.getString("data", "")
            if (str_value!!.matches("")) {
                et_hours!!.isEnabled = true
                btn_start!!.isEnabled = true
                tv_timer!!.text = ""
            } else {
                if (mpref.getBoolean("finish", false)) {
                    et_hours!!.isEnabled = true
                    btn_start!!.isEnabled = true
                    tv_timer!!.text = ""
                } else {
                    et_hours!!.isEnabled = false
                    btn_start!!.isEnabled = false
                    tv_timer!!.text = str_value
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun listener() {
        btn_start!!.setOnClickListener(this)
        btn_cancel!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_timer -> if (et_hours!!.text.toString().length > 0) {
                val int_hours = Integer.valueOf(et_hours!!.text.toString())
                if (int_hours <= 24) {
                    et_hours!!.isEnabled = false
                    btn_start!!.isEnabled = false
                    calendar = Calendar.getInstance()
                    simpleDateFormat = SimpleDateFormat("HH:mm:ss")
                    date_time = simpleDateFormat!!.format(calendar.getTime())
                    mEditor!!.putString("data", date_time).commit()
                    mEditor!!.putString("hours", et_hours!!.text.toString()).commit()
                    val intent_service = Intent(applicationContext, Timer_Service::class.java)
                    startService(intent_service)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Please select the value below 24 hours",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                */
/*
                    mTimer = new Timer();
                    mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 5, NOTIFY_INTERVAL);*//*

            } else {
                Toast.makeText(applicationContext, "Please select value", Toast.LENGTH_SHORT).show()
            }
            R.id.btn_cancel -> {
                val intent = Intent(applicationContext, Timer_Service::class.java)
                stopService(intent)
                mEditor!!.clear().commit()
                et_hours!!.isEnabled = true
                btn_start!!.isEnabled = true
                tv_timer!!.text = ""
            }
        }
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val str_time = intent.getStringExtra("time")
            tv_timer!!.text = str_time
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(broadcastReceiver, IntentFilter(Timer_Service.str_receiver))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }
}*/
