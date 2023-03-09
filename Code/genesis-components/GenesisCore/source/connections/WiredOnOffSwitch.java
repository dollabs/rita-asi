package connections;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

import javax.swing.JCheckBox;
import javax.swing.event.*;



/*
 * Created on Nov 9, 2008
 * @author phw
 */

public class WiredOnOffSwitch extends JCheckBox implements WiredBox {

	private AtomicBoolean trackingValue;

	protected static String key = Connections.class.getCanonicalName();
	
	public WiredOnOffSwitch(String name, AtomicBoolean atomicBoolean) {
		super(name, Preferences.userRoot().getBoolean(key + name, true));
		setName(name);
		Connections.getPorts(this).addSignalProcessor(Port.INPUT, "processWiredOnOffSwitch");
		trackingValue = atomicBoolean;
		if (trackingValue != null) {
			trackingValue.set(this.isSelected());
		}
		addChangeListener(new MyPropertyChangeListener(this));
		setOpaque(false);
	}
	
	public WiredOnOffSwitch(String name) {
		this(name, false);
	}
	
	public WiredOnOffSwitch(String name, boolean state) {
		super(name);
		setName(name);
		setOpaque(false);
		this.setSelected(state);
	}

	public void processWiredOnOffSwitch(Object signal) {
		if (isSelected()) {
			Connections.getPorts(this).transmit(Port.OUTPUT, signal);
		}
	}

	protected class MyPropertyChangeListener implements ChangeListener {

		WiredOnOffSwitch button;

		public MyPropertyChangeListener(WiredOnOffSwitch button) {
			this.button = button;
		}

		public void stateChanged(ChangeEvent e) {
			String name = button.getText();
			Preferences.userRoot().putBoolean(key + name, button.isSelected());
			if (trackingValue != null) {
				trackingValue.set(button.isSelected());
			}
		}

	}

	public String toString() {
		return "<Wired on-off switch " + getName() + ">";
	}

	public void open() {
		setEnabled(true);
	}

	public void close() {
		setEnabled(false);
	}
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 570145500004368561L;
	
}
