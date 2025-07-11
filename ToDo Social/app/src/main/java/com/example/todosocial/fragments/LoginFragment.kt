package com.example.todosocial.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.todosocial.R
import com.example.todosocial.data.AppDatabase
import com.example.todosocial.data.UserDao
import com.example.todosocial.utils.SharedPrefsManager
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var dao: UserDao

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        dao = AppDatabase.getDatabase(requireContext()).userDao()

        val etEmail = view.findViewById<EditText>(R.id.etEmailLogin)
        val etPassword = view.findViewById<EditText>(R.id.etPasswordLogin)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvRegisterLink = view.findViewById<TextView>(R.id.tvRegisterLink)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Completează toate câmpurile", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = dao.login(email, password)
                if (user != null) {
                    SharedPrefsManager.saveUser(requireContext(), user.id, user.username, user.email, user.password)
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                } else {
                    Toast.makeText(requireContext(), "Credențiale invalide", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvRegisterLink.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }
}
