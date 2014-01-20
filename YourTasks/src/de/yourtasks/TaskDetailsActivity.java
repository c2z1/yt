package de.yourtasks;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class TaskDetailsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_details);
		EditText t = (EditText) findViewById(R.id.editText1);
		t.setText(getIntent().getStringExtra("selected") + " gewählt");
	}
}
