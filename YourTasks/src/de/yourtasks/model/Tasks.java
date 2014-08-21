package de.yourtasks.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import de.yourtasks.taskendpoint.Taskendpoint;
import de.yourtasks.taskendpoint.Taskendpoint.ListTask;
import de.yourtasks.taskendpoint.model.Task;
import de.yourtasks.utils.DataChangeListener;
import de.yourtasks.utils.IdCreator;
import de.yourtasks.utils.Util;

public class Tasks {

	private static final String FILENAME = "localTasks.txt";
	
	public static final String NAME = "name", PRIO = "prio", DESCRIPTION = "description";

	public static final long DEFAULT_TASK_ID = 6666666666666666l;

	public static String TASK_ID_PARAM = "taskId";
	public static String TASK_PARENT_ID_PARAM = "taskParentId";
	
	private static LocalTaskPersistence localPersister;
	
	private List<Task> createdTasks = Collections.synchronizedList(new ArrayList<Task>());
	
	private static boolean local = false;
	
	private static Tasks instance;
	private Context applContext;
	private Map<Long, Task> taskMap = new LinkedHashMap<Long, Task>();
	private Map<Long, Collection<Task>> parentTaskMap = new LinkedHashMap<Long, Collection<Task>>();
	
	public static synchronized Tasks getService(Context context) {
		if (instance == null) {
			instance = new Tasks();
			instance.init(context);
		}
		return instance;
	}

	public Task getTask(Long taskId) {
		return taskMap.get(taskId);
	}
	
	public List<Task> getTasks(Long parentTaskId, boolean showWithCompleted) {
		Collection<Task> val = parentTaskMap.get(parentTaskId);
		if (val == null) {
			return Collections.<Task>emptyList();
		}
		List<Task> ret = new ArrayList<Task>();
		for (Task task : val) {
			if (showWithCompleted || task.getCompleted() == null) {
				ret.add(task);
			}
		}
		return ret;
	}
	
	private void init(Context context) {
		applContext = context;
		String filePath = context.getFilesDir().getPath().toString() + "/" + FILENAME;
		localPersister = new LocalTaskPersistence(new AndroidJsonFactory(), new File(filePath));
		loadAllTasksLocal();
	}

	private static Taskendpoint getEndpoint() {
		Taskendpoint.Builder builder = new Taskendpoint.Builder(
				AndroidHttp.newCompatibleTransport(), new GsonFactory(), null /* httpRequestInitializer */);
		return builder.build();
	}
	
//	public void loadTasks(final boolean withCompleted) {
//		if (!local) {
//			new AsyncTask<Void, Void, List<Task>>() {
//				@Override
//				protected List<Task> doInBackground(Void... params) {
//						try {
//							ListTask lt = getEndpoint().listTask();
//							lt.setParentTaskId(parentTaskId);
//							lt.setWithCompleted(withCompleted);
//							List<Task> val = lt.execute().getItems();
//							if (val != null) return val;
//						} catch (IOException e) {
//							Log.e("TaskListActivity", "Error during loading tasks", e);
//							e.printStackTrace();
//						}
//					return Collections.emptyList();
//				}
//	
//				@Override
//				protected void onPostExecute(List<Task> result) {
//					Log.d("TaskListActivity", "tasks loaded " + result.size());
//					taskList.clear();
//					taskList.addAll(result);
//					fireDataChanged();
//				}
//			}.execute();
//		} else {
//			taskList.clear();
//			createDummies();
//			fireDataChanged();
//		}
//	}
	
	private void loadAllTasksLocal() {
		Collection<Task> tasks;
		try {
			tasks = localPersister.loadTasks();
			for (Task task : tasks) {
				addToMaps(task);
			}		
		} catch (IOException e) {
			Toast.makeText(applContext, "Load failed : " + e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
	
	@SuppressLint("ShowToast")
	public void saveAllTasksLocal() {
		Collection<Task> allTasks = new ArrayList<Task>(taskMap.values());
				
		try {
			localPersister.saveTasks(allTasks);
		} catch (IOException e) {
			Toast.makeText(applContext, "Save failed : " + e.getMessage(), 5);
			e.printStackTrace();
		}
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
		for (Task subTask : getChildTasks(t)) {
			removeTask(subTask);
		}
		if (!createdTasks.remove(t)) {
			if (!local) {
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
		}
		taskMap.remove(t.getId());
		parentTaskMap.get(t.getParentTaskId()).remove(t);
		fireDataChanged();
	}

	public List<Task> getChildTasks(Task t) {
		return getTasks(t.getId(), true);
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

	public Task createChildTask(long parentTaskId) {
		return createTask(IdCreator.createId(), parentTaskId);
	}
	
	public Task createTask(long taskId) {
		return createTask(taskId, null);
	}
	
	private Task createTask(long taskId, Long parentTaskId) {
		Task t = new Task();
		t.setPrio(3);
		t.setId(taskId);
		t.setParentTaskId(parentTaskId);
		addToMaps(t);
		createdTasks.add(t);
		return t;
	}

	private void addToMaps(Task t) {
		taskMap.put(t.getId(), t);
		Collection<Task> coll = parentTaskMap.get(t.getParentTaskId());
		if (coll == null) {
			coll = new ArrayList<Task>();
			parentTaskMap.put(t.getParentTaskId(), coll);
		}
		coll.add(t);
	}

	public void saveTask(Task task) {
		if (Util.isEmpty(task.getName()) && Util.isEmpty(task.getDescription())) {
			removeTask(task);
		} else {
			if (!local) {
				if (createdTasks.remove(task)) {
					insertTask(task);
				} else {
					updateTask(task);
				}
			}
		}
		fireDataChanged();
	}
	
	List<DataChangeListener<Task>> listenerList = new ArrayList<DataChangeListener<Task>>();
	
	public void addDataChangeListener(DataChangeListener<Task> t) {
		listenerList.add(t);
	}
	public void removeDataChangeListener(DataChangeListener<Task> t) {
		listenerList.remove(t);
	}
	
	public void fireDataChanged() {
		for (DataChangeListener<Task> l : listenerList) {
			l.dataChanged();
		}
	}

	public void reloadTask(final Long id, final Runnable postExecution) {
		if (!local) {
			new AsyncTask<Void, Void, List<Task>>() {
				@Override
				protected List<Task> doInBackground(Void... params) {
						try {
							ListTask lt = getEndpoint().listTask();
							lt.setParentTaskId(id);
							lt.setWithCompleted(true);
							List<Task> val = lt.execute().getItems();
							if (val != null) return val;
						} catch (IOException e) {
							Log.e("TaskListActivity", "Error during loading tasks", e);
							e.printStackTrace();
						}
					return Collections.emptyList();
				}
	
				@Override
				protected void onPostExecute(List<Task> result) {
					Log.d("TaskListActivity", "tasks loaded " + result.size());
					Collection<Task> oldChilds = parentTaskMap.put(id, new ArrayList<Task>(result));
					if (oldChilds != null) {
						oldChilds.removeAll(result);
						for (Task deletedTask : oldChilds) {
							taskMap.remove(deletedTask.getId());
						}
					}
					for (Task task : result) {
						taskMap.put(task.getId(), task);
					}
					fireDataChanged();
					postExecution.run();
				}
			}.execute();
		} else {
			new Handler().postDelayed(postExecution, 5000);
		}
	}

	public boolean isDefaultTask(Task task) {
		return task.getId() == DEFAULT_TASK_ID;
	}

	public boolean isCreated(Task task) {
		return createdTasks.contains(task);
	}
}
