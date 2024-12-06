package com.example.foodsaverapps.Fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.foodsaverapps.Adapter.VoucherAdapter
import com.example.foodsaverapps.Model.VoucherItem
import com.example.foodsaverapps.databinding.FragmentVoucherBottomSheetBinding
import com.google.firebase.database.*

class VoucherBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentVoucherBottomSheetBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var voucherItems: MutableList<VoucherItem>
    private var listener: VoucherAdapter.OnVoucherClickListener? = null

    private var totalAmount: Int = 0
    private var shopKey: String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is VoucherAdapter.OnVoucherClickListener) {
            listener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            totalAmount = it.getInt(ARG_TOTAL_AMOUNT, 0)
            shopKey = it.getString(Shop_Key, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVoucherBottomSheetBinding.inflate(inflater, container, false)
        binding.buttonBack.setOnClickListener {
            dismiss()
        }
        retrieveVoucherItems()
        return binding.root
    }

    private fun retrieveVoucherItems() {
        database = FirebaseDatabase.getInstance()
        val voucherRef: DatabaseReference = database.reference.child("shop/$shopKey/voucher")
        voucherItems = mutableListOf()

        voucherRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val key = foodSnapshot.child("key").getValue(String::class.java)
                    val voucherName = foodSnapshot.child("voucherName").getValue(String::class.java)
                    val discount = foodSnapshot.child("discount").getValue(String::class.java)
                    val voucherConditionStr = foodSnapshot.child("voucherCondition").getValue(String::class.java)

                    // Convert voucherCondition from String to Int
                    val voucherCondition = voucherConditionStr?.toIntOrNull() ?: 0

                    val voucherItem = VoucherItem(
                        key,
                        voucherName,
                        discount,
                        voucherCondition.toString() // Convert back to String if needed
                    )

                    voucherItems.add(voucherItem)
                }
                setAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun setAdapter() {
        if (voucherItems.isNotEmpty()) {
            if (listener != null) {
                val adapter = VoucherAdapter(voucherItems, requireContext(), listener!!, totalAmount)
                binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.menuRecyclerView.adapter = adapter
            }
        }
    }

    companion object {
        private const val ARG_TOTAL_AMOUNT = "total_amount"
        private const val Shop_Key = "shop_key"

        @JvmStatic
        fun newInstance(totalAmount: Int, shopKey: String): VoucherBottomSheetFragment {
            return VoucherBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TOTAL_AMOUNT, totalAmount)
                    putString(Shop_Key, shopKey)
                }
            }
        }
    }
}
