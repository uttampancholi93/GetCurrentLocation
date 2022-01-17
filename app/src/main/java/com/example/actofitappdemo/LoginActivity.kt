package com.example.actofitappdemo

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.actofitappdemo.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    private val sharedPrefFile = "kotlinsharedpreference"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        //permissionStatus = getSharedPreferences("permissionStatus", Context.MODE_PRIVATE)
        initView()
    }

    private fun initView() {
        initListner()
    }

    private fun initListner() {
        binding.btnLogin.setOnClickListener {
            loginValidation()
        }
    }

    private fun loginValidation() {
        val userName = binding.etUserName.getText().toString().trim()
        var mobileNumber = binding.etMobileNumber.getText().toString().trim()

        if (userName.length == 0) {
            binding.etUserName.setError("User Name required!")
            binding.etUserName.requestFocus()
        } else if (mobileNumber.length == 0) {
            binding.etMobileNumber.setError("Mobile Number required!")
            binding.etMobileNumber.requestFocus()
        } else if (mobileNumber.length != 10) {
            binding.etMobileNumber.setError("10 Digits Mobile Number required!")
        } else {
            goToNextScreen(userName, mobileNumber)
        }
    }

    private fun goToNextScreen(userName: String, mobileNumber: String) {
        intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        val sharedpreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor = sharedpreferences.edit()
        editor.putBoolean("isLogin", true)
        editor.putString("userName", userName)
        editor.putString("mobileNumber", mobileNumber)
        editor.apply()
        finish()
    }
}