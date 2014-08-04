package de.yourtasks.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import de.yourtasks.R;
import de.yourtasks.model.Tasks;
import de.yourtasks.taskendpoint.model.Task;
import de.yourtasks.utils.ui.UIService;

public class TaskDetailView {
	
	private View view;
	private Task task;
	private ViewGroup containerView;
	private View detailView;
	private LayoutInflater inflater;
	private boolean expanded;

	private TaskDetailView(Context context, Tasks service, ViewGroup parent, Task task) {
		this.task = task;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.task_details, parent, false); 
		
		containerView = (ViewGroup) view.findViewById(R.id.container);
		
		if (service.isDefaultTask(task)) {
			view.setVisibility(View.INVISIBLE);
		} else {
			view.setVisibility(View.VISIBLE);
			initFields();
		}
	}
	
	public static View creatTaskDetailView(Context context, Tasks service, ViewGroup parent, Task task) {
		return new TaskDetailView(context, service, parent, task).getView();
	}

	private View getView() {
		return view;
	}
	
	private void toggleExpand() {
		if (!expanded) {
			containerView.addView(detailView);
			expanded = true;
		} else {
			containerView.removeView(detailView);
			expanded = false;
		}
	}

	private void initFields() {
		UIService.bind((EditText) view.findViewById(R.id.editName), task, Tasks.NAME);
		
		view.findViewById(R.id.button_expand).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleExpand();
			}
		});

		detailView = inflater.inflate(R.layout.task_details_details, containerView, false);
		UIService.bind((EditText) detailView.findViewById(R.id.editPrio), task, Tasks.PRIO);
		UIService.bind((EditText) detailView.findViewById(R.id.editDescription), task, Tasks.DESCRIPTION);
	
		detailView.findViewById(R.id.upPrioButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addToPrio(task, -1, v);
			}
		});
		detailView.findViewById(R.id.downPrioButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addToPrio(task, +1, v);
			}
		});
	}



	private static void addToPrio(Task task, int i, View view) {
		((EditText) view.findViewById(R.id.editPrio)).setText("" + (task.getPrio() + i));
	}
	
//	protected abstract void layoutchanged();
}
