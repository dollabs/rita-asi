package gui;

import utils.Mark;
import connections.*;

/*
 * Created on Jan 24, 2010
 * @author phw
 */

public class TabbedTextAdapter extends WiredBlinkingBox {

	public TabbedTextAdapter(String tabName, TabbedTextViewer viewer) {
		super(tabName);
		Connections.wire(TabbedTextViewer.TAB, this, TabbedTextViewer.TAB, viewer);
		Connections.wire(this, viewer);
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object o) {
		blink();
		Connections.getPorts(this).transmit(TabbedTextViewer.TAB, this.getName());
		Connections.getPorts(this).transmit(o);
	}

}
