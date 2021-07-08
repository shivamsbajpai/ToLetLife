package com.sudotracker.toletlife

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RentalOptions : AppCompatActivity() {

    private lateinit var newRecyclerview: RecyclerView
    private lateinit var newArrayList : ArrayList<RentDetails>
    lateinit var imageId: Array<Int>
    lateinit var heading: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rental_options)


        imageId = arrayOf(
            R.drawable.bg_triangle,
            R.drawable.bg_triangle,
            R.drawable.bg_triangle,
            R.drawable.bg_triangle,
            R.drawable.bg_triangle,
            R.drawable.bg_triangle,
            R.drawable.bg_triangle,
            R.drawable.bg_triangle,
            R.drawable.bg_triangle,
            R.drawable.bg_triangle
        )

        heading = arrayOf(
            "Argentina",
            "Australia",
            "Belgium",
            "Denmark",
            "Brazil",
            "Fiji",
            "Germany",
            "India",
            "Kuwait",
            "New Zealand"
        )

        newRecyclerview = findViewById(R.id.rv_rentalOptions)
        newRecyclerview.layoutManager = LinearLayoutManager(this)
        newRecyclerview.setHasFixedSize(true)

        newArrayList = arrayListOf<RentDetails>()
        getUserdata()

    }

    private fun getUserdata(){
        for(i in imageId.indices){
            val rentDetails = RentDetails("test user",heading[i],"address",5000,"testStatus",imageId[i])
            newArrayList.add(rentDetails)
        }
        var adapter = RvAdapter(newArrayList)
        newRecyclerview.adapter = adapter

        adapter.setOnItemClickListener(object : RvAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                Toast.makeText(this@RentalOptions,"You clicked on item no. $position", Toast.LENGTH_SHORT).show()
            }

        })
    }
}