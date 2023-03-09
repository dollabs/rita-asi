package memory2.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import memory2.datatypes.Chain;
import memory2.M2;
import connections.Connections;
import connections.WiredBox;

/**
 * Used to visualize a set of Chains. Chains are mutable, so we need to be able
 * to handle that.
 * 
 * @author sglidden
 */
public class RepViewer extends JPanel implements WiredBox {
	private static final long serialVersionUID = 7951486464909702580L;

	private ChainCircle circle = new ChainCircle();

	public RepViewer() {
		super();
		this.setLayout(new BorderLayout());
		this.add(this.circle, BorderLayout.CENTER);
		Connections.getPorts(this).addSignalProcessor("input");
	}

	// receives data to display
	public void input(Object input) {
		// System.err.println("RepViewer received input: "+input);
		if (input instanceof List) {
			this.display((List<Chain>) input);
		}
		else {
			System.err.println("Bad input to RepViewer");
		}
	}

	public void display(final List<Chain> chains) {
//		System.out.println(chains);
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
				circle.setEntries(chains);
//			}
//		});
	}

	// outputs elements that have been moused over
	public void output(Object obj) {
		if (obj instanceof Chain) {
			Connections.getPorts(this).transmit((Chain) obj);
		}
	}

	/**
	 * @author Sam Glidden
	 */
	private class ChainCircle extends JPanel {

		private List<Chain> chains = new ArrayList<Chain>();

		private List<Point> points = new ArrayList<Point>();

		private List<DotLabel> labels = new ArrayList<DotLabel>();

		private int width, height;

		private int radius;

		int dotSize;

		private Point center = new Point(0, 0);

		public ChainCircle() {
			super();
			this.setBackground(Color.WHITE);
			this.setLayout(null);
		}

		/**
		 * Provides the entries for the circle to draw out. The entries are
		 * provided in a map, where each entry maps to a set of neighbor entries
		 * 
		 * @param neighborhood
		 *            Map[E -> Set[E]]
		 */
		public void setEntries(List<Chain> chains) {
			try {
				this.chains = chains;

				// add DotLabels
				this.removeAll();
				this.labels.clear();
				for (int i = 0; i < chains.size(); i++) {
					Chain e = this.chains.get(i);
					DotLabel label = new DotLabel(e);
					label.addMouseListener(label);
					this.labels.add(label);
					this.add(label);
				}
			}
			catch (Exception e) {
				System.out.println("Blew out in setEntries in RepViewer");
			}
		}

		// paints out the component
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			// step 1: refresh local values
			this.width = this.getWidth();
			this.height = this.getHeight();
			this.radius = (Math.min(this.width, this.height) - 20) / 2;
			this.center = new Point(this.width / 2, this.height / 2);
			this.dotSize = Math.min(Math.max(this.radius / 8, 6), 12);

			// List<DotLabel> tempLabels = new ArrayList<DotLabel>(this.labels);
			// List<Chain> tempChains = new ArrayList<Chain>(this.chains);
			// List<Point> tempPoints = new ArrayList<Point>(this.points);

			this.plotEntries();

			if (points.size() != (chains.size())) {
				if (M2.DEBUG) System.out.println("Broken rep invarient 1 in ChainCircle! Expect wonky behavor.");
				// return;
			}
			if (labels.size() != (chains.size())) {
				if (M2.DEBUG) System.out.println("Broken rep invarient 2 in ChainCircle! Expect wonky behavor.");
				// return;
			}

			// step 2: draw the circle
			g.setColor(Color.LIGHT_GRAY);
			g.fillOval((int) this.center.getX() - this.radius - 2, (int) this.center.getY() - this.radius - 2, this.radius * 2 + 4, this.radius * 2 + 4);
			g.setColor(Color.WHITE);
			g.fillOval((int) this.center.getX() - this.radius + 1, (int) this.center.getY() + 1 - this.radius, this.radius * 2 - 2, this.radius * 2 - 2);
			// step 3: connect neighbors
			try {
//				for (int i = 0; i < chains.size(); i++) {
//					Point start = points.get(i);
//					for (Chain target : chains.get(i).getNeighborCopy()) {
//						Point end = points.get(i);
//						g.setColor(Color.BLACK);
//						g.drawLine(start.x, start.y, end.x, end.y);
//					}
//				}
			}
			catch (Exception e) {
				System.out.println("Blew out of step 3 in rep viewer");
			}
			// step 4: position the dots
			try {
				this.drawDots();
			}
			catch (Exception e) {
				System.out.println("Blew out of step 4 in rep viewer");
			}
		}

		// refreshes circle coordinate map
		private void plotEntries() {
			points.clear();
			double angle = 360.0 / chains.size();
			for (Chain entry : chains) {
				double x = this.center.getX() + this.radius * Math.cos(Math.toRadians(angle * chains.indexOf(entry)));
				double y = this.center.getY() + this.radius * Math.sin(Math.toRadians(angle * chains.indexOf(entry)));
				int intX = (int) x;
				int intY = (int) y;
				points.add(new Point(intX, intY));
			}
		}

		// puts the DotLabels to their correct location
		private void drawDots() {
			for (int i = 0; i < chains.size(); i++) {
				Point p = points.get(i);
				DotLabel label = labels.get(i);
				int weight = chains.get(i).getWeight();
				int size = Math.min(this.dotSize + weight, this.radius);
				label.setBounds(p.x - size / 2, p.y - size / 2, size, size);
			}
		}

		// represents a dot on the circle
		private class DotLabel extends JLabel implements MouseListener {
			// private static final long serialVersionUID =
			// -6035584728265299996L;
			private Chain entry;

			public DotLabel(Chain e) {
				this.entry = e;
			}

			@Override
			public void paintComponent(Graphics g) {
				int size = this.getSize().width;
				// super.paintComponent(g);
				g.setColor(Color.BLUE);
				g.fillOval(0, 0, size, size);
				g.setColor(Color.BLACK);
				g.drawOval(0, 0, size - 1, size - 1);
			}

			public void mouseClicked(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
				// System.out.println("MOUSED OVER: " + entry);
				output(this.entry);
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}
		}
	}
}
