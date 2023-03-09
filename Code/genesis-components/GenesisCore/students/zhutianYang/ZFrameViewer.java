package zhutianYang;

import gui.NegatableJPanel;

import java.awt.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.*;

import utils.Mark;
import connections.WiredBox;
import frames.classic.FrameBundle;
import frames.classic.MultilineToolTip;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;

public class ZFrameViewer extends JScrollPane implements WiredBox {
	// ============= CONSTANTS ============
	public static final int SCROLL_ALWAYS = 2;

	public static final int SCROLL_AS_NEEDED = 1;

	public static final int SCROLL_NEVER = 0;

	protected int index = 0;

	protected String title;

	protected Vector panels = new Vector();

	protected GridLayout layout;

	protected TitledBorder border = new TitledBorder("");

	protected int id = -1;

	protected NegatableJPanel bodyPanel;

	public NegatableJPanel getBodyPanel() {
		if (bodyPanel == null) {
			bodyPanel = new NegatableJPanel();
		}
		return bodyPanel;
	}

	protected int scrollMode;

	// =============== CONSTRUCTORS =============
	public ZFrameViewer() {
		layout = new GridLayout(1, 0);
		getBodyPanel().setLayout(layout);
		this.setOpaque(false);
		getBodyPanel().setOpaque(true);
		getBodyPanel().setBackground(Color.WHITE);
		getViewport().setOpaque(true);
		setViewportView(getBodyPanel());
		setAtPreferredSize();
		setScrollable(SCROLL_NEVER);
	}

	public ZFrameViewer(String t) {
		this();
		title = t;
		border.setTitle(title);
		setBorder(border);
	}

	public ZFrameViewer(String t, int scrollable) {
		this(t);
		setScrollable(scrollable);
	}

	// Added so you can change the border color -- MAF.26.Jan.04
	public ZFrameViewer(String t, Color color) {
		this();
		title = t;
		border = new TitledBorder(new LineBorder(color, 2), title);
		setBorder(border);
	}

	// ================= METHODS =================
	
	public void setScrollable(int scrollMode) {
		if (scrollMode == SCROLL_ALWAYS) {
			setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		}
		else if (scrollMode == SCROLL_AS_NEEDED) {
			setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}
		else if (scrollMode == SCROLL_NEVER) {
			setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		else {
			throw new IllegalArgumentException("setScrollable called with bad argument -- expected SCROLL_ALWAYS, SCROLL_AS_NEEDED, or SCROLL_NEVER");
		}
		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		this.scrollMode = scrollMode;
	}

	public Vector getPanels() {
		return panels;
	}

	public void setID(int i) {
		id = i;
	}

	public int getID() {
		return id;
	}

	public void setInput(Entity t) {
		setInput(ZEntityToViewerTranslator.translate(t));
	}

	public void setInput(FrameBundle b) {
		setInput(index, b);
		revalidate();
		repaint();
	}

	public void setStory(Sequence s) {
		Vector<Entity> v = s.getElements();
		Vector<FrameBundle> bundles = new Vector<FrameBundle>();
		for (Entity t : v) {
			bundles.add(ZEntityToViewerTranslator.translate(t, ZEntityToViewerTranslator.SHOW_NO_THREADS));
		}
		setInputVector(bundles);
	}

	public void setInputVector(Vector v) {
		clearData();
		if (v == null) {
			return;
		}
		for (int i = 0; i < v.size(); ++i) {
			if (v.elementAt(i) instanceof FrameBundle || v.elementAt(i) instanceof Entity) {
			}
			else {
				continue;
			}
		}
		for (int i = 0; i < v.size(); ++i) {
			if (v.elementAt(i) instanceof FrameBundle) {
				FrameBundle bundle = (FrameBundle) (v.elementAt(i));
				setInput(i, bundle);
			}
			else if (v.elementAt(i) instanceof Entity) {
				Entity t = (Entity) (v.elementAt(i));
				FrameBundle bundle = ZEntityToViewerTranslator.translate(t);
				setInput(i, bundle);
			}
		}
		revalidate();
		repaint(); // ISE
	}

	private void setInput(int panel, FrameBundle b) {
		if (panel >= panels.size()) {
			for (int i = panels.size(); i <= panel; ++i) {
				ZBasicFrameViewer bfv = new ZBasicFrameViewer();
				panels.add(bfv);
				getBodyPanel().add(bfv);
				revalidate();
				repaint();
			}
		}
		ZBasicFrameViewer target = (ZBasicFrameViewer) (panels.elementAt(panel));

		target.setInput(b);
		repaint(); // ISE
	}

	/*
	 * Sets title variable, does not set border.
	 */

	public void setTitle(String t) {
		title = t;
	}

	public String getTitle() {
		return title;
	}

	public void setBorder(String t) {
		setTitle(t);
		getBodyPanel().setBorder(new TitledBorder(title));
	}

	public void setBorderColor(Color color) {
		border = new TitledBorder(new LineBorder(color, 2), title);
		getBodyPanel().setBorder(border);
	}

	public void clearData() {
		index = 0;
		panels.clear();
		getBodyPanel().removeAll();
		repaint();
	}

	public void setAtPreferredSize() {
		ZBasicFrameViewer bfv;
		Dimension size;
		if (panels == null) return;
		for (int i = 0; i < panels.size(); i++) {
			bfv = (ZBasicFrameViewer) panels.get(i);
			size = bfv.getPreferredSize();
			size.height = Math.min(size.height, getViewportBorderBounds().height);
			bfv.setSize(size);
		}
		Dimension preferredSize = getPreferredSize();
		preferredSize.height = Math.min(preferredSize.height, getViewportBorderBounds().height);
		getBodyPanel().setSize(preferredSize);

		getBodyPanel().repaint();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#reshape(int, int, int, int)
	 */
	public void reshape(int x, int y, int w, int h) {
		setAtPreferredSize();
		super.reshape(x, y, w, h);
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#repaint()
	 */
	public void repaint() {
		setAtPreferredSize();
		super.repaint();
	}

	public Dimension getPreferredSize() {
		int prefWidth = 0;
		int prefHeight = 0;
		ZBasicFrameViewer bfv;
		for (int i = 0; i < getPanels().size(); i++) {
			bfv = (ZBasicFrameViewer) panels.get(i);
			prefWidth += bfv.getPreferredSize().width;
			prefHeight = Math.max(prefHeight, bfv.getPreferredSize().height);
		}
		if (getBodyPanel().getBorder() != null) {
			Insets insets = getBodyPanel().getBorder().getBorderInsets(this);
			prefWidth = prefWidth + insets.left + insets.right;
			// prefWidth = prefWidth + layout.getHgap()*layout.getColumns();
		}

		return new Dimension(prefWidth, prefHeight);
	}

	/**
	 * Use multi-line tool tips
	 */
	public JToolTip createToolTip() {
		return new MultilineToolTip();
	}

	public static void main(String[] ignore) {
		JFrame frame = new JFrame();
		ZFrameViewer view = new ZFrameViewer();
		frame.getContentPane().add(view);
		Entity t = new Entity("Patrick");
		Function d = new Function(t);
		d.addType("Foo");
		d.addType("Bar");
		Relation r = new Relation(d, d);
		r.addType("X");
		r.addType("Y");
		r.addType("Z");
		view.setInput(r);
		frame.setBounds(0, 0, 200, 200);
		frame.setVisible(true);
	}

}
