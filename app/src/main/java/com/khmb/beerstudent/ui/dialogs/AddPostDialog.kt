package com.khmb.beerstudent.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.khmb.beerstudent.R
import com.khmb.beerstudent.databinding.DialogAddPostBinding

class AddPostDialog : DialogFragment() {

    private lateinit var binding: DialogAddPostBinding
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
            val postText = binding.description.editText?.text.toString()
            val imageUrl = binding.filepath.editText?.text.toString()
            // Check that the room name is not empty
            if (postName?.isNotEmpty() == true) {
                    binding.roomName.error = ""
                    mListener?.onPositiveClick(postName, postText, imageUrl)
                    dismiss()
            }else{
                binding.roomName.error = getString(R.string.missing_input)
            }
        }
        binding.dialogNegativeButton.setOnClickListener {
            dismiss()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogAddPostBinding.inflate(layoutInflater)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }


    interface DialogListener {
        fun onPositiveClick(postName: String, postText: String, imageUrl: String?)
    }
}






