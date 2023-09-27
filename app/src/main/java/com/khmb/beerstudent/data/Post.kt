package com.khmb.beerstudent.data

import android.os.Parcelable
import com.google.firebase.database.PropertyName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Post(
    val postName: String? = null,
    val ownerId: String? = null,
    val postText: String? = null,
    val imageURL: String? = null,
    var lastComment: String? = null,
    var lastCommentAuthor: String? = null,
    val lastCommentTimestamp:Long? = null,
    val minusVotes:Int? = null,
    val plusVotes:Int? = null
): Parcelable

