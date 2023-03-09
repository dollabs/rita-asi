package connections;

import java.awt.event.*;

import javax.swing.JButton;

/*
 * Created on Mar 27, 2009
 * @author phw
 */

public class JButtonBox extends JButton implements WiredBox, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4407622739728274352L;

	public static final String ENABLE = "enable";

	public static final String VISIBLE = "visible";

	public static String output = "pushed";

	public JButtonBox(String string) {
		super(string);
		addActionListener(this);
		Connections.getPorts(this).addSignalProcessor(ENABLE, "enable");
		Connections.getPorts(this).addSignalProcessor(VISIBLE, "visible");
	}

	public void enable(Object object) {
		// System.out.println("Enable receiving !!!!!!!!!!!!!!!!!!!!!!! " + object);
		if (object == Boolean.TRUE) {
			setEnabled(true);
		}
		else if (object == Boolean.FALSE) {
			setEnabled(false);
		}
	}
	
	public void visible(Object object) {
		// System.out.println("Enable visibility !!!!!!!!!!!!!!!!!!!!!!! " + object);
		if (object == Boolean.TRUE) {
			setVisible(true);
		}
		else if (object == Boolean.FALSE) {
			setVisible(false);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Connections.getPorts(this).transmit(output);
	}

}
