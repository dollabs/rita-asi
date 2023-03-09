package gui.panels;

import java.awt.*;

import javax.swing.*;

import utils.Mark;

/*
 * Created on Jan 10, 2006 @author Patrick
 */

public class FramingPanel extends JPanel {

	FramingLayout layout = new FramingLayout(90);

	Component component;

	public FramingPanel() {
		super();
		setBackground(Color.WHITE);
		setLayout(layout);
	}

	public FramingPanel(int percent) {
		this();
		setFillPercent(percent);
	}

	public FramingPanel(Component center) {
		this();
		add(center);
	}

	public FramingPanel(Component center, int percent) {
		this(center);
		setFillPercent(percent);
	}

	public Component add(Component center) {
		if (component != null) {component.setBounds(0, 0, 0, 0);}
		removeAll();
		super.add(center);
		this.component = center;
		return center;
	}

	public void setFillPercent(int i) {
		layout.setFillPercent(i);
	}

	private class FramingLayout implements LayoutManager {

		protected int fillPercent = 90;

		public FramingLayout() {
			super();
		}

		public FramingLayout(int i) {
			this();
			fillPercent = i;
		}

		public void setFillPercent(int i) {
			fillPercent = i;

		}

		public void layoutContainer(Container parent) {
			synchronized (parent.getTreeLock()) {
				if (parent.getComponents().length == 0) {
					return;
				}
				int w = parent.getWidth();
				int h = parent.getHeight();

				Dimension d = component.getMinimumSize();
				d = component.getMaximumSize();
				d = component.getPreferredSize();
				double centerW = d.getWidth();
				double centerH = d.getHeight();

				// Height governs
				int hSize = h * fillPercent / 100;
				int wSize = (int) ((centerW / centerH) * hSize);

				if (((centerW * h) / (centerH * w)) > 1.0) {
					// Width governs
					wSize = w * fillPercent / 100;
					hSize = (int) ((centerH / centerW) * wSize);
				}
				
				int wOffset = (w - wSize) / 2;
				int hOffset = (h - hSize) / 2;
				if (component != null) {
					component.setBounds(wOffset, hOffset, wSize, hSize);
				}
			}
		}

		public void removeLayoutComponent(Component component) {

		}

		public void addLayoutComponent(String arg0, Component component) {

		}

		public Dimension minimumLayoutSize(Container parent) {
			if (parent.getComponents().length == 0) {
				return null;
			}
			return parent.getComponent(0).getMinimumSize();
		}

		public Dimension preferredLayoutSize(Container parent) {
			if (parent.getComponents().length == 0) {
				return null;
			}
			return parent.getComponent(0).getPreferredSize();
		}
	}

	public static void main(String[] ignore) {
		JFrame frame = new JFrame();
		FramingPanel bf = new FramingPanel();
		bf.setBackground(Color.WHITE);
		bf.setFillPercent(90);
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(352, 288));
		panel.setBackground(Color.RED);
		bf.add(panel);
		// frame.getContentPane().setLayout(new GridLayout(1, 1));
		frame.getContentPane().add(bf);

		frame.setBounds(0, 0, 500, 500);
		frame.show();
	}

}
