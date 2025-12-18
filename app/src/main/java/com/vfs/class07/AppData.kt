package com.vfs.class07

data class Task (val name: String, var completed: Boolean)

data class Group (val name: String, var tasks: MutableList<Task>)

class AppData
{
    companion object
    {
        var groups: MutableList<Group> = mutableListOf()

        fun initialize ()
        {
            val task_1 = Task("Task 1", false)
            val task_2 = Task("Task 2", false)
            val task_3 = Task("Task 3", false)
            val task_4 = Task("Task 4", false)
            val task_5 = Task("Task 5", false)
            val task_6 = Task("Task 6", false)
            val task_7 = Task("Task 7", false)
            val task_8 = Task("Task 8", false)

            val group_1 = Group("Home", mutableListOf(task_1, task_2))
            val group_2 = Group("Work", mutableListOf(task_3))
            val group_3 = Group("School", mutableListOf(task_4))
            val group_4 = Group("Groceries", mutableListOf(task_5, task_6, task_7))
            val group_5 = Group("Other", mutableListOf(task_8))

            groups = mutableListOf(group_1, group_2, group_3, group_4, group_5)
        }
    }
}