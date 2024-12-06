package com.example.foodsaverapps.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.foodsaverapps.DetailsActivity
import com.example.foodsaverapps.Model.AllMenu
import com.example.foodsaverapps.R
import com.example.foodsaverapps.databinding.MenuShopBinding
import com.google.firebase.database.DatabaseReference

class MenuItem2Adapter(
    private val context: Context,
    private val menuList: ArrayList<AllMenu>?,
    private var quantities: IntArray,
    databaseReference: DatabaseReference,
    private val onPlusClickListener: (position: Int) -> Unit,
    private val onMinusClickListener: (position: Int) -> Unit
) : RecyclerView.Adapter<MenuItem2Adapter.AddItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddItemViewHolder {
        val binding = MenuShopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddItemViewHolder, position: Int) {
        if (menuList.isNullOrEmpty()) {
            holder.empty()
        } else {
            holder.bind(position)
        }
    }

    override fun getItemCount(): Int {
        return if (menuList.isNullOrEmpty()) 1 else menuList.size
    }

    inner class AddItemViewHolder(val binding: MenuShopBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            menuList?.getOrNull(position)?.let { menuItem ->
                binding.apply {
                    val quantity = quantities[position]
                    val uriString = menuItem.foodImage
                    val uri = Uri.parse(uriString)
                    val price = "Rp. ${menuItem.foodPrice}"
                    val shelfLife = "berlaku " + menuItem.foodShelfLife + " lagi"
                    container.setOnClickListener {
                        val intent = Intent(context, DetailsActivity::class.java).apply {
                            putExtra("MenuItemName", menuItem.foodName)
                            putExtra("MenuItemPrice", menuItem.foodPrice)
                            putExtra("MenuItemImage", menuItem.foodImage)
//                            putExtra("MenuItemStock", menuItem.foodStock)
                            putExtra("MenuItemDescription", menuItem.foodDescription)
                            putExtra("MenuItemShelfLife", menuItem.foodShelfLife)
                        }
                        context.startActivity(intent)
                    }
                    DiscText.text = menuItem.foodName
                    DiscText1.text = price
                    foodShelfLife.text = shelfLife
                    Glide.with(context).load(uri).transform(CenterCrop(), RoundedCorners(20)).into(discic)

                    if (menuItem.foodAvailable == true) {
                        DiscText2.visibility = View.VISIBLE
                        DiscText2.isEnabled = true
                        DiscText2.setOnClickListener {
                            onPlusClickListener(position)
                            if (quantities[position] == 1) {
                                DiscText2.visibility = View.INVISIBLE
                                DiscText2.isEnabled = false
                            }
                            DNum.text = quantities[position].toString()
                        }
                        DMin.setOnClickListener {
                            onMinusClickListener(position)
                            DNum.text = quantities[position].toString()

                            if (quantities[position] <= 0) {
                                DiscText2.visibility = View.VISIBLE
                                DiscText2.isEnabled = true
                            } else if (!DPlus.isEnabled) {
                                DPlus.isEnabled = true
                                DPlus.setBackgroundResource(R.drawable.bg_login_button_green)
                            }
                        }
                        DPlus.setOnClickListener {
                            onPlusClickListener(position)
                            //quantities[position]++
                            DNum.text = quantities[position].toString()

                            if (quantities[position] >= 99) {
                                DPlus.isEnabled = false
                                DPlus.setBackgroundResource(R.drawable.bg_login_button_ngreen)
                            }
                        } // Update quantity display
                        DNum.text = quantity.toString()
                    } else {
                        DiscText1.text = ""
                        DiscText1.hint = context.getString(R.string.unavail)
                        DiscText2.visibility = View.INVISIBLE
                        DiscText2.isEnabled = false
                        DPlus.visibility = View.GONE
                        DMin.visibility = View.GONE
                        DNum.visibility = View.GONE
                        discic.alpha = 0.5F
                        DiscText.alpha = 0.5F
                        DiscText1.alpha = 0.5F
                    }
                }
            }
        }

        fun empty() {
            binding.apply {
                DiscText.text = context.getString(R.string.nomenua)
                DiscText1.text = context.getString(R.string.nomenub)
                Glide.with(context).clear(discic) // Clear any previous image
                DiscText2.setOnClickListener(null)
                DPlus.setOnClickListener(null)
                DMin.setOnClickListener(null)
                field.setOnClickListener(null)
                DiscText2.visibility = View.GONE
                DPlus.visibility = View.GONE
                DMin.visibility = View.GONE
                DNum.visibility = View.GONE
            }
        }
    }
}
