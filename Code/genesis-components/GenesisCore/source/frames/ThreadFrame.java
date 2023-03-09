package frames;

import constants.RecognizedRepresentations;
import frames.entities.Entity;
import frames.memories.BasicMemory;

/*
 * Created on Jul 24, 2006
 * @author phw
 */
public class ThreadFrame extends Frame {
	public static String FRAMETYPE = (String) RecognizedRepresentations.THREAD_THING;

	private String subclass;

	private String superclass;

	private Entity thing;

	private BasicMemory memory = BasicMemory.getStaticMemory();

	public ThreadFrame(String subclass, String superclass) {
		this.subclass = subclass;
		this.superclass = superclass;
		thing = new Entity();
		thing.addType(superclass);
		thing.addType(subclass);
		// Storage happens in Memory Expert
		// memory.store(thing);
		memory.extendVia(thing, "thing");
		// System.out.println("Result of ThreadFrameProcessing:" + thing);
	}

	public Entity getThing() {
		return thing;
	}

	public Entity getThingRepresentation() {
		return thing;
	}

	public String toString() {
		if (getThing() != null) {
			return getThing().toString();
		}
		return "";
	}
}
