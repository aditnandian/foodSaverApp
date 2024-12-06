package com.example.foodsaverapps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.foodsaverapps.Model.UserModel
import com.example.foodsaverapps.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(binding.root)

        // Initialization of Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        // Initialization of Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // login with email and password
        binding.buttonLogin.setOnClickListener {
            loginUser()
        }

        binding.BackButton.setOnClickListener {
            finish()
        }

        binding.donthavebutton.setOnClickListener {
            val intent = Intent(this, RegisterCustomerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = firebaseAuth.currentUser?.uid
                    if (uid != null) {
                        val userRef = database.child("customer").child(uid)
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                Log.d("LoginActivity", "onDataChange called")
                                if (snapshot.exists()) {
                                    Log.d("LoginActivity", "User exists in database")
                                    val user = snapshot.getValue(UserModel::class.java)
                                    if (user != null) {
                                        Log.d("LoginActivity", "User model is not null")
                                        runOnUiThread {
                                            Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                            finish()
                                        }
                                    } else {
                                        Log.d("LoginActivity", "User model is null")
                                        runOnUiThread {
                                            Toast.makeText(this@LoginActivity, "User data is invalid", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Log.d("LoginActivity", "User does not exist in database")
                                    runOnUiThread {
                                        Toast.makeText(this@LoginActivity, "User does not exist", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("LoginActivity", "Database error: ${error.message}")
                                runOnUiThread {
                                    Toast.makeText(this@LoginActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                    } else {
                        Log.e("LoginActivity", "UID is null")
                    }
                } else {
                    Log.e("LoginActivity", "Authentication failed: ${task.exception?.message}")
                    runOnUiThread {
                        Toast.makeText(this, "Invalid Email or password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Please enter your Email and Password", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            val userRef = database.child("customer").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(UserModel::class.java)
                        if (user != null) {
                            loggedin()
                        } else {
                            firebaseAuth.signOut()
                            setContentView(binding.root)
                        }
                    } else {
                        firebaseAuth.signOut()
                        setContentView(binding.root)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("LoginActivity", "Database error: ${error.message}")
                }
            })
        } else {
            firebaseAuth.signOut()
            setContentView(binding.root)
        }
    }

    private fun loggedin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
