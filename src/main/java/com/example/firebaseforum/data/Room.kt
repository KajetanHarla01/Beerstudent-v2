package com.example.firebaseforum.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Room(
    val roomName: String? = null, // Name of the chat room
    val ownerId: String? = null, // ID of the room owner
    val ownerEmail: String? = null, // Email address of the room owner
    var lastMessage: String? = null, // Last message sent in the chat room
    var lastMessageAuthor: String? = null, // Author of the last message sent in the chat room
    val lastMessageTimestamp:Long? = null, // Timestamp of the last message sent in the chat room
    var isPrivate:Boolean? = false, // Boolean value indicating whether the chat room is
    // private or not
    var roomPassword: String? = null // Password for the private chat room
): Parcelable

