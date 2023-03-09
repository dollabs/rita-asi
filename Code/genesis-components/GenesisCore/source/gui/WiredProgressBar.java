package gui;

import javax.swing.JProgressBar;

import connections.*;
import connections.signals.BetterSignal;

/*
 * Created on Jul 4, 2012
 * @author phw
 */

public class WiredProgressBar extends JProgressBar implements WiredBox {

	public WiredProgressBar() {
		this.setMinimum(0);
		this.setStringPainted(true);
		Connections.getPorts(this).addSignalProcessor("process");
	}
	
	public void process(Object o) {
		if (!(o instanceof BetterSignal)) {
			return;
		}
		BetterSignal signal = (BetterSignal)o;
		
		this.setMaximum(signal.get(1, Integer.class));
		this.setValue(signal.get(0, Integer.class));
	}
}
