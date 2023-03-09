/* Filename: Pluggable.java
 * Author: M. A. Finlayson
 * Format: Java 2 v.1.4.2
 * Date created: Oct 28, 2003
 */

package connections;


/**
 * Allows a class to be connected with wires without extending the Connectable class.
 * Requires that there be a method which returns a connectable object.  Usually this is
 * implemented with the following code:
 * 
 * <tt>
 * 	public Connectable getPlug(){
 *		if(plug == null){
 *			plug = new LocalPlug();
 *		}
 *		return plug;
 *	}
 * </tt>
 * 
 * where LocalPlug is a private inner class which extends the Connectable class and implements
 * the setInput() and getOutput() methods.
 * 
 * @author M. A. Finlayson
 * @since JDK 1.4
 * @version 2.0
 */

//Example code for Pluggable implementation shown below:
//
//public static String SOME_INPUT_PORT = "some_input_port";
//public static String SOME_OUTPUT_PORT = "some_output_port";
//
//private LocalPlug plug;
//
//public Connectable getPlug(){
//	if(plug == null){
//		  plug = new LocalPlug();
//	}
//	return plug;
//}
//
//private class LocalPlug extends Connectable {
//	public void setInput(Object input, Object port){
//		if(port == SOME_INPUT_PORT){
//			// setInput code here
//		} else {
//			warning("Input port " + port.toString() + " not recognized.");
//		}
//	}
//		
//	public Object getOutput(Object port){
//		if(port == SOME_OUTPUT_PORT){
//			// getOutput code here
//			return null;
//		} else {
//			warning("Output port " + port.toString() + " not recognized.");
//			return null;
//		}
//	}
//}


public interface Pluggable {
	
	/**
	 * Returns an object which can be connected via the Wires methodology.
	 * 
	 * @return plug An object which extends the Connectable class and implements the input/output functions
	 * for the object implementing this interface.
	 */
	public Connectable getPlug();
}


