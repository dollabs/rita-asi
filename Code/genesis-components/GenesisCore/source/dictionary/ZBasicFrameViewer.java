package dictionary;

//import java.awt.BorderLayout;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import constants.Markers;
import frames.classic.FrameBundle;
import frames.entities.Bundle;
import utils.Mark;
import utils.Rectangle;
import zhutianYang.ZPage;

//import javax.swing.*;

//import bridge.adapters.EntityToViewerTranslator;

/**
* Provides capability of drawing Jackendoff frames, expressed as display
* bundles.
* 
*/

class FontMetricz extends FontMetrics {

	private static final long serialVersionUID = 1L;

	public FontMetricz(Font f) {
		super(f);
	}
}

public class ZBasicFrameViewer extends JComponent implements MouseMotionListener, MouseListener {
	
	private static Boolean PRINT_SIMPLE = false;
	private static int LESS_HEIGHT = 400;

	/*
	 * Edited 1/16/08 TAL When bars are moused over, threads are displayed.
	 */

	private static final long serialVersionUID = 1L;

	public static boolean DEBUG_FULL_FONT_INFO = false;

	private FrameBundle data = null;

	private static final double shrinkFactor = 0.97; // how much the font shrinks
//	private static final double shrinkFactor = 0.9; // how much the font shrinks
	
	private static final double padFactor = 1.25; // extra size on edges

	private String banner = null;

	private int preferredWidth = 0;

	/*
	 * In order to display threads upon mouse clicks to the frame, we will store
	 * a map of rectangles to Things. The rectangles represent screen areas
	 * where parts of frames are displayed. -TAL.16.Jan.08
	 */
	private Map<Rectangle, String> pointMap = null;

	/*
	 * The PopupFactory will allow for the creation of mouseover thread
	 * components -TAL.16.Jan.08
	 */

	// The location where the popup will be displayed on the screen
	private Point point;

	// Added so name can be displayed, rather than a frame bundle -- phw 24
	// February 2004
	public void setBanner(String s) {
		banner = s;
	}

	public void setInput(FrameBundle b) {
		data = b;
		repaint();
	}

	// TAL.16.Jan.08
	public ZBasicFrameViewer() {
		pointMap = null;
		pointMap = new HashMap<Rectangle, String>();
		addMouseMotionListener(this);
		addMouseListener(this);
		point = new Point();
	}

	/**
	 * Replaces inherited method with bundle painter.
	 */
	public void paintComponent(Graphics g) {
		// Forget old thread location information
		pointMap.clear();
		int height = getHeight();
//		Mark.night(getHeight());
		int width = getWidth();
		// System.err.println("Painting frame");
		if (banner != null) {
			paintBanner(g, width, height, banner);
			return;
		}
		// Logger.info(this, "Painting ladders");
		preferredWidth = 0;
		if (data == null) {
			return;
		}
		double hfactor = getBundleHeightFactor(data);
		if (DEBUG_FULL_FONT_INFO) {
			System.out.println("Bundle Size: " + hfactor);
		}
		paintFrameBundle(g, data, 0, 0, width, height, Math.min((height / hfactor), 25));
	}

	private void paintBanner(Graphics g, int w, int h, String m) {
		// Logger.info(this, "Painting banner " + m);
		Font f = g.getFont();
		String name = f.getName();
		int style = f.getStyle();
		int wSpan = 0;
		int hSpan = 0;
		for (int size = 30; size > 0; size = size - 2) {
			g.setFont(new Font(name, style, size));
			FontMetrics fm = g.getFontMetrics();
			wSpan = fm.stringWidth(m);
			hSpan = fm.getAscent();
			if (w > wSpan && h > hSpan) {
				break;
			}
		}
		g.drawString(banner, (w - wSpan) / 2, (h + hSpan) / 2);
	}

	/**
	 * Does work of displaying a bundle and recursively displaying subbundles.
	 */
	private void paintFrameBundle(Graphics g, FrameBundle bundle, int x, int y, int width, int height, double preciseFontHeight) {
		int topPrefWidth = 0, bottomPrefWidth = 0, maxPreferredWidth = 0;

		// If no size, return
		if (height == 0 || width == 0) {
			return;
		}

		// If no bundle, return
		if (data == null) {
			return;
		}

		// Calculate font heights
		double nextFontHeight = (preciseFontHeight * shrinkFactor);
		int fontHeight = (int) preciseFontHeight;
		int halfFontHeight = (int) (preciseFontHeight * 0.5);
		int quarterFontHeight = (int) (preciseFontHeight * 0.25);
		int threeQuarterFontHeight = (int) (preciseFontHeight * 0.75);
		int topFontHeight = Math.min(fontHeight, height / 2);
		int bottomFontHeight = Math.min(threeQuarterFontHeight, height / 4);
		int lineWidth = 5;

		// Draw top text
		if (topFontHeight > 0) {
			g.setFont(g.getFont().deriveFont((float) topFontHeight));
			
			// added by Zhutian for changing font color
			Font font = g.getFont().deriveFont((float) topFontHeight);
			g.setFont(new Font(ZPage.defaultFontName, Font.PLAIN, font.getSize()));
			g.setColor(bundle.getBarColor());
			
			if (DEBUG_FULL_FONT_INFO) {
				FontMetricz fm = new FontMetricz(g.getFont().deriveFont((float) topFontHeight));
				System.out.println("FH: " + fm.getHeight());
			}
			FontMetrics fm = g.getFontMetrics();
			topPrefWidth = fm.getStringBounds(bundle.getTop(), g).getBounds().width;
			topPrefWidth = x + fontHeight + lineWidth + topPrefWidth;
			
			// added by Zhutin for printing
			String top = bundle.getTop();
			if(!top.equals(Markers.ROLE_MARKER) && !top.equals(Markers.CONJUNCTION)) {
				g.drawString(top, x + fontHeight + lineWidth, y + topFontHeight);
			}
			
			
			if (bundle.isNegated()) {
				Color handle = g.getColor();
				String theTop = bundle.getTop();
				int xOrigin = x + fontHeight + lineWidth;
				int yOrigin = y + fm.getHeight() / 2;
				int stringWidth = fm.stringWidth(theTop);
				g.setColor(Color.RED);
				g.fillRect(xOrigin, yOrigin, stringWidth, 2);
				g.setColor(handle);
			}
		}

		// Draw bottom text
		if (bottomFontHeight > 0) {
			g.setFont(g.getFont().deriveFont((float) bottomFontHeight));
			if (DEBUG_FULL_FONT_INFO) {
				FontMetricz fm = new FontMetricz(g.getFont().deriveFont((float) bottomFontHeight));
				System.out.println("FB: " + fm.getHeight());
			}
			bottomPrefWidth = g.getFontMetrics().getStringBounds(bundle.getBottom(), g).getBounds().width;
			bottomPrefWidth = x + fontHeight + lineWidth + bottomPrefWidth;
			// System.out.println("Preferred right x-point of string \""+
			// bundle.getBottom() +"\" is " + bottomPrefWidth);

			// added by Zhutian to show only essential info of frames
			String bottom = bundle.getBottom();
			if(PRINT_SIMPLE) {
				
				if(bottom.contains(") (")) {
					bottom = bottom.replace("(clause_holders: [])", "").replace("(characterized: true)", "");
				}
				bottom = bottom.substring(bottom.indexOf(": ")+1);
//				Mark.night(bottom);
			}
			if(!bottom.contains(Markers.NAME)) {
				g.drawString(bottom, x + fontHeight + lineWidth, y + height - bottomFontHeight);
			}
			
			/*
			 * Store coordinates of the bar, mapped to appropriate threads.
			 * --TAL.16.Jan.08
			 */
			pointMap.put(new Rectangle(x + halfFontHeight, y + quarterFontHeight, lineWidth, height - halfFontHeight), bundle.getListenerBottom());
		}

		// Draw line
		Color colorHandle = g.getColor();
		g.setColor(bundle.getBarColor());
		g.fillRect(x + halfFontHeight, y + quarterFontHeight, lineWidth, height - halfFontHeight);
		g.setColor(colorHandle);

		// Set perferred width -- MAF.21.Feb.04
		maxPreferredWidth = Math.max(bottomPrefWidth, topPrefWidth);
		preferredWidth = Math.max(maxPreferredWidth, preferredWidth);
		// System.out.println("Preferred width is now: " + preferredWidth);

		// Then, work recursively
		int xOffset = fontHeight;
		double yOffset = (preciseFontHeight * padFactor);
		double bundleHeight = height - (2 * yOffset);
		double sum = 0;
		Vector bundles = bundle.getFrameBundles();
		double total = 0;
		for (int i = 0; i < bundles.size(); ++i) {
			FrameBundle innerBundle = (FrameBundle) (bundles.elementAt(i));
			total += getBundleHeightFactor(innerBundle);
		}
		for (int i = 0; i < bundles.size(); ++i) {
			FrameBundle innerBundle = (FrameBundle) (bundles.elementAt(i));
			int thisBundleHeight = (int) (bundleHeight * (getBundleHeightFactor(innerBundle) / total));
			paintFrameBundle(g, innerBundle, x + xOffset, (int) (y + yOffset + sum), (int) (width - yOffset), thisBundleHeight, nextFontHeight);
			sum += thisBundleHeight;
		}
	}

	private double getBundleHeightFactor(FrameBundle bundle) {
		double bhf = 2 * padFactor;
		Vector bundles = bundle.getFrameBundles();
		for (int i = 0; i < bundles.size(); i++) {
			FrameBundle innerBundle = (FrameBundle) (bundles.elementAt(i));
			bhf += shrinkFactor * getBundleHeightFactor(innerBundle);
		}
		return bhf;
	}

	public Dimension getPreferredSize() {
		return new Dimension(preferredWidth, getHeight());
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		ZFrameViewer display = new ZFrameViewer();
		Vector v = new Vector();
		v.add("Go");
		v.add("Move");
		v.add("Thing");
		FrameBundle bundle1 = new FrameBundle("Go", v, true);
		FrameBundle bundle2 = new FrameBundle("Bird", v, false); // "Bird Animal Thing");
		FrameBundle bundle3 = new FrameBundle("Tree", v, true); // "Tree Plant Thing");
		bundle1.addFrameBundle(bundle2);
		bundle2.addFrameBundle(bundle3);
		System.out.println("Depth: " + bundle1.depth());
		System.out.println("Depth: " + bundle2.depth());
		System.out.println("Depth: " + bundle3.depth());
		display.setInput(bundle1);
		frame.getContentPane().add(display, BorderLayout.CENTER);
		frame.setSize(600, 400);
		frame.show();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

	}

	public void mouseDragged(MouseEvent arg0) {

	}

	public void mouseMoved(MouseEvent e) {
		// Hide popup on subsequent mouse move.
		/*
		 * Find the location of the mouse, and determine which, if any, bar was
		 * moused-over. Report appropriate threads.
		 */
		int x = e.getX();
		int y = e.getY();
		point.setLocation(x, y);
		// System.out.println("Mouse Clicked at " + x + "," + y);
		String threads = "";
		String[] splitThreads;
		for (Rectangle rect : pointMap.keySet()) {
			if (rect.contains(x, y)) {
				threads = pointMap.get(rect);
				// System.out.println(result);
			}
		}
		if (threads.isEmpty()) {
			if (popup != null) {
				popup.hide();
				// popup = null;
			}
		}
		/*
		 * If there are threads to show, show one per line.
		 */
		if (!threads.equals("")) {
			splitThreads = threads.split(",");
			threads = "";
			for (String thread : splitThreads) {
				threads = threads.concat(thread);
				threads = threads.concat("\n");
			}
			/*
			 * The coordinates of point are relative to the BasicFrameViewer.
			 * They need to be converted to screen coordinates.
			 */
			SwingUtilities.convertPointToScreen(point, this);
			if (popup != null) {
				popup.hide();
			}
			popup = PopupFactory.getSharedInstance().getPopup(this, new JTextArea(threads), (int) point.getX(), (int) point.getY());
			popup.show();
		}
	}

	static Popup popup;

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (popup != null) {
			// popup.hide();
		}

	}

}