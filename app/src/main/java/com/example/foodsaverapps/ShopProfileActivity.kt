package com.example.foodsaverapps

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.foodsaverapps.Model.ShopModel
import com.example.foodsaverapps.databinding.ActivityShopProfileBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ShopProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShopProfileBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var imageUri: Uri? = null
    private var locationUri: Uri? = null

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val TAG = "ShopProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.ShopImage.setOnClickListener {
            pickImage2()
        }

        binding.ShopView.setOnClickListener {
            pickImage()
        }

        binding.buttonComplete.setOnClickListener {
            val name = binding.ShopName.text.toString()
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Please enter your Shop Name", Toast.LENGTH_SHORT).show()
            } else if (imageUri == null) {
                Toast.makeText(this, "Please upload the Shop Picture", Toast.LENGTH_SHORT).show()
            } else {
                uploadData(name)
            }
        }

        binding.buttonMaps.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivityForResult(intent,0)
        }
        getSelectedLocation()
    }

    private fun getSelectedLocation() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val shop = database.getReference("shop").child(userId)
            shop.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val shop = snapshot.getValue(ShopModel::class.java)
                        if (shop != null) {
                            if (shop.address != null) {
                                val address = shop.address
                                val displayAddress = if (address.length > 80) {
                                    "${address.substring(0, 80)}..."
                                } else {
                                    address
                                }
                                binding.currentLocation.setText(displayAddress)
                            } else {
                                binding.currentLocation.text = ""
                                binding.currentLocation.hint = "Location not yet defined."
                            }

                            shop.pictureUrl?.let { url ->
                                if (url.isNotEmpty()) {
                                    Glide.with(this@ShopProfileActivity)
                                        .load(url)
                                        .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(10)))
                                        .into(binding.ShopView)
                                    binding.ShopView.elevation = 2F
                                }
                            }
                            shop.pictureUrl?.let { url ->
                                if (url.isNotEmpty()) {
                                    Glide.with(this@ShopProfileActivity)
                                        .load(url)
                                        .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(10)))
                                        .into(binding.ShopView)
                                    binding.ShopView.elevation = 2F
                                }
                            }

                        } else {
                            binding.currentLocation.text = ""
                            binding.currentLocation.hint = "Location not yet defined."
                        }
                    } else {
                        binding.currentLocation.text = ""
                        binding.currentLocation.hint = "Location not yet defined."
                    }
                } override fun onCancelled(error: DatabaseError) {  }
            })
        }
    }

    private fun pickImage() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(512, 512)
            .start(2)
    }
    private fun pickImage2() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(512, 256)
            .start(1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                0 -> {
                    getSelectedLocation()
                }
                1 -> {
                    val fileUri = data.data
                    fileUri?.let {
                        uploadImageToFirebase2(it)
                        imageUri = it
                    }
                }
                2 -> {
                    val fileUri = data.data
                    fileUri?.let {
                        uploadImageToFirebase(it)
                        imageUri = it
                    }
                }
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebase(fileUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("shoPictures/$userId.jpg")

        storageRef.putFile(fileUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                updateShopPictureUri(uri.toString())
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Upload failed: ${e.message}")
        }
    }
    private fun uploadImageToFirebase2(fileUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("shopPictures/$userId.jpg")

        storageRef.putFile(fileUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                updateShopPictureUri2(uri.toString())
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Upload failed: ${e.message}")
        }
    }

    private fun updateShopPictureUri(uri: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("shop").child(userId)

        userRef.child("pictureUrl").setValue(uri).addOnSuccessListener {
            Glide.with(this)
                .load(uri)
                .transform(CenterCrop(), RoundedCorners(10))
                .into(binding.ShopView)
            binding.ShopView.elevation = 1F
            Toast.makeText(this, "Shop picture uploaded", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failure: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Failure: ${e.message}")
        }
    }
    private fun updateShopPictureUri2(uri: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("shop").child(userId)

        userRef.child("coverUrl").setValue(uri).addOnSuccessListener {
            Glide.with(this)
                .load(uri)
                .transform(CenterCrop(), RoundedCorners(10))
                .into(binding.ShopImage)
            binding.ShopImage.elevation = 2F
            Toast.makeText(this, "Shop cover uploaded", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failure: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Failure: ${e.message}")
        }
    }

    private fun uploadData(name: String) {
        val userId = auth.currentUser?.uid ?: return
        val shopRef = database.getReference("shop").child(userId)
        val newShop = mapOf<String, Any>(
            "name" to name,
            "heavymeals" to false,
            "snacks" to false,
            "drinks" to false
        )
        shopRef.updateChildren(newShop)
            .addOnSuccessListener {
                Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@ShopProfileActivity, Main2Activity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save data: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to save data: ${e.message}")
            }
    }
}
