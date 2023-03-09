package frames;

import javax.swing.JFrame;

import translator.Distributor;

import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import gui.ForceViewer;
import memory.Memory;
import java.util.List;
import java.util.ArrayList;

/**
 * Created on January 15, 2008
 * 
 * @author ryscheng Combines use of memory and substrate frames to create a ForceFrame
 */

public class ForceInterpreter extends AbstractWiredBox {
	/**
	 * Contains one input port and one output port. Takes in frames in form of substrate (usually from Demultiplexor)
	 * Outputs a ForceFrame relation to go to the ForceViewer
	 */

	/**
	 * forceWords = words that assume antagonist wins forceThread = the thread to search for in memory in ambiguous
	 * cases causeWords = words that assume antagonist wins in causal relations activeWords = words to look for in
	 * agonist's verb thread to check for action
	 */
	public static final String[] forceWords = { "coerce" };

	public static final String[] forceThread = { "action", "induce", "compel", "coerce", "force", "forced" };

	public static final String[] causeWords = { "because", "cause" };

	public static final String[] activeWords = { "trajectory", "transition" };

	public ForceInterpreter() {
		super("Force interpreter");
		Connections.getPorts(this).addSignalProcessor("view");
	}

	public void view(Object signal) {
		if (signal instanceof Relation) {
			Relation force = (Relation) signal;
			ForceInterpreter.this.setParameters(force);
		}
	}

	/**
	 * Finds properties: - Antagonist, Agonist, Strengths, Tendencies, Shifts, and transmits a ForceFrame relation
	 * across the output wire
	 * 
	 * @param force
	 *            - Force relation in the form of the substrate
	 */
	public void setParameters(Relation force) {
		Entity agonist, antagonist;
		String agoShift, agoStrength, agoTend, antShift;

		// Assume the agonist is always the object.
		// Consequence of the parser
		agonist = ForceInterpreter.getAgonist(force);
		antagonist = ForceInterpreter.getAntagonist(force);

		// Relation's type determines the result and strengths
		agoStrength = ForceInterpreter.findAgonistStrength(force);
		agoTend = ForceInterpreter.findAgonistTendency(force);

		// TBD - Unknown shift patterns. Setting notExit as default
		agoShift = "notExit";
		antShift = "notExit";
		Relation forceRelation = ForceFrame.makeForceRelation(agonist, agoShift, agoStrength, agoTend, antagonist, antShift);
		if (force.hasFeature(Markers.NOT)) {
			forceRelation.addFeature(Markers.NOT);
		}
		// Relation forceRelation = (Relation)
		// ForceFrame.getMap().get("The shed kept standing despite the wind blowing against it.");
		transmit(forceRelation);
	}

	/**
	 * @param force
	 *            - Force Relation
	 * @return Either "restful" or "active", depending if the agonist's verb is a form of "trajectory" or "transition"
	 */
	public static String findAgonistTendency(Relation force) {
		return findAgonistTendencyHelper(force.getObject());
	}

	/**
	 * @param force
	 *            - Force Relation
	 * @return Either "restful" or "active", depending if the agonist's verb is a form of "trajectory" or "transition"
	 */
	public static String findAntagonistTendency(Relation force) {
		return ForceFrame.oppositeStrength(ForceInterpreter.findAgonistTendency(force));
	}

	/**
	 * Find's the tendency of the agonist
	 * 
	 * @param agoTree
	 *            - Thing containing agonist
	 * @return Either "restful" or "active, depending if the agonist's verb is a form of "trajectory" or "transition"
	 */
	private static String findAgonistTendencyHelper(Entity agoTree) {
		// Look for the Relation in this tree:
		// If a sequence, try to find first relation's tendency
		if (agoTree instanceof Sequence) {
			return ForceInterpreter.findAgonistTendencyHelper(agoTree.getElement(0));
		}
		else if (agoTree instanceof Relation) {
			String result;

			// If the thread contains "trajectory", agonist verb is "active"
			if (agoTree.isAnyOf(ForceInterpreter.activeWords)) {
				result = ForceFrame.tendencies[0];
			}// Otherwise, it is "restful"
			else {
				result = ForceFrame.tendencies[1];
			}

			// We can assume that the agonist always wants to do opposite of what is requested
			result = ForceFrame.oppositeTendency(result);

			return result;
		} // Currently same handling as Relation
		else if (agoTree instanceof Function) {
			String result;

			// If the thread contains "transition", agonist verb is "active"
			if (agoTree.isAnyOf(ForceInterpreter.activeWords)) {
				result = ForceFrame.tendencies[0];
			}// Otherwise, it is "restful"
			else {
				result = ForceFrame.tendencies[1];
			}

			// We can assume that the agonist always wants to do opposite of what is requested
			result = ForceFrame.oppositeTendency(result);

			return result;
		}
		// Unknown agonist tree structure - return "unknown"
		else
			return ForceFrame.tendencies[2];
	}

	/**
	 * Finds the strength of the antagonist. From previous checking, we can assume that the input is of a Relation of
	 * TYPE = 'force'
	 * 
	 * @param force
	 *            - The entire force relation straight from input
	 * @return Either "strong" or "weak" depending on which entity is stronger
	 */
	public static String findAgonistStrength(Relation force) {
		return ForceFrame.oppositeStrength(ForceInterpreter.findAntagonistStrength(force));
	}

	/**
	 * Finds the strength of the antagonist. From previous checking, we can assume that the input is of a Relation of
	 * TYPE = 'force'
	 * 
	 * @param force
	 *            - The entire force relation straight from input
	 * @return Either "strong" or "weak" depending on which entity is stronger
	 */
	public static String findAntagonistStrength(Relation force) {
		// If thread contains "coerce", we know antagonist is stronger
		// Remember that in memory
		if (force.isAnyOf(ForceInterpreter.forceWords)) {
			// ForceMemory.addCompareRelation(compareRelations, antagonist, agonist);
			return ForceFrame.strengths[0];
		}
		// If thread contains "because", we know antagonist is stronger
		// but this isn't always true, so don't remember it
		else if (force.isAnyOf(ForceInterpreter.causeWords)) {
			return ForceFrame.strengths[0];
		}
		// If ambiguous who is stronger, find stronger relations in memory
		else {
			if (ForceMemory.isForceRelation(force)) {
				return ForceFrame.strengths[0];
			}
		}
		// Catch-all: "unknown"
		return ForceFrame.strengths[4];
	}

	public static Entity getAgonist(Relation force) {
		return ForceInterpreter.findSubjectThing(force.getObject());
	}

	public static Entity getAntagonist(Relation force) {
		return ForceInterpreter.findSubjectThing(force.getSubject());
	}

	/**
	 * Given a Thing, we want to find the first noun Thing (not a Derivative, Relation, or Sequence). TODO Currently it
	 * cannot deal with sequences - returns "unknown" Thing
	 * 
	 * @param input
	 *            - Any thing
	 * @return Thing that is not a Derivative, Relation, or Sequence
	 */
	public static Entity findSubjectThing(Entity input) {
		if (input instanceof Relation) {
			return ForceInterpreter.findSubjectThing(((Relation) input).getSubject());
		}
		else if (input instanceof Function) {
			return ForceInterpreter.findSubjectThing(((Function) input).getSubject());
		}
		else if (input instanceof Sequence) {
			return new Entity("unknown");
		}
		else {
			return input;
		}
	}

	// Transmits to default output port
	private void transmit(Entity t) {
		Connections.getPorts(this).transmit(Markers.VIEWER, t);
	}

	private void transmit(ForceFrame f) {
		transmit(f.getThing());
	}

	/**
	 * @param args
	 *            main function currently doesn't test interpreter's ability
	 */
	public static void main(String[] args) {
		ForceViewer view = new ForceViewer();

		JFrame frame = new JFrame();
		frame.getContentPane().add(view);
		frame.setBounds(0, 0, 200, 200);
		frame.setVisible(true);
		view.view(ForceFrame.getMap().get("The ball kept rolling despite the stiff grass"));

	}

}
