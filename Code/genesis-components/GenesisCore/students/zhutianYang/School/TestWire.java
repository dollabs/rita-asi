/**
 * 
 * A practice on a Java mechanism that enables you to 
 *   connect system modules to one another using a box-and-wire metaphor.
 *   connect distinct instances of a classes together
 * 
>>>  Test instance named source transmits Hello world 
>>>  Test instance named destination receives Hello world 
 * 
 */
package zhutianYang.School;

import connections.AbstractWiredBox;
import connections.WiredBox;
import connections.signals.Signal;
import connections.Connections; //
import utils.Mark;

public class TestWire extends AbstractWiredBox {
	
	public static final String MY_OUTPUT = "controlling";
	public static final String MY_INPUT = "controlled";
	public static final String MIDDLE = "middle";
	
	public static boolean IS_NAME_PORT = true;
	public static boolean IS_FAN_OUT = true;
	public static boolean IS_COMBINED_SIGNAL = true;
	
	public TestWire(String name) {
		super(name);
		
		if (!IS_FAN_OUT && IS_NAME_PORT) {
			  Connections.getPorts(this).addSignalProcessor(MY_INPUT, "processInput");
		  } else {
			  Connections.getPorts(this).addSignalProcessor("processInput");
		  }
		
	}
	
	public void demonstrate() {
		String signal = "Hello world";
		  Mark.say(getName(), "transmits", signal);
		  
		  if (IS_COMBINED_SIGNAL) {
		       Mark.say(getName(), "transmits combined", signal, signal);
		       Connections.getPorts(this).transmit(new Signal(signal, signal));
		  }
		  
		  if (!IS_FAN_OUT && IS_NAME_PORT) {
			  Connections.getPorts(this).transmit(MY_OUTPUT, signal);
		  } else {
			  // That of the symbol will be sent to the input port of instance destination
			  Connections.getPorts(this).transmit(signal);
		  }
	}
	
	  
	public void processInput(Object input) {
		if (input instanceof Signal) {
	         Signal signal = (Signal) input;
	         String x = signal.get(0, String.class);
	         String y = signal.get(1, String.class);
	         Mark.say(getName(), "receives", x, y);
		} else {
			Mark.say(getName(), "receives", input);
			Connections.getPorts(this).transmit(input);
		}
		
	}
	
	public static void main(String[] args) {
		
		  TestWire source = new TestWire(MY_OUTPUT);
		  TestWire destination = new TestWire(MY_INPUT);
		  
		  if (IS_FAN_OUT) {
			  TestWire middle = new TestWire(MIDDLE);
			  Connections.wire(source, middle);
			  Connections.wire(middle, destination);
		  } else {
			  if (IS_NAME_PORT) {
				  Connections.wire(MY_OUTPUT, source, MY_INPUT, destination);
			  } else {
				  Connections.wire(source, destination);
			  }
		  }
		  source.demonstrate();
	}
	
	// if implements WiredBox
//	public String getName() {
//		return "Another test instance";
//	}

}


  