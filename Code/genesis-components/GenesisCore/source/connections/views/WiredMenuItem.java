package connections.views;

import java.awt.event.*;

import javax.swing.JMenuItem;


import connections.*;

/*
 * Created on Dec 31, 2011
 * @author phw
 */

@SuppressWarnings("serial")
public class WiredMenuItem extends JMenuItem implements WiredBox, ActionListener{
	
	private String signal = "Clicked";

	public WiredMenuItem(String string) {
	    super(string);
	    addActionListener(this);
    }

	@Override
    public void actionPerformed(ActionEvent e) {
	    Connections.getPorts(this).transmit(signal);
    }

}
