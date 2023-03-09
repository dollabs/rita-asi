package gui.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/*
 * Utility class for displaying one to three aligned vertical lists of components.  Tries to be smart about pleasing arrangement.
 * When no component occupies a place, add null in that place.  Adding a string adds a JLabel using that string.
 * Height of all rows is determined by the height of the tallest component shown anywhere in the container.
 */
public class ParallelJPanel extends SpecialJPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6221771356216603892L;

	protected ArrayList leftList = new ArrayList();

	protected ArrayList centerList = new ArrayList();

	protected ArrayList rightList = new ArrayList();

	protected int vSpacer = 10;
	protected int hSpacer = 10;

	//private int boundary;

	public ParallelJPanel () {
		setLayout(new MyLayoutManager());
		setOpaque(false);
	}

	public void clear() {
		leftList.clear();
		centerList.clear();
		rightList.clear();
	}

	/*
	 * Add a component to the left-side list.
	 */
	public void addLeft(Object j) {
		if (j instanceof String) {
			j = new JLabel((String) j);
		}
		if (j instanceof JComponent) {
			add((JComponent) j);
		}
		leftList.add(j);
	}

	/*
	 * Add a component to the center list, if any.
	 */
	public void addCenter(Object j) {
		if (j instanceof String) {
			j = new JLabel((String) j);
		}
		if (j instanceof JComponent) {
			add((JComponent) j);
		}
		centerList.add(j);
	}

	/*
	 * Add a component to the right-side list.
	 */
	public void addRight(Object j) {
		if (j instanceof String) {
			j = new JLabel((String) j);
		}
		if (j instanceof JComponent) {
			add((JComponent) j);
		}
		rightList.add(j);
	}
	
	/*
	 * Convenience function to make it easier to create two-column panels.
	 * ACW 2010 June 18
	 */
	public void addPair(JComponent left, JComponent right) {
		addLeft(left); addRight(right);
	}


	protected int getLWidth() {
		int lWidth = 0;
		int maxCount = Math.max(centerList.size(), Math.max(leftList.size(), rightList.size()));
		for (int i = 0; i < maxCount; ++i) {
			Object l = null, c = null, r = null;
			if (i < leftList.size()) {l = leftList.get(i);}
			if (i < centerList.size()) {c = centerList.get(i);}
			if (i < rightList.size()) {r = rightList.get(i);}
			if (l instanceof JComponent) {
				Dimension lDimension = ((JComponent) l).getPreferredSize();
				if (c != null || r != null) {
					lWidth = Math.max(lWidth, lDimension.width);
				}
			}
		}
		return lWidth;
	}

	protected int getCWidth() {
		int cWidth = 0;
		int maxCount = Math.max(centerList.size(), Math.max(leftList.size(), rightList.size()));
		Object c = null, r = null;
		for (int i = 0; i < maxCount; ++i) {
			if (i < centerList.size()) {
				c = centerList.get(i);
			}
			if (i < rightList.size()) {
				r = rightList.get(i);
			}
			if (c instanceof JComponent) {
				Dimension cDimension = ((JComponent) c).getPreferredSize();
				if (r != null) {
					cWidth = Math.max(cWidth, cDimension.width);
				}
			}
		}
		return cWidth;
	}

	protected int getRWidth() {
		int rWidth = 0;
		int maxCount = Math.max(centerList.size(), Math.max(leftList.size(), rightList.size()));
		Object r = null;
		for (int i = 0; i < maxCount; ++i) {
			if (i < rightList.size()) {
				r = rightList.get(i);
			}
			if (r instanceof JComponent) {
				Dimension rDimension = ((JComponent) r).getPreferredSize();
				rWidth = Math.max(rWidth, rDimension.width);
			}
		}
		return rWidth;
	}

	protected int getMaxHeight() {
		int maxCount = Math.max(centerList.size(), Math.max(leftList.size(), rightList.size()));
		int lHeight = 0, cHeight = 0, rHeight = 0;
		for (int i = 0; i < maxCount; ++i) {
			Object l = null, c = null, r = null;
			if (i < leftList.size()) {l = leftList.get(i);}
			if (i < centerList.size()) {c = centerList.get(i);};
			if (i < rightList.size()) {r = rightList.get(i);};
			if (l instanceof JComponent) {
				Dimension lDimension = ((JComponent) l).getPreferredSize();
				lHeight = Math.max(lHeight, lDimension.height);
			}
			if (c instanceof JComponent) {
				Dimension cDimension = ((JComponent) c).getPreferredSize();
				cHeight = Math.max(cHeight, cDimension.height);
			}
			if (r instanceof JComponent) {
				Dimension rDimension = ((JComponent) r).getPreferredSize();
				rHeight = Math.max(rHeight, rDimension.height);
			}
		}
		return Math.max(cHeight, Math.max(lHeight, rHeight));
	}

	protected int getMinHeight() {
		int maxCount = Math.max(centerList.size(), Math.max(leftList.size(), rightList.size()));
		int lHeight = 0, cHeight = 0, rHeight = 0;
		for (int i = 0; i < maxCount; ++i) {
			Object l = null, c = null, r = null;
			if (i < leftList.size()) {l = leftList.get(i);}
			if (i < centerList.size()) {c = centerList.get(i);};
			if (i < rightList.size()) {r = rightList.get(i);};
			if (l instanceof JComponent) {
				Dimension lDimension = ((JComponent) l).getPreferredSize();
				lHeight = Math.max(lHeight, lDimension.height);
			}
			if (c instanceof JComponent) {
				Dimension cDimension = ((JComponent) c).getPreferredSize();
				cHeight = Math.max(cHeight, cDimension.height);
			}
			if (r instanceof JComponent) {
				Dimension rDimension = ((JComponent) r).getPreferredSize();
				rHeight = Math.max(rHeight, rDimension.height);
			}
		}
		int result;
		if (lHeight > 0) {result = lHeight;}
		else if (cHeight > 0) {result = cHeight;}
		else {result = rHeight;}
		if (lHeight > 0 && lHeight < result) {result = lHeight;}
		if (cHeight > 0 && cHeight < result) {result = cHeight;}
		if (rHeight > 0 && rHeight < result) {result = rHeight;}
		return result;
	}




	public Dimension getPreferredSize() {
		int maxCount = Math.max(centerList.size(), Math.max(leftList.size(), rightList.size()));
		int lWidth = getLWidth(), cWidth = getCWidth(), rWidth = getRWidth();
		int maxHeight = getMaxHeight();

		// Uncomment following if it causes layout problems
		int vSpacer = maxHeight / 2;
		int totalHeight = (maxCount * maxHeight) + ((maxCount + 1) * vSpacer);

		int hSpacer = 2 * vSpacer;
		int totalWidth = lWidth + cWidth + rWidth + hSpacer + TitledJPanel.LEFT_MARGIN;
		if (cWidth > 0) {totalWidth += hSpacer;}
		if (rWidth > 0) {totalWidth += hSpacer;}

		Dimension result = new Dimension (totalWidth, totalHeight); 
		return result;
	}

	protected class MyLayoutManager implements LayoutManager {
		public void removeLayoutComponent(Component component) {
		}

		public void addLayoutComponent(String string, Component arg1) {
		}

		public Dimension minimumLayoutSize(Container container) {
			int count = Math.max(centerList.size(), Math.max(leftList.size(), rightList.size()));
			int maxHeight = getMaxHeight();
			int lWidth = getLWidth(), cWidth = getCWidth(), rWidth = getRWidth();
			return new Dimension(lWidth + cWidth + rWidth, count*maxHeight);
		}

		public Dimension preferredLayoutSize(Container container) {
			int count = Math.max(centerList.size(), Math.max(leftList.size(), rightList.size()));
			int maxHeight = getMaxHeight();
			int lWidth = getLWidth(), cWidth = getCWidth(), rWidth = getRWidth();
			return new Dimension(20 + lWidth + cWidth + rWidth, 10 + count*(maxHeight + 10));
		}

		public void layoutContainer(Container container) {
			int height = getHeight() - getInsets().top - getInsets().bottom;
			int width = getWidth() - getInsets().left - getInsets().right;;
			if (height == 0 || width == 0) {return;}
			int count = Math.max(centerList.size(), Math.max(leftList.size(), rightList.size()));

			//Dimension d = getPreferredSize();

			int lWidth = getLWidth(), cWidth = getCWidth(), rWidth = getRWidth();

			// [phw 1-Jun-2007] Adjust widths so that right side takes precedence when need to shrink

			int maxHeight = getMaxHeight();

			int vSpacer = maxHeight / 2;
			int hSpacer = 2 * vSpacer;
			int theRightSpace = hSpacer;
			int theLeftOffset = TitledJPanel.LEFT_MARGIN; // hSpacer; // + interlineSpace;

			int totalNeededWidth = theLeftOffset + lWidth + hSpacer;
			if (cWidth > 0) {totalNeededWidth += hSpacer + cWidth;}
			if (rWidth > 0) {totalNeededWidth += hSpacer + rWidth;}

			if (totalNeededWidth > width) {
				// Need to shrink by totalNeededWidth - width
				int shortage = totalNeededWidth - width;
				int original = lWidth + cWidth;
				if (original >= shortage) {
					lWidth = (int)(lWidth - ((float)lWidth / original) * shortage);
					cWidth = (int)(cWidth - ((float)cWidth / original) * shortage);
				}
				else {
					lWidth = 0;
					cWidth = 0;
					rWidth = rWidth - shortage + original + 2 * hSpacer;
				}
			}

			// Maybe need to shrink interline space
			// [dtc 11-Apr-2007]  This used to be (height - maxHeight) / (1 + count), which left buttons off the bottom.
			int interlineSpace = Math.min(vSpacer, (height - count * maxHeight) / (1 + count));
			// Better not be negative
			interlineSpace = Math.max(interlineSpace, 0);

			int theCenterOffset = theLeftOffset + lWidth;
			int theRightOffset = theCenterOffset + cWidth;
			if (lWidth > 0) {
				theCenterOffset += hSpacer;
				theRightOffset += hSpacer;
			}
			if (cWidth > 0) {
				theRightOffset += hSpacer;
			}
			for (int i = 0; i < count; ++i) {
				Object l = null, c = null, r = null;
				if (i < leftList.size()) {l = leftList.get(i);}
				if (i < centerList.size()) {c = centerList.get(i);}
				if (i < rightList.size()) {r = rightList.get(i);};
				// int yOffset = (int)(i * (maxHeight + interlineSpace) + 0.5 * interlineSpace);
				int yOffset = (int)(i * (maxHeight + interlineSpace) + interlineSpace);

				if (l instanceof JComponent) {
					int preferredWidth = ((JComponent)l).getPreferredSize().width;
					// System.err.println("The preferred left width: " + preferredWidth);
					if (r != null) {
						preferredWidth = Math.min(preferredWidth, theRightOffset - theLeftOffset - hSpacer);
					}
					if (c != null) {
						preferredWidth = Math.min(preferredWidth, theCenterOffset - theLeftOffset - hSpacer);
					}
					// System.err.println("The left width: " + preferredWidth);
					((JComponent) l).setBounds(theLeftOffset + getInsets().left, yOffset + getInsets().top, preferredWidth, maxHeight);
				}
				if (c instanceof JComponent) {
					int preferredWidth = ((JComponent)c).getPreferredSize().width;
					// System.err.println("The preferred center width: " + preferredWidth);
					if (r != null) {
						preferredWidth = Math.min(preferredWidth, theRightOffset - theCenterOffset - hSpacer);
					}
					// System.err.println("The left center: " + preferredWidth);
					((JComponent) c).setBounds(theCenterOffset + getInsets().left, yOffset + getInsets().top, preferredWidth, maxHeight);
				}
				if (r instanceof JComponent) {
					int preferredWidth = ((JComponent)r).getPreferredSize().width;
					// System.err.println("The preferred right width: " + preferredWidth);
					preferredWidth = Math.min(preferredWidth, width - theRightOffset - theRightSpace);
					// System.err.println("The right width: " + preferredWidth);
					((JComponent) r).setBounds(theRightOffset + getInsets().left, yOffset + getInsets().top, preferredWidth, maxHeight);
				}
			}
		}
	}

	public static void main(String[] ignore) {
		TitledJPanel titledPanel = new TitledJPanel("Hello world");
		ParallelJPanel panel = new ParallelJPanel();
		panel.addLeft(new JLabel("Hello world, how are you?"));
		panel.addCenter(null);
		panel.addRight(null);
		panel.addLeft(null);
		panel.addCenter(null);
		panel.addRight(new JTextField("Look over here"));
		panel.addLeft(new JLabel("Hello"));
		panel.addCenter(new JLabel("my"));
		panel.addRight(new JLabel("friend"));
		panel.addLeft(null);
		panel.addCenter(new JTextField("Look over here"));
		panel.setBackground(Color.YELLOW);
		panel.setOpaque(true);
		System.err.println("Preferred left " + panel.getPreferredSize());

		ParallelJPanel panel2 = new ParallelJPanel();
		panel2.addLeft("x");
		panel2.addRight("This is a nice long test");
		panel2.setBackground(Color.RED);
		panel2.setOpaque(true);
		System.err.println("Preferred right " + panel2.getPreferredSize());
		// HorizontalGridPanel combo = new HorizontalGridPanel();
		HorizontalGridPanel combo = new HorizontalGridPanel();
		combo.add(panel);
		combo.add(panel2);
		JFrame frame = new JFrame();
		titledPanel.setMainPanel(combo);
		frame.getContentPane().add(titledPanel);
		frame.pack();
		frame.setVisible(true);

	}


}
