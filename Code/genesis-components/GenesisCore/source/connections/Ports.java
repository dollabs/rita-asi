package connections;

import java.util.*;
import java.util.function.Consumer;

import utils.Mark;

/*
 * Key to whole works. Contains a collection of individual Port objects, one for each port name of each WiredBox. For
 * any given WiredBox object, the port object is obtained using Connections.getPorts(); See Connections for further
 * documentation. Created on Jul 12, 2007 @author phw
 */
public class Ports {

	private HashMap<String, Port> ports = new HashMap<String, Port>();

	private HashMap<String, ArrayList<String>> runnables = new HashMap<String, ArrayList<String>>();

	private Map<String, Collection<Consumer<Object>>> runnableMethods = new HashMap<>();

	/*
	 * Fetch output port with given name.
	 */
	public Port getPort(String name) {
		Object o = ports.get(name);
		if (o != null) {
			return (Port) o;
		}
		else {
			Port port = new Port(name);
			ports.put(name, port);
			return port;
		}
	}

	/*
	 * Transmit signal to output port with given name.
	 */
	public void transmit(String name, Object signal) {
		getPort(name).transmit(signal);
	}

	/*
	 * Transmit signal to output port named "output".
	 */
	public void transmit(Object signal) {
		transmit(Port.OUTPUT, signal);
	}

	public void transmitWithTimer(String name, Object signal) {
		transmit(1000, name, signal);
	}

	public void transmitWithTimer(Object signal) {
		transmit(1000, signal);
	}

	public void transmit(long timeout, String name, Object signal) {
		TimerThread timer = new TimerThread(timeout);
		timer.start();
		getPort(name).transmit(signal);
		timer.quit();
	}

	private class TimerThread extends Thread {
		long timeout;

		long start;

		boolean quit = false;

		public void quit() {
			quit = true;
		}

		public TimerThread(long timeout) {
			this.timeout = timeout;
			start = System.currentTimeMillis();
		}

		public void run() {
			long end = System.currentTimeMillis();
			if ((end - start) > timeout) {
				System.err.println(">>>>> Timer timed out in " + (end - start) + " milliseconds");
				return;
			}
			else if (quit) {
				return;
			}
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				System.err.println(">>>>> Timer sleep crashed");
			}
		}
	}

	/*
	 * Transmit signal to output port named "output".
	 */
	public void transmit(long timeout, Object signal) {
		transmit(timeout, Port.OUTPUT, signal);
	}

	/*
	 * More general method
	 */
	public void transmit(Object... objects) {

	}

	/*
	 * Get signal processors associated with a given input key. Used in transmission.
	 */
	ArrayList<String> getSignalProcessors(String key) {
		if (runnables.get(key) != null) {
			return runnables.get(key);
		}
		ArrayList<String> list = new ArrayList<String>();
		runnables.put(key, list);
		return list;
	}

	Collection<Consumer<Object>> getSignalProcessorMethods(String key) {
		if (runnableMethods.get(key) != null) return runnableMethods.get(key);
		Collection<Consumer<Object>> list = new ArrayList<>();
		runnableMethods.put(key, list);
		return list;
	}

	public Map<String, List<String>> getPortToProcessorsMapping() {
		return new HashMap<String, List<String>>(runnables);
	}

	public Map<String, Collection<Consumer<Object>>> getPortToProcessorMethodsMapping() {
		return new HashMap<String, Collection<Consumer<Object>>>(runnableMethods);
	}

	/*
	 * Add a signal processor with name defaulted to "input".
	 */
	public void addSignalProcessor(String r) {
		addSignalProcessor(Port.INPUT, r);
	}

	public void addSignalProcessor(String portName, String methodName) {
		if (Connections.isVerbose()) {
			System.out.println("Defining response for port " + portName);
		}
		if (!getSignalProcessors(portName).contains(methodName)) {
			getSignalProcessors(portName).add(methodName);
		}
		else {
//			Mark.err("Ooops, tried to add signal processor", methodName, "to port", portName, "twice");
		}
	}

	/*
	 * It is known that this sort of Signal Processor is currently local only
	 */
	public void addSignalProcessor(Consumer<Object> method) {
		addSignalProcessor(Port.INPUT, method);
	}

	public void addSignalProcessor(String portName, Consumer<Object> method) {
		if (Connections.isVerbose()) {
			System.out.println("Defining response for port " + portName);
		}
		getSignalProcessorMethods(portName).add(method);
	}

	public ArrayList<Port> getPorts() {
		ArrayList<Port> result = new ArrayList<Port>();
		result.addAll(ports.values());
		return result;
	}

	/*
	 * Like transmit, but runs in its own thread.
	 */
	public void launch(String message) {
		Wrapper wrapper = new Wrapper(message);
		wrapper.start();
	}

	public void launch(String port, String message) {
		Wrapper wrapper = new Wrapper(port, message);
		wrapper.start();
	}

	private class Wrapper extends Thread {
		Object o;

		String port = Port.OUTPUT;

		public Wrapper(Object o) {
			this.o = o;
		}

		public Wrapper(String port, Object o) {
			this.port = port;
			this.o = o;
		}

		public void run() {
			// System.out.println("Starting analysis");
			transmit(port, o);
			// System.out.println("Analysis complete");
		}

	}

}
