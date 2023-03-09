package connections;

import java.util.*; 

import utils.logging.*;
import utils.logging.Logger;

public class Wire implements Observer {
 boolean verbose = false;
 public Connectable source;
 public Ported target;

 public static Object VIEWER = "viewer";
 public static Object INPUT = "input";
 public static Object OUTPUT = "output";
 public static Object CLEAR = "clear";
 public static Object STOP = "stop";

 public Object sourcePort = OUTPUT;
 public Object targetPort = INPUT;

 boolean connected = true;

 private List<WireListener> wireListeners = Collections.synchronizedList(new LinkedList<WireListener>());
 protected void notifyWireListeners(Object transmitting, boolean stillTransmitting) {
     synchronized(wireListeners) {
         Iterator<WireListener> iListeners = wireListeners.iterator();
         while (iListeners.hasNext()) {
             WireListener listener = iListeners.next();
             if (stillTransmitting) {
                 listener.wireStartTransmitting(this, transmitting);
             } else {
                 listener.wireDoneTransmitting(this, transmitting);
             }
         }
     }
 } 
 public void addWireListener(WireListener wl) {
     wireListeners.add(wl);
 } /**
  * Create a wire using a factory method.  Preferred method.
  */
 public static Wire wire (Connectable source, Ported target) {
  fine ("Creating wire from " + OUTPUT + " to " + INPUT);
  return new Wire(source, OUTPUT, target, INPUT);
 }

 /**

  * Create a wire using a factory method.  Preferred method.
  */
 public static Wire wire (Connectable source, Object sourcePort,  Ported target, Object targetPort) {
  fine ("Creating wire from " + sourcePort + " to " + targetPort);
  return new Wire(source, sourcePort,  target, targetPort);
 }

 /**
  * Construct wire, with no ports provided.
  */
 private Wire (Connectable source, Ported target) {
  this.source = source; this.target = target;
  this.source.addObserver(this);
  notifyWireCreationListeners();
 }  

 /**
  * Construct wire, with given ports.
  */
 private Wire (Connectable source, Object sourcePort,  Ported target, Object targetPort) {
  this.source = source; this.target = target;
  this.source.addObserver(this);
  this.sourcePort = sourcePort;
  this.targetPort = targetPort;
  notifyWireCreationListeners();
 }  

 public void disconnect () {connected = false;}
 public void connect () {connected = true;}

 public void update (Observable observable, Object x) {
  if (!connected) {return;}
  fine ("x/sourcePort/targetPort = " + x + ", " + sourcePort + ", " + targetPort);
  Object sourceOutput;
  // If x is null, this is not a ported update; ignore 
  if (x == null) {return;}
  // If sourcePort matches, move information
  if (sourcePort.equals(x)) {
   try {
    sourceOutput = source.getOutput(sourcePort);
   }
   catch (Exception ignore) {
    warning("Wire transfer failed when calling getOutput in source " + source.getClass() + ", port " + sourcePort);
    ignore.printStackTrace();
    return;
   }
   try {
    if (verbose) {
     info("Moving information on wire from the " + source.getClass() + ", port " + sourcePort
                                                 + ", downstream to the "
                                                 + target.getClass() + ", port " + targetPort);
    }
    notifyWireListeners(sourceOutput, true);
    target.setInput(sourceOutput, targetPort);
    notifyWireListeners(sourceOutput, false);
    return;
   }
   catch (Exception ignore) {
    warning("Wire transfer failed when calling setInput in target " + target.getClass() + ", port " + targetPort
             + ". Target could not accept " + sourceOutput.toString());
    ignore.printStackTrace();
    return;
   }
  }
 }

 /**
  * Sets switch; true means message is printed when wire transmits
  * information.
  */
 public void setVerbose(boolean b) {verbose = b;}
 
 // ================== WIRES WORKBENCH STUFF =============
 private static List<WireCreationListener> wireCreationListeners = Collections.synchronizedList(new LinkedList<WireCreationListener>());
 public static void addWireCreationListener(WireCreationListener wcl) {
     wireCreationListeners.add(wcl);
 }
 protected void notifyWireCreationListeners() {
     synchronized(wireCreationListeners) {
         Iterator<WireCreationListener> iCreationListeners = wireCreationListeners.iterator();
         while (iCreationListeners.hasNext()) {
             WireCreationListener creationListener = iCreationListeners.next();
             creationListener.wireCreated(this);
         }
     }
 }

 public static void fine (Object s) {
  Logger.getLogger("wires.Wire").fine(s);
 }
 public static void info (Object s) {
  Logger.getLogger("wires.Wire").info(s);
 }
 public static void warning (Object s) {
  Logger.getLogger("wires.Wire").warning(s);
 }

}

