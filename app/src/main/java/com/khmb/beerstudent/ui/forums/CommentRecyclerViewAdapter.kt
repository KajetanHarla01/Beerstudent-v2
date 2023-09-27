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
import com.khmb.beerstudent.helpers.PostItemClickListener
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
        //holder.setOnMinusClickListener(itemClickListener)
        //holder.setOnPlusClickListener(itemClickListener)
    }

    inner class ViewHolder(binding: PostCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val commentText: TextView = binding.post
        private val commentIMG: ImageView = binding.image
        private val commentAuthor: TextView = binding.forumPostAuthor
        private val commentDate: TextView = binding.date
        private val decoration: View = binding.decoration1
        //private val postPlusButton = binding.plusButtonComment
        //private val postMinusButton = binding.minusButtonComment
        //
        //private val minusVote: TextView = binding.minusButtonComment
        //private val plusVote: TextView = binding.plusButtonComment

        override fun toString(): String {
            return super.toString() + " '" + (commentText.text) + "'"
        }
        /*
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
         */
        fun bind(comment: Comment) {
            commentText.text = comment.message
            FirebaseHandler.RealtimeDatabase.getNick(comment?.author).addOnSuccessListener {
                commentAuthor.text = it.value.toString()
            }
            commentDate.text = comment.timestamp?.toDateString()
            //minusVote.text = (comment.minusVotes ?: 0).toString()
            //plusVote.text = (comment.plusVotes ?: 0).toString()
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
