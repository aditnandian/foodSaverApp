package com.example.foodsaverapps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.foodsaverapps.Adapter.VoucherAdapter
import com.example.foodsaverapps.Fragment.CongratsBottomSheet
import com.example.foodsaverapps.Fragment.VoucherBottomSheetFragment
import com.example.foodsaverapps.databinding.ActivityPayOutBinding
import com.example.foodsaverapps.Model.OrderDetails
import com.example.foodsaverapps.Model.VoucherItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PayOutActivity : AppCompatActivity(), VoucherAdapter.OnVoucherClickListener, CongratsBottomSheet.OnCloseRequestListener {
    lateinit var binding: ActivityPayOutBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var shopKey: String
    private var orderKey: Int = 0
    private lateinit var name: String
    private lateinit var phone: String
    private var voucherName: String? = null // Declare voucherName as nullable
    private var totalAmount: Int = 0
    private lateinit var foodItemName: ArrayList<String>
    private lateinit var foodItemPrice: ArrayList<Int>
    private lateinit var foodItemImage: ArrayList<String>
    private lateinit var foodItemDescription: ArrayList<String>
    private lateinit var foodItemQuantities: ArrayList<Int>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userId: String
    private var appliedDiscount: Int = 0

    private var shopName: String = ""  // Declare shopName variable
    private var shopNumber: String = ""

    override fun onVoucherClick(voucher: VoucherItem) {
        applyVoucher(voucher)
    }
    override fun onCloseRequest() {
        finish()
    }

    private fun applyVoucher(voucher: VoucherItem) {
        val originalTotal = calculateTotalAmount()
        val discount = voucher.discount?.toIntOrNull() ?: 0
        appliedDiscount = discount
        val discountedTotal = originalTotal - (originalTotal * discount / 100)
        binding.totalAmount.setText("Rp$discountedTotal")

        // Set voucherName here
        voucherName = voucher.voucherName ?: ""
        binding.voucherApplied.setText("$voucherName ($appliedDiscount%)")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        shopKey = intent.getStringExtra("shopKey") ?: ""

        if (shopKey.isEmpty()) {
            Toast.makeText(this, "Shop key is missing!", Toast.LENGTH_SHORT).show()
        } else {
            retrieveShopName(shopKey)  // Retrieve shopName when activity is created
            retrieveShopPhone(shopKey)
        }

        // Get intent extras
        val intent = intent
        foodItemName = intent.getStringArrayListExtra("FoodItemName") ?: arrayListOf()
        foodItemPrice = intent.getIntegerArrayListExtra("FoodItemPrice") ?: arrayListOf()
        foodItemImage = intent.getStringArrayListExtra("FoodItemImage") ?: arrayListOf()
        foodItemDescription = intent.getStringArrayListExtra("FoodItemDescription") ?: arrayListOf()
        foodItemQuantities = intent.getIntegerArrayListExtra("FoodItemQuantities") ?: arrayListOf()

        // Calculate total amount
        totalAmount = calculateTotalAmount()

        // Display total amount
        binding.totalAmount.setText("Rp$totalAmount")

        // Set click listeners
        binding.backeButton.setOnClickListener {
            finish()
        }

        binding.PlaceMyOrder.setOnClickListener {
            name = binding.name.text.toString().trim()
            phone = binding.phone.text.toString().trim()
            if (name.isBlank() || phone.isBlank()) {
                Toast.makeText(this, "Please Enter All The Details", Toast.LENGTH_SHORT).show()
            } else {
                placeOrder()
            }
        }

        binding.AddVoucher.setOnClickListener {
            val bottomSheetDialog = VoucherBottomSheetFragment.newInstance(totalAmount, shopKey)
            bottomSheetDialog.show(supportFragmentManager, "VoucherBottomSheetFragment")
        } // Set user data
        setUserData()
    }

    private fun retrieveShopName(shopKey: String) {
        val shopNameRef = databaseReference.child("shop/$shopKey/name")
        shopNameRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                shopName = snapshot.getValue(String::class.java) ?: ""
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PayOutActivity, "Failed to retrieve shop name", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun retrieveShopPhone(shopKey: String) {
        val shopNameRef = databaseReference.child("seller/$shopKey/phone")
        shopNameRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                shopNumber = snapshot.getValue(String::class.java) ?: ""
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PayOutActivity, "Failed to retrieve shop num", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun placeOrder() {
        userId = auth.currentUser?.uid ?: ""
        val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) // Use this for current time
        val itemPushKey = databaseReference.child("customer/$userId/OrderDetails").push().key
        val getOrderKey = databaseReference.child("ids").child("number")

        getOrderKey.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var okey = dataSnapshot.getValue(Int::class.java)
                if (okey != null) {
                    okey += 1
                    orderKey = okey
                    Log.d("OrderKey1", "orderKey: $orderKey")
                    databaseReference.child("ids").child("number").setValue(okey)
                    // Now that orderKey is set, proceed with creating the order
                    val orderDetails = OrderDetails(
                        userId,
                        name,
                        foodItemName,
                        foodItemImage,
                        foodItemPrice,
                        foodItemQuantities,
                        totalAmount - (totalAmount * appliedDiscount / 100),  // Adjust totalAmount with discount
                        phone,
                        false,  // orderConfirmed (default value)
                        shopKey,
                        itemPushKey,
                        currentTime, // Updated to use formatted string
                        voucherName ?: "",  // Pass voucherName if it's not null
                        "Pending",  // Set the order status
                        shopName,  // Pass the retrieved shopName here
                        appliedDiscount,
                        orderKey
                    ) // Ensure the correct path is used
                    val orderReference = databaseReference.child("shop/$shopKey/OrderDetails").child(itemPushKey!!)
                    orderReference.setValue(orderDetails).addOnSuccessListener {
                        val bottomSheetDialog = CongratsBottomSheet.newInstance(shopKey,phone)
                        Log.d("PayOutActivity", "Showing CongratsBottomSheet with shopKey: $shopKey")
                        bottomSheetDialog.show(supportFragmentManager, "CongratsBottomSheet")
                        removeItemFromCart()
                        addOrderToHistory(orderDetails)
                    }
                } else {
                    Toast.makeText(this@PayOutActivity, "Failed to retrieve order key", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {  }
        })
    }

    private fun addOrderToHistory(orderDetails: OrderDetails) {
        databaseReference.child("customer").child(userId).child("buyHistory").child(orderDetails.itemPushKey!!).setValue(orderDetails)
    }

    private fun removeItemFromCart() {
        val cartItemsReference = databaseReference.child("customer").child(userId).child("cartItems")
        cartItemsReference.removeValue()
    }

    private fun calculateTotalAmount(): Int {
        var totalAmount = 0
        for (i in 0 until foodItemPrice.size) {
            val price = foodItemPrice[i]
            val quantity = foodItemQuantities[i]
            totalAmount += price * quantity
        }
        return totalAmount
    }

    private fun setUserData() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userReference = databaseReference.child("customer").child(userId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val names = snapshot.child("name").getValue(String::class.java) ?: ""
                        val phones = snapshot.child("phone").getValue(String::class.java) ?: ""
                        binding.name.setText(names)
                        binding.phone.setText(phones)
                    }
                }
                override fun onCancelled(error: DatabaseError) {  }
            })
        }
    }
}
