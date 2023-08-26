package com.khmb.beerstudent.ui.forums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.getValue
import com.khmb.beerstudent.R
import com.khmb.beerstudent.data.Post
import com.khmb.beerstudent.databinding.FragmentPostsBinding
import com.khmb.beerstudent.firebase.FirebaseHandler
import com.khmb.beerstudent.helpers.RVItemClickListener
import com.khmb.beerstudent.ui.dialogs.AddPostDialog


class PostsFragment : Fragment(), ChildEventListener {
    // View binding for the fragment

    private var _binding: FragmentPostsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var listAdapter: PostsRecyclerViewAdapter
    private val posts: ArrayList<Post> = ArrayList()
    private val listItemCLickListener: RVItemClickListener = object : RVItemClickListener{
        override fun onItemClick(position: Int) {
            // Gets the room associated with the clicked item
            val post = posts[position]
            post.postName?.let {
                val navigateToPostFragmentAction =
                    PostsFragmentDirections.actionNavigationPostsToPostFragment(it)
                findNavController().navigate(navigateToPostFragmentAction)
            }
        }
    }

    private val invalidPostNames: ArrayList<String> = ArrayList()
    private val addDialogListener = object : AddPostDialog.DialogListener{
        override fun onPositiveClick(postName: String) {
            val newPost = Post(
                postName,
                FirebaseHandler.Authentication.getUserEmail()
                    ?.let { it1 -> FirebaseHandler.RealtimeDatabase.getUserNickname(it1) },
                FirebaseHandler.Authentication.getUserEmail(),
                "",
                "",
                System.currentTimeMillis().toString()
            )
            FirebaseHandler.RealtimeDatabase.addPost(newPost)
            FirebaseHandler.RealtimeDatabase.addUserPosts(postName)
        }
    }
    private fun showAddDialog() {
        val newInstance = AddPostDialog().newInstance()
        newInstance.apply {
            setDialogListener(addDialogListener)
        }
        newInstance.show(requireActivity().supportFragmentManager, "AddRoomDialog")
    }
    private fun setupButtons() {
        binding.addForumFab.setOnClickListener {
            showAddDialog()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.postDelayed({
            //isFirstGet = true
            setupButtons()
            setupRecyclerView()
            FirebaseHandler.RealtimeDatabase.getPosts().addOnSuccessListener {
                for (child in it.children){
                    val roomFromDB = child.getValue<Post>()
                    roomFromDB?.let{
                        addPost(roomFromDB)
                    }
                }
                showList(posts)
                FirebaseHandler.RealtimeDatabase.listenToPostsReference(this@PostsFragment)
            }

        }, 100)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        posts.clear()
        invalidPostNames.clear()
        FirebaseHandler.RealtimeDatabase.stopListeningToPostsRef(this)
        _binding = null
    }
    private fun setupRecyclerView() {
        listAdapter = PostsRecyclerViewAdapter(listItemCLickListener)
        with(binding.forumList) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = listAdapter
        }
    }
    private fun addPost(post: Post, isFirst: Boolean = false): Int {
        var idx = 0
        if (!isFirst) {
            for ((i, existingRoom) in posts.withIndex()) {
                if (post.lastCommentTimestamp!! >= existingRoom.lastCommentTimestamp!!) {
                    idx = i
                    break
                } else
                    idx = i + 1
            }
        }
        invalidPostNames.add(idx, post.postName!!)
        posts.add(idx, post)
        return idx
    }
    private fun showList(posts: List<Post>, position: Int = -1) {
        binding.forumList.visibility = View.INVISIBLE
        binding.root.postDelayed({
            binding.forumList.visibility = View.VISIBLE
            val animation: LayoutAnimationController =
                AnimationUtils.loadLayoutAnimation(
                    requireContext(),
                    R.anim.layout_animation_fall_down
                )
            binding.forumList.layoutAnimation = animation
            binding.forumList.scheduleLayoutAnimation()
            listAdapter.submitList(posts)
            if (position != -1) {
                listAdapter. notifyDataSetChanged()
            }
            binding.forumList.smoothScrollToPosition(0)
        }, 50)
    }

    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
        if (snapshot.value != null && !invalidPostNames.contains(snapshot.key)) {
            val lastPost = snapshot.getValue<Post>()
            lastPost?.let {
                val roomPos = addPost(lastPost,true)
                showList(posts, roomPos)
            }
        }
    }

    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
        if (snapshot.value != null) {
            val changedPost = snapshot.getValue<Post>()
            changedPost?.let {
                val postPos = invalidPostNames.indexOf(changedPost.postName)
                invalidPostNames.removeAt(postPos)
                posts.removeAt(postPos)
                addPost(changedPost, true)
                val newRoomPos = posts.indexOf(changedPost)
                showList(posts, newRoomPos)
            }
        }
    }

    override fun onChildRemoved(snapshot: DataSnapshot) {
        TODO("Not yet implemented")
    }

    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
        TODO("Not yet implemented")
    }

    override fun onCancelled(error: DatabaseError) {
        TODO("Not yet implemented")
    }
}

