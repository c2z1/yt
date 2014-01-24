package de.yourtasks.task.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import de.yourtasks.R;
import de.yourtasks.task.TaskService;
import de.yourtasks.taskendpoint.model.Task;
import de.yourtasks.utils.DataChangeListener;

public class TaskListActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_list);
		
		TaskService.getService().loadTasks();
		
		initListView();
		
		findViewById(R.id.newBtn).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startDetailsView(null);
				}
			});
		
		findViewById(R.id.btnRefresh).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TaskService.getService().loadTasks();
			}
		});
	}
	
	private void initListView() {

		final ArrayAdapter<Task> adapter = 
				new ArrayAdapter<Task>(getApplicationContext(), R.layout.btn_list_item, 
						TaskService.getService().getTasks()) {
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						LayoutInflater inflater = (LayoutInflater) getContext()
								.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						
						final View rowView = convertView == null 
								? inflater.inflate(R.layout.btn_list_item, parent, false) : convertView;
						
						final Task item = getItem(position);

						TextView textView = (TextView) rowView.findViewById(R.id.itemTextView);
						
						textView.setText(item.getName());
						
						textView.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								startDetailsView(item);
							}
						});
						
						if (item != null) {
							int color = item.getPrio() != null && item.getPrio() < 3 ? android.R.color.holo_red_dark
											: android.R.color.black;
							
							textView.setTextColor(Resources.getSystem().getColor(color));
						}
		
						CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.itemCheck);
						
						checkBox.setChecked(Boolean.TRUE.equals(item.getCompleted()));
		
						checkBox.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								boolean checked = ((CheckBox) v).isChecked();
								item.setCompleted(checked);
								notifyDataSetChanged();
							}
						});
						
						if (Boolean.TRUE.equals(item.getCompleted())) {
							rowView.setAlpha(0.3f);
						} else {
							rowView.setAlpha(1.0f);
						}

						return rowView;
					}
				};
				
		TaskService.getService().addDataChangeListener(new DataChangeListener<Task>() {
			@Override
			public void dataChanged() {
				adapter.notifyDataSetChanged();
			}
		});

		((ListView) findViewById(R.id.taskListView)).setAdapter(adapter);
	}
	
	private void startDetailsView(Task t) {
		Intent intent = new Intent(this, TaskDetailsActivity.class);
		intent.putExtra(TaskService.TASK_ID_PARAM, t == null ? -1 : t.getId());
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.task_list, menu);
		return true;
	}
}
