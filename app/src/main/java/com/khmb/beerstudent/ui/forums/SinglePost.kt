package com.khmb.beerstudent.ui.forums

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.khmb.beerstudent.R
import com.khmb.beerstudent.data.Post
import com.khmb.beerstudent.databinding.FragmentSinglePostBinding
import com.khmb.beerstudent.firebase.FirebaseHandler
import com.squareup.picasso.Picasso

class SinglePost : Fragment() {

    private lateinit var binding: FragmentSinglePostBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSinglePostBinding.inflate(layoutInflater)

        // Pobierz argumenty za pomocą navArgs()
        val receivedBundle = arguments
        if (receivedBundle != null) {
            val postName = receivedBundle.getString("postName")
            val result = FirebaseHandler.RealtimeDatabase.getPost(postName!!)
            result?.addOnSuccessListener { dataSnapshot ->
                val value = dataSnapshot.getValue(Post::class.java)
                //Log.d("postName", value.toString())
                binding.minusButton.setOnClickListener {
                    FirebaseHandler.RealtimeDatabase.votePostMinus(value?.postName!!)
                    val postReference = FirebaseHandler.RealtimeDatabase.getPostReference(postName)
                    postReference.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val updatedPost = dataSnapshot.getValue(Post::class.java)
                            Log.d("postName", updatedPost.toString())
                            if (updatedPost != null) {
                                binding.minusButton.text = (updatedPost.minusVotes ?: 0).toString()
                                binding.plusButton.text = (updatedPost.plusVotes ?: 0).toString()
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    })
                }
                binding.plusButton.setOnClickListener {
                    FirebaseHandler.RealtimeDatabase.votePostPlus(value?.postName!!)
                    val postReference = FirebaseHandler.RealtimeDatabase.getPostReference(postName)
                    postReference.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val updatedPost = dataSnapshot.getValue(Post::class.java)
                            if (updatedPost != null) {
                                binding.minusButton.text = (updatedPost.minusVotes ?: 0).toString()
                                binding.plusButton.text = (updatedPost.plusVotes ?: 0).toString()
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    })
                }
                binding.forumLabel.text = value?.postName
                FirebaseHandler.RealtimeDatabase.getNick(value?.ownerId).addOnSuccessListener {
                    binding.forumOwner.setText("by "  + it.value.toString())
                }
                binding.postDescription.text = value?.postText
                binding.minusButton.text = (value?.minusVotes ?: 0).toString()
                binding.plusButton.text = (value?.plusVotes ?: 0).toString()
                if (value?.ownerId == FirebaseHandler.Authentication.getUserUid()){
                    binding.decoration.setBackgroundColor(binding.decoration.context.getColor(R.color.secondary))
                }
                else{
                    binding.decoration.setBackgroundColor(binding.decoration.context.getColor(R.color.primary))
                }
                if (value?.ownerId == FirebaseHandler.Authentication.getUserUid()){
                    binding.decoration2.setBackgroundColor(binding.decoration2.context.getColor(R.color.secondary))
                }
                else{
                    binding.decoration2.setBackgroundColor(binding.decoration2.context.getColor(R.color.primary))
                }
                if (value?.imageURL != null && value?.imageURL!="") {
                    Picasso.get().load(value?.imageURL).into(binding.postIMG)
                }
            }

        }
        return binding.root
    }
}
