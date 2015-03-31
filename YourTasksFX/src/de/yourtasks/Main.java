package de.yourtasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.api.client.json.gson.GsonFactory;

import de.yourtasks.taskendpoint.Taskendpoint;
import de.yourtasks.taskendpoint.Taskendpoint.ListTask;
import de.yourtasks.taskendpoint.model.Task;

public class Main {
	
	public static final long DEFAULT_TASK_ID = 6666666666666666l;

	public static void main(String[] args) throws Exception {
		ListTask lt = getEndpoint().listTask();
//		lt.setParentTaskId(DEFAULT_TASK_ID);
		lt.setWithCompleted(true);
		
		Collection<Task> tasks = getAllTasks(DEFAULT_TASK_ID, lt);
		
		saveTasksLocal(tasks);
	}

	private static Collection<Task> getAllTasks(long parentTaskId, ListTask lt) throws IOException {
		System.out.println("load : " + parentTaskId + " ...");
		Collection<Task> tasks = new ArrayList<>();
		lt.setParentTaskId(parentTaskId);
		List<Task> val = lt.execute().getItems();
		if (val != null) {
			tasks.addAll(val);
			for (Task task : val) {
				tasks.addAll(getAllTasks(task.getId(), lt));
			}
		}
		return tasks;
//		return val;
	}

	private static void saveTasksLocal(Collection<Task> tasks) throws IOException {
		int ind = 0;
		File file = null;
		do {
			file = Paths.get("C:\\java\\tmp\\YourTask" + (ind == 0 ? "" : ind) + ".txt").toFile();
			ind++;
		} while (file.exists());
		new LocalTaskPersistence(new GsonFactory() , file).saveTasks(tasks);
	}
	
	private static Taskendpoint getEndpoint() throws Exception {
		Taskendpoint.Builder builder = new Taskendpoint.Builder(
				com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport(), 
				new GsonFactory(), null /* httpRequestInitializer */);
		builder.setApplicationName("YourTasks");
		return builder.build();
	}
}
