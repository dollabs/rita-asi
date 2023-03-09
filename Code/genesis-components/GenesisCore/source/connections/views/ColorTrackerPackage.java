package connections.views;

import java.awt.Color;

/*
 * Created on Apr 23, 2010
 * @author phw
 */

public class ColorTrackerPackage {

	private long showTime = 2000;

	private Color permanentColor;

	private Color temporaryColor;

	private long quitTime;

	private ColoredBox coloredBox;

	public long getQuitTime() {
		return quitTime;
	}

	public void setQuitTime(long quitTime) {
		this.quitTime = quitTime;
	}

	public ColoredBox getColoredBox() {
		return coloredBox;
	}

	public Color getPermanentColor() {
		return permanentColor;
	}

	public Color getTemporaryColor() {
		return temporaryColor;
	}

	public ColorTrackerPackage(Color temporaryColor, Color permanentColor, ColoredBox b) {
		this.permanentColor = permanentColor;
		this.temporaryColor = temporaryColor;
		coloredBox = b;
		quitTime = System.currentTimeMillis() + showTime;
	}

}
