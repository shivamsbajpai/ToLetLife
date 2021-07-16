package com.sudotracker.toletlife

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.isVisible
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sudotracker.toletlife.Error.ErrorResponse
import com.sudotracker.toletlife.Error.ValidationErrorResponse
import com.sudotracker.toletlife.Requests.RegisterRequest
import com.sudotracker.toletlife.Services.IdentityService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val et_register_name: EditText = findViewById(R.id.et_register_name)
        val et_register_phone_number: EditText = findViewById(R.id.et_register_phone_number)
        val et_register_email: EditText = findViewById(R.id.et_register_email)
        val et_register_otp: EditText = findViewById(R.id.et_register_otp)
        val et_register_password: EditText = findViewById(R.id.et_register_password)
        val et_register_confirm_password: EditText = findViewById(R.id.et_register_confirm_password)
        val tv_register_error: TextView = findViewById(R.id.tv_register_error)
        val btn_register: Button = findViewById(R.id.btn_register)

        et_register_phone_number.setText("+91")
        progressBarVisibility(false)

        val intent_email = intent.getStringExtra("email")
        et_register_email.setText(intent_email)

        btn_register.setOnClickListener {
            tv_register_error.isVisible = false
            val name = et_register_name.text.toString()
            val phone_number = et_register_phone_number.text.toString()
            val email = et_register_email.text.toString()
            val otp_code = et_register_otp.text.toString()
            val password = et_register_password.text.toString()
            val confirm_password = et_register_confirm_password.text.toString()
            et_register_password.setText("")
            et_register_confirm_password.setText("")
            if (name == "" || phone_number == "" || email == "" || otp_code == "" || password == "" || confirm_password == "") {
                Toast.makeText(this, "Fields should not be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (password != confirm_password) {
                tv_register_error.text = "Passwords do not match"
                tv_register_error.isVisible = true
                return@setOnClickListener
            }
            registerUser(name, phone_number, email, otp_code, password)
        }
    }

    //function to hide the keyboard
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        currentFocus?.let {
            val imm: InputMethodManager = getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as (InputMethodManager)
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun registerUser(
        name: String,
        phone_number: String,
        email: String,
        otp_code: String,
        password: String
    ) {
        val registerRequest = RegisterRequest(name, phone_number, email, otp_code, password)
        val call = IdentityService.identityInstance.registerUser(registerRequest)
        val intent = Intent(this, MainActivity::class.java)
        val gson = Gson()
        progressBarVisibility(true)
        call.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.code() == 422) {
                    progressBarVisibility(false)
                    val type = object : TypeToken<ValidationErrorResponse>() {}.type
                    val errorResponse: ValidationErrorResponse? =
                        gson.fromJson(response.errorBody()?.charStream(), type)
                    Toast.makeText(
                        this@RegisterActivity,
                        errorResponse?.detail?.first()?.msg.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    return
                } else if (response.code() > 399) {
                    progressBarVisibility(false)
                    val type = object : TypeToken<ErrorResponse>() {}.type
                    val errorResponse: ErrorResponse? =
                        gson.fromJson(response.errorBody()?.charStream(), type)
                    Toast.makeText(
                        this@RegisterActivity,
                        errorResponse?.detail.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    return
                } else if (response.code() == 201) {
                    progressBarVisibility(false)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    Toast.makeText(
                        this@RegisterActivity,
                        "Registration Successful",
                        Toast.LENGTH_LONG
                    ).show()
                    intent.putExtra("email", email)
                    startActivity(intent)
                    finish()
                    return
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                progressBarVisibility(false)
                Toast.makeText(
                    this@RegisterActivity,
                    "Could not connect to internet. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("failure", "Error in failure", t)
            }
        })
    }

    private fun progressBarVisibility(visibility: Boolean) {
        val progress: ProgressBar = findViewById(R.id.register_activity_circular_progress_load)
        if (visibility) {
            progress.visibility = View.VISIBLE
        } else {
            progress.visibility = View.GONE
        }
    }
}