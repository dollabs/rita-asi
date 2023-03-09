package tomLarson;

import java.awt.Color;
import java.awt.Dimension;

import java.text.AttributedString;
import java.util.Set;
import java.awt.*;
import java.awt.font.TextAttribute;
import javax.swing.*;
//import bridge.views.frameviews.classic.FrameViewer.LocalPlug;

import frames.entities.Entity;

@SuppressWarnings("rawtypes")
public class DisambiguatorViewerHelper extends JApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// ============= CONSTANTS ============
	public static final int SCROLL_ALWAYS = 2;
	public static final int SCROLL_AS_NEEDED = 1;
	public static final int SCROLL_NEVER = 0;


	// =============== MEMBER FIELDS =============

	final static int maxCharHeight = 15;
	final static int minFontSize = 6;

	final static Color bg = Color.white;
	final static Color fg = Color.black;
	final static Color red = Color.red;
	final static Color white = Color.white;

	final static BasicStroke stroke = new BasicStroke(2.0f);
	final static BasicStroke wideStroke = new BasicStroke(8.0f);

	final static float dash1[] = {10.0f};
	final static BasicStroke dashed = new BasicStroke(1.0f, 
			BasicStroke.CAP_BUTT, 
			BasicStroke.JOIN_MITER, 
			10.0f, dash1, 0.0f);
	String test;
	Dimension totalSize;
	FontMetrics fontMetrics;

	ThreadTree tree;
	double sum;

	public DisambiguatorViewerHelper() {
		this.init();
		tree = null;

	}
	public void init() {
		//Initialize drawing colors
		setBackground(bg);
		setForeground(fg);
	}


	@SuppressWarnings({ "unchecked", "unused" })
	private void display(Node node, Graphics2D g2, int x, int y, int offset) {
		AttributedString s = new AttributedString(node.getName());
		double weight = node.getWeight();
		double ratio = weight/sum;
		int red, blue;
		//g2.setFont(new Font("name",9,9));
		if (ratio <= .1) {
			blue = (int) ((.1-ratio) * (255.0/.1));
			red = 0;
		}
		else {
			red = (int) ((ratio - .1)*(255.0/.1));
			if (red >=255) {red = 255;}
			blue = 0;
		}
		//System.out.println(red);
		//System.out.println(blue);
		//s.addAttribute(TextAttribute.JUSTIFICATION, .5);
		//s.addAttribute(TextAttribute.FONT, new Font("SansSerif",0,14));
		//s.addAttribute(TextAttribute.BACKGROUND, Color.red);
		s.addAttribute(TextAttribute.FOREGROUND, new Color(red,0,blue));
		g2.drawString(s.getIterator(), x,y);
		//g2.drawOval(x-5, y-5, 30, 30);
		int deltay = 20;
		int deltax = 20;
		int newx;
		int factor;
		Set<Node> children = node.getChildren();
		int numChildren = children.size();
		boolean odd = (numChildren % 2 == 1);
		int halfway = numChildren/2;
		double half = (double)1.0*numChildren/2.0;
		double fromMid;
		for (Node child : children) {
			int newDeltax = deltax - offset*3;
			factor = (numChildren > half) ? 1 : -1;
			factor = (numChildren == halfway + 1 && odd) ? 0 : factor;
			fromMid = factor*numChildren;
			//fromMid = numChildren - half;
			newx = x +  (int)fromMid*newDeltax+offset*deltax;
			g2.drawLine(x,y, newx, y+deltay);
			Dimension d = getSize();
			display(child, g2, newx, y+deltay, offset+(int)fromMid);
			numChildren--;
			
		}
	}
	public void paint(Graphics g) {
		if (tree!= null) {
			Graphics2D g2 = (Graphics2D) g;
			Dimension d = getSize();
			g2.clearRect(0,0,d.width, d.height);
			int x = d.width/2;
			int y = 10;
			//g2.drawString("Hello", d.width/2, 0);
			Node head = tree.getHead();
			if (head != null) {
				display(head, g2, x,y, 0);
			}
			
		}
	}

	public void run(Entity s) {
//		JFrame f = new JFrame("ShapesDemo2D");
//		f.addWindowListener(new WindowAdapter() {
//		public void windowClosing(WindowEvent e) {System.exit(0);}
//		});
//		JApplet applet = new DisambiguatorViewerHelper();
//		f.getContentPane().add("Center", applet);
//		applet.init();
//		f.pack();
//		//    f.setSize(new Dimension(550,100));
//		//    f.setVisible(true);
	}


	public void setInput(ThreadTree t) {
		tree = t;
		if (t.isEmpty()) {sum = 0;}
		else {sum = getSum(t.getHead());}
		repaint();
	}
	
	@SuppressWarnings("unchecked")
	private double getSum(Node n) {
		double sum = n.getWeight();
		Set<Node> children = n.getChildren();
		for (Node child : children) {
			sum+= getSum(child);
		}
		return sum;
	}



}