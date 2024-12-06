package com.example.foodsaverapps.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodsaverapps.databinding.ItemVoucherBinding
import com.example.foodsaverapps.Model.VoucherItem
import com.google.firebase.database.DatabaseReference

class VoucherItemAdapter(
    private val context: Context,
    private val discList: ArrayList<VoucherItem>?,
    databaseReference: DatabaseReference,
    private val onItemClickListener:(position :Int) ->Unit,
    private val onDeleteClickListener:(position :Int) ->Unit
) : RecyclerView.Adapter<VoucherItemAdapter.AddItemViewHolder>() {
    private val itemQuantities = IntArray(discList?.size?:0) { 1 }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddItemViewHolder {
        val binding = ItemVoucherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddItemViewHolder, position: Int) {
        if (discList.isNullOrEmpty()) {
            holder.empty()
        } else {
            holder.bind(position)
        }
    }

    override fun getItemCount(): Int {
        return if (discList.isNullOrEmpty()) { 1 }
        else { discList.size }
    }

    inner class AddItemViewHolder(private val binding: ItemVoucherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            discList?.getOrNull(position)?.let { discItem ->
                binding.apply {
                    val quantity = itemQuantities[position]
                    val cont = "Min. Rp. ${discItem.voucherCondition}"
                    val percent = "${discItem.discount}%"
                    container.setOnClickListener(null)
                    DiscText.text = discItem.voucherName
                    DiscText1.text = cont
                    DiscText3.text = percent
                    DiscText4.setOnClickListener {
                        onDeleteClickListener(position)
                    }
                    DiscText5.setOnClickListener {
                        onItemClickListener(position)
                    }
                }
            }
        }
        fun empty() {
            binding.apply {
                container.setOnClickListener {
                    onItemClickListener(-1)
                }
                DiscText.text = "No Discounts Yet"
                DiscText1.text = "Tap here to add discounts"
                DiscText3.text = ""
//                DiscText3.hint = ""
                DiscText4.setOnClickListener(null)
                DiscText5.setOnClickListener(null)
                DiscText4.visibility = View.INVISIBLE
                DiscText5.visibility = View.INVISIBLE
            }
        }
    }
}