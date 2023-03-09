package utils.logging;

public class Level {
	public final static Level OFF = new Level("off", 7);

	public final static Level SEVERE = new Level("severe", 6);

	public final static Level WARNING = new Level("warning", 5);

	public final static Level INFO = new Level("info", 4);

	public final static Level CONFIG = new Level("config", 3);

	public final static Level FINE = new Level("fine", 2);

	public final static Level FINER = new Level("finer", 1);

	public final static Level FINEST = new Level("finest", 0);

	public final static Level All = new Level("all", -1);

	final int myLevel;

	final String name;

	protected Level(String name, int level) {
		myLevel = level;
		this.name = name;
	}

	public String toString() {
		return name;
	}

	public int intValue() {
		return myLevel;
	}

	public String getName() {
		return name;
	}
}