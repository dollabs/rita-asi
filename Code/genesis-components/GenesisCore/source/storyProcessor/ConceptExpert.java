package storyProcessor;

import java.util.*;

import javax.swing.JOptionPane;

import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Switch;
import frames.entities.Entity;
import frames.entities.Sequence;
import generator.Generator;
import generator.RoleFrames;
import gui.ConceptBar;
import matchers.*;
import utils.Html;
import utils.minilisp.LList;
import utils.tools.Predicates;
import utils.*;

/**
 * This expert reviews information from the story processor, searching for concept instances, once the story processor
 * has read a story and done rule-based processing. Purpose is to create instances of the ConceptDescription class, used
 * extensively, and in particular, in summary.
 * <p>
 * Basic operation starts with iteration over all concept patterns. Each such pattern is unpacked looking for various
 * sorts of elements. Sometimes the concept pattern element specifies only a single action, as in suicide, "xx killx xx"
 * . Sometimes the concept pattern element specifies a relationship, as in teach a lesson, which requires, among other
 * elements, that the reader is an ally/friend of entity returning harm "xx friend yy". And of course there are leads-to
 * pattern elements, as in "xx's harming yy leads to yy's harming xx".
 * <p>
 * When a match of all required concept pattern elements is found, the system creates a description, an instance of
 * ConceptDescription.
 * <p>
 * Once all concept patterns are searched for, all the ConceptDescriptions eventually are sent to the summarizer, along
 * with other story descriptors.
 * <p>
 * Each ConceptDescription instance contains elements describing, for example,
 * <ul>
 * <li>How concept pattern variables match entities in the story
 * <li>Which story elements are involved in the match
 * <li>The name of the concept
 * </ul>
 * <p>
 * Created on Oct 9, 2010
 * 
 * @author phw
 */
public class ConceptExpert extends AbstractWiredBox {

	// Define port names

	public static final String CONCEPT_ANALYSIS = "Reflection analysis port";

	public static final String INSTANTIATED_CONCEPTS = "Instantiated concepts port";

	public static final String TAB = "tab port";

	public static final String ENGLISH = "English port";

	public static final String INJECT_ELEMENT = "inject element";

	public static final String TEST_ELEMENT = "test element";

	public static final String DEBUG = "debugging port";

	private ArrayList<ConceptDescription> discoveries = new ArrayList<ConceptDescription>();
	
	private ArrayList<ConceptDescription> persistentDiscoveries = new ArrayList<ConceptDescription>();

	/**
	 * Construct instance and wire up.
	 */
	public ConceptExpert() {
		super("Concept expert");
		Connections.getPorts(this).addSignalProcessor(this::process);
	}

	/**
	 * The key method. Receives concept patterns + story + inferences from the story processor, which are all that are
	 * needed to search for concept constellations. Many instances of Mark.say(debug, ...) occur because of need for
	 * extensive debugging. Not that all processing is tail-recursive. That is, there are no returned values, but when
	 * there is a successful concept discovery deep in the recursive calls, the success is noted by adding to the
	 * discoveries variable in processDiscoveredConcept
	 */
	public void process(Object signal) {

		boolean debug = false;

		// Turns on working light on GUI.
		NewTimer.conceptProcessingTimer.reset();
		Mark.say(debug, "Entering ConceptExpert", signal);

		BetterSignal triple = BetterSignal.isSignal(signal);
		// Return if input is not what was expected.
		if (triple == null) {
			return;
		}

		// Unpack signal
		Sequence concepts = triple.get(0, Sequence.class);
		Sequence story = triple.get(1, Sequence.class);
		Sequence inferences = triple.get(2, Sequence.class);
		
		Mark.say(debug, "\n>>>  Concept pattern detector received story of length", story.getElements().size());

		// Start fresh by clearing discoveries list.
		discoveries = new ArrayList<ConceptDescription>();

		// When debugging, reportconcept descriptions. These will describe, for example, necessary elements that appear
		// alone
		// ("xx is a king", "yy kills xx") and elements derived from leads-to expressions
		// ("xx's harming yy leads to yy's harmimg xx), which specify that there is a chain of causal connections.
		Mark.say(debug, "Working with", concepts.getElements().size(), "concept patterns");
		if (debug) {
			for (Entity concept : concepts.getElements()) {
				Mark.say("Concept elements for", concept.getName());
				for (Entity t : concept.getElements()) {
					Mark.say("Element:", t.asStringWithIndexes());
				}
			}
		}

		// Iterate over concepts.
		for (Entity concept : concepts.getElements()) {

			// For debugging
			if (debug) {
				Mark.say(debug, "Looking for instances of", concept.getType());
				for (Entity t : concept.getElements()) {
					Mark.say(debug, "Concept:", t.asString());
					Mark.say(debug, "The inference count is:", inferences.getElements().size());
				}
			}
			// Key call: Hand off concept patterns one by one for processing.
			try {
				disectAndThenProcessConcept((translateConceptToList((Sequence) concept)), translateStoryToList(story), inferences
				        .getElements(), concept.getType(), new LList<Entity>(), new LList<Entity>(), story);
			}
			catch (Exception e) {
				Mark.err("Blew out of processing", concept.getName(), "probably because recursion to deep");
			}

			Mark.say(debug, "Completed search for instances of", concept.getType());
		}

		// Rest of method delivers concepts found to GUI and the story processor, which then transmits them on to other
		// interested experts, such as the summarizer.

		// Set up concept bar in GUI
		Connections.getPorts(this).transmit(ConceptBar.CLEAR_CONCEPT_BUTTONS, Markers.RESET);
		for (ConceptDescription completion : discoveries) {
			Connections.getPorts(this).transmit(ConceptBar.CONCEPT_BUTTON, completion);
			Sequence instantiation = completion.getInstantiations();
			instantiation.addType(completion.getName());
			BetterSignal message = new BetterSignal(Markers.CONCEPT_ANALYSIS_TAB, Html.normal(ConceptTranslator.translateConcept(completion)));
			Connections.getPorts(this).transmit(ENGLISH, message);
		}

		// Create and transmit concept analysis object
		ConceptAnalysis analysis = new ConceptAnalysis(discoveries, story);
		Connections.getPorts(this).transmit(CONCEPT_ANALYSIS, analysis);

		// Transmit concept list to GUI
		Sequence instantiations = new Sequence(Markers.CONCEPT_MARKER);
		for (ConceptDescription rd : analysis.getConceptDescriptions()) {
			instantiations.addElement(rd.getInstantiations());
		}
		Connections.getPorts(this).transmit(INSTANTIATED_CONCEPTS, instantiations);

		Mark.say(debug, "Completed concept analysis");
		NewTimer.conceptProcessingTimer.report(true, "Concept processing slow beyond description");

	}

	/**
	 * A key method. Determines whether first part of a leads to element is connected to second part. For example, if
	 * concept pattern specifies that "xx's harming yy leads to yy's harming xx", then this method could receive
	 * "Macbeth harms Macduff" and "Macduff harms macbeth" (in inner language of course) and then look for a path
	 * through the inferences joining the two. Works using standard, queue-based, depth-first search.
	 */
	public static Vector<Entity> isConnectedViaInferences(Entity instantiatedAntecedent, Entity instantiatedConsequent, Vector<Entity> inferences) {
		boolean debug = false;
		Mark.say(debug, "Checking to see if\n", instantiatedAntecedent, "\nis connected to\n", instantiatedConsequent);
		Vector<Vector<Entity>> queue = new Vector<Vector<Entity>>();
		Vector<Entity> extendedList = new Vector<Entity>();

		Mark.say(debug, "There are", inferences.size(), "inferences");
		for (Entity rule : inferences) {
			Vector<Entity> antecedents = rule.getSubject().getElements();
			if (antecedents == null) {
				Mark.err("Strange lack of antecedents in", rule);
			}
			else {
				if (antecedents.contains(instantiatedAntecedent)) {
					Vector<Entity> path = new Vector<Entity>();
					path.add(instantiatedAntecedent);
					path.add(rule.getObject());
					queue.add(path);
				}
			}
		}
		while (!queue.isEmpty()) {
			Vector<Entity> path = queue.firstElement();
			if (instantiatedConsequent == path.lastElement()) {
				Mark.say(debug, "Antecedent is connected to consequent!");
				// Here is where success occurs; path found.
				return path;
			}
			queue.remove(0);
			Entity lastElement = path.lastElement();
			if (extendedList.contains(lastElement)) {
				continue;
			}
			else {
				extendedList.add(lastElement);
			}

			boolean more = false;

			Mark.say(debug, "Looking for", lastElement);
			// Look through all inferences; when one is found that can continue a path, create a new path for it and add
			// new path to queue.
			for (int i = 0; i < inferences.size(); ++i) {
				Entity inference = inferences.get(i);
				Vector<Entity> antecedents = inference.getSubject().getElements();
				if (antecedents.contains(lastElement)) {
					Vector<Entity> newPath = new Vector<Entity>();
					newPath.addAll(path);
					newPath.add(inference.getObject());
					queue.add(newPath);
					more = true;
				}
			}
			if (!more) {
				Mark.say(debug, "Cannot continue path");
			}
		}
		// Could not find a path
		return null;
	}

	/**
	 * Disect concept pattern, looking for special case concept patterns that demand that, for example, two variables do
	 * not match same story element or that specify the consequences of a successful, complete concept pattern match.
	 */
	private void disectAndThenProcessConcept(LList<Entity> patterns, LList<Entity> story, Vector<Entity> inferences, String name, LList<Entity> participants, LList<Entity> instantiation, Sequence storySequence) {
		boolean debug = false;

		Mark.say(debug, "Processing one concept");

		// Set up variables to receive concept pattern elements
		ArrayList<PairOfEntities> exclusions = new ArrayList<PairOfEntities>();
		ArrayList<Entity> consequences = new ArrayList<Entity>();
		ArrayList<Entity> consequently = new ArrayList<Entity>();
		ArrayList<Entity> notably = new ArrayList<Entity>();

		LList<Entity> handle = new LList<Entity>();

		// Look through various parts of the concept pattern for special idioms.
		for (Entity e : patterns) {
			Mark.say(debug, "Pattern", e);
			// This looks for idiom that stipulates two variables cannot have same binding, as in xx cannot be yy
			if (e.isA(Markers.EQUAL_MARKER) && e.hasFeature(Markers.NOT) && e.hasProperty(Markers.MODAL, Markers.MUST_WORD)) {
				exclusions.add(new PairOfEntities(e.getSubject(), RoleFrames.getObject(e)));
			}
			// Remaining special cases look for consequences of concept match, rather than requirements of concept
			// match, as in want to kill + kill has consequence of murder.
			else if (e.hasProperty(Markers.PROPERTY_TYPE, Markers.CONSEQUENTLY)) {
				if (notably.isEmpty()) {
					// Mark.say("Adding consequently", e);
					consequences.add(e);
					consequently.add(e);
				}
				else {
					// Reason: addition causes notably to be called again after insertion and other problems.
					Mark.err("1 Cannot have both a notably and a consequently in same concept");
				}
			}
			else if (e.hasFeature(Markers.CONSEQUENTLY)) {
				if (notably.isEmpty()) {
					// Mark.say("Adding consequently", e);
					consequences.add(e);
					consequently.add(e);
				}
				else {
					// Reason: addition causes notably to be called again after insertion and other problems.
					Mark.err("2 Cannot have both a notably and a consequently in same concept");
				}
			}
			else if (e.hasFeature(Markers.PREVIOUSLY)) {
				consequences.add(e);
			}
			else if (e.hasFeature(Markers.OBSERVABLY)) {
				consequences.add(e);
			}
			else if (e.hasFeature(Markers.EVIDENTLY)) {
				consequences.add(e);
			}
			else if (e.hasFeature(Markers.NOTABLY)) {
				if (consequently.isEmpty()) {
					// Mark.say("Adding notably", e);
					consequences.add(e);
					notably.add(e);
				}
				else {
					// Reason: addition causes notably to be called again after insertion and other problems.
					Mark.err("3 Cannot have both a notably and a consequently in same concept");
				}
			}
			else if (Predicates.isCause(e) && e.getObject().hasFeature(Markers.CONSEQUENTLY)) {
				consequences.add(e);
			}
			else if (Predicates.isCause(e) && e.getObject().hasFeature(Markers.PREVIOUSLY)) {
				consequences.add(e);
			}
			else if (Predicates.isCause(e) && e.getObject().hasFeature(Markers.OBSERVABLY)) {
				consequences.add(e);
			}
			else {
				handle = handle.cons(e);
			}
		}
		// Revise patterns so as to exclude special idioms
		patterns = handle;

		// for (Entity x : patterns) {
		// Mark.say("Pattern element:", x);
		// }

		try {
			// A key call: Move on to where the real work is done. Note additions of unpacked exclusions and
			// consequences. No return value noted as tail recursive.
			processWithAndWithoutOptionalElements(patterns, story, new LList<PairOfEntities>(), inferences, name, participants, instantiation, storySequence, exclusions, consequences);
		}
		catch (Exception x) {
			Mark.err("Blew out of processing next concept pattern, probably because recursion to deep");
		}
	}

	/**
	 * Look for optional elements that can be, but don't have to be skipped, as in "sometimes xx loves yy", then
	 * continute with processing. This method also deals with instances of the check-if idiom, as in
	 * "check if xx loves yy".
	 */
	private void processWithAndWithoutOptionalElements(LList<Entity> patterns, LList<Entity> fullStory, LList<PairOfEntities> bindings, Vector<Entity> inferences, String name, LList<Entity> participants, LList<Entity> instantiation, Sequence storySequence, ArrayList<PairOfEntities> exclusions, ArrayList<Entity> consequences) {
		// If at the end of elements specified in the concept, skip this section and continue with
		// processConceptPatternElement
		boolean debug = false;
		if (debug && !patterns.endP()) {
			Mark.say("Patterns", patterns.first());
		}

		if (patterns.endP()) {
		}
		// If SOMETIMES marker present, it is ok to skip as well as to include it. If found, then included in one of the
		// results. Other result without it will be weeded out later.
		else if (Predicates.isSometimes(patterns.first())) {
			Mark.say(debug, "First element", patterns.first());

			// Skip over sometimes concept pattern element and work with rest of the pattern elements.
			processWithAndWithoutOptionalElements(patterns
			        .rest(), fullStory, bindings, inferences, name, participants, instantiation, storySequence, exclusions, consequences);
		}
		// Checks for check-if idiom. If found, check for presence in story. If not there work harder.
		else if (patterns.first().hasProperty(Markers.IDIOM, Markers.CHECK)) {
			Entity patternElement = patterns.first();
			boolean found = false;
			Entity storyElement = null;
			// Try looking for element in the story
			for (Entity e : storySequence.getElements()) {
				if (StandardMatcher.getBasicMatcher().match(e, patternElement, bindings) != null) {
					storyElement = e;
					found = true;
					break;
				}
			}
			// If not in the story, see if element would be supported by an explanation rule; not sure this actually
			// works
			if (!found) {
				storyElement = Substitutor.substitute(patternElement, bindings);
				Connections.getPorts(this).transmit(TEST_ELEMENT, storyElement);
			}
			// If still not found, ask system user about it.
			if (!found) {
				storyElement = Substitutor.substitute(patternElement, bindings);
				String question = Generator.getGenerator().generateXPeriod(storyElement, Markers.PRESENT) + "?";
				if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, question, "", JOptionPane.YES_NO_OPTION)) {

					// User has said yes, so note that element is a participant in the concept
					participants = participants.cons(storyElement);

					// Add back into story at this point!
					Connections.getPorts(this).transmit(INJECT_ELEMENT, storyElement);

					found = true;
				}
			}
			if (found) {
				// Now can skip over check-if element and work with the rest of the pattern elements
				processWithAndWithoutOptionalElements(patterns
				        .rest(), fullStory, bindings, inferences, name, participants, instantiation, storySequence, exclusions, consequences);
			}
		}
		// In any case, try to find first element and rest of the elements whether first element has SOMETIMES marker or
		// CHECK marker or neither.
		processNextConceptPatternElement(patterns, fullStory, fullStory, bindings, inferences, name, participants, instantiation, storySequence, exclusions, consequences);
	}

	/**
	 * Much real work done here. This is where individual concept pattern elements are checked.
	 */
	private void processNextConceptPatternElement(LList<Entity> patterns, LList<Entity> fullStory, LList<Entity> restOfStory, LList<PairOfEntities> bindings, Vector<Entity> inferences, String name, LList<Entity> participants, LList<Entity> instantiation, Sequence storySequence, ArrayList<PairOfEntities> exclusions, ArrayList<Entity> consequences) {
		boolean debug = false;

		// Mark.say("Working on", patterns.first());

		StandardMatcher matcher = StandardMatcher.getBasicMatcher();
		// If get here, system has won, all pattern elements have been matched
		if (patterns.endP()) {
			Mark.say(debug, "Won, found", name);

			for (Entity c : consequences) {
				Mark.say(debug, "Consequence", c);
				if (c.isA("refuse")) {
					Mark.say(debug, "I refuse!!!");
					instantiation.forEach(x -> {
						if (x.isA("entail")) {
							Entity starter = x.getElements().firstElement();
							Mark.say(debug, "Instantiation", starter);

							for (int i = 0; i < storySequence.getElements().size(); ++i) {
								Entity element = storySequence.getElements().get(i);
								LList<PairOfEntities> match = matcher.match(starter, element);
								if (match != null) {
									Mark.say(debug, "Match at", i, "for", name, "with element", element);
									for (int n = i; n < storySequence.getElements().size(); ++n) {
										storySequence.getElements().get(n).addFeature(Markers.NOT);
									}
									storySequence.getElements().subList(i + 1, storySequence.getElements().size()).clear();
									storySequence.stream().forEachOrdered(e -> {
										Mark.say(debug, "Story element", e);
									});
									break;
								}
							}
						}
					});
					return;
				}
			}


			Sequence participantSequence = translateListToSequence(participants);
			Sequence instantiationSequence = translateListToSequence(instantiation);



			instantiationSequence.addType(Markers.CONCEPT);

			// This is where exclusions are checked, and if there is one, such as xx must not be yy, then the whole
			// concept match fails.
			if (checkForDuplicateBindingsToSameEntity(bindings, exclusions)) {
				return;
			}

			// This is where consequences of a complete match are recognized and put back into the story if
			// appropriately marked.
			Sequence consequentSequence = new Sequence();

			if (!consequences.isEmpty()) {
				// Handles elements such as "Consequently, xx murders yy because xx wanted to kill yy."
				for (Entity e : consequences) {
					Entity consequence = Substitutor.substitute(e, bindings);
					instantiationSequence.addElement(consequence);
					Mark.say(debug, "One result of", name, "is consequence:", consequence);
					String property = (String) (e.getProperty(Markers.PROPERTY_TYPE));
					// If a causal construction, marker will be on object
					if (property == null) {
						Mark.say("Got property from object", e);
						property = (String) (e.getObject().getProperty(Markers.PROPERTY_TYPE));
					}
					Mark.say(debug, "The property is", property, "for", e);

					String mannerMarker = extractConceptMannerMarker(e);
					if (mannerMarker == null) {
						mannerMarker = extractConceptMannerMarker(e.getObject());
					}

					// This is new, feature based code
					if (mannerMarker != null) {
						Entity object = consequence.getObject();
						if (object != null) {
							for (Entity r : (Vector<Entity>) consequence.getObject().getElements().clone()) {
								if (r.isA(Markers.MANNER_MARKER) && r.getSubject().isA(mannerMarker)) {
									consequence.getObject().getElements().remove(r);
									// consequence.addProperty(Markers.PROPERTY_TYPE, mannerMarker);
								}
							}
						}
						consequence.addProperty(Markers.PROPERTY_TYPE, property);
						consequence.addFeature(mannerMarker);

						if (mannerMarker.equals(Markers.NOTABLY)) {
							String result = Generator.getGenerator().generate(consequence);
							Mark.say(debug, "Announce", result);
							Ask.getAsk().comment(result);
						}
						else {
							Mark.say(debug, "Injecting", consequence);
							consequentSequence.addElement(consequence);
							Connections.getPorts(this).transmit(INJECT_ELEMENT, consequence);
						}
					}
					else if (e.hasFeature(Markers.EVIDENTLY)) {
						// Don't want to insert if an evidently expression; idiomatic choice
						Mark.say(debug, "No injection of", consequence);
					}
				}
			}

			Mark.say(debug, "Name:", name);

			// Legacy adapter

			// Construct and process concept description because all pattern elements have been dealt with.
			// processDiscoveredConcept is where the buck stops in the tail recursion.
			processDiscoveredConcept(new ConceptDescription(name, bindings, participantSequence, instantiationSequence, consequentSequence,
			        storySequence));
			return;
		}
		// If get here, no further matching is possible, so stop.
		else if (restOfStory.endP()) {
			if (debug) {
				Mark.say("End of story, and still not matching with bindings", bindings);
				for (Entity t : patterns) {
					Mark.say(t.asString());
				}
			}
			return;
		}

		// Evidently, not done, so look at first pattern element.
		Entity patternElement = patterns.first();

		// if (restOfStory.first().isA(Markers.WHETHER_QUESTION) &&
		// !restOfStory.first().getObject().isA(Markers.PROPERTY_TYPE)) {
		// Mark.say("Here we go: Pattern\n", patternElement.toXML(), "\nDatum\n", restOfStory.first().toXML());
		// }
		// If the pattern element is a leads-to element, signifying a search, dig out antecedent and consequent pattern
		// and call method for finding instances and looking for connections. Looks like only one antecedent is checked,
		// so right way to express multiple-antecedent leads-to situations is to put each antecedent into its own
		// leads-to expression. Entail is synonym for leads to.

		if (patternElement.isA(Markers.ENTAIL_RULE)) {

			Entity consequent = patternElement.getObject();
			Entity antecedents = patternElement.getSubject();
			Entity antecedent = null;

			if (antecedents.sequenceP(Markers.CONJUNCTION) && !antecedents.getElements().isEmpty()) {
				antecedent = antecedents.getElement(0);
				if (antecedents.getElements().size() > 1) {
					Mark.err("Multiple antecedents in leads-to expression");
				}
			}
			else {
				Mark.err("Malformed entail antecedent construct");
			}

			Mark.say(debug, "Discovered antecedent pattern", antecedent.asString());
			// Look for antecedent and consequent and connection between them and follow tail recursion to see if there
			// is a complete concept pattern match.
			processOneLeadsToPatternElementAntecedent(antecedent, consequent, patterns, fullStory, fullStory, bindings, inferences, name, participants, instantiation, storySequence, exclusions, consequences);
			return;
		}

		String timeConstraint = Predicates.isTimeConstraint(patternElement);

		if (timeConstraint != null) {
			Mark.say(debug, "Found time constraint in concept!!!!!");
			LList<PairOfEntities> newBindings = checkTimeConstraint(patternElement, fullStory, bindings);
			if (newBindings != null) {
				processWithAndWithoutOptionalElements(patterns
				        .rest(), fullStory, newBindings, inferences, name, participants, instantiation, storySequence, exclusions, consequences);
			}
			else {
				return;
			}
		}

		// Ok, first element in concept pattern element list was not a leads-to element. Look at first story element.
		Entity storyElement = restOfStory.first();
		// See if pattern element matches first element in the story.
		LList<PairOfEntities> newBindings = matcher.match(patternElement, storyElement, bindings);
		// Mark.say("New bindings", newBindings);
		// if (storyElement.isA(Markers.PERSONALITY_TRAIT)) {
		// Mark.say("Pattern", patternElement.toXML());
		// Mark.say("Datum ", storyElement.toXML());
		// }
		if (newBindings == null) {
			// Mark.say(debug, "Failed to match:\n", patternElement, "\n", storyElement);
			Mark.say(debug, "Failed to match:", storyElement);
			if (false && storyElement.isA("believe")) {
				Entity pSubject = patternElement.getSubject();
				Entity sSubject = storyElement.getSubject();
				Entity pObject = patternElement.getObject();
				Entity sObject = storyElement.getObject();
				Mark.say("Subject match", matcher.match(pSubject, sSubject, bindings));
				Mark.say("Object match", matcher.match(pObject, sObject, bindings));
				Mark.say("Complete match", matcher.match(patternElement, storyElement, bindings));
				Mark.say("Subjects\n", pSubject.toXML(), "\n", sSubject.toXML());

				Mark.say("Wholes\n", patternElement.toXML(), "\n", storyElement.toXML());

			}
			// Mark.say(debug, "Failed on", storyElement);
		}
		else if (newBindings != null) {
			Mark.say(debug, "Matched:\n", patternElement.asString(), "\nwith\n", storyElement
			        .asString(), "\n with bindings\n", newBindings, "\nand moving on");

			// It does match, look at special cases.
			if (patternElement.isA(Markers.CLASSIFICATION_MARKER)
			        && patternElement.getSubject().getType().equals(storyElement.getSubject().getType())) {

				// Ok, there is not only a match, classifications are the same exactly.
				Mark.say("Classification test passed", patternElement.asString(), storyElement.asString());

				// So, ready to carry on with rest of the pattern elements.
				processWithAndWithoutOptionalElements(patterns.rest(), fullStory, newBindings, inferences, name, participants
				        .cons(storyElement), instantiation.cons(storyElement), storySequence, exclusions, consequences);

			}
			else if (patternElement.isA(Markers.JOB_TYPE_MARKER) && patternElement.getObject().getType().equals(storyElement.getObject().getType())) {
				// Ok, there is not only a match, job types are the same exactly.

				// So, ready to carry on with rest of the pattern elements.
				processWithAndWithoutOptionalElements(patterns.rest(), fullStory, newBindings, inferences, name, participants
				        .cons(storyElement), instantiation.cons(storyElement), storySequence, exclusions, consequences);

			}
			else if (patternElement.isA(Markers.CLASSIFICATION_MARKER) || patternElement.isA(Markers.JOB_TYPE_MARKER)) {
				// Too bad, there was a match, but classification/job types not exactly the same. Lose.
				Mark.say(debug, "Classification and job type test failed", patternElement.asString(), storyElement.asString());
			}
			else {
				Mark.say(debug, "Not a classification or job type situation");
				// Not a classification or job type situation, so ordinary match suffices, carry on with the rest of the
				// pattern elements.
				processWithAndWithoutOptionalElements(patterns.rest(), fullStory, newBindings, inferences, name, participants
				        .cons(storyElement), instantiation.cons(storyElement), storySequence, exclusions, consequences);
			}
		}
		// else if (restOfStory.first().isA(Markers.WHETHER_QUESTION) &&
		// !restOfStory.first().getObject().isA(Markers.PROPERTY_TYPE)) {
		// Mark.say("Failed to match Pattern\n", patternElement.toXML(), "\nDatum\n", restOfStory.first().toXML());
		// Connections.getPorts(this).transmit(DEBUG, patternElement);
		// Connections.getPorts(this).transmit(DEBUG, restOfStory.first());
		// // try {
		// // String english = Generator.getGenerator().generate(restOfStory.first());
		// // Entity entity = Translator.getTranslator().translate(english).getElements().get(0);
		// // LList<PairOfEntities> theBindings = matcher.match(patternElement, entity);
		// // Mark.say("Bindings:", theBindings);
		// // }
		// // catch (Exception e) {
		// // // TODO Auto-generated catch block
		// // e.printStackTrace();
		// // }
		//
		// }
		else {
			// Mark.say(debug, "Ignored!!!!!!!!!!!!", patternElement);
		}
		// Pretend it did not get found, look for other matches in the rest of the story. This way, all matching
		// alternatives are found.
		try {
			processNextConceptPatternElement(patterns, fullStory, restOfStory
			        .rest(), bindings, inferences, name, participants, instantiation, storySequence, exclusions, consequences);
		}
		catch (Exception e) {
			e.printStackTrace();
			Mark.err("Blew out of processing one concept pattern probably because recursion to deep");
		}
	}

	public static String extractConceptMannerMarker(Entity e) {
		return e.hasFeature(Markers.CONSEQUENTLY) ? Markers.CONSEQUENTLY
		        : e.hasFeature(Markers.PREVIOUSLY) ? Markers.PREVIOUSLY
		                : e.hasFeature(Markers.OBSERVABLY) ? Markers.OBSERVABLY : e.hasFeature(Markers.NOTABLY) ? Markers.NOTABLY : null;
	}

	private LList<PairOfEntities> checkTimeConstraint(Entity patternElement, LList<Entity> fullStory, LList<PairOfEntities> bindings) {
		
		boolean debug = false;
		
		LList<PairOfEntities> newBindings = null;
		
		
		for (Entity e : fullStory) {
			newBindings = StandardMatcher.getBasicMatcher().match(patternElement, e, bindings);
			if (newBindings != null) {
				return newBindings;
			}
		}
		Entity x;
		Entity y;
		if (patternElement.isA(Markers.AFTER)) {
			x = patternElement.getObject();
			y = patternElement.getSubject();
		}
		else if (patternElement.isA(Markers.BEFORE)) {
			x = patternElement.getSubject();
			y = patternElement.getObject();
		}
		else {
			Mark.err("Unrecognized time marker in", patternElement);
			return null;
		}
		boolean firstFound = false;
		for (Entity e : fullStory) {
			if (!firstFound) {
				newBindings = StandardMatcher.getBasicMatcher().match(x, e, bindings);
				if (newBindings != null) {
					Mark.say(debug, "Found first", e);
					firstFound = true;
					bindings = newBindings;
				}
			}
			else {
				newBindings = StandardMatcher.getBasicMatcher().match(y, e, bindings);
				if (newBindings != null) {
					Mark.say(debug, "Found second", e);
					return newBindings;
				}
			}
		}
		Mark.say(debug, "Nothing matched", patternElement);
		return null;
	}

	/**
	 * This is where leads-to elements in the concept pattern are handled. Called for antecedent, and requires a match
	 * of antecedent in story. If there is one, hands off to processOneLeadsToPatternElementConsequent.
	 */
	private void processOneLeadsToPatternElementAntecedent(Entity antecedent, Entity consequent, LList<Entity> patterns, LList<Entity> fullStory, LList<Entity> restOfStory, LList<PairOfEntities> bindings, Vector<Entity> inferences, String name, LList<Entity> participants, LList<Entity> instantiation, Sequence storySequence, ArrayList<PairOfEntities> exclusions, ArrayList<Entity> consequences) {
		boolean debug = false;

		if (restOfStory.endP()) {
			Mark.say(debug, "Lost, couldn't find antecedent pattern", antecedent.asString());
			return;
		}

		// Recurse down story, looking for antecedent
		StandardMatcher matcher = StandardMatcher.getBasicMatcher();
		Entity storyElement = restOfStory.first();

		LList<PairOfEntities> newBindings = matcher.match(antecedent, storyElement, bindings);

		if (newBindings != null) {
			// Found one!
			Mark.say(debug, "Found instantiation of antecedent\n", antecedent.asString(), "\nnamely\n", storyElement.asString(), "\nand moving on");
			Mark.say(debug, "Bindings", bindings);
			Mark.say(debug, "New Bindings", newBindings);
			// Now look for consequent candidates
			processOneLeadsToPatternElementConsequent(storyElement, antecedent, consequent, patterns, fullStory, fullStory, newBindings, inferences, name, participants, instantiation, storySequence, exclusions, consequences);
		}
		else {
			Mark.say(debug, "Unable to match", antecedent, storyElement);
		}
		// Pretend it did not get found, look for other matches, so as to ensure that all possible matches are found.
		try {
			processOneLeadsToPatternElementAntecedent(antecedent, consequent, patterns, fullStory, restOfStory
			        .rest(), bindings, inferences, name, participants, instantiation, storySequence, exclusions, consequences);
		}
		catch (Exception e) {
			Mark.err("Blew out of processing one entail antecedentprobably because recursion to deep");
		}
	}

	/**
	 * Rest of leads-to processing done here. Looks for consequent matching story element with bindings already obtained
	 * from antecedent.
	 */
	private void processOneLeadsToPatternElementConsequent(Entity instantiatedAntecedent, Entity antecedent, Entity consequent, LList<Entity> patterns, LList<Entity> fullStory, LList<Entity> restOfStory, LList<PairOfEntities> bindings, Vector<Entity> inferences, String name, LList<Entity> participants, LList<Entity> instantiation, Sequence storySequence, ArrayList<PairOfEntities> exclusions, ArrayList<Entity> consequences) {
		boolean debug = false;
		if (restOfStory.endP()) {
			Mark.say(debug, "At end of story and couldn't find consequent pattern", consequent.asString(), "with bindngs\n", bindings);
			return;
		}
		// Recurse down story, looking for antecedent
		StandardMatcher matcher = StandardMatcher.getBasicMatcher();
		Entity storyElement = restOfStory.first();

		// Debugging trap.
		if (storyElement.functionP(Markers.APPEAR_MARKER) && storyElement.getSubject().relationP(Markers.POSITION_TYPE)) {
			Mark.say(debug, "\nTrying to match\n", consequent.asString(), "with\n", storyElement.asString(), "\nwith bindings", bindings);
		}

		LList<PairOfEntities> newBindings = matcher.match(consequent, storyElement, bindings);

		if (newBindings != null) {
			// Found one!
			Mark.say(debug, "Found instantiation of consequent", consequent.asString(), "namely", storyElement
			        .asString(), "with bindings\n", bindings, "and moving on");

			// Now test for connection by doing a search. This is a very important call, central to concept of concept
			// pattern.
			Vector<Entity> path = isConnectedViaInferences(instantiatedAntecedent, storyElement, inferences);
			if (path != null) {
				// Found one!
				Mark.say(debug, "Antecedent", instantiatedAntecedent.asString(), "IS connected to consequent", storyElement.asString());
				Sequence pathSequence = new Sequence(Markers.ENTAIL_RULE);
				pathSequence.addElement(path.get(0));
				pathSequence.addElement(path.get(path.size() - 1));

				// Carry on with the rest of the pattern elements.
				processWithAndWithoutOptionalElements(patterns.rest(), fullStory, newBindings, inferences, name, participants
				        .append(translateVectorToList(path)), instantiation.cons(pathSequence), storySequence, exclusions, consequences);
			}
			else {
				Mark.say(debug, "Antecedent", instantiatedAntecedent.asString(), "NOT connected to consequent", storyElement.asString());

				// But wait, if sometimes leads to, this is ok. Handled elsewhere adequately.

			}
		}
		// Pretend it did not get found, look for other matches, so as to ensure that all possible matches are found.
		processOneLeadsToPatternElementConsequent(instantiatedAntecedent, antecedent, consequent, patterns, fullStory, restOfStory
		        .rest(), bindings, inferences, name, participants, instantiation, storySequence, exclusions, consequences);
	}

	/**
	 * Add discovered concept to list of discoveries, weeding out concepts that are contained in other concepts if
	 * desired.
	 */
	public void processDiscoveredConcept(Object o) {
		if (!Switch.reportSubConceptsSwitch.isSelected()) {
			discoveries = weedOutSubdiscovieries((ConceptDescription) o, discoveries);
			persistentDiscoveries = weedOutSubdiscovieries((ConceptDescription) o, persistentDiscoveries);

		}
		else {
			discoveries.add((ConceptDescription) o);
			persistentDiscoveries.add((ConceptDescription) o);
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////
	// // Material below here sparsely commented and not included in review of 26-27 Nov 2014
	// ///////////////////////////////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////

	private ArrayList<ConceptDescription> weedOutSubdiscovieries(ConceptDescription newCandidate, ArrayList<ConceptDescription> candidates) {

		if (isSubsetOfAnotherCandidateInList(newCandidate, candidates)) {
			return candidates;
		}
		else if (isSameAsAnotherCandidateInList(newCandidate, candidates)) {
			return candidates;
		}
		ArrayList<ConceptDescription> newCandidateList = new ArrayList<ConceptDescription>();
		newCandidateList.add(newCandidate);

		ArrayList<ConceptDescription> resultList = new ArrayList<ConceptDescription>();

		for (ConceptDescription candidate : candidates) {
			if (!isSubsetOfAnotherCandidateInList(candidate, newCandidateList)) {
				resultList.add(candidate);
			}
		}
		resultList.add(newCandidate);
		return resultList;
	}

	private ArrayList<ConceptDescription> weedOutSubdiscovieries(ArrayList<ConceptDescription> candidates) {
		// Very inefficient, as doing a complete n2 operation on every addtion. Should fix so as to check only addition
		// agains others.
		ArrayList<ConceptDescription> result = new ArrayList<ConceptDescription>();
		// Mark.say("Input:", completions.size());
		for (ConceptDescription candidate : candidates) {
			if (!isSameAsAnotherCandidateInList(candidate, result)) {
				if (!isSubsetOfAnotherCandidateInList(candidate, candidates)) {
					// Mark.say("Keeping", candidate.getName());
					result.add(candidate);
				}
				else {
					// Mark.say("Eliminating", candidate.getName());
				}
			}
			else {
				// Mark.say("Flushing", candidate.getName());
			}
		}
		return result;
	}

	private boolean isSubsetOfAnotherCandidateInList(ConceptDescription candidate, ArrayList<ConceptDescription> candidates) {
		Vector<Entity> candidateStoryElements = candidate.getStoryElementsInvolved().getElements();
		for (ConceptDescription otherCandidate : candidates) {
			if (candidate == otherCandidate) {
				continue;
			}
			if (candidate.getName() != otherCandidate.getName()) {
				continue;
			}
			// At this point, not the same candidates, so see if they have the same elements
			Vector<Entity> otherCandidateStoryElements = otherCandidate.getStoryElementsInvolved().getElements();

			if (isSubset(candidateStoryElements, otherCandidateStoryElements)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSubset(Vector<Entity> candidateStoryElements, Vector<Entity> otherCandidateStoryElements) {
		for (Entity storyElement : candidateStoryElements) {
			if (!otherCandidateStoryElements.contains(storyElement)) {
				return false;
			}
		}
		if (candidateStoryElements.size() == otherCandidateStoryElements.size()) {
			return false;
		}
		return true;
	}

	private boolean isSameAsAnotherCandidateInList(ConceptDescription candidate, ArrayList<ConceptDescription> candidates) {
		Vector<Entity> candidateStoryElements = candidate.getStoryElementsInvolved().getElements();
		for (ConceptDescription otherCandidate : candidates) {
			if (candidate == otherCandidate) {
				// Of course it is the same as itself, so ignore
				// Mark.say("Equality");
				continue;
			}
			else if (candidate.getName().equals(otherCandidate.getName())) {
				// At this point, not the same candidates, so see if they have the same elements
				Vector<Entity> otherCandidateStoryElements = otherCandidate.getStoryElementsInvolved().getElements();
				if (isSameAs(candidateStoryElements, otherCandidateStoryElements)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isSameAs(Vector<Entity> candidateStoryElements, Vector<Entity> otherCandidateStoryElements) {
		for (Entity storyElement : candidateStoryElements) {
			if (!otherCandidateStoryElements.contains(storyElement)) {
				return false;
			}
		}
		if (candidateStoryElements.size() == otherCandidateStoryElements.size()) {
			return true;
		}
		return false;
	}

	private LList<Entity> translateConceptToList(Sequence s) {
		LList<Entity> result = new LList<Entity>();
		Vector<Entity> v = s.getElements();

		// First, sort out so that in same order as given but with "Check whether" elements at the end.

		Collections.reverse(v);

		// No put checks at end, that is, beginning.

		Vector<Entity> front = new Vector<>();

		Vector<Entity> back = new Vector<>();

		for (Entity x : v) {
			if (x.hasProperty(Markers.IDIOM, Markers.CHECK)) {
				back.add(x);
			}
			else {
				front.add(x);
			}
		}
		back.addAll(front);
		v = back;

		for (int i = v.size() - 1; i >= 0; --i) {
			result = result.cons(v.get(i));
		}
		return result;
	}

	private LList<Entity> translateStoryToList(Sequence s) {
		LList<Entity> result = new LList<Entity>();
		Vector<Entity> v = s.getElements();

		// Reverse story while creating list
		for (int i = v.size() - 1; i >= 0; --i) {
			result = result.cons(v.get(i));
		}
		return result;
	}

	private LList<Entity> translateVectorToList(Vector<Entity> v) {
		LList<Entity> result = new LList<Entity>();
		for (int i = v.size() - 1; i >= 0; --i) {
			result = result.cons(v.get(i));
		}
		return result;
	}

	private Sequence translateListToSequence(LList<Entity> l) {
		Sequence result = new Sequence();
		// Reverse while constructing
		for (Entity t : l) {
			if (!result.contains(t)) {
				result.addElement(0, t);
			}
		}
		return result;
	}

	// private Sequence translateVectorToSequence(Vector<Entity> l) {
	// Sequence result = new Sequence();
	// for (Entity t : l) {
	// if (!result.contains(t)) {
	// result.addElement(t);
	// }
	// }
	// return result;
	// }

	private boolean checkForDuplicateBindingsToSameEntity(LList<PairOfEntities> bindings, ArrayList<PairOfEntities> exclusions) {
		// Mark.say("Entering duplicate check", exclusions);
		HashMap<Entity, Entity> map = new HashMap<Entity, Entity>();
		for (PairOfEntities p : bindings) {
			Entity variable = p.getPattern();
			Entity binding = p.getDatum();
			map.put(variable, binding);
		}
		for (PairOfEntities p : exclusions) {
			if (map.get(p.getPattern()) == map.get(p.getDatum())) {
				// Mark.say("Value of", p.getPattern().asString(), "and", p.getDatum().asString(), "are both",
				// map.get(p.getPattern()));
				return true;
			}
		}

		return false;
	}

	// private String extractProperNameType(Vector<String> sThread) {
	// String type = null;
	// for (String c : sThread) {
	// if (c.equals(Markers.NAME)) {
	// return type;
	// }
	// else {
	// type = c;
	// }
	// }
	// return null;
	// }

	public boolean member(LList<PairOfEntities> first, LList<LList<PairOfEntities>> rest) {
		for (Object b : rest) {
			LList<PairOfEntities> bindingSet = (LList<PairOfEntities>) b;
			if (equals(first, bindingSet)) {
				return true;
			}
		}
		return false;
	}

	private boolean equals(LList<PairOfEntities> first, LList<PairOfEntities> second) {
		if (first.endP() && second.endP()) {
			return true;
		}
		else if (first.endP() || second.endP()) {
			return false;
		}
		else if (first.first().equals(second.first())) {
			return equals(first.rest(), second.rest());
		}
		return false;
	}
	
	/***
	@author dxh
	**/
	public ArrayList<ConceptDescription> findConceptPatterns(Sequence concepts, Sequence story, Sequence inferences) {
		// Start fresh by clearing discoveries list.
		this.discoveries = new ArrayList<ConceptDescription>();

		
		Mark.say("Concept count.................", concepts.getElements().size());
		
		
		for (Entity concept : concepts.getElements()) {
			try {
				disectAndThenProcessConcept((translateConceptToList((Sequence) concept)), translateStoryToList(story), inferences
				        .getElements(), concept.getType(), new LList<Entity>(), new LList<Entity>(), story);
			}
			catch (Exception e) {
				Mark.err("Blew out of processing", concept.getName(), "probably because recursion to deep");
				return null;
			}

		}
		

		for (ConceptDescription completion : discoveries) {
			Sequence instantiation = completion.getInstantiations();
			instantiation.addType(completion.getName());
		}
		// Create and transmit concept analysis object
		ConceptAnalysis analysis = new ConceptAnalysis(discoveries, story);

		Sequence instantiations = new Sequence(Markers.CONCEPT_MARKER);
		for (ConceptDescription rd : analysis.getConceptDescriptions()) {
			instantiations.addElement(rd.getInstantiations());
		}
		
		return persistentDiscoveries;
		
	}	

}
