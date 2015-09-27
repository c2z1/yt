package de.yourtasks.activities

import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.yourtasks.R
import de.yourtasks.model.Tasks
import de.yourtasks.taskendpoint.model.Task
import de.yourtasks.utils.DataChangeListener
import java.util.ArrayList
import java.util.Date

import static de.yourtasks.activities.TaskActivity.*

class TaskActivity extends Activity {
	Tasks tasks
	static final String LOG_TAG = typeof(TaskActivity).getSimpleName()
	boolean showWithCompleted = false
	Task task
	DataChangeListener<Task> changeListener
	ArrayList<Task> taskList = new ArrayList<Task>()
	long userTaskId = Tasks::DEFAULT_TASK_ID
	SwipeRefreshLayout swipeLayout
	TaskAdapter adapter
	Date completedSince = new Date()

	override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout::activity_task_list)
		tasks = Tasks::getService(getApplicationContext())
		registerReceiver(new ScreenReceiver([completedSince = new Date()]),
			new IntentFilter(Intent::ACTION_SCREEN_ON))
		initSwipeToRefresh()
		if (getIntent().hasExtra(Tasks::TASK_ID_PARAM)) {
			task = tasks.getTask(getIntent().getLongExtra(Tasks::TASK_ID_PARAM, -1))
		} else if (getIntent().hasExtra(Tasks::TASK_PARENT_ID_PARAM)) {
			var Long parentId = getIntent().getLongExtra(Tasks::TASK_PARENT_ID_PARAM, -1)
			task = tasks.createChildTask(parentId)
		} else {
			task = tasks.getTask(getDefaultTaskId())
			if (task == null) {
				task = tasks.createTask(getDefaultTaskId())
				tasks.saveTask(task, this)
			}

		}
		if (!(task != null)) {
			throw new AssertionError()
		}
		initActionBar()
		initList()
	}

	// private void initUserTaskId() {
	// AccountManager am = AccountManager.get(getApplicationContext());
	// Account[] acconts = am.getAccountsByType("com.google");
	//
	// if (acconts.length > 0) {
	// userTaskId = acconts[0].name.hashCode();
	// Log.i(LOG_TAG, acconts[0].name + " / " + acconts[0].type + " : " + acconts[0].name.hashCode());
	// } 
	// }
	def private long getDefaultTaskId() {
		return userTaskId
	}

	def private void initSwipeToRefresh() {
		swipeLayout = findViewById(R.id::swipeLayout) as SwipeRefreshLayout
		swipeLayout.setColorSchemeResources(android.R.color::holo_blue_dark, android.R.color::holo_blue_light,
			android.R.color::holo_green_light, android.R.color::holo_green_light)
		swipeLayout.setOnRefreshListener([
			completedSince = new Date()
			reload([Log::i(LOG_TAG, "end refreshing") swipeLayout.setRefreshing(false)])
		])
	}

	override protected void onResume() {
		super.onResume()
		refreshTaskList()
	}

	override boolean onNavigateUp() {
		startActivity(createOpenIntent(this, tasks.getParent(task)))
		return true
	}

	def private void refreshTaskList() {
		taskList.clear()
		taskList.add(task)
		taskList.addAll(tasks.getTasks(task.getId(), completedSince))
		adapter.notifyDataSetChanged()
	}

	def private void initActionBar() {
		var ActionBar actionBar = getActionBar()
		actionBar.setDisplayHomeAsUpEnabled(true)
		actionBar.setDisplayShowTitleEnabled(false)
		actionBar.setNavigationMode(ActionBar::NAVIGATION_MODE_LIST)
		val ArrayAdapter<Task> spinnerAdapter = new ArrayAdapter<Task>(this, R.layout::menu_task_list_item,
			tasks.getTasks(task.getParentTaskId(), completedSince)) {
			override View getView(int position, View convertView, ViewGroup parent) {
				var TextView tv = new TextView(getApplicationContext())
				tv.setTextColor(getResources().getColor(android.R.color::black))
				tv.setText(getItem(position).getName())
				tv.setTextSize(16)
				return tv
			}

			override View getDropDownView(int position, View convertView, ViewGroup parent) {
				var View tv = getView(position, convertView, parent)
				tv.setPadding(20, 20, 20, 20)
				return tv
			}
		}
		actionBar.setListNavigationCallbacks(spinnerAdapter,[ int itemPosition, long itemId |
			var Task t = spinnerAdapter.getItem(itemPosition) as Task
			if (t != task) {
				startActivity(createOpenIntent(TaskActivity.this, t))
			}
			return true
		])
		actionBar.setSelectedNavigationItem(spinnerAdapter.getPosition(task))
		actionBar.show()
	}

	def private void initList() {
		var RecyclerView recyclerView = (findViewById(R.id::taskListView) as RecyclerView)
		recyclerView.setLayoutManager(new LinearLayoutManager(this))
		recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext()))
		adapter = new TaskAdapter(taskList, tasks)
		recyclerView.setAdapter(adapter)
		changeListener = [refreshTaskList()]
		tasks.addDataChangeListener(changeListener)
	}

	override boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu::task_list, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id::action_new: {
				startActivity(createNewIntent(this, task))
			}
			case R.id::action_accept: {
				ok()
			}
			case R.id::show_completed: {
				showWithCompleted = !item.isChecked()
				item.setChecked(showWithCompleted)
				completedSince = if(showWithCompleted) new Date(1) else new Date()
				refreshTaskList()
			}
			default: {
				return super.onOptionsItemSelected(item)
			}
		}
		return true
	}

	def private void reload(Runnable postExecution) {
		tasks.reloadTask(task.getId(), postExecution, this)
	}

	override protected void onSaveInstanceState(Bundle outState) {
		tasks.saveAllTasksLocal()
		super.onSaveInstanceState(outState)
	}

	override protected void onDestroy() {
		tasks.removeDataChangeListener(changeListener)
		super.onDestroy()
	}

	override void onBackPressed() {
		ok()
	}

	def private void ok() {
		tasks.saveTask(task, this)
		finish()
	}

	def static Intent createNewIntent(Context ctx, Task parent) {
		var Intent intent = new Intent(ctx, typeof(TaskActivity))
		intent.putExtra(Tasks::TASK_PARENT_ID_PARAM, parent.getId())
		return intent
	}

	def static Intent createOpenIntent(Context ctx, Task task) {
		var Intent intent = new Intent(ctx, typeof(TaskActivity))
		intent.putExtra(Tasks::TASK_ID_PARAM, if (task == null) Tasks::DEFAULT_TASK_ID else task.getId())
		return intent
	}
}
