package de.yourtasks.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.yourtasks.model.Tasks

class ScreenReceiver extends BroadcastReceiver {
	
	override onReceive(Context context, Intent intent) {
		if (intent.action == Intent.ACTION_SCREEN_ON) {
			Tasks.getService(context).fireDataChanged
		}
	}
	
}