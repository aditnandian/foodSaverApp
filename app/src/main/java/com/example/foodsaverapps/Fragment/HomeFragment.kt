package com.example.foodsaverapps.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodsaverapps.Adapter.ArticleAdapter
import com.example.foodsaverapps.Adapter.MenuAdapter
import com.example.foodsaverapps.Model.AllMenu
import com.example.foodsaverapps.Model.ArticleModel
import com.example.foodsaverapps.R
import com.example.foodsaverapps.databinding.FragmentHomeBinding
import com.example.foodsaverapps.ArticleDetailActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: FirebaseDatabase
    private val originalMenuItems = mutableListOf<AllMenu>()
    private val originalSellerKeys = mutableListOf<String>()
    private val originalMenuIds = mutableListOf<String>()
    private val menuIdToSellerIdMap = mutableMapOf<String, String>()
    private val TAG = "HomeFragment"
    private val articleList = mutableListOf<ArticleModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: called")
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupClickListeners()
        retrieveSellerKeys()
        retrieveArticles() // Retrieve articles
        return binding.root
    }

    private fun setupClickListeners() {
        binding.imageButtonHeavyMeal.setOnClickListener {
            showShopBottomSheet("Heavy Meal")
        }
        binding.imageButtonSnack.setOnClickListener {
            showShopBottomSheet("Snacks")
        }
        binding.imageButtonDrinks.setOnClickListener {
            showShopBottomSheet("Drinks")
        }
    }

    private fun showShopBottomSheet(type: String) {
        val bottomSheetDialog = ShopBottomSheetFragment()
        val args = Bundle()
        args.putString("type", type)
        bottomSheetDialog.arguments = args
        bottomSheetDialog.show(parentFragmentManager, "ShopBottomSheetFragment")
    }

    private fun retrieveSellerKeys() {
        val menuRef = FirebaseDatabase.getInstance().reference.child("shop")

        menuRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (sellerSnapshot in snapshot.children) {
                    val sellerKey = sellerSnapshot.key
                    sellerKey?.let { originalSellerKeys.add(it) }
                }
                for (key in originalSellerKeys) {
                    retrieveMenuItem(key)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun retrieveMenuItem(seller: String) {
        val foodReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("shop/$seller/menu")
        foodReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(AllMenu::class.java)
                    menuItem?.let {
                        val menuId = foodSnapshot.key ?: ""
                        if (it.foodAvailable == true && !originalMenuIds.contains(menuId)) {
                            originalMenuItems.add(it)
                            originalMenuIds.add(menuId)
                            menuIdToSellerIdMap[menuId] = seller
                        }
                    }
                }
                sortAndSetAdapter()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sortAndSetAdapter() {
        val (sortedMenuItems, sortedMenuIds) = originalMenuItems.zip(originalMenuIds)
            .sortedByDescending { it.first.foodOrdered }
            .unzip()

        val limitedMenuItems = sortedMenuItems.take(6)
        val limitedMenuIds = sortedMenuIds.take(6)

        setAdapter(limitedMenuItems, originalSellerKeys, limitedMenuIds, menuIdToSellerIdMap)
    }

    private fun setAdapter(menuItems: List<AllMenu>, sellers: List<String>, menuIds: List<String>, menuIdToSellerIdMap: Map<String, String>) {
        val adapter = MenuAdapter(menuItems, requireContext(), sellers, menuIds, menuIdToSellerIdMap)
        binding.PopulerRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.PopulerRecyclerView.adapter = adapter
    }

    private fun retrieveArticles() {
        val articleRef = FirebaseDatabase.getInstance().reference.child("article")

        articleRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                articleList.clear()
                for (articleSnapshot in snapshot.children) {
                    val article = articleSnapshot.getValue(ArticleModel::class.java)
                    article?.let { articleList.add(it) }
                }
                updateRecyclerView(articleList) // Call the method here
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateRecyclerView(articleList: List<ArticleModel>) {
        binding.imageSlider.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val articleAdapter = ArticleAdapter(requireContext(), articleList, object : ArticleAdapter.OnArticleClickListener {
            override fun onArticleClick(article: ArticleModel) {
                val intent = Intent(context, ArticleDetailActivity::class.java).apply {
                    putExtra("ArticleName", article.title)
                    putExtra("ArticleContent", article.content)
                    putExtra("ArticlePicture", article.pictureUrl)
                }
                startActivity(intent)
            }
        })

        binding.imageSlider.adapter = articleAdapter
    }


    private fun updateImageSlider() {
        val slideAdapter = SlideAdapter(articleList)
        binding.imageSlider.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.imageSlider.adapter = slideAdapter
    }

    inner class SlideAdapter(private val articles: List<ArticleModel>) : RecyclerView.Adapter<SlideAdapter.SlideViewHolder>() {

        inner class SlideViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.imageview)
            val titleTextView: TextView = view.findViewById(R.id.atitle)
            val previewTextView: TextView = view.findViewById(R.id.apreview)

            fun bind(article: ArticleModel) {
                // Load image using your preferred library (e.g., Glide)
                Glide.with(imageView.context).load(article.pictureUrl).into(imageView)
                titleTextView.text = article.title
                previewTextView.text = article.content
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.slide_item, parent, false)
            return SlideViewHolder(view)
        }

        override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
            holder.bind(articles[position])
        }

        override fun getItemCount(): Int = articles.size
    }
}
