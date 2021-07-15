package com.sudotracker.toletlife

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sudotracker.toletlife.Error.ErrorResponse
import com.sudotracker.toletlife.Error.ValidationErrorResponse
import com.sudotracker.toletlife.Requests.UploadImageRequest
import com.sudotracker.toletlife.Responses.PresignedURLResponse
import com.sudotracker.toletlife.Services.ImageService
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class UploadImageActivity : AppCompatActivity() {
    private var selectedImageUri: Uri? = null


    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 101
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_image)
        val jwtToken = loadJWTTokenData()
        val image_view: ImageView = findViewById(R.id.image_view)
        val button_upload: Button = findViewById(R.id.button_upload)

        val tv_activiy_upload_image_success: TextView = findViewById(R.id.tv_activiy_upload_image_success)
        tv_activiy_upload_image_success.visibility = View.GONE

        val rent_id = intent.getStringExtra("rent_id")

        setBottomNavigationBarProperties()


        image_view.setOnClickListener {
            val progress_bar: ProgressBar = findViewById(R.id.progress_bar)
            progress_bar.progress = 0
            openImageChooser()
        }
        button_upload.setOnClickListener {
            if (rent_id != null) {
                uploadImage(rent_id)
            }
        }

    }

    private fun openImageChooser() {
        Intent(Intent.ACTION_PICK).also {
            it.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(it, REQUEST_CODE_PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val image_view: ImageView = findViewById(R.id.image_view)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PICK_IMAGE -> {
                    selectedImageUri = data?.data
                    image_view.setImageURI(selectedImageUri)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun uploadImage(rent_id: String) {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select the image", Toast.LENGTH_LONG).show()
            return
        }

        val parcelFileDescriptor =
            contentResolver.openFileDescriptor(selectedImageUri!!, "r", null) ?: return

        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(cacheDir, contentResolver.getFileName(selectedImageUri!!))
        val file_type = contentResolver.getType(selectedImageUri!!)
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        presignedUrl(file_type!!, rent_id, loadJWTTokenData(), file)


    }

    fun ContentResolver.getFileName(fileUri: Uri): String {
        var name = ""
        val returnCursor = this.query(fileUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }
        return name
    }

    private fun presignedUrl(file_type: String, rent_id: String, token: String?, file: File) {
        val progress_bar: ProgressBar = findViewById(R.id.progress_bar)
        progress_bar.progress = 0
        val call = ImageService.imageInstance.presignedUrl(
            file_name = file.name,
            file_type = file_type,
            rent_id = rent_id,
            token = "Bearer $token"
        )
        Log.i("file_name", file.name)
        Log.i("file_type", file_type)
        val gson = Gson()
        call.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.code() == 422) {
                    val type = object : TypeToken<ValidationErrorResponse>() {}.type
                    val errorResponse: ValidationErrorResponse? =
                        gson.fromJson(response.errorBody()?.charStream(), type)
                    Toast.makeText(
                        this@UploadImageActivity,
                        errorResponse?.detail?.first()?.msg.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    return
                } else if (response.code() > 399) {
                    val type = object : TypeToken<ErrorResponse>() {}.type
                    val errorResponse: ErrorResponse? =
                        gson.fromJson(response.errorBody()?.charStream(), type)
                    Toast.makeText(
                        this@UploadImageActivity,
                        errorResponse?.detail.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    return
                } else if (response.code() == 200) {
                    val jsonResponse = gson.toJson(response.body())
                    val resp: PresignedURLResponse =
                        gson.fromJson(jsonResponse, PresignedURLResponse::class.java)
                    progress_bar.progress = 20
                    sendToAws(
                        rent_id,
                        resp.url,
                        resp.fields.file_address_aws,
                        resp.fields.xAmzAlgorithm,
                        resp.fields.xAmzCredential,
                        resp.fields.xAmzDate,
                        resp.fields.policy,
                        resp.fields.xAmzSignature,
                        file
                    )
                    return
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Toast.makeText(this@UploadImageActivity,"Could not connect to internet. Please try again.", Toast.LENGTH_LONG).show()
                Log.d("failure", "Error in failure", t)
            }

        })

    }

    private fun sendToAws(
        rent_id: String,
        url: String,
        file_address_aws: String,
        sxmzAlgorithm: String,
        sxmzCredential: String,
        sxmzDate: String,
        spolicy: String,
        sxmzSignature: String,
        sfile: File
    ) {
        val requestBodyFile_address_aws: RequestBody =
            RequestBody.create(MediaType.parse("text/plain"), file_address_aws)
        val xmzAlgorithm: RequestBody =
            RequestBody.create(MediaType.parse("text/plain"), sxmzAlgorithm)
        val xmzCredential: RequestBody =
            RequestBody.create(MediaType.parse("text/plain"), sxmzCredential)
        val xmzDate: RequestBody = RequestBody.create(MediaType.parse("text/plain"), sxmzDate)
        val policy: RequestBody = RequestBody.create(MediaType.parse("text/plain"), spolicy)
        val xmzSignature: RequestBody =
            RequestBody.create(MediaType.parse("text/plain"), sxmzSignature)
        val file: RequestBody = RequestBody.create(MediaType.parse("image/*"), sfile)

        val progress_bar: ProgressBar = findViewById(R.id.progress_bar)
        val call = ImageService.imageInstance.sendToAws(
            url = url,
            key = requestBodyFile_address_aws,
            algorithm = xmzAlgorithm,
            credential = xmzCredential,
            date = xmzDate,
            policy = policy,
            signature = xmzSignature,
            file = file
        )
        Log.i("key", file_address_aws)

        val gson = Gson()

        call.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.code() == 422) {
                    val type = object : TypeToken<ValidationErrorResponse>() {}.type
                    val errorResponse: ValidationErrorResponse? =
                        gson.fromJson(response.errorBody()?.charStream(), type)
                    Toast.makeText(
                        this@UploadImageActivity,
                        errorResponse?.detail?.first()?.msg.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    return
                } else if (response.code() > 399) {
                    val type = object : TypeToken<ErrorResponse>() {}.type
                    val errorResponse: ErrorResponse? =
                        gson.fromJson(response.errorBody()?.charStream(), type)
                    Toast.makeText(
                        this@UploadImageActivity,
                        errorResponse?.detail.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    return
                } else if (response.code() == 204) {
                    progress_bar.progress = 60
                    saveImageDetails(rent_id, file_address_aws, loadJWTTokenData())
                    return
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                progress_bar.progress = 0
                Toast.makeText(this@UploadImageActivity,"Could not connect to internet. Please try again.", Toast.LENGTH_LONG).show()
                Log.d("failure", "Error in failure", t)
            }
        })
    }

    private fun saveImageDetails(rent_id: String, file_address_aws: String, token: String?) {
        val image_details = UploadImageRequest(rent_id, file_address_aws)
        Log.i("rent_id", rent_id)
        Log.i("file_address_aws", file_address_aws)
        val call =
            ImageService.imageInstance.saveImageDetails(image_details, token = "Bearer $token")
        val progress_bar: ProgressBar = findViewById(R.id.progress_bar)
        val gson = Gson()
        call.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.code() == 422) {
                    val type = object : TypeToken<ValidationErrorResponse>() {}.type
                    val errorResponse: ValidationErrorResponse? =
                        gson.fromJson(response.errorBody()?.charStream(), type)
                    Toast.makeText(
                        this@UploadImageActivity,
                        errorResponse?.detail?.first()?.msg.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    return
                } else if (response.code() > 399) {
//                    val type = object : TypeToken<ErrorResponse>() {}.type
//                    val errorResponse: ErrorResponse? =
//                        gson.fromJson(response.errorBody()?.charStream(), type)
//                    Toast.makeText(
//                        this@UploadImageActivity,
//                        errorResponse?.detail.toString(),
//                        Toast.LENGTH_LONG
//                    ).show()
                    Log.i("error_response", response.toString())
                    return
                } else if (response.code() == 200) {
                    progress_bar.progress = 100
                    Toast.makeText(
                        this@UploadImageActivity,
                        "Image Uploaded Successfully",
                        Toast.LENGTH_LONG
                    ).show()
                    val tv_activiy_upload_image_success: TextView = findViewById(R.id.tv_activiy_upload_image_success)
                    tv_activiy_upload_image_success.visibility = View.VISIBLE
                    return
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Toast.makeText(this@UploadImageActivity,"Could not connect to internet. Please try again.", Toast.LENGTH_LONG).show()
                Log.d("failure", "Error in failure", t)
            }
        })
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

    private fun setBottomNavigationBarProperties(){
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.background = null
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


        fab.setOnClickListener{
            val intent = Intent(this, CreateProductActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

}