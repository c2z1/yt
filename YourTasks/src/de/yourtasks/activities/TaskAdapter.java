package de.yourtasks.activities;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.api.client.util.DateTime;

import de.yourtasks.R;
import de.yourtasks.model.Tasks;
import de.yourtasks.taskendpoint.model.Task;
import de.yourtasks.utils.Util;

public abstract class TaskAdapter extends ArrayAdapter<Task> {

	private Tasks service;
	
	private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.task_list_context, menu);
	        return true;
	    }

	    @Override
	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	        return false; 
	    }

	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.btnDel:
	                service.removeTask(selectedItem);
	                mode.finish();
	                return true;
	            default:
	                return false;
	        }
	    }

	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	        actionMode = null;
	        selectedItem  = null;
	    }
	};
	
	protected Object actionMode;
	
	private Task selectedItem  = null;

	private Activity activity;

	private Task task;


	public TaskAdapter(Context context, final Tasks service, Activity activity, Task task, final List<Task> tasks) {
		super(context, R.layout.task_list_item, tasks);
		this.service = service;
		this.activity = activity;
		this.task = task;
	}
	
	protected abstract void startDetailsView(Task t);

	@Override
	public View getView(int position, final View convertView, final ViewGroup parent) {
		final Task tmpTask = getItem(position);
		return (task == tmpTask) 
				? TaskDetailView.creatTaskDetailView(getContext(), service, parent, task, 
						shouldDetailsExpand())
				: getTaskListEntryView(tmpTask, convertView, parent);
	}
	
	private boolean shouldDetailsExpand() {
		return getCount() <= 1 || (!Util.isEmpty(task.getDescription()) && getCount() < 3);
	}

	private static Object LIST_ENTRY_TAG = new Object();
	
	private View getTaskListEntryView(final Task task, final View convertView, final ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View rowView = (convertView == null || convertView.getTag() != LIST_ENTRY_TAG)
			? inflater.inflate(R.layout.task_list_item, parent, false)
			: convertView;
		
		rowView.setTag(LIST_ENTRY_TAG);
		
		int col = task.equals(selectedItem)
				? getContext().getResources().getColor(android.R.color.holo_blue_light)
						: getContext().getResources().getColor(android.R.color.transparent);
				
		Log.i("TaskListActivity", "Item : " + task + " , Col : " + col);
		
		rowView.setBackgroundColor(col);
		
		TextView textView = (TextView) rowView.findViewById(R.id.itemTextView);
		
		textView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {

					if (actionMode != null) {
						return false;
					}
					selectedItem = task;

					// start the CAB using the ActionMode.Callback defined above
					actionMode = activity.startActionMode(actionModeCallback);
					rowView.setSelected(true);
					notifyDataSetChanged();
					return true;
				}
			});
		
		textView.setText(task.getName());
		
		textView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDetailsView(task);
			}
		});
		
		if (task != null) {
			int color = task.getPrio() == null || task.getPrio() == 3 
					? android.R.color.holo_green_dark
						: task.getPrio() < 2 
						? android.R.color.holo_red_dark
								: task.getPrio() < 3 
								? android.R.color.holo_orange_dark
										: android.R.color.darker_gray;
			
			textView.setTextColor(Resources.getSystem().getColor(color));
		}

		CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.itemCheck);
		
		checkBox.setChecked(task.getCompleted() != null);

		checkBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean checked = ((CheckBox) v).isChecked();
				task.setCompleted(checked ? new DateTime(new Date()) : null);
				service.saveTask(task, getContext());
				notifyDataSetChanged();
				
				new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								public void run() {
									service.fireDataChanged();
								}
							});
						}
					}, 5000);
			}
		});
		
		if (task.getCompleted() != null) {
			textView.setAlpha(0.3f);
		} else {
			textView.setAlpha(1.0f);
		}
		return rowView;
	}
}