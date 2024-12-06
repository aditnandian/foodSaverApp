package com.example.foodsaverapps

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodsaverapps.Model.UserModel
import com.example.foodsaverapps.databinding.ActivityProfileInformationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileInformation2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileInformationBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUserData()

        binding.apply {
            name.isEnabled = true
            email.isEnabled = false
            phone.isEnabled = true

//            editButton.setOnClickListener {
//                name.isEnabled = !name.isEnabled
//                phone.isEnabled = !phone.isEnabled
//            }

            changeSettings.setOnClickListener {
                val name = binding.name.text.toString()
                val email = binding.email.text.toString()
                val phone = binding.phone.text.toString()

                updateUserData(name, email, phone)
            }
        }

        binding.imageButtonBack.setOnClickListener {
            finish()
        }
    }

    private fun updateUserData(name: String, email: String, phone: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = database.getReference("seller").child(userId)

            // Read current user data
            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Get current user data
                        val currentUserData = snapshot.getValue(UserModel::class.java)

                        // Create a map to hold the updated user data
                        val updatedUserData = hashMapOf<String, Any>()

                        // Update only the fields that are being changed
                        if (name.isNotEmpty()) updatedUserData["name"] = name
                        if (email.isNotEmpty()) updatedUserData["email"] = email
                        if (phone.isNotEmpty()) updatedUserData["phone"] = phone

                        // Write the updated user data back to the database
                        userReference.updateChildren(updatedUserData).addOnSuccessListener {
                            Toast.makeText(this@ProfileInformation2Activity, "Profile Update successfully", Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK)
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this@ProfileInformation2Activity, "Profile Update Failed", Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_CANCELED)
                            finish()
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
            val userReference = database.getReference("seller").child(userId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userProfile = snapshot.getValue(UserModel::class.java)
                        if (userProfile != null) {
                            binding.name.setText(userProfile.name)
                            binding.email.setText(userProfile.email)
                            binding.phone.setText(userProfile.phone)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }
}
