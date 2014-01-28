package de.yourtasks.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

import de.yourtasks.taskendpoint.Taskendpoint;
import de.yourtasks.taskendpoint.Taskendpoint.ListTask;
import de.yourtasks.taskendpoint.model.Task;
import de.yourtasks.utils.DataChangeListener;
import de.yourtasks.utils.IdCreator;

public class TaskService {
	// {Line}
	public static final String NAME = "name", PRIO = "prio";
	
	public static String TASK_ID_PARAM = "taskId";
	
	private static Map<Long, TaskService> serviceMap = new LinkedHashMap<Long, TaskService>();
	
	private List<Task> createdTasks = Collections.synchronizedList(new ArrayList<Task>());
	private List<Task> taskList = Collections.synchronizedList(new ArrayList<Task>());
	
	private long projectId;
	
	protected TaskService(long projectId) {
		this.projectId = projectId;
	}
	
	public static synchronized TaskService getService(long projectId) {
		TaskService instance = serviceMap.get(projectId);
		if (instance == null) {
			instance  = new TaskService(projectId);
			serviceMap.put(projectId, instance);
			instance.loadTasks();
		}
		return instance;
	}
	
	private static Taskendpoint getEndpoint() {
		Taskendpoint.Builder builder = new Taskendpoint.Builder(
				AndroidHttp.newCompatibleTransport(), new GsonFactory(), null /* httpRequestInitializer */);
		return builder.build();
	}
	
	public void loadTasks() {
		new AsyncTask<Void, Void, List<Task>>() {
			@Override
			protected List<Task> doInBackground(Void... params) {
				if (!ProjectService.local) {
					try {
						ListTask lt = getEndpoint().listTask();
						lt.setProjectId(projectId);
						return lt.execute().getItems();
					} catch (IOException e) {
						Log.e("TaskListActivity", "Error during loading tasks", e);
						e.printStackTrace();
					}
				} else {
					return createDummies();
				}
				return Collections.emptyList();
			}

			@Override
			protected void onPostExecute(List<Task> result) {
				Log.d("TaskListActivity", "tasks loaded " + result.size());
				taskList.clear();
				taskList.addAll(result);
				fireDataChanged();
				
				for (Task task : result) {
					
				}
			}
		}.execute();
	}
	
	private List<Task> createDummies() {
		ArrayList<Task> l = new ArrayList<Task>();
		for (int i = 0; i < 5; i++) {
			Task t = createTask();
			t.setName("Task " + i + "  " + projectId);
			l.add(t);
		}
		return l;
	}

	public Task getTask(Long id) {
		for (Task task : taskList) {
			if (task.getId().equals(id)) return task;
		}
		return null;
	}
	
	private void insertTask(final Task t) {
		new AsyncTask<Void, Void, Task>() {
			@Override
			protected Task doInBackground(Void... params) {
				try {
					return getEndpoint().insertTask(t).execute();
				} catch (IOException e) {
					Log.e("TaskListActivity", "Error during inserting task: " + t, e);
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}.execute();
	}
	
	public void removeTask(final Task t) {
		if (!ProjectService.local) {
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					try {
						return getEndpoint().removeTask(t.getId()).execute();
					} catch (IOException e) {
						Log.e("TaskListActivity", "Error during removing task: " + t, e);
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			}.execute();
		}
		taskList.remove(t);
		fireDataChanged();
	}

	private void updateTask(final Task t) {
		new AsyncTask<Void, Void, Task>() {
			@Override
			protected Task doInBackground(Void... params) {
				try {
					return getEndpoint().updateTask(t).execute();
				} catch (IOException e) {
					Log.e("TaskListActivity", "Error during loading tasks", e);
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}.execute();
	}

	public Task createTask() {
		Task t = new Task();
		t.setPrio(3);
		t.setId(IdCreator.createId());
		t.setProjectId(projectId);
		taskList.add(t);
		createdTasks.add(t);
		return t;
	}

	public void saveTask(Task task) {
		if (!ProjectService.local) {
			if (createdTasks.remove(task)) {
				insertTask(task);
			} else {
				updateTask(task);
			}
		}
		fireDataChanged();
	}
	
	DataChangeListener<Task> listener;
	
	public void setDataChangeListener(DataChangeListener<Task> t) {
		listener = t;
	}
	
	public void fireDataChanged() {
		if (listener != null) {
			listener.dataChanged();
		}
	}

	public List<Task> getTasks() {
		return Collections.unmodifiableList(taskList);
	}
}