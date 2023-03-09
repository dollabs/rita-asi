package connections.views;

import java.awt.Color;
import java.util.*;

import utils.Colors;
import connections.WiredBox;

/*
 * Created on Feb 25, 2009
 * @author phw
 */

public class ViewerBox extends Observable implements ColoredBox {

	public static final int VIRGIN = 0;

	public static final int ACTUATED = 1;

	public static final int BLEW_OUT = 2;

	public static final String PAINT = "Paint";

	public static final String REDO = "Redo";

	private Set<String> inputPortNames;

	private Set<String> outputPortNames;

	private int state = VIRGIN;

	private boolean selected = false;

	private int width = 110, height = 80;

	// private int deltaX = 30, deltaY = 15;
	
	private int deltaX = 50, deltaY = 25;

	private int x, y;

	// private int showTime = 5000, delayTime = 1000;

	private String text;

	private WiredBox source;

	public static Color defaultColor = Color.WHITE;

	public static Color activeColor = Color.GREEN;

	public static Color rootColor = Color.PINK;

	public static Color usedColor = Colors.USED_COLOR;

	private Color color = defaultColor;

	private Color permanentColor = defaultColor;

	private boolean visible = true;

	public static int NEITHER = -1, OFF_SWITCH = 0, ON_SWITCH = 1;

	private int switchState = NEITHER;

	private boolean toggleSwitch = false;

	private boolean negative = false;

	private boolean dotted = false;

	public ViewerBox(int row, int column, String label, WiredBox source) {
		super();
		this.x = column * (width + deltaX);
		this.y = row * (height + deltaY);
		this.text = label;
		this.source = source;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String getText() {
		return text;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getDeltaX() {
		return deltaX;
	}

	public int getDeltaY() {
		return deltaY;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		changed(PAINT);
	}

	public void setPermanentColor(Color color) {
		this.permanentColor = color;
		setColor(color);
	}

	public void changed(String x) {
		this.setChanged();
		this.notifyObservers(x);
	}

	public void resetColor() {
		setColor(permanentColor);
	}

	public synchronized void setTemporaryColor() {
		// Only spins up one tread.  Previous version choked system with one thread per box
		// ColorTracker.getTracker().process(new ColorTrackerPackage(activeColor, getColor(), this));
	}
	
	public Class<? extends WiredBox> getSourceClass() {
		return source.getClass();
	}

	public WiredBox getSource() {
		return source;
	}

	public void setSelected(boolean b) {
		selected = b;
		if (!selected && state == BLEW_OUT) {
			state = ACTUATED;
		}
	}

	public boolean isSelected() {
		return selected || state == BLEW_OUT;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setVisible(boolean b) {
		visible = b;
	}

	public boolean isVisible() {
		return visible;
	}

	public int getSwitchState() {
		// Mark.a("State of " + getText() + " returned: " + this.switchState);
		return switchState;
	}

	public void setSwitchState(int switchState) {
		this.switchState = switchState;
		// Mark.a("State of " + getText() + " set to: " +
		// this.getSwitchState());
	}

	public boolean isToggleSwitch() {
		return toggleSwitch;
	}

	public void setToggleSwitch(boolean toggleSwitch) {
		this.toggleSwitch = toggleSwitch;
	}

	public boolean isNegative() {
		return negative;
	}

	public void setNegative(boolean negative) {
		this.negative = negative;
	}

	public boolean isDotted() {
		return dotted;
	}

	public void setDotted(boolean dotted) {
		this.dotted = dotted;
	}

	public Set<String> getOutputPortNames() {
		if (outputPortNames == null) {
			outputPortNames = new TreeSet<String>();
		}
		return outputPortNames;
	}

	public void setOutputPortNames(Set<String> outputPortNames) {
		this.outputPortNames = outputPortNames;
	}

	public Set<String> getInputPortNames() {
		if (inputPortNames == null) {
			inputPortNames = new TreeSet<String>();
		}
		return inputPortNames;
	}

	public void setInputPortNames(Set<String> inputPortNames) {
		this.inputPortNames = inputPortNames;
	}
}
