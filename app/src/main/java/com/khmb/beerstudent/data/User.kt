package com.khmb.beerstudent.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(val email:String? = null, var nickname:String? = null) : Parcelable
