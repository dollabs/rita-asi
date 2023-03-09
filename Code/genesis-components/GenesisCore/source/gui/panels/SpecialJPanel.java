package gui.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JPanel;

/*
 * When recognized by PanelColumn (or PanelRow if implemented), subclasses of this panel 
 * will shrink to their preferred size, leaving other panels containing tables and such 
 * to expand as much as possible.
 * @copyright Ascent Technology, Inc, 2005
 */
public class SpecialJPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4442656082338946934L;

	private static boolean showAdvice = false;

	private boolean hasAdvice = false;

	public static boolean showNames = false;

	protected TitledJPanelAdvice advicePanel;

	private String advice;

	public SpecialJPanel() {
		super();
	}

	public SpecialJPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public SpecialJPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public SpecialJPanel(LayoutManager layout) {
		super(layout);
	}

	protected Dimension getMySize(Component component) {
		Dimension p = component.getPreferredSize();
		if (!(component instanceof SpecialJPanel)) {
			Dimension m = component.getMinimumSize();
			if (m != null) {
				return m;
			}
		}
		if (p != null) {
			return p;
		}
		return new Dimension(100, 100);
	}

	public void setAdvice(String advice) {
		if (advice == null || "".equals(advice.trim())) {
			hasAdvice = false;
		}
		else {
			hasAdvice = true;
		}
		this.advice = advice;
	}

	public TitledJPanelAdvice getAdvicePanel() {
		if (advicePanel == null) {
			advicePanel = new TitledJPanelAdvice();
		}
		String s = "";
		if (showNames) {
			s += "<b><font color = \"red\">";
			if (this.getName() != null) {
				s += "I am ";
			}
			else {
				s += "My class is ";
			}
			s += getAName(this);
			s += ".</font><b>";
			String subNames = getComponentNames(this, 1);
			if (!subNames.equals("")) {
				s += "  I contain " + subNames;
				s += ". ";
			}

		}
		else {
			s += getAdvice();
		}
		advicePanel.setText(s);
		return advicePanel;
	}

	private String getAdvice() {
		return advice;
	}

	private String getAName(JPanel component) {
		String name = component.getName();
		String result = "";
		if (name != null) {
			result += name;
		}
		else {
			result += component.getClass().getName();
		}
		return result;
	}

	private String getComponentNames(JPanel component, int level) {
		String result = "";
		Component[] components = component.getComponents();
		for (int i = 0; i < components.length; ++i) {
			// System.err.println(i);
			String name = components[i].getName();
			if (name != null) {
				result += " (" + name + " at level " + level + ")";
			}
			else {

			}
			if (components[i] instanceof JPanel) {
				JPanel panel = (JPanel) components[i];
				result += getComponentNames(panel, level + 1);
			}
		}
		return result;
	}
	
	public static boolean isShowAdvice() {
		return showAdvice;
	}

	public static boolean isShowNames() {
		return showNames;
	}
	
	public boolean hasAdvice() {
		return hasAdvice;
	}

	public static void toggleShowNames() {
		SpecialJPanel.showNames = !SpecialJPanel.showNames;
	}

	public static void setShowNames(boolean showName) {
		SpecialJPanel.showNames = showName;
	}
	
	public static void setShowAdvice(boolean b) {
		showAdvice = b;
	}



}
