package de.yourtasks.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import de.yourtasks.R
import de.yourtasks.model.Tasks
import de.yourtasks.taskendpoint.model.Task
import de.yourtasks.utils.ui.UIService
import de.yourtasks.utils.Util

class ParentTaskHolder extends TaskHolder {
	
	EditText nameEdit
	Tasks tasks
	boolean expanded
	
	ViewGroup containerView
	
	LayoutInflater inflater
	
	View detailView
	
	EditText editPrio
	
	EditText editDescription
	
	EditText editRepeatingDays
	
	TaskAdapter adapter
	
	View view 
	
	new(View view, Tasks tasks, TaskAdapter adapter) {
		super(view)
		this.view = view
		this.inflater = LayoutInflater.from(view.context)
		this.tasks = tasks
		this.adapter = adapter
		nameEdit = view.findViewById(R.id.editName) as EditText
		detailView = inflater.inflate(R.layout.task_details_details, containerView, false)

		containerView = view.findViewById(R.id.container) as ViewGroup

		
		itemView.findViewById(R.id.button_expand).setOnClickListener[toggleExpand]

		editPrio = detailView.findViewById(R.id.editPrio) as EditText
		editDescription = detailView.findViewById(R.id.editDescription) as EditText
		editRepeatingDays = detailView.findViewById(R.id.editRepeatingDays) as EditText
	}
	
	def toggleExpand() {
		expand(!expanded)
	}
	
	def expand(boolean exp) {
		containerView.removeView(detailView)
		if (exp) {
			containerView.addView(detailView)
		}
		expanded = exp
	}
	
	override bindTask(Task task) {
		
		if (tasks.isDefaultTask(task)) {
			view.layoutParams.height = 0
		} else {
			val taskCount = adapter.itemCount
			val shouldExpand =  taskCount <= 1 || (!Util.isEmpty(task.getDescription()) && taskCount < 4);
		
			UIService.bind(nameEdit, task, Tasks.NAME)
			if (tasks.isCreated(task)) {
				nameEdit.requestFocus()
			}
	
			UIService.bind(editPrio, task, Tasks.PRIO)
			UIService.bind(editDescription, task, Tasks.DESCRIPTION)
			UIService.bind(editRepeatingDays, task, Tasks.REPEAT_INTERVAL_DAYS)
			
			detailView.findViewById(R.id.upPrioButton).setOnClickListener[addToPrio(task, -1)]
			detailView.findViewById(R.id.downPrioButton).setOnClickListener[addToPrio(task, 1)]
			expand(shouldExpand)
		}
	}
	
	def addToPrio(Task task, int i) {
		editPrio.setText("" + (task.getPrio() + i));
	}
}