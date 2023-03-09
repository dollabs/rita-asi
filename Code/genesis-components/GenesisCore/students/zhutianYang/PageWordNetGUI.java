package zhutianYang;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dictionary.WordNet;
import frames.entities.Thread;
import jakeBarnwell.treeGui.DrawableSpool;
import jakeBarnwell.treeGui.RectArea;
import utils.Mark;
import utils.Z;

/**
 * A simple GUI for WordNet threads.
 *
 * @author jb16
 */
public class PageWordNetGUI extends JPanel implements MouseListener,
			MouseWheelListener, MouseMotionListener, WindowListener {

	class TextFieldListener implements DocumentListener {
		private void update() {
			String w = wordBox.getText().trim();
			if(w.length() != 0) {
				int index = wordBox.getText().indexOf('.');
				if (index >= 0) {
					w = w.substring(0, index);
				}
				// Get the new threads from WordNet
				threads = wordnet.lookup(w).stream().collect(Collectors.toList());
				newThreads = true;

				// Wipe out all history of mouse-over stuff
				moNode = null;
				lastMoNode = null;
				moNodeAncestors.clear();
				moNodeDescendants.clear();
				drawLocations.clear();

				// Repaint everything
				repaint();
			}
		}

        public void changedUpdate(DocumentEvent e) {
        	update();
        }

        public void insertUpdate(DocumentEvent e) {
			update();
        }

        public void removeUpdate(DocumentEvent e) {
        	update();
        }
	}

	// Colors of various nodes when being drawn
	// deleted "final" by Zhutian for using other color for screenshot
	private static Color MO_COLOR = Color.GREEN;
	private static Color MO_ANCESTOR_COLOR = Color.CYAN;
	private static Color MO_DESCENDANT_COLOR = Color.YELLOW;
	private static Color DEFAULT_COLOR = new Color(245, 230, 230);

	// How much to zoom by (multiplicative) when zooming in/out
	private static final double ZOOM_MULTIPLIER = 1.2;

	private static final long serialVersionUID = 1L;

	// Only relevant when running as its own frame (i.e. not through Genesis)
	private static final int FRAME_WIDTH_DFLT = 1024, FRAME_HEIGHT_DFLT = 768;

	// The size of the permissible drawing canvas for the tree
	private int canvasWidth, canvasHeight;

	// The layout
	private BorderLayout layout;

	// Container for typing in a word to look up
	private final JTextField wordBox;
	private final WordNet wordnet = new WordNet();

	// Threads corresponding to the things we want to draw on screen
	private List<Thread> threads = new ArrayList<Thread>();

	// True if the list of threads has been updated
	private boolean newThreads = true;

	// Used when drawing threads
	private static final String ROOT_WORD = "ROOT-WORD";
	private static final Thread ROOT_THREAD = Thread.constructThread(ROOT_WORD);
	private static final DrawableSpool ROOT_NODE = new DrawableSpool(ROOT_THREAD, null, 0);

	// x, y translation (correction) for drawn elements.
	// If the user drags the canvas left, we record a negative x-translation;
	// If the user drags the canvas up, we record a negative y-translation.
	private int tx = 0, ty = 0;

	// Variables for mouse movement and mouseover events
	private int mX, mY; // The current x, y coords of the mouse
	private DrawableSpool moNode; // The node currently moused over;
	private DrawableSpool lastMoNode; // The node last moused over;

	// Variables for mouse dragging events
	private int mouseDownX, mouseDownY; // The x, y positions of mouse when mouse went down
	private int txAtMouseDown, tyAtMouseDown; // The tx and ty when the mouse went down
	private boolean mouseDownP = false; // Whether or not the mouse is down

	// Current zoom level; default zoom is 1
	private double zoom = 1.0;


	/** Strict ancestors of the moused-over node; maybe empty, but never null */
	private HashSet<DrawableSpool> moNodeAncestors = new HashSet<>();

	/** Strict descendants of the moused-over node; maybe empty, but never null */
	private HashSet<DrawableSpool> moNodeDescendants = new HashSet<>();

	/** Stores where things are painted so we don't have to do as much re-drawing */
	private HashMap<DrawableSpool, RectArea> drawLocations = new HashMap<>();

	public PageWordNetGUI() {
		super();

		// Sets the user-visible entry in the Genesis menu of items
		this.setName("WordNet tree viewer");

		layout = new BorderLayout();
		this.setLayout(layout);
		setBackground(Color.WHITE);

		// Where you type in the word you want to look up
		wordBox = new JTextField();
		this.add(wordBox, BorderLayout.SOUTH);
		wordBox.setFont(new Font("Helvetica", Font.BOLD, 18));

		// added by zhutian for beautify interface and take screenshot
		MO_COLOR = Z.LIGHT_GREEN;
		MO_ANCESTOR_COLOR = Z.LIGHT_CYAN;
		MO_DESCENDANT_COLOR = Z.LIGHT_YELLOW;
		DEFAULT_COLOR = Z.LIGHT_PINK;

		wordBox.setText("");

		// Add text field listener to the word box
		wordBox.getDocument().addDocumentListener(new TextFieldListener());
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	private void drawWordBoxCentered(Graphics2D g2d, String s, int cx, int cy, int w, int h, Color c) {
		FontMetrics fm = g2d.getFontMetrics();
		Rectangle2D r = fm.getStringBounds(s, g2d);
		int x = cx - w / 2;
		int y = cy - h / 2;
		int sx = x + (w - (int) r.getWidth()) / 2;
		int sy = y + (h - (int) r.getHeight()) / 2 + fm.getAscent();

		if(c == null) {
			g2d.drawRect(x, y, w, h);

		} else {
			// added by zhutian for beautify interface and take screenshot
			g2d.setColor(c);
			g2d.fillRoundRect(x, y, w, h, h/5, h/5);
			g2d.drawRoundRect(x, y, w, h, h/5, h/5);
			g2d.setColor(Z.BLACK);

		}

		g2d.drawString(s, sx, sy);
	}

	/**
	 * Draws the thread trees
	 *
	 * @param g2d
	 * @param threads
	 */
	private void drawThreads(Graphics2D g2d) {
		if(threads.size() == 0) {
			return;
		}

		// Prepend common label to all threads so that it's technically 1 tree to draw
		if(newThreads) {
			for(Thread t : threads) {
				t.add(0, ROOT_WORD);
			}
			newThreads = false;
		}

		// This will store every single node in the tree
		HashMap<Thread, DrawableSpool> allNodes = new HashMap<>();

		// Make sure the root node starts out with no children
		ROOT_NODE.children.clear();

		// Put the root node into the tree
		allNodes.put(ROOT_THREAD, ROOT_NODE);

		// Now we put everything else into the tree
		for(Thread t : threads) {
			// For a thread, iteratively create children down the thread
			for(int depth = 1; depth < t.size(); depth++) { // note we halt on 2nd-last layer
				Thread currThread = Thread.constructThread(t, 0, depth);
				DrawableSpool currNode = allNodes.get(currThread);

				Thread childThread = Thread.constructThread(t, 0, depth + 1);
				DrawableSpool childNode = allNodes.getOrDefault(childThread, new DrawableSpool(childThread, currNode, depth + 1));
				allNodes.putIfAbsent(childThread, childNode);
				currNode.children.putIfAbsent(childThread, childNode);
			}
		}

		// added by zhutian for beautify interface and take screenshot
		g2d.setFont(ZPage.medianFont);

		// Compute a few necessary constants
		double maxStringWidth = 0, maxStringHeight = 0;
		FontMetrics fm = g2d.getFontMetrics();
		Rectangle2D r;
		for(DrawableSpool node : allNodes.values()) {
			r = fm.getStringBounds(node.word, g2d);
			maxStringWidth = Math.max(maxStringWidth, r.getWidth());
			maxStringHeight = Math.max(maxStringHeight, r.getHeight());
		}
		int maxBoxWidth = (int) (maxStringWidth * 1.1);
		int maxBoxHeight = (int) (maxStringHeight * 1.5);

		// Compute the coordinates, normalized to [0,1]; call this method on the root
		allNodes.get(ROOT_THREAD).computeNormalizedDrawCoordinates();

		int boxWidth, boxHeight, x, y;
		// Draw the nodes and connector lines.
		List<DrawableSpool> orderedDrawNodes = getDrawOrder(allNodes.values());
		for(DrawableSpool node : orderedDrawNodes) {
			// For the root node, don't draw anything
			if(node == ROOT_NODE) {
				continue;
			}

			x = mapX(node.x, maxBoxWidth);
			y = mapY(node.y, maxBoxHeight);

			r = fm.getStringBounds(node.word, g2d);
			boxWidth = (int) (r.getWidth() * 1.1);
			boxWidth = boxWidth < maxBoxWidth * 0.5 ? (int)(boxWidth * 1.25) : boxWidth; // bias a bit to bigger box
			boxHeight = maxBoxHeight;

			// Draw this node as a box
			if(moNode != null) {
				// This means the user has moused over some box
				if(moNode.equals(node)) {
					drawWordBoxCentered(g2d, node.word, x, y, boxWidth, boxHeight, MO_COLOR);
				} else {
					if(moNodeAncestors.contains(node)) {
						drawWordBoxCentered(g2d, node.word, x, y, boxWidth, boxHeight, MO_ANCESTOR_COLOR);
					} else if(moNodeDescendants.contains(node)) {
						drawWordBoxCentered(g2d, node.word, x, y, boxWidth, boxHeight, MO_DESCENDANT_COLOR);
					} else {
						drawWordBoxCentered(g2d, node.word, x, y, boxWidth, boxHeight, DEFAULT_COLOR);
					}
				}
			} else {
				// This means the user is not mousing over any box
				drawWordBoxCentered(g2d, node.word, x, y, boxWidth, boxHeight, DEFAULT_COLOR);
			}
			drawLocations.put(node, new RectArea(x - boxWidth/2, y - boxHeight/2, x + boxWidth/2, y + boxHeight/2));

			// Draw the connector lines to children
			int startX = x;
			int startY = y + boxHeight / 2;
			for (DrawableSpool child : node.children.values()) {
				int endX = mapX(child.x, maxBoxWidth);
				int endY = mapY(child.y, maxBoxHeight) - boxHeight / 2;

				// added by zhutian for beautify interface and take screenshot
				g2d.setColor(Z.BLACK);
				g2d.drawLine(startX, startY, endX, endY);
			}
		}
	}

	/**
	 * Maps a node's x-coordinate (center) from a [0,1] range
	 * to a drawable x-coordinate (center) within the frame.
	 * @param x
	 * @param boxWidth
	 * @return
	 */
	private int mapX(double x, int boxWidth) {
		// Only use the inner __% of the canvas.
		double innerWidthFraction = 0.95;

		// Also be sure to account for the width of the boxes so that they don't go off-screen
		double effectiveCanvasW = innerWidthFraction * canvasWidth - boxWidth;

		// The pre-transformation value
		double rawX = x * effectiveCanvasW + 0.05 * effectiveCanvasW + boxWidth / 2;

		// Correct for transformations
		return (int)rawX + tx;
	}

	/**
	 * Maps a node's y-coordinate (center) from a [0,1] range
	 * to a drawable y-coordinate (center) within the frame.
	 * @param y
	 * @param boxHeight
	 * @return
	 */
	private int mapY(double y, int boxHeight) {
		// Only use the inner __% of the canvas.
		double innerHeightFraction = 0.95;

		// Also be sure to account for the height of the boxes so that they don't go off-screen
		double effectiveCanvasH = innerHeightFraction * canvasHeight - boxHeight;

		// The pre-transformation value
		double rawY = y * effectiveCanvasH - 0.025 * effectiveCanvasH + boxHeight / 2;

		// Correct for transformations
		return (int)rawY + ty;
	}

	/**
	 * Given a collection of nodes to draw, computes an effective order
	 * to draw them in.
	 * @param allDrawNodes
	 * @return
	 */
	private List<DrawableSpool> getDrawOrder(Collection<DrawableSpool> allDrawNodes) {
		// Draw the lineage of the MO node *last* so that they appear on top.
		//  This is useful for when the screen is small and there is overlap
		//  amongst siblings.
		HashSet<DrawableSpool> lineage = new HashSet<>(moNodeAncestors);
		lineage.addAll(moNodeDescendants);
		if(moNode != null) {
			lineage.add(moNode);
		}
		List<DrawableSpool> finalOrder = new ArrayList<>(lineage);
		finalOrder.addAll(lineage);
		for(DrawableSpool node : allDrawNodes) {
			if(!lineage.contains(node)) {
				finalOrder.add(node);
			}
		}
		Collections.reverse(finalOrder);
		return finalOrder;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		canvasWidth = getWidth();
		canvasHeight = getHeight() - wordBox.getHeight() - layout.getVgap();

		Graphics2D g2d = (Graphics2D) g;

		drawThreads(g2d);
	}

	// Main function for demo
	public static void main(String[] args) {
		PageWordNetGUI gui = new PageWordNetGUI();

		JFrame frame = new JFrame();
		frame.add(gui);
		frame.setTitle("WordNet Exploration");
		frame.setBounds(0, 0, FRAME_WIDTH_DFLT, FRAME_HEIGHT_DFLT);
		// frame.addWindowListener(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		frame.addComponentListener(new ComponentListener() {
		    public void componentResized(ComponentEvent e) {
		        gui.repaint();
		    }

			@Override
			public void componentMoved(ComponentEvent e) { }

			@Override
			public void componentShown(ComponentEvent e) { }

			@Override
			public void componentHidden(ComponentEvent e) { }
		});

		Mark.say("WordNet GUI initialized.");
	}

	@Override
	public void windowOpened(WindowEvent e) { }

	@Override
	public void windowClosing(WindowEvent e) { }

	@Override
	public void windowClosed(WindowEvent e) { }

	@Override
	public void windowIconified(WindowEvent e) { }

	@Override
	public void windowDeiconified(WindowEvent e) { }

	@Override
	public void windowActivated(WindowEvent e) { }

	@Override
	public void windowDeactivated(WindowEvent e) { }

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			zoom *= ZOOM_MULTIPLIER;
		}
		else if(e.getButton() == MouseEvent.BUTTON3) {
			zoom /= ZOOM_MULTIPLIER;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseDownX = e.getX();
		mouseDownY = e.getY();
		txAtMouseDown = tx;
		tyAtMouseDown = ty;
		mouseDownP = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseDownP = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }

	@Override
	public void mouseDragged(MouseEvent e) {
		if(!mouseDownP) {
			return;
		}
		tx = txAtMouseDown + (e.getX() - mouseDownX);
		ty = tyAtMouseDown + (e.getY() - mouseDownY);
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	    mX = (int) e.getPoint().getX();
	    mY = (int) e.getPoint().getY();
	    moNode = RectArea.which(drawLocations, mX, mY);
	    if(			(moNode == null && lastMoNode != null)
	    		|| 	(moNode != null && lastMoNode == null)
	    		||	(moNode != null && !moNode.equals(lastMoNode))) {
	    	if(moNode == null) {
	    		moNodeAncestors.clear();
	    		moNodeDescendants.clear();
	    	} else {
	    		moNodeAncestors = moNode.getAncestors();
	    		moNodeDescendants = moNode.getDescendants();
	    	}
		    drawLocations.clear();
	    	repaint();
	    }
	    lastMoNode = moNode;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		double rotation = e.getPreciseWheelRotation();
		// TODO for future zoom capabilities?
	}


}
