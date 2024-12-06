package com.example.foodsaverapps

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodsaverapps.Adapter.OrderDetailsAdapter
import com.example.foodsaverapps.databinding.ActivityOrderDetailsBinding
import com.example.foodsaverapps.Model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OrderDetailsShopActivity : AppCompatActivity() {

    private val TAG = "OrderDetailsShopActivity"

    private val binding: ActivityOrderDetailsBinding by lazy {
        ActivityOrderDetailsBinding.inflate(layoutInflater)
    }

    private var userName: String? = null
    private var phoneNumber: String? = null
    private var customerKey: String? = null
    private var totalPrice: Int? = null
    private var appliedDisc: String? = null
    private var discAmount: Int? = null
    private var foodNames: ArrayList<String> = arrayListOf()
    private var foodImages: ArrayList<String> = arrayListOf()
    private var foodQuantity: ArrayList<Int> = arrayListOf()
    private var foodPrices: ArrayList<Int> = arrayListOf()

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var orderDetails: OrderDetails
    private var transactionKey: String? = null
    private var status: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.backeButton.setOnClickListener {
            finish()
            Log.d(TAG, "onCreate: Back button clicked, finishing activity")
        }

        binding.confirmButton.visibility = View.GONE
        binding.declineButton.visibility = View.GONE

        transactionKey = intent.getStringExtra("itemPushKey")
        if (transactionKey != null) {
            fetchOrderDetails(transactionKey!!)
        } else {
            Log.e(TAG, "onCreate: Transaction key is null")
        }

        status = intent.getStringExtra("status")
        if (status == "Pending") {
            binding.completeButton.visibility = View.GONE
            binding.cancelButton.visibility = View.GONE
            binding.confirmButton.setOnClickListener {
                updateOrderStatus("Active")
            }
            binding.declineButton.setOnClickListener {
                updateOrderStatus("Cancelled")
            }
        } else if (status == "Active") {
            binding.buttonContainer.visibility = View.GONE
            binding.completeButton.setOnClickListener {
                updateOrderStatus("Completed")
            }
            binding.cancelButton.setOnClickListener {
                updateOrderStatus("Cancelled")
            }
        } else {
            binding.buttonContainer.visibility = View.GONE
            binding.completeButton.visibility = View.GONE
            binding.cancelButton.visibility = View.GONE
        }
    }

    private fun fetchOrderDetails(transactionKey: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val orderDetailsRef = database.child("shop").child(userId).child("OrderDetails").child(transactionKey)
            orderDetailsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    orderDetails = dataSnapshot.getValue(OrderDetails::class.java) ?: return
                    orderDetails.let {
                        userName = it.userName
                        foodNames = it.foodNames as ArrayList<String>
                        foodImages = it.foodImages as ArrayList<String>
                        foodQuantity = it.foodQuantities as ArrayList<Int>
                        phoneNumber = it.phoneNumber
                        foodPrices = it.foodPrices as ArrayList<Int>
                        totalPrice = it.totalPrice
                        appliedDisc = it.voucherName ?: ""
                        discAmount = it.appliedDiscount
                        appliedDisc = "$appliedDisc ($discAmount%)"

                        Log.d(TAG, "fetchOrderDetails: User Name: $userName")
                        Log.d(TAG, "fetchOrderDetails: Phone Number: $phoneNumber")
                        Log.d(TAG, "fetchOrderDetails: Total Price: $totalPrice")
                        Log.d(TAG, "fetchOrderDetails: Applied Discount: $appliedDisc")
                        Log.d(TAG, "fetchOrderDetails: Food Names: $foodNames")
                        Log.d(TAG, "fetchOrderDetails: Food Images: $foodImages")
                        Log.d(TAG, "fetchOrderDetails: Food Quantities: $foodQuantity")
                        Log.d(TAG, "fetchOrderDetails: Food Prices: $foodPrices")

                        setUserDetail()
                        setAdapter()

                        // Show buttons when order details are fetched
                        binding.confirmButton.visibility = View.VISIBLE
                        binding.declineButton.visibility = View.VISIBLE
                    } ?: run {
                        Log.e(TAG, "fetchOrderDetails: No order details found")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "fetchOrderDetails: Failed to fetch order details", databaseError.toException())
                }
            })
        } else {
            Log.e(TAG, "fetchOrderDetails: User ID is null")
        }
    }

    private fun updateOrderStatus(status: String) {
        val userId = auth.currentUser?.uid
        if (userId != null && transactionKey != null) {
            val orderDetailsRef = database.child("shop").child(userId).child("OrderDetails").child(transactionKey!!)
            orderDetailsRef.child("status").setValue(status).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    orderDetailsRef.child("orderConfirmed").setValue(true)
                    Log.d(TAG, "updateOrderStatus: Order status updated to $status")
                    updateCustomerOrderStatus(status)
                    if (status == "Active") {
                        decreaseFoodStock()
                    }
                } else {
                    Log.e(TAG, "updateOrderStatus: Failed to update order status", task.exception)
                }
            }
        } else {
            Log.e(TAG, "updateOrderStatus: User ID or transaction key is null")
        }
    }

    private fun decreaseFoodStock() {
        val userId = auth.currentUser?.uid ?: return
        Log.d(TAG, "decreaseFoodStock: Starting stock update for user $userId")

        for (i in foodNames.indices) {
            val foodName = foodNames[i]
            val orderedQuantity = foodQuantity[i]
            Log.d(TAG, "decreaseFoodStock: Processing $foodName with ordered quantity $orderedQuantity")

            val menuRef = database.child("shop").child(userId).child("menu")
            menuRef.orderByChild("foodName").equalTo(foodName).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "decreaseFoodStock: Received snapshot for $foodName")

                    if (!snapshot.exists()) {
                        Log.e(TAG, "decreaseFoodStock: No item found with name $foodName")
                        return
                    }

                    for (foodSnapshot in snapshot.children) {
                        val menuKey = foodSnapshot.key
                        Log.d(TAG, "decreaseFoodStock: Found menu key $menuKey for $foodName")

                        if (menuKey != null) {
//                            val currentQuantity = foodSnapshot.child("foodStock").getValue(Int::class.java) ?: 0
                            val currentOrdered = foodSnapshot.child("foodOrdered").getValue(Int::class.java) ?: 0
//                            val newQuantity = currentQuantity - orderedQuantity
                            val newOrdered = currentOrdered + orderedQuantity
//                            Log.d(TAG, "decreaseFoodStock: Current stock: $currentQuantity, New stock: $newQuantity")

                            menuRef.child(menuKey).child("foodOrdered").setValue(newOrdered)
//                            menuRef.child(menuKey).child("foodStock").setValue(newQuantity)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
//                                        Log.d(TAG, "decreaseFoodStock: Stock updated for $foodName, new stock: $newQuantity")
                                    } else {
                                        Log.e(TAG, "decreaseFoodStock: Failed to update stock for $foodName", task.exception)
                                    }
                                }
                        } else {
                            Log.e(TAG, "decreaseFoodStock: Menu key is null for $foodName")
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "decreaseFoodStock: Failed to query menu items", error.toException())
                    Toast.makeText(this@OrderDetailsShopActivity, "Failed to update food quantities", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateCustomerOrderStatus(status: String) {
        val userId = auth.currentUser?.uid

        val shopNameRef = database.child("shop").child(userId!!).child("OrderDetails").child(transactionKey!!).child("userUid")
        shopNameRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                customerKey = snapshot.getValue(String::class.java) ?: ""
                Log.d(TAG, "updateCustomerOrderStatus: customer key is $customerKey and transaction key is $transactionKey")

                if (customerKey != null && transactionKey != null) {
                    val orderDetailsRef = database.child("customer").child(customerKey!!).child("buyHistory").child(transactionKey!!)
                    orderDetailsRef.child("status").setValue(status).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            orderDetailsRef.child("orderConfirmed").setValue(true)
                            Log.d(TAG, "updateCustomerOrderStatus: Order status updated to $status")
                            finish()
                        } else {
                            Log.e(TAG, "updateCustomerOrderStatus: Failed to update customer order status", task.exception)
                        }
                    }
                } else {
                    Log.e(TAG, "updateCustomerOrderStatus: Customer key or transaction key is null")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OrderDetailsShopActivity, "Failed to retrieve shop name", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "updateCustomerOrderStatus: Failed to retrieve shop name", error.toException())
            }
        })
    }

    private fun setUserDetail() {
        Log.d(TAG, "setUserDetail: Setting user details")
        binding.name.text = userName
        binding.phone.text = phoneNumber
        binding.totalPay.text = "Rp" + totalPrice.toString()
        binding.discount.text = appliedDisc
    }

    private fun setAdapter() {
        Log.d(TAG, "setAdapter: Setting adapter for RecyclerView")
        binding.orderDetailRecyclerVew.layoutManager = LinearLayoutManager(this)
        val adapter = OrderDetailsAdapter(this, foodNames, foodImages, foodQuantity, foodPrices)
        binding.orderDetailRecyclerVew.adapter = adapter
    }
}
