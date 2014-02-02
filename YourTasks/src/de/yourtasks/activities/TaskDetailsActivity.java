package de.yourtasks.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import de.yourtasks.R;
import de.yourtasks.model.ProjectService;
import de.yourtasks.model.TaskService;
import de.yourtasks.taskendpoint.model.Task;
import de.yourtasks.utils.ui.UIService;

public class TaskDetailsActivity extends Activity {
	
	public static String RESULT = "taskAsString";
	
	private Task task;

	private TaskService taskService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_details);

		Long id = getIntent().getLongExtra(TaskService.TASK_ID_PARAM, -1);
		Long projectId = getIntent().getLongExtra(ProjectService.PROJECT_ID_PARAM, -1);
		
		taskService = TaskService.getService(projectId);
		
		task = taskService.getTask(id);
		
		if (task == null) {
			task = taskService.createTask();
		}
		
		UIService.bind((EditText) findViewById(R.id.editName), task, TaskService.NAME);
		UIService.bind((EditText) findViewById(R.id.editPrio), task, TaskService.PRIO);
		UIService.bind((EditText) findViewById(R.id.editDescription), task, TaskService.DESCRIPTION);
		
		findViewById(R.id.upPrioButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addToPrio(-1);
			}
		});
		findViewById(R.id.downPrioButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addToPrio(+1);
			}
		});
	}

	
	protected void addToPrio(int i) {
		((EditText) findViewById(R.id.editPrio)).setText("" + (task.getPrio() + i));
	}


	@Override
	public void onBackPressed() {
		ok();
	}

	private void ok() {
		taskService.saveTask(task);
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.task_details, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.task_details_ok:
	        	ok();
	            return true;
	        case R.id.btnDel:
	        	removeTask();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void removeTask() {
		new AlertDialog.Builder(this)
	    .setTitle("Update Status")
	    .setMessage(R.string.task_remove_message)
	    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton) {
	                // Do nothing.
	         }
	    })
	    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton) {
	        	 taskService.removeTask(task);
	        	 finish();
	         }
	    }).show();
	}
}
