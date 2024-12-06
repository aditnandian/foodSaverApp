package com.example.foodsaverapps.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodsaverapps.ArticleDetailActivity
import com.example.foodsaverapps.Model.ArticleModel
import com.example.foodsaverapps.R
import com.example.foodsaverapps.databinding.ArticleItemBinding

class ArticleAdapter(
    private val context: Context,
    private val articleItems: List<ArticleModel>,
    private val listener: OnArticleClickListener
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    interface OnArticleClickListener {
        fun onArticleClick(article: ArticleModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ArticleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = articleItems.size

    inner class ArticleViewHolder(private val binding: ArticleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onArticleClick(articleItems[position])
                }
            }
        }

        fun bind(position: Int) {
            val articleItem = articleItems[position]

            binding.apply {
                atitle.text = articleItem.title
                apreview.text = articleItem.content
                val uri = Uri.parse(articleItem.pictureUrl)
                Glide.with(context).load(uri).into(imageview)
            }
        }
    }
}
