package de.yourtasks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import de.yourtasks.activities.ProjectListActivity;

/**
 * The Main Activity.
 * 
 * This activity starts up the RegisterActivity immediately, which communicates
 * with your App Engine backend using Cloud Endpoints. It also receives push
 * notifications from backend via Google Cloud Messaging (GCM).
 * 
 * Check out RegisterActivity.java for more details.
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
//		Intent intent = new Intent(this, RegisterActivity.class);
		Intent intent = new Intent(this, ProjectListActivity.class);
		startActivity(intent);

		finish();
	}
}
