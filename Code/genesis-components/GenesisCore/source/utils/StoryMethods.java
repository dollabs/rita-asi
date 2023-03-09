package utils;

import java.util.List;
import java.util.Vector;

import matchers.StandardMatcher;
import utils.minilisp.LList;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;

/*
 * Created on Aug 6, 2012
 * @author phw
 */

public class StoryMethods {

	public StoryMethods() {
	}

	// TODO remove this
	public static boolean isPHW() {
		return !Webstart.isWebStart() && "phw".equalsIgnoreCase(System.getProperty("user.name"));

	}

	/**
	 * Determines if element is inferred by looking at inferences in the story.
	 */
	public static boolean isInferred(Entity t, Sequence story) {
		for (Entity element : story.getElements()) {
			if (element.relationP(Markers.CAUSE_MARKER)) {
				if (t == element.getObject()) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * Does breadth-first search so as to get the shortest path; null means no path found or the path is from d to s.
	 */
	public static Vector<Entity> findPath(Entity s, Entity d, Sequence story) {
		if (s == null || d == null) {
			return new Vector<Entity>();
		}
		Vector<Vector<Entity>> queue = new Vector<Vector<Entity>>();
		Vector<Entity> extendedList = new Vector<Entity>();
		// Get started
		Vector<Entity> initialPath = new Vector<Entity>();
		initialPath.add(s);
		queue.add(initialPath);

		while (!queue.isEmpty()) {
			Vector<Entity> path = queue.firstElement();
			queue.remove(0);
			Entity lastElement = path.lastElement();

			// LList<PairOfEntities> bindings = BasicMatcher.getBasicMatcher().match(d, lastElement);

			/**
			 * Unbelievable hack
			 */
			// if (bindings!=null) {
			if (d.hash().equals(lastElement.hash())) {
				// Mark.say("Evidently", d.asString(), "matches", lastElement.asString(), lastElement.hash());
				return path;
			}
			if (extendedList.contains(lastElement)) {
				continue;
			}
			else {
				extendedList.add(lastElement);
			}
			// Find extensions of path and put them at the end for breadth first search
			for (Entity element : story.getElements()) {
				if (element.relationP(Markers.CAUSE_MARKER) || element.relationP(Markers.EXPLANATION_RULE)) {
					Vector<Entity> antecedents = element.getSubject().getElements();
					boolean match = false;
					for (Entity antecedent : antecedents) {
						LList<PairOfEntities> matchings = StandardMatcher.getBasicMatcher().match(antecedent, lastElement);
						if (matchings != null) {
							match = true;
						}
					}

					if (match) {
						Vector<Entity> newPath = new Vector<Entity>();
						newPath.addAll(path);
						newPath.add(element.getObject());
						queue.add(newPath);
					}

				}
			}
		}
		return null;
	}
}
