package com.khmb.beerstudent.ui.forums

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.get
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khmb.beerstudent.R
import com.khmb.beerstudent.data.Post
import com.khmb.beerstudent.databinding.PostsScreenItemBinding
import com.khmb.beerstudent.firebase.FirebaseHandler
import com.khmb.beerstudent.helpers.PostItemClickListener
import com.khmb.beerstudent.helpers.RVItemClickListener
import com.khmb.beerstudent.helpers.myCapitalize
import com.khmb.beerstudent.helpers.toDateString
import com.squareup.picasso.Picasso

class PostsRecyclerViewAdapter(private val clickListener: RVItemClickListener, private val itemClickListener: PostItemClickListener) :
    ListAdapter<Post, PostsRecyclerViewAdapter.ViewHolder>(Comparator) {

    object Comparator : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.postName == newItem.postName && oldItem.lastCommentTimestamp == newItem.lastCommentTimestamp
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            PostsScreenItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.setOnClickListener(clickListener)
        holder.setOnMinusClickListener(itemClickListener)
        holder.setOnPlusClickListener(itemClickListener)
    }

    inner class ViewHolder(binding: PostsScreenItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val postLabel: TextView = binding.forumLabel
        private val postOwner: TextView = binding.forumOwner
        private val postDate: TextView = binding.forumDate
        private val postIMG: ImageView = binding.forumIMG
        private val postText: TextView = binding.postText
        private val lastMessageLabel: TextView = binding.lastMessageLabel
        private val commentAuthor: TextView = binding.postAuthor
        private val minusVote: TextView = binding.minusButton
        private val plusVote: TextView = binding.plusButton
        private val decoration: View = binding.decoration
        private val decoration2: View = binding.decoration2
        private val rootView = binding.root
        private val postPlusButton = binding.plusButton
        private val postMinusButton = binding.minusButton

        override fun toString(): String {
            return super.toString() + " '" + (postLabel.text) + "'"
        }
        fun setOnClickListener(listener: RVItemClickListener) {
            rootView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
        fun setOnPlusClickListener(listener: PostItemClickListener) {
            postPlusButton.setOnClickListener {
                listener.onPlusClick(adapterPosition)
            }
        }
        fun setOnMinusClickListener(listener: PostItemClickListener) {
            postMinusButton.setOnClickListener {
                listener.onMinusClick(adapterPosition)
            }
        }
        fun bind(post: Post) {
            postLabel.text = post.postName?.myCapitalize()
            FirebaseHandler.RealtimeDatabase.getNick(post.ownerId).addOnSuccessListener {
                postOwner.text = "by ${it.value.toString()}"
            }
            postDate.text = post.lastCommentTimestamp?.toDateString()
            postText.text = post.postText
            minusVote.text = (post.minusVotes ?: 0).toString()
            plusVote.text = (post.plusVotes ?: 0).toString()
            Log.d("Post", "bind: ${post.imageURL}")
            if (post.imageURL != null && post.imageURL != "") {
                Picasso.get().load(post.imageURL).into(postIMG);
            }
            FirebaseHandler.RealtimeDatabase.getNick(post.lastCommentAuthor).addOnSuccessListener {
                commentAuthor.text = it.value.toString()
            }
            if (post.lastCommentAuthor == ""){
                lastMessageLabel.visibility = View.GONE
            }
            else
            {
                lastMessageLabel.visibility = View.VISIBLE
            }
            val isOwner = post.ownerId == FirebaseHandler.Authentication.getUserUid()
            decoration.setBackgroundColor(
                decoration.context.getColor(
                    if (isOwner)
                        R.color.secondary
                    else
                        R.color.primary
                )
            )
            decoration2.setBackgroundColor(
                decoration2.context.getColor(
                    if (isOwner)
                        R.color.secondary
                    else
                        R.color.primary
                )
            )
        }
    }
}


