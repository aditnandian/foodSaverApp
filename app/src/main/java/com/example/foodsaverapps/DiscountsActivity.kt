package com.example.foodsaverapps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import com.example.foodsaverapps.Adapter.VoucherItemAdapter
import com.example.foodsaverapps.Model.VoucherItem
import com.example.foodsaverapps.databinding.ActivityDiscountsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DiscountsActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private val auth = FirebaseAuth.getInstance()
    private var vItems: ArrayList<VoucherItem> = ArrayList()
    private val binding : ActivityDiscountsBinding by lazy {
        ActivityDiscountsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        databaseReference = FirebaseDatabase.getInstance().reference
        retrieveVouchers()

        binding.BackButton.setOnClickListener {
            finish()
        }
        binding.addbutton.setOnClickListener{
            clickvItems(-1)
        }
    }

    private fun retrieveVouchers() {
        val userId = auth.currentUser?.uid ?: return
        database = FirebaseDatabase.getInstance()
        val vRef: DatabaseReference = database.reference.child("shop/$userId/voucher")

        // fetch data from database
        vRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear existing data before populating
                vItems.clear()

                // loop for through each food item
                for (foodSnapshot in snapshot.children){
                    val vItem = foodSnapshot.getValue(VoucherItem::class.java)
                    vItem?.let {
                        vItems.add(it)
                    }
                }
                setAdapter()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", "Error: ${error.message}")
            }
        })
    }

    private fun setAdapter() {

        val adapter = VoucherItemAdapter(
            this@DiscountsActivity,
            vItems,
            databaseReference,
            onItemClickListener = { position ->
                clickvItems(position)
            },
            onDeleteClickListener = { position ->
                deletevItems(position)
            })
        binding.RecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.RecyclerView.adapter= adapter
    }
    private fun clickvItems(position: Int) {
        if (position == -1){
            val intent = Intent(this@DiscountsActivity, AddDiscountActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val vItemToEdit= vItems[position]
            val vItemKey = vItemToEdit.key
            val intent = Intent(this@DiscountsActivity, EditDiscountActivity::class.java)
            intent.putExtra("voucherItemKey", vItemKey)
            startActivity(intent)
            finish()
        }
    }

    private fun deletevItems(position: Int) {
        val userId = auth.currentUser?.uid ?: return
        val vItemToDelete= vItems[position]
        val vItemKey = vItemToDelete.key
        val vMenuReference = database.reference.child("shop/$userId/voucher").child(vItemKey!!)
        vMenuReference.removeValue().addOnCompleteListener {task ->
            if (task.isSuccessful){
                vItems.removeAt(position)
                binding.RecyclerView.adapter?.notifyItemRemoved(position)
            }else {
                Toast.makeText(this, "Item Undeleted", Toast.LENGTH_SHORT).show()
            }
        }
    }

}