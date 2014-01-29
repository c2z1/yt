package de.yourtasks.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import de.yourtasks.R;
import de.yourtasks.model.ProjectService;
import de.yourtasks.model.TaskService;
import de.yourtasks.projectendpoint.model.Project;
import de.yourtasks.utils.ui.UIService;

public class ProjectDetailsActivity extends Activity {

    private Project project;


	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);
        // Show the Up button in the action bar.
        setupActionBar();
        
        Long projectId = getIntent().getLongExtra(ProjectService.PROJECT_ID_PARAM, -1);
        
        project = ProjectService.getService().getProject(projectId);
        
        if (project == null) {
        	project = ProjectService.getService().createProject();
		}
        
        UIService.bind((EditText) findViewById(R.id.editName), project, TaskService.NAME);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.project_details, menu);
        return true;
    }
    
	@Override
	public void onBackPressed() {
		ok();
	}

	private void ok() {
		ProjectService.getService().saveProject(project);
		finish();
	}
    

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.project_details_ok:
	        	ok();
	            return true;
	        case R.id.btnDel:
	        	removeProject();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void removeProject() {
		new AlertDialog.Builder(this)
	    .setTitle(R.string.delete_question_title)
	    .setMessage(R.string.project_remove_message)
	    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton) {
	                // Do nothing.
	         }
	    })
	    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton) {
	        	 ProjectService.getService().removeProject(project);
	        	 finish();
	         }
	    }).show();
	}
}
