package gui;
import java.awt.*;
import java.rmi.server.UID;


import connections.*;
import frames.entities.Entity;
import frames.entities.Sequence;
/**
 * View an eventSequence as a row of other viewers
 * 
 * @author mtklein
 */
@SuppressWarnings("serial")
public class EventViewer extends WiredViewer{
	public EventViewer() {
		Connections.getPorts(this).addSignalProcessor("view");
		this.setLayout(new GridLayout(1, 0));
		this.setBackground(Color.WHITE);
		this.setOpaque(true);
	}
	public void view(Object signal) {
		if (signal instanceof Sequence) {
			this.removeAll();
			Sequence event = (Sequence) signal;
			for (Entity kid : event.getElements()) {
				WiredPanel wiredPanel = new GenericViewer();
				this.add(wiredPanel);
				UID uid = new UID();
				Connections.wire(uid.toString(), this, wiredPanel);
				Connections.getPorts(this).transmit(uid.toString(), kid);
			}
			this.revalidate();
			this.repaint();
		}
	}
}
