package com.example.foodsaverapps.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodsaverapps.Model.ShopModel
import com.example.foodsaverapps.R
import com.example.foodsaverapps.ShoppingActivity
import com.example.foodsaverapps.databinding.ShopListBinding

class ShopAdapter(
    private val shops: List<ShopModel>,
    private val sellers: List<String>,
    private val context: Context
) : RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val binding = ShopListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val shop = shops[position]
        val seller = sellers[position]
        holder.bind(shop, seller)
    }

    override fun getItemCount(): Int = shops.size

    inner class ShopViewHolder(private val binding: ShopListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(shop: ShopModel, seller: String) {
            binding.apply {
                shopName.text = shop.name

                val address = shop.address ?: ""
                val displayAddress = if (address.length > 32) {
                    "${address.substring(0, 32)}..."
                } else {
                    address
                }
                binding.shopAddress.setText(displayAddress)

                val pictureUrl = shop.pictureUrl

                if (pictureUrl != null && pictureUrl.isNotEmpty()) {
                    Glide.with(context)
                        .load(Uri.parse(pictureUrl))
                        .into(shopImage)
                } else {
                    // Set a placeholder image if pictureUrl is null or empty
                    shopImage.setImageResource(R.drawable.profpic)
                }

                binding.root.setOnClickListener {
                    val intent = Intent(context, ShoppingActivity::class.java).apply {
                        putExtra("sellerId", seller)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}
