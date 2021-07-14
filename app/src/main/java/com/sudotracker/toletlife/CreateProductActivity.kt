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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sudotracker.toletlife.Error.ErrorResponse
import com.sudotracker.toletlife.Error.ValidationErrorResponse
import com.sudotracker.toletlife.Responses.LoginResponse
import com.sudotracker.toletlife.Responses.ProductCategoryResponse
import com.sudotracker.toletlife.Responses.UserAllRentDetails
import com.sudotracker.toletlife.Services.ProductCategoryService
import com.sudotracker.toletlife.Services.RentDetailsService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateProductActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_product)
        progressBarVisibility(false)
        setBottomNavigationBarProperties()
        val productCategoriesString = loadProductCategories()
        val gson = Gson()
        val productCategories: ProductCategoryResponse = gson.fromJson(productCategoriesString, ProductCategoryResponse::class.java)

        var spinnerMap = mutableMapOf<String,String>()
        var spinnerItems = mutableListOf<String>()

        var i = 0
        for(item in productCategories){
            spinnerMap[item.productCategory] = item.productCategoryId
            spinnerItems.add(item.productCategory)
            i++
        }


        val spinner: Spinner = findViewById(R.id.spinner_create_product)
        val btn_create_product: Button = findViewById(R.id.btn_create_product)

        val et_create_product: EditText = findViewById(R.id.et_create_product)
        val et_create_product_description: EditText =
            findViewById(R.id.et_create_product_description)
        val et_create_product_security_deposit: EditText =
            findViewById(R.id.et_create_product_security_deposit)
        val et_create_product_monthly_rent: EditText =
            findViewById(R.id.et_create_product_monthly_rent)


        val arrayAdapter = ArrayAdapter(this, R.layout.spinner_item, spinnerItems)
        arrayAdapter.setDropDownViewResource(R.layout.spinner_item)
        var selectedText = ""
        spinner.adapter = arrayAdapter
        spinner.setSelection(1)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedText = spinnerMap[spinnerItems[position]].toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }


        btn_create_product.setOnClickListener {
            val product = et_create_product.text.toString()
            val product_category = selectedText
            val product_description = et_create_product_description.text.toString()
            val security_deposit = et_create_product_security_deposit.text.toString()
            val monthly_rent = et_create_product_monthly_rent.text.toString()

            if (product == "" || product_category == "" || product_description == "" || security_deposit == "" || monthly_rent == "") {
                Toast.makeText(this, "Fields should not be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            addAddress(
                productName = product,
                productDescription = product_description,
                productCategory = product_category,
                securityDeposit = security_deposit,
                monthlyRent = monthly_rent
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


    private fun addAddress(
        productName: String,
        productCategory: String,
        productDescription: String,
        securityDeposit: String,
        monthlyRent: String
    ) {
        val intent: Intent = Intent(this, CreateRentActivity::class.java)
        intent.putExtra("product_name", productName)
        intent.putExtra("product_category", productCategory)
        intent.putExtra("product_description", productDescription)
        intent.putExtra("security_deposit", securityDeposit)
        intent.putExtra("monthly_rent", monthlyRent)
        startActivity(intent)
        finish()

    }

    private fun progressBarVisibility(visibility: Boolean) {
        val progress: ProgressBar =
            findViewById(R.id.create_product_activity_circular_progress_load)
        if (visibility) {
            progress.visibility = View.VISIBLE
        } else {
            progress.visibility = View.GONE
        }
    }

    private fun loadProductCategories(): String? {
        val sharedPreferences = getSharedPreferences("productCategories",Context.MODE_PRIVATE)
        return sharedPreferences.getString("productCategories",null)
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
            saveToken(null)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return@setOnMenuItemClickListener true
        }


        fab.setOnClickListener {
//            val intent = Intent(this, CreateRentActivity::class.java)
//            startActivity(intent)
//            finish()
        }

    }
}