package com.rpn.mosquetime.ui.activity


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rpn.mosquetime.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    val TAG = "AuthTAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}