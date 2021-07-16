package com.sudotracker.toletlife

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sudotracker.toletlife.Error.ErrorResponse
import com.sudotracker.toletlife.Error.ValidationErrorResponse
import com.sudotracker.toletlife.Responses.ProductCategoryResponse
import com.sudotracker.toletlife.Responses.UserAllRentDetails
import com.sudotracker.toletlife.Responses.UserAllRentDetailsItem
import com.sudotracker.toletlife.Services.RentDetailsService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchRentDetails : AppCompatActivity() {
    private lateinit var newRecyclerview: RecyclerView
    private lateinit var newArrayList: ArrayList<UserAllRentDetailsItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_rent_details)
        progressBarVisibility(false)
        setBottomNavigationBarProperties()
        val searchView: SearchView = findViewById(R.id.search_view)
        val search_term = searchView.query

        newRecyclerview = findViewById(R.id.rv_search_rent)
        newRecyclerview.layoutManager = LinearLayoutManager(this)
        newRecyclerview.setHasFixedSize(true)

        newArrayList = ArrayList<UserAllRentDetailsItem>()
        Log.i("very different", search_term.toString())

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                newArrayList = ArrayList<UserAllRentDetailsItem>()
                search_by_product(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {

                return false
            }
        })

    }

    private fun getUserdata(response: UserAllRentDetails) {
        val productCategoriesString = loadProductCategories()
        val gson = Gson()
        val productCategories: ProductCategoryResponse =
            gson.fromJson(productCategoriesString, ProductCategoryResponse::class.java)

        var productCategoryMap = mutableMapOf<String, String>()

        for (item in productCategories) {
            productCategoryMap[item.productCategoryId] = item.productCategory
        }

        for (i in response.indices) {
            val userAllRentDetailsItem =
                UserAllRentDetailsItem(
                    rentId = response[i].rentId,
                    userId = response[i].userId,
                    productName = response[i].productName,
                    productCategoryId = response[i].productCategoryId,
                    productDescription = response[i].productDescription,
                    securityDeposit = response[i].securityDeposit,
                    monthlyRent = response[i].monthlyRent,
                    address = response[i].address,
                    area = response[i].area,
                    city = response[i].city,
                    state = response[i].state,
                    statusId = response[i].statusId,
                    pincode = response[i].pincode,
                    imageUrls = response[i].imageUrls,
                    ownerName = response[i].ownerName,
                    ownerEmail = response[i].ownerEmail
                )
            newArrayList.add(userAllRentDetailsItem)
        }
        var adapter = RvAdapter(newArrayList,productCategoryMap)
        newRecyclerview.adapter = adapter
        progressBarVisibility(false)

        adapter.setOnItemClickListener(object : RvAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                val gson = Gson()
                val jsonResponse = gson.toJson(newArrayList[position])
                val intent = Intent(this@SearchRentDetails, ProductDetailsActivity::class.java)
                intent.putExtra("productDetails",jsonResponse)
                startActivity(intent)
            }

        })
    }

    private fun search_by_product(search_term: String) {
        progressBarVisibility(true)
        val jwtToken = loadJWTTokenData()
        val call =
            RentDetailsService.rentDetailsInstance.search_by_product(
                product_search_term = search_term,
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
                        this@SearchRentDetails,
                        errorResponse?.detail?.first()?.msg.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    return
                } else if (response.code() > 399) {
                    val type = object : TypeToken<ErrorResponse>() {}.type
                    val errorResponse: ErrorResponse? =
                        gson.fromJson(response.errorBody()?.charStream(), type)
                    Toast.makeText(
                        this@SearchRentDetails,
                        errorResponse?.detail.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    return
                } else if (response.code() == 200) {
                    val jsonResponse = gson.toJson(response.body())
                    val resp: UserAllRentDetails =
                        gson.fromJson(jsonResponse, UserAllRentDetails::class.java)
                    if (resp.isEmpty()) {
                        Toast.makeText(this@SearchRentDetails, "Not found", Toast.LENGTH_LONG)
                            .show()
                        progressBarVisibility(false)
                        return
                    }
                    getUserdata(resp)
                    return
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Toast.makeText(
                    this@SearchRentDetails,
                    "Could not connect to internet. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("failure", "Error in failure", t)
            }

        })

    }

    private fun loadProductCategories(): String? {
        val sharedPreferences = getSharedPreferences("productCategories", Context.MODE_PRIVATE)
        return sharedPreferences.getString("productCategories", null)
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
        bottomNavigationView.selectedItemId = R.id.menu_search
        bottomNavigationView.menu.getItem(2).isEnabled = false
        val fab: FloatingActionButton = findViewById(R.id.fab)

        bottomNavigationView.menu.getItem(0).setOnMenuItemClickListener {
            val intent = Intent(this, RentalOptions::class.java)
            startActivity(intent)
            finish()
            return@setOnMenuItemClickListener true
        }
        bottomNavigationView.menu.getItem(1).setOnMenuItemClickListener {
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

    private fun progressBarVisibility(visibility: Boolean) {
        val progress: ProgressBar =
            findViewById(R.id.search_activity_circular_progress_load)
        if (visibility) {
            progress.visibility = View.VISIBLE
        } else {
            progress.visibility = View.GONE
        }
    }
}