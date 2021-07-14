package com.sudotracker.toletlife

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sudotracker.toletlife.Error.ErrorResponse
import com.sudotracker.toletlife.Error.ValidationErrorResponse
import com.sudotracker.toletlife.Requests.FeedbackRequest
import com.sudotracker.toletlife.Requests.LoginRequest
import com.sudotracker.toletlife.Responses.LoginResponse
import com.sudotracker.toletlife.Services.FeedbackService
import com.sudotracker.toletlife.Services.IdentityService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedbackActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        progressBarVisibility(false)
        setBottomNavigationBarProperties()

        val et_feedback_name:EditText = findViewById(R.id.et_feedback_name)
        val et_feedback_email:EditText = findViewById(R.id.et_feedback_email)
        val et_feedback_detail:EditText = findViewById(R.id.et_feedback_detail)

        val btn_send_feedback:Button = findViewById(R.id.btn_send_feedback)

        btn_send_feedback.setOnClickListener {
            val name = et_feedback_name.text.toString()
            val email = et_feedback_email.text.toString()
            val feedback = et_feedback_detail.text.toString()

            if (name == "" || email == "" || feedback == "") {
                Toast.makeText(this, "Fields should not be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            sendFeedback(name=name,email=email,feedback=feedback)
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

    private fun sendFeedback(name: String,email: String, feedback: String) {
        val feedbackRequest = FeedbackRequest(name = name,email = email, feedback = feedback)
        val call = FeedbackService.feedbackInstance.sendFeedback(feedbackRequest)
        val gson = Gson()
        progressBarVisibility(true)
        call.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.code() == 422) {
                    val type = object : TypeToken<ValidationErrorResponse>() {}.type
                    val errorResponse: ValidationErrorResponse? =
                        gson.fromJson(response.errorBody()?.charStream(), type)
                    Toast.makeText(
                        this@FeedbackActivity,
                        errorResponse?.detail?.first()?.msg.toString(),
                        Toast.LENGTH_LONG
                    ).show()

                    return
                } else if (response.code() > 399) {
                    val type = object : TypeToken<ErrorResponse>() {}.type
                    val errorResponse: ErrorResponse? =
                        gson.fromJson(response.errorBody()?.charStream(), type)
                    Toast.makeText(
                        this@FeedbackActivity,
                        errorResponse?.detail.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    return
                } else if (response.code() == 200) {
                    Toast.makeText(this@FeedbackActivity,"Thank you for sharing your valuable feedback.",Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(
                        this@FeedbackActivity,
                        "Could not connect to internet. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Log.i("progress","check this")
                progressBarVisibility(false)
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                progressBarVisibility(false)
                Log.d("failure", "Error in failure login", t)
                Toast.makeText(
                    this@FeedbackActivity,
                    "Could not connect to internet. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }

        })

    }

    private fun progressBarVisibility(visibility: Boolean) {
        val progress: ProgressBar =
            findViewById(R.id.feedback_activity_circular_progress_load)
        if (visibility) {
            progress.visibility = View.VISIBLE
        } else {
            progress.visibility = View.GONE
        }
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
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
            return@setOnMenuItemClickListener true
        }


        fab.setOnClickListener {
            val intent = Intent(this, CreateRentActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}