package com.rpn.mosquetime.trial
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.content.ContentValues
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.os.CountDownTimer
//import android.os.Handler
//import android.provider.MediaStore
//import android.view.View
//import android.widget.Button
//import android.widget.EditText
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ImageCapture
//import androidx.camera.core.ImageCaptureException
//import androidx.camera.core.Preview
//import androidx.camera.core.VideoCapture
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.google.common.util.concurrent.ListenableFuture
//import java.util.concurrent.ExecutionException
//import java.util.concurrent.Executor
//
//package com.example.cameraxtrial
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ImageCapture
//import androidx.camera.core.ImageCaptureException
//import androidx.camera.core.Preview
//import androidx.camera.core.VideoCapture
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//
//class MainActivity : AppCompatActivity(), View.OnClickListener {
//    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
//    var previewView: PreviewView? = null
//    var changescreen: Button? = null
//    var bTakePicture: Button? = null
//    private var imageCapture: ImageCapture? = null
//    private var videoCapture: VideoCapture? = null
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        changescreen = findViewById(R.id.switch1)
//        bTakePicture = findViewById(R.id.bCapture)
//        previewView = findViewById(R.id.previewView)
//        bTakePicture.setOnClickListener(this)
//        changescreen.setOnClickListener(this)
//        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture!!.addListener({
//            try {
//                val cameraProvider: ProcessCameraProvider = cameraProviderFuture!!.get()
//                startCameraX(cameraProvider)
//            } catch (e: ExecutionException) {
//                e.printStackTrace()
//            } catch (e: InterruptedException) {
//                e.printStackTrace()
//            }
//        }, executor)
//    }
//
//    private val executor: Executor
//        private get() = ContextCompat.getMainExecutor(this)
//
//    @SuppressLint("RestrictedApi")
//    private fun startCameraX(cameraProvider: ProcessCameraProvider) {
//        cameraProvider.unbindAll()
//        val cameraSelector: CameraSelector = Builder()
//            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//            .build()
//        val preview: Preview = Builder().build()
//        preview.setSurfaceProvider(previewView.getSurfaceProvider())
//        imageCapture = Builder()
//            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//            .build()
//        videoCapture = Builder()
//            .setVideoFrameRate(30)
//            .build()
//        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture)
//    }
//
//    @SuppressLint("RestrictedApi")
//    override fun onClick(view: View) {
//        when (view.id) {
//            R.id.bCapture -> {
//                val editText = findViewById<EditText>(R.id.Capture_count)
//                val temp = editText.text.toString()
//                val n = temp.toInt()
//                val editText2 = findViewById<EditText>(R.id.Capture_delay)
//                val temp2 = editText2.text.toString()
//                val n2 = temp2.toInt()
//                val delay_final = seconds(n2)
//                var i = 1
//                while (i <= n) {
//                    if (i == 1) {
//                        capturePhoto()
//                    } else {
//                        val textview = findViewById<TextView>(R.id.textview)
//                        object : CountDownTimer(delay_final.toLong(), 1000) {
//                            override fun onTick(millisUntilFinished: Long) {
//                                textview.text = "seconds remaining: " + millisUntilFinished / 1000
//                                // logic to set the EditText could go here
//                            }
//
//                            override fun onFinish() {
//                                textview.text = "done!"
//                                capturePhoto()
//                            }
//                        }.start()
//                        Handler().postDelayed({ capturePhoto() }, delay_final.toLong())
//                    }
//                    // HERE IS THE PROBLEM!!
//                    if (i == n) {
//                        move()
//                    }
//                    i++
//                }
//            }
//            R.id.switch1 -> {
//                val myIntent = Intent(this@MainActivity, MainActivity2::class.java)
//                //myIntent.putExtra("array", mylist);
//                startActivity(myIntent)
//            }
//        }
//    }
//
//    fun seconds(value: Int): Int {
//        val thousand = 1000
//        return value * thousand
//    }
//
//    fun move() {
//        val myIntent = Intent(this@MainActivity, MainActivity2::class.java)
//        //    myIntent.putExtra("array", mylist);
//        startActivity(myIntent)
//    }
//
//    @SuppressLint("RestrictedApi")
//    private fun recordVideo() {
//        if (videoCapture != null) {
//            val timeStamp = System.currentTimeMillis()
//            val contentValues = ContentValues()
//            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp)
//            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.RECORD_AUDIO
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return
//            }
//            videoCapture.startRecording(
//                Builder(
//                    contentResolver,
//                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
//                    contentValues
//                ).build(),
//                executor,
//                object : OnVideoSavedCallback() {
//                    fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
//                        Toast.makeText(this@MainActivity, "Saving...", Toast.LENGTH_SHORT).show()
//                    }
//
//                    fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
//                        Toast.makeText(this@MainActivity, "Error: $message", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                }
//            )
//        }
//    }
//
//    private fun capturePhoto() {
//        val timeStamp = System.currentTimeMillis()
//        val contentValues = ContentValues()
//        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp)
//        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//        imageCapture.takePicture(
//            Builder(
//                contentResolver,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                contentValues
//            ).build(),
//            executor,
//            object : OnImageSavedCallback() {
//                fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                    Toast.makeText(this@MainActivity, "Saving Image...", Toast.LENGTH_SHORT).show()
//                }
//
//                fun onError(exception: ImageCaptureException) {
//                    Toast.makeText(
//                        this@MainActivity,
//                        "Error: " + exception.getMessage(),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        )
//    }
//}
//
///*
//
//import android.Manifest
//import android.widget.FrameLayout
//import androidx.appcompat.app.AppCompatActivity
//import android.content.SharedPreferences
//import android.widget.TextView
//import android.speech.tts.TextToSpeech
//import androidx.core.content.res.ResourcesCompat
//import android.os.Build
//import android.view.WindowManager
//import android.view.ViewTreeObserver.OnGlobalLayoutListener
//import com.rpn.mosquetime.trial.FsClockView
//import android.speech.tts.TextToSpeech.OnInitListener
//import android.annotation.SuppressLint
//import android.app.Activity
//import android.content.Context
//import com.google.gson.Gson
//import android.widget.Toast
//import android.media.RingtoneManager
//import android.media.Ringtone
//import androidx.core.content.ContextCompat
//import android.content.pm.PackageManager
//import android.graphics.*
//import android.graphics.drawable.BitmapDrawable
//import android.net.Uri
//import android.provider.CalendarContract
//import android.text.format.DateFormat
//import android.util.AttributeSet
//import android.util.Log
//import android.util.TypedValue
//import android.view.View
//import android.view.View.MeasureSpec
//import android.widget.ImageView
//import java.io.File
//import java.lang.Exception
//import java.text.SimpleDateFormat
//import java.util.*
//
//class FsClockView(c: Context, attrs: AttributeSet?) : FrameLayout(c, attrs) {
//    var mActivity: AppCompatActivity? = null
//    var mSharedPref: SharedPreferences? = null
//    var mRootView: View? = null
//    var mBatteryView: View? = null
//    var mBatteryText: TextView? = null
//    var mBatteryImage: ImageView? = null
//    var mClockText: TextView? = null
//    var mSecondsText: TextView? = null
//    var mDateText: TextView? = null
//    var mTextViewEvents: TextView? = null
//    var mClockBackgroundImage: ImageView? = null
//    var mSecondsHand: ImageView? = null
//    var mMinutesHand: ImageView? = null
//    var mHoursHand: ImageView? = null
//    var timerAnalogClock: Timer? = null
//    var timerCalendarUpdate: Timer? = null
//    var timerCheckEvent: Timer? = null
//    var tts: TextToSpeech? = null
//    var events: Array<Event>?
//    var format24hrs = false
//    private fun commonInit(c: Context) {
//        inflate(context, R.layout.view_fsclock, this)
//
//        // init settings
//        mSharedPref =
//            c.getSharedPreferences(SettingsActivity.SHARED_PREF_DOMAIN, Context.MODE_PRIVATE)
//
//        // find views
//        mRootView = findViewById(R.id.fsclockRootView)
//        mClockText = findViewById(R.id.textViewClock)
//        mSecondsText = findViewById(R.id.textViewClockSeconds)
//        mDateText = findViewById(R.id.textViewDate)
//        mTextViewEvents = findViewById(R.id.textViewEvents)
//        mClockBackgroundImage = findViewById(R.id.imageViewClockBackground)
//        mHoursHand = findViewById(R.id.imageViewClockHoursHand)
//        mMinutesHand = findViewById(R.id.imageViewClockMinutesHand)
//        mSecondsHand = findViewById(R.id.imageViewClockSecondsHand)
//        mBatteryView = findViewById(R.id.linearLayoutBattery)
//        mBatteryText = findViewById(R.id.textViewBattery)
//        mBatteryImage = findViewById(R.id.imageViewBattery)
//        mBatteryImage.setImageResource(R.drawable.ic_battery_full_black_24dp)
//
//        // init font
//        val fontLed = ResourcesCompat.getFont(c, R.font.dseg7classic_regular)
//        val fontDate = ResourcesCompat.getFont(c, R.font.cairo_regular)
//        mClockText.setTypeface(fontLed)
//        mSecondsText.setTypeface(fontLed)
//        mDateText.setTypeface(fontDate)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            // auto-calc text size
//            mClockText.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
//            mSecondsText.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
//            mDateText.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
//        } else {
//            // calc text sizes manually on older Android versions
//            updateClock()
//            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//            val display = wm.defaultDisplay
//            val size = Point()
//            display.getSize(size)
//            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
//                override fun onGlobalLayout() {
//                    val clockTextContainerWidth = mClockText.getWidth()
//                    val secondsContainerWidth = mSecondsText.getWidth()
//                    val dateContainerWidth = mDateText.getWidth()
//                    val clockText = mClockText.getText().toString()
//                    val secondsText = mSecondsText.getText().toString()
//                    val dateText = mDateText.getText().toString()
//                    run {
//                        var i = 140
//                        while (i >= 50) {
//                            val textWidth = getTextWidth(context, clockText, i, size, fontLed)
//                            if (textWidth < clockTextContainerWidth) {
//                                mClockText.setTextSize(i.toFloat())
//                                Log.i(
//                                    "CALC_TSIZE_CLOCK",
//                                    "$i => $textWidth (max $clockTextContainerWidth) @ $clockText"
//                                )
//                                break
//                            }
//                            i -= 2
//                        }
//                    }
//                    run {
//                        var i = 80
//                        while (i >= 20) {
//                            val textWidth = getTextWidth(context, secondsText, i, size, fontLed)
//                            if (textWidth < secondsContainerWidth) {
//                                mSecondsText.setTextSize(i.toFloat())
//                                Log.i(
//                                    "CALC_TSIZE_SECS",
//                                    "$i => $textWidth (max $secondsContainerWidth) @ $secondsText"
//                                )
//                                break
//                            }
//                            i -= 2
//                        }
//                    }
//                    var i = 120
//                    while (i >= 20) {
//                        val textWidth = getTextWidth(context, dateText, i, size, fontDate)
//                        if (textWidth < dateContainerWidth) {
//                            mDateText.setTextSize(i.toFloat())
//                            Log.i(
//                                "CALC_TSIZE_DATE",
//                                "$i => $textWidth (max $dateContainerWidth) @ $dateText"
//                            )
//                            break
//                        }
//                        i -= 2
//                    }
//                }
//            })
//        }
//
//        // init preferences
//        loadSettings(mActivity)
//    }
//
//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//
//        // init text to speech
//        tts = TextToSpeech(context) { status ->
//            if (status != TextToSpeech.SUCCESS) {
//                tts = null
//            }
//        }
//    }
//
//    override fun onDetachedFromWindow() {
//        super.onDetachedFromWindow()
//
//        // destroy tts service connection
//        tts!!.stop()
//        tts!!.shutdown()
//    }
//
//    @SuppressLint("SimpleDateFormat")
//    private fun updateClock() {
//        val cal = Calendar.getInstance()
//        val sdfSystem = DateFormat.getDateFormat(context) as SimpleDateFormat
//        val sdfDate = SimpleDateFormat(
//            "EEEE, " + sdfSystem.toLocalizedPattern().replace("yy", "yyyy"),
//            Locale.getDefault()
//        )
//        val sdfTime = SimpleDateFormat(if (format24hrs) "HH:mm" else "hh:mm")
//        val sdfSeconds = SimpleDateFormat("ss")
//        mClockText!!.text = sdfTime.format(cal.time)
//        mSecondsText!!.text = sdfSeconds.format(cal.time)
//        mDateText!!.text = sdfDate.format(cal.time)
//        val secRotation =
//            (cal[Calendar.SECOND] + cal[Calendar.MILLISECOND].toFloat() / 1000) * 360 / 60
//        val minRotation = (cal[Calendar.MINUTE] + cal[Calendar.SECOND].toFloat() / 60) * 360 / 60
//        val hrsRotation = (cal[Calendar.HOUR] + cal[Calendar.MINUTE].toFloat() / 60) * 360 / 12
//        mSecondsHand!!.rotation = secRotation
//        mMinutesHand!!.rotation = minRotation
//        mHoursHand!!.rotation = hrsRotation
//    }
//
//    private fun startTimer() {
//        val taskAnalogClock: TimerTask = object : TimerTask() {
//            @SuppressLint("SimpleDateFormat")
//            override fun run() {
//                post { updateClock() }
//            }
//        }
//        val taskCalendarUpdate: TimerTask = object : TimerTask() {
//            override fun run() {
//                post { updateEventView() }
//            }
//        }
//        val taskCheckEvent: TimerTask = object : TimerTask() {
//            override fun run() {
//                post(object : Runnable {
//                    val cal = Calendar.getInstance()
//                    override fun run() {
//                        if (events != null) {
//                            for (e in events) {
//                                if (cal[Calendar.HOUR_OF_DAY] == e.triggerHour && cal[Calendar.MINUTE] == e.triggerMinute && cal[Calendar.SECOND] == 0) {
//                                    doEventStuff(e)
//                                }
//                            }
//                        }
//                    }
//                })
//            }
//        }
//        timerAnalogClock = Timer(false)
//        timerCalendarUpdate = Timer(false)
//        timerCheckEvent = Timer(false)
//        timerAnalogClock!!.schedule(taskAnalogClock, 0, 100)
//        timerCalendarUpdate!!.schedule(taskCalendarUpdate, 0, 10000)
//        timerCheckEvent!!.schedule(taskCheckEvent, 0, 1000)
//    }
//
//    fun loadSettings(a: Activity?) {
//        if (a != null) {
//            if (mSharedPref!!.getBoolean("keep-screen-on", true)) {
//                a.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//                Log.i("SCREEN", "Keep ON")
//            } else {
//                a.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//                Log.i("SCREEN", "Keep OFF")
//            }
//        }
//        val gson = Gson()
//        events = gson.fromJson<Array<Event>>(
//            mSharedPref!!.getString("events", ""),
//            Array<Event>::class.java
//        )
//        format24hrs = mSharedPref!!.getBoolean("24hrs", true)
//        if (mSharedPref!!.getBoolean(
//                "show-analog",
//                true
//            )
//        ) findViewById<View>(R.id.analogClockContainer).visibility =
//            VISIBLE else findViewById<View>(R.id.analogClockContainer).visibility = GONE
//        if (mSharedPref!!.getBoolean(
//                "show-digital",
//                true
//            )
//        ) findViewById<View>(R.id.digitalClockContainer).visibility =
//            VISIBLE else findViewById<View>(R.id.digitalClockContainer).visibility = GONE
//        if (mSharedPref!!.getBoolean("show-date", true)) mDateText!!.visibility = VISIBLE else {
//            mDateText!!.visibility = INVISIBLE
//        }
//        if (!mSharedPref!!.getBoolean(
//                "show-digital",
//                true
//            ) && !mSharedPref!!.getBoolean("show-date", true)
//        ) findViewById<View>(R.id.digitalClockAndDateContainer).visibility =
//            GONE else findViewById<View>(R.id.digitalClockAndDateContainer).visibility = VISIBLE
//        if (mSharedPref!!.getBoolean(
//                "show-seconds-analog",
//                true
//            )
//        ) findViewById<View>(R.id.imageViewClockSecondsHand).visibility =
//            VISIBLE else findViewById<View>(R.id.imageViewClockSecondsHand).visibility = GONE
//        if (mSharedPref!!.getBoolean(
//                "show-seconds-digital",
//                true
//            )
//        ) findViewById<View>(R.id.textViewClockSeconds).visibility =
//            VISIBLE else findViewById<View>(R.id.textViewClockSeconds).visibility = GONE
//
//        // init custom digital color
//        val colorDigital = Color.argb(
//            0xff,
//            mSharedPref!!.getInt("color-red", 255),
//            mSharedPref!!.getInt("color-green", 255),
//            mSharedPref!!.getInt("color-blue", 255)
//        )
//        mClockText!!.setTextColor(colorDigital)
//        mSecondsText!!.setTextColor(colorDigital)
//        mDateText!!.setTextColor(colorDigital)
//        mTextViewEvents!!.setTextColor(colorDigital)
//        mBatteryText!!.setTextColor(colorDigital)
//        mBatteryImage!!.setColorFilter(colorDigital, PorterDuff.Mode.SRC_ATOP)
//
//        // init custom analog color
//        val colorAnalog = Color.argb(
//            0xff,
//            mSharedPref!!.getInt("color-red-analog", 255),
//            mSharedPref!!.getInt("color-green-analog", 255),
//            mSharedPref!!.getInt("color-blue-analog", 255)
//        )
//        if (mSharedPref!!.getBoolean("own-color-analog-clock-face", true)) {
//            mClockBackgroundImage!!.setColorFilter(colorAnalog, PorterDuff.Mode.SRC_ATOP)
//        } else {
//            mClockBackgroundImage!!.clearColorFilter()
//        }
//        if (mSharedPref!!.getBoolean("own-color-analog", true)) {
//            mHoursHand!!.setColorFilter(colorAnalog, PorterDuff.Mode.SRC_ATOP)
//            mMinutesHand!!.setColorFilter(colorAnalog, PorterDuff.Mode.SRC_ATOP)
//        } else {
//            mHoursHand!!.clearColorFilter()
//            mMinutesHand!!.clearColorFilter()
//        }
//        if (mSharedPref!!.getBoolean("own-color-analog-seconds", false)) {
//            mSecondsHand!!.setColorFilter(colorAnalog, PorterDuff.Mode.SRC_ATOP)
//        } else {
//            mSecondsHand!!.clearColorFilter()
//        }
//
//        // init custom background color
//        val colorBack = Color.argb(
//            0xff,
//            mSharedPref!!.getInt("color-red-back", 0),
//            mSharedPref!!.getInt("color-green-back", 0),
//            mSharedPref!!.getInt("color-blue-back", 0)
//        )
//        mRootView!!.setBackgroundColor(colorBack)
//
//        // init custom images
//        if (mSharedPref!!.getBoolean("own-image-back", false)) {
//            val img: File =
//                SettingsActivity.getStorage(context, SettingsActivity.FILENAME_BACKGROUND_IMAGE)
//            if (img.exists()) {
//                try {
//                    val myBitmap = BitmapFactory.decodeFile(img.absolutePath)
//                    mRootView!!.background = BitmapDrawable(myBitmap)
//                } catch (ignored: Exception) {
//                    Toast.makeText(context, "Image corrupted or too large", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            }
//        }
//        if (mSharedPref!!.getBoolean("own-image-analog", false)) {
//            var img: File =
//                SettingsActivity.getStorage(context, SettingsActivity.FILENAME_CLOCK_FACE)
//            if (img.exists()) {
//                try {
//                    val myBitmap = BitmapFactory.decodeFile(img.absolutePath)
//                    mClockBackgroundImage!!.setImageBitmap(myBitmap)
//                } catch (ignored: Exception) {
//                    Toast.makeText(context, "Image corrupted or too large", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            }
//            img = SettingsActivity.getStorage(context, SettingsActivity.FILENAME_HOURS_HAND)
//            if (img.exists()) {
//                try {
//                    val myBitmap = BitmapFactory.decodeFile(img.absolutePath)
//                    mHoursHand!!.setImageBitmap(myBitmap)
//                } catch (ignored: Exception) {
//                    Toast.makeText(context, "Image corrupted or too large", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            }
//            img = SettingsActivity.getStorage(context, SettingsActivity.FILENAME_MINUTES_HAND)
//            if (img.exists()) {
//                try {
//                    val myBitmap = BitmapFactory.decodeFile(img.absolutePath)
//                    mMinutesHand!!.setImageBitmap(myBitmap)
//                } catch (ignored: Exception) {
//                    Toast.makeText(context, "Image corrupted or too large", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            }
//            img = SettingsActivity.getStorage(context, SettingsActivity.FILENAME_SECONDS_HAND)
//            if (img.exists()) {
//                try {
//                    val myBitmap = BitmapFactory.decodeFile(img.absolutePath)
//                    mSecondsHand!!.setImageBitmap(myBitmap)
//                } catch (ignored: Exception) {
//                    Toast.makeText(context, "Image corrupted or too large", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            }
//        } else {
//            mClockBackgroundImage!!.setImageResource(R.drawable.ic_bg)
//            mHoursHand!!.setImageResource(R.drawable.ic_h)
//            mMinutesHand!!.setImageResource(R.drawable.ic_m)
//            mSecondsHand!!.setImageResource(R.drawable.ic_s)
//        }
//    }
//
//    fun doEventStuff(e: Event) {
//        if (e.playAlarm) {
//            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
//            val r = RingtoneManager.getRingtone(context, notification)
//            r.play()
//            val taskEndAlarm: TimerTask = object : TimerTask() {
//                override fun run() {
//                    if (r.isPlaying) r.stop()
//                }
//            }
//            Timer(false).schedule(taskEndAlarm, 10000)
//        }
//        if (e.speakText != null && !e.speakText.trim().equals("")) {
//            speak(e.speakText)
//        }
//    }
//
//    private fun speak(text: String) {
//        if (tts != null) {
//            tts!!.language = Locale.GERMANY
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
//            } else {
//                tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
//            }
//        }
//    }
//
//    @SuppressLint("SetTextI18n")
//    fun updateBattery(plugged: Int, level: Int) {
//        if (plugged == 0 && mSharedPref!!.getBoolean("show-battery-info", true)
//            || plugged != 0 && mSharedPref!!.getBoolean("show-battery-info-when-charging", false)
//        ) {
//            mBatteryText!!.text = "$level%"
//            mBatteryView!!.visibility = VISIBLE
//            if (level < 10) {
//                mBatteryImage!!.setImageResource(R.drawable.ic_baseline_battery_alert_24)
//            } else {
//                mBatteryImage!!.setImageResource(R.drawable.ic_battery_full_black_24dp)
//            }
//        } else {
//            mBatteryView!!.visibility = INVISIBLE
//        }
//    }
//
//    init {
//        commonInit(c)
//    }
//
//    @SuppressLint("SetTextI18n")
//    fun updateEventView() {
//        // clear previous event
//        mTextViewEvents!!.visibility = GONE
//
//        // 1. check app internal events
//        if (events != null) {
//            val calNow = Calendar.getInstance()
//            var lastDiffMins = EVENT_WINDOW_MINUTES.toLong()
//            for (e in events) {
//                if (!e.showOnScreen) continue
//                val calEvent = Calendar.getInstance()
//                calEvent[Calendar.HOUR_OF_DAY] = e.triggerHour
//                calEvent[Calendar.MINUTE] = e.triggerMinute
//                val diffMins = (calEvent.timeInMillis - calNow.timeInMillis) / 1000 / 60
//                // check if event is in current time window
//                if (diffMins > 0 && diffMins < EVENT_WINDOW_MINUTES) {
//                    // if multiple events are in current time window: show nearest event
//                    if (diffMins < lastDiffMins) {
//                        lastDiffMins = diffMins
//                        // show event text
//                        mTextViewEvents!!.visibility = VISIBLE
//                        mTextViewEvents.setText(e.toString())
//                    }
//                }
//            }
//        }
//
//        // 2. check system calendar events
//        if (ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.READ_CALENDAR
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            //SimpleDateFormat sdfLog = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
//            val sdfDisplay = SimpleDateFormat("HH:mm", Locale.getDefault())
//            val start = Calendar.getInstance()
//            val end = Calendar.getInstance()
//            end.time = start.time
//            end[Calendar.HOUR_OF_DAY] = 0
//            end[Calendar.MINUTE] = 0
//            end[Calendar.SECOND] = 0
//            end.add(Calendar.DATE, 1)
//            val selection =
//                "((dtstart >= " + start.timeInMillis + ") AND (dtend <= " + end.timeInMillis + "))"
//            //Log.e("EVENT", sdfLog.format(start.getTime()));
//            //Log.e("EVENT", sdfLog.format(end.getTime()));
//            val cursor = context.contentResolver.query(
//                Uri.parse("content://com.android.calendar/events"),
//                arrayOf("calendar_id", "title", "description", "dtstart", "dtend", "eventLocation"),
//                selection,
//                null,
//                CalendarContract.Events.DTSTART + " ASC"
//            ) ?: return
//            cursor.moveToFirst()
//            val length = cursor.count
//            for (i in 0 until length) {
//                mTextViewEvents!!.visibility = VISIBLE
//                mTextViewEvents!!.text =
//                    sdfDisplay.format(cursor.getLong(3)) + " " + cursor.getString(1)
//                //Log.e("EVENT",cursor.getString(1) + " "+sdfLog.format(cursor.getLong(3)));
//                cursor.moveToNext()
//                break
//            }
//            cursor.close()
//        }
//    }
//
//    protected fun pause() {
//        timerAnalogClock!!.cancel()
//        timerAnalogClock!!.purge()
//        timerCalendarUpdate!!.cancel()
//        timerCalendarUpdate!!.purge()
//        timerCheckEvent!!.cancel()
//        timerCheckEvent!!.purge()
//    }
//
//    protected fun resume() {
//        loadSettings(mActivity)
//        startTimer()
//    }
//
//    companion object {
//        fun getTextWidth(
//            context: Context?,
//            text: CharSequence?,
//            textSize: Int,
//            deviceSize: Point,
//            typeface: Typeface?
//        ): Int {
//            val textView = TextView(context)
//            textView.setTypeface(typeface)
//            textView.setText(text, TextView.BufferType.SPANNABLE)
//            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
//            val widthMeasureSpec = MeasureSpec.makeMeasureSpec(deviceSize.x, MeasureSpec.AT_MOST)
//            val heightMeasureSpec =
//                MeasureSpec.makeMeasureSpec(deviceSize.y, MeasureSpec.UNSPECIFIED)
//            textView.measure(widthMeasureSpec, heightMeasureSpec)
//            return textView.measuredWidth
//        }
//
//        private const val EVENT_WINDOW_MINUTES = 60
//    }
//}*/
