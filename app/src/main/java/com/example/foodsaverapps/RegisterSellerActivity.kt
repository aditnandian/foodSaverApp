package com.example.foodsaverapps

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodsaverapps.Model.UserModel2
import com.example.foodsaverapps.databinding.ActivityRegisterCustomerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterSellerActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityRegisterCustomerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase auth and database
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

//        binding.spinnerCountryCode.setSelection(19)

        binding.GoRegister.setOnClickListener {
            val fullName = binding.editTextFullName.text.toString()
            val phoneNumber = binding.editTextPhoneNumber.text.toString()
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            val confirmPassword = binding.editTextConfirmPassword.text.toString()

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "You must fill all the fields", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Confirm Password does not match", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            } else {
                // Create account with email and password
                createAccount(email, password)
            }
        }

        binding.BackButton.setOnClickListener {
            finish()
        }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Registration successful, save user data
                    val userId = auth.currentUser!!.uid
                    val fullName = binding.editTextFullName.text.toString().trim()
                    var phoneNumber = binding.editTextPhoneNumber.text.toString().trim()
                    val email = binding.editTextEmail.text.toString().trim()
                    val password = binding.editTextPassword.text.toString().trim()

                    if (phoneNumber.first() != '0') {
                        phoneNumber = "0$phoneNumber";
                    }

                    val user = UserModel2(fullName, email, phoneNumber, password)
                    database.child("seller").child(userId).setValue(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                            // Start MainActivity and finish current activity
                            startActivity(Intent(this, ShopProfileActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Registration Data Failure: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Registration failed
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
