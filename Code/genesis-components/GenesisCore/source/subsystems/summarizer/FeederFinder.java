package subsystems.summarizer;

import generator.Generator;

import java.util.*;
import java.util.stream.Collectors;

import constants.Switch;
import frames.entities.Entity;
import storyProcessor.ConceptDescription;
import utils.Mark;
import utils.tools.Predicates;

/*
 * Created on Sep 21, 2014
 * @author phw
 */

public class FeederFinder {

	private static FeederFinder feederFinder = null;

	private FeederFinder() {

	}

	public static FeederFinder getFeederFinder() {
		if (feederFinder == null) {
			feederFinder = new FeederFinder();
		}
		return feederFinder;
	}

	public Set<Entity> findFeeders(ConceptDescription description, Vector<Entity> story) {
		Set<Entity> result = new HashSet<>();
		for (Entity e : description.getStoryElementsInvolved().getElements()) {
			result.addAll(findFeeders(e, story));
		}

		return result;
	}

	public Collection<? extends Entity> findFeeders(Entity e, Vector<Entity> story) {
		return findFeeders(e, story, new HashSet<Entity>());
	}

	public Collection<? extends Entity> findFeeders(Entity e, Vector<Entity> story, Set<Entity> encountered) {
		Set<Entity> result = new HashSet<>();
		for (Entity element : story) {
			if (Predicates.isCause(element) && !Predicates.isMeans(element) && !Predicates.isExplanation(element)) {
				if (e == element.getObject()) {
					for (Entity antecedent : element.getSubject().getElements()) {
						if (!encountered.contains(antecedent)) {
							encountered.add(antecedent);
							result.addAll(findFeeders(antecedent, story, encountered));
						}
						else {
							// Mark.err("Hit same entity again in findFeeders at", antecedent);
						}
					}
				}
			}
			else if (Predicates.isCause(element) && Predicates.isMeans(element)) {
				if (e == element.getObject()) {
					// Mark.say("Found means expression", element);
					// Keep the gory details
					result.add(element);
					// Also recurse through
					for (Entity antecedent : element.getSubject().getElements()) {
						if (!encountered.contains(antecedent)) {
							encountered.add(antecedent);
							result.addAll(findFeeders(antecedent, story, encountered));
						}
					}
				}
			}
		}
		if (result.isEmpty()) {
			result.add(e);
		}
		return result;
	}

	/*
	 * Tracks back from elements found in concepts to root causes, with retention of inferences.
	 */
	public Set<Entity> findSummaryFeeders(ConceptDescription description, Vector<Entity> story, boolean debug) {
		Set<Entity> result = new HashSet<>();
		Set<Entity> extended = new HashSet<>();
		List<Entity> inferences = extractInferences(story);
		Mark.say(debug, "\n>>>  Working on", description.getName());
		for (Entity origin : description.getStoryElementsInvolved().getElements()) {
			result.addAll(findSummaryFeeders(story, origin, inferences, extended, debug));
		}
		return result;
	}

	private List<Entity> extractInferences(Vector<Entity> story) {
		return story.stream().map(f -> Predicates.isCause(f) ? f : null).filter(p -> p != null).collect(Collectors.toList());
	}

	/**
	 * Queue based search for feeders and participating inferences
	 */
	public Set<Entity> findSummaryFeeders(Vector<Entity> story, Entity origin, List<Entity> inferences, Set<Entity> extended, boolean debug) {
		Vector<Vector<Entity>> queue = new Vector<Vector<Entity>>();
		Set<Entity> result = new HashSet<>();

		Mark.say(debug, "\n>>>  Working on paths from", Generator.getGenerator().generate(origin));

		// Initialize queue

		Vector<Entity> path = new Vector<Entity>();
		path.add(origin);
		queue.add(path);

		// Search
		while (!queue.isEmpty()) {
			Vector<Entity> firstPath = queue.firstElement();
			queue.remove(0);
			Entity lastElement = firstPath.lastElement();
			// Already searched beyond here
			if (extended.contains(lastElement)) {
				Mark.say(debug, "Already looked past", Generator.getGenerator().generate(lastElement));
				continue;
			}
			else {
				extended.add(lastElement);
			}
			boolean more = false;
			Mark.say(debug, "Working on", Generator.getGenerator().generate(lastElement));
			// Look through all inferences; when one is found that can continue a path, create a new path for it and add
			// new path to queue.
			for (Entity inference : inferences) {
				Vector<Entity> antecedents = inference.getSubject().getElements();
				Entity consequent = inference.getObject();
				if (consequent == lastElement) {
					// Don't seek beyond a presumption consequent if consequent is in story
					if (!Switch.includePresumptions.isSelected() && Predicates.isPresumption(inference)) {
						Mark.say(debug, "Presumption", inference);
					}
					// Don't seek beyond an abduction consequent if consequent is in story
					else if (!Switch.includeAbductions.isSelected() && Predicates.isAbduction(inference)) {
						Mark.say(debug, "Abduction", inference);
					}
					else {
						Mark.say(debug, "Not presumption", inference);
						result.add(inference);
						for (Entity antecedent : antecedents) {
							Vector<Entity> newPath = new Vector<Entity>();
							newPath.addAll(firstPath);
							newPath.add(antecedent);
							queue.add(0, newPath);
							more = true;
						}
					}
				}
			}
			if (!more) {
				// Nothing left, cannot continue path, this must be a feeder
				Mark.say(debug, "Cannot continue path,", Generator.getGenerator().generate(lastElement), "is a feeder");
				result.add(lastElement);
			}
		}
		// Could not find a path
		return result;
	}
	// public Set<Entity> findFeeders(Entity origin, Sequence theInstantiatedConcept, Sequence theStorySequence) {
	// return findFeeders(origin, theInstantiatedConcept, theStorySequence, new HashSet<Entity>());
	// }
	//
	// public Set<Entity> findFeeders(Entity origin, Sequence theInstantiatedConcept, Sequence theStorySequence,
	// Set<Entity> encountered) {
	// boolean debug = false;
	// Mark.say(debug, "Looking for feeders of", origin);
	// Mark.say(debug, "From concept", theInstantiatedConcept);
	// Mark.say(debug, "In ", theStorySequence);
	// Set<Entity> feeders = new HashSet<Entity>();
	//
	// if (origin == null || theInstantiatedConcept == null) return feeders;
	// Vector<Entity> story = theStorySequence.getElements();
	// boolean found = false;
	// for (Entity element : story) {
	// if (Predicates.isCause(element)) {
	// if (origin == element.getObject()) {
	// found = true;
	// // if (element.isA(Markers.EXPLANATION) || element.isA(Markers.ENTAIL_MARKER) ||
	// // element.isA(Markers.ABDUCTION)) {
	// if (true || element.isA(Markers.PREDICTION_RULE)) {
	// feeders.add(element);
	// }
	// else if (!element.isA(Markers.PREDICTION_RULE)) {
	// feeders.add(element);
	// // Screwed up everything; stops too soon.
	// if (theInstantiatedConcept.getElements().contains(origin)) {
	// // for (Entity x : theInstantiatedConcept.getElements()) {
	// // Mark.say(debug, "Instantiation:", x);
	// // }
	// // Stop here with explanation
	// Mark.say(debug, "Stopping at", Generator.getGenerator().generate(element));
	// return feeders;
	// }
	// else {
	// // Mark.say(debug, "NOT stopping at", e);
	// }
	// }
	// for (Entity antecedent : element.getSubject().getElements()) {
	// // Mark.say(debug, "Looking for connections to", antecedent);
	// if (!encountered.contains(antecedent)) {
	// encountered.add(antecedent);
	// for (Entity x : findFeeders(antecedent, theInstantiatedConcept, theStorySequence, encountered)) {
	// // Not necessarily a circularity; probably just two paths to same place
	// if (feeders.contains(x)) {
	// Mark.say(debug, "Hit same entity again in findFeeders at", x);
	// }
	// else {
	// feeders.add(x);
	// }
	// }
	// }
	// else {
	// Mark.err(debug, "Noted loop at", antecedent);
	// }
	// }
	// }
	//
	// }
	// }
	// if (!found) {
	// feeders.add(origin);
	// }
	// // for (Entity x : feeders) {
	// // Mark.say(debug, "Feeders include", x);
	// // }
	// return feeders;
	//
	// }

}
