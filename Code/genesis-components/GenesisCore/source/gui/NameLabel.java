package gui;

import java.awt.Color;

import utils.Punctuator;
import utils.JHtmlLabel;
import connections.*;

/*
 * Created on Dec 8, 2012
 * @author phw
 */

public class NameLabel extends JHtmlLabel implements WiredBox {
	public NameLabel(String... x) {
		super(x);
		super.setText("");
		// setBackground(Color.CYAN);
		// setOpaque(true);
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object input) {
		super.setText(Punctuator.conditionName(input.toString()));
	}
}
