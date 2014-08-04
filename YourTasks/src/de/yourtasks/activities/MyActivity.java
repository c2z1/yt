package de.yourtasks.activities;

import android.app.Activity;
import android.util.Log;

public class MyActivity extends Activity {
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.i(getClass().getSimpleName(), "Start " + getClass());
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.i(getClass().getSimpleName(), "Stop " + getClass());
	}

}
