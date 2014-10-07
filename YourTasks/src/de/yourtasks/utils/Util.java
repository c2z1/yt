package de.yourtasks.utils;

import java.util.Calendar;
import java.util.Date;

import com.google.api.client.util.DateTime;

public class Util {

	public static boolean isEmpty(String name) {
		return name == null || name.isEmpty();
	}

	public static boolean shouldRepeat(DateTime completedDate, Integer repeatIntervalDays) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(completedDate.getValue());
		cal.add(Calendar.DAY_OF_MONTH, repeatIntervalDays);
		cal.set(Calendar.HOUR, 23);
		cal.set(Calendar.MINUTE, 59);
		boolean b = new Date().after(cal.getTime());
		if (b) {
			System.out.println("dkdk");
			cal = Calendar.getInstance();
			cal.setTimeInMillis(completedDate.getValue());
			cal.add(Calendar.DAY_OF_MONTH, repeatIntervalDays);
			cal.set(Calendar.HOUR, 23);
			cal.set(Calendar.MINUTE, 59);
			b = new Date().after(cal.getTime());
		}
		return b;
	}
}
