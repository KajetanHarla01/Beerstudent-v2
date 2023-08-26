package com.khmb.beerstudent.ui.forums

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.beerstudent.R
import com.example.beerstudent.data.Post
import com.example.beerstudent.databinding.FragmentRoomBinding
import com.example.beerstudent.firebase.FirebaseHandler
import com.example.beerstudent.helpers.KeyboardHelper
import com.example.beerstudent.helpers.MyTextWatcher
import com.example.beerstudent.helpers.TimerTaskListener
import com.example.beerstudent.helpers.myCapitalize
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class RoomFragment : Fragment(), ValueEventListener {

    private var _binding: FragmentRoomBinding? = null
    private val args: RoomFragmentArgs by navArgs()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        posts.clear()
        FirebaseHandler.RealtimeDatabase.stopListeningToRoomMessages(roomName, this)
    }
    private lateinit var listAdapter: RoomRecyclerViewAdapter // declare adapter variable
    private lateinit var roomName: String // declare room name variable
    private val posts: ArrayList<Post> = ArrayList() // create an array list to store posts
    private var isFirstGet: Boolean = true // initialize flag to true to indicate that it's the first time to get the posts
    // An instance of MyTextWatcher is created which updates the state of the UI based on user input.
    private val messageTextWatcher: MyTextWatcher = MyTextWatcher(object : TimerTaskListener {
        override fun timerRun() {
            // Run the following code on UI thread in order to be able to touch views
            requireActivity().runOnUiThread {
                // Get the message text and check if it's empty
                val message = binding.message.editText?.text.toString()
                val notEmpty = message.isNotEmpty()
                // Enable or disable the post send button based on whether the message is empty
                binding.postSendButton.isEnabled = notEmpty
                // Set the tint color of the post send button based on whether the message is empty
                val tintColor =
                    binding.root.context.getColor(if (notEmpty) R.color.secondary else R.color.grey)
                binding.postSendButton.backgroundTintList = ColorStateList.valueOf(tintColor)
            }
        }
    })
    private fun setupButtons(){
        binding.postSendButton.isEnabled = false
        binding.postSendButton.setOnClickListener {
            messageTextWatcher.cancelTimer()
            val message = binding.message.editText?.text.toString()
            FirebaseHandler.RealtimeDatabase.addMessage(
                roomName, Post(
                    FirebaseHandler.Authentication.getUserEmail(),
                    message,
                    System.currentTimeMillis()
                )
            )
            binding.message.editText?.text?.clear()
            KeyboardHelper.hideSoftwareKeyboard(requireContext(), binding.root.windowToken)
        }
    }
    private fun setupEditText(){
        binding.message.editText?.addTextChangedListener(messageTextWatcher)
    }
    private fun setupRecyclerView(){
        listAdapter = RoomRecyclerViewAdapter()
        with(binding.messageList){
            layoutManager = LinearLayoutManager(requireContext())
            adapter = listAdapter
        }
    }
    private fun showList(posts: List<Post>) {
        binding.messageList.visibility = View.VISIBLE // make the recycler view visible
        listAdapter.submitList(posts) // submit the list of posts to the adapter
        binding.messageList.scrollToPosition(posts.size - 1) // scroll to the last position
        if (!isFirstGet) {
            // notify the adapter that an item has been inserted if it's not the first time
            listAdapter.notifyItemInserted(posts.size)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isFirstGet = true
        roomName = args.roomName
        (requireActivity() as AppCompatActivity).supportActionBar?.title = roomName.myCapitalize()
        FirebaseHandler.RealtimeDatabase.listenToMessageFromRoom(roomName, this)
        setupButtons()
        setupEditText()
        setupRecyclerView()
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        // If the snapshot is not empty
        if(snapshot.value != null) {
            // If it's the first time getting data, add all the posts in the snapshot
            if (isFirstGet) {
                for (child in snapshot.children) {
                    val post = child.getValue<Post>()
                    post?.let {
                        posts.add(post)
                    }
                }
            }
            // If it's not the first time getting data, add only the last post
            else {
                val lastPost = snapshot.children.lastOrNull()?.getValue<Post>()
                lastPost?.let {
                    posts.add(lastPost)
                }
            }
            // Show the posts in the RecyclerView and update isFirstGet to false
            showList(posts)
            isFirstGet = false
        }
    }

    override fun onResume() {
        super.onResume()
        setupEditText()
    }

    override fun onPause() {
        super.onPause()
        binding.message.editText?.removeTextChangedListener(messageTextWatcher)
        messageTextWatcher.cancelTimer()
    }
    override fun onCancelled(error: DatabaseError) {

    }
}

