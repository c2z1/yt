package de.yourtasks.activities;

import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
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

import com.google.api.client.util.DateTime;

import de.yourtasks.R;
import de.yourtasks.model.ProjectService;
import de.yourtasks.model.TaskService;
import de.yourtasks.projectendpoint.model.Project;
import de.yourtasks.taskendpoint.model.Task;
import de.yourtasks.utils.DataChangeListener;

public class TaskListActivity extends Activity {

	private TaskService taskService;
	private long projectId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_list);
		
		projectId = getIntent().getLongExtra(ProjectService.PROJECT_ID_PARAM, -1);
		
		init(projectId);
		
		initActionBar();
	}
	
	private void initActionBar() {
		ActionBar actionBar = getActionBar();
		
		actionBar.setDisplayShowTitleEnabled(false);
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		final ArrayAdapter<Project> spinnerAdapter = new ArrayAdapter<Project>(this, R.layout.project_list_item, 
				ProjectService.getService().getProjects()) {
				public View getView(int position, View convertView, ViewGroup parent) {
					TextView tv = new TextView(getApplicationContext());
					tv.setText(getItem(position).getName());
					return tv;
					
//					LayoutInflater inflater = (LayoutInflater) getContext()
//							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//					
//					final View rowView = convertView == null 
//							? inflater.inflate(R.layout.project_list_item, parent, false) : convertView;
//					
//					final Project item = getItem(position);
//	
//					TextView textView = (TextView) rowView.findViewById(R.id.textView_project_list_item);
//					
//					textView.setText(item.getName());
//					
//					return rowView;
				}
				@Override
					public View getDropDownView(int position, View convertView, ViewGroup parent) {
						return getView(position, convertView, parent);
					}
			};
		
		ProjectService.getService().addDataChangeListener(new DataChangeListener<Project>() {
			@Override
			public void dataChanged() {
				spinnerAdapter.notifyDataSetChanged();
			}
		});
		
		actionBar.setListNavigationCallbacks(spinnerAdapter, new OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				Project p = (Project) spinnerAdapter.getItem(itemPosition);
				init(p.getId());
				return true;
			}
		});
		
		actionBar.show();
		
	}

	private void init(long projectId) {
		if (taskService != null) {
			taskService.setDataChangeListener(null);
		}
		
		taskService = TaskService.getService(projectId);

		final ArrayAdapter<Task> adapter = new TaskAdapter(getApplicationContext(), R.layout.btn_list_item, 
				taskService.getTasks());
					
		taskService.setDataChangeListener(new DataChangeListener<Task>() {
			@Override
			public void dataChanged() {
				Log.i("TaskListActivity", "adapter.notifyDataSetChanged() "
						+ taskService.getTasks().size());
				adapter.notifyDataSetChanged();
			}
		});

		((ListView) findViewById(R.id.taskListView)).setAdapter(adapter);
	}

	private void startDetailsView(Task t) {
		Intent intent = new Intent(this, TaskDetailsActivity.class);
		intent.putExtra(TaskService.TASK_ID_PARAM, t == null ? -1 : t.getId());
		intent.putExtra(ProjectService.PROJECT_ID_PARAM, t == null ? -1 : t.getId());
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
	        	taskService.loadTasks();
	            return true;
	        case R.id.action_projects:
	        	openProjectsActivity();
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void openProjectsActivity() {
		Intent intent = new Intent(this, ProjectListActivity.class);
		startActivity(intent);
	}
	
	private class TaskAdapter extends ArrayAdapter<Task> {

		public TaskAdapter(Context context, int resource, List<Task> objects) {
			super(context, resource, objects);
		}
	
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
}
