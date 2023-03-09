package memory;

import java.util.*;

import constants.RecognizedRepresentations;
import frames.entities.Entity;
import utils.EntityUtils;

/**
 * Useful for pulling apart reps and putting them back together.
 * 
 * @author sglidden
 */
public class RepProcessor {

	/**
	 * Extracts representations hidden within Thing t. For example, a Trajectory might contain a set of Path Elements.
	 * This will extract them.
	 * 
	 * @param t
	 *            Thing
	 * @return Set of sub-representations
	 */
	public static Set<Entity> extractSubReps(Entity t) {
		Set<Entity> results = new HashSet<Entity>();
		Set<Entity> children = t.getDescendants();
		for (Entity c : children) {
			if (RecognizedRepresentations.ALL_THING_REPS.contains(EntityUtils.getRepType(c))) {
				results.add(c);
			}
			// else if (c.getPrimedThread().contains("trajectory")) {
			// results.add(wrap(c));
			// }
			// else if (c.getPrimedThread().contains("transition")) {
			// results.add(wrap(c));
			// }
		}
		return results;
	}

	/**
	 * Returns the concrete Things that make up a complex rep. Example: Trajectory: "A dog ran to a tree." Returns:
	 * Thing Dog; Thing Ran; Thing Tree
	 * 
	 * @param t
	 *            Thing representation
	 * @return Set of Things
	 */
	public static Set<Entity> extractAtoms(Entity t) {
		// hack: look for keywords on a Thing's primed-thread
		Entity clone = t.deepClone();
		Set<Entity> children = clone.getDescendants();
		Set<Entity> results = new HashSet<Entity>();
		for (Entity c : children) {
			if (!c.entityP()) continue; // skip nested things
			if (c.getTypes().contains("entity")) {
				results.add(c);
			}
			if (c.getTypes().contains("action")) {
				results.add(c);
			}
		}
		return results;
	}

	// borrowed from engineering/Demultiplexor for now
	public static Entity wrap(Entity thing) {
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

	public static Entity unwrap(Entity thing) {
		// if ((EntityUtils.getRepType(thing) == RecognizedRepresentations.TRAJECTORY_THING ||
		// EntityUtils.getRepType(thing) == RecognizedRepresentations.TRANSITION_THING)) {
		// return thing.getElement(0).getElement(0);
		// }
		return thing;
	}
}
