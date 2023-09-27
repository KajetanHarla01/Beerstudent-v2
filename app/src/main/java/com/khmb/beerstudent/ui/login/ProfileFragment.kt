package com.khmb.beerstudent.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.khmb.beerstudent.R
import com.khmb.beerstudent.data.Post
import com.khmb.beerstudent.databinding.FragmentProfileBinding
import com.khmb.beerstudent.firebase.FirebaseHandler

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginUsername.editText?.setText(FirebaseHandler.Authentication.getUserEmail())
        val task = FirebaseHandler.RealtimeDatabase.getUserNickname()
        task?.addOnSuccessListener { dataSnapshot ->
            val value = dataSnapshot.getValue(String::class.java)
           binding.nick.editText?.setText(value)
        }
        binding.changeNick.setOnClickListener {
            if (binding.nick.editText?.text.toString().length < 4){
                Toast.makeText(context, "Error: Invalid nick", Toast.LENGTH_SHORT).show()
            }
            else{
                FirebaseHandler.RealtimeDatabase.setUserNickname(binding.nick.editText?.text.toString())
                Toast.makeText(context, "Nick changed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}