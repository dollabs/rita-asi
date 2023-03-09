package matthewFay.CharacterModeling;

import java.util.*;

import matthewFay.representations.BasicCharacterModel;
import matthewFay.viewers.CharacterViewer;
import matthewFay.StoryGeneration.PlotWeaver;
import start.StartPreprocessor;
import utils.Mark;
import connections.AbstractWiredBox;
import connections.Connections;
import connections.signals.BetterSignal;
import constants.Markers;
import frames.entities.Entity;

/**
 * A WiredBox Processor that Manages all the characters in a story Current Goal: Get output from StoryProcessors and use
 * that to create characters Longer Goal: Model the readers as characters thinking about characters
 * 
 * @author Matthew
 */
public class CharacterProcessor extends AbstractWiredBox {
	public static boolean debug_logging = false;

	public static final String PLOT_PLAY_BY_PLAY_PORT = "plot play by play port";

	public static final String STAGE_DIRECTION_PORT = "reset port";

	public static final String COMPLETE_STORY_ANALYSIS_PORT = "complete story analysis port";

	public CharacterProcessor() {
		super("CharacterProcessor");

		Connections.getPorts(this).addSignalProcessor(STAGE_DIRECTION_PORT, "reset");
		Connections.getPorts(this).addSignalProcessor(PLOT_PLAY_BY_PLAY_PORT, "processPlotElement");
		Connections.getPorts(this).addSignalProcessor(COMPLETE_STORY_ANALYSIS_PORT, "processCompleteStory");
		reset(Markers.RESET);
	}

	// PLOT INJECTION HACK
	private List<String> seen_english = new ArrayList<>();

	private static LinkedHashMap<Entity, BasicCharacterModel> character_library = new LinkedHashMap<>();

	private static Set<Entity> generics_library = new HashSet<>();

	private static ArrayList<BasicCharacterModel> active_characters = new ArrayList<>();

	public static ArrayList<BasicCharacterModel> getActiveCharacters() {
		return new ArrayList<>(active_characters);
	}

	public static BasicCharacterModel getCharacterModel(Entity e, boolean create_on_fail) {
		BasicCharacterModel c = character_library.get(e);
		if (!character_library.containsKey(e)) {
			if (create_on_fail) {
				BasicCharacterModel character = new BasicCharacterModel(e);
				character_library.put(e, character);
				active_characters.add(character);
				Mark.say(debug_logging, "New character added to library: " + e);
			}
			else {
				return null;
			}
		}
		return character_library.get(e);
	}

	public static BasicCharacterModel findBestCharacterModel(String name) {
		for (Entity character_entity : character_library.keySet()) {
			if (character_entity.getType().equals(name)) {
				return character_library.get(character_entity);
			}
		}
		return null;
	}

	public static BasicCharacterModel findBestCharacterModel(Entity e) {
		if (character_library.containsKey(e)) return character_library.get(e);
		for (Entity character_entity : character_library.keySet()) {
			if (character_entity.getType().equals(e.getType())) {
				return character_library.get(character_entity);
			}
		}
		return null;
	}

	public static boolean isCharacter(Entity e) {
		if (character_library.containsKey(e)) {
			return true;
		}
		return false;
	}

	public static LinkedHashMap<Entity, BasicCharacterModel> getCharacterLibrary() {
		return character_library;
	}

	public static Set<Entity> getGenericsLibrary() {
		return generics_library;
	}

	public static void deleteCharacter(Entity e) {
		if (character_library.containsKey(e)) {
			active_characters.remove(character_library.get(e));
		}
		character_library.remove(e);
	}

	private static HashMap<String, Integer> action_library = new HashMap<>();

	// CascadingCharacterModel reader;

	// CascadingCharacterModel readerLimited;

	public void reset(Object o) {
		if (o.equals(Markers.RESET)) {
			active_characters.clear();
			// character_library.clear();

			// reader = new CascadingCharacterModel(new Entity("Reader"));
			// reader.mode = ObservationMode.ALL;
			// readerLimited = new CascadingCharacterModel(new Entity("Limited Reader"));

			// PLOT INJECTION HACK
			seen_english.clear();
		}
	}

	public void processPlotElement(Object o) {
		if (CharacterViewer.disableCharacterProcessor.isSelected()) {
			return;
		}
		// Verify it's a Signal
		BetterSignal s = BetterSignal.isSignal(o);
		if (s == null) return;

		// Verify it's a plot element
		Entity element = s.get(0, Entity.class);
		if (element == null) return;

		if (storyComplete) {
			active_characters.clear();
			storyComplete = false;
		}

		// Check for specific "Save Command"
		if (element.toString().contains("save") && element.toString().contains("characters")) {
			Mark.err("Save command detected!");
			Mark.err(element);
			return;
		}

		seen_english.add(element.toEnglish());

		if (PlotWeaver.isWeaveCharactersEvent(element)) {
			Mark.say("Weave character plots!");
			for (BasicCharacterModel character : active_characters) {
				Mark.say(character);
			}
			PlotWeaver pw = new PlotWeaver(active_characters);
			List<Entity> plot = pw.weavePlots();

			CharacterViewer.disableCharacterProcessor.setSelected(true);
			List<String> new_plot_events = new ArrayList<>();
			for (Entity plot_elt : plot) {
				String english = plot_elt.toEnglish();

				// PLOT INJECTION HACK
				// Two passes, one for 'is a's and the other for action
				if (!seen_english.contains(english)) {
					seen_english.add(english);
					if (english.contains(" is a")) {
						Mark.say(english);
						StartPreprocessor.getStartPreprocessor().process(english);
					}
					else {
						new_plot_events.add(english);
					}
				}

			}
			for (String english : new_plot_events) {
				Mark.say(english);
				StartPreprocessor.getStartPreprocessor().process(english);
			}
			CharacterViewer.disableCharacterProcessor.setSelected(false);
			return;
		}

		// Do Basic Character Processing
		if (BasicCharacterModel.isCharacterMarker(element)) {
			Entity character_entity = BasicCharacterModel.extractCharacterEntity(element);
			BasicCharacterModel character = CharacterProcessor.getCharacterModel(character_entity, true);
			// Mark.say("Found character: " + character);
		}
		else {
			// Not a character marker
			if (BasicCharacterModel.isGenericMarker(element)) {

				Entity e = BasicCharacterModel.extractGenericEntity(element);
				generics_library.add(e);

				Mark.say(debug_logging, "New generic added to library: " + e);
			}
		}

		updateModels(element);
	}

	public void updateModels(Entity element) {
		if (!CharacterViewer.trackOnlyExplicitEvents.isSelected()
		        || (!element.getType().equals("prediction") && !element.getType().equals("explanation") && !element.getType().equals("entail") /*
																																				 * &&
																																				 * !
																																				 * element
																																				 * .
																																				 * getType
																																				 * (
																																				 * )
																																				 * .
																																				 * equals
																																				 * (
																																				 * "cause"
																																				 * )
																																				 */)) {
			for (BasicCharacterModel character : active_characters) {
				character.observeEvent(element);
			}
		}

		// Do Cascading Character Processing
		// reader.observeEvent(element);
		// readerLimited.observeEvent(element);
	}

	boolean storyComplete = true;

	public void processCompleteStory(Object o) {
		// reader.endStory();
		// readerLimited.endStory();

		storyComplete = true;

	}
}
