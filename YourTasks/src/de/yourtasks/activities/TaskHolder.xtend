package de.yourtasks.activities

import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.View
import de.yourtasks.taskendpoint.model.Task

abstract class TaskHolder extends ViewHolder {
	
	
	new(View itemView) {
		super(itemView)
	}

	def abstract void bindTask(Task task)	
}