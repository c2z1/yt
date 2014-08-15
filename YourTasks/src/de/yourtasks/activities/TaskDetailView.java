package de.yourtasks.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
	private Tasks taskService;
	private Context context;

	private TaskDetailView(Context context, Tasks service, ViewGroup parent, Task task, boolean expanded) {
		this.context = context;
		this.taskService = service;
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
		if (expanded) {
			toggleExpand();
		}
	}
	
	public static View creatTaskDetailView(Context context, Tasks service, ViewGroup parent, Task task, boolean expanded) {
		return new TaskDetailView(context, service, parent, task, expanded).getView();
	}

	private View getView() {
		return view;
	}
	
	private void toggleExpand() {
//		ImageButton btn = ((ImageButton) view.findViewById(R.id.button_expand));
		if (!expanded) {
			containerView.addView(detailView);
//			btn.setImageResource(AndrR..drawable."@android:drawable/arrow_up_float");
			expanded = true;
		} else {
			containerView.removeView(detailView);
			expanded = false;
		}
	}

	private void initFields() {
		EditText nameEdit = (EditText) view.findViewById(R.id.editName);
		UIService.bind(nameEdit, task, Tasks.NAME);
		
		if (taskService.isCreated(task)) {
			nameEdit.requestFocus();
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(nameEdit, InputMethodManager.SHOW_FORCED);
		}
		
		
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
				addToPrio(task, -1, view);
			}
		});
		detailView.findViewById(R.id.downPrioButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addToPrio(task, +1, view);
			}
		});
	}



	private static void addToPrio(Task task, int i, View view) {
		((EditText) view.findViewById(R.id.editPrio)).setText("" + (task.getPrio() + i));
	}
	
//	protected abstract void layoutchanged();
}
