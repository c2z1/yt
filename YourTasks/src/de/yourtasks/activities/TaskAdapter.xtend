package de.yourtasks.activities

import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import de.yourtasks.R
import de.yourtasks.model.Tasks
import de.yourtasks.taskendpoint.model.Task
import java.util.List

class TaskAdapter extends Adapter<TaskHolder> {
	
	public static final int PARENT_VIEW_TYPE = 1
	public static final int CHILD_VIEW_TYPE = 2
	
	List<Task> taskList
	
	Tasks tasks
	
	new(List<Task> taskList, Tasks tasks) {
		this.taskList = taskList
		this.tasks = tasks
	}
	
	override getItemCount() {
		taskList.size
	}
	
	override onBindViewHolder(TaskHolder holder, int pos) {
		holder.bindTask(taskList.get(pos))
	}
	
	override onCreateViewHolder(ViewGroup parent, int type) {
		val inflater = LayoutInflater.from(parent.context)
		if (type == PARENT_VIEW_TYPE) {
			return new ParentTaskHolder(inflater.inflate(R.layout.task_details, parent, false), tasks, this)
		} else {
			return new ChildTaskHolder(inflater.inflate(R.layout.task_list_item, parent, false), tasks)
		}
	}
	
	override getItemViewType(int position) {
		return if (position == 0) PARENT_VIEW_TYPE else CHILD_VIEW_TYPE 
	}
}