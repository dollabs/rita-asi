package gui;
import frames.*;
import frames.Frame;
import frames.entities.Entity;

import java.awt.*;
import java.rmi.server.UID;

import connections.*;
/**
 * Generic Viewer: Use this as a GUI element when you're not sure what representation needs to be viewed. This
 * determines the correct viewer -- falling back on NewFrameViewer -- and displays it in its place.
 * 
 * @author mtklein
 */
@SuppressWarnings("serial")
public class GenericViewer extends WiredPanel {
	public GenericViewer() {
		Connections.getPorts(this).addSignalProcessor("onSignal");
		this.setLayout(new GridLayout(1, 1));
		this.setBackground(Color.WHITE);
		this.setOpaque(true);
	}
	public void onSignal(Object signal) {
		if (signal instanceof Entity) {
			this.removeAll();
			Entity thing = (Entity) signal;
			WiredViewer wiredPanel;
			Frame frame = SmartFrameFactory.translate(thing);
			if (frame == null) {
				wiredPanel = new NewFrameViewer();
			} else {
				wiredPanel = frame.getThingViewer();
			}
			this.add(wiredPanel);
			String port = new UID().toString();
			Connections.wire(port, this, wiredPanel);
			Connections.getPorts(this).transmit(port, thing);
			this.revalidate();
			this.repaint();
		}
	}
}
