package com.sudotracker.toletlife

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.isVisible
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sudotracker.toletlife.Error.ErrorResponse
import com.sudotracker.toletlife.Error.ValidationErrorResponse
import com.sudotracker.toletlife.Requests.RegisterRequest
import com.sudotracker.toletlife.Requests.UserCreateRentRequest
import com.sudotracker.toletlife.Responses.OtpResponse
import com.sudotracker.toletlife.Responses.UserCreateRentResponse
import com.sudotracker.toletlife.Services.IdentityService
import com.sudotracker.toletlife.Services.UserRentService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateRentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_rent)
        progressBarVisibility(false)
        setBottomNavigationBarProperties()

        val btn_create_rent_upload_images: Button = findViewById(R.id.btn_create_rent_upload_images)

        val et_create_rent_address: EditText = findViewById(R.id.et_create_rent_address)
        val et_create_rent_area: EditText = findViewById(R.id.et_create_rent_area)
        val et_create_rent_city: EditText = findViewById(R.id.et_create_rent_city)
        val et_create_rent_pincode: EditText = findViewById(R.id.et_create_rent_pincode)
        val et_create_rent_state: EditText = findViewById(R.id.et_create_rent_state)

        //intent values
        val product_name = intent.getStringExtra("product_name").toString()
        val product_category = intent.getStringExtra("product_category").toString()
        val product_description = intent.getStringExtra("product_description").toString()
        val security_deposit = intent.getStringExtra("security_deposit").toString()
        val monthly_rent = intent.getStringExtra("monthly_rent").toString()
        val statusId = "3cb9559b-fe39-4e6b-9f58-25ed7c18003f"


        btn_create_rent_upload_images.setOnClickListener {
            val address = et_create_rent_address.text.toString()
            val area = et_create_rent_area.text.toString()
            val city = et_create_rent_city.text.toString()
            val pincode = et_create_rent_pincode.text.toString()
            val state = et_create_rent_state.text.toString()
            if (address == "" || area == "" || city == "" || pincode == "" || state == "") {
                Toast.makeText(this, "Fields should not be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            userCreateRent(
                product_name = product_name,
                product_category = product_category,
                product_description = product_description,
                security_deposit = security_deposit,
                monthly_rent = monthly_rent,
                pincode = pincode,
                address = address,
                area = area,
                city = city,
                state = state,
                status_id = statusId
            )
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

    private fun userCreateRent(
        product_name: String,
        product_category: String,
        product_description: String,
        security_deposit: String,
        monthly_rent: String,
        pincode: String,
        address: String,
        area: String,
        city: String,
        state: String,
        status_id: String
    ) {

        val createRent = UserCreateRentRequest(
            productName = product_name,
            productCategoryId = product_category,
            productDescription = product_description,
            securityDeposit = security_deposit,
            monthlyRent = monthly_rent,
            pincode = pincode,
            address = address,
            area = area,
            city = city,
            state = state,
            statusId = status_id
        )
        val jwtToken = loadJWTTokenData()
        val call = UserRentService.userRentInstance.userCreateRent(createRent, "Bearer $jwtToken")
        val intent = Intent(this, UploadImageActivity::class.java)
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
                        this@CreateRentActivity,
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
                        this@CreateRentActivity,
                        errorResponse?.detail.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    return
                } else if (response.code() == 201) {
                    progressBarVisibility(false)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    val jsonResponse = gson.toJson(response.body())
                    val resp: UserCreateRentResponse =
                        gson.fromJson(jsonResponse, UserCreateRentResponse::class.java)
                    intent.putExtra("rent_id", resp.rentId)
                    startActivity(intent)
                    finish()
                    return
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                progressBarVisibility(false)
                Toast.makeText(
                    this@CreateRentActivity,
                    "Could not connect to internet. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("failure", "Error in failure", t)
            }
        })
    }

    private fun progressBarVisibility(visibility: Boolean) {
        val progress: ProgressBar = findViewById(R.id.create_rent_activity_circular_progress_load)
        if (visibility) {
            progress.visibility = View.VISIBLE
        } else {
            progress.visibility = View.GONE
        }
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
        bottomNavigationView.selectedItemId = R.id.fab
        bottomNavigationView.selectedItemId = R.id.placeholder
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
            val intent = Intent(this, CreateProductActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}