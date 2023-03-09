package connections;

import javax.swing.AbstractButton;

/*
 * Created on Apr 5, 2009
 * @author phw
 */

public class SelectorWiredBox extends AbstractWiredBox {
	
	public static final String A = "portA";
	public static final String B = "portB";
	public static final String C = "portC";
	
	private AbstractButton buttonA, buttonB, buttonC;
	
	public SelectorWiredBox() {
		Connections.getPorts(this).addSignalProcessor(A, "processA");
		Connections.getPorts(this).addSignalProcessor(B, "processB");
		Connections.getPorts(this).addSignalProcessor(C, "processC");	}
	
	public void addButtonA (AbstractButton b) {
		buttonA = b;
	}
	
	public void addButtonB (AbstractButton b) {
		buttonB = b;
	}
	public void addButtonC (AbstractButton b) {
		buttonC = b;
	}

	public void processA(Object signal) {
		if (buttonA != null && buttonA.isSelected()) {
				Connections.getPorts(this).transmit(signal);
		}
	}
	public void processB(Object signal) {
		if (buttonB != null && buttonB.isSelected()) {
				Connections.getPorts(this).transmit(signal);
		}
	}
	public void processC(Object signal) {
		if (buttonC != null && buttonC.isSelected()) {
				Connections.getPorts(this).transmit(signal);
		}
	}

}
