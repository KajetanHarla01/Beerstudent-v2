package com.example.firebaseforum.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.firebaseforum.R
import com.example.firebaseforum.databinding.DialogRoomPasswordBinding

class RoomPasswordDialog : DialogFragment() {

    // Declare variables for view binding, dialog listener, and room position
    private lateinit var binding: DialogRoomPasswordBinding
    private var mListener: DialogListener? = null
    private var mRoomPosition: Int = -1
    // Set the room position to be used in the dialog
    fun setRoomPosition(roomPosition: Int) {
        mRoomPosition = roomPosition
    }
    // Set the dialog listener to be used in the fragment
    fun setDialogListener(listener: DialogListener) {
        mListener = listener
    }
    // Set the click listeners for the positive and negative buttons
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.dialogPositiveButton.setOnClickListener {
            val password = binding.roomPassword.editText?.text?.toString()
            if (password?.isNotEmpty() == true) {
                // Call the positive click listener with the room position and password,
                // then dismiss the dialog
                mListener?.onPositiveClick(mRoomPosition, password)
                dismiss()
            } else {
                // Show an error message if the password is empty
                binding.roomPassword.error = getString(R.string.missing_input)
            }
        }
        binding.dialogNegativeButton.setOnClickListener {
            // Dismiss the dialog when the negative button is clicked
            dismiss()
        }
        super.onViewCreated(view, savedInstanceState)
    }
    // Create a new instance of the RoomPasswordDialog fragment
    fun newInstance(): RoomPasswordDialog {
        return RoomPasswordDialog()
    }


    // Inflate the view for the dialog
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogRoomPasswordBinding.inflate(layoutInflater)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }


    // Interface for the dialog listener
    interface DialogListener {
        fun onPositiveClick(roomPosition: Int, password: String)
    }
}



