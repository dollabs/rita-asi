package utils;

import java.util.HashMap;

import javax.swing.JCheckBoxMenuItem;

/*
 * Created on Mar 27, 2010
 * @author phw
 */

public class OldTimer {

	private static HashMap<String, Long> timeMap = new HashMap<String, Long>();

	private static String seconds(long millis) {
		return String.valueOf(millis / 1000);
	}

	private static String hundredths(long millis) {
		int hundredths = (int) (millis / 10);
		int sec = (int) (millis / 1000);
		hundredths = hundredths - 100 * sec;
		if (hundredths < 10) {
			return sec + ".0" + hundredths;
		}
		return sec + "." + hundredths;
	}

	private static String tenths(long millis) {
		int tenths = (int) (millis / 100);
		int sec = tenths / 10;
		tenths = tenths - 10 * sec;
		return sec + "." + tenths;
	}

	private static void laptime(boolean trigger, String message, long startTime) {
		laptime(trigger, message, message, startTime);
	}

	private static void laptime(JCheckBoxMenuItem trigger, String message, long startTime) {
		laptime(trigger.isSelected(), message, message, startTime);
	}

	private static void laptime(JCheckBoxMenuItem trigger, String key, String message, long startTime) {
		laptime(trigger.isSelected(), key, message, startTime);
	}

	private static String laptime(String key, long startTime) {
		return calulateTime(key, startTime, true);
	}

	private static void laptime(boolean trigger, String key, String message, long startTime) {
		if (trigger) {
			String result = calulateTime(key, startTime, true);
		}
	}

	private static void laptime(boolean trigger, String key, String message, long startTime, long threshold) {
		if (System.currentTimeMillis() > startTime + threshold) {
			OldTimer.laptime(trigger, key, message, startTime);
		}
	}

	private static void time(boolean trigger, String message, long startTime) {
		time(trigger, message, message, startTime);
	}

	private static void time(JCheckBoxMenuItem trigger, String message, long startTime) {
		time(trigger.isSelected(), message, message, startTime);
	}

	private static void time(JCheckBoxMenuItem trigger, String key, String message, long startTime) {
		time(trigger.isSelected(), key, message, startTime);
	}

	private static String time(String key, long startTime) {
		return calulateTime(key, startTime, false);
	}

	private static void time(boolean trigger, String key, String message, long startTime, long threshold) {
		if (System.currentTimeMillis() > startTime + threshold) {
			OldTimer.time(trigger, key, message, startTime);
		}
	}

	private static void time(boolean trigger, String key, String message, long startTime) {
		if (trigger) {
			String result = calulateTime(key, startTime, false);
			// Mark.say(trigger, message, result);
		}
	}

	private static String calulateTime(String key, long startTime, boolean showCumulative) {
		Long cumulative = timeMap.get(key);
		if (cumulative == null) {
			cumulative = new Long(0);
		}
		long nowTime = System.currentTimeMillis();
		long delta = nowTime - startTime;
		cumulative += delta;
		timeMap.put(key, cumulative);
		// Mark.say("Key", key, delta / 10, timeMap.get(key) / 1000);
		String result;
		if (showCumulative) {
			result = hundredths(delta) + " sec / " + tenths(cumulative) + " sec";
		}
		else {
			result = tenths(delta) + " sec";
		}
		return result;
	}
}
