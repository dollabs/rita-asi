package gui;

import javax.swing.*;

/*
 * Created on Jul 8, 2012
 * @author phw
 */

public class JTransparentCheckBox extends JCheckBox {

	public JTransparentCheckBox() {
		setOpaque(false);
	}

	public JTransparentCheckBox(Icon icon) {
		super(icon);
		setOpaque(false);
	}

	public JTransparentCheckBox(String text) {
		super(text);
		setOpaque(false);
	}

	public JTransparentCheckBox(Action a) {
		super(a);
		setOpaque(false);
	}

	public JTransparentCheckBox(Icon icon, boolean selected) {
		super(icon, selected);
		setOpaque(false);
	}

	public JTransparentCheckBox(String text, boolean selected) {
		super(text, selected);
		setOpaque(false);
	}

	public JTransparentCheckBox(String text, Icon icon) {
		super(text, icon);
		setOpaque(false);
	}

	public JTransparentCheckBox(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
		setOpaque(false);
	}

}
