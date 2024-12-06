package com.example.foodsaverapps

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.foodsaverapps.databinding.ActivityAddProductBinding
import com.example.foodsaverapps.Model.AllMenu
import com.example.foodsaverapps.Model.MenuItem
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddProductActivity : AppCompatActivity() {

    // Food item Details
    private lateinit var foodName: String
    private lateinit var foodPrice: String
    private lateinit var foodCategory: String
    //    private lateinit var foodStock: String
    private lateinit var foodDescription: String
    private lateinit var foodShelfLife: String
    private var foodImageUri: Uri? = null

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val binding: ActivityAddProductBinding by lazy {
        ActivityAddProductBinding.inflate(layoutInflater)
    }
//    var side: Boolean = false

//    private val timePickerDialogListener: TimePickerDialog.OnTimeSetListener =
//        TimePickerDialog.OnTimeSetListener { view, hour, min ->
//            val formTime: String = when {
//                hour < 10 -> {
//                    if (min < 10) {
//                        "0${hour}:0${min}"
//                    } else {
//                        "0${hour}:${min}"
//                    }
//                }
//                else -> {
//                    if (min < 10) {
//                        "${hour}:0${min}"
//                    } else {
//                        "${hour}:${min}"
//                    }
//                }
//            }
//            if (side) {
//                binding.TextField5a.text = formTime
//            } else {
//                binding.TextField5b.text = formTime
//            }
//        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // initialize FireBase & database instance
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val category = resources.getStringArray(R.array.category)
        val arrayAdapter = ArrayAdapter(this, R.layout.categories, category)
        val autocompleteTV = findViewById<AutoCompleteTextView>(R.id.CatField)
        autocompleteTV.setAdapter(arrayAdapter)

        binding.addbutton.setOnClickListener {

            foodName = binding.TextField1.text.toString().trim()
            foodPrice = binding.CurrencyVal.text.toString().trim()
            foodCategory = binding.CatField.text.toString().trim()
//            foodStock = binding.TextField3.text.toString().trim()
            foodDescription = binding.TextField4.text.toString().trim()
            foodShelfLife = binding.TextField5.text.toString().trim()

            if (!(foodName.isBlank() || foodPrice.isBlank() || foodCategory.isBlank() || foodDescription.isBlank() || foodShelfLife.isBlank())
            ) {
                uploadData()
                Toast.makeText(this, "Menu Added Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
            }
        }
        binding.ImageView1.setOnClickListener {
            pickImage()
        }
        binding.BackButton.setOnClickListener {
            val intent = Intent(this@AddProductActivity, ProductsActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.CatField.setOnClickListener {
            binding.TextField2.hint = ""
        }
        binding.TextField2.setOnClickListener {
            binding.TextField2.hint = ""
        }
    }

    private fun uploadData() {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("AddProductActivity", "User is not authenticated")
            return
        }
        Log.d("AddProductActivity", "User ID: $userId")

        val menuRef = database.getReference("shop/$userId/menu")
        val newItemKey = menuRef.push().key ?: run {
            Log.e("AddProductActivity", "Failed to generate new item key")
            return
        }
        Log.d("AddProductActivity", "New item key: $newItemKey")

        if (foodImageUri == null) {
            Log.e("AddProductActivity", "Food image URI is null")
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("AddProductActivity", "Food image URI: $foodImageUri")

        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("shop/$userId/menu_images/${newItemKey}.jpg")
        val uploadTask = imageRef.putFile(foodImageUri!!)

        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Log.d("AddProductActivity", "Image uploaded successfully: $uri")
                val newItem = MenuItem(
                    newItemKey,
                    foodName = foodName,
                    foodPrice = foodPrice.toInt(),
                    foodCategory = foodCategory,
                    foodDescription = foodDescription,
                    foodShelfLife = foodShelfLife,
                    foodImage = uri.toString(),
                    foodOrdered = 0,
                    foodAvailable = true

                )

                menuRef.child(newItemKey).setValue(newItem)
                    .addOnSuccessListener {
                        Log.d("AddProductActivity", "Menu added successfully")
                        Toast.makeText(this, "Menu added successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .addOnFailureListener {
                        Log.e("AddProductActivity", "Menu add failure", it)
                        Toast.makeText(this, "Menu add failure", Toast.LENGTH_SHORT).show()
                    }
                val intent = Intent(this@AddProductActivity, ProductsActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener {
            Log.e("AddProductActivity", "Upload failed", it)
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
        }
    }


    private fun pickImage() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(512, 512)
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val fileUri = data.data
            fileUri?.let {
                previewImage(it)
                foodImageUri = it
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this@AddProductActivity, ImagePicker.getError(data), Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(this@AddProductActivity, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun previewImage(fileUri: Uri) {
        Glide.with(this@AddProductActivity)
            .load(fileUri)
            .transform(CenterCrop(), RoundedCorners(20))
            .into(binding.ImageView1)
        binding.ImageView1.setPadding(0)
        binding.ImageView1.background =
            ResourcesCompat.getDrawable(resources, R.drawable.nullify, null)
    }
}
