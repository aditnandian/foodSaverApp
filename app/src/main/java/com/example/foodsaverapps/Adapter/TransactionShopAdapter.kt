package com.example.foodsaverapps.Adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodsaverapps.Model.OrderDetails
import com.example.foodsaverapps.OrderDetailsActivity
import com.example.foodsaverapps.OrderDetailsShopActivity
import com.example.foodsaverapps.databinding.ItemTransactionBinding

class TransactionShopAdapter(
    private var transactionList: List<OrderDetails>
) : RecyclerView.Adapter<TransactionShopAdapter.TransactionViewHolder>() {

    fun updateList(newList: List<OrderDetails>) {
        transactionList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactionList[position])
    }

    override fun getItemCount(): Int = transactionList.size

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(orderDetails: OrderDetails) {
            binding.textViewTransactionId.text = "FS-" + orderDetails.orderKey.toString()
            binding.textViewTransactionShop.text = orderDetails.userName ?: ""
            binding.textViewTransactionStatus.text = orderDetails.status ?: ""
            binding.textViewTransactionAmount.text = "Rp" + orderDetails.totalPrice.toString()
            binding.textViewTransactionDate.text = orderDetails.currentTime

            binding.checkButton.setOnClickListener {
                val context = it.context
                val intent = Intent(context, OrderDetailsShopActivity::class.java)
                intent.putExtra("itemPushKey", orderDetails.itemPushKey)
                intent.putExtra("status", orderDetails.status)
                context.startActivity(intent)
            }
            // If you have date information in OrderDetails, bind it here
            // binding.textViewTransactionDate.text = orderDetails.currentTime.toString()
        }
    }
}
