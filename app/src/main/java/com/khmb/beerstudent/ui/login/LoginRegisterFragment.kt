package com.khmb.beerstudent.ui.login

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.khmb.beerstudent.R
import com.khmb.beerstudent.data.User
import com.khmb.beerstudent.databinding.FragmentLoginBinding
import com.khmb.beerstudent.firebase.FirebaseHandler
import com.khmb.beerstudent.helpers.KeyboardHelper
import com.khmb.beerstudent.helpers.MyTextWatcher
import com.khmb.beerstudent.helpers.TimerTaskListener


class LoginRegisterFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var passwordConfirmValid: Boolean = false
    private var passwordValid: Boolean = false
    private var nicknameValid: Boolean = false
    private var usernameValid: Boolean = false
    private var isLogin = true


    override fun onStart() {
        super.onStart()
        if (FirebaseHandler.Authentication.isLoggedIn())
            findNavController().navigate(R.id.action_navigation_login_to_navigation_home)
    }

    override fun onPause() {
        super.onPause()
        cancelTimers()
    }

    override fun onResume() {
        super.onResume()
        setupEditTexts()
        setupEditTexts()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isLogin = true
        binding.loginPasswordConfirm.visibility = View.GONE
        binding.nick.visibility = View.GONE
        binding.loginRegisterButton.isEnabled = false
        setupButtons()
        setupEditTexts()
        binding.resetPassword.setOnClickListener {
            val email = binding.loginUsername.editText?.text.toString()

            if (isUserNameValid(email)) {
                FirebaseHandler.Authentication.resetPassword(email)
                    ?.addOnSuccessListener {
                        Toast.makeText(context, "Password reset successful. Check your mailbox.", Toast.LENGTH_SHORT).show()
                    }
                    ?.addOnFailureListener { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Error: Invalid e-mail", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupEditTexts() {
        binding.loginUsername.editText?.addTextChangedListener(usernameTextWatcher)
        binding.loginPassword.editText?.addTextChangedListener(passwordTextWatcher)
        binding.loginPasswordConfirm.editText?.addTextChangedListener(passwordConfirmTextWatcher)
        binding.nick.editText?.addTextChangedListener(nicknameTextWatcher)
    }

    private fun validate(): Boolean {
        return if (isLogin)
            passwordValid && usernameValid
        else
            passwordValid && usernameValid && passwordConfirmValid && nicknameValid
    }
    private fun checkUserName(username: String) {
        Log.i("checkUserName", username)
        if (!isUserNameValid(username)) {
            binding.loginUsername.error = getString(R.string.invalid_username)
            usernameValid = false
        } else {
            binding.loginUsername.error = ""
            usernameValid = true
        }
    }
    private fun checkNickName(nickname: String) {
        Log.i("checkNickName", nickname)
        if (!isNicknameValid(nickname)) {
            binding.nick.error = getString(R.string.invalid_nickname)
            nicknameValid = false
        } else {
            binding.nick.error = ""
            nicknameValid = true
        }
    }
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            false
        }
    }
    private fun checkPassword(password: String) {
        Log.i("checkPassword", password)
        if (!isPasswordValid(password)) {
            binding.loginPassword.error = getString(R.string.invalid_password)
            // Set the passwordValid flag to false
            passwordValid = false
        } else {
            binding.loginPassword.error = ""
            passwordValid = true
        }
    }
    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }
    private fun isNicknameValid(nickname: String): Boolean{
        return nickname.length >= 4
    }
    private fun checkRegisterPassword(passwordConfirm: String, password: String) {
        Log.i("checkRegisterPassword", "$passwordConfirm $password")
        if (!isPasswordConfirmValid(passwordConfirm, password)) {
            binding.loginPasswordConfirm.error = getString(R.string.password_mismatch)
            passwordConfirmValid = false
        } else {
            true
            binding.loginPasswordConfirm.error = ""
            passwordConfirmValid = true
        }
    }
    private fun isPasswordConfirmValid(passwordConfirm: String, password: String): Boolean {
        return passwordConfirm == password
    }
    private val passwordTextWatcher: MyTextWatcher = MyTextWatcher(object : TimerTaskListener {
        override fun timerRun() {
            binding.loginPassword.post {
                val passwordInput = binding.loginPassword.editText?.text.toString()
                if (passwordInput.isNotEmpty()) {
                    checkPassword(passwordInput)
                    binding.loginRegisterButton.isEnabled = validate()
                } else{
                    passwordValid = false
                    binding.loginRegisterButton.isEnabled = false
                }
            }
        }
    })
    private val passwordConfirmTextWatcher: MyTextWatcher =
        MyTextWatcher(object : TimerTaskListener {
            override fun timerRun() {
                binding.loginPasswordConfirm.post {
                    val passwordConfirm = binding.loginPasswordConfirm.editText?.text.toString()
                    val password = binding.loginPassword.editText?.text.toString()
                    if (passwordConfirm.isNotEmpty() && password.isNotEmpty()) {
                        checkRegisterPassword(passwordConfirm, password)
                        binding.loginRegisterButton.isEnabled = validate()
                    } else{
                        passwordConfirmValid = false
                        binding.loginRegisterButton.isEnabled = false
                    }
                }
            }
        })
    private val usernameTextWatcher: MyTextWatcher = MyTextWatcher(object : TimerTaskListener {
        override fun timerRun() {
            binding.loginUsername.post {
                val username = binding.loginUsername.editText?.text.toString()
                if (username.isNotEmpty()) {
                    checkUserName(username)
                    binding.loginRegisterButton.isEnabled = validate()
                } else{
                    usernameValid = false
                    binding.loginRegisterButton.isEnabled = false
                }
            }
        }
    })
    private val nicknameTextWatcher: MyTextWatcher = MyTextWatcher(object : TimerTaskListener {
        override fun timerRun() {
            binding.nick.post {
                val nickname = binding.nick.editText?.text.toString()
                if (nickname.isNotEmpty()) {
                    checkNickName(nickname)
                    binding.loginRegisterButton.isEnabled = validate()
                } else{
                    nicknameValid = false
                    binding.loginRegisterButton.isEnabled = false
                }
            }
        }
    })
    private fun cancelTimers() {
        usernameTextWatcher.cancelTimer()
        passwordTextWatcher.cancelTimer()
        passwordConfirmTextWatcher.cancelTimer()
        nicknameTextWatcher.cancelTimer()
        binding.loginUsername.editText?.removeTextChangedListener(usernameTextWatcher)
        binding.loginPassword.editText?.removeTextChangedListener(passwordTextWatcher)
        binding.loginPasswordConfirm.editText?.removeTextChangedListener(passwordConfirmTextWatcher)
        binding.nick.editText?.removeTextChangedListener(nicknameTextWatcher)
    }
    private fun setupButtons() {
        binding.loginRegisterToggle.setOnClickListener {
            toggleLoginRegister()
        }
        binding.loginRegisterButton.setOnClickListener {
            cancelTimers()
            KeyboardHelper.hideSoftwareKeyboard(requireContext(), binding.root.windowToken)
            if (validate()) {
                val email = binding.loginUsername.editText?.text.toString()
                val password = binding.loginPassword.editText?.text.toString()
                if (isLogin)
                    login(email, password)
                else {
                    val nickname = binding.nick.editText?.text.toString()
                    register(email, password, nickname)
                }
            }
        }
    }
    private fun toggleLoginRegister() {
        isLogin = !isLogin
        if (isLogin) {
            binding.loginRegisterButton.setText(R.string.login_str)
            binding.loginRegisterToggle.setText(R.string.register_str)
            binding.loginPasswordConfirm.visibility = View.GONE
            binding.resetPassword.visibility = View.VISIBLE
            binding.nick.visibility = View.GONE
        } else {
            binding.loginRegisterButton.setText(R.string.register_str)
            binding.loginRegisterToggle.setText(R.string.login_str)
            binding.loginPasswordConfirm.visibility = View.VISIBLE
            binding.nick.visibility = View.VISIBLE
            binding.resetPassword.visibility = View.INVISIBLE
        }
        binding.loginRegisterButton.isEnabled = validate()
    }
    private fun login(email: String, password: String){
        FirebaseHandler.Authentication.login(email, password).apply {
            addOnSuccessListener {
                Log.d("Login", "signInWithEmail:success")
                findNavController().navigate(R.id.action_navigation_login_to_navigation_home)
            }
            addOnFailureListener {
                Log.w("Login", "signInWithEmail:failure", it)
                Snackbar.make(
                    binding.root,
                    "Authentication failed.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun register(email: String, password: String, nickname: String){
        FirebaseHandler.Authentication.register(email, password).apply {
            addOnSuccessListener {
                Log.d("Register", "createUserWithEmail:success")
                FirebaseHandler.RealtimeDatabase.addUser(User(email))
                FirebaseHandler.RealtimeDatabase.setUserNickname(nickname)
                findNavController().navigate(R.id.action_navigation_login_to_navigation_home)
            }
            addOnFailureListener {
                Log.d("Register", "createUserWithEmail:failure")
                Snackbar.make(
                    binding.root,
                    "Registering failed.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}
