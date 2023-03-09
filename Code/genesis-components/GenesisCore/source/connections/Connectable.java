package connections;

import java.util.*;

import connections.Wire;


abstract public class Connectable extends Observable implements Ported {

    public Connectable() {
        notifyConnectableCreationListeners();
    }
    
 /** If no port is specified, assumes standard input port.
  * 
  * @author M.A. Finlayson
  * @since Jan 19, 2004; JDK 1.4.2
  */
 public void setInput (Object input) {
	setInput(input, Wire.INPUT);
 }
 
 /** If no port is specified, assumes standard output port.
  * 
  * @author M.A. Finlayson
  * @since Jan 19, 2004; JDK 1.4.2
  */
 public Object getOutput () {
  return getOutput(Wire.OUTPUT);
 }

 /*
  * Implement method that sets input on wire destination.
  */
 public abstract void setInput (Object input, Object port) ;

 /*
  * Implement method that gets output from wire source.
  */
 public abstract Object getOutput (Object port) ;

 /**
  * Transmit informaton over default port.
  */
 public void transmit () {
  transmit(Wire.OUTPUT);
 }
 /**
  * Transmit informaton over wire, with specified port.
  */
 public void transmit (Object port) {
  setChanged();
  notifyObservers(port);
 }
 
 // ================== WIRES WORKBENCH STUFF =============
 private static List<ConnectableCreationListener> connectableCreationListeners = Collections.synchronizedList(new LinkedList<ConnectableCreationListener>());
 public static void addConnectableCreationListener(ConnectableCreationListener ccl) {
     connectableCreationListeners.add(ccl);
 }


/**
 * 
 */
protected void notifyConnectableCreationListeners() {
    synchronized(connectableCreationListeners) {
         Iterator<ConnectableCreationListener> iCreationListeners = connectableCreationListeners.iterator();
         while (iCreationListeners.hasNext()) {
             ConnectableCreationListener creationListener = iCreationListeners.next();
             creationListener.connectableCreated(this);
         }
     }
}
 
}



