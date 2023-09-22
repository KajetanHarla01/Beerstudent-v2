package com.khmb.beerstudent.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khmb.beerstudent.R
import com.khmb.beerstudent.data.Post
import com.khmb.beerstudent.databinding.HomeScreenItemBinding
import com.khmb.beerstudent.helpers.PostItemClickListener
import com.khmb.beerstudent.helpers.RVItemClickListener
import com.khmb.beerstudent.helpers.myCapitalize
import com.khmb.beerstudent.helpers.toDateString
import com.squareup.picasso.Picasso


class HomeRecyclerViewAdapter(
    private val clickListener: RVItemClickListener
) : ListAdapter<Post, HomeRecyclerViewAdapter.ViewHolder>(Comparator) {

    object Comparator :
        DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.postName == newItem.postName && oldItem.lastCommentTimestamp == newItem.lastCommentTimestamp
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            HomeScreenItemBinding.inflate(
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

    inner class ViewHolder(binding: HomeScreenItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val itemLabel: TextView = binding.forumLabel
        private val itemDate: TextView = binding.forumDate
        private val itemCommentAuthor: TextView = binding.postAuthor
        private val postIMG: ImageView = binding.forumIMG
        private val postText: TextView = binding.postText
        private val minusVote: TextView = binding.minusButton
        private val plusVote: TextView = binding.plusButton
        private val decoration: View = binding.decoration
        private val decoration2: View = binding.decoration2
        private val lastMessageLabel: TextView = binding.lastMessageLabel
        private val rootView = binding.root
        private val postPlusButton = binding.plusButton
        private val postMinusButton = binding.minusButton


        override fun toString(): String {
            return super.toString() + " '" + (itemLabel.text) + "'"
        }
        fun setOnClickListener(listener: RVItemClickListener) {
            rootView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
        fun bind(post: Post) {
            itemLabel.text = post.postName?.myCapitalize()
            itemDate.text = post.lastCommentTimestamp?.toDateString()
            itemCommentAuthor.text = post.lastCommentAuthor
            postText.text = post.postText
            minusVote.text = (post.minusVotes ?: 0).toString()
            plusVote.text = (post.plusVotes ?: 0).toString()
            if (post.imageURL != null && post.imageURL != "") {
                Picasso.get().load(post.imageURL).into(postIMG);
            }
            if (post.lastCommentAuthor == ""){
                lastMessageLabel.visibility = View.GONE
            }
            else
            {
                lastMessageLabel.visibility = View.VISIBLE
            }
            // Sets the background color of the decoration view based on whether the user is the owner of the Room
            decoration.setBackgroundColor(
                decoration.context.getColor(
                    R.color.secondary
                )
            )
            decoration2.setBackgroundColor(
                decoration2.context.getColor(
                    R.color.secondary
                )
            )
        }
    }

}


