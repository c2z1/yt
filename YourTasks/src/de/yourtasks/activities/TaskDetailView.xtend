package de.yourtasks.activities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import de.yourtasks.R
import de.yourtasks.model.Tasks
import de.yourtasks.taskendpoint.model.Task
import de.yourtasks.utils.ui.UIService

class TaskDetailView {
	View view
	Task task
	ViewGroup containerView
	View detailView
	LayoutInflater inflater
	boolean expanded
	Tasks taskService
	Context context

	private new(Context context, Tasks service, ViewGroup parent, Task task, boolean expanded) {
		this.context = context
		this.taskService = service
		this.task = task
		inflater = context.getSystemService(Context::LAYOUT_INFLATER_SERVICE) as LayoutInflater
		view = inflater.inflate(R.layout::task_details, parent, false)
		containerView = view.findViewById(R.id::container) as ViewGroup
		if (service.isDefaultTask(task)) {
			view.setVisibility(View::INVISIBLE)
		} else {
			view.setVisibility(View::VISIBLE)
			initFields()
		}
		if (expanded) {
			toggleExpand()
		}
	}

	def static View creatTaskDetailView(Context context, Tasks service, ViewGroup parent, Task task, boolean expanded) {
		return new TaskDetailView(context, service, parent, task, expanded).view
	}

	def private void toggleExpand() {
		if (!expanded) {
			containerView.addView(detailView)
			expanded = true
		} else {
			containerView.removeView(detailView)
			expanded = false
		}
	}

	def private void initFields() {
		var EditText nameEdit = view.findViewById(R.id::editName) as EditText
		UIService::bind(nameEdit, task, Tasks::NAME)
		if (taskService.isCreated(task)) {
			nameEdit.requestFocus()
			var InputMethodManager imm = context.getSystemService(Context::INPUT_METHOD_SERVICE) as InputMethodManager
			imm.showSoftInput(nameEdit, InputMethodManager::SHOW_FORCED)
		}
		view.findViewById(R.id::button_expand).setOnClickListener([toggleExpand()])
		detailView = inflater.inflate(R.layout::task_details_details, containerView, false)
		UIService::bind(detailView.findViewById(R.id::editPrio) as EditText, task, Tasks::PRIO)
		UIService::bind(detailView.findViewById(R.id::editDescription) as EditText, task, Tasks::DESCRIPTION)
		UIService::bind(detailView.findViewById(R.id::editRepeatingDays) as EditText, task,
			Tasks::REPEAT_INTERVAL_DAYS)
		detailView.findViewById(R.id::upPrioButton).setOnClickListener([addToPrio(task, -1, view)])
		detailView.findViewById(R.id::downPrioButton).setOnClickListener([addToPrio(task, 1, view)])
	}

	def private static void addToPrio(Task task, int i, View view) {
		(view.findViewById(R.id::editPrio) as EditText).setText("" + task.getPrio())
	}
}
