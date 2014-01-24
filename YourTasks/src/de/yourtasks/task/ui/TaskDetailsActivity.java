package de.yourtasks.task.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import de.yourtasks.R;
import de.yourtasks.task.TaskService;
import de.yourtasks.taskendpoint.model.Task;
import de.yourtasks.utils.ui.UIService;

public class TaskDetailsActivity extends Activity {
	
	public static String RESULT = "taskAsString";
	
	private Task task;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_details);

		initTask();
		
		UIService.bind((EditText) findViewById(R.id.editName), task, TaskService.NAME);
		UIService.bind((EditText) findViewById(R.id.editPrio), task, TaskService.PRIO);
		
		findViewById(R.id.task_details_ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ok();
			}
		});
		findViewById(R.id.btnDel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TaskService.getService().removeTask(task);
				finish();
			}
		});
	}

	private void initTask() {
		Long id = getIntent().getLongExtra(TaskService.TASK_ID_PARAM, -1);
		task = TaskService.getService().getTask(id);
		
		if (task == null) {
			task = TaskService.getService().createTask();
		}
	}
	
	@Override
	public void onBackPressed() {
		ok();
	}

	private void ok() {
		TaskService.getService().saveTask(task);
		finish();
	}
}
