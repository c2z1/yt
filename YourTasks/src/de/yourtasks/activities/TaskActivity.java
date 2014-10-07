package de.yourtasks.activities;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.yourtasks.R;
import de.yourtasks.model.Tasks;
import de.yourtasks.taskendpoint.model.Task;
import de.yourtasks.utils.DataChangeListener;

public class TaskActivity extends Activity {

	private Tasks tasks;
	
	private static final String LOG_TAG = TaskActivity.class.getSimpleName();
	
	
	private boolean showWithCompleted = false;
	private Task task;
	private DataChangeListener<Task> changeListener;

	private ArrayList<Task> taskList = new ArrayList<Task>();

	private TaskAdapter adapter;

	private long userTaskId = Tasks.DEFAULT_TASK_ID;

	private SwipeRefreshLayout swipeLayout;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		initUserTaskId();
		
		setContentView(R.layout.activity_task_list);
		
		tasks = Tasks.getService(getApplicationContext());

		initSwipeToRefresh();
		
		if (getIntent().hasExtra(Tasks.TASK_ID_PARAM)) {
			Long id = getIntent().getLongExtra(Tasks.TASK_ID_PARAM, -1);
		
			task = tasks.getTask(id);
		} else if (getIntent().hasExtra(Tasks.TASK_PARENT_ID_PARAM)) {
			Long parentId = getIntent().getLongExtra(Tasks.TASK_PARENT_ID_PARAM, -1);
			task = tasks.createChildTask(parentId);
			
		} else {
			task = tasks.getTask(getDefaultTaskId());
			if (task == null) {
				task = tasks.createTask(getDefaultTaskId());
				tasks.saveTask(task);
			}
			swipeLayout.setRefreshing(true);
			reload(new Runnable() {
				@Override
				public void run() {
					Log.i(LOG_TAG, "end refreshing");
					swipeLayout.setRefreshing(false);
				}
			});
		}
		assert task != null;
		
		
		initActionBar();
		
		initList();
	}

//	private void initUserTaskId() {
//		AccountManager am = AccountManager.get(getApplicationContext());
//		Account[] acconts = am.getAccountsByType("com.google");
//		
//		if (acconts.length > 0) {
//			userTaskId = acconts[0].name.hashCode();
//			Log.i(LOG_TAG, acconts[0].name + " / " + acconts[0].type + " : " + acconts[0].name.hashCode());
//		} 
//	}

	private long getDefaultTaskId() {
		return userTaskId;
	}
	
	private void initSwipeToRefresh() {
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
//        swipeLayout.setColorScheme(android.R.color.holo_blue_dark, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_green_light);
        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_dark, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_green_light);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload(new Runnable() {
					@Override
					public void run() {
						Log.i(LOG_TAG, "end refreshing");
						swipeLayout.setRefreshing(false);
					}
				});
            }
        });
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshTaskList();
	}
	
	@Override
	public boolean onNavigateUp() {
		startDetailsView(tasks.getParent(task));
		return true;
	}
	
	private void refreshTaskList() {
		taskList.clear();
		if (!tasks.isDefaultTask(task)) {
			taskList.add(task);
		}
		taskList.addAll(tasks.getTasks(task.getId(), showWithCompleted));
		adapter.notifyDataSetChanged();
	}
	
	private void initActionBar() {
		ActionBar actionBar = getActionBar();
		
		actionBar.setDisplayHomeAsUpEnabled(true);

		actionBar.setDisplayShowTitleEnabled(false);
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		final ArrayAdapter<Task> spinnerAdapter = new ArrayAdapter<Task>(this, R.layout.menu_task_list_item, 
						tasks.getTasks(task.getParentTaskId(), false)) {
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
					return tv;
				}
			};
		
			
		actionBar.setListNavigationCallbacks(spinnerAdapter, new OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				Task t = (Task) spinnerAdapter.getItem(itemPosition);
				if (t != task) {
					startDetailsView(t);
				}
				return true;
			}
		});
		
		actionBar.setSelectedNavigationItem(spinnerAdapter.getPosition(task));

		actionBar.show();

	}

	private void initList() {
		adapter = new TaskAdapter(this, tasks, this, tasks.isDefaultTask(task) ? null : task,  taskList) {
					@Override
					protected void startDetailsView(Task t) {
						TaskActivity.this.startDetailsView(t);
					}
				};
					
		ListView listView = ((ListView) findViewById(R.id.taskListView));
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		changeListener = new DataChangeListener<Task>() {
			@Override
			public void dataChanged() {
				refreshTaskList();
			}
		};
		tasks.addDataChangeListener(changeListener);
	}

	private void startDetailsView(Task t) {
		Intent intent = new Intent(this, TaskActivity.class);
		if (t == null) {
			intent.putExtra(Tasks.TASK_PARENT_ID_PARAM, task.getId());
		} else {
			intent.putExtra(Tasks.TASK_ID_PARAM, t.getId());
		}
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
	        case R.id.action_accept:
	        	ok();
//	        case R.id.action_refresh:
//	        	reload();
//	            return true;
	        case R.id.show_completed:
	        	showWithCompleted = !item.isChecked();
				item.setChecked(showWithCompleted);
				refreshTaskList();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void reload(Runnable postExecution) {
		tasks.reloadTask(task.getId(), postExecution);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		tasks.saveAllTasksLocal();
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onDestroy() {
		tasks.removeDataChangeListener(changeListener);
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		ok();
	}

	private void ok() {
		tasks.saveTask(task);
		finish();
	}
}
