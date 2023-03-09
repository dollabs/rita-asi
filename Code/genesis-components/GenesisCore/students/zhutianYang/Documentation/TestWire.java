/**
 * 
 */
package zhutianYang.Documentation;

import connections.Connections;
import utils.Mark;

/**
 * @author z
 *
 */
public class TestWire {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		SourceClass source = new SourceClass();
		DestinationClass destination = new DestinationClass();
		
		// Single output port and Single input port
//		Connections.wire(source, destination);
//		source.sayHello();

		
		// Interim class
//		InterimClass interim = new InterimClass();
//		Connections.wire(source, interim);
//		Connections.wire(interim, destination);
//		source.sayHello();
		
		
		// Multiple output port and Single input port
//		Connections.wire(SourceClass.OUTPUT_PORT_1, source, DestinationClass.INPUT_PORT_1, destination);
//		Connections.wire(SourceClass.OUTPUT_PORT_2, source, DestinationClass.INPUT_PORT_2, destination);
//		source.sayHello();
//		source.sayHelloAgain();
		
		
		// Multiple signals
		Connections.wire(source, destination);
		source.sayManyThings();
	}

}
