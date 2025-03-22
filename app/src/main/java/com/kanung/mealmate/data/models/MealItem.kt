package com.kanung.mealmate.data.models

import android.os.Parcel
import android.os.Parcelable

data class MealItem(
    val id: String = "",
    val recipeId: String = "",
    val recipeName: String = "",
    val mealType: String = "", // Breakfast, Lunch, Dinner, Snack
    val day: String = "", // Monday, Tuesday, etc.
    val servings: Int = 1,
    val notes: String = "",
    val position: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(recipeId)
        parcel.writeString(recipeName)
        parcel.writeString(mealType)
        parcel.writeString(day)
        parcel.writeInt(servings)
        parcel.writeString(notes)
        parcel.writeInt(position)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MealItem> {
        override fun createFromParcel(parcel: Parcel): MealItem {
            return MealItem(parcel)
        }

        override fun newArray(size: Int): Array<MealItem?> {
            return arrayOfNulls(size)
        }
    }
}