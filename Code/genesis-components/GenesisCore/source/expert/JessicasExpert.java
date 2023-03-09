package expert;

import java.util.*;

import connections.*;
import connections.signals.BetterSignal;
import consciousness.JustDoIt;
import constants.*;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.Generator;
import generator.RoleFrames;
import genesis.GenesisGetters;
//import jessicaNoss.*;
import matchers.StandardMatcher;
import mentalModels.MentalModel;
import start.Start;
import storyProcessor.StoryProcessor;
import utils.*;
import utils.minilisp.LList;

/*
 * Created on Jan 13, 2015; Refactored June 22, 2015; Refactored July 27, 2015;
 *   major refactor July 20-27, 2016 (merged in old ObservationsFactory)
 * @author jmn, phw
 */

public class JessicasExpert extends AbstractWiredBox { //TODO rename all instances of JessicasExpert to ObservationsExpert

	public static final String COMMENTARY = "Jessica's commentary";
	public static String STORY_PORT = "story port";
	public static String QUESTION_PORT = "question port";
	public static String DISPLAY_PORT = "display port";
	public static String TELL_PORT = "tell port";

	private final boolean debug = true;
	
	//naive assumptions about Scenes (as delimited by the keyword "Then")
	private final boolean useSceneAssumptionInsertEnter = true; //if a character is mentioned, then they have been observing all events since the scene began
	private final boolean useSceneAssumptionEveryoneExits = true; //when a new scene begins, all characters exit

	private final String DEFAULT = "DEFAULT"; //default scene (if location is not specified)

	private Sequence story;
	private Sequence originalStory; //the raw story, without inferred events
	private StoryProcessor storyProcessor;

	private boolean isNewStory;

	private HashMap<Entity,HashSet<Entity>> containerToCharacters; //todo make this a class, eg ActorContainer, so you can query eg "who is in the kitchen?"
	private Entity currentContainer; //keep track of current location/place of story (eg "kitchen")
	private HashSet<Entity> unconsciousCharacters; //todo replace this list with a consciousness flag attached to each character

	private HashMap<String,Entity> nameToEntity;
	
	@Override
	public String getName() {
		return "Jessica's Observations Expert"; // todo rename
	}

	public JessicasExpert() {
		super("Jessica's Observations Expert"); //todo rename
		connections.Connections.getPorts(this).addSignalProcessor(QUESTION_PORT, this::processQuestion);
//		connections.Connections.getPorts(this).addSignalProcessor(RETELL_PORT, this::processRetell);
		connections.Connections.getPorts(this).addSignalProcessor(StoryProcessor.INCREMENT_PORT_COMPLETE, this::processIncrement);
		//connections.Connections.getPorts(this).addSignalProcessor(STORY_PORT, this::processStory);
		connections.Connections.getPorts(this).addSignalProcessor(STORY_PORT, this::processCompleteStory);
		connections.Connections.getPorts(this).addSignalProcessor(TELL_PORT, this::processTell);
		isNewStory = true;
	}

	public void processCompleteStory(Object s) {
		//things to do when story-reading is complete
		// Not needed, gatekeeper mechanism in use
		// if (!Switch.jessicaCheckBox.isSelected()) {
		// return;
		// }
		if (s instanceof BetterSignal) {
			isNewStory = true;
//			explainConflicts();
		}
	}

	public void processIncrement(Object s) {
		// Not needed, gatekeeper mechanism in use
		// if (!Switch.jessicaCheckBox.isSelected()) {
		// return;
		// }
		//Mark.say(debug, "Received something on INCREMENT_PORT_COMPLETE:", s);
		if (s instanceof BetterSignal) {
			BetterSignal signal = (BetterSignal) s;
			Entity increment = signal.get(0, Entity.class);
			Mark.say(debug, "Received increment:", increment);
			story = signal.get(1, Sequence.class);
			originalStory = GenesisGetters.getMentalModel1().getStoryProcessor().getExplicitElements();
			if (isNewStory) {
				storyProcessor = signal.get(2, StoryProcessor.class);
				containerToCharacters = new HashMap<Entity,HashSet<Entity>>();
				currentContainer = new Entity(DEFAULT);
				unconsciousCharacters = new HashSet<Entity>();
				nameToEntity = new HashMap<String,Entity>();
				isNewStory = false;
			}
			
			//check for scene marker
			if (isSceneMarker(increment)) {
				if (useSceneAssumptionEveryoneExits) {
					processExitEveryone(currentContainer);
				}
				return;
			}

			Entity subject = increment.getSubject();
			Entity object = RoleFrames.getObject(increment);

			//todo combine the enter/exit statements (eg with ternaries) to have half as many if's
			if (isEnterScene(increment)) {
				processEntry(subject, currentContainer);
				openMentalModel(subject);
				Mark.say(debug, "processing entry of", subject.getName(), "into current container", currentContainer.getName());
			} else if (isExitScene(increment)) {
				processExit(subject, currentContainer);
				Mark.say(debug, "processing exit of", subject.getName(), "from current container", currentContainer.getName());
			} else if (isEnterPlace(increment)) {
				processEntry(subject, object); //note that object is not necessarily the current container
				if (object == currentContainer) {
					openMentalModel(subject);
				}
				Mark.say(debug, "processing entry of", subject.getName(), "into", object.getName());
			} else if (isExitPlace(increment)) {
				processExit(subject, object); //note that object is not necessarily the current container
				Mark.say(debug, "processing exit of", subject.getName(), "from", object.getName());
			} else if (isLoseConsciousness(increment)) {
				Mark.say(debug, "Lose consciousness:", subject);
				processLoseConsciousness(subject);
			} else if (isAwaken(increment)) {
				Mark.say(debug, "Awaken:", subject);
				processAwaken(subject);

			} else if (isSceneShift(increment)) {
				Entity newContainer = increment.getObject().get(0).getSubject(); //todo gross
				if (currentContainer == newContainer) {
					return; //ignore scene shift //todo but maybe don't ignore if it represents a time delay (another scene in the same place, but later in the story?)
				}

				//close currentContainer MMs, if any
				HashSet<Entity> oldCharacters = containerToCharacters.get(currentContainer);
				if (oldCharacters != null) {
					for (Entity character : oldCharacters) {
						storyProcessor.closeMentalModel(character);
					}
				}

				//update currentContainer
				Mark.say(debug, "set current container to", increment.getObject().get(0).getSubject()); //todo gross
				currentContainer = newContainer;

				//open new-currentContainer MMs, if any
				if (!containerToCharacters.containsKey(currentContainer)) {
					containerToCharacters.put(currentContainer, new HashSet<Entity>());
				} else {
					HashSet<Entity> newCharacters = containerToCharacters.get(currentContainer);
					if (newCharacters != null) {
						for (Entity character : newCharacters) {
							if (!isUnconscious(character)) {
								openMentalModel(character);
							}
						}
					}
				}

				// Leah sample
				Entity sceneMarker = Start.makeThing("scene");
				sceneMarker.addProperty(Markers.SCENE, true);
				storyProcessor.addElement(sceneMarker, story);

			} else {
				Mark.say(debug, "standard event (not entry/exit, scene shift, etc");
				// insert entrances of mentioned characters
				if (!story.isAPrimed(Markers.CONCEPT_MARKER)) {
					//regular story, not concept story
					if (useSceneAssumptionInsertEnter) { //TODO clean up nested if/for's
						for (Entity character : charactersMentioned(increment)) {
							if (!isPresentInScene(character) && !isUnconscious(character)) {
								//insert character's entrance at beginning of current scene
								Mark.say(debug, " -> inserting entrance of", character.getName(), "at scene start");
								Entity entranceEntity = createEntranceEntity(character);
								storyProcessor.injectElementWithDereferenceAtSceneStart(entranceEntity); //todo they need to enter a room, not "scene"
							}
						}
					}
				}
				
				//todo? I probably need recordEvent to happen *after* processing enter/exit/scene change
//				recordEvent(increment); //apparently this happens automatically??

				//TODO transmit observations to JessicasDisplay (or decide not to)
//				observations = observationsFactory.getObservations();
//				Mark.say(debug, "Transmitting Observations to JessicasDisplay"); //TODO rename
//				Connections.getPorts(this).transmit(DISPLAY_PORT, new BetterSignal(observations)); //TODO don't need to transmit entire obs each time
			}
		}
	}

	private void openMentalModel(Entity character) {
		String name = character.toEnglish().toLowerCase();
		if (!nameToEntity.containsKey(name)) {
			nameToEntity.put(name, character);
		}
		storyProcessor.openMentalModel(character);
	}

	private MentalModel getMentalModel(String name) {
		//input: name as string without identifier, e.g. "david" or "David"
		Entity character = getEntity(name);
		if (character == null) {
			return null;
		}
		return getMentalModel(character);
	}
	private MentalModel getMentalModel(Entity character) {
		return storyProcessor.getMentalModel().getLocalMentalModel(character.getName());
	}

	private Entity getEntity(String name) {
		return nameToEntity.get(name.toLowerCase());
	}

////////////////////////////////////////////////////////////////////////////////
// Adapted from ObservationsFactory
	private void processEntry(Entity character, Entity container) {
		processAwaken(character);
		if (!containerToCharacters.containsKey(container)) {
			containerToCharacters.put(container, new HashSet<Entity>());
		}
		containerToCharacters.get(container).add(character);
		Mark.say(debug, "Added to scene and opened mental model:", character);
	}

	private void processAwaken(Entity character) {
		if (isPresentInScene(character)) {
			openMentalModel(character);
		}
		unconsciousCharacters.remove(character);
	}

	private void processExit(Entity character, Entity container) {
		// special case for "Everyone exits"
		if ("everyone".equals(character.getProperName()) && getMentalModel("everyone") == null) {
			// Note (6/3/2016): This magically started working again with no changes.
			// But if character.getProperName() starts returning null again, try: "Everyone".equals(character.toEnglish())
			processExitEveryone(container);
			return;
		}
		
		processLoseConsciousness(character);
		containerToCharacters.get(container).remove(character);
		if (container == currentContainer) {
			storyProcessor.closeMentalModel(character);
		}
		Mark.say(debug, "Removed from scene and closed mental model:", character);
	}
	
	private void processExitEveryone(Entity container) {
		Set<Entity> charactersToRemove = new HashSet<Entity>(containerToCharacters.get(container));
		for (Entity character : charactersToRemove) {
			processExit(character, container);
		}
	}

	private void processLoseConsciousness(Entity character) {
		storyProcessor.closeMentalModel(character);
		unconsciousCharacters.add(character);
	}

	private boolean isPresentInScene(Entity character) {
		Mark.say(debug, "current container", currentContainer);
		Mark.say(debug, containerToCharacters);
		if (!containerToCharacters.containsKey(currentContainer)) {
			return false;
		}
		return containerToCharacters.get(currentContainer).contains(character);
	}

	private boolean isUnconscious(Entity character) {
		//by default, assume every character is awake unless we're told otherwise
		//todo this is a bit of a hack (eg "Everyone is sleeping. John gets up in the night...")
		return unconsciousCharacters.contains(character);
	}

////////////////////////////////////////////////////////////////////////////////

	private boolean isSceneMarker(Entity e) {
		return e.getBooleanProperty(Markers.SCENE);
	}
	
	private boolean isSceneShift(Entity e) {
//		Entity seq = e.getObject();
//		Entity fun = seq.get(0);
//		Mark.say("     ", fun.getSubject());
		return e.getSubject() != null && e.getSubject().isA("scene") //todo please find a cleaner way to do this (also see the mess in charactersMentioned, below)
				&& e.isA("shift")
				&& e.getObject() != null
				&& e.getObject().get(0) != null
				&& isPlace(e.getObject().get(0).getSubject());
	}
	
	private boolean isEnterScene(Entity e) {
		return e.isA("enter") && isObjectAScene(e);
	}

	private boolean isExitScene(Entity e) {
		return e.isA("exit") && isObjectAScene(e);
	}
	
	private boolean isEnterPlace(Entity e) {
		return e.isA("enter") && isObjectAPlace(e);
	}

	private boolean isExitPlace(Entity e) {
		return e.isA("exit") && isObjectAPlace(e);
	}

	private boolean isObjectAScene(Entity e) {
		return RoleFrames.getObject(e)!=null && RoleFrames.getObject(e).isA("scene");
	}

	private boolean isObjectAPlace(Entity e) {
		return RoleFrames.getObject(e)!=null && isPlace(RoleFrames.getObject(e));
	}

	private boolean isPlace(Entity e) {
		return e.isA("structure") || e.isA("location") || e.isA("land");
	}

	private boolean isLoseConsciousness(Entity e) {
		return e.isA("lose") && RoleFrames.getObject(e)!=null && RoleFrames.getObject(e).isA("consciousness");
	}

	private boolean isAwaken(Entity e) {
		return e.isA("awaken");
	}

	private Set<Entity> charactersMentioned(Entity increment) {
		Set<Entity> mentioned = new HashSet<Entity>();
		List<Entity> possibleCharacters = new ArrayList<Entity>();
		possibleCharacters.add(increment.getSubject());
		Entity object = RoleFrames.getObject(increment);
		if (object != null) {
			possibleCharacters.add(object);
		}
		for (Entity child : increment.getObject().getChildren()) {
			possibleCharacters.add(child.getSubject());
			//TODO this assumes that every sentence has the form [subject, roles]
			//... and that every "roles" has children of the form (fun __ (ent NOUN)),
			//... where NOUN is the Entity that we want to add to possibleCharacters.
		}
		
		for (Entity e : possibleCharacters) {
			if (e.isA("name") && e.hasProperty("proper")) {
				mentioned.add(e);
			}
		}
		return mentioned;
	}
	
	private Entity createEntranceEntity(Entity character) { //TODO is there a way to avoid this step by directly invoking processEntry()?
		Function object = new Function("object", new Entity("scene"));
		Sequence roles = new Sequence("roles");
		roles.addElement(object);
		return new Relation("enter", character, roles);
	}
	
	private Entity negation(Entity e) {
		Entity e2 = (Entity)e.clone();
		e2.addFeature(Markers.NOT);
		return e2;
	}
	
	public void explainConflict(Entity char1, Entity char2) {
		/**
		 * Detects the first conflict that occurs between char1 and char2 and explains it.
		 */
		List<Entity> eventList = detectConflict(char1, char2);
		if (eventList.isEmpty()) {
			say("No conflict detected between", char1.toEnglish(), "and", char2.toEnglish());
			return;
		}
		Entity event1 = eventList.get(0);
		Entity event2 = eventList.get(1);
		say("Question: Why did", char1.toEnglish(), "think that", event1.toEnglish() + "?");
		answerWhyBelieveQuestion(char1, event1);
		say("Question: Why did", char2.toEnglish(), "think that", event2.toEnglish() + "?");
		answerWhyBelieveQuestion(char2, event2);
	}
	
	public List<Entity> detectConflict(Entity char1, Entity char2) {
		/**
		 * Detects the first conflict that occurs between char1 and char2
		 */
		List<Entity> eventsList = new ArrayList<Entity>();
		Sequence charStory1 = getMentalModel(char1).getStoryProcessor().getStory();
		Sequence charStory2 = getMentalModel(char2).getStoryProcessor().getStory();
		for (Entity event : charStory1.getElements()) {
//			Mark.say(" *** ", event.toEnglish(), negation(event).toEnglish()); //todo rm
			if (matchesStoryElement(negation(event), charStory2)) {
				say("Conflict detected:", char1.toEnglish(), "and", char2.toEnglish(), "disagree about", event.toEnglish());
				eventsList.add(event);
				eventsList.add(negation(event));
				return eventsList;
			}
		}
		for (Entity event : charStory2.getElements()) {
			if (matchesStoryElement(negation(event), charStory1)) {
				say("Conflict detected:", char2.toEnglish(), "and", char1.toEnglish(), "disagree about", event.toEnglish());
				eventsList.add(negation(event));
				eventsList.add(event);
				return eventsList;
			}
		}
		return eventsList;
	}
	
	public void explainConflicts() { //todo better: call this when user asks "Why do X and Y disagree?"
		for (Entity character : nameToEntity.values()) {
			MentalModel mentalModel = getMentalModel(character);
			Sequence characterStory = mentalModel.getStoryProcessor().getStory();
			for (Entity event : characterStory.getElements()) {
				for (Entity otherCharacter : nameToEntity.values()) { //todo don't double-count characters, and don't let characters conflict with selves?
					MentalModel otherMentalModel = getMentalModel(otherCharacter);
					Sequence otherStory = otherMentalModel.getStoryProcessor().getStory();
					if (matchesStoryElement(negation(event), otherStory)) {
						Mark.say("Conflict detected:", character, event, otherCharacter, negation(event));
					}
				}
			}
		}
	}

	public void processQuestion(Object s) {
		// Not needed, gatekeeper mechanism in use
		// if (!Switch.jessicaCheckBox.isSelected()) { //todo maybe this should depend on the Question handler radio
		// button?
		// return;
		// }
		if (s instanceof BetterSignal) {
			BetterSignal signal = (BetterSignal) s;
			Entity fullQuestion = signal.get(2, Entity.class);
			Entity questionContent = signal.get(0, Entity.class);

			if (QuestionExpert.isWhy(fullQuestion)) {
				Mark.say(debug, "JessicasExpert received a WHY question:", questionContent);
				answerWhyQuestion(questionContent);
			} else if (QuestionExpert.isDid(fullQuestion)) {
				Mark.say(debug, "JessicasExpert received a DID question:", questionContent);
				boolean ans = answerDidQuestion(questionContent, true);
				if (debug) {
					String questionString = "Did " + Generator.getGenerator().generate(questionContent);
					String answerString = ans ? "yes" : "no";
					String displayString = "Question: " + questionString + ", Answer: " + answerString;
					Mark.say(displayString);
				}
//				Connections.getPorts(this).transmit(DISPLAY_PORT, new BetterSignal(displayString)); //todo rm
			} else {
				Mark.err("Unsupported question type in Agent-focused mode, with question:", fullQuestion);
			}
		}
	}
	
	public void processTell(Object s) {
//		Mark.say(debug, "TELL port received", s);
		// Not needed, gatekeeper mechanism in use
		// if (!Switch.jessicaCheckBox.isSelected()) {
		// return;
		// }
		if (s instanceof Relation) {
			Relation tellCommand = (Relation) s;
			Mark.say(debug, "JessicasExpert received a TELL command:", tellCommand);
			if (!RoleFrames.getObject(tellCommand).isA("story")) {
				say("Unfamiliar TELL command:", RoleFrames.getObject(tellCommand).toEnglish());
				return;
			}
			Entity character = tellCommand.getObject().getElement(1).getSubject(); //TODO use Entity + matcher instead of name?
			String name = character.toEnglish();
			say("Trying to tell story as", name + "...");
			MentalModel mentalModel = getMentalModel(name);
			if (mentalModel == null) {
				say(name, "is not a character in this story.");
				say("The characters are:", nameToEntity.keySet());
				return;
			}

			String storyString = "";
			Sequence seq = mentalModel.getStoryProcessor().getStory();
			for (Entity e : seq.getElements()) {
				storyString += e.toEnglish() + " ";
			}
			say("=>", name + "'s story:", storyString);
		}
//		observations = observationsFactory.getObservations();
//		Mark.say(debug, "Retelling story as each actor...");
//		for (Actor actor : observations.getActors()) {
//			Mark.say(debug, "    =>", actor.getName() + "'s story:", actor.getStoryString());
//		}
	}

	private void say(Object... objects) {
		Mark.comment(this, COMMENTARY, GenesisConstants.LEFT, Markers.PERSPECTIVE_TAB, objects);
	}

	private void answerWhyQuestion(Entity question) {
		if (isDisagreeQuestion(question)) {
			Mark.say("Found disagree question");
			answerWhyDisagreeQuestion(question);
			return;
		}
		if (!answerDidQuestion(question, false)) {
			say("Invalid question: It is false that", question.toEnglish());
			return;
		}
		if (!isBeliefQuestion(question)) {
			Mark.err("Unsupported WHY question type");
			return;
		}

		Entity character = question.getSubject();
		Entity event = RoleFrames.getObject(question);
		answerWhyBelieveQuestion(character, event);
	}
	
	private void answerWhyBelieveQuestion(Entity character, Entity event) {
		String name = character.toEnglish();
		if (matchesStoryElement(event, originalStory)) {
			say("Because", name, "observed that", event.toEnglish());
		} else {
			MentalModel mentalModel = getMentalModel(character);
//			BetterSignal s = (new JustDoIt()).explain(mentalModel, event);
//			Entity explanation = s.get(1, Entity.class);
//			say(name, "infers that", explanation.toEnglish());
//			say("Because", name, "inferred that", event.toEnglish()); //simple explanation
		}
	}
	
	private void answerWhyDisagreeQuestion(Entity question) {
		Entity char1 = question.getSubject();
		Entity object = question.getObject(); //todo find a cleaner way to extract char2
		Sequence s = (Sequence)object;
		Function f = (Function)s.getElement(0);
		Entity char2 = f.getAllComponents().get(0);
		explainConflict(char1, char2);
	}

	private boolean answerDidQuestion(Entity question, boolean displayAnswer) {
		if (matchesStoryElement(question, story)) { //note that story includes inferred events
			Mark.say(debug, "found event in story");
			if (displayAnswer) {say("Yes", question.toEnglish());} //todo test this
			return true;
		}
		else if (isBeliefQuestion(question)) {
			return answerBeliefQuestion(question, displayAnswer);
		}
		if (displayAnswer) {say("No");} //todo maybe change this?
		return false;
	}

	private boolean isBeliefQuestion(Entity question) {
		return question.getType().equals("think") || question.getType().equals("believe");
	}

	private boolean isDisagreeQuestion(Entity question) {
		return question.getType().equals("disagree") || question.getType().equals("conflict");
	}

	private boolean answerBeliefQuestion(Entity question, boolean displayAnswer) {
		Entity subject = question.getSubject();
		MentalModel mentalModel = getMentalModel(subject);
		String name = subject.toEnglish();
		Entity event = RoleFrames.getObject(question);
		if (mentalModel == null) {
			if (displayAnswer) {say("No,", name, "is not a character in this story.");}
			return false;
		} else if (mentalModelContainsEvent(mentalModel, event)) {
			if (displayAnswer) {say("Yes,", name, "thinks that", event.toEnglish());}
			return true;
		} else {
			if (displayAnswer) {say("No,", name, "does not think that", event.toEnglish());}
			return false;
		}
	}

	private boolean mentalModelContainsEvent(MentalModel mentalModel, Entity event) {
		Sequence seq = mentalModel.getStoryProcessor().getStory();
		return matchesStoryElement(event, seq);
	}

	private boolean matchesStoryElement(Entity assertion, Sequence targetStory) {
		// StandardMatcher matcher = StandardMatcher.getIdentityMatcher();
		StandardMatcher matcher = StandardMatcher.getBasicMatcher();
//		if (debug) {
//			Vector<Entity> elements = story.getElements();
//			for (Entity storyElement : elements) {
//				LList<PairOfEntities> bindings = matcher.match(assertion, storyElement);
//				if (bindings != null) {
//					Mark.say("found match:", storyElement);
//					Mark.say("  with bindings:", bindings);
//				}
//			}
//		}
		return targetStory.getElements().parallelStream().anyMatch(storyElement -> {
			LList<PairOfEntities> bindings = matcher.match(assertion, storyElement);
			return (bindings != null);
		});
	}

//	private boolean appearsInStory(Entity element) {
//		return story.getElements().parallelStream().anyMatch(f -> f == element);
//	}

}
