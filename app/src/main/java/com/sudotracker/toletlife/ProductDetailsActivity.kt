package com.sudotracker.toletlife

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.gson.Gson
import com.sudotracker.toletlife.Responses.ProductCategoryResponse
import com.sudotracker.toletlife.Responses.UserAllRentDetailsItem

class ProductDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        val imageSlider: ImageSlider = findViewById(R.id.imageSliderProductDetails)
        val tvProductName: TextView = findViewById(R.id.tvProduct)
        val tvCategory: TextView = findViewById(R.id.tvCategory)
        val tvDescription: TextView = findViewById(R.id.tvDescription)
        val tvRent: TextView = findViewById(R.id.tvRent)
        val tvSecurityDeposit: TextView = findViewById(R.id.tvSecurityDeposit)
        val tvCity: TextView = findViewById(R.id.tvCity)
        val tvAddress: TextView = findViewById(R.id.tvAddress)
        val imageList = ArrayList<SlideModel>()

        val productDetailsString = intent.getStringExtra("productDetails")
        val gson = Gson()
        val productDetails: UserAllRentDetailsItem = gson.fromJson(productDetailsString, UserAllRentDetailsItem::class.java)

        val productCategoriesString = loadProductCategories()
        val productCategories: ProductCategoryResponse = gson.fromJson(productCategoriesString, ProductCategoryResponse::class.java)

        var productCategoryMap = mutableMapOf<String,String>()

        for(item in productCategories){
            productCategoryMap[item.productCategoryId] = item.productCategory
        }


        if(productDetails.imageUrls.isEmpty()){ //no image found error
            imageList.add(SlideModel(R.drawable.bg_triangle))
        }
        for(url in productDetails.imageUrls){
            imageList.add(SlideModel(url.imageUrl))
        }
        imageSlider.setImageList(imageList,ScaleTypes.FIT)

        tvProductName.setText(productDetails.productName)
        tvCategory.setText(productCategoryMap[productDetails.productCategoryId])
        tvDescription.setText(productDetails.productDescription)
        tvRent.setText(productDetails.monthlyRent)
        tvSecurityDeposit.setText(productDetails.securityDeposit)
        tvCity.setText(productDetails.city)
        tvAddress.setText(productDetails.address)


    }
    private fun loadProductCategories(): String? {
        val sharedPreferences = getSharedPreferences("productCategories", Context.MODE_PRIVATE)
        return sharedPreferences.getString("productCategories",null)
    }
}