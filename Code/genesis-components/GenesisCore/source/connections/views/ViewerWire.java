package connections.views;

import java.awt.Color;

import connections.*;

/*
 * Created on Feb 28, 2009
 * @author phw
 */

public class ViewerWire {

	public final static Color RED_THREAD = Color.red;

	private ViewerBox source;

	private ViewerBox target;

	private Port sourcePort;

	private String sourcePortName;

	private String targetPortName;

	private Color color;

	private Color permanentColor;

	boolean visible = true;

	boolean dashed;

	int destinationIndex = 1;

	int destinationCount = 1;

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getPermanentColor() {
		return permanentColor;
	}

	public void setPermanentColor(Color permanentColor) {
		this.permanentColor = permanentColor;
	}

	public ViewerWire(ViewerBox source, ViewerBox target) {
		super();
		this.source = source;
		this.target = target;
	}

	public ViewerWire(ViewerBox source, ViewerBox target, int x, int count) {
		this(source, target);
		destinationIndex = x;
		destinationCount = count;
	}

	// public ViewerWire(ViewerBox source, Port port, ViewerBox target) {
	// this(source, target);
	// sourcePort = port;
	// }

	public ViewerWire(Port sourcePort, ViewerBox source, ViewerBox target) {
		this(source, target);
		this.sourcePort = sourcePort;
		sourcePortName = sourcePort.getSourceName();
	}

	public ViewerWire(Port sourcePort, ViewerBox source, String targetPortName, ViewerBox target) {
		this(sourcePort, source, target);
		this.targetPortName = targetPortName;
	}

	public ViewerBox getSource() {
		return source;
	}

	public ViewerBox getTarget() {
		return target;
	}

	public String toString() {
		return "<Wire connecting " + getSource().getText() + " to " + getTarget().getText() + ">";
	}

	public void setVisible(boolean b) {
		visible = b;
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isDotted() {
		if (sourcePort != null) {
			if (sourcePort.getSourceName() == Port.OUTPUT || sourcePort.getSourceName() == Port.UP) {
				if (source.getSwitchState() == ViewerBox.OFF_SWITCH) {
					return true;
				}
			}
			else if (sourcePort.getSourceName() == Port.DOWN) {
				if (source.getSwitchState() == ViewerBox.ON_SWITCH) {
					return true;
				}
			}
		}
		return false;
	}

	public Port getSourcePort() {
		return sourcePort;
	}

	public String getSourcePortName() {
		return sourcePortName;
	}

	public String getTargetPortName() {
		return targetPortName;
	}

	public int getDestinationIndex() {
		return destinationIndex;
	}

	public int getDestinationCount() {
		return destinationCount;
	}

	public void setDashed(boolean dashed) {
		this.dashed = dashed;
	}

	public boolean isDashed() {
		return dashed;
	}

}
