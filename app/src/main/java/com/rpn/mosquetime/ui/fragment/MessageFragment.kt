package com.rpn.mosquetime.ui.fragment

import android.app.UiModeManager
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.rpn.mosquetime.databinding.FragmentMessageBinding
import com.rpn.mosquetime.extensions.extractFileNameFromImageUri
import com.rpn.mosquetime.ui.activity.LoginActivity
import com.rpn.mosquetime.utils.SettingsUtility
import org.koin.android.ext.android.inject
import java.util.*


class MessageFragment : CoroutineFragment() {

    private val mHandler = Handler()
    val settingsUtility by inject<SettingsUtility>()


    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!
    var message = ""
    var uiModeManager: UiModeManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)

        val root: View = binding.root
        return root
    }

    fun loadImage(index: Int, lastMinute: Boolean = false) {
        try {
            if (lastMinute == false) {
                goBackInOneMinute()
                binding.countDownTimer.visibility = View.GONE
                binding.containerTime.visibility = View.VISIBLE
            } else {
                binding.containerTime.visibility = View.GONE
                binding.countDownTimer.visibility = View.VISIBLE
            }

            val displayMetrics = DisplayMetrics()
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            val width = displayMetrics.widthPixels
            val requestOptions = RequestOptions()
                .override(width, height)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .centerCrop()


            val currentImage = mainViewModel.imgMsg.value?.get(index)
            var imageList = ""
            mainViewModel.imgMsg.value?.forEach {
                imageList += "${it?.extractFileNameFromImageUri()}\n"
            }

            Log.d(
                TAG, "loadImage is \n" +
                        "index -> $index\n" +
                        "currentImage -> ${currentImage}\n" +
                        "imageList ->\n" +
                        "$imageList"
            )

            uiModeManager =
                requireActivity().getSystemService(FragmentActivity.UI_MODE_SERVICE) as UiModeManager


            Glide.with(requireActivity())
                .asBitmap()
                .load(currentImage)
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.signatureOf(ObjectKey(System.currentTimeMillis().toString())))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA))
                .into(binding.imageView)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate ${mainViewModel.imgMsg.value}")
        super.onActivityCreated(savedInstanceState)



        message = requireArguments().getString("message").toString()

        when (message) {
            "1" -> {
                loadImage(0)
            }
            "2" -> {
                loadImage(1)
            }
            "3" -> {
                loadImage(2)
            }
            "4" -> {
                loadImage(3)
            }
            "5" -> {
                loadImage(4)
            }
            "6" -> {
                loadImage(5)
            }
            "7" -> {
                //Show last minute countdown

                object : CountDownTimer(60000, 1000) {

                    override fun onTick(millisUntilFinished: Long) {
                        binding.tvCountdown.setText("" + millisUntilFinished / 1000)
                    }

                    override fun onFinish() {
                        gotoMainFragment()
                    }
                }.start()

                loadImage(6, true)
            }
        }

        binding.let {
            it.viewModel = mainViewModel
            it.executePendingBindings()
            it.lifecycleOwner = viewLifecycleOwner
        }


    }

    fun goBackInOneMinute() {
        mTimer = Timer()
        mTimer?.schedule(TimeDisplayTimerTask(), 60000)
//        mTimer!!.scheduleAtFixedRate(TimeDisplayTimerTask(), 60000)
    }

    private var mTimer: Timer? = null

    internal inner class TimeDisplayTimerTask : TimerTask() {
        override fun run() {
            mHandler.post {
                gotoMainFragment()
            }

        }
    }

    fun gotoMainFragment() {
        mainViewModel.userMessageShown()
        findNavController().popBackStack()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    companion object {
        private val TAG = "MessageFragment"
    }


    override fun onStart() {
        super.onStart()
        if (settingsUtility.userId == "" && settingsUtility.userId.isEmpty()) {
            updateUiWithOutUser()
        }
    }


    private fun updateUiWithOutUser() {
        settingsUtility.clearAllData()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }
}