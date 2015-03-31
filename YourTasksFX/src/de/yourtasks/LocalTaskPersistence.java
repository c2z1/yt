package de.yourtasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Key;

import de.yourtasks.taskendpoint.model.Task;

public class LocalTaskPersistence {
	
	private JsonFactory jsonFactory;
	private File file;
	
	public LocalTaskPersistence(JsonFactory jsonFactory, File file) {
		this.jsonFactory = jsonFactory;
		this.file = file;
	}

	public void saveTasks(Collection<Task> tasks) throws IOException {
		saveTasks(tasks, false);
	}
	public void saveTasks(Collection<Task> tasks, boolean pretty) throws IOException {
		FileOutputStream fo = null;
		try {
			TaskList tl = new TaskList();
			tl.list.addAll(tasks);
			byte[] outputbytes = jsonFactory.toPrettyString(tl).getBytes();
			
			if (!file.exists()) {
				file.createNewFile();
			}
			
			fo = new FileOutputStream(file);
			
			fo.write(outputbytes);
	
			fo.flush();
		} finally {
			if (fo != null) fo.close();
		}
	}
	
	public Collection<Task> loadTasks() throws IOException {
		if (!file.exists()) {
			return Collections.emptyList();
		}
		RandomAccessFile f = null;
		try {
			f = new RandomAccessFile(file, "r");
			byte[] b = new byte[(int)f.length()];
			f.read(b);
	
			TaskList tl = jsonFactory.fromString(new String(b, "UTF-8"), TaskList.class);
			return (Collection<Task>) tl.list;
		} finally {
			if (f != null) f.close();
		}
	}
	
	public static class TaskList extends GenericJson {
		@Key public List<Task> list = new ArrayList<Task>();
	}
}
