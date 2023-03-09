/**
 * 
 */
package zhutianYang.Documentation;

import connections.AbstractWiredBox;
import connections.Connections;

/**
 * @author z
 *
 */
public class InterimClass extends AbstractWiredBox {
	
	public InterimClass() {
		Connections.getPorts(this).addSignalProcessor("process");
	}
	
	public void process(Object input) {
		input = (Object) ((String) input + " (processed)");
		Connections.getPorts(this).transmit(input);
	}
	
}
