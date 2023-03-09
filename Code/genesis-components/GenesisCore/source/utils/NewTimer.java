package utils;

import java.text.DecimalFormat;
import java.util.HashMap;

import gui.OnOffLabel;
import zhutianYang.AcquireRecipes.RecipeGenerator;

/*
 * Created on Jun 14, 2015 Extended with button 24 March 2016
 * @author phw
 */

public class NewTimer {
	
	private long threshold = 500;

	private long startTime;

	private long lapTime;

	private long totalTime;

	private long count;

	private String name;

	private OnOffLabel onOffLabel;

	private DecimalFormat format = new DecimalFormat("#,###,###,##0.000");

	private static HashMap<String, NewTimer> newTimers = new HashMap<>();

	public static final NewTimer startDirectTimer = NewTimer.getTimer("Parser ", 1000);

	public static final NewTimer startServerTimer = NewTimer.getTimer("Parser via server");

	public static final NewTimer startBetaTimer = NewTimer.getTimer("Parser beta");

	public static final NewTimer startFailureTimer = NewTimer.getTimer("Parser failure");

	public static final NewTimer generatorTimer = NewTimer.getTimer("Generator ");

	public static final NewTimer generatorBetaTimer = NewTimer.getTimer("Generator Beta");

	public static final NewTimer generatorServerTimer = NewTimer.getTimer("Generator via server");

	public static final NewTimer bundleGeneratorTimer = NewTimer.getTimer("Wordnet server");

	public static final NewTimer translationTimer = NewTimer.getTimer("Translator ");

	public static final NewTimer conceptProcessingTimer = NewTimer.getTimer("Concept expert", 1000);

	public static final NewTimer ruleProcessingTimer = NewTimer.getTimer("Common sense expert", 1000);
	
	public static final NewTimer conceptNetTimer = NewTimer.getTimer("ConceptNet");

	public static final NewTimer zTimer = NewTimer.getTimer("zTimer");

	// No lights

	public static final NewTimer storyTimer = NewTimer.getTimer("Story timer");

	public static final NewTimer statisticsBarTimer = NewTimer.getTimer("Statistics bar timer");

	public static final NewTimer totalProcessingTimer = NewTimer.getTimer("Total processing timer");

	public static final NewTimer connectionTimer = NewTimer.getTimer("Connection bug timer", 10000);

	public static final NewTimer matcherTimer = NewTimer.getTimer("Matcher timer");

	private NewTimer(String name) {
		this.name = name;
		initialize();
	}

	private NewTimer(String name, long threshold) {
		this(name);
		this.threshold = threshold;
	}

	private static NewTimer getTimer(String name) {
		NewTimer newTimer = newTimers.get(name);
		if (newTimer == null) {
			newTimer = new NewTimer(name);
			newTimers.put(name, newTimer);
		}
		newTimer.startTime = System.currentTimeMillis();
		newTimer.lapTime = newTimer.startTime;
		return newTimer;
	}
	
	private static NewTimer getTimer(String name, long threshold) {
		NewTimer newTimer = getTimer(name);
		newTimer.threshold = threshold;
		return newTimer;
	}

	public void report(boolean debug, String... message) {
		if (debug) {
			long currentTime = System.currentTimeMillis();
			long delta = currentTime - startTime;
			bump(currentTime);
			if (delta > threshold) {
				String accumulator = "";
				if (message.length > 0) {
					for (String s : message) {
						accumulator += s + " ";
					}
				}
				if (RecipeGenerator.VERBOSE) Mark.say("\n>>> ", name, composeTime(delta), accumulator);
			}
		}
		// Modify button only if button created
		turnOff();
	}

	public void lapTime(boolean debug, String... message) {
		if (debug) {
			long currentTime = System.currentTimeMillis();
			long delta = currentTime - lapTime;
			totalTime += currentTime - lapTime;
			lapTime = currentTime;
			++count;
			if (true) {
				String accumulator = "";
				if (message.length > 0) {
					for (String s : message) {
						accumulator += s + " ";
					}
				}
				Mark.say("\n>>> ", name, composeTime(delta), accumulator);
			}
		}
	}



	public void test(boolean debug, String... message) {
		if (debug) {
			long currentTime = System.currentTimeMillis();
			long delta = currentTime - startTime;
			bump(currentTime);
			if (delta > threshold) {
				String accumulator = "";
				if (message.length > 0) {
					for (String s : message) {
						accumulator += s + " ";
					}
				}
				Mark.say("\n>>> ", name, "triggered with", composeTime(delta), accumulator);

			}
		}
	}

	private String composeTime(long milli) {
		double sec = milli / 1000.0;
		return format.format(sec) + " sec";
	}

	public void reset() {
		lapTime = startTime = System.currentTimeMillis();
		// Modify button only if button created
		turnOn();
	}

	public void initialize() {
		lapTime = startTime = System.currentTimeMillis();
		totalTime = 0;
		count = 0;
	}

	private void bump(long currentTime) {
		totalTime += currentTime - lapTime;
		lapTime = currentTime;
		++count;
	}

	public String time() {
		long delta = System.currentTimeMillis() - startTime;
		return delta / 1000 + " sec.";
	}

	public long millis() {
		return System.currentTimeMillis() - startTime;
	}

	public OnOffLabel getOnOffLabel() {
		if (onOffLabel == null) {
			onOffLabel = new OnOffLabel(name);
		}
		return onOffLabel;
	}

	public void summarize() {
		if (count > 0) {
			Mark.say("\n>>> ", name);
			Mark.say("Intervals ", count);
			Mark.say("Total time", composeTime(totalTime));
			Mark.say("Average   ", composeTime(totalTime / count));
		}
	}

	public void turnOn() {
		if (onOffLabel != null) {
			onOffLabel.turnOn();
		}
	}

	public void turnOff() {
		if (onOffLabel != null) {
			onOffLabel.turnOff();
		}
	}

}
