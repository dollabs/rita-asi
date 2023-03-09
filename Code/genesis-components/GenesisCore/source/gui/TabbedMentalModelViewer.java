package gui;

import javax.swing.JTabbedPane;

import connections.*;
import constants.Markers;

/*
 * Created on Jun 20, 2012
 * @author phw
 */

public class TabbedMentalModelViewer extends JTabbedPane implements WiredBox {

	public TabbedMentalModelViewer() {
		Connections.getPorts(this).addSignalProcessor(Markers.RESET, "reset");
	}

	public void reset(Object o) {
		if (o == Markers.RESET) {
			this.removeAll();
		}
	}

}
