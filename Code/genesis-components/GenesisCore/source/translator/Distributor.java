package translator;

import java.util.*;

import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import memory2.M2;

/*
 * Created on Nov 23, 2007 @author phw
 */

public class Distributor extends AbstractWiredBox {

	public final static String START_STORY = "startStory";

	public final static String TELL_STORY = "tellStory";

	public final static String STOP_STORY = "stopStory";

	public final static String THREAD = "thread";

	public final static String PLACE = "place";

	public final static String PATH_ELEMENT = "pathElement";

	public final static String TRAJECTORY = "trajectory";

	public final static String TRANSITION = "transition";

	public final static String TRANSFER = "transfer";

	public final static String ROLES = "roles";

	public final static String CAUSE = "cause";

	public final static String COERCE = "coerce";

	public final static String TIME = "time";

	public final static String QUESTION = "question";

	public final static String IMAGINE = "imagine";

	public final static String DESCRIBE = "describe";

	Entity input;

	public Distributor() {
		super("Distributor");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object o) {
		input = null;
		// System.out.println("Input to demultiplexor:\n" + input);
		if (o instanceof Entity) {
			input = (Entity) o;
			transmitMem(input);
			sort(input);
		}
	}

	private void transmitMem(Entity t) {
		if (t.sequenceP() && t.getElements().size() > 0) {
			t = t.getElements().get(0);
		}
		transmit(M2.PORT_ENGLISH, t);
	}

	private void transmit(String port, Entity t) {
		// System.out.println("Transmitting on port " + port);
		Connections.getPorts(this).transmit(port, t);
		// Connections.getPorts(this).transmit(t); // I (Sam) think the default is for the memory only; I made a port
		// just for it.
	}

	public void sort(Object o) {
		if (!(o instanceof Entity)) {
			return;
		}
		Entity thing = (Entity) o;
		if (thing.relationP()) {
			sort(((Relation) thing).getSubject());
			sort(((Relation) thing).getObject());

			if (thing.isA("question")) {
				transmit(QUESTION, wrap(thing));
			}
			else if (thing.isA("trajectory")) {
				transmit(TRAJECTORY, wrap(thing));
			}
			else if (thing.isA(ROLES)) {
				transmit(ROLES, thing);
			}
			else if (thing.isA("transfer")) {
				transmit(TRANSFER, thing);
			}
			else if (thing.isA("because") || thing.isA("cause")) {
				transmit(CAUSE, thing);
			}
			else if (thing.isA("coerce")) {
				transmit(COERCE, thing);
			}
			else if (thing.isA("ask")) {
				transmit(COERCE, thing);
			}
			else if (thing.isA("before") || thing.isA("after") || thing.isA("while")) {
				transmit(TIME, thing);
			}
			else if (thing.isA("classification")) {
				transmit(THREAD, thing);
			}
		}
		if (thing.functionP()) {
			sort(((Function) thing).getSubject());
			if (thing.isA("question")) {
				transmit(QUESTION, thing);
			}
			else if (thing.isA("imagine")) {
				transmit(IMAGINE, thing);
			}
			else if (thing.isA(Markers.DESCRIBE_MARKER)) {
				transmit(DESCRIBE, thing);
			}
			else if (thing.isA("transition")) {
				transmit(TRANSITION, wrap(thing));
			}
		}
		if (thing.sequenceP()) {
			Sequence s = (Sequence) thing;
			for (Entity t : s.getElements())
				sort(t); // added by harold 6/26/08
			// is there a reason not to do this?
			if (s.isA("path") || s.isA("location")) {
				Vector v = s.getElements();
				for (Iterator<Function> i = v.iterator(); i.hasNext();) {
					Function element = i.next();
					if (s.isA("path")) {
						transmit(PATH_ELEMENT, element);
						transmit(PLACE, element.getSubject());
					}
					transmit(PLACE, element);
				}
			}
		}
	}

	private Entity wrap(Entity thing) {
		// if (thing.isA("trajectory")) {
		// Sequence ladder = JFactory.createTrajectoryLadder();
		// Sequence space = JFactory.createTrajectoryEventSpace();
		// ladder.addElement(thing);
		// space.addElement(ladder);
		// return space;
		// }
		// else if (thing.isA("transition")) {
		// Sequence ladder = BFactory.createTransitionLadder();
		// Sequence space = BFactory.createTransitionEventSpace();
		// ladder.addElement(thing);
		// space.addElement(ladder);
		// return space;
		// }
		// else if (thing.isA("question") && thing.functionP()) {
		// Derivative question = (Derivative) thing;
		// Thing content = question.getSubject();
		// question.setSubject(wrap(content));
		// return question;
		// }
		return thing;

	}

	public Entity getInput() {
		return input;
	}
}
