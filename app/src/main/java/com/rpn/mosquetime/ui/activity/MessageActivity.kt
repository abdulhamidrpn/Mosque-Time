package com.rpn.mosquetime.ui.activity

import android.app.UiModeManager
import android.content.*
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.rpn.mosquetime.R
import com.rpn.mosquetime.databinding.FragmentMessageBinding
import com.rpn.mosquetime.ui.viewmodel.MainViewModel
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import java.util.*

class MessageActivity : FragmentActivity(), KoinComponent {

    val mainViewModel by inject<MainViewModel>()

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = FragmentMessageBinding.inflate(layoutInflater)
        val root: View = binding.root
        setContentView(root)



        binding.let {
            it.viewModel = mainViewModel
            it.executePendingBindings()
            it.lifecycleOwner = this
        }


    }


}