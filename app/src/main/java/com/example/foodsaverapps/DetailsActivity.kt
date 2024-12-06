package com.example.foodsaverapps

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.foodsaverapps.databinding.ActivityDetailsBinding
import com.example.foodsaverapps.Model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private var foodName: String? = null
    private var foodImage: String? = null
    private var foodDescriptions: String? = null
    private var foodPrice: Int? = null
//    private var foodStock: Int? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "onCreate: DetailsActivity started")

        // initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()
        foodName = intent.getStringExtra("MenuItemName")
        foodDescriptions = intent.getStringExtra("MenuItemDescription")
        foodPrice = intent.getIntExtra("MenuItemPrice", 0)
//        foodStock = intent.getIntExtra("MenuItemStock", 0)
        foodImage = intent.getStringExtra("MenuItemImage")

        Log.d(TAG, "onCreate: Received food details - Name: $foodName, Description: $foodDescriptions,  Price: $foodPrice, Image: $foodImage")

        with(binding) {
            detailFoodName.text = foodName
            detailDescription.text = foodDescriptions
            detailPrice.text = "Rp" + foodPrice.toString()
//            detailStock.text = foodStock.toString()
            Glide.with(this@DetailsActivity).load(Uri.parse(foodImage)).into(detailFoodImage)
        }

        binding.imageButton.setOnClickListener {
            Log.d(TAG, "onCreate: Back button clicked")
            finish()
        }
//        binding.addItemButton.setOnClickListener {
//            Log.d(TAG, "onCreate: Add item button clicked")
//            addItemToCart()
//        }
    }

    private fun addItemToCart() {
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid ?: ""

        Log.d(TAG, "addItemToCart: Adding item to cart for user $userId")

        // Create a cartItems object
        val cartItem = CartItems(
            foodName.toString(),
            foodPrice?.toInt(),
            foodDescriptions.toString(),
            foodImage.toString(),
            1
        )

        // Save data to cart item to firebase database
        database.child("customer").child(userId).child("cartItems").push().setValue(cartItem)
            .addOnSuccessListener {
                Log.d(TAG, "addItemToCart: Item added to cart successfully")
                Toast.makeText(this, "Add item to cart successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Log.e(TAG, "addItemToCart: Add item to cart failed", it)
                Toast.makeText(this, "Add item failed", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val TAG = "DetailsActivity"
    }
}
