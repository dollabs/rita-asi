/**
 * 
 */
package zhutianYang.Documentation;

import connections.AbstractWiredBox;
import connections.Connections;
import connections.signals.Signal;
import utils.Mark;

/**
 * @author z
 *
 */
public class DestinationClass extends AbstractWiredBox {

	public static final String INPUT_PORT_1 = "port 1 input";
	public static final String INPUT_PORT_2 = "port 2 input";
	
	public DestinationClass() {
		Connections.getPorts(this).addSignalProcessor("replyHello");
//		Connections.getPorts(this).addSignalProcessor(INPUT_PORT_1, "replyHello");
//		Connections.getPorts(this).addSignalProcessor(INPUT_PORT_2, "replyHelloAgain");
		Connections.getPorts(this).addSignalProcessor("replyManyThings");
	}
	
	public void replyHello(Object input) {
		Mark.say("Destination receives", input);
	}
	public void replyHelloAgain(Object input) {
		Mark.say("Destination receives again", input);
	}
	public void replyManyThings(Object input) {
		if (input instanceof Signal) {
			Signal signal = (Signal) input;
			String x = signal.get(0, String.class);
			int y = signal.get(1, Integer.class);
			Mark.say("Destination receives many things", x, y);
		}
	}
}
