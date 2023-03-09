package gui;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;

import gui.panels.BorderedParallelJPanel;
import utils.Mark;

/*
 * Created on Jul 6, 2012
 * @author phw
 */

public class JMatrixPanel extends JPanel {

	public JMatrixPanel() {
		setLayout(new MyLayoutManager());
	}

	private ArrayList<Triple> components = new ArrayList<Triple>();

	private int cWidth = 0;

	private int cHeight = 0;

	public void add(Component c, int x, int y, int w, int h) {
		super.add(c);
		cWidth = Math.max(cWidth, x + w);
		cHeight = Math.max(cHeight, y + h);
		components.add(new Triple(c, x, y, w, h));
		setBackground(Color.white);
		setOpaque(true);
	}

	protected class MyLayoutManager implements LayoutManager {
		
		public void layoutContainer(Container parent) {
			synchronized (parent.getTreeLock()) {
				Insets insets = parent.getInsets();
				int ncomponents = getComponentCount();
				if (ncomponents == 0) {
					return;
				}

				double wMultiplier = (double) getWidth() / cWidth;

				double hMultiplier = (double) getHeight() / cHeight;

				for (Triple t : components) {
					t.c.setBounds((int) (t.x * wMultiplier), (int) (t.y * hMultiplier), (int) (t.w * wMultiplier), (int) (t.h * hMultiplier));
				}
			}
		}

		@Override
        public void addLayoutComponent(String name, Component comp) {
	        
        }

		@Override
        public void removeLayoutComponent(Component comp) {
	        
        }

		@Override
        public Dimension preferredLayoutSize(Container parent) {
	        return null;
        }

		@Override
        public Dimension minimumLayoutSize(Container parent) {
	        return null;
        }
	}

	class Triple {
		Component c;

		public int x, y, w, h;

		public Triple(Component c, int x, int y, int w, int h) {
			this.c = c;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}
	}
	
	public static void main (String [] ignore) {
		JFrame frame = new JFrame();
		JMatrixPanel panel = new JMatrixPanel();
		frame.getContentPane().add(panel);
		BorderedParallelJPanel bpjp1 = new BorderedParallelJPanel("Hello");
		BorderedParallelJPanel bpjp2 = new BorderedParallelJPanel("World");
		BorderedParallelJPanel bpjp3 = new BorderedParallelJPanel("Cruel");
		bpjp1.addLeft("Hello world");
		Mark.say("Insets", bpjp1.getInsets());
		panel.add(bpjp1, 0, 0, 10, 10);
		panel.add(bpjp2, 10, 0, 5, 5);
		panel.add(bpjp3, 10, 5, 5, 5);
		frame.setBounds(0, 0, 600, 400);
		Mark.say("foo");
		frame.setVisible(true);
	}

}
