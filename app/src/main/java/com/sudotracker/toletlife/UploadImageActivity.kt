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
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sudotracker.toletlife.Error.ErrorResponse
import com.sudotracker.toletlife.Error.ValidationErrorResponse
import com.sudotracker.toletlife.Requests.OtpRequest
import com.sudotracker.toletlife.Requests.UploadImageRequest
import com.sudotracker.toletlife.Responses.OtpResponse
import com.sudotracker.toletlife.Responses.PresignedURLResponse
import com.sudotracker.toletlife.Services.IdentityService
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

        val rent_id = intent.getStringExtra("rent_id")


        image_view.setOnClickListener {
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
            //layout_root.snackbar("Select an Image First")
            return
        }

        val parcelFileDescriptor =
            contentResolver.openFileDescriptor(selectedImageUri!!, "r", null) ?: return

        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(cacheDir, contentResolver.getFileName(selectedImageUri!!))
        val file_type = contentResolver.getType(selectedImageUri!!)
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        presignedUrl(file_type!!,rent_id,loadJWTTokenData(),file)

       // progress_bar.progress = 0
//        val body = UploadRequestBody(file, "image", this)
//        MyAPI().uploadImage(
//            MultipartBody.Part.createFormData(
//                "image",
//                file.name,
//                body
//            ),
//            RequestBody.create(MediaType.parse("multipart/form-data"), "json")
//        ).enqueue(object : Callback<UploadResponse> {
//            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
//                layout_root.snackbar(t.message!!)
//                progress_bar.progress = 0
//            }
//
//            override fun onResponse(
//                call: Call<UploadResponse>,
//                response: Response<UploadResponse>
//            ) {
//                response.body()?.let {
//                    layout_root.snackbar(it.message)
//                    progress_bar.progress = 100
//                }
//            }
//        })

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

    private fun presignedUrl(file_type: String,rent_id: String,token: String?, file: File) {
        val call = ImageService.imageInstance.presignedUrl(
            file_name = file.name,
            file_type = file_type,
            rent_id = rent_id,
            token = "Bearer $token"
        )
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

                    sendToAws(
                        rent_id,
                        resp.url,
                        resp.fields.key,
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
                Log.d("failure", "Error in failure", t)
            }

        })

    }

    private fun sendToAws(
        rent_id: String,
        url: String,
        skey: String,
        sxmzAlgorithm: String,
        sxmzCredential: String,
        sxmzDate: String,
        spolicy: String,
        sxmzSignature: String,
        sfile: File
    ) {
        val key: RequestBody = RequestBody.create(MediaType.parse("text/plain"), skey)
        val xmzAlgorithm: RequestBody =
            RequestBody.create(MediaType.parse("text/plain"), sxmzAlgorithm)
        val xmzCredential: RequestBody =
            RequestBody.create(MediaType.parse("text/plain"), sxmzCredential)
        val xmzDate: RequestBody = RequestBody.create(MediaType.parse("text/plain"), sxmzDate)
        val policy: RequestBody = RequestBody.create(MediaType.parse("text/plain"), spolicy)
        val xmzSignature: RequestBody =
            RequestBody.create(MediaType.parse("text/plain"), sxmzSignature)
        val file: RequestBody = RequestBody.create(MediaType.parse("image/*"), sfile)


        val call = ImageService.imageInstance.sendToAws(
            url = url,
            file_name = key,
            algorithm = xmzAlgorithm,
            credential = xmzCredential,
            date = xmzDate,
            policy = policy,
            signature = xmzSignature,
            file = file
        )

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
                    saveImageDetails(rent_id,skey,loadJWTTokenData())
                    return
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Log.d("failure", "Error in failure", t)
            }

        })
    }

    private fun saveImageDetails(rent_id: String,file_name: String,token: String?) {
        val image_details = UploadImageRequest(rent_id, file_name)
        val call = ImageService.imageInstance.saveImageDetails(image_details,token = "Bearer $token")
        //val intent = Intent(this, RegisterActivity::class.java)
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
                    Log.i("error_response",response.toString())
                    return
                } else if (response.code() == 200) {
                    Log.i("success","this was successful")
                    Toast.makeText(this@UploadImageActivity,"Image Uploaded Successfully",Toast.LENGTH_LONG).show()
                    return
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Log.d("failure", "Error in failure", t)
            }
        })
    }



    private fun loadJWTTokenData(): String? {
        val sharedPreferences = getSharedPreferences("jwtToken", Context.MODE_PRIVATE)
        return sharedPreferences.getString("JWT_TOKEN", null)
    }

}