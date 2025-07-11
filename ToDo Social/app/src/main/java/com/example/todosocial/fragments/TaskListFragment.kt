package com.example.todosocial.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todosocial.R
import com.example.todosocial.adapters.TaskAdapter
import com.example.todosocial.data.TaskDao
import com.example.todosocial.data.TaskDatabase
import com.example.todosocial.utils.SharedPrefsManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class TaskListFragment : Fragment() {

    private lateinit var taskDao: TaskDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task_list, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewTasks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        taskDao = TaskDatabase.getDatabase(requireContext()).taskDao()

        adapter = TaskAdapter { task ->
            lifecycleScope.launch {
                taskDao.delete(task)
            }
        }

        recyclerView.adapter = adapter

        val userId = SharedPrefsManager.getUserId(requireContext())
        taskDao.getAllForUser(userId).observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
        }

        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAddTask)
        fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_taskListFragment_to_addTaskFragment)
        }

        return view
    }
}
