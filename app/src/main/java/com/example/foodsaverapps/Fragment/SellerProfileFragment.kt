package com.example.foodsaverapps.Fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.foodsaverapps.ChangePasswordActivity
import com.example.foodsaverapps.MapsActivity
import com.example.foodsaverapps.Model.UserModel
import com.example.foodsaverapps.Model.ShopModel
import com.example.foodsaverapps.R
import com.example.foodsaverapps.StartActivity
import com.example.foodsaverapps.databinding.FragmentSellerProfileBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.common.InputImage

class SellerProfileFragment : Fragment() {
    private lateinit var binding: FragmentSellerProfileBinding

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var cate: Array<String>
//    private val cate = resources.getStringArray(R.array.categorys)

    companion object {
        private const val IMAGE_PICKER_REQUEST_CODE_1 = 129
        private const val IMAGE_PICKER_REQUEST_CODE_2 = 130
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSellerProfileBinding.inflate(inflater, container, false)

        cate = resources.getStringArray(R.array.categorys)

        setUserData()

        binding.Edit2.isEnabled = false

        binding.InputImage2.setOnClickListener {
            pickImage2()
        }

        setupTextWatcher(binding.Profile4a, null, binding.Edit2)
        setupTextWatcher(binding.Profile5a, null, binding.Edit2)

        binding.Edit2.setOnClickListener{
            if (binding.Edit2.isEnabled) {
                val name = binding.Profile4a.text.toString()
                val address = binding.Profile5a.text.toString()
                binding.Edit2.isEnabled = false
                binding.Edit2.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_login_button_ngreen, null)
                updateUserData2(name, address, "")
            }
        }

        binding.shopAddress.setOnClickListener {
            val intent = Intent(requireContext(), MapsActivity::class.java)
            startActivityForResult(intent,0)
        }

        return binding.root
    }

    private fun setupTextWatcher(editText: EditText, editable: Editable?, targetEditText: AppCompatButton) {
        editText.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  }
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        targetEditText.isEnabled = true
                        targetEditText.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_login_button_green, null)
                    }
                    override fun afterTextChanged(s: Editable?) {  }
                })
            }
        }
    }

    private fun pickImage2() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(512, 512)
            .start(IMAGE_PICKER_REQUEST_CODE_2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                0 -> {
                    setUserData()
                }
                IMAGE_PICKER_REQUEST_CODE_2 -> {
                    val fileUri = data.data
                    fileUri?.let {
                        uploadImageToFirebase2(it)
                    }
                }
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebase2(fileUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("shoPictures/$userId.jpg")

        storageRef.putFile(fileUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                updateProfilePictureUri2(uri.toString())
            }
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfilePictureUri2(uri: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("shop").child(userId)
        userRef.child("pictureUrl").setValue(uri).addOnSuccessListener {
            Glide.with(this)
                .load(uri)
//                .transform(CircleCrop())
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(10)))
                .into(binding.InputImage2)
            Toast.makeText(requireContext(), "Shop picture updated", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Failed to update shop picture: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserData2(name: String, location: String, category: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = database.getReference("shop").child(userId)

            // Read current user data
            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Get current user data
                        val currentUserData = snapshot.getValue(ShopModel::class.java)

                        // Create a map to hold the updated user data
                        val updatedUserData = hashMapOf<String, Any>()

                        // Update only the fields that are being changed
                        if (name.isNotEmpty()) updatedUserData["name"] = name
                        if (location.isNotEmpty()) updatedUserData["location"] = location
//                        if (category.isNotEmpty()) updatedUserData["category"] = category

                        // Write the updated user data back to the database
                        userReference.updateChildren(updatedUserData).addOnSuccessListener {
                            Toast.makeText(requireContext(), "Updated successfully", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(requireContext(), "Update Failure", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun setUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val shopReference = database.getReference("shop").child(userId)

            shopReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val shop = snapshot.getValue(ShopModel::class.java)
                        if (shop != null) {
                            binding.Profile4a.setText(shop.name)

                            val address = shop.address ?: ""
                            val displayAddress = if (address.length > 32) {
                                "${address.substring(0, 32)}..."
                            } else {
                                address
                            }
                            binding.Profile5a.setText(displayAddress)

                            shop.pictureUrl?.let { url ->
                                if (url.isNotEmpty()) {
                                    Glide.with(this@SellerProfileFragment)
                                        .load(url)
                                        .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(10)))
                                        .into(binding.InputImage2)
                                    binding.InputImage2.elevation = 2F
                                }
                            }

                            val meal = shop.heavymeals
                            val meals = if (meal) 1 else 0
                            val drink = shop.drinks
                            val drinks = if (drink) 2 else 0
                            val snack = shop.snacks
                            val snacks = if (snack) 4 else 0
                            val categories = meals or drinks or snacks
                            if (categories > 0 && categories <= cate.size) {
                                binding.Profile8a.setText(cate[categories])
                            } else {
                                binding.Profile8a.setText(R.string.undefined)
                            }

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {  }
            })
        }
    }
}
