package de.yourtasks;

import de.androidlistexample.BtnListAdapter;
import de.androidlistexample.TaskDetailsActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

public class TaskListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_list);
		
		final BtnListAdapter<String> adapter = new BtnListAdapter<String>(getApplicationContext(), valueList) {
			@Override
			public void itemBtnClicked(final View row, final View btn, final String item) {
	          row.animate().setDuration(2000).alpha(0)
	              .withEndAction(new Runnable() {
	                @Override
	                public void run() {
	                  valueList.remove(item);
	                  notifyDataSetChanged();
	                  row.setAlpha(1);
	                }
	              });
			}

			@Override
			public void itemClicked(View rowView, View textView, String item) {
				Intent intent = new Intent();
				intent.setClassName(getPackageName(), TaskDetailsActivity.class.getName());
				intent.putExtra("selected", item.toString());
				startActivity(intent);
			}
		};
	
		((ListView) findViewById(R.id.taskListView)).setAdapter(adapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.task_list, menu);
		return true;
	}

}
