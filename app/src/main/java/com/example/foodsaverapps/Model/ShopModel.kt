package com.example.foodsaverapps.Model

import android.os.Parcel
import android.os.Parcelable

data class ShopModel(
    val name: String? = null,
    val address: String? = null,
    val category: String? = null,
    val pictureUrl: String? = null,
    val coverUrl: String? = null,
    val heavymeals: Boolean = false,
    val drinks: Boolean = false,
    val snacks: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeString(category)
        parcel.writeString(pictureUrl)
        parcel.writeString(coverUrl)
        parcel.writeByte(if (heavymeals) 1 else 0)
        parcel.writeByte(if (drinks) 1 else 0)
        parcel.writeByte(if (snacks) 1 else 0)
    }
    override fun describeContents(): Int {
        return 0
    }
    companion object CREATOR : Parcelable.Creator<ShopModel> {
        override fun createFromParcel(parcel: Parcel): ShopModel {
            return ShopModel(parcel)
        }
        override fun newArray(size: Int): Array<ShopModel?> {
            return arrayOfNulls(size)
        }
    }
}
//val domain: String? = null,
//val city: String? = null,
//val category2: String? = null,