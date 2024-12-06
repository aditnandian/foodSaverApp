package com.example.foodsaverapps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodsaverapps.Model.UserModel
import com.example.foodsaverapps.databinding.ActivityOtpBinding
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern

class OTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null
    private var fullName: String? = null
    private var phoneNumber: String? = null
    private var email: String? = null
    private var password: String? = null

    private lateinit var smsBroadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        verificationId = intent.getStringExtra("verificationId")
        fullName = intent.getStringExtra("fullName")
        phoneNumber = intent.getStringExtra("phoneNumber")
        email = intent.getStringExtra("email")
        password = intent.getStringExtra("password")

        binding.buttonSubmit.setOnClickListener {
            val enteredOTP = binding.editTextOTP.text.toString().trim()
            if (TextUtils.isEmpty(enteredOTP)) {
                binding.editTextOTP.error = "Please enter OTP"
                return@setOnClickListener
            }

            verifyCode(enteredOTP)
        }

        registerSmsReceiver()
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveUserData()
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserData() {
        val userId = auth.currentUser!!.uid
        val user = UserModel(fullName!!, email!!, phoneNumber!!, password!!)
        val database = Firebase.database.reference

        database.child("user").child(userId).setValue(user).addOnSuccessListener {
            Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to register user", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerSmsReceiver() {
        val client = SmsRetriever.getClient(this)
        val retriever = client.startSmsRetriever()
        retriever.addOnSuccessListener {
            smsBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
                        val extras = intent.extras
                        val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as com.google.android.gms.common.api.Status
                        when (smsRetrieverStatus.statusCode) {
                            com.google.android.gms.common.api.CommonStatusCodes.SUCCESS -> {
                                val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                                val otpPattern = Pattern.compile("(\\d{6})")
                                val matcher = otpPattern.matcher(message)
                                if (matcher.find()) {
                                    val otp = matcher.group(0)
                                    binding.editTextOTP.setText(otp)
                                    verifyCode(otp!!)
                                }
                            }
                            com.google.android.gms.common.api.CommonStatusCodes.TIMEOUT -> {
                                Toast.makeText(this@OTPActivity, "SMS Retriever API Timeout", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            val filter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            registerReceiver(smsBroadcastReceiver, filter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsBroadcastReceiver)
    }
}
