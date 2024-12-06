package com.example.foodsaverapps.Model

import android.os.Parcel
import android.os.Parcelable

class OrderDetails(
    var userUid: String? = null,
    var userName: String? = null,
    var foodNames: MutableList<String>? = null,
    var foodImages: MutableList<String>? = null,
    var foodPrices: MutableList<Int>? = null,
    var foodQuantities: MutableList<Int>? = null,
    var totalPrice: Int? = null,
    var phoneNumber: String? = null,
    var orderConfirmed: Boolean = false,
    var shopKey: String? = null,
    var itemPushKey: String? = null,
    var currentTime: String = "", // Changed to String
    var voucherName: String? = null,
    var status: String? = null,
    var shopName: String? = null,
    var appliedDiscount: Int? = null,
    var orderKey: Int? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList()?.toMutableList(),
        parcel.createStringArrayList()?.toMutableList(),
        parcel.createIntArray()?.toMutableList(),
        parcel.createIntArray()?.toMutableList(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString() ?: "", // Updated to read String
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userUid)
        parcel.writeString(userName)
        parcel.writeStringList(foodNames)
        parcel.writeStringList(foodImages)
        parcel.writeIntArray(foodPrices?.toIntArray())
        parcel.writeIntArray(foodQuantities?.toIntArray())
        parcel.writeValue(totalPrice)
        parcel.writeString(phoneNumber)
        parcel.writeByte(if (orderConfirmed) 1 else 0)
        parcel.writeString(shopKey)
        parcel.writeString(itemPushKey)
        parcel.writeString(currentTime) // Updated to write String
        parcel.writeString(voucherName)
        parcel.writeString(status)
        parcel.writeString(shopName)
        parcel.writeValue(appliedDiscount)
        parcel.writeValue(orderKey)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OrderDetails> {
        override fun createFromParcel(parcel: Parcel): OrderDetails {
            return OrderDetails(parcel)
        }

        override fun newArray(size: Int): Array<OrderDetails?> {
            return arrayOfNulls(size)
        }
    }
}
