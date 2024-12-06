package com.example.foodsaverapps.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodsaverapps.Adapter.ArticleAdapter
import com.example.foodsaverapps.ArticleDetailActivity
import com.example.foodsaverapps.Model.ArticleModel
import com.example.foodsaverapps.databinding.FragmentArticleBinding
import com.google.firebase.database.*

class ArticleFragment : Fragment() {
    private lateinit var binding : FragmentArticleBinding
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        val transaction = activity?.supportFragmentManager?.beginTransaction()
//        transaction?.remove(SearchFragment())
//        transaction?.commit()

        binding = FragmentArticleBinding.inflate(inflater,container,false)

        retrieveArticles()

        return binding.root
    }

    private fun retrieveArticles() {
        val database = FirebaseDatabase.getInstance()
        val artRef = database.reference.child("article")
        artRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val articleList = mutableListOf<ArticleModel>()
                for (data in snapshot.children) {
                    val article = data.getValue(ArticleModel::class.java)
                    if (article != null) {
                        articleList.add(article)
                    }
                }
                updateRecyclerView(articleList)
            }
            override fun onCancelled(error: DatabaseError) {  }
        })
    }

    private fun updateRecyclerView(articleList: List<ArticleModel>) {
        binding.ListArticle.layoutManager = LinearLayoutManager(requireContext())
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
        binding.ListArticle.adapter = articleAdapter
    }

    companion object {
    }
}