package com.example.foodsaverapps.Fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.foodsaverapps.MainActivity
import com.example.foodsaverapps.databinding.FragmentCongratsBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.*

class CongratsBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentCongratsBottomSheetBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var shopId: String
    private lateinit var shopPhone: String
    private var addressUrl: String? = null

    interface OnCloseRequestListener {
        fun onCloseRequest()
    }
    private var closeListener: OnCloseRequestListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Ensure the host activity implements the close request listener interface
        closeListener = context as? OnCloseRequestListener
            ?: throw ClassCastException("$context must implement OnCloseRequestedListener")
    }
    override fun onDetach() {
        super.onDetach()
        closeListener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance()
        shopId = arguments?.getString("shopKey") ?: ""
        shopPhone = arguments?.getString("shopNumber") ?: ""
        Log.d("CongratsBottomSheet", "shopKey: $shopId")  // Log the shopKey
        Log.d("CongratsBottomSheet", "shopNum: $shopPhone")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCongratsBottomSheetBinding.inflate(inflater, container, false)

        binding.goHome.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requestClose()
        }
        binding.linkSeller.setOnClickListener {
            openSellerAddressUrl()
        }
        retrieveShopDetails(shopId)  // Retrieve the shop details when the fragment is created
        return binding.root
    }

    private fun retrieveShopDetails(shopKey: String) {
        if (shopKey.isNotEmpty()) {
            val shopRef = database.reference.child("shop/$shopKey")
            val sellerRef = database.reference.child("seller/$shopKey")
            shopRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val shopName = snapshot.child("name").getValue(String::class.java)
                    addressUrl = snapshot.child("addressUrl").getValue(String::class.java)

                    if (!shopName.isNullOrEmpty()) {
                        val content = SpannableString(shopName)
                        content.setSpan(UnderlineSpan(), 0, content.length, 0)
                        binding.linkSeller.text = content  // Update the UI with the shop name as underlined text
                    } else {
                        Toast.makeText(requireContext(), "Shop name not available", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to retrieve shop details", Toast.LENGTH_SHORT).show()
                }
            })
            sellerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val seller = snapshot.child("phone").getValue(String::class.java)

                    if (!seller.isNullOrEmpty()) {
                        val contact = "Contact: $seller"
                        binding.contactSeller.text = contact
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to retrieve seller number", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun openSellerAddressUrl() {
        addressUrl?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            startActivity(intent)
        } ?: run {
            Toast.makeText(requireContext(), "Address URL not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestClose() {
        closeListener?.onCloseRequest()
    }

    companion object {
        fun newInstance(shopKey: String, shopNumber: String): CongratsBottomSheet {
            val args = Bundle()
            args.putString("shopKey", shopKey)
            args.putString("shopNumber", shopNumber)
            val fragment = CongratsBottomSheet()
            fragment.arguments = args
            return fragment
        }
    }
}
