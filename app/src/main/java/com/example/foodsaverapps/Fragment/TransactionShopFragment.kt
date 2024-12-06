package com.example.foodsaverapps.Fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodsaverapps.Adapter.TransactionShopAdapter
import com.example.foodsaverapps.Model.OrderDetails
import com.example.foodsaverapps.R
import com.example.foodsaverapps.databinding.FragmentTransactionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TransactionShopFragment : Fragment() {

    private lateinit var binding: FragmentTransactionBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var transactionAdapter: TransactionShopAdapter
    private lateinit var transactionList: MutableList<OrderDetails>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupRecyclerView()
        loadTransactions()

        binding.buttonPending.setOnClickListener {
            filterTransactions("Pending")
            greyout()
            binding.buttonPending.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_login_register_button_green, null)
            binding.buttonPending.setTextColor(Color.parseColor("#FFFFFF"))
        }
        binding.buttonActive.setOnClickListener {
            filterTransactions("Active")
            greyout()
            binding.buttonActive.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_login_register_button_green, null)
            binding.buttonActive.setTextColor(Color.parseColor("#FFFFFF"))
        }
        binding.buttonCompleted.setOnClickListener {
            filterTransactions("Completed")
            greyout()
            binding.buttonCompleted.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_login_register_button_green, null)
            binding.buttonCompleted.setTextColor(Color.parseColor("#FFFFFF"))
        }
        binding.buttonCancelled.setOnClickListener {
            filterTransactions("Cancelled")
            greyout()
            binding.buttonCancelled.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_login_register_button_green, null)
            binding.buttonCancelled.setTextColor(Color.parseColor("#FFFFFF"))
        }

        return binding.root
    }

    private fun greyout() {
        binding.buttonPending.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_login_register_button_light_green, null)
        binding.buttonPending.setTextColor(Color.parseColor("#3E721D"))
        binding.buttonActive.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_login_register_button_light_green, null)
        binding.buttonActive.setTextColor(Color.parseColor("#3E721D"))
        binding.buttonCompleted.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_login_register_button_light_green, null)
        binding.buttonCompleted.setTextColor(Color.parseColor("#3E721D"))
        binding.buttonCancelled.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_login_register_button_light_green, null)
        binding.buttonCancelled.setTextColor(Color.parseColor("#3E721D"))
    }

    private fun setupRecyclerView() {
        transactionList = mutableListOf()
        transactionAdapter = TransactionShopAdapter(transactionList)
        binding.recyclerViewTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }
    }

    private fun loadTransactions() {
        val userId = auth.currentUser?.uid ?: return
        val transactionsRef = database.reference.child("shop").child(userId).child("OrderDetails")

        transactionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transactionList.clear()
                for (orderSnapshot in snapshot.children) {
                    val order = orderSnapshot.getValue(OrderDetails::class.java)
                    order?.let { transactionList.add(it) }
                }
                transactionAdapter.notifyDataSetChanged()
                updateUI()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun filterTransactions(status: String) {
        val filteredList = transactionList.filter { it.status == status }
        transactionAdapter.updateList(filteredList)
    }

    private fun updateUI() {
        if (transactionList.isEmpty()) {
            binding.textViewNoTransactions.visibility = View.VISIBLE
            binding.recyclerViewTransactions.visibility = View.GONE
        } else {
            binding.textViewNoTransactions.visibility = View.GONE
            binding.recyclerViewTransactions.visibility = View.VISIBLE
        }
    }


}
