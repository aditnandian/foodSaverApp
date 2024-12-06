package com.example.foodsaverapps

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodsaverapps.Adapter.TransactionShopAdapter
import com.example.foodsaverapps.Model.OrderDetails
import com.example.foodsaverapps.databinding.ActivityRevenueBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class RevenueActivity : AppCompatActivity() {
    private val binding: ActivityRevenueBinding by lazy {
        ActivityRevenueBinding.inflate(layoutInflater)
    }
    private val auth = FirebaseAuth.getInstance()
    private lateinit var database: FirebaseDatabase
    private lateinit var completedOrderReference: DatabaseReference
    private lateinit var transactionAdapter: TransactionShopAdapter
    private val transactionsList = mutableListOf<OrderDetails>()
    private val userId = auth.currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize the database reference
        database = FirebaseDatabase.getInstance()
        completedOrderReference = database.getReference("shop/$userId/OrderDetails")

        // Set up RecyclerView and Adapter
        transactionAdapter = TransactionShopAdapter(transactionsList)
        binding.Transactionlist.apply {
            layoutManager = LinearLayoutManager(this@RevenueActivity)
            adapter = transactionAdapter
        }

        // Set up the spinner for date filtering
        val spinner = findViewById<Spinner>(R.id.selectedDate)
        ArrayAdapter.createFromResource(
            this,
            R.array.date_filter_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val selectedOption = parent.getItemAtPosition(position).toString()
                loadData(selectedOption)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        binding.BackButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun loadData(selectedOption: String) {
        completedOrderReference.orderByChild("status").equalTo("Completed")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    transactionsList.clear()
                    var totalRevenue = 0
                    var completedOrderCount = 0
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

                    for (orderSnapshot in snapshot.children) {
                        val order = orderSnapshot.getValue(OrderDetails::class.java)
                        if (order != null) {
                            val orderTimeString = order.currentTime ?: ""
                            val orderTime = try {
                                dateFormat.parse(orderTimeString)?.time
                            } catch (e: ParseException) {
                                e.printStackTrace()
                                null
                            }

                            if (orderTime != null && isWithinSelectedTimePeriod(orderTime, selectedOption)) {
                                transactionsList.add(order)
                                val price = order.totalPrice ?: 0
                                totalRevenue += price
                                completedOrderCount++
                            }
                        }
                    }

                    transactionAdapter.updateList(transactionsList)
                    binding.revalue.text = "Rp. ${totalRevenue}"
                    binding.stnum.text = "$completedOrderCount"
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if necessary
                }
            })
    }

    private fun isWithinSelectedTimePeriod(orderTime: Long, selectedOption: String): Boolean {
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        val orderDate = Date(orderTime)

        return when (selectedOption) {
            "Daily" -> {
                val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                formatter.format(orderDate) == formatter.format(Date(currentTime))
            }
            "Weekly" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                orderDate.after(calendar.time)
            }
            "Monthly" -> {
                calendar.add(Calendar.MONTH, -1)
                orderDate.after(calendar.time)
            }
            "Yearly" -> {
                calendar.add(Calendar.YEAR, -1)
                orderDate.after(calendar.time)
            }
            "All" -> true
            else -> false
        }
    }
}
