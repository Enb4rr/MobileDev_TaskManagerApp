package com.vfs.class07

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

interface TaskListener
{
    fun taskClicked(index : Int)
    fun taskLongClicked (index : Int)
}

class TaskViewHolder (rootView : LinearLayout) : RecyclerView.ViewHolder(rootView)
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

        //if (task.completed)
        //{
        //taskNameTextView.paintFlags = taskNameTextView.paintFlags or
        //            Paint.STRIKE_THRU_TEXT_FLAG
        //    itemView.setBackgroundColor(Color.GRAY)
        //}
        //else
        //{
        //    taskNameTextView.paintFlags = taskNameTextView.paintFlags or
        //            Paint.STRIKE_THRU_TEXT_FLAG.inv()
        //    itemView.setBackgroundColor(Color.TRANSPARENT)
        //}

        taskDividerViewHolder.visibility = View.VISIBLE
        if (hideDivider) taskDividerViewHolder.visibility = View.GONE
    }
}

class TasksAdapter (var listener : TaskListener, val group : Group) : RecyclerView.Adapter<TaskViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder
    {
        val rootLinearLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_row, parent, false) as LinearLayout

        return TaskViewHolder(rootLinearLayout)
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