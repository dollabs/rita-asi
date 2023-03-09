package gui.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JComponent;

/*
 * A JPanel layed out with a horizontal grid with size determined by sizes of components.
 * @copyright Ascent Technology, Inc, 2005
 */
public class HorizontalGridPanel extends SpecialJPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1423772263073479166L;

	public HorizontalGridPanel() {
		setLayout(new GridLayout(1, 0));
	}

	public Dimension getPreferredSize() {
		Component[] components = getComponents();
		int width = 0, maxWidth = 0, maxHeight = 0;
		for (int i = 0; i < components.length; ++i) {
			if (components[i] instanceof JComponent) {
				Dimension d = ((JComponent)(components[i])).getPreferredSize();
				width += d.width;
				maxWidth = Math.max(maxWidth, d.width);
				maxHeight = Math.max(maxHeight, d.height);
			}
		}
		return new Dimension(components.length * maxWidth, maxHeight);
	}


}
