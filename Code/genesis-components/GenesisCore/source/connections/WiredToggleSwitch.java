package connections;


/*
 * Created on Aug 9, 2009
 * @author phw
 */

public class WiredToggleSwitch extends WiredOnOffSwitch {



	public WiredToggleSwitch(String name) {
		super(name);
		Connections.getPorts(this).addSignalProcessor(Port.INPUT, "processWiredToggleSwitch");
	}

	public void processWiredToggleSwitch(Object signal) {
		if (isSelected()) {
			Connections.getPorts(this).transmit(Port.UP, signal);
		}
		else {
			Connections.getPorts(this).transmit(Port.DOWN, signal);
		}
	}
	
	public String toString() {
		return "<Wired toggle switch " + getName() + ">";
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8619692450571977036L;
}
