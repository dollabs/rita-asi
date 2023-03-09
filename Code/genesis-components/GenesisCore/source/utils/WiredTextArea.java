package utils;

import java.util.ArrayList;

import javax.swing.JTextArea;

import connections.*;

/*
 * Created on Jun 2, 2013
 * @author phw
 */

public class WiredTextArea extends JTextArea implements WiredBox {

	public final static String TRANSMIT = "transmit";

	public WiredTextArea() {
		Connections.getPorts(this).addSignalProcessor("process");
		Connections.getPorts(this).addSignalProcessor(TRANSMIT, "transmit");
	}

	public void transmit(Object o) {
		Connections.getPorts(this).transmit(TRANSMIT, this.getText());
	}

	public void process(Object o) {
		if (o instanceof ArrayList) {
			String text = "";
			for (Object s : (ArrayList) o) {
				text += s.toString() + "\n";
			}
			setText(text);
		}
		else if (o instanceof String) {
			this.setText((String) o);
		}
	}
}
