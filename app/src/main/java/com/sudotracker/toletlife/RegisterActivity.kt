package com.sudotracker.toletlife

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
        call.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.code() == 422) {
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
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    Toast.makeText(this@RegisterActivity, "Registration Successful", Toast.LENGTH_LONG).show()
                    intent.putExtra("email",email)
                    startActivity(intent)
                    finish()
                    return
                }
            }
            override fun onFailure(call: Call<Any>, t: Throwable) {
                Log.d("failure", "Error in failure", t)
            }
        })
    }
}