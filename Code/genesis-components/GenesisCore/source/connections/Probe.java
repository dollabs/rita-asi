package connections;

/*
 * Created on Jul 26, 2008
 * @author phw
 */

public class Probe extends AbstractWiredBox {

	public Probe(String name) {
		setName(name);
		Connections.getPorts(this).addSignalProcessor("input");
	}

	public void input(Object input) {
		System.out.println("Probe " + getName() + " received " + input.toString());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Probe A = new Probe("source");
		Probe B = new Probe("destination");
		Connections.wire(A, B);
		Connections.getPorts(A).transmit("Hello world");
	}

}
