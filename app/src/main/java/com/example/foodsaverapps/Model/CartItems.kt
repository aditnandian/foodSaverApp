package com.example.foodsaverapps.Model

data class CartItems(
    var foodName: String ?=null,
    var foodPrice: Int ?=null,
    var foodDescription: String ?=null,
    var foodImage: String ?=null,
    var foodQuantity: Int ?=null,
    var shopKey: String ?=null
)
