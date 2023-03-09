package connections;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

import utils.Mark;
import connections.views.*;

/*
 * See Connections for documentation. Created on Jul 12, 2007 @author phw
 */
public class Port {

	public final static String OUTPUT = "output";

	public final static String INPUT = "input";

	public final static String VIEWER = "viewer";

	public final static String A = "port a";

	public final static String B = "port b";

	public final static String C = "port c";

	public final static String D = "port d";

	public final static String E = "port e";

	public final static String F = "port f";

	public final static String UP = "up";

	public final static String DOWN = "down";

	public final static String RESET = "reset";

	private HashMap<String, ArrayList<WiredBox>> destinations = new HashMap<String, ArrayList<WiredBox>>();

	private ArrayList<String> keys = new ArrayList<String>();

	private String sourceName;

	private WiredBox sourceBox;

	/*
	 * All ports have a name, to ease debugging. Often the names default to "output" and "input"
	 */
	public Port(String name) {
		this.sourceName = name;
	}

	/*
	 * Utility function that associates a destination name with a particular destination which receives signals from
	 * this port on an input port named destinationName. There can be many, of course, as the port may be connected to,
	 * for example, the "input" port of many wired boxes.
	 */
	public void attach(WiredBox sourceBox, String destinationPortName, WiredBox destinationBox) {
		setSourceBox(sourceBox);
		getDestinations(destinationPortName).add(destinationBox);
		receiveByOthers(destinationPortName, destinationBox);

	}

	public void detach(String destinationName, WiredBox destination) {
		getDestinations(destinationName).remove(destination);
	}

	public boolean isAttached(String destinationName, WiredBox destination) {
		return getDestinations(destinationName).contains(destination);
	}

	/*
	 * Returns the wired boxes assocated with a given name. These boxes receive input from the port on a port named
	 * name.
	 */
	public ArrayList<WiredBox> getDestinations(String name) {
		ArrayList<WiredBox> o = destinations.get(name);
		if (o != null) {
			return o;
		}
		else {
			ArrayList<WiredBox> list = new ArrayList<WiredBox>();
			destinations.put(name, list);
			keys.add(name);
			return list;
		}
	}

	/*
	 * The port is moving a signal out to all the destinations to which it is connected. The key list is used, rather
	 * than using destinations.getKeys, so that the order of transmission is the same as the order in which connections
	 * were made. IMPORTANT NOTE: this method contains a dangerous mixture of GUI support code with core functionality.
	 * The GUI code caused an insidious bug when the Propagators project was incorporated in a library to support this
	 * WiredBox stuff in other languages. In order to address the problem without finding out how deep the rabbit hole
	 * goes (I followed it for a while: it's very deep) I've wrapped the offending statements in conditions that causes
	 * them not to be executed if we are in a library environment rather than the usual Genesis environment, but this is
	 * obviously a cheap hack. The method needs to be rewritten, but I'm afraid the problem may be too widespread at
	 * this point. -ADK
	 */
	public void transmit(Object signal) {
		for (Iterator<String> i = keys.iterator(); i.hasNext();) {
			String key = (i.next());
			ArrayList<WiredBox> list = destinations.get(key);
			Adapter adapter = null;
			ViewerBox sourceViewerBox = null;
			if (!LibUtil.isLib()) { // Don't execute the misplaced gui code if
			                        // we're running as a library. please
			                        // refactor this
				adapter = Adapter.makeConnectionAdapter();
				sourceViewerBox = adapter.getViewerBox(getSourceBox());
				if (sourceViewerBox != null) { // ADK: sometimes the call to
				                               // getViewerBox above returns
				                               // null. I won't debug it because
				                               // I
					// think all of this gui code is misplaced here. somebody
					// please refactor.
					sourceViewerBox.setTemporaryColor();
				}
			}
			for (int x = 0; x < list.size(); ++x) {
				WiredBox destinationBox = list.get(x);

				if (destinationBox == null) {
					System.err.println("Destination box null in " + list + " obtained from " + key);
				}

				if (destinationBox instanceof AbstractWiredBox) {
					AbstractWiredBox theBox = (AbstractWiredBox) destinationBox;
					if (theBox.getGateKeeper() != null && !theBox.getGateKeeper().isSelected()) {
						// This box has been turned off by a check box with memory
						// Mark.say("No signals accepted by", destinationBox.getName());
						continue;
					}
				}

				ViewerBox destinationViewerBox = null;
				if (!LibUtil.isLib()) {
					destinationViewerBox = adapter.getViewerBox(destinationBox);
					if (destinationViewerBox != null) { // ADK: buried
					                                    // mysterious null
					                                    // pointer exceptions
						destinationViewerBox.setTemporaryColor();
					}
					if (sourceViewerBox != null && destinationViewerBox != null) { // ADK:
					                                                               // buried
					                                                               // mysterious
					                                                               // null
					                                                               // pointer
					                                                               // exceptions
						if (Connections.isVerbose() || sourceViewerBox.isSelected() || destinationViewerBox.isSelected()) {
							String source = "Nameless source";
							if (getSourceBox() instanceof WiredBox) {
								source = getSourceBox().getName();
							}
							String destination = "Nameless destination";
							if (destinationBox instanceof WiredBox) {
								destination = ((WiredBox) destinationBox).getName();
							}
							if (Connections.isVerbose()) {
								System.out.println(">>> " + source + " transmitting signal <" + signal.getClass() + ">\n---\n" + signal
								        + "\n---\nfrom port " + this.sourceName + " to port " + key + " of " + destination);
							}
						}
					}
				}
				ArrayList<String> runnables = Connections.getPorts(destinationBox).getSignalProcessors(key);

				if (Connections.isVerbose()) {
					System.out.println(">>> Runnable count is " + runnables.size() + " for " + key);
				}
				for (int r = 0; r < runnables.size(); ++r) {
					Object element = runnables.get(r);
					if (Connections.isVerbose()) {
						System.out.println(">>> Signal processor is " + element);
					}
					if (element instanceof String) {
						String methodName = (String) (runnables.get(r));
						if (methodName.contains("Lambda")) continue;
						Class<?>[] parameters = { Object.class };
						Object[] arguments = { signal };
						Class<?> c = null;
						Method m = null;
						WireClientEndpoint.getInstance().hook(getSourceBox());// ADK:
						                                                      // this
						                                                      // incantation
						                                                      // is
						                                                      // required
						// if the destinationBox is a WireClientEndpoint (i.e.
						// local proxy of remote WiredBox).
						String place = "unknown";
						try {
							c = destinationBox.getClass();
							place = "A";
							m = c.getMethod(methodName, parameters);
							place = "B";
							m.invoke(destinationBox, arguments);
							place = "C";
						}
						catch (Exception e) {
							if (!LibUtil.isLib()) {
								destinationViewerBox.setState(ViewerBox.BLEW_OUT);
							}
							String error = ">>> Blew out while trying to apply method named " + methodName;
							error += " listening to port " + key;
							error += " in box " + destinationBox.getName();
							error += " of " + destinationBox.getClass();
							error += " on signal " + signal;
							error += " of " + signal.getClass();
							error += " after getting to " + place;
							System.err.println(error);
							e.printStackTrace();
						}
					}
					else {
						System.err.println("Unable to process " + element + " in " + destinationBox);
					}
				}

				Collection<Consumer<Object>> runnableMethods = Connections.getPorts(destinationBox).getSignalProcessorMethods(key);
				for (int r = 0; r < runnables.size(); r++) {

				}

				if (Connections.isVerbose()) {
					System.out.println(">>> Runnable Methods count is " + runnableMethods.size() + " for " + key);
				}
				for (Consumer<Object> method : runnableMethods) {
					if (Connections.isVerbose()) {
						System.out.println(">>> Signal processor is " + method.toString());
					}
					Class<?>[] parameters = { Object.class };
					Object[] arguments = { signal };
					Class<?> c = null;
					Method m = null;
					WireClientEndpoint.getInstance().hook(getSourceBox());// ADK:
					                                                      // this
					                                                      // incantation
					                                                      // is
					                                                      // required
					// if the destinationBox is a WireClientEndpoint (i.e.
					// local proxy of remote WiredBox).
					try {
						method.accept(signal);
					}
					catch (Exception e) {
						if (!LibUtil.isLib()) {
							destinationViewerBox.setState(ViewerBox.BLEW_OUT);
						}
						String error = ">>> Blew out while trying to apply method " + method.toString();
						error += " listening to port " + key;
						error += " in box " + destinationBox.getName();
						error += " on signal of class " + signal.getClass();
						System.err.println(error);
						e.printStackTrace();
					}
				}
			}
		}
		if (linkedOutputPortPairs != null) {
			transmitToOthers(signal);
		}
	}

	public String getSourceName() {
		return sourceName;
	}

	public String toString() {
		return "<Port: " + sourceName + ">";
	}

	public WiredBox getSourceBox() {
		return sourceBox;
	}

	public void setSourceBox(WiredBox sourceBox) {
		if (this.sourceBox != null && sourceBox != this.sourceBox) {
			System.err.println("Ooops, changed box associated with a port from " + this.sourceBox + " to " + sourceBox);
		}
		this.sourceBox = sourceBox;
	}

	public Set<String> getDestinationNames() {
		return destinations.keySet();
	}

	public ArrayList<WiredBox> getTargets() {
		ArrayList<WiredBox> result = new ArrayList<WiredBox>();
		for (ArrayList<WiredBox> list : destinations.values()) {
			result.addAll(list);
		}
		return result;
	}

	/**
	 * The following makes it possible:
	 * <p>
	 * ... to receive from an inner input port when there is a reception from an outer input port, as when wired boxes
	 * contain other wired boxes;
	 * <p>
	 * ... to broadcast from an outer output port when there is a broadcast from an inner output port, as when wired
	 * boxes contain other wired boxes.
	 * 
	 * <pre>
	 * </pre>
	 */

	// Receive to receive

	public void receiveByOthers(String destinationPortName, WiredBox destinationBox) {
		ArrayList<LinkedPortPair> pairs = Connections.getPorts(destinationBox).getPort(destinationPortName).getLinkedInputPortPairs();
		for (LinkedPortPair pair : pairs) {
			getDestinations(pair.destinationPortName).add(pair.destinationBox);
			// System.out.println("Blending " + destinationPortName + " " + destinationBox.getClass() + " with " +
			// pair.destinationPortName + " " + pair.destinationBox.getClass());
		}
	}

	public void forwardTo(WiredBox sourceBox, WiredBox destinationBox) {
		forwardTo(Port.INPUT, sourceBox, destinationBox);
	}

	public void forwardTo(String portName, WiredBox sourceBox, WiredBox destinationBox) {
		forwardTo(portName, sourceBox, portName, destinationBox);
	}

	public void forwardTo(String sourcePortName, WiredBox sourceBox, String destinationPortName, WiredBox destinationBox) {
		getLinkedInputPortPairs().add(new LinkedPortPair(sourcePortName, sourceBox, destinationPortName, destinationBox));
		// System.out.println("Added forward to " + sourcePortName + "/" + sourceBox.getClass() + " connecting to " +
		// destinationPortName + "/" + destinationBox.getClass());
	}

	private ArrayList<LinkedPortPair> linkedInputPortPairs;

	private ArrayList<LinkedPortPair> getLinkedInputPortPairs() {
		if (linkedInputPortPairs == null) {
			linkedInputPortPairs = new ArrayList<LinkedPortPair>();
		}
		return linkedInputPortPairs;
	}

	// Broadcast to broadcast

	public ArrayList<LinkedPortPair> linkedOutputPortPairs;

	private void transmitToOthers(Object signal) {
		for (LinkedPortPair pair : getLinkedOutputPortPairs()) {
			Connections.getPorts(pair.destinationBox).transmit(pair.destinationPortName, signal);
		}
	}

	public void forwardFrom(WiredBox sourceBox, WiredBox destinationBox) {
		forwardFrom(Port.OUTPUT, sourceBox, destinationBox);
	}

	public void forwardFrom(String portName, WiredBox sourceBox, WiredBox destinationBox) {
		forwardFrom(portName, sourceBox, portName, destinationBox);
	}

	public void forwardFrom(String sourcePortName, WiredBox sourceBox, String destinationPortName, WiredBox destinationBox) {
		getLinkedOutputPortPairs().add(new LinkedPortPair(sourcePortName, sourceBox, destinationPortName, destinationBox));
	}

	private ArrayList<LinkedPortPair> getLinkedOutputPortPairs() {
		if (linkedOutputPortPairs == null) {
			linkedOutputPortPairs = new ArrayList<LinkedPortPair>();
		}
		return linkedOutputPortPairs;
	}

}
