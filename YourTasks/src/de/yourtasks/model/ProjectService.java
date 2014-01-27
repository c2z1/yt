package de.yourtasks.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

import de.yourtasks.projectendpoint.Projectendpoint;
import de.yourtasks.projectendpoint.model.Project;
import de.yourtasks.utils.DataChangeListener;
import de.yourtasks.utils.IdCreator;

public class ProjectService {

	public static final String PROJECT_ID_PARAM = "project_id";

	private static ProjectService instance;
	
	private List<Project> createdProjects = Collections.synchronizedList(new ArrayList<Project>());
	private List<Project> projectList = Collections.synchronizedList(new ArrayList<Project>());
	
	public static boolean local = false;
	
	protected ProjectService() {
	}
	
	public static synchronized ProjectService getService() {
		if (instance == null) {
			instance  = new ProjectService();
		}
		return instance;
	}
	
	private static Projectendpoint getEndpoint() {
		Projectendpoint.Builder builder = new Projectendpoint.Builder(
				AndroidHttp.newCompatibleTransport(), new GsonFactory(), null /* httpRequestInitializer */);
		return builder.build();
	}
	
	public void loadProjects() {
		new AsyncTask<Void, Void, List<Project>>() {
			@Override
			protected List<Project> doInBackground(Void... params) {
				if (!local) {
					try {
						List<Project> val = getEndpoint().listProject().execute().getItems();
						if (val != null) return val;
					} catch (IOException e) {
						Log.e("ProjectListActivity", "Error during loading projects", e);
						e.printStackTrace();
					}
				} else {
					return createDummies();
				}
				return Collections.emptyList();
			}

			@Override
			protected void onPostExecute(List<Project> result) {
				Log.d("ProjectService", "projects loaded " + result.size());
				projectList.clear();
				projectList.addAll(result);
				fireDataChanged();
			}
		}.execute();
	}
	
	private List<Project> createDummies() {
		ArrayList<Project> l = new ArrayList<Project>();
		for (int i = 0; i < 5; i++) {
			Project t = createProject();
			t.setName("Project " + i);
			l.add(t);
		}
		return l;
	}

	public Project getProject(Long id) {
		for (Project project : projectList) {
			if (project.getId().equals(id)) return project;
		}
		return null;
	}
	
	private void insertProject(final Project t) {
		new AsyncTask<Void, Void, Project>() {
			@Override
			protected Project doInBackground(Void... params) {
				try {
					return getEndpoint().insertProject(t).execute();
				} catch (IOException e) {
					Log.e("ProjectListActivity", "Error during inserting project: " + t, e);
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}.execute();
	}
	
	public void removeProject(final Project t) {
		if (!local) {
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					try {
						return getEndpoint().removeProject(t.getId()).execute();
					} catch (IOException e) {
						Log.e("ProjectListActivity", "Error during removing project: " + t, e);
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			}.execute();
		}
		projectList.remove(t);
		fireDataChanged();
	}

	private void updateProject(final Project t) {
		new AsyncTask<Void, Void, Project>() {
			@Override
			protected Project doInBackground(Void... params) {
				try {
					return getEndpoint().updateProject(t).execute();
				} catch (IOException e) {
					Log.e("ProjectService", "Error during loading projects", e);
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}.execute();
	}

	public Project createProject() {
		Long id = IdCreator.createId();
		Project t = new Project();
		t.setId(id);
		projectList.add(t);
		createdProjects.add(t);
		return t;
	}

	public void saveProject(Project project) {
		if (!local) {
			if (createdProjects.remove(project)) {
				insertProject(project);
			} else {
				updateProject(project);
			}
		}
		fireDataChanged();
	}
	
	
	List<DataChangeListener<Project>> listenerList = new ArrayList<DataChangeListener<Project>>();
	
	public void addDataChangeListener(DataChangeListener<Project> t) {
		listenerList.add(t);
	}
	
	public void fireDataChanged() {
		for (DataChangeListener<Project> listener : listenerList) {
			listener.dataChanged();
		}
	}

	public List<Project> getProjects() {
		return Collections.unmodifiableList(projectList);
	}
}
