package utils.tools;

import java.util.Vector;

import constants.Markers;
import dictionary.BundleGenerator;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.ISpeak;
import matchers.StandardMatcher;
import storyProcessor.ConceptExpert;
import utils.*;
import utils.minilisp.LList;


/*
 * Provides utilities for identifying places where actions lead to prescribed effects. Created on Apr 26, 2014
 * @author phw
 */

public class Filters {

	public static Vector<Relation> findActorWhoseActionLeadsToEffect(Entity effect, Sequence sequence) {
		Vector<Entity> story = sequence.getElements();
		Vector<Entity> inferences = keepOnlyInferences(story);
		Vector<Relation> results = new Vector<>();

		for (int i = 0; i < story.size(); ++i) {

			// Locate action in which actor is the subject
			Entity x = story.get(i);
			if (Predicates.isAction(x)) {
				// Ok, have a match, look for effect downstream
				for (int r = i + 1; r < story.size(); ++r) {
					Entity candidate = story.get(r);
					// Mark.say("Trying to match\n", effect, "\n", candidate);
					LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(effect, candidate);
					// if (candidate.getType().equals("harm")) {
					// Mark.say("Hello world");
					// Mark.say("E", effect.toXML());
					// Mark.say("C", candidate.toXML());
					// }
					if (bindings != null) {
						// Mark.say("Matched", candidate, effect);
						if (ConceptExpert.isConnectedViaInferences(x, candidate, inferences) != null) {
							results.add((Relation) x);
						}
					}
				}
			}
		}
		return results;
	}

	public static Vector<Relation> findActionsBy(Object object, Vector<Relation> actions) {
		String actor;
		if (object instanceof Entity) {
			actor = ((Entity) object).getType();
		}
		else {
			actor = object.toString();
		}
		Vector<Relation> result = new Vector<>();
		for (Relation e : actions) {
			if (e.getSubject().getType().equals(actor)) {
				result.addElement(e);
			}
		}
		return result;
	}

	public static Vector<Relation> findHarmingActions(Sequence story) {
		Entity harm = getHarm();
		return findActorWhoseActionLeadsToEffect(harm, story);
	}

	public static Vector<Relation> findHelpingActions(Sequence story) {
		Entity actor = getPerson("xx");
		Entity help = getHelp();
		return findActorWhoseActionLeadsToEffect(help, story);
	}

	private static Vector<Entity> keepOnlyInferences(Vector<Entity> story) {
		Vector<Entity> inferences = new Vector<>();
		for (Entity e : story) {
			if (Predicates.isCause(e)) {
				inferences.addElement(e);
			}
		}
		return inferences;
	}

	public static Entity getPerson(String name) {
		Entity xx = new Entity(BundleGenerator.getBundle("person"));
		xx.addType(Markers.NAME);
		xx.addType(name);
		return xx;
	}

	public static Entity getHarm() {
		Entity r = ISpeak.makeRoleFrame(getPerson("xx"), "harm", getPerson("yy"));
		r.limitToRoot(Markers.ACTION_MARKER);
		return r;
	}

	public static Entity getHelp() {
		Entity r = ISpeak.makeRoleFrame(getPerson("xx"), "help", getPerson("yy"));
		r.limitToRoot(Markers.ACTION_MARKER);
		return r;
	}

	public static void main(String[] ignore) {
		Mark.say(getHarm().toXML());
	}

}
