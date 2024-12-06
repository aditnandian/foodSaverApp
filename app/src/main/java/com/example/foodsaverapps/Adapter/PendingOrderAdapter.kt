package com.example.foodsaverapps.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodsaverapps.Model.OrderDetails
import com.example.foodsaverapps.OrderDetailsShopActivity
import com.example.foodsaverapps.databinding.PendingOrdersItemBinding

class PendingOrderAdapter(
    private var transactionList: List<OrderDetails>
) : RecyclerView.Adapter<PendingOrderAdapter.PendingOrderViewHolder>() {

    fun updateList(newList: List<OrderDetails>) {
        transactionList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingOrderAdapter.PendingOrderViewHolder {
        val binding = PendingOrdersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PendingOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PendingOrderAdapter.PendingOrderViewHolder, position: Int) {
        holder.bind(transactionList[position])
    }

    override fun getItemCount(): Int = transactionList.size

    inner class PendingOrderViewHolder(private val binding: PendingOrdersItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(orderDetails: OrderDetails) {
            binding.customerName.text = orderDetails.userName ?: ""
            binding.orderMoney.text = "Rp" + orderDetails.totalPrice.toString()

            // Load first food image using Glide
            val imageUrl = orderDetails.foodImages?.firstOrNull() ?: ""
            Glide.with(binding.orderFoodImage.context)
                .load(imageUrl)
                .into(binding.orderFoodImage)

            binding.orderedButton.setOnClickListener {
                val context = it.context
                val intent = Intent(context, OrderDetailsShopActivity::class.java)
                intent.putExtra("itemPushKey", orderDetails.itemPushKey)
                intent.putExtra("status", orderDetails.status)
                context.startActivity(intent)
            }
        }

        private fun showToast(message: String) {
            Toast.makeText(binding.root.context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
