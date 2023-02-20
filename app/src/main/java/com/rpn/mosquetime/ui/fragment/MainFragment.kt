package com.rpn.mosquetime.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rpn.mosquetime.R
import com.rpn.mosquetime.databinding.FragmentMainBinding
import com.rpn.mosquetime.extensions.convertToList
import com.rpn.mosquetime.extensions.convertToString
import com.rpn.mosquetime.extensions.extractFileNameFromImageUri
import com.rpn.mosquetime.repository.FireStoreRepository
import com.rpn.mosquetime.ui.activity.LoginActivity
import com.rpn.mosquetime.utils.DownloadImageTask
import com.rpn.mosquetime.utils.GeneralUtils.moreDialog
import com.rpn.mosquetime.utils.SettingsUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.*


class MainFragment : CoroutineFragment() {
    val settingsUtility by inject<SettingsUtility>()
    val TAG = "MainFragment"
    private var _binding: FragmentMainBinding? = null

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        val root: View = binding.root
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onActivityCreated(savedInstanceState)



        binding.let {
            it.viewModel = mainViewModel
            it.executePendingBindings()
            it.lifecycleOwner = viewLifecycleOwner
            it.tvMessage.isSelected = true
        }
        mainViewModel.currentTimeSecond.observe(viewLifecycleOwner, Observer {
            val aniSlide: Animation = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.side_down
            )
            binding.tvTimeSecond.text = it
            binding.tvTimeSecond.startAnimation(aniSlide)
        })
        mainViewModel.currentTimeMinute.observe(viewLifecycleOwner, Observer {
            val aniSlide: Animation = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.side_down
            )
            binding.tvTimeMinute.text = it
            binding.tvTimeMinute.startAnimation(aniSlide)
        })
        mainViewModel.currentTimeHour.observe(viewLifecycleOwner, Observer {
            val aniSlide: Animation = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.side_down
            )
            binding.tvTimeHour.text = it
            binding.tvTimeHour.startAnimation(aniSlide)
        })
        binding.btnMore.setOnClickListener {
            requireContext().moreDialog { dialog, bindingView ->
                bindingView.btnLogout.setOnClickListener {
                    Toast.makeText(requireContext(), "Logout", Toast.LENGTH_SHORT).show()
                    loginRegisterViewModel.logOut()
                    updateUiWithOutUser()
                    dialog.dismiss()
                }
                bindingView.btnChangeFormat.setOnClickListener {
                    Toast.makeText(
                        requireContext(),
                        "Change time 24 hour format ${!settingsUtility.is24HourFormat}",
                        Toast.LENGTH_SHORT
                    ).show()
                    mainViewModel.changeTimeFormat(!settingsUtility.is24HourFormat)
                    settingsUtility.is24HourFormat = !settingsUtility.is24HourFormat
                    dialog.dismiss()
                }

            }
        }


        loginRegisterViewModel.userLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                if (it != null) {
                    Log.d(TAG, "onActivityCreated: reqest for mosque")
                    settingsUtility.userId = it.uid
                    mainViewModel.getMyMosquebyOwnerId {
                        mainViewModel.setTodayMosqueTime()
                    }
                }
            })

        handleUI()
        imageSaveForOffline()
    }


    fun handleUI() {


        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                mainViewModel.uiState
                    .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                    .collect { uiState ->
                        if (uiState.userMessage != null) {
                            try {

                                val amount = uiState.userMessage
                                val bundle = bundleOf("message" to amount)
                                findNavController().navigate(
                                    R.id.action_navigation_main_to_messageFragment,
                                    bundle
                                )

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
            }
        }
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

    fun imageSaveForOffline() {

        mainViewModel.imgMsg.observe(viewLifecycleOwner, Observer { imgMessage ->
            settingsUtility.messageImages = imgMessage.convertToString()
            //checking file is empty or not
           mainViewModel.imgMsgDownloaded.clear()
            if (imgMessage.isNotEmpty()) {
                if (!verifyPermissions()) {
                    return@Observer
                }

                var imageList = ""
                imgMessage?.forEach {
                    imageList += "${it?.extractFileNameFromImageUri()}\n"
                }
                Log.d(TAG, "imageSaveForOffline: fetchImage is \n" + "$imageList")
                CoroutineScope(Main).launch {
                    for (i in 0..imgMessage.size - 1) {
                        //checking File is avilable in Storage or not
                        var downloadedImage = async {
                            val fileName = imgMessage[i]?.extractFileNameFromImageUri()
                            DownloadImageTask().isImageIsExist(
                                requireContext(),
                                fileName.toString()
                            )
                        }.await()
                        downloadedImage =
                            if ((imgMessage[i]?.contains("file:///")!!) && downloadedImage.isEmpty()) imgMessage[i].toString() else downloadedImage

                        if (downloadedImage.isEmpty()) {
                            //Downloading Image by volley
                            val file = async {
                                DownloadImageTask().imageDownload(
                                    requireContext(),
                                    imgMessage[i].toString()
                                )
                            }.await()

                            if (file.isNotEmpty()) {
                                mainViewModel.imgMsgDownloaded.add(file)
                                mainViewModel.imgMsg.value?.set(i, file)
                            }
                        } else {
                            mainViewModel.imgMsgDownloaded.add(downloadedImage)
                            mainViewModel.imgMsg.value?.set(i, downloadedImage)
                        }
                    }

                    settingsUtility.messageImages = mainViewModel.imgMsg.value.convertToString()


                }


            }
        })

    }

    fun verifyPermissions(): Boolean {

        // This will return the current Status
        val permissionExternalMemory =
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        if (permissionExternalMemory != PackageManager.PERMISSION_GRANTED) {
            val STORAGE_PERMISSIONS = arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            // If permission not granted then ask for permission real time.
            ActivityCompat.requestPermissions(requireActivity(), STORAGE_PERMISSIONS, 1)
            return false
        }
        return true
    }


    companion object {
        val CHANNEL_ID = "4747"
    }

}