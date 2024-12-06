package com.example.foodsaverapps

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.foodsaverapps.LoginActivity
import com.example.foodsaverapps.databinding.ActivityStartBinding


class StartActivity : AppCompatActivity() {
    private val binding: ActivityStartBinding by lazy {
        ActivityStartBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Change status bar color to green
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.green)
        }

        binding.buttonCustomer.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
//            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        binding.buttonSeller.setOnClickListener {
            val intent = Intent(this,Login2Activity::class.java)
            startActivity(intent)
        }
    }
}