package connections;

import java.awt.BorderLayout;

import javax.swing.*;


import connections.views.*;

/*
 * Created on Jul 12, 2007 @author phw
 */
public class Test {
	
	public static int counter = 100;

	public static void main(String[] args) {
		
		// ConnectionViewer.getInstance();
		
		JFrame frame = new JFrame();
		ConnectionViewer viewer = Adapter.makeConnectionAdapter().getViewer();
		
		frame.getContentPane().add(new JScrollPane(viewer), BorderLayout.CENTER);
		frame.getContentPane().add(viewer.getSlider(), BorderLayout.SOUTH);
		frame.setBounds(0, 0, 800, 800);
		frame.setVisible(true);
		new Test().demonstrate();
	}

	public void demonstrate() {
		// Connections.setVerbose(true);
		TestBox a = new TestBox("A");
		WiredOnOffSwitch b = new WiredOnOffSwitch("Switch");
		b.setSelected(false);
		TestBox l = new TestBox("L");
		TestBox m = new TestBox("M");
		TestBox n = new TestBox("N");
		TestBox x = new TestBox("X");
		TestBox d1 = new TestBox("Y1");
		TestBox d2 = new TestBox("russia want russia damage a very big computer_networks");
		TestBox e = new TestBox("E");
		TestBox z = new TestBox("Z");

		// Start start = new Start();
		
		
		
		Connections.biwire(l, m);
		Connections.wire(m, l);
		Connections.wire(l, n);

		
		Connections.wire(x, d1);
		Connections.wire(x, d2);
		Connections.wire(d2, e);
		Connections.wire("foo", x, "bar", d1);
		Connections.wire(Port.UP, b, z);
		Connections.wire(Port.DOWN, b, e);
		Connections.biwire(a, b);
		// start.setName("Start");

		// System.out.println("Start's name is " + start.getName());
		// Connections.wire(z, start);
		// Connections.wire(z, x);
		
		// Connections.wire(d1, l);

		Connections.getPorts(a).transmit("Hello world");
		Connections.getPorts(l).transmit("Hello world");
		
	}



	public Test() {
		// Connections.getInstance();
		// Adapter.getInstance();
		// ConnectionViewer.getInstance();
	}

	// Connections.setVerbose(true);
	// TestBox A = new TestBox("A");
	// TestBox B = new TestBox("B");
	// TestBox C = new TestBox("C");
	// Connections.wire("x", A, "y", B);
	// Connections.wire(A, B); // Defaults to "output" and "input"
	//
	// A.demonstrate();

	public class TestBox extends AbstractWiredBox {

		public TestBox(String name) {
			super(name);
			Connections.getPorts(this).addSignalProcessor("aMethod");
			// Connections.getPorts(this).addSignalProcessor("another", "anotherMethod");
		}

		public void aMethod(Object signal) {
			// System.out.println("Processing signal <" + signal + "> in " + this);
			// Blow out
			// Integer i = (Integer)signal;
			System.out.println("...");
			if (counter-- > 0) {
				try {
	                Thread.sleep(100);
                }
                catch (InterruptedException e) {
	                e.printStackTrace();
                }
				Connections.getPorts(this).transmit(signal);
			}
		}

		public void anotherMethod(Object signal) {
			// System.out.println("Another method processing signal <" + signal
			// + "> in " + this);
		}
	}
}
