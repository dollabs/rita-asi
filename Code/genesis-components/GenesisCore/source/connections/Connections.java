package connections;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Observable;

/*
 * Main access point to new wire mechanism. Connections.wire methods connect
 * named output port to named input port: Connections.wire (String outputName,
 * WiredBox source, String inputName, WiredBox destination); Variations default
 * to "output" and "input". To transmit on an output port of a given name, the
 * design pattern is
 * @
 * @ Connections.getPorts(this).transmit(String outputName, Object signal);
 * @
 * @ outputName defaults to "output". On the other end, signal-argument methods
 * are attached to input ports of a given name thusly:
 * @
 * @ Connections.getPorts(this).addSignalProcessor(String inputName, String
 * processName);
 * @
 * @inputName defaults to "input". See Test class for sample use. Created on Jul
 * 14, 2007 @author phw
 */
public class Connections extends Observable implements Network<WiredBox> {

	private IdentityHashMap<WiredBox, Ports> portsHashMap;

	private static boolean verbose = false;

	private static Connections instance;

	private ArrayList<WiredBox> boxes;

	private Connections() {
		// System.out.println("Creating Connections");
	}

	public static void biwire(String sourceName, WiredBox source, String destinationName, WiredBox destination) {
		wire(sourceName, source, destinationName, destination);
		wire(destinationName, destination, sourceName, source);
	}

	public static void wire(String sourceName, WiredBox source, String destinationName, WiredBox destination) {
		if (Connections.getPorts(source).getPort(sourceName).isAttached(destinationName, destination)) {
			if (Connections.isVerbose()) {
				System.out.println("Already connected port \"" + sourceName + "\" of " + source + " to port \"" + destinationName + "\" of "
				        + destination);
			}
			return;
		}
		getInstance().addBox(source);
		getInstance().addBox(destination);
		if (Connections.isVerbose()) {
			System.out.println("Connecting port " + sourceName + " of " + source + " to port " + destinationName + " of " + destination);
		}
		Connections.getPorts(source).getPort(sourceName).attach(source, destinationName, destination);
		Connections.getInstance().changed();
	}

	public static void disconnect(String sourceName, WiredBox source, String destinationName, WiredBox destination) {
		if (Connections.isVerbose()) {
			System.out.println("Disconnecting port " + sourceName + " of " + source + " from port " + destinationName + " of " + destination);
		}
		Connections.getPorts(source).getPort(sourceName).detach(destinationName, destination);
		Connections.getInstance().changed();
	}

	public static void biwire(WiredBox source, String destinationName, WiredBox destination) {
		wire("output", source, destinationName, destination);
		wire(destinationName, destination, "output", source);
	}

	public static void wire(WiredBox source, String destinationName, WiredBox destination) {
		wire("output", source, destinationName, destination);
	}

	public static void disconnect(WiredBox source, String destinationName, WiredBox destination) {
		disconnect("output", source, destinationName, destination);
	}

	public static void biwire(String sourceName, WiredBox source, WiredBox destination) {
		wire(sourceName, source, "input", destination);
		wire("input", destination, sourceName, source);
	}

	public static void wire(String sourceName, WiredBox source, WiredBox destination) {
		wire(sourceName, source, "input", destination);
	}

	public static void disconnect(String sourceName, WiredBox source, WiredBox destination) {
		disconnect(sourceName, source, "input", destination);
	}

	public static void biwire(WiredBox source, WiredBox destination) {
		wire(source, destination);
		wire(destination, source);
	}

	public static void wire(WiredBox source, WiredBox destination) {
		wire("output", source, "input", destination);
	}

	public static void disconnect(WiredBox source, WiredBox destination) {
		disconnect("output", source, "input", destination); //
	}

	public static Ports getPorts(WiredBox box) {
		Ports ports = getInstance().getPortsHashMap().get(box);
		if (ports != null) {
			return ports;
		}
		// if (isVerbose()) {
		// System.out.println(">>>> Getting new Ports instance for " + box);
		// }
		ports = new Ports();
		getInstance().getPortsHashMap().put(box, ports);
		return ports;
	}

	/*
	 * Describe actions or not.
	 */
	public static boolean isVerbose() {
		return verbose;
	}

	/*
	 * Determine if actions will be described or not.
	 */
	public static void setVerbose(boolean verbose) {
		Connections.verbose = verbose;
	}

	public static Connections getInstance() {
		if (instance == null) {
			instance = new Connections();
		}
		return instance;
	}

	public static int getPortCount() {
		return getInstance().getPortsHashMap().size();
	}

	public ArrayList<WiredBox> getBoxes() {
		if (boxes == null) {
			boxes = new ArrayList<WiredBox>();
		}
		return boxes;
	}

	public void addBox(WiredBox box) {
		if (getBoxes().contains(box)) {
			return;
		}
		getBoxes().add(box);
	}

	public void changed() {
		this.setChanged();
		this.notifyObservers();
	}

	public IdentityHashMap<WiredBox, Ports> getPortsHashMap() {
		if (portsHashMap == null) {
			portsHashMap = new IdentityHashMap<WiredBox, Ports>();
		}
		return portsHashMap;
	}

	public ArrayList<WiredBox> getTargets(WiredBox box) {
		Ports ports = Connections.getPorts(box);
		Collection<Port> portCollection = ports.getPorts();
		ArrayList<WiredBox> result = new ArrayList<WiredBox>();
		for (Port port : portCollection) {
			result.addAll(port.getTargets());
		}
		return result;
	}

	/**
	 * For use during interactive network building. Completely clears
	 * the current network and leaves a blank slate
	 */
	public static void obliterateNetwork() {
		// 
		getInstance().portsHashMap = null;
		getInstance().boxes = null;
		getInstance().changed();
	}

	// The following makes it possible to tie ports together, so that
	// the first forwards signals to the other

	public static void forwardTo(WiredBox sourceBox, WiredBox destinationBox) {
		forwardTo(Port.INPUT, sourceBox, destinationBox);
	}

	public static void forwardTo(String portName, WiredBox sourceBox, WiredBox destinationBox) {
		forwardTo(portName, sourceBox, portName, destinationBox);
	}

	public static void forwardTo(String sourcePortName, WiredBox sourceBox, String destinationPortName, WiredBox destinationBox) {
		Connections.getPorts(sourceBox).getPort(sourcePortName).forwardTo(sourcePortName, sourceBox, destinationPortName, destinationBox);
	}

	public static void forwardFrom(WiredBox sourceBox, WiredBox destinationBox) {
		forwardFrom(Port.OUTPUT, sourceBox, destinationBox);
	}

	public static void forwardFrom(String portName, WiredBox sourceBox, WiredBox destinationBox) {
		forwardFrom(portName, sourceBox, portName, destinationBox);
	}

	public static void forwardFrom(String sourcePortName, WiredBox sourceBox, String destinationPortName, WiredBox destinationBox) {
		Connections.getPorts(sourceBox).getPort(sourcePortName).forwardFrom(sourcePortName, sourceBox, destinationPortName, destinationBox);
	}

	// End of forwarding code

	// Adam's network wire code:

	// User guide:
	// First, call useWireServer() to connect to a running wire server (instance
	// of WireServer.war in the WireServer project)
	// The serverURL parameter should be the full path, e.g.
	// http://foo.bar.mit.edu/WireServer
	//
	// Then, call publish() to publish one of your local WiredBox instances to
	// the server. you must perform all addSignalProcessor(...)
	// calls prior to publishing. Publishing a WiredBox makes its input and
	// output ports available to other users of the wire server.
	// You must choose a unique string to identify every WiredBox instance that
	// you publish on the server.
	//
	// Then, other users of the wire server may subscribe() to the box you
	// published. subscribe returns a proxy WiredBox that appears to the
	// subscriber as though it is the remote WiredBox you published. signals
	// transmitted to this box will be handled by the publisher's signal
	// processor methods. signals transmitted by the publisher will appear as
	// though they were transmitted by the local proxy.
	//
	// In the event of an error during useWireServer/publish/subscribe, a
	// NetWireException will be thrown. in the event of a network-wire related
	// error during transmit (including death of the publishing process,
	// transport failure, etc.) a NetWireError runtime error may be thrown.

	/**
	 * You must call this before you can call publish or subscribe. Provide the
	 * URL of the hub server which mediates all net wires.
	 * 
	 * @param serverURL
	 *            the address of the hub server which must be running.
	 * @throws NetWireException
	 *             if there is an error connecting/negotiating with the server
	 */
	public static void useWireServer(URL serverURL) throws NetWireException {
		boolean changed = WireClientEndpoint.getInstance().initialize(serverURL);
		// WiredBoxStubFactory.setFactoryClass(RemoteCodeGenerationStubFactory.class);
		// //for stub generation on the server
		if (changed) {
			WiredBoxStubFactory.setFactoryClass(LocalCodeGenStubFactory.class);
			WireClientEndpoint.getInstance().sayHello();
			WireClientEndpoint.getInstance().startPollingThread();
			WireClientEndpoint.setInitialized();
		}
	}

	public static void useWireServer(String serverURL) throws NetWireException {
		// thanks, Java, for being such a pedant about URL correctness. you have
		// rendered yourself useless again.
		try {
			useWireServer(new URL(serverURL));
		}
		catch (MalformedURLException e) {
			throw new NetWireException(e);
		}
	}

	/**
	 * Get a wired box corresponding to the GUID provided. The wired box must
	 * have been published to the server prior to the call to subscribe(...).
	 * useWireServer(URL) must have been called prior to the call to
	 * subscribe(...).
	 * 
	 * @param globalUniqueID
	 *            the GUID of the box you want
	 * @return A stub of a remote WiredBox or a local WiredBox. The object
	 *         returned may be used wherever WiredBoxen are used elsewhere.
	 * @throws NetWireException
	 */
	public static WiredBox subscribe(String globalUniqueID) throws NetWireException {
		return subscribe(globalUniqueID, 0);
	}

	/**
	 * Get a wired box corresponding to the GUID provided. useWireServer(URL)
	 * must have been called prior to the call to subscribe(...). if the box
	 * specified by globalUniqueID is not available, keep trying to subscribe
	 * until the timeout expires. if timeout is less than 0, wait indefinitely.
	 * 
	 * @param globalUniqueID
	 *            the GUID of the box you want
	 * @param timeout
	 *            seconds before aborting with an exception. waits indefinitely
	 *            if timeout < 0.
	 * @return A stub of a remote WiredBox or a local WiredBox. The object
	 *         returned may be used wherever WiredBoxen are used elsewhere.
	 * @throws NetWireException
	 */
	public static WiredBox subscribe(String globalUniqueID, double timeout) throws NetWireException {
		return subscribe(globalUniqueID, timeout, "Java");
	}

	public static WiredBox subscribe(String globalUniqueID, double timeout, String apiLanguage) throws NetWireException {
		if (!WireClientEndpoint.isInitialized()) {
			useWireServer(DefaultSettings.WIRE_SERVER);
		}
		double RETRY_INTERVAL = 0.25;// seconds
		double elapsed = 0.0;
		NetWireException error = null;
		// if(timeout != 0){throw new
		// NetWireException("unimplemented for timeout not equal to 0");}
		while (timeout < 0 || elapsed <= timeout) {
			try {
				if (WireClientEndpoint.getInstance().isConnected(globalUniqueID)) {
					WiredBox stub = WiredBoxStubFactory.getInstance().getStub(globalUniqueID);
					WireClientEndpoint.getInstance().subscribe(globalUniqueID, stub, apiLanguage);
					return stub;
				}
				else {
					error = new NetWireException(globalUniqueID + " is not published, or timeout expired");
				}
			}
			catch (NetWireException e) {
				if (timeout == 0) {
					error = e;
					break;
				}
			}
			catch (NetWireError e) {
				if (timeout == 0) {
					error = new NetWireException(e);
					break;
				}
			}
			double t = System.currentTimeMillis() * 1000;
			try {
				Thread.sleep((long) (RETRY_INTERVAL * 1000));
				elapsed += System.currentTimeMillis() * 1000 - t;
			}
			catch (InterruptedException f) {
				// pass
			}
		}
		if (error != null) {
			throw error;
		}
		throw new NetWireException("timeout waiting for \"" + globalUniqueID + "\" to be published");
	}

	/**
	 * Make a WiredBox available to subscribers. You need to provide a name that
	 * uniquely distinguishes the box. useWireServer(URL) must have been called
	 * prior to the call to publish(...).
	 * 
	 * @param box
	 *            a local WiredBox to be published
	 * @param globalUniqueID
	 *            a string that will name the box so that others can connect to
	 *            it. No other boxes may be published under this name.
	 * @throws NetWireException
	 */
	public static void publish(WiredBox box, String globalUniqueID) throws NetWireException {
		publish(box, globalUniqueID, "Java");
	}

	public static void publish(WiredBox box, String globalUniqueID, String apiLanguage) throws NetWireException {
		if (globalUniqueID.contains("|")) {
			throw new NetWireException("'|' cannot be in the ID because it is reserved");
		}
		if (!WireClientEndpoint.isInitialized()) {
			useWireServer(DefaultSettings.WIRE_SERVER);
		}
		WireClientEndpoint.getInstance().publish(box, globalUniqueID, apiLanguage);
	}

	/**
	 * for debugging, let errors and exceptions thrown by local wired boxes
	 * responding to remote requests be handled locally by e.
	 * 
	 * @param e
	 *            See ErrorHandler
	 */
	public static void setLocalErrorHandler(ErrorHandler e) {
		WireClientEndpoint.getInstance().registerErrorHandler(e);
	}

	@SuppressWarnings("serial")
	public static class NetWireException extends Exception {
		// checked exception thrown by net-wire specific methods
		public NetWireException(String e) {
			super(e);
		}

		public NetWireException(Throwable cause) {
			super(cause);
		}
	}

	@SuppressWarnings("serial")
	public static class NetWireError extends Error {
		// unchecked exception thrown by other wire methods
		// the design of wired boxes does not permit use of checked exceptions
		public NetWireError(String e) {
			super(e);
		}

		public NetWireError(Throwable cause) {
			super(cause);
		}
	}
}
