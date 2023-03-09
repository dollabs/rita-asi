package connections;

import java.util.ArrayList;

import javax.swing.AbstractButton;


/*
 * Created on Apr 5, 2009
 * @author phw
 */

public class WiredDistributorBox extends AbstractWiredBox {
	
	ArrayList<AbstractButton> buttons = new ArrayList<AbstractButton>();

	public WiredDistributorBox() {
	Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object signal) {
		for (AbstractButton button : buttons) {
			if (button.isSelected()) {
				Connections.getPorts(this).transmit(button.getText(), signal);
			}
		}
	}
	
	public void addPort(AbstractButton b) {
		buttons.add(b);
	}
	

}
