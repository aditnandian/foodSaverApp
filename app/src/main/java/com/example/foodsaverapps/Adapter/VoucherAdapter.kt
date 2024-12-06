package com.example.foodsaverapps.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.foodsaverapps.Model.VoucherItem
import com.example.foodsaverapps.R
import com.example.foodsaverapps.databinding.VoucherItemBinding

class VoucherAdapter(
    private val voucherItems: List<VoucherItem>,
    private val requireContext: Context,
    private val listener: OnVoucherClickListener,
    private val totalAmount: Int
) : RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {

    interface OnVoucherClickListener {
        fun onVoucherClick(voucher: VoucherItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val binding = VoucherItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VoucherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = voucherItems.size

    inner class VoucherViewHolder(private val binding: VoucherItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onVoucherClick(voucherItems[position])
                }
            }
        }

        fun bind(position: Int) {
            val voucherItem = voucherItems[position]

            binding.apply {
                voucherName.text = voucherItem.voucherName
                voucherDescription.text = "Min. order: Rp" + voucherItem.voucherCondition
                discount.text = voucherItem.discount + "%"

                applyButton.setOnClickListener {
                    listener.onVoucherClick(voucherItem)
                }

                val condition = voucherItem.voucherCondition?.toIntOrNull() ?: 0
                applyButton.isEnabled = totalAmount >= condition

                if (applyButton.isEnabled) {
                    applyButton.background = ResourcesCompat.getDrawable(requireContext.resources, R.drawable.bg_login_button_green, null)
                } else {
                    applyButton.background = ResourcesCompat.getDrawable(requireContext.resources, R.drawable.bg_login_button_ngreen, null)
                }
            }
        }
    }
}
