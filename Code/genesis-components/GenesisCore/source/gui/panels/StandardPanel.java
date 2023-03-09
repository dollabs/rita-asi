package gui.panels;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import utils.Mark;
import connections.WiredBox;

/*
 * Created on Aug 14, 2011
 * @author PHW
 */

public class StandardPanel extends JPanel implements WiredBox {

	private JPanel buttonPanel;

	public static int buttonFillerWidth = 5;

	public static int buttonPanelHeight = 20;

	private JButton percent0;

	private JButton percent25;

	private JButton percent50;

	private JButton percent75;

	private JButton percent100;

	private JButton percentEqual;

	private JButton removePanel;

	private JButton addPanel;

	private MyButtonListener myButtonListener;

	private int heightPercent = 100;

	public StandardPanel() {
		setBackground(Color.YELLOW);
		setLayout(new BorderLayout());
		add(getButtonPanel(), BorderLayout.NORTH);
	}

	public JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setBackground(Color.gray);
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
			Dimension filler = new Dimension(buttonFillerWidth, buttonPanelHeight);
			buttonPanel.add(new Box.Filler(filler, filler, filler));
			buttonPanel.add(new Box.Filler(filler, filler, filler));
			buttonPanel.add(getPercent0());
			buttonPanel.add(new Box.Filler(filler, filler, filler));
			buttonPanel.add(getPercent25());
			buttonPanel.add(new Box.Filler(filler, filler, filler));
			buttonPanel.add(getPercent50());
			buttonPanel.add(new Box.Filler(filler, filler, filler));
			buttonPanel.add(getPercent75());
			buttonPanel.add(new Box.Filler(filler, filler, filler));
			buttonPanel.add(getPercent100());

			buttonPanel.add(new Box.Filler(filler, filler, filler));
			buttonPanel.add(new Box.Filler(filler, filler, filler));
			buttonPanel.add(getPercentEqual());

			buttonPanel.add(new Box.Filler(filler, filler, filler));
			buttonPanel.add(new Box.Filler(filler, filler, filler));
			buttonPanel.add(new Box.Filler(filler, filler, filler));
			buttonPanel.add(getAddPanel());
			buttonPanel.add(getRemovePanel());
		}
		return buttonPanel;
	}

	class LocalButton extends JButton {
		private Dimension transformHeight(int height, Dimension input) {
			return new Dimension(input.width, height);
		}

		public LocalButton(String label) {
			super(label);
			this.setMinimumSize(transformHeight(buttonPanelHeight, getMinimumSize()));
			this.setPreferredSize(transformHeight(buttonPanelHeight, getPreferredSize()));
			this.setMaximumSize(transformHeight(buttonPanelHeight, getMaximumSize()));
		}
	}

	public JButton getAddPanel() {
		if (addPanel == null) {
			addPanel = new LocalButton("+");
			addPanel.addActionListener(getListener());
		}
		return addPanel;
	}

	public JButton getRemovePanel() {
		if (removePanel == null) {
			removePanel = new LocalButton("x");
			removePanel.addActionListener(getListener());
		}
		return removePanel;
	}

	public JButton getPercentEqual() {
		if (percentEqual == null) {
			percentEqual = new LocalButton("=");
			percentEqual.addActionListener(getListener());
		}
		return percentEqual;
	}

	public JButton getPercent0() {
		if (percent0 == null) {
			percent0 = new LocalButton("0%");
			percent0.addActionListener(getListener());
		}
		return percent0;
	}

	public JButton getPercent25() {
		if (percent25 == null) {
			percent25 = new LocalButton("25%");
			percent25.addActionListener(getListener());
		}
		return percent25;
	}

	public JButton getPercent50() {
		if (percent50 == null) {
			percent50 = new LocalButton("50%");
			percent50.addActionListener(getListener());
		}
		return percent50;
	}

	public JButton getPercent75() {
		if (percent75 == null) {
			percent75 = new LocalButton("75%");
			percent75.addActionListener(getListener());
		}
		return percent75;
	}

	public JButton getPercent100() {
		if (percent100 == null) {
			percent100 = new LocalButton("100%");
			percent100.addActionListener(getListener());
		}
		return percent100;
	}

	private MyButtonListener getListener() {
		if (myButtonListener == null) {
			myButtonListener = new MyButtonListener();
		}
		return myButtonListener;
	}

	class MyButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == getPercent0()) {
				setHeightPercentAndFire(0);
			}
			else if (e.getSource() == getPercent25()) {
				setHeightPercentAndFire(25);
			}
			else if (e.getSource() == getPercent50()) {
				setHeightPercentAndFire(50);
			}
			else if (e.getSource() == getPercent75()) {
				setHeightPercentAndFire(75);
			}
			else if (e.getSource() == getPercent100()) {
				setHeightPercentAndFire(100);
			}
			else if (e.getSource() == getPercentEqual()) {
				firePropertyChange("equal", -1, 0);
			}
			else if (e.getSource() == getRemovePanel()) {
				firePropertyChange("remove", -1, 0);
			}
			else if (e.getSource() == getAddPanel()) {
				firePropertyChange("add", -1, 0);
			}
		}
	}

	public int getHeightPercent() {
		return heightPercent;
	}

	public void setHeightPercent(int heightPercent) {
		this.heightPercent = heightPercent;
	}

	public void setHeightPercentAndFire(int newValue) {
		// Always not change on other end; may, for example, exert same new
		// value but different panel.
		int oldValue = -1;
		setHeightPercent(newValue);
		firePropertyChange("height", oldValue, newValue);
	}

	public static void main(String[] args) {
		StandardPanel panel = new StandardPanel();
		JFrame frame = new JFrame();
		frame.setContentPane(panel);
		frame.setBounds(0, 0, 500, 400);
		frame.setVisible(true);
	}

}
