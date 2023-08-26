package com.khmb.beerstudent.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.beerstudent.R
import com.example.beerstudent.data.Room
import com.example.beerstudent.databinding.HomeScreenItemBinding
import com.example.beerstudent.helpers.RVItemClickListener
import com.example.beerstudent.helpers.myCapitalize
import com.example.beerstudent.helpers.toDateString


class HomeRecyclerViewAdapter(
    private val clickListener: RVItemClickListener // Constructor that takes an RVItemClickListener object as a parameter
) : ListAdapter<Room, HomeRecyclerViewAdapter.ViewHolder>(Comparator) { // Extends ListAdapter, with Room as the data type and ViewHolder as the view holder class

    object Comparator :
        DiffUtil.ItemCallback<Room>() {
        // Defines a Comparator object that extends DiffUtil.ItemCallback and specifies the data type (Room)

        override fun areItemsTheSame(oldItem: Room, newItem: Room): Boolean {
            // Checks if the items are the same object (i.e., have the same reference)
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Room, newItem: Room): Boolean {
            // Compares the contents of the items to check if they're the same
            return oldItem.roomName == newItem.roomName && oldItem.lastMessageTimestamp == newItem.lastMessageTimestamp
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        // Overrides the onCreateViewHolder method to inflate the layout and create a ViewHolder
        return ViewHolder(
            HomeScreenItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    // Overrides the onBindViewHolder method to bind the data to the ViewHolder and set the click listener
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = getItem(position) // Gets the Room object at the specified position
        holder.bind(item)
        holder.setOnClickListener(clickListener)
    }

    // Defines a ViewHolder that extends RecyclerView.ViewHolder and takes a binding object as a parameter
    inner class ViewHolder(binding: HomeScreenItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // Initializes the TextViews and Views with their respective views in the layout
        private val itemLabel: TextView = binding.homeItemLabel
        private val itemDate: TextView = binding.date
        private val itemPost: TextView = binding.post
        private val decoration: View = binding.decoration
        private val rootView = binding.root


        override fun toString(): String {
            return super.toString() + " '" + (itemLabel.text) + "'"
        }
        fun setOnClickListener(listener: RVItemClickListener) {
            rootView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
        // Binds the Room object to the views in the ViewHolder layout
        fun bind(room: Room) {
            itemLabel.text = room.roomName?.myCapitalize()
            itemDate.text = room.lastMessageTimestamp?.toDateString()
            itemPost.text = room.lastMessage

            // Sets the background color of the decoration view based on whether the user is the owner of the Room
            decoration.setBackgroundColor(
                decoration.context.getColor(
                    R.color.secondary
                )
            )
        }
    }

}


