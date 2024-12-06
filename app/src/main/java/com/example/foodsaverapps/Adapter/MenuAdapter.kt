package com.example.foodsaverapps.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodsaverapps.Model.AllMenu
import com.example.foodsaverapps.ShoppingActivity
import com.example.foodsaverapps.databinding.MenuItemBinding
import com.google.firebase.database.*

class MenuAdapter(
    private val menuItems: List<AllMenu>,
    private val requireContext: Context,
    private val sellers: List<String>,
    private val menuIds: List<String>,
    private val menuIdToSellerIdMap: Map<String, String>
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = MenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuItem = menuItems[position]
        val menuId = menuIds[position]
        val sellerId = menuIdToSellerIdMap[menuId] ?: ""
        holder.bind(menuItem, sellerId, menuId)
    }

    override fun getItemCount(): Int = menuItems.size

    inner class MenuViewHolder(private val binding: MenuItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    openDetailsActivity(position)
                }
            }
        }

        private fun openDetailsActivity(position: Int) {
            val menuItem = menuItems[position]
            val menuId = menuIds[position]
            val sellerId = menuIdToSellerIdMap[menuId] ?: ""

            // Retrieve shop name from Firebase
            val databaseReference = FirebaseDatabase.getInstance().reference
            val shopNameRef = databaseReference.child("shop").child(sellerId).child("name")

            shopNameRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val shopName = dataSnapshot.getValue(String::class.java)

                    // Intent to open details activity and pass data
                    val intent = Intent(requireContext, ShoppingActivity::class.java).apply {
                        putExtra("MenuItemName", menuItem.foodName)
                        putExtra("MenuItemImage", menuItem.foodImage)
                        putExtra("MenuItemDescription", menuItem.foodDescription)
                        putExtra("MenuItemPrice", menuItem.foodPrice)
//                        putExtra("MenuItemStock", menuItem.foodStock)
                        putExtra("sellerId", sellerId)
                        putExtra("shopName", shopName) // Add the shop name
                    }
                    requireContext.startActivity(intent)
                }

                override fun onCancelled(databaseError: DatabaseError) {  }
            })
        }

        fun bind(menuItem: AllMenu, sellerId: String, menuId: String) {
            binding.apply {
                menuFoodName.text = menuItem.foodName
                menuPrice.text = "Rp" + menuItem.foodPrice.toString()
                val uri = Uri.parse(menuItem.foodImage)
                Glide.with(requireContext).load(uri).into(menuImage)

                // Retrieve and bind the shop name from Firebase
                val databaseReference = FirebaseDatabase.getInstance().reference
                val shopNameRef = databaseReference.child("shop").child(sellerId).child("name")

                shopNameRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val shopName = dataSnapshot.getValue(String::class.java)
                        binding.shopName.text = shopName ?: "Unknown Shop"
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        binding.shopName.text = "Unknown Shop"
                    }
                })
            }
        }
    }
}
