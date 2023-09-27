package com.khmb.beerstudent.firebase

import android.util.Log
import com.khmb.beerstudent.data.Post
import com.khmb.beerstudent.data.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.values
import com.google.firebase.ktx.Firebase
import com.khmb.beerstudent.data.Comment

object FirebaseHandler {

    object RealtimeDatabase {
        private const val postsPath: String = "posts"
        private const val commentsPath: String = "comments"
        private const val usersPath: String = "users"
        private const val postLastCommentPath = "lastComment"
        private const val postLastCommentAuthorPath = "lastCommentAuthor"
        private const val postLastCommentTimestampPath = "lastCommentTimestamp"
        private const val userNicknamePath = "nickname"
        private val firebaseDatabase by lazy {
            Firebase.database("https://fir-forum-32d99-default-rtdb.europe-west1.firebasedatabase.app/")
        }
        private fun getUsersReference(): DatabaseReference {
            return firebaseDatabase.reference.child(usersPath)
        }
        fun addUser(user: User) {
            val userUid = Authentication.getUserUid()
            userUid?.let {
                val userReference = getUsersReference().child(userUid)
                userReference.setValue(user)
            }
        }
        fun getUserNickname(): Task<DataSnapshot>? {
            val userUid = Authentication.getUserUid()
            if (userUid != null) {
                val userReference = getUsersReference().child(userUid).child(userNicknamePath)
                return userReference.get()
            } else {
                val completionSource = TaskCompletionSource<DataSnapshot>()
                completionSource.setException(Exception("User not logged in"))
                return completionSource.task
            }
        }
        fun getNick(userUid: String?): Task<DataSnapshot> {
            return if (userUid != null) {
                val userReference = getUsersReference().child(userUid).child(userNicknamePath)
                userReference.get()
            } else {
                val completionSource = TaskCompletionSource<DataSnapshot>()
                completionSource.setException(Exception("User not logged in"))
                completionSource.task
            }
        }
        fun setUserNickname(newNick: String){
            val userUid = Authentication.getUserUid()
            userUid?.let {
                val userReference = getUsersReference().child(userUid).child(userNicknamePath)
                userReference.setValue(newNick)
            }
        }
        private fun getCommentsReference(): DatabaseReference{
            return firebaseDatabase.reference.child(postsPath)
        }
        fun addPost(post: Post){
            getCommentsReference().child(post.postName!!).setValue(post)
        }
        fun addUserPosts(postName: String){
            val userUid = Authentication.getUserUid()
            userUid?.let {
                getUsersReference().child(userUid).child(postName).setValue(true)
            }
        }
        fun getPosts(): Task<DataSnapshot>{
            return getCommentsReference().get()
        }
        fun getPost(name: String): Task<DataSnapshot>{
            val ref = getPostReference(name)
            return ref.get()
        }
        fun listenToPostsReference(listener: ChildEventListener){
            getCommentsReference().addChildEventListener(listener)
        }
        fun stopListeningToPostsRef(listener: ChildEventListener){
            getCommentsReference().removeEventListener(listener)
        }
        private fun getCommentsReference(room: String): DatabaseReference {
            return firebaseDatabase.reference.child(commentsPath).child(room)
        }
        fun listenToCommentsFromPost(post: String, listener: ValueEventListener) {
            getCommentsReference(post).addValueEventListener(listener)
        }
        private fun updatePostLastComment(postName: String, comment: Comment) {
            getCommentsReference().child(postName).apply {
                updateChildren(mapOf<String, Any>(
                    postLastCommentPath to comment.message!!,
                    postLastCommentAuthorPath to comment.author!!,
                    postLastCommentTimestampPath to comment.timestamp!!
                ))
            }
        }
        fun addComment(postName: String, comment: Comment) {
            getCommentsReference(postName).child(comment.timestamp!!.toString()).setValue(comment)
            updatePostLastComment(postName, comment)
        }
        fun stopListeningToPostComments(postName: String, listener: ValueEventListener) {
            getCommentsReference(postName).removeEventListener(listener)
        }
        fun getPostReference(postName: String) : DatabaseReference{
            return firebaseDatabase.reference.child(postsPath).child(postName)
        }
        fun voteCommentMinus(postName: String, comment: String){
            val userUid = Authentication.getUserUid()
            if (userUid != null) {
                val commentReference = getCommentsReference(postName).child(comment)
                commentReference.child("minusVotes").get().addOnSuccessListener { minusVotesSnapshot ->
                    val currentMinusVotes = minusVotesSnapshot.getValue(Int::class.java) ?: 0
                    commentReference.child("plusVotes").get().addOnSuccessListener { plusVotesSnapshot ->
                        val currentPlusVotes = plusVotesSnapshot.getValue(Int::class.java) ?: 0
                        val userVoteReference = commentReference.child("votes").child(userUid)
                        userVoteReference.get().addOnSuccessListener { userVoteSnapshot ->
                            val userVote = userVoteSnapshot.getValue(String::class.java)

                            if (userVote == null) {
                                userVoteReference.setValue("minus")
                                commentReference.child("minusVotes").setValue(currentMinusVotes + 1)
                            } else if (userVote == "plus") {
                                userVoteReference.setValue("minus")
                                commentReference.child("plusVotes").setValue(currentPlusVotes - 1)
                                commentReference.child("minusVotes").setValue(currentMinusVotes + 1)
                            }
                            else{
                                userVoteReference.setValue(null)
                                commentReference.child("minusVotes").setValue(currentMinusVotes - 1)
                            }
                        }
                    }
                }
            }
        }
        fun voteCommentPlus(postName: String, comment: String){
            val userUid = Authentication.getUserUid()
            if (userUid != null) {
                val commentReference = getCommentsReference(postName).child(comment)
                commentReference.child("plusVotes").get().addOnSuccessListener { plusVotesSnapshot ->
                    val currentPlusVotes = plusVotesSnapshot.getValue(Int::class.java) ?: 0
                    commentReference.child("minusVotes").get().addOnSuccessListener { minusVotesSnapshot ->
                        val currentMinusVotes = minusVotesSnapshot.getValue(Int::class.java) ?: 0
                        val userVoteReference = commentReference.child("votes").child(userUid)
                        userVoteReference.get().addOnSuccessListener { userVoteSnapshot ->
                            val userVote = userVoteSnapshot.getValue(String::class.java)

                            if (userVote == null) {
                                userVoteReference.setValue("plus")
                                commentReference.child("plusVotes").setValue(currentPlusVotes + 1)
                            } else if (userVote == "minus") {
                                userVoteReference.setValue("plus")
                                commentReference.child("minusVotes").setValue(currentMinusVotes - 1)
                                commentReference.child("plusVotes").setValue(currentPlusVotes + 1)
                            }
                            else{
                                userVoteReference.setValue(null)
                                commentReference.child("plusVotes").setValue(currentPlusVotes - 1)
                            }
                        }
                    }
                }
            }
        }
        fun votePostMinus(postName: String){
            val userUid = Authentication.getUserUid()
            if (userUid != null) {
                val postReference = getPostReference(postName)

                postReference.child("minusVotes").get().addOnSuccessListener { minusVotesSnapshot ->
                    val currentMinusVotes = minusVotesSnapshot.getValue(Int::class.java) ?: 0
                    postReference.child("plusVotes").get().addOnSuccessListener { plusVotesSnapshot ->
                        val currentPlusVotes = plusVotesSnapshot.getValue(Int::class.java) ?: 0
                        val userVoteReference = postReference.child("votes").child(userUid)
                        userVoteReference.get().addOnSuccessListener { userVoteSnapshot ->
                            val userVote = userVoteSnapshot.getValue(String::class.java)

                            if (userVote == null) {
                                userVoteReference.setValue("minus")
                                postReference.child("minusVotes").setValue(currentMinusVotes + 1)
                            } else if (userVote == "plus") {
                                userVoteReference.setValue("minus")
                                postReference.child("plusVotes").setValue(currentPlusVotes - 1)
                                postReference.child("minusVotes").setValue(currentMinusVotes + 1)
                            }
                            else{
                                userVoteReference.setValue(null)
                                postReference.child("minusVotes").setValue(currentMinusVotes - 1)
                            }
                        }
                    }
                }
            }
        }
        fun votePostPlus(postName: String){
            val userUid = Authentication.getUserUid()

            if (userUid != null) {
                val postReference = getPostReference(postName)

                postReference.child("plusVotes").get().addOnSuccessListener { plusVotesSnapshot ->
                    val currentPlusVotes = plusVotesSnapshot.getValue(Int::class.java) ?: 0
                    postReference.child("minusVotes").get().addOnSuccessListener { minusVotesSnapshot ->
                        val currentMinusVotes = minusVotesSnapshot.getValue(Int::class.java) ?: 0
                        val userVoteReference = postReference.child("votes").child(userUid)
                        userVoteReference.get().addOnSuccessListener { userVoteSnapshot ->
                            val userVote = userVoteSnapshot.getValue(String::class.java)

                            if (userVote == null) {
                                userVoteReference.setValue("plus")
                                postReference.child("plusVotes").setValue(currentPlusVotes + 1)
                            } else if (userVote == "minus") {
                                userVoteReference.setValue("plus")
                                postReference.child("minusVotes").setValue(currentMinusVotes - 1)
                                postReference.child("plusVotes").setValue(currentPlusVotes + 1)
                            }
                            else{
                                userVoteReference.setValue(null)
                                postReference.child("plusVotes").setValue(currentPlusVotes - 1)
                            }
                        }
                    }
                }
            }
        }
    }

    object Authentication {
        private val firebaseAuth by lazy { Firebase.auth }
        fun login(email: String, password: String): Task<AuthResult> {
            return firebaseAuth.signInWithEmailAndPassword(email, password)
        }
        fun register(email: String, password: String): Task<AuthResult> {
            return firebaseAuth.createUserWithEmailAndPassword(email, password)
        }
        fun getUserEmail():String?{
            return firebaseAuth.currentUser?.email
        }
        fun getUserUid():String?{
            return firebaseAuth.currentUser?.uid
        }
        fun isLoggedIn(): Boolean {
            return firebaseAuth.currentUser != null
        }
        fun resetPassword(email: String): Task<Void>? {
            return email.let { firebaseAuth.sendPasswordResetEmail(it) }
        }
        public fun logout() {
            firebaseAuth.signOut()
        }
    }
}
