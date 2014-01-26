package de.yourtasks.task.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
		
//		findViewById(R.id.task_details_ok).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				ok();
//			}
//		});
//		findViewById(R.id.btnDel).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				TaskService.getService().removeTask(task);
//				finish();
//			}
//		});
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
	        	 TaskService.getService().removeTask(task);
	        	 finish();
	         }
	    }).show();
	}
}
