package com.example.foodsaverapps.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.foodsaverapps.databinding.ItemItemBinding
import com.example.foodsaverapps.Model.AllMenu
import com.example.foodsaverapps.R
import com.google.firebase.database.DatabaseReference

class MenuItemAdapter(
    private val context: Context,
    private val menuList: ArrayList<AllMenu>?,
    databaseReference: DatabaseReference,
    private val onItemClickListener:(position :Int) ->Unit,
    private val onDeleteClickListener:(position :Int) ->Unit,
    private val onSwitchChangeListener:(position :Int,isChecked: Boolean) ->Unit
) : RecyclerView.Adapter<MenuItemAdapter.AddItemViewHolder>() {
    private val itemQuantities = IntArray(menuList?.size?:0) { 1 }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddItemViewHolder {
        val binding = ItemItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        return if (menuList.isNullOrEmpty()) { 1 }
        else { menuList.size }
    }

    inner class AddItemViewHolder(private val binding: ItemItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            menuList?.getOrNull(position)?.let { menuItem ->
                binding.apply {
                    val quantity = itemQuantities[position]
                    val uriString = menuItem.foodImage
                    val uri = Uri.parse(uriString)
                    val price = "Rp${menuItem.foodPrice}"
                    val avail: Switch = binding.DiscBool
                    container.setOnClickListener(null)
                    DiscText.text = menuItem.foodName
                    DiscText1.text = price
                    Glide.with(context).load(uri).transform(CenterCrop(), RoundedCorners(20)).into(discic)
//                    DiscText3.text = menuItem.foodStock.toString()
                    avail.isEnabled = true
                    avail.isChecked = menuItem.foodAvailable ?: false
                    DiscText4.setOnClickListener {
                        onDeleteClickListener(position)
                    }
                    DiscText5.setOnClickListener {
                        onItemClickListener(position)
                    }
                    avail.setOnCheckedChangeListener{ _, isChecked ->
                        onSwitchChangeListener(position, isChecked)
                    }
                }
            }
        }
        fun empty() {
            binding.apply {
                container.setOnClickListener {
                    onItemClickListener(-1)
                }
                DiscText.text = "No Products Yet"
                DiscText1.text = "Tap here to add products"
                Glide.with(context).clear(discic) // Clear any previous image
//                discic.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.plusprdct, null))
                discic.setBackgroundResource(R.drawable.plusprdct)
                DiscText2.text = ""
//                DiscText3.text = ""
                DiscBool.isEnabled = false
                DiscBool.isChecked = false
                DiscBool.visibility = View.INVISIBLE
                DiscText4.setOnClickListener(null)
                DiscText5.setOnClickListener(null)
                DiscText4.visibility = View.INVISIBLE
                DiscText5.visibility = View.INVISIBLE
            }
        }

//        private fun deleteQuantitiy(position: Int) {
//            menuList.removeAt(position)
//            menuList.removeAt(position)
//            menuList.removeAt(position)
//            notifyItemRemoved(position)
//            notifyItemRangeChanged(position, menuList.size)
//        }

    }
}