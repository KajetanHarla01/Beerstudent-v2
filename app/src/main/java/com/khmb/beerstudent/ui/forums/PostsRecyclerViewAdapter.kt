package com.khmb.beerstudent.ui.forums

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khmb.beerstudent.R
import com.khmb.beerstudent.data.Post
import com.khmb.beerstudent.databinding.PostsScreenItemBinding
import com.khmb.beerstudent.firebase.FirebaseHandler
import com.khmb.beerstudent.helpers.RVItemClickListener
import com.khmb.beerstudent.helpers.myCapitalize
import com.khmb.beerstudent.helpers.toDateString

class PostsRecyclerViewAdapter(private val clickListener: RVItemClickListener) :
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
    }

    inner class ViewHolder(binding: PostsScreenItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val postLabel: TextView = binding.forumLabel
        private val postOwner: TextView = binding.forumOwner
        private val postDate: TextView = binding.forumDate
        private val commentAuthor: TextView = binding.postAuthor
        private val decoration: View = binding.decoration
        private val rootView = binding.root


        override fun toString(): String {
            return super.toString() + " '" + (postLabel.text) + "'"
        }
        fun setOnClickListener(listener: RVItemClickListener) {
            rootView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
        fun bind(post: Post) {
            postLabel.text = post.postName?.myCapitalize()
            postOwner.text = "by ${post.ownerNickname}"
            postDate.text = post.lastCommentTimestamp?.toDateString()
            commentAuthor.text = post.lastCommentAuthor
            val isOwner = post.ownerNickname == FirebaseHandler.Authentication.getUserEmail()
            decoration.setBackgroundColor(
                decoration.context.getColor(
                    if (isOwner)
                        R.color.secondary
                    else
                        R.color.primary
                )
            )
        }
    }
}


