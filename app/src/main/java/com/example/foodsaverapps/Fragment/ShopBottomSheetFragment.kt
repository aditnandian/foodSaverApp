package com.example.foodsaverapps.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.example.foodsaverapps.Adapter.ShopAdapter
import com.example.foodsaverapps.Model.ShopModel
import com.example.foodsaverapps.databinding.ShopBottomSheetFragmentBinding
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShopBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: ShopBottomSheetFragmentBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var shops: MutableList<ShopModel>
    private lateinit var sellers: MutableList<String>
    private val TAG = "ShopBottomSheetFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ShopBottomSheetFragmentBinding.inflate(inflater, container, false)
        binding.buttonBack.setOnClickListener {
            dismiss()
        }
        val type = arguments?.getString("type")
        retrieveShopsWithCategory(type)
        return binding.root
    }

    private fun retrieveShopsWithCategory(category: String?) {
        if (category == null) return
        database = FirebaseDatabase.getInstance()
        val shopRef = database.reference.child("shop")
        shops = mutableListOf()
        sellers = mutableListOf()

        shopRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (shopSnapshot in snapshot.children) {
                    val shopKey = shopSnapshot.key ?: continue
                    val menuRef = shopRef.child(shopKey).child("menu")
                    menuRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(menuSnapshot: DataSnapshot) {
                            for (menu in menuSnapshot.children) {
                                val foodCategory = menu.child("foodCategory").getValue(String::class.java)
                                if (foodCategory == category) {
                                    val shop = shopSnapshot.getValue(ShopModel::class.java)
                                    if (shop != null) {
                                        shops.add(shop)
                                        sellers.add(shopKey)  // Assuming shopKey is the sellerId
                                    }
                                    break
                                }
                            }
                            setAdapter(shops, sellers)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "Error retrieving menu items: ${error.message}")
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error retrieving shops: ${error.message}")
            }
        })
    }

    private fun setAdapter(shops: List<ShopModel>, sellers: List<String>) {
        if (shops.isNotEmpty()) {
            val adapter = ShopAdapter(shops, sellers, requireContext())
            binding.shopRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.shopRecyclerView.adapter = adapter
        }
    }
}
