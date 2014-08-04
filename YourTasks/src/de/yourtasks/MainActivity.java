package de.yourtasks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import de.yourtasks.activities.TaskActivity;

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
		
		Intent intent = new Intent(this, TaskActivity.class);
		startActivity(intent);

		finish();
	}
}
