package com.example.todosocial.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.todosocial.R
import com.example.todosocial.data.Task
import com.example.todosocial.data.TaskDao
import com.example.todosocial.data.TaskDatabase
import com.example.todosocial.utils.SharedPrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddTaskFragment : Fragment() {

    private lateinit var taskDao: TaskDao
    private lateinit var edtTitle: EditText
    private lateinit var edtDescription: EditText
    private lateinit var btnAdd: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_task, container, false)

        edtTitle = view.findViewById(R.id.edtTitle)
        edtDescription = view.findViewById(R.id.edtDescription)
        btnAdd = view.findViewById(R.id.btnAddTask)

        taskDao = TaskDatabase.getDatabase(requireContext()).taskDao()

        btnAdd.setOnClickListener {
            val title = edtTitle.text.toString().trim()
            val description = edtDescription.text.toString().trim()
            val userId = SharedPrefsManager.getUserId(requireContext())

            if (title.isNotEmpty()) {
                lifecycleScope.launch {
                    val task = Task(title = title, description = description, userId = userId)
                    taskDao.insert(task)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Task added", Toast.LENGTH_SHORT).show()
                        edtTitle.text.clear()
                        edtDescription.text.clear()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}