package memory.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import connections.Connections;
import connections.WiredBox;
import frames.Frame;
import frames.entities.Entity;
import memory.soms.NewSom;
import memory.soms.Som;
/**
 * Used to visualize self-organizing maps.
 * 
 * @author sglidden
 */
public class SomViewer extends JPanel implements WiredBox {
	CirclePanel<Entity>	circle	= new CirclePanel<Entity>();
	public SomViewer() {
		super();
		this.setLayout(new BorderLayout());
		this.add(this.circle, BorderLayout.CENTER);
		Connections.getPorts(this).addSignalProcessor("input");
	}
	/**
	 * Displays the given self-organizing map.
	 * 
	 * @param s
	 *            Som
	 */
	public void display(Som<Entity> s) {
		// this.removeAll();
		if (s != null) {
			// make the Map that clickcircle needs
			Map<Entity, Set<Entity>> neighborhood = new HashMap<Entity, Set<Entity>>();
			for (Entity e : s.getMemory()) {
				neighborhood.put(e, new HashSet<Entity>(s.neighbors(e)));
			}
			Map<Entity, Integer> weights = new HashMap<Entity, Integer>();
			if (s instanceof NewSom) {
				weights = ((NewSom) s).getWeights();
			}
			this.circle.setEntries(neighborhood, weights);
		}
		this.repaint();
	}
	// receives SOMs to display
	public void input(Object input) {
		if (input instanceof Som) {
			this.display((Som<Entity>) input);
		} else {
			System.err.println("Bad input to SomViewer");
		}
	}
	// outputs elements that have been moused over
	public void output(Object output) {
		if (output instanceof Entity) {
			Connections.getPorts(this).transmit(output);
		} else if (output instanceof Frame) {
			Frame el = (Frame) output;
			Connections.getPorts(this).transmit(el.getThing());
		}
	}
	/**
	 * Designed for visualizing Gauntlet's self organizing maps Makes a clickable label. This label draws a circle, and
	 * places entries evenly spaced around its perimeter. Then, it moves the entries to optimize their position. Each
	 * entry tries to be as close to neighboring entries as possible.
	 * 
	 * @author Sam Glidden
	 * @param <E>
	 *            entry type
	 */
	private class CirclePanel<E> extends JPanel {
		private Map<E, Set<E>>		neighborhood	= new HashMap<E, Set<E>>();
		Map<E, Integer>				weights			= new HashMap<E, Integer>();
		private List<E>				order			= new ArrayList<E>();
		private Map<E, DotLabel>	dots			= new HashMap<E, DotLabel>();
		private Map<E, Point>		circle			= new HashMap<E, Point>();
		private int					width, height;
		private int					radius;
		int							dotSize;
		private Point				center			= new Point(0, 0);
		SomViewer					parent;
		public CirclePanel() {
			super();
			this.setBackground(Color.WHITE);
			this.setLayout(null);
		}
		/**
		 * Provides the entries for the circle to draw out. The entries are provided in a map, where each entry maps to
		 * a set of neighbor entries
		 * 
		 * @param neighborhood
		 *            Map[E -> Set[E]]
		 */
		public void setEntries(Map<E, Set<E>> neighborhood, Map<E, Integer> weights) {
			this.neighborhood = neighborhood;
			this.weights = weights;
			// init random circle
			this.order = new ArrayList<E>(neighborhood.keySet());
			this.width = this.getWidth();
			this.height = this.getHeight();
			this.radius = (Math.min(this.width, this.height) - 20) / 2;
			this.center = new Point(this.width / 2, this.height / 2);
			this.dotSize = Math.min(Math.max(this.radius / 8, 6), 12);
			// add DotLabels
			this.removeAll();
			this.dots.clear();
			for (E e : neighborhood.keySet()) {
				DotLabel label = new DotLabel(e);
				this.dots.put(e, label);
				this.add(label);
			}
			// optimize circle
			int maxPasses = 22; // number of times to loop around, optimizing
			for (int pass = 0; pass < maxPasses; pass++) {
				boolean doneEarly = true;
				for (int i = 0; i < this.order.size(); i++) {
					// see if switch with next entry is beneficial
					int j = i + 1;
					if (j == this.order.size()) {
						j = 0;
					}
					E current = this.order.get(i);
					E next = this.order.get(j);
					double oldScore = this.computeScore(current) + this.computeScore(next);
					this.order.set(j, current);
					this.order.set(i, next);
					double newScore = this.computeScore(current) + this.computeScore(next);
					if (oldScore <= newScore) {
						this.order.set(i, current);
						this.order.set(j, next);
					} else {
						// made a change -- better make another pass
						doneEarly = false;
					}
					// draw changes
					// SwingUtilities.invokeLater(new CircleRepainter(this));
					// try {Thread.sleep(200);} catch (Exception e) {System.err.println("ClickCircle Thread Sleep
					// Error");} // slow it down so we can watch
				}
				if (doneEarly == true) {
					break;
				}
			}
		}
		// returns the total distance to all the entry's neighbors
		private double computeScore(E entry) {
			this.plotEntries();
			Point p = this.circle.get(entry);
			double score = 0;
			for (E n : this.neighborhood.get(entry)) {
				Point d = this.circle.get(n);
				score += d.distance(p);
			}
			return score;
		}
		// refreshes circle coordinate map
		private void plotEntries() {
			if (!(this.order.size() == this.neighborhood.keySet().size())) {
				System.err.println("Broken rep invarient in ClickCircle! Expect wonky behavor.");
			}
			this.circle.clear();
			double angle = 360.0 / this.order.size();
			for (E entry : this.order) {
				double x = this.center.getX() + this.radius
						* Math.cos(Math.toRadians(angle * this.order.indexOf(entry)));
				double y = this.center.getY() + this.radius
						* Math.sin(Math.toRadians(angle * this.order.indexOf(entry)));
				// System.err.println(x +", "+ y);
				int intX = (int) x;
				int intY = (int) y;
				this.circle.put(entry, new Point(intX, intY));
			}
			if (!this.neighborhood.keySet().equals(this.circle.keySet())) {
				System.err.println("Broken rep invarient in ClickCircle! Expect wonky behavor.");
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
			this.plotEntries();
			// step 2: draw the circle
			g.setColor(Color.LIGHT_GRAY);
			g.fillOval((int) this.center.getX() - this.radius - 2, (int) this.center.getY() - this.radius - 2,
					this.radius * 2 + 4, this.radius * 2 + 4);
			g.setColor(Color.WHITE);
			g.fillOval((int) this.center.getX() - this.radius + 1, (int) this.center.getY() + 1 - this.radius,
					this.radius * 2 - 2, this.radius * 2 - 2);
			// step 3: connect neighbors
			for (E entry : this.circle.keySet()) {
				Point start = this.circle.get(entry);
				for (E target : this.neighborhood.get(entry)) {
					Point end = this.circle.get(target);
					g.setColor(Color.BLACK);
					g.drawLine(start.x, start.y, end.x, end.y);
				}
			}
			// step 4: position the dots
			this.drawDots();
		}
		// moves the DotLabels to their correct location
		private void drawDots() {
			for (E e : this.dots.keySet()) {
				Point p = this.circle.get(e);
				DotLabel label = this.dots.get(e);
				label.addMouseListener(label);
				// label.setToolTipText(e.toString());
				int weight = this.weights.get(e) == null ? 1 : this.weights.get(e);
				int size = this.dotSize + weight * 2;
				label.setBounds(p.x - size / 2, p.y - size / 2, size, size);
			}
		}
		// from Winston
		class CircleRepainter implements Runnable {
			CirclePanel	circle;
			public CircleRepainter(CirclePanel c) {
				this.circle = c;
			}
			public void run() {
				this.circle.repaint();
			}
		}
		// represents a dot on the circle
		private class DotLabel extends JLabel implements MouseListener {
			private E	entry;
			public DotLabel(E e) {
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
				SomViewer.this.output(this.entry);
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
