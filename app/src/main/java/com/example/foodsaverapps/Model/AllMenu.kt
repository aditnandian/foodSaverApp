package com.example.foodsaverapps.Model

data class AllMenu(

    val key: String? = null,
    val foodName: String? = null,
    val foodPrice: Int? = null,
    val foodCategory: String? = null,
    val foodDescription: String? = null,
    val foodImage: String? = null,
    val foodShelfLife: String? = null,
    val foodOrdered: Int? = null,
    val foodAvailable: Boolean? = false,

) : Comparable<AllMenu> {

    override fun compareTo(other: AllMenu): Int {
        // Compare by foodOrdered (descending order)
        val thisOrdered = this.foodOrdered ?: Int.MIN_VALUE // Default to lowest possible value if null
        val otherOrdered = other.foodOrdered ?: Int.MIN_VALUE
        return otherOrdered.compareTo(thisOrdered)
    }
//    override fun compareTo(other: AllMenu): Int {
//        // Compare by foodOrdered (ascending order)
//        val thisOrdered = this.foodOrdered ?: Int.MAX_VALUE // Default to highest possible value if null
//        val otherOrdered = other.foodOrdered ?: Int.MAX_VALUE
//        return thisOrdered.compareTo(otherOrdered)
//    }
}