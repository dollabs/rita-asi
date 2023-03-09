/**
 * 
 */
package zhutianYang.Documentation;

import connections.AbstractWiredBox;
import connections.Connections;
import connections.WiredBox;
import connections.signals.Signal;
import utils.Mark;

/**
 * @author z
 *
 */
public class SourceClass implements WiredBox {
	
	public static final String OUTPUT_PORT_1 = "port 1 output";
	public static final String OUTPUT_PORT_2 = "port 2 output";
	
	public void sayHello() {
		String signal = "Hello world";
		Mark.say("Source transmits", signal);
		Connections.getPorts(this).transmit(signal);
//		Connections.getPorts(this).transmit(OUTPUT_PORT_1, signal);
	}
	
	public void sayHelloAgain() {
		String signal = "Hello world again";
		Mark.say("Source transmits again", signal);
		Connections.getPorts(this).transmit(OUTPUT_PORT_2, signal);
	}
	
	public void sayManyThings() {
		String string = "Hello world";
		int array = 11;
		Object signal = new Signal(string, array);
		Mark.say("Source transmits many things", string, array);
		Mark.say("Source transmits", signal);
		Connections.getPorts(this).transmit(signal);
	}

	@Override
	public String getName() {
		return null;
	}

}
