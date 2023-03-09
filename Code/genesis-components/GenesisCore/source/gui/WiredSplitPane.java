package gui;

import java.awt.*;

import javax.swing.*;

import utils.Mark;
import connections.*;

/*
 * Created on Nov 10, 2010
 * @author phw
 */

public class WiredSplitPane extends JSplitPane implements WiredBox {

	public static final Object SHOW_LEFT = "Show first perspective only";

	public static final Object SHOW_RIGHT = "Show second perspective only";

	public static final Object SHOW_BOTH = "Show both perspectives";

	private JComponent left;

	private JComponent right;

	public WiredSplitPane(JComponent left, JComponent right) {
		this.left = left;
		this.right = right;
		setPreferredWidth(left, 1000);
		setPreferredWidth(right, 1000);
		setMinimumWidth(left, 0);
		setMinimumWidth(right, 0);
		setMinimumHeight(left, 0);
		setMinimumHeight(right, 0);
		setLeftComponent(left);
		setRightComponent(right);
		setOneTouchExpandable(true);
		leftOnly();
		Connections.getPorts(this).addSignalProcessor("process");
	}
	
	private void reset() {
		validate();
		repaint();
	}

	public void both() {
		setResizeWeight(0.5);
		setDividerLocation(0.5);
		reset();
	}

	public void leftOnly() {
		setResizeWeight(1.0);
		setDividerLocation(1.0);
		reset();
	}

	public void rightOnly() {
		setResizeWeight(0.0);
		setDividerLocation(0.0);
		invalidate();
	}

	public void process(Object object) {
		if (object == SHOW_BOTH) {
			both();
		}
		else if (object == SHOW_LEFT) {
			leftOnly();
		}
		else if (object == SHOW_RIGHT) {
			rightOnly();
		}
	}

	protected void setPreferredWidth(Component c, int w) {
		int h = c.getPreferredSize().height;
		c.setPreferredSize(new Dimension(w, h));
	}

	protected void setPreferredHeight(Component c, int h) {
		int w = c.getPreferredSize().width;
		c.setPreferredSize(new Dimension(w, h));
	}

	protected void setMinimumWidth(Component c, int w) {
		int h = c.getMinimumSize().height;
		c.setMinimumSize(new Dimension(w, h));
	}

	protected void setMinimumHeight(Component c, int h) {
		int w = c.getMinimumSize().width;
		c.setMinimumSize(new Dimension(w, h));
	}

}
