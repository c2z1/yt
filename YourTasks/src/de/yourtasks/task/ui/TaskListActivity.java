package de.yourtasks.task.ui;

import java.util.Date;

import com.google.api.client.util.DateTime;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import de.yourtasks.R;
import de.yourtasks.task.TaskService;
import de.yourtasks.taskendpoint.model.Task;
import de.yourtasks.utils.DataChangeListener;

public class TaskListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_list);
		
		getActionBar().show();

		
		TaskService.getService().loadTasks();
		
		initListView();
	}
	
	private void initListView() {
		final ArrayAdapter<Task> adapter = 
				new ArrayAdapter<Task>(getApplicationContext(), R.layout.btn_list_item, 
						TaskService.getService().getTasks()) {
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						LayoutInflater inflater = (LayoutInflater) getContext()
								.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						
						final View rowView = convertView == null 
								? inflater.inflate(R.layout.btn_list_item, parent, false) : convertView;
						
						final Task item = getItem(position);

						TextView textView = (TextView) rowView.findViewById(R.id.itemTextView);
						
						textView.setText(item.getName());
						
						textView.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								startDetailsView(item);
							}
						});
						
						if (item != null) {
							int color = item.getPrio() == null || item.getPrio() == 3 ? android.R.color.holo_orange_dark
									: item.getPrio() < 3 ? android.R.color.holo_red_dark
											: android.R.color.holo_green_dark;
							
							textView.setTextColor(Resources.getSystem().getColor(color));
						}
		
						CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.itemCheck);
						
						checkBox.setChecked(Boolean.TRUE.equals(item.getCompleted()));
		
						checkBox.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								boolean checked = ((CheckBox) v).isChecked();
								item.setCompleted(checked ? new DateTime(new Date()) : null);
								notifyDataSetChanged();
							}
						});
						
						if (item.getCompleted() != null) {
							rowView.setAlpha(0.3f);
						} else {
							rowView.setAlpha(1.0f);
						}

						return rowView;
					}
				};
				
		TaskService.getService().addDataChangeListener(new DataChangeListener<Task>() {
			@Override
			public void dataChanged() {
				Log.i("TaskListActivity", "adapter.notifyDataSetChanged() " + TaskService.getService().getTasks().size());
				adapter.notifyDataSetChanged();
			}
		});

		((ListView) findViewById(R.id.taskListView)).setAdapter(adapter);
	}
	
	private void startDetailsView(Task t) {
		Intent intent = new Intent(this, TaskDetailsActivity.class);
		intent.putExtra(TaskService.TASK_ID_PARAM, t == null ? -1 : t.getId());
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.task_list, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.action_new:
	        	startDetailsView(null);
	            return true;
	        case R.id.action_refresh:
	        	TaskService.getService().loadTasks();
	            return true;
	        case R.id.action_projects:
	        	openProjectsActivity();
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void openProjectsActivity() {
		
	}
}
