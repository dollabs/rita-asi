package gui.panels;

import java.awt.*;
import java.beans.*;

import javax.swing.*;

import utils.Mark;

import connections.WiredBox;

/*
 * Created on Aug 14, 2011
 * @author PHW
 */

public class MasterPanel extends JPanel implements PropertyChangeListener {

    private static final long serialVersionUID = 1L;

	JPanel tickledComponent;

	int tickledSize;

	public MasterPanel() {
		setLayout(new MyLayoutManager());
		setBackground(Color.CYAN);
	}
	
	public void addStandardPanel (StandardPanel p) {
		super.add(p);
		p.addPropertyChangeListener(this);
		setSizesEqual();
		adjustToHundredPercent();
	}

	class MyLayoutManager implements LayoutManager {

		@Override
		public void addLayoutComponent(String arg0, Component arg1) {

		}

		@Override
		public void layoutContainer(Container arg0) {
			int count = getComponents().length;
			int accumulated = 0;
			int reservedForButtonBars = StandardPanel.buttonPanelHeight * count;
			int adjustedHeight = getHeight() - reservedForButtonBars;
			for (int i = 0; i < count; ++i) {
				StandardPanel p = (StandardPanel) (getComponents()[i]);
				int thisHeight = StandardPanel.buttonPanelHeight + p.getHeightPercent() * adjustedHeight / 100;
				if (i == count - 1) {
					p.setBounds(0, accumulated, getWidth(), getHeight() - accumulated);
				}
				else {
					p.setBounds(0, accumulated, getWidth(), thisHeight);
				}
				accumulated += thisHeight;
			}
		}

		@Override
		public Dimension minimumLayoutSize(Container arg0) {
			return null;
		}

		@Override
		public Dimension preferredLayoutSize(Container arg0) {
			return null;
		}

		@Override
		public void removeLayoutComponent(Component arg0) {

		}

	}

	public void propertyChange(PropertyChangeEvent e) {
		if ("add".equals(e.getPropertyName())) {
			Mark.say("Hello world");
			this.addStandardPanel(new StandardPanel());
			// this.remove((Component)(e.getSource()));
			setSizesEqual();
			adjustToHundredPercent();
			return;
		}
		else if ("remove".equals(e.getPropertyName())) {
			this.remove((Component)(e.getSource()));
			setSizesEqual();
			adjustToHundredPercent();
			return;
		}
		else if ("equal".equals(e.getPropertyName())) {
			setSizesEqual();
			adjustToHundredPercent();
			return;
		}
		else if (!"height".equals(e.getPropertyName())) {
			return;
		}
		tickledComponent = (StandardPanel) (e.getSource());
		tickledSize = (Integer) (e.getNewValue());
		int unadjustedSum = 0;
		for (Component c : getComponents()) {
			StandardPanel p = (StandardPanel) c;
			if (p == tickledComponent) {
			}
			else {
				unadjustedSum += p.getHeightPercent();
			}
		}
		if (unadjustedSum == 0) {
			for (Component c : getComponents()) {
				StandardPanel p = (StandardPanel) c;
				if (p == tickledComponent) {
				}
				else {
					p.setHeightPercent(1);
					unadjustedSum += 1; 
				}
			}
		}
		int remainder = 100 - tickledSize;
		for (Component c : getComponents()) {
			StandardPanel p = (StandardPanel) c;
			if (p == tickledComponent) {
			}
			else {
				int newPercent = p.getHeightPercent() * remainder / unadjustedSum;
				p.setHeightPercent(newPercent);
			}
		}
		adjustToHundredPercent();
 }

	private void adjustToHundredPercent() {
	    int total = 0;
	    for (Component c : getComponents()) {
	    	StandardPanel p = (StandardPanel)c;
	    	total += p.getHeightPercent();
	    }
	    int delta = 100 - total;
	    if (delta != 0) {
	    	StandardPanel tallestPanel = null;
	    	int tallestHeight = -1;
	    	for (Component c : getComponents()) {
	    		StandardPanel p = (StandardPanel) c;
	    		if (p.getHeightPercent() > tallestHeight) {
	    			tallestPanel = p;
	    			tallestHeight = p.getHeightPercent();
	    		}
	    	}
	    	tallestPanel.setHeightPercent(tallestPanel.getHeightPercent() + delta);
	    }
	    revalidate();
    }

	private void setSizesEqual() {
	    int equalPercent = 100 / getComponents().length;
	    for (int i = 0; i < getComponents().length; ++i) {
	    	StandardPanel p = (StandardPanel) (getComponents()[i]);
	    	p.setHeightPercent(equalPercent);
	    }
    }

	private Dimension transformHeight(int height, Dimension input) {
		return new Dimension(input.width, height);
	}

	public static void main(String[] args) {
		MasterPanel panel = new MasterPanel();

		JFrame frame = new JFrame();
		frame.setContentPane(panel);
		frame.setBounds(0, 0, 500, 400);
		frame.setVisible(true);
	}
}
