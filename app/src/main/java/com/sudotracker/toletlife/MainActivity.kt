package com.sudotracker.toletlife


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sudotracker.toletlife.Error.ErrorResponse
import com.sudotracker.toletlife.Error.ValidationErrorResponse
import com.sudotracker.toletlife.Requests.OtpRequest
import com.sudotracker.toletlife.Responses.OtpResponse
import com.sudotracker.toletlife.Services.IdentityService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val et_email: EditText = findViewById(R.id.et_email)
        et_email.setHint("Email")
        val et_password: EditText = findViewById(R.id.et_password)
        et_password.setHint("Password")
        val btn_login: Button = findViewById(R.id.btn_login)
        val btn_send_otp: Button = findViewById(R.id.btn_send_otp)
        val et_send_otp_email: EditText = findViewById(R.id.et_send_otp_email)

        btn_login.setOnClickListener {
            val intent = Intent(this, RentalOptions::class.java)
            startActivity(intent)
        }


        btn_send_otp.setOnClickListener {
            val email = et_send_otp_email.text
            et_send_otp_email.setText("")
            if(email.toString() != ""){
                sendOtp(email.toString())
            }
        }
    }

    private fun sendOtp(email: String) {
        val otpRequest = OtpRequest(email)
        val call = IdentityService.identityInstance.sendOtp(otpRequest)
        val intent = Intent(this@MainActivity, RegisterActivity::class.java)
        val gson = Gson()
        call.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.code() == 422) {
                    val type = object : TypeToken<ValidationErrorResponse>() {}.type
                    var errorResponse: ValidationErrorResponse? =
                        gson.fromJson(response.errorBody()?.charStream(), type)
                    Toast.makeText(this@MainActivity,errorResponse?.detail?.first()?.msg.toString(),Toast.LENGTH_LONG).show()
                    return
                } else if (response.code() > 399) {
                    val type = object : TypeToken<ErrorResponse>() {}.type
                    var errorResponse: ErrorResponse? =
                        gson.fromJson(response.errorBody()?.charStream(), type)
                    if(errorResponse?.detail.toString().contains("Otp already sent to")){
                        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        Toast.makeText(this@MainActivity,errorResponse?.detail.toString(),Toast.LENGTH_LONG).show()
                        startActivity(intent)
                        return
                    }
                    Toast.makeText(this@MainActivity,errorResponse?.detail.toString(),Toast.LENGTH_LONG).show()
                    return
                }else if (response.code() == 200) {
                    var jsonResponse = gson.toJson(response.body())
                    var resp: OtpResponse = gson.fromJson(jsonResponse,OtpResponse::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    Toast.makeText(this@MainActivity,resp.otpResponse,Toast.LENGTH_LONG).show()
                    startActivity(intent)
                    return
                }
            }
            override fun onFailure(call: Call<Any>, t: Throwable) {
                Log.d("failure", "Error in failure", t)
            }
        })
    }
}