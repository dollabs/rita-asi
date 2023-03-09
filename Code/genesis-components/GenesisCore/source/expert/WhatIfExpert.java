package expert;

import generator.Generator;
import genesis.GenesisGetters;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

import matthewFay.StoryAlignment.Aligner;
import matthewFay.StoryAlignment.Alignment;
import matthewFay.StoryAlignment.SortableAlignmentList;
import matthewFay.Utilities.Pair;
import mentalModels.MentalModel;
import storyProcessor.StoryProcessor;
import subsystems.summarizer.Summarizer;
import utils.Mark;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;

/*
 * Created on Oct 4, 2014
 * @author phw
 */

public class WhatIfExpert extends AbstractWiredBox {

	public static final String FROM_QUESTION_EXPERT = "from question processor";
	
	public static final String TO_MEANS_ENDS_EXPERT = "ask means-ends processor for additional help";


	public static final String TO_SECOND_PERSPECTIVE = "to second perspective story elements";

	public static final String TO_SECOND_RULES = "to second perspective's rules";

	public static final String TO_SECOND_TRAITS = "to second perspective's traits";

	public static final String TO_SECOND_CONCEPTS = "to second perspective's concepts";

	public static final String HTML = "html output displays results";

	public WhatIfExpert() {
		super("What-if expert");
		Connections.getPorts(this).addSignalProcessor(FROM_QUESTION_EXPERT, this::process);
	}

	public void process(Object object) {
		boolean debug = true;
		boolean debug_transfer_story_verbatim = false;

		if (object instanceof BetterSignal) {
			BetterSignal signal = (BetterSignal) object;
			String key = signal.get(0, String.class);
			// Test to see if it is the right kind of question
			Entity hypothetical = signal.get(1, Entity.class);
			MentalModel model = signal.get(2, MentalModel.class);

			if (key == QuestionExpert.WHAT_IF) {
				//Mark.say("\n\n>>> ", "Aha, found my way into new WhatIfExpert!!!");
				
				// So, it came out of the question expert with a WHAT_IF key, an hypothetical Entity, and a MentalModel,
				// so go get them

				StoryProcessor storyProcessor = model.getStoryProcessor();
				Sequence revisedStory = new Sequence();
				Sequence explicitElements = storyProcessor.getExplicitElements();

				
				for (Entity e : explicitElements.getElements()) {
					//Mark.say("explicitly in first", e);

				}
				
				if (debug_transfer_story_verbatim) {
					Connections.getPorts(this).transmit(TO_SECOND_PERSPECTIVE, model.getStoryProcessor().getExplicitElements());

					Mark.say("Concept patterns ");
					for (Entity e : model.getStoryProcessor().getConceptPatterns().getElements()) {
						Mark.say("concept ", e);
					}
					Mark.say("Commonsense rules ");
					for (Entity e : model.getStoryProcessor().getCommonsenseRules().getElements()) {
						Mark.say("rule ", e);
					}

					return;
				}

				// See if the question is of the form "What if Macbeth did not murder Duncan."
				if (hypothetical.hasFeature(Markers.NOT)) {

					// If so, this is needed to remove negative marker, so that dereferencing will work.
					hypothetical.removeFeature(Markers.NOT);

					Mark.say(debug, "Without the negation, the hypothetical is\n", hypothetical);

					// Now we have an entity that, when dereferenced, will have same objects as in story
					Entity dereferencedHypothetical = storyProcessor.reassembleAndDereference(hypothetical);

					Mark.say(dereferencedHypothetical);
					
					for (Entity e : explicitElements.getElements()) {
						Mark.say("I am a story event:", e);
						if (e != dereferencedHypothetical) {
							revisedStory.addElement(e);
						}
					
					}
					// Stream<Entity> reducedElements = storyProcessor.getExplicitElements().getElements().stream();
					// reducedElements = reducedElements.filter(
					// // remove the dereferenced hypothetical
					// e -> e != dereferencedHypothetical &&
					// // Possibly due to the "recursive story insertion", we must also remove some entailments
					// !((Sequence)e).getElements().contains(dereferencedHypothetical)
					// //!e.isAPrimed(Markers.ENTAIL_MARKER)
					// );
					// reducedElements.forEach(f -> revisedStory.addElement(f));

					Mark.say(debug, "Original/revised story length", explicitElements.getElements().size(), revisedStory.getElements().size());

					Mark.say(debug, "Transmitting to SECOND_PERSPECTIVE");
					for (Entity e : revisedStory.getElements()) {
					//	Mark.say("toward second story", e);

					}
					Connections.getPorts(this).transmit(TO_SECOND_PERSPECTIVE, revisedStory);

					this.noteDifferences(dereferencedHypothetical);

				}

				else {
					// The question is of the form "What if Macbeth murdered Duncan," so it is not in the story. It will
					// require more thinking to resolve index numbers and then to figure out where to stick it.

					// Right now, just transmitting whole works, with new entity at the end, to be sure that nothing is
					// missing in second perspective.

					// dxh: The MeansEnds analyzer will handle these questions.
					
					return;
//					revisedStory.addAll(explicitElements);
//					revisedStory.addElement(hypothetical);
//
//					Mark.say(debug, "Original/revised story length", explicitElements.getElements().size(), revisedStory.getElements().size());
//					Mark.say(debug, "Transmitting to SECOND_PERSPECTIVE");
//					
//			
//					Connections.getPorts(this).transmit(TO_SECOND_PERSPECTIVE, revisedStory);

				}

			}
			else {
				Mark.say("Evidently not a what if question", hypothetical);
			}
		}
	}

	private void noteDifferences(Entity hypo) {
		Mark.say("Made it to noteDifferences");

		Generator generator = Generator.getGenerator();
		Aligner aligner = new Aligner();
		String bullet_points = "";
		HashMap<String, Boolean> redundant = new HashMap<String, Boolean>();

		Sequence allElts2 = GenesisGetters.getMentalModel2().getStoryProcessor().getStory();

		
		for (Entity e : allElts2.getElements()) {
			//Mark.say("second model's story element", e);

		}
		
		// Entity nn = (Entity)hypo.clone();
		// nn.addFeature(Markers.NOT);
		// Connections.getPorts(this).transmit(HTML, new BetterSignal("What-if analysis",
		// "<h1>"+generator.generate(nn)+"</h1>"));

		// *********************************** FROM AN EVENT-BASED PERSPECTIVE

		String event_result = "From an event-based perspective, I note the following changes:";

		Sequence infer1 = GenesisGetters.getMentalModel1().getStoryProcessor().getInferredElements();
		Sequence infer2 = GenesisGetters.getMentalModel2().getStoryProcessor().getInferredElements();

		Alignment<Entity, Entity> align = aligner.align(infer1, infer2).get(0);

		Vector<Entity> removedInferences = new Vector<Entity>();

		// Collect all inferences that are present in the first telling, but not the second
		align.stream().filter(p -> p.b == null).forEachOrdered(p -> removedInferences.add(p.a));

		// // PREVIOUSLY, ALIGN CONTAINED NO REPEATED INFERENCES.
		// for(Pair<Entity, Entity> p : align) {
		// if(p.b == null) {
		// if(removed.stream().filter(already -> WhatIfExpert.matchEntity(false, p.a.getObject(),
		// already.getObject())).count() == 0){
		// // p.a's consequent hasn't already been added to the list of removed elements
		// //Mark.say(removed);
		// //Mark.say("potential newcomer", generator.generate(p.a), p.a);
		// removed.add(p.a);
		// }
		// }
		// }
		removedInferences.forEach(p -> Mark.say("REMOVED: ", p));
		if (removedInferences.isEmpty()) {
			event_result = "From an event-based perspective, nothing changes.<br/>\n";
		}
		else {

			Mark.say("All removed elements");
			removedInferences.stream().forEachOrdered(r -> Mark.say(r.getSubject().getType() == Markers.CONJUNCTION ? "true" : "false", generator
			        .generate(r)));

			for (Entity removedElt : removedInferences) {
				String bullet = "";
				bullet_points += "<li>It's no longer the case that " + generator.generate(removedElt) + "</li>";
			}
			bullet_points = "<ul>" + bullet_points + "</ul>";

			for (Entity removedElt : removedInferences.subList(0, 0)) { // TODO: dxh: DEBUG

				// Case 1: an inference where (some of the) antecedents appear, but the consequent doesn't.
				// Response: "Although [antecedents], NOT(consequent)"

				// Case 2: an inference where neither the antecedents nor the consequents appear.
				// Response: "NOT(consequent)"

				// Case 3: the statement is simply absent
				// Response: "NOT(statement)"

				// Case 4: The consequent is present, but no antecedents are.

				// Since all the rules are presumed intact, perhaps we can look at whether
				// the rules are prediction-type (antecedents perhaps missing; conseq presumed missing) or
				// explanation-type (consequents perhaps missing; antecedent presumed missing).

				// if(true || removedElt.getSubject().equals(hypo) ||
				// removedElt.getSubject().getAllComponents().contains(hypo)) {

				Entity negated = (Entity) removedElt.getObject().clone();
				boolean consequent_happens = allElts2.getElements().stream().anyMatch(e -> WhatIfExpert.matchEntity(false, negated, e));

				negated.addFeature(Markers.NOT);

				// Mark.say("ALL ELTS");
				// int i = 0;
				// for(Entity e : allElts2.getElements()) {
				// Mark.say(++i, generator.generate(e));
				// }

				if (removedElt.getSubject().getType() == Markers.CONJUNCTION) {
					Sequence matchedPremises = new Sequence(Markers.CONJUNCTION);

					for (Entity e : removedElt.getSubject().getElements()) {
						if (WhatIfExpert.findEntity(false, e, allElts2)) {
							matchedPremises.addElement(e);
						}
					}
					if (matchedPremises.getAllComponents().size() > 0) {
						String bullet = "";
						// At least some of the premises appear in the story.
						bullet += "<li>";

						if (!consequent_happens) {
							bullet += "Although ";
							bullet += WhatIfExpert.oxfordCommas(generator, matchedPremises);
							bullet += false ? ", nevertheless " : ", ";
							bullet += generator.generate(negated);
						}
						else {
							// This needs more precision, and probably needs to refer to the elaboration graph.
							// Because there are multiple instances of people becoming harmed, and this predicate
							// becomes tricked when the antecedent is missing, but a copy of it appears later in the
							// story.
							if (false) {
								bullet += generator.generate(removedElt.getObject());
								bullet = bullet.substring(0, bullet.length() - 1); // remove punctuation
								bullet += ", but not because ";
								bullet += WhatIfExpert.oxfordCommas(generator, matchedPremises);
								bullet += ".";
							}
						}
						bullet += "</li>";
						if (bullet.indexOf(",") >= 0 && !redundant.containsKey(bullet)) {
							redundant.put(bullet, true);
							bullet_points += bullet;
						}
					}
					else {
						String bullet = "";
						// None of the premises appear in the story.
						bullet += "<li>";
						bullet += "no-premise " + generator.generate(removedElt);
						bullet += "</li>";
						continue;
						// // TODO : Need to determine whether the consequent still exists despite the inference being
						// absent.
						// bullet_points += "<li>";
						// bullet_points += generator.generate(removedElt.getObject());
						// bullet_points = bullet_points.substring(0,bullet_points.length()-1);
						// bullet_points += "(It never happens that ";
						// bullet_points += WhatIfExpert.oxfordCommas(generator, removedElt.getSubject());
						// bullet_points += ")";
						// bullet_points += "</li>";

					}
				}
				else {
					Mark.say(removedElt);
					bullet_points += "<li>nonconj: " + generator.generate(removedElt) + " " + generator.generate(negated) + "</li>";
				}

				// } // endif
			}

			if (!removedInferences.isEmpty()) {
				event_result += "<ul>" + bullet_points + "</ul>";
			}
		}

		if (bullet_points == "") {
			event_result = "From an event-based perspective, nothing changes.<br/>\n";
		}

		Connections.getPorts(this).transmit(HTML, new BetterSignal("What-if analysis", event_result));

		// *********************************** FROM A THEMATIC PERSPECTIVE

		String thematic_result = "From a thematic perspective, ";
		// TODO: Nice abbreviation: Macbeth's revenge, Lady Macbeth's suicide, xx's (concept pattern headed by xx).

		boolean some_concepts_removed = false;
		boolean some_concepts_introduced = false;

		Sequence ptns1 = GenesisGetters.getMentalModel1().getStoryProcessor().getInstantiatedConceptPatterns();
		Sequence ptns2 = GenesisGetters.getMentalModel2().getStoryProcessor().getInstantiatedConceptPatterns();
		aligner = new Aligner();
		align = aligner.align(ptns1, ptns2).get(0);
		// Mark.say(align);
		// Mark.say(WhatIfExpert.conceptNickname(ptns1.getElement(0)));

		// ---- Find concepts removed
		bullet_points = "";
		redundant = new HashMap<String, Boolean>();
		for (Pair<Entity, Entity> p : align) {
			if (p.b == null) {
				String bullet = "";
				bullet += "<li>";
				// bullet_points += WhatIfExpert.findEntity(false, hypo, p.a) ? "within " : "without ";
				// bullet_points += hypo.toString()+"||";
				// bullet_points += p.a.toString();

				bullet += WhatIfExpert.conceptNickname(p.a); // + ", leading to " +
				                                             // generator.generate(p.a.getElement(0));
				bullet += "</li>";
				if (!redundant.containsKey(bullet)) {
					redundant.put(bullet, true);
					bullet_points += bullet;
				}
			}
		}
		if (bullet_points != "") {
			some_concepts_removed = true;
			thematic_result += "the following concepts disappear: <ul>" + bullet_points + "</ul>";
		}

		// ---- Find concepts introduced
		bullet_points = "";
		redundant = new HashMap<String, Boolean>();
		for (Pair<Entity, Entity> p : align) {
			if (p.a == null) {
				String bullet = "";
				bullet += "<li>";
				bullet += WhatIfExpert.conceptNickname(p.b);// + ", leading to " + p.b.toString();
				bullet += "</li>";
				if (!redundant.containsKey(bullet)) {
					redundant.put(bullet, true);
					bullet_points += bullet;
				}
			}
		}

		if (bullet_points != "") {
			some_concepts_introduced = true;
			if (some_concepts_removed) {
				thematic_result += "... and ";
			}
			thematic_result += "the following concepts are introduced: <ul>" + bullet_points + "</ul>";
		}

		Connections.getPorts(this).transmit(HTML, new BetterSignal("What-if analysis", thematic_result));

	}

	public static String conceptNickname(Entity conceptPattern) {
		String[] tmp = conceptPattern.getName().split("-");
		String ret = "";
		for (int i = 0; i < tmp.length - 1; i++) {
			ret += tmp[i].toLowerCase();
		}

		Entity e = conceptPattern;
		int depth = 10;
		while (--depth > 0 && e.getAllComponents().size() > 0) {
			e = e.getAllComponents().get(0);
		}

		String who = e.getType();
		ret = Character.toUpperCase(who.charAt(0)) + who.substring(1) + "'s " + ret;
		return ret;
	}

	/**
	 * Takes a conjunction-type sequence and returns a comma-separated list in English.
	 * 
	 * @param g
	 * @param conjunction
	 * @return
	 */
	public static String oxfordCommas(Generator g, Entity conjunction) {
		final int size = conjunction.getElements().size();
		if (conjunction.getType() != Markers.CONJUNCTION || size == 0) {
			return "";
		}

		Vector<String> sentences = new Vector<String>();
		conjunction.getElements().stream().map(e -> g.generate(e)).forEach(s -> sentences.add(s.substring(0, s.length() - 1)));

		if (size == 1) {
			return sentences.get(0);
		}
		else if (size == 2) {
			return sentences.get(0) + " and " + sentences.get(1);
		}
		else {
			String ret = "";
			sentences.set(size - 1, "and " + sentences.get(size - 1));

			for (int i = 0; i < size; i++) {
				ret += sentences.get(i);
				if (i < size - 1) {
					ret += ", ";
				}
			}
			return ret;
		}
	}

	public static boolean matchEntity(boolean strictIndexing, Entity needle, Entity haystack) {
		List<Entity> elts1 = needle.getAllComponents();
		List<Entity> elts2 = haystack.getAllComponents();
		if (needle.getPrettyPrintType() != haystack.getPrettyPrintType() || needle.getType() != haystack.getType() || elts1.size() != elts2.size() ||

		(strictIndexing && needle.getName() != haystack.getName())) {
			return false;
		}
		for (int i = 0; i < elts1.size(); i++) {
			if (!WhatIfExpert.matchEntity(strictIndexing, elts1.get(i), elts2.get(i))) {
				return false;
			}
		}

		return true;
	}

	public static boolean findEntity(boolean strictIndexing, Entity needle, Entity haystack) {
		if (matchEntity(strictIndexing, needle, haystack)) {
			return true;
		}

		for (Entity e : haystack.getAllComponents()) {
			if (WhatIfExpert.findEntity(strictIndexing, needle, e)) {
				return true;
			}
		}
		return false;
	}

	public WhatIfExpert(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

}

// Case 1: an inference with the removed element as its antecedent.
// Response: "NOT(consequent)"
// Case 2: an inference further downstream, where the removed element doesn't explicitly appear
// Response: "NOT(consequent) when antecedent" (supposing antecedent still happens)

