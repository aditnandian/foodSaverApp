package com.example.foodsaverapps

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.foodsaverapps.Model.AllMenu
import com.example.foodsaverapps.databinding.ActivityAddProductBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class EditProductActivity : AppCompatActivity() {

    // Views and Firebase
    private lateinit var binding: ActivityAddProductBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userId: String
    private lateinit var menuItemKey: String
    private lateinit var foodName: String
    private lateinit var foodPrice: String
    private lateinit var foodCategory: String
    //    private lateinit var foodStock: String
    private lateinit var foodDescription: String
    private lateinit var foodShelfLife: String
    private var foodImageUri: Uri? = null
    var imageischanged: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Ensure user is logged in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Handle not logged in case
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        userId = currentUser.uid

        // Retrieve menuItemKey from intent
        menuItemKey = intent.getStringExtra("menuItemKey") ?: ""

        // Populate UI with existing data
        setData(menuItemKey, userId)

        // Set up category autocomplete
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

            if (foodImageUri != null && foodName.isNotBlank() && foodPrice.isNotBlank() &&
                foodCategory.isNotBlank() && foodDescription.isNotBlank()) {
                uploadData(menuItemKey, userId)
            } else {
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ImageView1.setOnClickListener {
            pickImage()
        }

        binding.BackButton.setOnClickListener {
            val intent = Intent(this@EditProductActivity, ProductsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setData(menuItemKey: String, userId: String) {
        val menuRef = database.reference.child("shop/$userId/menu").child(menuItemKey)

        menuRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val menuItem = snapshot.getValue(AllMenu::class.java)
                if (menuItem != null) {
                    binding.addbutton.text = "Update Menu"
                    binding.TextView0.text = "Update Menu"
                    binding.TextField1.setText(menuItem.foodName)
                    binding.TextField2.hint = ""
                    binding.TextView2.text = "Update Menu Image"
                    menuItem.foodPrice?.let { binding.CurrencyVal.setText(it.toString()) }
                    binding.CatField.setText(menuItem.foodCategory)
//                    menuItem.foodStock?.let { binding.TextField3.setText(it.toString()) }
                    binding.TextField4.setText(menuItem.foodDescription)
                    binding.TextField5.setText(menuItem.foodShelfLife)
                    menuItem.foodImage?.let { url ->
                        Glide.with(this@EditProductActivity)
                            .load(url)
                            .transform(CenterCrop(), RoundedCorners(20))
                            .into(binding.ImageView1)
                        binding.ImageView1.setPadding(0)
                        binding.ImageView1.background = ResourcesCompat.getDrawable(resources,R.drawable.nullify,null)
                        foodImageUri = Uri.parse(url)
                    }
                    val category = resources.getStringArray(R.array.category)
                    val categoryIndex = category.indexOf(menuItem.foodCategory)
                    if (categoryIndex != -1) {
                        binding.CatField.setSelection(categoryIndex)
                    }
                } else {
                    Toast.makeText(this@EditProductActivity, "Error loading data", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditProductActivity, "Database error", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun uploadData(menuItemKey: String, userId: String) {
        if (foodImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }
        val menuRef = database.reference.child("shop/$userId/menu")
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("shop/$userId/menu_images/${menuItemKey}.jpg")

        if (imageischanged) {
            val uploadTask = imageRef.putFile(foodImageUri!!)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    val newItem = AllMenu(
                        menuItemKey,
                        foodName,
                        foodPrice.toInt(),
                        foodCategory,
                        foodDescription,
                        downloadUri.toString(),
                        foodShelfLife
                    )
                    menuRef.child(menuItemKey).setValue(newItem)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Menu updated successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@EditProductActivity, ProductsActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to update product: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Failed to upload image: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val newItem = AllMenu(
                menuItemKey,
                foodName,
                foodPrice.toInt(),
                foodCategory,
                foodDescription,
                foodImageUri.toString(),
                foodShelfLife
            )
            menuRef.child(menuItemKey).setValue(newItem)
                .addOnSuccessListener {
                    Toast.makeText(this, "Menu updated successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@EditProductActivity, ProductsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update product: ${it.message}", Toast.LENGTH_SHORT).show()
                }
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
                imageischanged = true
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
    private fun previewImage(fileUri: Uri) {
        Glide.with(this)
            .load(fileUri)
            .transform(CenterCrop(), RoundedCorners(20))
            .into(binding.ImageView1)
    }
}