package com.vfs.class07

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

interface TaskListener
{
    fun taskClicked(index : Int)
    fun taskLongClicked (index : Int)
}

class TaskViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView)
{
    lateinit var taskNameTextView: TextView
    lateinit var taskCompletedCheckBox: CheckBox
    lateinit var taskDividerViewHolder : View

    init
    {
        taskNameTextView = itemView.findViewById<TextView>(R.id.taskTextView_id)
        taskCompletedCheckBox = itemView.findViewById<CheckBox>(R.id.taskCompletionCheckBox_id)
        taskDividerViewHolder = itemView.findViewById<View>(R.id.taskDividerView_id)
    }

    fun bind (task : Task, hideDivider : Boolean)
    {
        taskNameTextView.text = task.name
        taskCompletedCheckBox.isChecked = task.completed

        if (task.completed)
        {
            taskNameTextView.paintFlags = taskNameTextView.paintFlags or
                    Paint.STRIKE_THRU_TEXT_FLAG
            taskNameTextView.alpha = 0.5f
        }
        else
        {
            taskNameTextView.paintFlags = taskNameTextView.paintFlags and
                    Paint.STRIKE_THRU_TEXT_FLAG.inv()
            taskNameTextView.alpha = 1.0f
        }

        taskDividerViewHolder.visibility = View.VISIBLE
        if (hideDivider) taskDividerViewHolder.visibility = View.GONE
    }
}

class TasksAdapter (var listener : TaskListener, val group : Group) : RecyclerView.Adapter<TaskViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder
    {
        val rootView = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_row, parent, false)

        return TaskViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int)
    {
        val thisTask = group.tasks[position]
        holder.bind(thisTask, position == group.tasks.count() - 1)

        holder.itemView.setOnClickListener {
            listener.taskClicked(position)
        }
        holder.itemView.setOnLongClickListener {
            listener.taskLongClicked(position)
            true
        }
    }

    override fun getItemCount(): Int
    {
        return group.tasks.count()
    }
}