package gui;

import javax.swing.JLabel;

import connections.Connections;

public class WiredLabelPanel extends WiredPanel {
	JLabel text = new JLabel();

	public WiredLabelPanel() {
		Connections.getPorts(this).addSignalProcessor("display");
		this.add(text);
	}

	public void display(Object signal) {
		text.setText(signal.toString());
	}
}