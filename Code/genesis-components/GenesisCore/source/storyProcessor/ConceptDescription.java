package storyProcessor;

import frames.entities.Entity;
import frames.entities.Sequence;
import utils.*;
import utils.minilisp.LList;

/*
 * Concept descriptions, instances of this class, are produced by the concept expert once the story has been read
 * completely. The concept expert builds these descriptions by inspecting the elaboration graph, looking for a
 * constellations of story elements that match those in each of the concept pattern considered.
 * @author phw
 */

public class ConceptDescription {

	private Sequence story;

	private String name;

	private LList<PairOfEntities> bindings;

	private Sequence rules;

	private Sequence instantiations = new Sequence();

	private Sequence storyElementsInvolved = new Sequence();
	
	private Sequence consequences = new Sequence();

	public ConceptDescription() {
	}

	public String toString() {
		return getName();
	}

	public ConceptDescription(String name, LList<PairOfEntities> bindings, Sequence storyElementsInvolved, Sequence instantiations, Sequence consequences, Sequence story) {
		super();
		this.name = name;
		this.bindings = bindings;
		this.storyElementsInvolved = storyElementsInvolved;
		// Mark.say("\n");
		// Mark.say("Story elements involved in", name);
		// for (Thing t : storyElementsInvolved.getElements()) {
		// Mark.say(t.asString());
		// }
		// Mark.say("\n");
		this.instantiations = instantiations;
		this.consequences = consequences;
		this.story = story;
		// getRules();
	}

	public Sequence getConsequences() {
	return consequences;}

	/*
	 * Work done in getter, so only done if needed.
	 */
	public Sequence getRules() {
		if (rules == null) {
			// Mark.say("Getting rules for reflection", getName());
			rules = new Sequence();
			for (Entity t : instantiations.getElements()) {
				if (t.sequenceP("path")) {
					for (Entity p : t.getElements()) {
						// Look for each path element as consequent in an
						// instantiated rule
						for (Entity s : story.getElements()) {
							if (s.relationP("cause")) {
								if (s.getObject() == p) {
									rules.addElement(s);
								}
							}
						}
					}
				}
			}
		}
		return rules;
	}

	/**
	 * Returns the name of the concept matched.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a binding list of variable-value pairs indicating how concept-pattern variables matched up with entities
	 * in the story elements. For example, one such binding might indicate that variable xx corresponds to Macbeth.
	 */
	public LList<PairOfEntities> getBindings() {
		return bindings;
	}

	/**
	 * 
	 */
	public Sequence getInstantiations() {
		return instantiations;
	}

	/*
	 * Returns the story in which the reflection was observed.
	 */
	public Sequence getStory() {
		return story;
	}

	/*
	 * A list of all the story elements involved in the reflection noted.
	 */
	public Sequence getStoryElementsInvolved() {
		return storyElementsInvolved;
	}
}
