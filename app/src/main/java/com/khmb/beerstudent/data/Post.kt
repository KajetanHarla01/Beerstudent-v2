package com.khmb.beerstudent.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Post(
    val postName: String? = null,
    val ownerId: String? = null,
    val ownerNickname: String? = null,
    val postText: String? = null,
    val image: String? = null,
    var lastComment: String? = null,
    var lastCommentAuthor: String? = null,
    val lastCommentTimestamp:Long? = null,
): Parcelable

