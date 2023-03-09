package jessicaNoss;

import storyProcessor.StoryProcessor;
import utils.Mark;
//
import connections.*;
import connections.signals.BetterSignal;
import frames.entities.Sequence;

/**
 * A local processor class that just receives a complete story description, takes apart the object to fetch various
 * parts of the complete story description, and prints them so I can see what is in there.
 */
public class LocalProcessor extends AbstractWiredBox {

	public final String MY_INPUT_PORT = "my input port";

	public final String MY_OUTPUT_PORT = "my output port";

	/**
	 * The constructor places the processSignal signal processor on two input ports for illustration. Only the
	 * StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT is wired to in LocalGenesis.
	 */
	public LocalProcessor() {
		this.setName("Local story processor");
		// Example of default port connection
		Connections.getPorts(this).addSignalProcessor("processSignal");
		// Example of named port connection
		Connections.getPorts(this).addSignalProcessor(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, "processSignal");
	}

	/**
	 * I have to make all signal processors void methods of one argument, which must be of the Object class, so there
	 * will be a bit of casting.
	 */
	public void processSignal(Object signal) {
		// Should always check to be sure my input is in the expected form and ignore it if not. A BetterSignal is just
		// a convenient container for multiple objects that allows easy extraction of objects without further casting.
		if (signal instanceof BetterSignal) {
			// Shows how to take BetterSignal instance apart, the one coming in on COMPLETE_STORY_ANALYSIS_PORT port.
			BetterSignal s = (BetterSignal) signal;
			Sequence story = s.get(0, Sequence.class);
//			Sequence explicitElements = s.get(1, Sequence.class);
//			Sequence inferences = s.get(2, Sequence.class);
//			Sequence concepts = s.get(3, Sequence.class);
			// Now proceed to print what has come into my box.
			// Mark.say("\n\n\nStory elements");
			// for (Entity e : story.getElements()) {
			// Mark.say(e.asString());
			// }
			// Mark.say("\n\n\nExplicit story elements");
			// for (Entity e : explicitElements.getElements()) {
			// Mark.say(e.asString());
			// }
			// Mark.say("\n\n\nInstantiated commonsense rules");
			// for (Entity e : inferences.getElements()) {
			// Mark.say(e.asString());
			// }
			// Mark.say("\n\n\nInstantiated concept patterns");
			// for (Entity e : concepts.getElements()) {
			// Mark.say(e.asString());
			// }

//			story.getElements().stream().forEach(e -> noteEntry(e));
			story.getElements().stream().forEach(e -> Mark.say(e));
//			Mark.say("---------------");
	
// Moved to JessicasExpert
//			Vector<Entity> elements = story.getElements();
//			HashMap<Entity, Entity> observations = new HashMap<Entity, Entity>();
//			Set<Entity> actorsInScene = new HashSet<Entity>();
//			for (Entity e : elements) {
//				Entity subject = e.getSubject();
//				if (noteEntry(e)) {
//					actorsInScene.add(subject);
//					Mark.say("Entered:", subject);
//				} else if (noteExit(e)) {
//					actorsInScene.remove(subject);
//					Mark.say("Exited:", subject);
//				} else {
//					//e is some event other than enter/exit
//					for (Entity actor : actorsInScene) {
//						observations.put(actor, e);
//						Mark.say("Actor", actor, "observed:", e);
//					}
//				}
//			}
		}
	}

//	private boolean noteEntry(Entity e) {
//		return e.isA("enter") && RoleFrames.getObject(e).isA("scene");
////		return noteEntryOrExit(e, false);
//	}
//
//	private boolean noteExit(Entity e) {
//		return e.isA("exit") && RoleFrames.getObject(e).isA("scene");
////		return noteEntryOrExit(e, true);
//	}
	
//	private boolean noteEntryOrExit(Entity e, boolean isExit) {
//		if (e.isA(isExit ? "exit" : "enter") && Getters.getObject(e).isA("scene")) {
//			Mark.say("Noted", isExit ? "exit of" : "entry of", e.getSubject());
//			return true;
//		}
//		return false;
//	}
}