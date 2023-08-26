package com.khmb.beerstudent.firebase

import com.khmb.beerstudent.data.Post
import com.khmb.beerstudent.data.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.khmb.beerstudent.data.Comment

object FirebaseHandler {

    object RealtimeDatabase {
        // Set constants for paths to the different database objects
        private const val postsPath: String = "posts"
        private const val commentsPath: String = "comments"
        private const val usersPath: String = "users"
        private const val roomLastCommentPath = "lastComment"
        private const val roomLastCommentAuthorPath = "lastCommentAuthor"
        private const val roomLastCommentTimestampPath = "lastCommentTimestamp"
        // Initialize a Firebase database instance using the lazy initialization pattern
        private val firebaseDatabase by lazy {
            Firebase.database("https://fir-forum-32d99-default-rtdb.europe-west1.firebasedatabase.app/")
        }
        // Function to get a reference to the "users" object in the database
        private fun getUsersReference(): DatabaseReference {
            return firebaseDatabase.reference.child(usersPath)
        }
        // Function to add a new user to the database
        fun addUser(user: User) {
            // Get the user's unique ID
            val userUid = Authentication.getUserUid()
            // If the user ID exists, add the user to the database
            userUid?.let {
                // Get a reference to the database node for the current user
                val userReference = getUsersReference().child(userUid)
                // Set the user data at the user node in the database
                userReference.setValue(user)
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
        fun listenToPostsReference(listener: ChildEventListener){
            getCommentsReference().addChildEventListener(listener)
        }
        fun stopListeningToPostsRef(listener: ChildEventListener){
            getCommentsReference().removeEventListener(listener)
        }
        // Function to get a reference to the "messages" object for a specific room in the database
        private fun getCommentsReference(room: String): DatabaseReference {
            return firebaseDatabase.reference.child(commentsPath).child(room)
        }
        // Function to attach a ValueEventListener to a specific room's messages in the "messages" object
        // in the database
        fun listenToCommentsFromPost(post: String, listener: ValueEventListener) {
            getCommentsReference(post).addValueEventListener(listener)
        }
        // This function updates the "last message" information for a given room.
        // It takes the room name and a Post object as parameters.
        private fun updatePostLastComment(postName: String, comment: Comment) {
        // Get a reference to the "rooms" node in the database and
        // access the child node corresponding to the given room.
            getCommentsReference().child(postName).apply {
                // Set the "last message" child nodes to the values of the corresponding
                // properties of the Post object.
                updateChildren(mapOf<String, Any>(
                    roomLastCommentPath to comment.message!!,
                    roomLastCommentAuthorPath to comment.author!!,
                    roomLastCommentTimestampPath to comment.timestamp!!
                ))
            }
        }
        // This function adds a message to a given room in the database.
        // It takes the room name and a Post object as parameters.
        fun addComment(postName: String, comment: Comment) {
            // Get a reference to the "messages" node corresponding to the given room
            // and access the child node with a key equal to the timestamp of the message.
            // Set the value of the child node to the Post object.
            getCommentsReference(postName).child(comment.timestamp!!.toString()).setValue(comment)
            // Update the "last message" information for the room.
            updatePostLastComment(postName, comment)
        }
        // This function removes the given ValueEventListener from the child node of the
        // "messages" node corresponding to the given room name.
        fun stopListeningToRoomMessages(roomName: String, listener: ValueEventListener) {
            getCommentsReference(roomName).removeEventListener(listener)
        }
    }

    object Authentication {
        // Create a FirebaseAuth instance with a Firebase.auth
        private val firebaseAuth by lazy { Firebase.auth }
        // Login function using email and password credentials
        // It returns a Task<AuthResult> for asynchronous handling
        fun login(email: String, password: String): Task<AuthResult> {
            // Login process:
            // To sign in the app we use a signInWithEmailAndPassword method from the FirebaseAuth
            // variable and an OnCompleteListener to handle the result.
            // NOTE: This method of authentication has to be enabled in the project
            return firebaseAuth.signInWithEmailAndPassword(email, password)
        }
        // Register function using email and password credentials
        // It returns a Task<AuthResult> for asynchronous handling
        fun register(email: String, password: String): Task<AuthResult> {
            // register process:
            // To sign up in the app we use a createUserWithEmailAndPassword method from the FirebaseAuth
            // variable and an OnCompleteListener to handle the result.
            // NOTE: This method of authentication has to be enabled in the project
            return firebaseAuth.createUserWithEmailAndPassword(email, password)
        }
        // Get the email address of the current user
        fun getUserEmail():String?{
            return firebaseAuth.currentUser?.email
        }
        // Get the UID of the current user
        fun getUserUid():String?{
            return firebaseAuth.currentUser?.uid
        }
        // Check if there is a current user logged in
        fun isLoggedIn(): Boolean {
            return firebaseAuth.currentUser != null
        }
        // Log out the current user
        fun logout() {
            firebaseAuth.signOut()
        }
    }
}
