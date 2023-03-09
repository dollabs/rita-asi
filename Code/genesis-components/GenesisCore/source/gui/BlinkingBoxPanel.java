package gui;

import genesis.Genesis;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;

import connections.Port;

/*
 * Created on Aug 7, 2006
 * @author phw
 */
public class BlinkingBoxPanel extends JPanel {

	Arrow arrow;

	private static int maximumItemsPerRow = 8;

	public BlinkingBoxPanel() {
		setLayout(new MyLayout());
		// add(getArrow());
	}

	public Dimension getPreferredSize() {
		return new Dimension(1000, 500);
	}

	public Dimension getMinimumSize() {
		return new Dimension(100, 50);
	}

	public Component add(Component c) {
		super.add(c);
		getArrow().setTaps(getBoxes().size());
		return c;
	}

	class MyLayout implements LayoutManager {
		ArrayList components = new ArrayList();

		public void addLayoutComponent(String ignore, Component component) {
			components.add(component);
		}

		public void removeLayoutComponent(Component object) {
			components.remove(object);
		}

		public Dimension preferredLayoutSize(Container arg0) {
			return new Dimension(800, 800);
		}

		public Dimension minimumLayoutSize(Container arg0) {
			return null;
		}

		public void layoutContainer(Container arg0) {
			ArrayList boxes = getBoxes();
			int width = getWidth();
			int height = getHeight();
			if (width == 0 || height == 0) {
				return;
			}
			int items = boxes.size();
			int spacers = items + 1;
			if (items == 0) {
				return;
			}

			int rows = items / maximumItemsPerRow;
			int leftOvers = items % maximumItemsPerRow;

			int itemsInLastRow = maximumItemsPerRow;

			if (leftOvers > 0) {
				++rows;
				itemsInLastRow = leftOvers;
			}

			int boxHeight = 9 * height / 10;

			boxHeight /= rows;

			int yOffset = 0;

			// System.out.println("Rows: " + rows);

			// Lay out each row separately
			for (int row = 0; row < rows; ++row) {
				// System.out.println("row " + row);
				int totalPreferredBoxWidth = 0;
				for (int i = 0; i < ((row != rows - 1) ? maximumItemsPerRow : itemsInLastRow); ++i) {
					JComponent c = (JComponent) (boxes.get(i + row * maximumItemsPerRow));
					totalPreferredBoxWidth += c.getPreferredSize().width;
				}
				// int actualTotalBoxWidth = 9 * width / 10;
				int actualTotalBoxWidth = width;
				// int spacerWidth = width / 10 / spacers;
				int spacerWidth = 0;
				int xOffset = spacerWidth;
				for (int i = 0; i < ((row != rows - 1) ? maximumItemsPerRow : itemsInLastRow); ++i) {
					// System.out.println("Laying out " + (i + row *
					// itemsPerRow));
					Component c = (Component) (boxes.get(i + row * maximumItemsPerRow));
					int actualWidth = c.getPreferredSize().width * actualTotalBoxWidth / totalPreferredBoxWidth;
					c.setBounds(xOffset, yOffset + row * boxHeight, actualWidth, boxHeight);
					xOffset += actualWidth + spacerWidth;
				}
				// int arrowOffset = yOffset + boxHeight;
				// getArrow().setBounds(0, arrowOffset, width, height -
				// arrowOffset);
			}
		}

	}

	private ArrayList getBoxes() {
		ArrayList list = new ArrayList();
		Component[] components = getComponents();
		for (int i = 0; i < components.length; ++i) {
			if (components[i] instanceof BlinkingBox) {
				list.add(components[i]);
			}
			else if (components[i] instanceof JPanel) {
				list.add(components[i]);
			}
		}
		return list;
	}

	// public static void main(String[] args) {
	// BlinkingBoxPanel panel = new BlinkingBoxPanel();
	// WiredBlinkingBox box1 = new WiredBlinkingBox();
	// WiredBlinkingBox box2 = new WiredBlinkingBox();
	// WiredBlinkingBox box3 = new WiredBlinkingBox();
	// int newWidth = box1.getPreferredSize().width /= 2;
	// int newHeight = box1.getPreferredSize().height;
	// box1.setPreferredSize(new Dimension(newWidth, newHeight));
	// panel.add(box1);
	// panel.add(box2);
	// panel.add(box3);
	// box1.setTitle("Sample title");
	// box2.setTitle("Another sample title");
	// box3.setTitle("Yet Another sample title");
	// box1.setBackground(Color.CYAN);
	// box2.setBackground(Color.PINK);
	// box3.setBackground(Color.MAGENTA);
	// JFrame frame = new JFrame("Testing");
	// frame.getContentPane().add(panel, BorderLayout.CENTER);
	// frame.setBounds(100, 100, 800, 400);
	// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	// frame.setVisible(true);
	// try {
	// Thread.sleep(2000);
	// box1.setInput(new Object(), new Port("Sample"));
	// Thread.sleep(1000);
	// box2.setInput(new Object(), new Port("Sample"));
	// Thread.sleep(1000);
	// box1.setInput(new Object(), new Port("Sample"));
	// }
	// catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }

	public Arrow getArrow() {
		if (arrow == null) {
			arrow = new Arrow();
		}
		return arrow;
	}

	public void redo() {
		for (Component c : getComponents()) {
			((JComponent)c).revalidate();
		}
	}

}
