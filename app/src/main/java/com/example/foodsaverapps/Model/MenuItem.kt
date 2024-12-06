package com.example.foodsaverapps.Model

data class MenuItem(
    val key: String? = null,
    val foodName: String? = null,
    val foodPrice: Int? = null,
    val foodCategory: String? = null,
    val foodDescription: String? = null,
    val foodImage: String? = null,
    val foodOrdered: Int? = null,
    val foodAvailable: Boolean? = false,
    val foodShelfLife: String? = null
)
