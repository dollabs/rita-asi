package kevinWhite;

import utils.Mark;

import connections.*;
import frames.entities.Entity;
import frames.entities.Sequence;

public class LocalProcessor extends AbstractWiredBox {  
	
	public final String MY_INPUT_PORT = "my input port";
	public final String MY_OUTPUT_PORT = "my output port";

	public LocalProcessor() {
		this.setName("Kevin's story processor");
		Connections.getPorts(this).addSignalProcessor("processSignal");
		// Example of named port
		Connections.getPorts(this).addSignalProcessor(MY_INPUT_PORT, "processSignal");
	}

	public void processSignal(Object signal) {
		if (signal instanceof Entity) {
			Entity t = (Entity) signal;
			if (t.sequenceP()) {
				Sequence s = (Sequence) t;
				Mark.say("Story received:");
				for (Entity e : s.getElements()) {
					Mark.say(e.asString());
					Connections.getPorts(this).transmit(e);
					Connections.getPorts(this).transmit(MY_OUTPUT_PORT, e);
				}
			}
		}
	}
}