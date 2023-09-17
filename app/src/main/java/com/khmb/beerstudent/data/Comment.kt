package com.khmb.beerstudent.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Comment(val author:String? = null, val message:String? = null, val timestamp: Long? = null, val imageURL:String? = null): Parcelable

