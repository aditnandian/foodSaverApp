package com.example.foodsaverapps

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.foodsaverapps.databinding.FragmentArticleDetailBinding

class ArticleDetailActivity : AppCompatActivity() {
    private lateinit var binding: FragmentArticleDetailBinding

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageButtonBackArticle.setOnClickListener {
            finish()
        }

        val title = intent.getStringExtra("ArticleName") ?: getString(R.string.undefined)
        val imageUrl = intent.getStringExtra("ArticlePicture") ?: ""
        val body = intent.getStringExtra("ArticleContent") ?: getString(R.string.articledetail)

        binding.textViewHeader.text = title
        binding.textViewFooter.text = body
        val imageUri = Uri.parse(imageUrl)
        Glide.with(this)
            .load(imageUri)
            .transform(CenterCrop(), RoundedCorners(20))
            .into(binding.imageViewA)
    }
}