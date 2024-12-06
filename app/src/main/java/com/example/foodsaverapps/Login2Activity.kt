package com.example.foodsaverapps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.foodsaverapps.Model.ShopModel
import com.example.foodsaverapps.databinding.ActivityLoginBinding
import com.example.foodsaverapps.Model.UserModel2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Login2Activity : AppCompatActivity() {
    private var userName: String? = null
    private lateinit var role: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(binding.root)

// Initialization of Firebase Auth
        firebaseAuth = Firebase.auth
        // Initialization of Firebase Database
        database = Firebase.database.reference

        // login with email and password
        binding.buttonLogin.setOnClickListener {
            loginUser()
        }

        binding.BackButton.setOnClickListener {
            finish()
        }

//        binding.donthavebutton.setOnClickListener {
//            val intent = Intent(this, RegisterSellerActivity::class.java)
//            startActivity(intent)
//        }
        binding.donthavebutton.visibility = View.GONE
    }

    private fun loginUser() {
        val auth = FirebaseAuth.getInstance()
        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextPassword.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()){
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
                if (it.isSuccessful){
                    val uid = firebaseAuth.currentUser?.uid
                    if (uid != null) {
                        val database = FirebaseDatabase.getInstance().reference
                        val userRef = database.child("seller").child(uid)

                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val user = snapshot.getValue(UserModel2::class.java)
                                    if (user != null) {
                                        Toast.makeText(this@Login2Activity, "Login Successful", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this@Login2Activity, Main2Activity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this@Login2Activity, "Your Email or Password is Invalid", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {  }
                        })
                    }
                }else{
                    Toast.makeText(this,"Your Email or Password is Invalid", Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            Toast.makeText(this,"Please enter your Email and Password", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val auth = FirebaseAuth.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance().reference
            val userRef = database.child("seller").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(UserModel2::class.java)
                        if (user != null) {
                            loggedin(userId)
                        } else {
                            auth.signOut()
                            setContentView(binding.root)
                        }
                    } else {
                        auth.signOut()
                        setContentView(binding.root)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        } else {
            auth.signOut()
            setContentView(binding.root)
        }
    }
    private fun loggedin(userId: String) {
        val database = FirebaseDatabase.getInstance().reference
        val shop = database.child("shop").child(userId)
        shop.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val shop = snapshot.getValue(ShopModel::class.java)
                    if (shop?.name != null) {
                        val intent = Intent(this@Login2Activity, Main2Activity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val intent = Intent(this@Login2Activity, ShopProfileActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    val intent = Intent(this@Login2Activity, ShopProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

    }
//    private fun updateUi(user: FirebaseUser?) {
//        val intent = Intent(this, Main2Activity::class.java)
//        startActivity(intent)
//        finish()
//    }
}