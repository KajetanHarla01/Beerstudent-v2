package com.khmb.beerstudent.ui.home

import android.os.Bundle
import android.util.Log
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
import com.khmb.beerstudent.databinding.FragmentHomeBinding
import com.khmb.beerstudent.firebase.FirebaseHandler
import com.khmb.beerstudent.helpers.RVItemClickListener


class HomeFragment : Fragment(), ChildEventListener {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private lateinit var listAdapter: HomeRecyclerViewAdapter
    private val posts: ArrayList<Post> = ArrayList()

    private val listItemCLickListener: RVItemClickListener = object : RVItemClickListener {
        override fun onItemClick(position: Int) {
            val post = posts[position]
            post.postName?.let {
                val navigateToPostFragmentAction = HomeFragmentDirections.actionNavigationHomeToPostFragment(it)
                findNavController().navigate(navigateToPostFragmentAction)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun setupRecyclerView() {
        listAdapter = HomeRecyclerViewAdapter(listItemCLickListener)
        with(binding.homeList) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = listAdapter
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        posts.clear()
      //  invalidRoomNames.clear()
        FirebaseHandler.RealtimeDatabase.stopListeningToPostsRef(this)
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.postDelayed({
            //isFirstGet = true
            setupRecyclerView()
            FirebaseHandler.RealtimeDatabase.getPosts().addOnSuccessListener {
                for (child in it.children){
                    val postFromDB = child.getValue<Post>()
                    postFromDB?.let{
                        addPost(postFromDB)
                    }
                }
                showList(posts)
                FirebaseHandler.RealtimeDatabase.listenToPostsReference(this@HomeFragment)
            }

        }, 100)
    }
    private fun addPost(post: Post, isFirst: Boolean = false): Int {
        if (post.ownerId == FirebaseHandler.Authentication.getUserUid()) {
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
            posts.add(idx, post)
            return idx
        }
        else{
            return posts.size - 1
        }
    }
    private fun showList(posts: List<Post>, position: Int = -1) {
        binding.homeList.visibility = View.INVISIBLE
        binding.root.postDelayed({
            binding.homeList.visibility = View.VISIBLE
            val animation: LayoutAnimationController =
                AnimationUtils.loadLayoutAnimation(
                    requireContext(),
                    R.anim.layout_animation_fall_down
                )
            binding.homeList.layoutAnimation = animation
            binding.homeList.scheduleLayoutAnimation()
            listAdapter.submitList(posts)
            if (position != -1) {
                listAdapter. notifyDataSetChanged()
            }
            binding.homeList.smoothScrollToPosition(0)
        }, 50)
    }

    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

    }

    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
        if (snapshot.value != null) {
            val changedPost = snapshot.getValue<Post>()
            changedPost?.let {
                val postPos = posts.indexOf(changedPost)
                // invalidRoomNames.removeAt(roomPos)
                posts.removeAt(postPos)
                addPost(changedPost, true)
                val newPostPos = posts.indexOf(changedPost)
                showList(posts, newPostPos)
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
