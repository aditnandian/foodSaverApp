package com.example.foodsaverapps.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodsaverapp.Adapter.CartAdapter
import com.example.foodsaverapps.Model.CartItems
import com.example.foodsaverapps.PayOutActivity
import com.example.foodsaverapps.databinding.FragmentCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.util.Log

class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val cartItemsMap = mutableMapOf<String, CartItems>()  // Map to aggregate items
    private lateinit var cartAdapter: CartAdapter
    private lateinit var userId: String
    private lateinit var shopKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""

        retrieveCartItems()

        binding.proceedButton.setOnClickListener {
            // get order items details before proceeding to check out
            getOrderItemsDetail()
        }
        return binding.root
    }

    private fun getOrderItemsDetail() {
        val orderIdReference: DatabaseReference = database.reference.child("customer").child(userId).child("cartItems")

        val foodName = mutableListOf<String?>()
        val foodPrice = mutableListOf<Int>()
        val foodImage = mutableListOf<String?>()
        val foodDescription = mutableListOf<String?>()
        val foodQuantities = mutableListOf<Int>()

        orderIdReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val aggregatedItems = mutableMapOf<String, CartItems>()

                for (foodSnapshot in snapshot.children) {
                    val orderItem = foodSnapshot.getValue(CartItems::class.java)
                    if (orderItem != null) {
                        val existingItem = aggregatedItems[orderItem.foodName]
                        if (existingItem != null) {
                            existingItem.foodQuantity = (existingItem.foodQuantity ?: 0) + (orderItem.foodQuantity ?: 0)
                        } else {
                            aggregatedItems[orderItem.foodName!!] = orderItem
                        }
                    }
                }

                aggregatedItems.values.forEach {
                    foodName.add(it.foodName)
                    foodPrice.add(it.foodPrice ?: 0)
                    foodDescription.add(it.foodDescription)
                    foodImage.add(it.foodImage)
                    foodQuantities.add(it.foodQuantity ?: 0)
                    shopKey = it.shopKey!!
                }

                orderNow(foodName, foodPrice, foodDescription, foodImage, foodQuantities)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Order making Failed. Please Try Again.", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun orderNow(
        foodName: List<String?>,
        foodPrice: List<Int>,
        foodDescription: List<String?>,
        foodImage: List<String?>,
        foodQuantities: List<Int>
    ) {
        Log.d("CartFragment", "orderNow called with $foodName, $foodPrice, $foodDescription, $foodImage, $foodQuantities")

        if (isAdded && context != null) {
            val intent = Intent(requireContext(), PayOutActivity::class.java)
            intent.putExtra("FoodItemName", ArrayList(foodName))
            intent.putExtra("FoodItemPrice", ArrayList(foodPrice))
            intent.putExtra("FoodItemImage", ArrayList(foodImage))
            intent.putExtra("FoodItemDescription", ArrayList(foodDescription))
            intent.putExtra("FoodItemQuantities", ArrayList(foodQuantities))
            intent.putExtra("shopKey", shopKey)
            startActivity(intent)
        }
    }

    private fun retrieveCartItems() {
        val foodReference: DatabaseReference = database.reference.child("customer").child(userId).child("cartItems")

        foodReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartItemsMap.clear()
                for (foodSnapshot in snapshot.children) {
                    val cartItem = foodSnapshot.getValue(CartItems::class.java)
                    val foodName = cartItem?.foodName
                    if (foodName != null) {
                        val existingItem = cartItemsMap[foodName]
                        if (existingItem != null) {
                            // Update quantity if item already exists
                            existingItem.foodQuantity = (existingItem.foodQuantity ?: 0) + (cartItem.foodQuantity ?: 0)
                        } else {
                            // Add new item to map
                            cartItemsMap[foodName] = cartItem
                        }
                    }
                }
                setAdapter()
                updateProceedButtonVisibility()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Data not fetched", Toast.LENGTH_SHORT).show()
            }
        })
    }



    fun updateProceedButtonVisibility() {
        if (cartItemsMap.isEmpty()) {
            binding.proceedButton.visibility = View.GONE
        } else {
            binding.proceedButton.visibility = View.VISIBLE
        }
    }


    private fun setAdapter() {
        val foodNames = cartItemsMap.values.map { it.foodName }.toMutableList()
        val foodPrices = cartItemsMap.values.map { it.foodPrice ?: 0 }.toMutableList()
        val foodDescriptions = cartItemsMap.values.map { it.foodDescription ?: "" }.toMutableList()
        val foodImages = cartItemsMap.values.map { it.foodImage ?: "" }.toMutableList()
        val quantities = cartItemsMap.values.map { it.foodQuantity ?: 0 }.toMutableList()

        cartAdapter = CartAdapter(
            requireContext(),
            foodNames,
            foodPrices,
            foodDescriptions,
            foodImages,
            quantities
        )
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.cartRecyclerView.adapter = cartAdapter
    }
}
