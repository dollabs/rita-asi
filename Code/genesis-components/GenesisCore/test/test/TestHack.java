package test;

import connections.*;
import utils.Mark;

/*
 * Created on Jul 12, 2007 @author phw
 */
public class TestHack {
	
	public static void main(String[] args) {
		TestSource source = new TestSource("Source");
		TestTarget target = new TestTarget("Target");

		// These can used named ports, of course
		Connections.wire(source, target);
		Connections.wire(target, source);

		source.doSomething();
	}
}


class TestSource extends AbstractWiredBox {

	String h = "Hello";

	String w = "World";

	public TestSource(String name) {
		super(name);
		Connections.getPorts(this).addSignalProcessor(this::receive);
	}
	public void doSomething() {
		// Source sends message
		Connections.getPorts(this).transmit(h);
		// Source has sent message to target, target has sent message back
		// Return message has reset local variable w
		// Result is Hello Zhutian
		Mark.say(h, w);
	}

	public void receive(Object o) {
		Mark.say("Source receives", o, "from target");
		// Method resets local variable from "World" to whatever o is
		w = (String) o;
	}
}


class TestTarget extends AbstractWiredBox {

	public TestTarget(String name) {
		super(name);
		Connections.getPorts(this).addSignalProcessor(this::process);
	}

	public void process(Object signal) {
		// Target receives message
		Mark.say("Target receives", signal);
		// Target prepares reply
		String message = "Zhutian";
		// Target sends message back
		Connections.getPorts(this).transmit(message);
	}
}
