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

class OrderDetailsActivity : AppCompatActivity() {

    private val TAG = "OrderDetailsActivity"

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

        binding.confirmButton.visibility= View.GONE
        binding.declineButton.visibility= View.GONE
        binding.buttonContainer.visibility = View.GONE
        binding.completeButton.visibility = View.GONE

        transactionKey = intent.getStringExtra("itemPushKey")
        if (transactionKey != null) {
            fetchOrderDetails(transactionKey!!)
        } else {
            Log.e(TAG, "onCreate: Transaction key is null")
        }

        status = intent.getStringExtra("status")
        if (status == "Pending") {
            binding.cancelButton.setOnClickListener {
                updateOrderStatus("Cancelled")
            }
        } else {
            binding.cancelButton.visibility = View.GONE
            Log.d ("Button Cancel", "Cancel Button is Gone")
        }
    }

    private fun fetchOrderDetails(transactionKey: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val orderDetailsRef = database.child("customer").child(userId).child("buyHistory").child(transactionKey)
//            val orderDetailsRef2 = database.child("shop").child(ShopId)
            orderDetailsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val orderDetails = dataSnapshot.getValue(OrderDetails::class.java)
                    orderDetails?.let {
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
            val orderDetailsRef = database.child("customer").child(userId).child("buyHistory").child(transactionKey!!)
            orderDetailsRef.child("status").setValue(status).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    orderDetailsRef.child("orderConfirmed").setValue(true)
                    Log.d(TAG, "updateOrderStatus: Order status updated to $status")
                    updateCustomerOrderStatus(status)
                } else {
                    Log.e(TAG, "updateOrderStatus: Failed to update order status", task.exception)
                }
            }
        } else {
            Log.e(TAG, "updateOrderStatus: User ID or transaction key is null")
        }
    }
    private fun updateCustomerOrderStatus(status: String) {
        val userId = auth.currentUser?.uid
        val shopIdRef = database.child("customer").child(userId!!).child("buyHistory").child(transactionKey!!).child("shopKey")
        var shopId: String?
        shopIdRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                shopId = snapshot.getValue(String::class.java)

                val shopNameRef = database.child("shop").child(shopId!!).child("OrderDetails").child(transactionKey!!).child("userUid")
                shopNameRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
//                customerKey = snapshot.getValue(String::class.java) ?: ""
                        Log.d(TAG, "updateCustomerOrderStatus: customer key is $shopId and transaction key is $transactionKey")

                        if (shopId != null && transactionKey != null) {
                            val orderDetailsRef = database.child("shop").child(shopId!!).child("OrderDetails").child(transactionKey!!)
                            orderDetailsRef.removeValue().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    orderDetailsRef.child("orderConfirmed").setValue(true)
                                    Log.d(TAG, "updateCustomerOrderStatus: Order in the shop is deleted")
                                    finish()
                                } else {
                                    Log.e(TAG, "updateCustomerOrderStatus: Failed to delete customer order in seller", task.exception)
                                }
                            }
                        } else {
                            Log.e(TAG, "updateCustomerOrderStatus: Shop key or transaction key is null")
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@OrderDetailsActivity, "Failed to retrieve shop name", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "updateCustomerOrderStatus: Failed to retrieve shop name", error.toException())
                    }
                })
            }
            override fun onCancelled(error: DatabaseError) {  }
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
