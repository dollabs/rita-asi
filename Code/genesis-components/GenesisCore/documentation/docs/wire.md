# Box and Wire

In Genesis, modules are connected by wires that carry signals.

In this document, I explain a Java mechanism, achieved through the `Connections` class, that enables you to connect system modules to one another using a box-and-wire metaphor.

## Connect boxes through `Connections` Class

Static methods in the `Connections` class perform these purposes:

- `Connections.wire(source, destination)` sets up connections between wired boxes.
- `Connections.getPorts(this).transmit(signal)` transmits a signal via an instance’s output port.
- `Connections.getPorts(this).addSignalProcessor("functionName")` specifies the function to process the signal once it is received via an instance’s input port.

Suppose you want to connect `SourceClass` and `DestinationClass` together. Your first step is to have both class extend the `AbstractWiredBox` class:

```
public class SourceClass extends AbstractWiredBox {

}
```

```
public class DestinationClass extends AbstractWiredBox{

}
```

Then, you add the function `sayHello` to transmit signal in SourceClass:

```
public class SourceClass extends AbstractWiredBox {
	public void sayHello() {
		String signal = "Hello world";
		Mark.say("Source transmits", signal);
		Connections.getPorts(this).transmit(signal);
	}
}
```

Next, you add the mechanism to process received signal using function `replyHello` in DestinationClass:

```
public class DestinationClass extends AbstractWiredBox {
	public DestinationClass() {
		Connections.getPorts(this).addSignalProcessor("replyHello");
	}

	public void replyHello(Object input) {
		Mark.say("Destination receives", input);
	}
}
```

> **Note that the function for processing received signals in any destination class must be a void method with a single argument of the `Object` class**

To wire the two classes together and test the connection, let's create a test class named, unimaginatively, `TestWire`:

```
public class TestWire {
	public static void main(String[] args) {
		SourceClass source = new SourceClass();
		DestinationClass destination = new DestinationClass();
		Connections.wire(source, destination);
		source.sayHello();
	}
}
```
When you run the `TestWire`, the instance of `SourceClass` will send string "Hello world" to the instance of `DestinationClass`, where the string is processed by function `replyHello()`. So you will see the following results in console:

```
>>>  Source transmits Hello world
>>>  Destination receives Hello world
```



### You can transmit multiple objects by packing them

Because of characteristics of Java’s reflection mechanism, we required the signal processors to be void methods with single arguments. You can nevertheless send any number of objects at the same time by packaging them up in a `Signal` object, as in the following example.

```
public class SourceClass extends AbstractWiredBox {
	public void sayHello() {
		String string = "Hello world";
		int number = 11;
		Object signal = new Signal(string, number);
		Mark.say("Source transmits many things", string, number);
		Mark.say("Source transmits", signal);
		Connections.getPorts(this).transmit(signal);
	}
}
```

We can have two processing functions in `DestinationClass` to process the input signal, one of which knows how to unpack the signal back to the original objects:

```
public class DestinationClass extends AbstractWiredBox {

	public DestinationClass() {
		Connections.getPorts(this).addSignalProcessor("replyHello");
		Connections.getPorts(this).addSignalProcessor("replyManyThings");
	}

	public void replyHello(Object input) {
		Mark.say("Destination receives", input);
	}

	public void replyManyThings(Object input) {
		if (input instanceof Signal) {
			Signal signal = (Signal) input;
			String x = signal.get(0, String.class);
			int y = signal.get(1, Integer.class);
			Mark.say("Destination receives many things", x, y);
		}
	}
}
```

Without modifying the test class, we get some result like:

```
>>>  Source transmits many things Hello world 11
>>>  Source transmits Signals.Signal@30f39991
>>>  Destination receives Signals.Signal@30f39991
>>>  Destination receives many things Hello world 11
```



## You can add interim class

We can add one interim class to process the output signal:

```
public class InterimClass extends AbstractWiredBox {

	public InterimClass() {
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object input) {
		input = (Object) ((String) input + " (processed)");
		Connections.getPorts(this).transmit(input);
	}

}
```

We rewire the connection between `SourceClass` and `DestinationClass` so that signal is processed by `InterimClass` before reaching `DestinationClass`:

```
public class TestWire {
	public static void main(String[] args) {
		SourceClass source = new SourceClass();
		DestinationClass destination = new DestinationClass();

		InterimClass interim = new InterimClass();
		Connections.wire(source, interim);
		Connections.wire(interim, destination);

		source.sayHello();
	}
}
```

The output will be:

```
>>>  Source transmits Hello world
>>>  Destination receives Hello world (processed)
```


## You can turn existing subclasses into boxes

Suppose you have a class that already extends some other class. You won't be able to make it extend `AbstractWiredBox` class. So you can make it implement the `Wiredbox` interface instead. The only extra step is to implement a `getName` method:

```
public class SourceClass implements WiredBox {
	@Override
	public String getName() {
		return null;
	}
	...
}
```

## You can use multiple input ports or multiple output ports

Between two wired boxes, signals can be transmitted from and received by different ports. To refined three functions of the `Connections` class:

- `Connections.wire( [sourcePortName,] source, [destinationPortName,] destination)` sets up connections between wired boxes.
- `Connections.getPorts(this).transmit( [sourcePortName,] signal)` transmits a signal via an instance’s output port, optionally named `sourcePortName`.
- `Connections.getPorts(this).addSignalProcessor([destinationPortName,] "functionName")` specifies the function to process the signal once it is received via an instance’s input port, optionally named `destinationPortName`.

For example, `SourceClass` may have two ways of sending signals:

```
public class SourceClass extends AbstractWiredBox {

	public static final String OUTPUT_PORT_1 = "port 1 output";
	public static final String OUTPUT_PORT_2 = "port 2 output";

	public void sayHello() {
		String signal = "Hello world";
		Mark.say("Source transmits", signal);
		Connections.getPorts(this).transmit(OUTPUT_PORT_1, signal);
	}

	public void sayHelloAgain() {
		String signal = "Hello world again";
		Mark.say("Source transmits again", signal);
		Connections.getPorts(this).transmit(OUTPUT_PORT_2, signal);
	}

}
```

Accordingly, `DestinationClass` may have two ways of processing signals:

```
public class DestinationClass extends AbstractWiredBox {

	public static final String INPUT_PORT_1 = "port 1 input";
	public static final String INPUT_PORT_2 = "port 2 input";

	public DestinationClass() {
		Connections.getPorts(this).addSignalProcessor(INPUT_PORT_1, "replyHello");
		Connections.getPorts(this).addSignalProcessor(INPUT_PORT_2, "replyHelloAgain");
	}

	public void replyHello(Object input) {
		Mark.say("Destination receives", input);
	}
	public void replyHelloAgain(Object input) {
		Mark.say("Destination receives again", input);
	}

}

```

In `TestWire`:

```
public class TestWire {

	public static void main(String[] args) {
		SourceClass source = new SourceClass();
		DestinationClass destination = new DestinationClass();

		Connections.wire(SourceClass.OUTPUT_PORT_1, source, DestinationClass.INPUT_PORT_1, destination);
		Connections.wire(SourceClass.OUTPUT_PORT_2, source, DestinationClass.INPUT_PORT_2, destination);

		source.sayHello();
		source.sayHelloAgain();
	}

}
```

The output will be:

```
>>>  Source transmits Hello world
>>>  Destination receives Hello world
>>>  Source transmits again Hello world again
>>>  Destination receives again Hello world again
```

---

This version is edited by Zhutian Yang, on 5 August 2019

[An older version](https://groups.csail.mit.edu/genesis/Documentation/wire.pdf) is authored by Patrick H. Winston, on 7 October 2014
