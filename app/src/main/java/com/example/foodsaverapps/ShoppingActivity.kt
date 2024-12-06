package com.example.foodsaverapps

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.foodsaverapps.Adapter.MenuItem2Adapter
import com.example.foodsaverapps.Model.AllMenu
import com.example.foodsaverapps.databinding.ActivityShoppingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ShoppingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShoppingBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var shopKey: String
    private var menuItems: ArrayList<AllMenu> = ArrayList()
    private lateinit var quantities: IntArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        shopKey = intent.getStringExtra("sellerId") ?: ""
        databaseReference = FirebaseDatabase.getInstance().reference

        Log.d("ShoppingActivity", "Seller ID: $shopKey")

        retrieveShopDetails()
        retrieveMenuItem()

        binding.BackButton.setOnClickListener {
            finish()
        }

        binding.addtocart.setOnClickListener {
            checkAndAddToCart()
        }
    }

    private fun retrieveShopDetails() {
        databaseReference.child("shop").child(shopKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val shopName = snapshot.child("name").getValue(String::class.java) ?: ""
                val shopAddress = snapshot.child("address").getValue(String::class.java) ?: ""
                val shopImageUrl = snapshot.child("coverUrl").getValue(String::class.java) ?: ""

                Log.d("ShoppingActivity", "Shop Details - Name: $shopName, Address: $shopAddress")

                binding.ShopName.text = shopName
                binding.ShopAddress.text = shopAddress
                Glide.with(this@ShoppingActivity).load(shopImageUrl).into(binding.ShopImage)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", "Error: ${error.message}")
            }
        })
    }

    private fun retrieveMenuItem() {
        database = FirebaseDatabase.getInstance()
        val foodRef: DatabaseReference = database.reference.child("shop/$shopKey/menu")

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                menuItems.clear()
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(AllMenu::class.java)
                    menuItem?.let {
                        menuItems.add(it)
                        Log.d("ShoppingActivity", "Menu Item: ${it.foodName}")
                    }
                }
                // Initialize quantities array with the size of menuItems
                quantities = IntArray(menuItems.size) { 0 }
                setAdapter()
                // Initial call to set button visibility
                updateAddToCartButtonVisibility()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", "Error: ${error.message}")
            }
        })
    }

    private fun setAdapter() {
        val adapter = MenuItem2Adapter(
            this@ShoppingActivity,
            menuItems,
            quantities,
            databaseReference,
            onPlusClickListener = { position ->
                addQuantity(position)
            },
            onMinusClickListener = { position ->
                subQuantity(position)
            })
        binding.ShopMenus.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.ShopMenus.adapter = adapter

        Log.d("ShoppingActivity", "Adapter set with ${menuItems.size} items")
    }

    private fun addQuantity(position: Int) {
//        if (position < quantities.size && quantities[position] < menuItems[position].foodStock!!) {
        quantities[position]++
        Log.d("ShoppingActivity", "Quantity increased for position $position: ${quantities[position]}")
//        }
        updateAddToCartButtonVisibility()
    }

    private fun subQuantity(position: Int) {
//        if (position < quantities.size && quantities[position] > 0) {
        quantities[position]--
        Log.d("ShoppingActivity", "Quantity decreased for position $position: ${quantities[position]}")
//        }
        updateAddToCartButtonVisibility()
    }

    private fun allQuantitiesZero(): Boolean {
        return quantities.all { it == 0 }
    }

    private fun updateAddToCartButtonVisibility() {
        binding.addtocart.visibility = if (allQuantitiesZero()) View.INVISIBLE else View.VISIBLE
    }

    private fun checkAndAddToCart() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val cartRef = FirebaseDatabase.getInstance().reference.child("customer").child(userId).child("cartItems")

        cartRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var differentStore = false
                var cartIsEmpty = true

                for (itemSnapshot in snapshot.children) {
                    cartIsEmpty = false
                    val cartShopKey = itemSnapshot.child("shopKey").getValue(String::class.java)
                    if (cartShopKey != null && cartShopKey != shopKey) {
                        differentStore = true
                        break
                    }
                }

                if (differentStore) {
                    Toast.makeText(this@ShoppingActivity, "You can only add items from the same store to the cart", Toast.LENGTH_SHORT).show()
                } else {
                    if (cartIsEmpty || !allQuantitiesZero()) {
                        addToCart()
                    } else {
                        Toast.makeText(this@ShoppingActivity, "No items selected to add to cart", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", "Error: ${error.message}")
            }
        })
    }

    private fun addToCart() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val cartRef = FirebaseDatabase.getInstance().reference.child("customer").child(userId).child("cartItems")

        for (i in menuItems.indices) {
            if (quantities[i] > 0) {
                val cartItem = mapOf(
                    "foodName" to menuItems[i].foodName,
                    "foodPrice" to menuItems[i].foodPrice,
                    "foodDescription" to menuItems[i].foodDescription,
                    "foodImage" to menuItems[i].foodImage,
                    "foodQuantity" to quantities[i],
                    "shopKey" to shopKey  // Add the shopKey to the cart item
                )
                cartRef.push().setValue(cartItem)
            }
        }
        Toast.makeText(this, "Items added to cart", Toast.LENGTH_SHORT).show()
        // Optionally, clear the quantities after adding to cart
        quantities.fill(0)
        binding.ShopMenus.adapter?.notifyDataSetChanged()
        // Optionally, hide the add to cart button
        binding.addtocart.visibility = View.INVISIBLE
    }

    private fun updateItems() {
        for (i in menuItems.indices) {
            quantities[i] = 0

            val holder = binding.ShopMenus.findViewHolderForAdapterPosition(i) as? MenuItem2Adapter.AddItemViewHolder
            holder?.let {
                it.binding.DiscText2.visibility = View.VISIBLE
                it.binding.DiscText2.isEnabled = true
            }
        }
    }
}
