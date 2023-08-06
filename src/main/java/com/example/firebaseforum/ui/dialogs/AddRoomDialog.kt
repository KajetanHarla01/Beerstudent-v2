package com.example.firebaseforum.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.firebaseforum.R
import com.example.firebaseforum.databinding.DialogAddRoomBinding


// Define a class called AddRoomDialog that extends DialogFragment
class AddRoomDialog : DialogFragment() {

    // Declare some private properties
    private lateinit var binding: DialogAddRoomBinding // Lazily initialize the binding variable later
    private var mListener: DialogListener? = null // Define a listener object that will receive events from the dialog
    private var invalidNames = ArrayList<String>() // Keep track of invalid names for the room
    // Define a method for setting the dialog listener
    fun setDialogListener(listener: DialogListener) {
        mListener = listener
    }
    // Define a method for setting invalid names for the room
    fun setInvalidNames(list: ArrayList<String>){
        invalidNames = list
    }

    // Define a factory method for creating instances of the dialog
    fun newInstance(): AddRoomDialog {
        return AddRoomDialog()
    }

    // Override the onViewCreated method to set up event listeners for the dialog buttons
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set up a listener for the positive button in the dialog
        binding.dialogPositiveButton.setOnClickListener {
            // Get the text entered into the room name and password fields
            val roomName = binding.roomName.editText?.text?.toString()?.trim()?.lowercase()
            val password = binding.roomPassword.editText?.text?.toString()
            // Check that the room name is not empty
            if (roomName?.isNotEmpty() == true) {
                // Check if the room name is already taken
                if(invalidNames.isNotEmpty() && invalidNames.contains(roomName)){
                    binding.roomName.error = getString(R.string.name_taken)
                }else {
                    // Check if the room is private and if so, if a password was entered
                    if(!binding.privateSwitch.isChecked ||
                        (binding.privateSwitch.isChecked && password?.isNotEmpty() == true)) {
                        // Call the onPositiveClick method of the dialog listener with the room name
                        // and password (if any)
                        binding.roomName.error = ""
                        mListener?.onPositiveClick(roomName,password!!)
                        dismiss() // Dismiss the dialog
                    }else{
                        // Display an error message if a password is required but not entered
                        binding.roomPassword.error = getString(R.string.missing_input)
                    }
                }
            }else{
                // Display an error message if the room name is missing
                binding.roomName.error = getString(R.string.missing_input)
            }
        }
        // Set up a listener for the negative button in the dialog
        binding.dialogNegativeButton.setOnClickListener {
            dismiss() // Dismiss the dialog
        }
        // Set up a listener for the private switch in the dialog
        binding.privateSwitch.setOnCheckedChangeListener { _, state ->
            binding.roomPassword.visibility = if(state) View.VISIBLE else View.GONE // Toggle the visibility of the password field based on the state of the switch
        }
        super.onViewCreated(view, savedInstanceState)
    }

    // Override the onCreateView method to inflate the dialog layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogAddRoomBinding.inflate(layoutInflater) // Inflate the dialog layout using the DialogAddRoomBinding class
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Set the background of the dialog to be transparent
        return binding.root // Return the root view of the inflated layout
    }


    // Define an interface for the dialog listener
    interface DialogListener {
        fun onPositiveClick(roomName: String, password: String)
    }
}






