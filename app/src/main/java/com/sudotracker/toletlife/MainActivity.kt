package com.sudotracker.toletlife

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val et_email: EditText = findViewById(R.id.et_email)
        et_email.setHint("Email")
        val et_password: EditText = findViewById(R.id.et_password)
        et_password.setHint("Password")
        val btn_login: Button = findViewById(R.id.btn_login)

        btn_login.setOnClickListener {
            val intent = Intent(this,RentalOptions::class.java)
            startActivity(intent)
        }



    }

}