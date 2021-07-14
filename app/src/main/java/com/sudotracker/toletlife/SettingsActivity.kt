package com.sudotracker.toletlife

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sudotracker.toletlife.Error.ErrorResponse
import com.sudotracker.toletlife.Error.ValidationErrorResponse
import com.sudotracker.toletlife.Requests.UserProfileUpdateRequest
import com.sudotracker.toletlife.Responses.UserProfileDetailsResponse
import com.sudotracker.toletlife.Services.UserProfileService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        progressBarVisibility(false)
        setBottomNavigationBarProperties()
        val userProfileDetailsString = loadUserProfileDetails()
        val gson = Gson()
        val userProfileDetails: UserProfileDetailsResponse =
            gson.fromJson(userProfileDetailsString, UserProfileDetailsResponse::class.java)

        val et_settings_user_name: EditText = findViewById(R.id.et_settings_user_name)
        val et_settings_user_email: EditText = findViewById(R.id.et_settings_user_email)
        val et_settings_phone_number: EditText = findViewById(R.id.et_settings_phone_number)
        val et_settings_delete_account: EditText = findViewById(R.id.et_settings_delete_account)
        val btn_update_settings: Button = findViewById(R.id.btn_update_settings)
        val btn_move_to_feedback: Button = findViewById(R.id.btn_move_to_feedback)
        val btn_log_out: Button = findViewById(R.id.btn_log_out)
        val btn_delete_account: Button = findViewById(R.id.btn_delete_account)

        et_settings_user_email.isFocusable = false
        et_settings_user_email.inputType = InputType.TYPE_NULL
        et_settings_user_email.isClickable = true
        et_settings_user_email.setOnClickListener(View.OnClickListener {
            Toast.makeText(this,"User can not change the email",Toast.LENGTH_LONG).show()
        })



        et_settings_user_name.setHint(userProfileDetails.name)
        et_settings_user_email.setHint(userProfileDetails.email)
        et_settings_phone_number.setHint(userProfileDetails.phoneNumber)

        btn_update_settings.setOnClickListener {
            val name = et_settings_user_name.text.toString()
            val phone_number = et_settings_phone_number.text.toString()

            val phone = "+91${phone_number}"
            updateUserProfile(userId = userProfileDetails.userId,email = userProfileDetails.email,name=name,phoneNumber=phone) //todo
        }
        btn_move_to_feedback.setOnClickListener { //todo
            val intent = Intent(this, FeedbackActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn_log_out.setOnClickListener {
            saveToken(null)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        btn_delete_account.setOnClickListener {
            val delete = et_settings_delete_account.text.toString().lowercase()
            if (delete != "delete") {
                Toast.makeText(this, "Please type delete for the confirmation", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            deleteUser(delete)
        }


    }
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        currentFocus?.let {
            val imm: InputMethodManager = getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as (InputMethodManager)
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
    private fun deleteUser(deleteString: String){
        val jwtToken = loadJWTTokenData()
        progressBarVisibility(true)
        val call =
            UserProfileService.userProfileInstance.deleteUserProfileDetails(
                deleteString = deleteString,
                token = "Bearer $jwtToken"
            )
        val gson = Gson()
        call.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.code() == 422) {
                    val type = object : TypeToken<ValidationErrorResponse>() {}.type
                    val errorResponse: ValidationErrorResponse? =
                        gson.fromJson(response.errorBody()?.charStream(), type)
                    Toast.makeText(
                        this@SettingsActivity,
                        errorResponse?.detail?.first()?.msg.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    progressBarVisibility(false)
                    return
                } else if (response.code() > 399) {
                    val type = object : TypeToken<ErrorResponse>() {}.type
                    val errorResponse: ErrorResponse? =
                        gson.fromJson(response.errorBody()?.charStream(), type)
                    Toast.makeText(
                        this@SettingsActivity,
                        errorResponse?.detail.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    progressBarVisibility(false)
                    return
                } else if (response.code() == 200) {
                    saveToken(null)
                    Toast.makeText(this@SettingsActivity,"Your account is deleted",Toast.LENGTH_LONG).show()
                    val intent = Intent(this@SettingsActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    return
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Toast.makeText(
                    this@SettingsActivity,
                    "Could not connect to internet. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("failure", "Error in failure", t)
            }

        })
    }
    private fun updateUserProfile(userId: String,email: String,name: String,phoneNumber: String){
        progressBarVisibility(true)
        val et_settings_user_name: EditText = findViewById(R.id.et_settings_user_name)
        val et_settings_phone_number: EditText = findViewById(R.id.et_settings_phone_number)
        val jwtToken = loadJWTTokenData()
        val userProfileUpdatedData = UserProfileUpdateRequest(name=name,phoneNumber=phoneNumber)
        val call =
            UserProfileService.userProfileInstance.updateUserProfileDetails(
                userProfileUpdatedData,
                token = "Bearer $jwtToken"
            )
        val gson = Gson()
        call.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.code() == 422) {
                    val type = object : TypeToken<ValidationErrorResponse>() {}.type
                    val errorResponse: ValidationErrorResponse? =
                        gson.fromJson(response.errorBody()?.charStream(), type)
                    Toast.makeText(
                        this@SettingsActivity,
                        errorResponse?.detail?.first()?.msg.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    progressBarVisibility(false)
                    return
                } else if (response.code() > 399) {
                    val type = object : TypeToken<ErrorResponse>() {}.type
                    val errorResponse: ErrorResponse? =
                        gson.fromJson(response.errorBody()?.charStream(), type)
                    Toast.makeText(
                        this@SettingsActivity,
                        errorResponse?.detail.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    progressBarVisibility(false)
                    return
                } else if (response.code() == 200) {
                    if(name != "") {
                        et_settings_user_name.setHint(name)
                    }
                    if(phoneNumber != "+91") {
                        et_settings_phone_number.setHint(phoneNumber)
                    }
                    if(name != "" && phoneNumber != "+91"){
                        val response = UserProfileDetailsResponse(userId = userId,name = name,email = email,phoneNumber = phoneNumber)
                        val jsonResponse = gson.toJson(response)
                        saveUserProfileDetails(jsonResponse)
                    }else if(name != ""){
                        val oldPhoneNumber = et_settings_phone_number.hint.toString()
                        val response = UserProfileDetailsResponse(userId = userId,name = name,email = email,phoneNumber = oldPhoneNumber)
                        val jsonResponse = gson.toJson(response)
                        saveUserProfileDetails(jsonResponse)
                    }else if(phoneNumber != "+91"){
                        val oldName = et_settings_user_name.hint.toString()
                        val response = UserProfileDetailsResponse(userId = userId,name = oldName,email = email,phoneNumber = phoneNumber)
                        val jsonResponse = gson.toJson(response)
                        saveUserProfileDetails(jsonResponse)
                    }
                    Toast.makeText(this@SettingsActivity, "Profile details are updated", Toast.LENGTH_LONG)
                        .show()
                    progressBarVisibility(false)
                    return
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Toast.makeText(
                    this@SettingsActivity,
                    "Could not connect to internet. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("failure", "Error in failure", t)
            }

        })

    }

    private fun loadUserProfileDetails(): String? {
        val sharedPreferences = getSharedPreferences("userProfileDetails", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userProfileDetails", null)
    }
    private fun saveUserProfileDetails(response: String) {
        val sharedPreferences = getSharedPreferences("userProfileDetails", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply {
            putString("userProfileDetails",response)
        }.apply()
    }


    private fun loadJWTTokenData(): String? {
        val sharedPreferences = getSharedPreferences("jwtToken", Context.MODE_PRIVATE)
        return sharedPreferences.getString("JWT_TOKEN", null)
    }

    private fun saveToken(token: String?) {
        val sharedPreferences = getSharedPreferences("jwtToken", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply {
            putString("JWT_TOKEN", token)
        }.apply()
    }

    private fun setBottomNavigationBarProperties() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.background = null
        bottomNavigationView.selectedItemId = R.id.menu_settings
        bottomNavigationView.menu.getItem(2).isEnabled = false
        val fab: FloatingActionButton = findViewById(R.id.fab)

        bottomNavigationView.menu.getItem(0).setOnMenuItemClickListener {
            val intent = Intent(this, RentalOptions::class.java)
            startActivity(intent)
            finish()
            return@setOnMenuItemClickListener true
        }
        bottomNavigationView.menu.getItem(1).setOnMenuItemClickListener {
            val intent = Intent(this, SearchRentDetails::class.java)
            startActivity(intent)
            finish()
            return@setOnMenuItemClickListener true
        }
        bottomNavigationView.menu.getItem(3).setOnMenuItemClickListener {
            val intent = Intent(this, UserRentListActivity::class.java)
            startActivity(intent)
            finish()
            return@setOnMenuItemClickListener true
        }
        bottomNavigationView.menu.getItem(4).setOnMenuItemClickListener {
            return@setOnMenuItemClickListener true
        }
        fab.setOnClickListener {
            val intent = Intent(this, CreateRentActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
    private fun progressBarVisibility(visibility: Boolean) {
        val progress: ProgressBar =
            findViewById(R.id.settings_activity_circular_progress_load)
        if (visibility) {
            progress.visibility = View.VISIBLE
        } else {
            progress.visibility = View.GONE
        }
    }
}