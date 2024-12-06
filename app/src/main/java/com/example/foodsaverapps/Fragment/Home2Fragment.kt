package com.example.foodsaverapps.Fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.foodsaverapps.Adapter.PendingOrderAdapter
import com.example.foodsaverapps.Adapter.TransactionShopAdapter
import com.example.foodsaverapps.DiscountsActivity
import com.example.foodsaverapps.ProductsActivity
import com.example.foodsaverapps.Model.OrderDetails
import com.example.foodsaverapps.Model.ShopModel
import com.example.foodsaverapps.R
import com.example.foodsaverapps.databinding.FragmentHome2Binding
import com.example.foodsaverapps.Model.UserModel2
import com.example.foodsaverapps.RevenueActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class Home2Fragment : Fragment() {
    private lateinit var binding: FragmentHome2Binding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val userId = auth.currentUser?.uid
    private val databaseOrders = database.reference.child("shop/$userId/OrderDetails")
    private lateinit var pendingOrderAdapter: PendingOrderAdapter
    private lateinit var transactionList: MutableList<OrderDetails>
    private var listOfOrderItem: ArrayList<OrderDetails> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHome2Binding.inflate(inflater, container, false)

        // Setup RecyclerView before loading transactions
        setupRecyclerView()

        setUserData()

        binding.imageButton1.setOnClickListener {
            val intent = Intent(requireContext(), ProductsActivity::class.java)
            startActivity(intent)
        }
        binding.imageButton2.setOnClickListener {
            val intent = Intent(requireContext(), DiscountsActivity::class.java)
            startActivity(intent)
        }
        binding.imageButton3.setOnClickListener {
            val intent = Intent(requireContext(), RevenueActivity::class.java)
            startActivity(intent)
        }
        binding.ShopImage.setOnClickListener {
            pickImage()
            Log.d("isclick","The image is clicked!")
        }

        // Retrieve and display popular menu items
        loadTransactions()

        return binding.root
    }

    private fun setupRecyclerView() {
        transactionList = mutableListOf()
        pendingOrderAdapter = PendingOrderAdapter(transactionList)
        binding.Orderlist.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pendingOrderAdapter
        }
    }

    private fun loadTransactions() {
        val userId = auth.currentUser?.uid ?: return
        val transactionsRef = database.reference.child("shop").child(userId).child("OrderDetails")

        transactionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transactionList.clear()
                for (orderSnapshot in snapshot.children) {
                    val order = orderSnapshot.getValue(OrderDetails::class.java)
                    if (order != null && order.status == "Pending") {
                        transactionList.add(order)
                    }
                }
                pendingOrderAdapter.updateList(transactionList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun setUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = database.getReference("seller").child(userId)
            val shopReference = database.getReference("shop").child(userId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userProfile = snapshot.getValue(UserModel2::class.java)
                        if (userProfile != null) {
                            val byname = "by " + userProfile.name
                            binding.textViewUser.text = byname
//                            userProfile.profilePictureUrl?.let { url ->
//                                Glide.with(this@Home2Fragment)
//                                    .load(url)
//                                    .transform(CircleCrop())
//                                    .into(binding.imageButton)
//                                binding.imageButton.background = ResourcesCompat.getDrawable(resources,R.drawable.nullify,null)
//                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {  }
            })

            shopReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val shop = snapshot.getValue(ShopModel::class.java)
                        if (shop != null) {
                            binding.textViewShop.text = shop.name
                            shop.pictureUrl?.let { url ->
                                Glide.with(this@Home2Fragment)
                                    .load(url)
                                    .transform(CenterCrop(), RoundedCorners(10))
                                    .into(binding.imageButton)
                            }
                            if (!shop.coverUrl.isNullOrEmpty()) {
                                Glide.with(this@Home2Fragment)
                                    .load(shop.coverUrl)
                                    .transform(CenterCrop(), RoundedCorners(10))
                                    .into(binding.ShopImage)
                            } else {
                                binding.ShopImage.setImageDrawable(null)
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {  }
            })
        }
    }

    private fun pickImage() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(512, 256)
            .start()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val fileUri = data?.data
            fileUri?.let {
                uploadImageToFirebase(it)
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
    private fun uploadImageToFirebase(fileUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("shopPictures/$userId.jpg")

        storageRef.putFile(fileUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                updatePictureUri(uri.toString())
            }
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updatePictureUri(uri: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("shop").child(userId)
        userRef.child("coverUrl").setValue(uri).addOnSuccessListener {
            Glide.with(this)
                .load(uri)
//                .transform(CircleCrop())
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(10)))
                .into(binding.ShopImage)
            Toast.makeText(requireContext(), "Shop picture updated", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Failed to update shop picture: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}