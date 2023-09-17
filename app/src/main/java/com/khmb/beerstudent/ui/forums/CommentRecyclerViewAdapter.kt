package com.khmb.beerstudent.ui.forums

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khmb.beerstudent.R
import com.khmb.beerstudent.data.Comment
import com.khmb.beerstudent.databinding.PostCommentBinding
import com.khmb.beerstudent.firebase.FirebaseHandler
import com.khmb.beerstudent.helpers.toDateString
import com.squareup.picasso.Picasso


class CommentRecyclerViewAdapter
    : ListAdapter<Comment, CommentRecyclerViewAdapter.ViewHolder>(Comparator) {
    object Comparator : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            PostCommentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(binding: PostCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val commentText: TextView = binding.post
        private val commentIMG: ImageView = binding.image
        private val commentAuthor: TextView = binding.forumPostAuthor
        private val commentDate: TextView = binding.date
        private val decoration: View = binding.decoration1

        override fun toString(): String {
            return super.toString() + " '" + (commentText.text) + "'"
        }
        fun bind(comment: Comment) {
            commentText.text = comment.message
            commentAuthor.text = comment.author
            commentDate.text = comment.timestamp?.toDateString()
            Log.d("Post", "bind: ${comment.imageURL}")
            if (comment.imageURL != null &&  comment.imageURL != "") {
                Picasso.get().load(comment.imageURL).into(commentIMG);
            }
            val isOwner = comment.author == FirebaseHandler.Authentication.getUserEmail()
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
