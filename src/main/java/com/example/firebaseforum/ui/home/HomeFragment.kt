package com.example.firebaseforum.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebaseforum.R
import com.example.firebaseforum.data.Room
import com.example.firebaseforum.databinding.FragmentHomeBinding
import com.example.firebaseforum.firebase.FirebaseHandler
import com.example.firebaseforum.firebase.FirebaseHandler.RealtimeDatabase.addRoom
import com.example.firebaseforum.helpers.RVItemClickListener
import com.example.firebaseforum.ui.forums.ForumsFragmentDirections
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.getValue


class HomeFragment : Fragment(), ChildEventListener {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    // Get the binding object from the nullable _binding property. It will throw an exception
    // if accessed outside the lifecycle between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var listAdapter: HomeRecyclerViewAdapter
    private val rooms: ArrayList<Room> = ArrayList()
   // private val invalidRoomNames: ArrayList<String> = ArrayList()

    private val listItemCLickListener: RVItemClickListener = object : RVItemClickListener {
        override fun onItemClick(position: Int) {
            // Gets the room associated with the clicked item
            val room = rooms[position]
            room.roomName?.let {
                val navigateToRoomFragmentAction = HomeFragmentDirections.actionNavigationHomeToRoomFragment(it)
                findNavController().navigate(navigateToRoomFragmentAction)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the view using the generated binding class and set the _binding property.
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    // Sets up the recycler view for the forum list
    private fun setupRecyclerView() {
        // Creates the adapter with the item click listener
        listAdapter = HomeRecyclerViewAdapter(listItemCLickListener)
        with(binding.homeList) {
            // Sets the layout manager
            layoutManager = LinearLayoutManager(requireContext())
            // Sets the adapter for the recycler view
            adapter = listAdapter
        }
    }
    // This method is called when the view is destroyed.
    override fun onDestroyView() {
        super.onDestroyView()
        rooms.clear()
      //  invalidRoomNames.clear()
        FirebaseHandler.RealtimeDatabase.stopListeningToRoomsRef(this)
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.postDelayed({
            //isFirstGet = true
            setupRecyclerView()
            FirebaseHandler.RealtimeDatabase.getRooms().addOnSuccessListener {
                for (child in it.children){
                    val roomFromDB = child.getValue<Room>()
                    roomFromDB?.let{
                        addRoom(roomFromDB)
                    }
                }
                showList(rooms)
                FirebaseHandler.RealtimeDatabase.listenToRoomsReference(this@HomeFragment)
            }

        }, 100)
    }
    /*Adds a new room to the list of rooms and returns its index in the list.*/
    private fun addRoom(room: Room, isFirst: Boolean = false): Int {
        if (room.ownerId == FirebaseHandler.Authentication.getUserUid()) {
            // Initialize the index variable.
            var idx = 0
            // If the new room is not the first room in the list, find the appropriate index for it based
            // on its timestamp.
            // The newest items (with higher timestamp) are at the front of the list
            if (!isFirst) {
                for ((i, existingRoom) in rooms.withIndex()) {
                    if (room.lastMessageTimestamp!! >= existingRoom.lastMessageTimestamp!!) {
                        idx = i
                        break
                    } else
                        idx = i + 1
                }
            }
            // Add the new room to the list of rooms at the determined index.
            rooms.add(idx, room)
            // Return the index of the added room.
            return idx
        }
        else{
            return rooms.size - 1
        }
    }
    private fun showList(rooms: List<Room>, position: Int = -1) {
        // Make the forumList invisible
        binding.homeList.visibility = View.INVISIBLE
        // Schedule a delayed task to make the forumList visible
        binding.root.postDelayed({
            // Make the forumList visible
            binding.homeList.visibility = View.VISIBLE
            // Load an animation for the forumList
            val animation: LayoutAnimationController =
                AnimationUtils.loadLayoutAnimation(
                    requireContext(),
                    R.anim.layout_animation_fall_down
                )
            // Set the animation as the layout animation for the forumList
            binding.homeList.layoutAnimation = animation
            // Schedule the layout animation for the forumList
            binding.homeList.scheduleLayoutAnimation()
            // Submit the list of rooms to the listAdapter
            listAdapter.submitList(rooms)
            // If position is not -1, notify the adapter to update the list
            if (position != -1) {
                listAdapter. notifyDataSetChanged()
            }
            // Scroll the forumList to the top
            binding.homeList.smoothScrollToPosition(0)
        }, 50)
    }

    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

    }

    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
        // Check if the child node has a non-null value
        if (snapshot.value != null) {
            // Retrieve the changed room from the snapshot
            val changedRoom = snapshot.getValue<Room>()
            changedRoom?.let {
                // Find the position of the changed room in the list of invalid room names
                val roomPos = rooms.indexOf(changedRoom)
                // Remove the changed room's name from the list of invalid room names
              //  invalidRoomNames.removeAt(roomPos)
                // Remove the changed room from the list of rooms at its previous position
                rooms.removeAt(roomPos)
                // Add the changed room at the top of the list of rooms
                addRoom(changedRoom, true)
                val newRoomPos = rooms.indexOf(changedRoom)
                // Display the updated list of rooms with an animation that falls down into view
                showList(rooms, newRoomPos)
            }
        }
    }

    override fun onChildRemoved(snapshot: DataSnapshot) {

    }

    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

    }

    override fun onCancelled(error: DatabaseError) {

    }
}
