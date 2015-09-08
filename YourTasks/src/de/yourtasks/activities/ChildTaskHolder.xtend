package de.yourtasks.activities

import android.R
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.google.api.client.util.DateTime
import de.yourtasks.model.Tasks
import de.yourtasks.taskendpoint.model.Task
import java.util.Date

class ChildTaskHolder extends TaskHolder {
	
//	private static final long MILLIS_TO_REMOVE_ERLEDIGTE = 30000
	
	TextView textView
	
	CheckBox checkBox
	
	Tasks tasks
	
	Task task = null
	
	Context context
	
	new(View itemView, Tasks tasks) {
		super(itemView)
		
		this.context = itemView.context
		
		this.tasks = tasks
		
		textView = itemView.findViewById(de.yourtasks.R.id.itemTextView) as TextView

		checkBox = itemView.findViewById(de.yourtasks.R.id.itemCheck) as CheckBox
		
//		textView.setOnLongClickListener(new OnLongClickListener() {
//				override onLongClick(View view) {
//
//					if (actionMode != null) {
//						return false;
//					}
//					selectedItem = task;
//
//					// start the CAB using the ActionMode.Callback defined above
//					actionMode = activity.startActionMode(actionModeCallback);
//					rowView.setSelected(true);
//					notifyDataSetChanged();
//					return true;
//				}
//			});

		textView.setOnClickListener[context.startActivity(TaskActivity.createOpenIntent(context, task))]
		
		checkBox.setOnClickListener[
				val checked = (it as CheckBox).isChecked()
				task.setCompleted(if (checked) new DateTime(new Date()) else null)
				tasks.saveTask(task, context)
				
//				new Timer().schedule(new TimerTask() {
//						override void run() {
//							new Handler(Looper.getMainLooper()).post(new Runnable() {
//								override run() {
//									tasks.fireDataChanged();
//								}
//							});
//						}
//					}, MILLIS_TO_REMOVE_ERLEDIGTE);
		]		
	}
	
	override bindTask(Task task) {
		this.task = task
		textView.setText(task.getName());
		
		val color = if (task.getPrio() == null || task.getPrio() == 3) 
						R.color.holo_green_dark
					else if (task.getPrio() < 2) 
							R.color.holo_red_dark
						else if (task.getPrio() < 3) 
								R.color.holo_orange_dark
							else R.color.darker_gray;
			
		textView.setTextColor(Resources.getSystem().getColor(color));
		
		checkBox.setChecked(task.getCompleted() != null);
		
		textView.setAlpha(if (task.completed != null) 0.3f else 1.0f);
	}
}