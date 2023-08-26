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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.khmb.beerstudent.R
import com.khmb.beerstudent.data.Comment
import com.khmb.beerstudent.databinding.FragmentRoomBinding
import com.khmb.beerstudent.firebase.FirebaseHandler
import com.khmb.beerstudent.helpers.KeyboardHelper
import com.khmb.beerstudent.helpers.MyTextWatcher
import com.khmb.beerstudent.helpers.TimerTaskListener
import com.khmb.beerstudent.helpers.myCapitalize

class CommentFragment : Fragment(), ValueEventListener {

    private var _binding: FragmentRoomBinding? = null
    private val args: RoomFragmentArgs by navArgs()

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
        comments.clear()
        FirebaseHandler.RealtimeDatabase.stopListeningToPostComments(postName, this)
    }
    private lateinit var listAdapter: CommentRecyclerViewAdapter
    private lateinit var postName: String
    private val comments: ArrayList<Comment> = ArrayList()
    private var isFirstGet: Boolean = true
    private val commentTextWatcher: MyTextWatcher = MyTextWatcher(object : TimerTaskListener {
        override fun timerRun() {
            requireActivity().runOnUiThread {
                val message = binding.message.editText?.text.toString()
                val notEmpty = message.isNotEmpty()
                binding.postSendButton.isEnabled = notEmpty
                val tintColor =
                    binding.root.context.getColor(if (notEmpty) R.color.secondary else R.color.grey)
                binding.postSendButton.backgroundTintList = ColorStateList.valueOf(tintColor)
            }
        }
    })
    private fun setupButtons(){
        binding.postSendButton.isEnabled = false
        binding.postSendButton.setOnClickListener {
            commentTextWatcher.cancelTimer()
            val comment = binding.message.editText?.text.toString()
            FirebaseHandler.RealtimeDatabase.addComment(
                postName, Comment(
                    FirebaseHandler.Authentication.getUserEmail(),
                    comment,
                    System.currentTimeMillis()
                )
            )
            binding.message.editText?.text?.clear()
            KeyboardHelper.hideSoftwareKeyboard(requireContext(), binding.root.windowToken)
        }
    }
    private fun setupEditText(){
        binding.message.editText?.addTextChangedListener(commentTextWatcher)
    }
    private fun setupRecyclerView(){
        listAdapter = CommentRecyclerViewAdapter()
        with(binding.messageList){
            layoutManager = LinearLayoutManager(requireContext())
            adapter = listAdapter
        }
    }
    private fun showList(comments: List<Comment>) {
        binding.messageList.visibility = View.VISIBLE
        listAdapter.submitList(comments)
        binding.messageList.scrollToPosition(comments.size - 1)
        if (!isFirstGet) {
            listAdapter.notifyItemInserted(comments.size)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isFirstGet = true
        postName = args.roomName
        (requireActivity() as AppCompatActivity).supportActionBar?.title = postName.myCapitalize()
        FirebaseHandler.RealtimeDatabase.listenToCommentsFromPost(postName, this)
        setupButtons()
        setupEditText()
        setupRecyclerView()
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        if(snapshot.value != null) {
            if (isFirstGet) {
                for (child in snapshot.children) {
                    val comment = child.getValue<Comment>()
                    comment?.let {
                        comments.add(comment)
                    }
                }
            }
            else {
                val lastComment = snapshot.children.lastOrNull()?.getValue<Comment>()
                lastComment?.let {
                    comments.add(lastComment)
                }
            }
            showList(comments)
            isFirstGet = false
        }
    }

    override fun onResume() {
        super.onResume()
        setupEditText()
    }

    override fun onPause() {
        super.onPause()
        binding.message.editText?.removeTextChangedListener(commentTextWatcher)
        commentTextWatcher.cancelTimer()
    }
    override fun onCancelled(error: DatabaseError) {

    }
}
