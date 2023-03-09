package gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import utils.Mark;

import connections.WiredBox;
import connections.views.*;
import connections.views.ColorTracker;

/*
 * Created on Aug 5, 2006 @author phw
 */
public class BlinkingBox extends JPanel implements WiredBox, ColoredBox {

	private long blinkingTime = 1000;

	private int count = 0;

	private Color colorHandle;

	private JComponent memory;

	private JComponent graphic;

	private Font myFont;

	private boolean blinkSwitch = true;

	// public static JCheckBoxMenuItem memoryItem;
	//
	// public static JCheckBoxMenuItem getMemoryItem() {
	// if (memoryItem == null) {
	// memoryItem = new JCheckBoxMenuItem("Show memories");
	// memoryItem.setSelected(false);
	// }
	// return memoryItem;
	// }

	public boolean isBlinkSwitch() {
		return blinkSwitch;
	}

	public void setBlinkSwitch(boolean blinkSwitch) {
		this.blinkSwitch = blinkSwitch;
	}

	public BlinkingBox() {
		super();
		setLayout(new MyLayoutManager());
		setBackground(Color.WHITE);
		setOpaque(true);
		TitledBorder border = BorderFactory.createTitledBorder("");
		// Bug: border.getFont did not work
		Font newFont = new Font("Serif", Font.PLAIN, 10);
		border.setTitleFont(newFont);
		this.setBorder(border);

		this.setPreferredSize(new Dimension(200, 400));
		Font f = getFont();
		myFont = new Font(f.getName(), Font.BOLD, f.getSize());
	}

	public void blink() {
		incrementCount();
		// Following expensive; spins up thread.
		ColorTracker.getTracker().process(new ColorTrackerPackage(Color.YELLOW, Color.WHITE, this));
	}

	public void setColor(Color color) {
		setBackground(color);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int w = getWidth();
		if (count > 0) {
			g.setFont(myFont);
			FontMetrics fm = g.getFontMetrics();
			String report = Integer.toString(count);
			g.drawString(report, w - fm.stringWidth(report) - 10, fm.getHeight() + 4);
		}

	}

	public static void main(String[] args) {
		BlinkingBox box = new BlinkingBox();
		JFrame frame = new JFrame("Testing");
		frame.getContentPane().add(box, BorderLayout.CENTER);
		frame.setBounds(100, 100, 400, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		JLabel label = new JLabel("Hello World");
		box.add(label);
		box.blink();
		box.blink();
		label.setText("Goodby World");

	}

	public void setTitle(String text) {
		((TitledBorder) (getBorder())).setTitle(text);
	}

	public void incrementCount() {
		++count;
	}

	private Color getColorHandle() {
		if (colorHandle == null) {
			colorHandle = this.getBackground();
		}
		return colorHandle;
	}

	class MyLayoutManager implements LayoutManager {

		public void layoutContainer(Container container) {
			int height = getHeight();
			int width = getWidth();
			Insets insets = getInsets();
			if (height == 0 || width == 0) {
				return;
			}
			height -= insets.top + insets.bottom;
			int yOffset = 0;
			// if (getMemoryItem().isSelected() && memory != null) {
			// memory.setBounds(insets.left, insets.top, width - insets.left -
			// insets.right, height / 2);
			// yOffset = height = height / 2;
			// }
			// else
			if (memory != null) {
				memory.setBounds(0, 0, 0, 0);
			}
			if (graphic != null) {
				graphic.setBounds(insets.left, insets.top + yOffset, width - insets.left - insets.right, height);
			}
		}

		public void removeLayoutComponent(Component component) {
		}

		public void addLayoutComponent(String string, Component arg1) {
		}

		public Dimension minimumLayoutSize(Container parent) {
			return null;
		}

		public Dimension preferredLayoutSize(Container parent) {
			return null;
		}
	}

	public JComponent getGraphic() {
		return graphic;
	}

	public void setGraphic(JComponent graphic) {
		if (this.graphic != null) {
			this.remove(this.memory);
		}
		this.graphic = graphic;
		// this.graphic.setBackground(Color.ORANGE);
		// this.graphic.setOpaque(true);
		add(this.graphic);
	}

	public JComponent getMemory() {
		return memory;
	}

	public void setMemory(JComponent memory) {
		if (this.memory != null) {
			this.remove(this.memory);
		}
		this.memory = memory;
		// this.memory.setBackground(Color.RED);
		// this.memory.setOpaque(true);
		add(this.memory);
	}

}
