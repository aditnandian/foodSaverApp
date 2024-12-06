package com.example.foodsaverapps.Fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodsaverapps.Adapter.ShopAdapter
import com.example.foodsaverapps.Model.AllMenu
import com.example.foodsaverapps.Model.ShopModel
import com.example.foodsaverapps.databinding.FragmentSearchBinding
import com.google.firebase.database.*

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var shopAdapter: ShopAdapter
    private lateinit var database: FirebaseDatabase
    private val originalMenuItems = mutableListOf<AllMenu>()
    private val originalShops = mutableListOf<ShopModel>()
    private val originalSellerKeys = mutableListOf<String>()
    private val menuIdToSellerIdMap = mutableMapOf<String, String>() // Map to store menuId to sellerId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        // Initialize the adapter with empty lists
        shopAdapter = ShopAdapter(emptyList(), emptyList(), requireContext())

        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRecyclerView.adapter = shopAdapter

        // Retrieve the seller keys
        retrieveSellerKeys()

        // Setup the search view
        setupSearchView()

        return binding.root
    }

    private fun retrieveSellerKeys() {
        val database = FirebaseDatabase.getInstance()
        val shopRef = database.reference.child("shop")

        shopRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (sellerSnapshot in snapshot.children) {
                    val sellerKey = sellerSnapshot.key
                    val shop = sellerSnapshot.getValue(ShopModel::class.java)
                    sellerKey?.let {
                        originalSellerKeys.add(it)
                        shop?.let { shopModel ->
                            originalShops.add(shopModel)
                        }
                    }
                }
                for (key in originalSellerKeys) {
                    retrieveMenuItem(key)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun retrieveMenuItem(seller: String) {
        database = FirebaseDatabase.getInstance()
        val foodReference: DatabaseReference = database.reference.child("shop/$seller/menu")
        foodReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(AllMenu::class.java)
                    menuItem?.let {
                        originalMenuItems.add(it)
                        val menuId = foodSnapshot.key ?: ""
                        menuIdToSellerIdMap[menuId] = seller // Map menuId to sellerId
                        Log.d("SearchFragment", "Retrieved menuId: $menuId for sellerId: $seller") // Log the menuId and sellerId
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun setShopAdapter(shops: List<ShopModel>, sellers: List<String>) {
        shopAdapter = ShopAdapter(shops, sellers, requireContext())
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRecyclerView.adapter = shopAdapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterResults(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterResults(newText)
                return true
            }
        })
    }

    private fun filterResults(query: String) {
        if (query.isEmpty()) {
            // Clear the adapter if the query is empty
            setShopAdapter(emptyList(), emptyList())
        } else {
            // Filter menu items by food name
            val filteredMenuItems = originalMenuItems.filter {
                it.foodName?.contains(query, ignoreCase = true) == true
            }
            val filteredSellerKeysForMenus = filteredMenuItems.mapNotNull { menuItem ->
                menuIdToSellerIdMap[menuItem.key]
            }.distinct()

            // Filter shops by shop name or seller key
            val filteredShops = originalShops.filter { shop ->
                shop.name?.contains(query, ignoreCase = true) == true ||
                        filteredSellerKeysForMenus.contains(originalSellerKeys[originalShops.indexOf(shop)])
            }
            val filteredSellerKeysForShops = filteredShops.map { shop ->
                originalSellerKeys[originalShops.indexOf(shop)]
            }
            setShopAdapter(filteredShops, filteredSellerKeysForShops)
        }
    }

    companion object {
    }
}
