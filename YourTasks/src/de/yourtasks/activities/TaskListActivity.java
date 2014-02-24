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
import android.text.style.LineHeightSpan.WithDensity;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
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
	
	protected Object actionMode;
	private Task selectedItem  = null;
	
	private boolean showWithCompleted = false;

	private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
			    // Called when the action mode is created; startActionMode() was called
			    @Override
			    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			        // Inflate a menu resource providing context menu items
			        MenuInflater inflater = mode.getMenuInflater();
			        inflater.inflate(R.menu.task_list_context, menu);
			        return true;
			    }
		
			    // Called each time the action mode is shown. Always called after onCreateActionMode, but
			    // may be called multiple times if the mode is invalidated.
			    @Override
			    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			        return false; // Return false if nothing is done
			    }
		
			    // Called when the user selects a contextual menu item
			    @Override
			    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			        switch (item.getItemId()) {
			            case R.id.btnDel:
			                taskService.removeTask(selectedItem);
			                mode.finish();
			                return true;
			            default:
			                return false;
			        }
			    }
		
			    // Called when the user exits the action mode
			    @Override
			    public void onDestroyActionMode(ActionMode mode) {
			        actionMode = null;
			        selectedItem  = null;
			    }
			};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_list);
		
		projectId = getIntent().getLongExtra(ProjectService.PROJECT_ID_PARAM, -1);
		
		initActionBar();
		
		init(projectId);

	}
	
	private void initActionBar() {
		ActionBar actionBar = getActionBar();
		
		actionBar.setDisplayHomeAsUpEnabled(true);

		actionBar.setDisplayShowTitleEnabled(false);
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		final ArrayAdapter<Project> spinnerAdapter = new ArrayAdapter<Project>(this, R.layout.project_list_item, 
						ProjectService.getService().getProjects()) {
				public View getView(int position, View convertView, ViewGroup parent) {
					TextView tv = new TextView(getApplicationContext());
					tv.setTextColor(getResources().getColor(android.R.color.black));
					tv.setText(getItem(position).getName());
					tv.setTextSize(16);
					return tv;
				}
				@Override
					public View getDropDownView(int position, View convertView, ViewGroup parent) {
						View tv = getView(position, convertView, parent);
						tv.setPadding(20, 20, 20, 20);
//						tv.setBackground(getResources().getDrawable(R.drawable.row_border));
						return tv;
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
		this.projectId = projectId;
		if (taskService != null) {
			taskService.setDataChangeListener(null);
		}
		
		taskService = TaskService.getService(projectId);

		final ArrayAdapter<Task> adapter = new TaskAdapter(getApplicationContext(), R.layout.task_list_item, 
				taskService.getTasks());
					
		taskService.setDataChangeListener(new DataChangeListener<Task>() {
			@Override
			public void dataChanged() {
				Log.i("TaskListActivity", "adapter.notifyDataSetChanged() "
						+ taskService.getTasks().size());
				adapter.notifyDataSetChanged();
			}
		});

		ListView listView = ((ListView) findViewById(R.id.taskListView));
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		List<Project> projects = ProjectService.getService().getProjects();
		int indx = 0;
		for (Project project : projects) {
			if (project.getId().equals(projectId)) {
				getActionBar().setSelectedNavigationItem(indx);
				break;
			}
			indx++;
		}
	}

	private void startDetailsView(Task t) {
		Intent intent = new Intent(this, TaskDetailsActivity.class);
		intent.putExtra(TaskService.TASK_ID_PARAM, t == null ? -1 : t.getId());
		intent.putExtra(ProjectService.PROJECT_ID_PARAM, projectId);
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
	        	reload();
	            return true;
	        case R.id.action_projects:
	        	startProjectsActivity();
	        	return true;
	        case R.id.action_edit_project:
	        	startProjectDetailsView();
	        	return true;
	        case R.id.show_completed:
	        	showWithCompleted = !item.isChecked();
				item.setChecked(showWithCompleted);
				reload();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void reload() {
		taskService.loadTasks(isShowCompleted());
	}

	private boolean isShowCompleted() {
		return showWithCompleted;
	}

	private void startProjectDetailsView() {
		Intent intent = new Intent(this, ProjectDetailsActivity.class);
		intent.putExtra(ProjectService.PROJECT_ID_PARAM, projectId);
		startActivity(intent);
	}

	private void startProjectsActivity() {
		Intent intent = new Intent(this, ProjectListActivity.class);
		startActivity(intent);
	}
	
	private class TaskAdapter extends ArrayAdapter<Task> {

		public TaskAdapter(Context context, int resource, List<Task> objects) {
			super(context, resource, objects);
		}
	
		@Override
		public View getView(int position, final View convertView, final ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			final View rowView = convertView == null 
					? inflater.inflate(R.layout.task_list_item, parent, false) : convertView;
			
			final Task task = getItem(position);

			int col = task.equals(selectedItem)
					? getResources().getColor(android.R.color.holo_blue_light)
							: getResources().getColor(android.R.color.transparent);
					
			Log.i("TaskListActivity", "Item : " + task + " , Col : " + col);
			
			rowView.setBackgroundColor(col);
			
			TextView textView = (TextView) rowView.findViewById(R.id.itemTextView);
			
			textView.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View view) {
	
						if (actionMode != null) {
							return false;
						}
						selectedItem = task;
	
						// start the CAB using the ActionMode.Callback defined above
						actionMode = TaskListActivity.this.startActionMode(actionModeCallback);
						rowView.setSelected(true);
						notifyDataSetChanged();
						return true;
					}
				});
			
			textView.setText(task.getName());
			
			textView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startDetailsView(task);
				}
			});
			
			if (task != null) {
				int color = task.getPrio() == null || task.getPrio() == 3 
						? android.R.color.holo_green_dark
							: task.getPrio() < 2 
							? android.R.color.holo_red_dark
									: task.getPrio() < 3 
									? android.R.color.holo_orange_dark
											: android.R.color.darker_gray;
				
				textView.setTextColor(Resources.getSystem().getColor(color));
			}

			CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.itemCheck);
			
			checkBox.setChecked(task.getCompleted() != null);

			checkBox.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean checked = ((CheckBox) v).isChecked();
					task.setCompleted(checked ? new DateTime(new Date()) : null);
					taskService.saveTask(task);
					notifyDataSetChanged();
				}
			});
			
			if (task.getCompleted() != null) {
				textView.setAlpha(0.3f);
			} else {
				textView.setAlpha(1.0f);
			}

			return rowView;
		}
	};
}
