package com.khmb.beerstudent.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.khmb.beerstudent.R
import com.khmb.beerstudent.databinding.DialogAddRoomBinding

class AddPostDialog : DialogFragment() {

    private lateinit var binding: DialogAddRoomBinding
    private var mListener: DialogListener? = null
    fun setDialogListener(listener: DialogListener) {
        mListener = listener
    }
    fun newInstance(): AddPostDialog {
        return AddPostDialog()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set up a listener for the positive button in the dialog
        binding.dialogPositiveButton.setOnClickListener {
            // Get the text entered into the room name and password fields
            val postName = binding.roomName.editText?.text?.toString()?.trim()?.lowercase()
            // Check that the room name is not empty
            if (postName?.isNotEmpty() == true) {
                    binding.roomName.error = ""
                    mListener?.onPositiveClick(postName)
                    dismiss()
            }else{
                binding.roomName.error = getString(R.string.missing_input)
            }
        }
        binding.dialogNegativeButton.setOnClickListener {
            dismiss()
        }
        binding.privateSwitch.setOnCheckedChangeListener { _, state ->
            binding.roomPassword.visibility = if(state) View.VISIBLE else View.GONE
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogAddRoomBinding.inflate(layoutInflater)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }


    interface DialogListener {
        fun onPositiveClick(postName: String)
    }
}






