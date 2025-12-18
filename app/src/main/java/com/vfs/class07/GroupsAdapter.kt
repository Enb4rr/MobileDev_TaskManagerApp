package com.vfs.class07

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

interface GroupListener
{
    fun groupClicked(index : Int)
    fun groupLongClicked (index : Int)
}

class GroupsViewHolder (rootView : LinearLayout) : RecyclerView.ViewHolder(rootView)
{
    lateinit var groupNameTextView: TextView
    lateinit var groupCountTextView: TextView
    lateinit var dividerViewHolder : View

    init
    {
        groupNameTextView = itemView.findViewById<TextView>(R.id.groupNameTextView_id)
        groupCountTextView = itemView.findViewById<TextView>(R.id.groupCountTextView_id)
        dividerViewHolder = itemView.findViewById<View>(R.id.dividerView_id)
    }

    fun bind (group : Group, hideDivider : Boolean)
    {
        val groupName: String = group.name
        val groupTasksCount: Int = group.tasks.count()

        groupNameTextView.text = group.name
        groupCountTextView.text = "${group.tasks.count()} Active Items"

        dividerViewHolder.visibility = View.VISIBLE
        if (hideDivider) dividerViewHolder.visibility = View.GONE
    }
}

class GroupsAdapter (var listener : GroupListener) : RecyclerView.Adapter<GroupsViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder
    {
        val rootLinearLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_row, parent, false) as LinearLayout

        return GroupsViewHolder(rootLinearLayout)
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int)
    {
        val thisGroup = AppData.groups[position]
        holder.bind(thisGroup, position == AppData.groups.count() - 1)

        holder.itemView.setOnClickListener {
            listener.groupClicked(position)
        }
        holder.itemView.setOnLongClickListener {
            listener.groupLongClicked(position)
            true
        }
    }

    override fun getItemCount(): Int
    {
        return AppData.groups.count()
    }
}