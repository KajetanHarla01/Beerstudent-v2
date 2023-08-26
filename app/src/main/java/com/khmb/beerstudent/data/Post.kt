package com.khmb.beerstudent.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Post(
    val postName: String? = null,
    val ownerId: String? = null,
    val ownerEmail: String? = null,
    val image: String? = null,
    var lastComment: String? = null,
    var lastCommentAuthor: String? = null,
    val lastCommentTimestamp:Long? = null,
    var isPrivate:Boolean? = false,
    var roomPassword: String? = null
): Parcelable

