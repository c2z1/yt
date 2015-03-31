package de.yourtasks.utils;

import java.util.Calendar;
import java.util.Date;

import com.google.api.client.util.DateTime;

public class Util {

	public static boolean isEmpty(String name) {
		return name == null || name.isEmpty();
	}

	public static boolean isDaysAfter(DateTime date, Integer dayCount) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date.getValue());
		cal.add(Calendar.DAY_OF_MONTH, dayCount);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		return new Date().after(cal.getTime());
	}
	
	public static boolean isSecondsAfter(DateTime date, Integer secCount) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date.getValue());
		cal.add(Calendar.SECOND, secCount);
		return new Date().after(cal.getTime());
	}
}
