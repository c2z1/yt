package de.yourtasks.utils;

import java.util.Random;

import android.util.Log;

public class IdCreator {

	public static Long createId() {
		long range = Long.MAX_VALUE;
		Random r = new Random();
		long number = (long)(r.nextDouble()*range);
		Log.i("IdCreator", "createId : "+number);
		return number;
	}
}
