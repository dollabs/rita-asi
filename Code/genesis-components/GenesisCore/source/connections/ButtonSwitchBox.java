/**
 * 
 */
package connections;

import javax.swing.AbstractButton;

/**
 * Routes wire input/output based on the state of a particular JCheckBox. An input connected to ON/OFF will only be
 * accepted if the checkbox is checked/unchecked, respectively. Similarly an output connected to ON/OFF will only be
 * signaled if the checkbox is checked/unchecked. Inputs/outputs connected to the default port will always be
 * accepted/signaled.
 * 
 * @author harold
 */
public class ButtonSwitchBox extends AbstractWiredBox {
	public static final String ON = "on", OFF = "off";

	private final AbstractButton button;

	public ButtonSwitchBox(final AbstractButton button) {
		super("Button switch box");
		this.button = button;

		Connections.getPorts(this).addSignalProcessor("input");
		Connections.getPorts(this).addSignalProcessor(ON, "onInput");
		Connections.getPorts(this).addSignalProcessor(OFF, "offInput");
	}

	public void input(final Object o) {
		Connections.getPorts(this).transmit(o);
		Connections.getPorts(this).transmit(button.isSelected() ? ON : OFF, o);
	}

	public void onInput(final Object o) {
		if (button.isSelected()) {
			Connections.getPorts(this).transmit(o);
			Connections.getPorts(this).transmit(ON, o);
		}
	}

	public void offInput(final Object o) {
		if (!button.isSelected()) {
			Connections.getPorts(this).transmit(o);
			Connections.getPorts(this).transmit(OFF, o);
		}
	}

}
