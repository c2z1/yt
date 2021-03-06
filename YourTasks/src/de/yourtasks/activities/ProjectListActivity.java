package de.yourtasks.activities;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.TextView;
import de.yourtasks.R;
import de.yourtasks.model.ProjectService;
import de.yourtasks.projectendpoint.model.Project;
import de.yourtasks.taskendpoint.model.Task;
import de.yourtasks.utils.DataChangeListener;

public class ProjectListActivity extends Activity {
	
	protected Object actionMode;
	private Project selectedItem  = null;

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
		                ProjectService.getService().removeProject(selectedItem);
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


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_project_list);
		
		final ProjectAdapter adapter = new ProjectAdapter(getApplicationContext(), R.layout.activity_project_list, 
				ProjectService.getService().getProjects());
		
		((ListView) findViewById(R.id.project_list)).setAdapter(adapter);
				
		ProjectService.getService().loadProjects();
		
		ProjectService.getService().addDataChangeListener(new DataChangeListener<Project>() {
				@Override
				public void dataChanged() {
					adapter.notifyDataSetChanged();
				}
			});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.project_list, menu);
		return true;
	}
	
	private void startTaskListActivity(Project item) {
		Intent intent = new Intent(this, TaskListActivity.class);
		intent.putExtra(ProjectService.PROJECT_ID_PARAM, item.getId());
		startActivity(intent);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.action_new:
	        	startDetailsView(null);
	            return true;
	        case R.id.action_refresh:
	        	ProjectService.getService().loadProjects();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void startDetailsView(Task t) {
		Intent intent = new Intent(this, ProjectDetailsActivity.class);
		intent.putExtra(ProjectService.PROJECT_ID_PARAM, t == null ? -1 : t.getId());
		startActivity(intent);
	}


	private class ProjectAdapter extends ArrayAdapter<Project> {

		public ProjectAdapter(Context context, int resource, List<Project> objects) {
			super(context, resource, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			final View rowView = convertView == null 
					? inflater.inflate(R.layout.project_list_item, parent, false) : convertView;
			
			final Project item = getItem(position);

			TextView textView = (TextView) rowView.findViewById(R.id.textView_project_list_item);
			
			textView.setText(item.getName());
			
			textView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startTaskListActivity(item);
				}
			});
			
			textView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {

					if (actionMode != null) {
						return false;
					}
					selectedItem = item;

					// start the CAB using the ActionMode.Callback defined above
					actionMode = ProjectListActivity.this.startActionMode(actionModeCallback);
					rowView.setSelected(true);
					notifyDataSetChanged();
					return true;
				}
			});
			
			return rowView;
		}
	}
}
