package matthewFay.CharacterModeling;

import generator.Generator;

import java.util.ArrayList;
import java.util.List;

import translator.BasicTranslator;
import utils.Mark;
import matchers.StandardMatcher;
import matthewFay.Utilities.SimpleFileReader;
import connections.AbstractWiredBox;
import connections.Connections;
import connections.signals.BetterSignal;
import constants.Markers;
import frames.entities.Entity;

/**
 * A WiredBox Processor that Manages the domain of possible actions that characters can take in a story Current Goal:
 * Get parsed output from "Action Sets" which will contain possible actions characters can take Future Goal: Possibly
 * segment actions by character traits i.e. certain characters more or less likely to take particular actions
 * 
 * @author Matthew
 */
public class ActionProcessor extends AbstractWiredBox {
	public static final String PLOT_PLAY_BY_PLAY_PORT = "plot play by play port";

	public static final String STAGE_DIRECTION_PORT = "reset port";

	public ActionProcessor() {

		super("Action processor");

		Connections.getPorts(this).addSignalProcessor(STAGE_DIRECTION_PORT, "reset");
		Connections.getPorts(this).addSignalProcessor(PLOT_PLAY_BY_PLAY_PORT, "processPlotElement");
		reset(Markers.RESET);
	}

	List<Entity> actionSet = new ArrayList<Entity>();

	boolean actionAdditionMode = false;

	public void reset(Object o) {
		Mark.say("CCC");
		if (o.equals(Markers.RESET)) {

		}
	}

	public void loadActionSet(String fileName) {
		SimpleFileReader r = new SimpleFileReader(fileName);

		boolean preconditions = true;
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		Generator generator = Generator.getGenerator();
		generator.setStoryMode();
		generator.flush();

		String line;
		while ((line = r.nextLine()) != null) {
			// Do stuff with the actions stuff//
			if (line.contains("Start action library.")) {
				preconditions = false;
				continue;
			}
			if (line.contains("End action library.")) {
				preconditions = true;
				continue;
			}

			try {
				Entity element = basicTranslator.translate(line).getElement(0);
				if (!preconditions) {
					actionSet.add(element);
				}
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		generator.flush();
	}

	public void processPlotElement(Object o) {
		// Verify it's a Signal
		BetterSignal s = BetterSignal.isSignal(o);
		if (s == null) return;
		// Verify it's a plot element
		Entity element = s.get(0, Entity.class);

		// Check if we're starting an action set
		if (isActionSetStart(element)) {
			actionAdditionMode = true;
			return;
		}
		if (isActionSetEnd(element)) {
			actionAdditionMode = false;
			return;
		}

		// Should probably chop out characterizations at some point
		if (actionAdditionMode) actionSet.add(element);
	}

	public static boolean isActionSetStart(Entity element) {
		if (element.relationP("start")) {
			if (element.getObject().sequenceP("roles")) {
				if (element.getObject().getElement(0).functionP("object")) {
					if (element.getObject().getElement(0).getSubject().entityP("action_library")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isActionSetEnd(Entity element) {
		if (element.relationP("end")) {
			if (element.getObject().sequenceP("roles")) {
				if (element.getObject().getElement(0).functionP("object")) {
					if (element.getObject().getElement(0).getSubject().entityP("action_library")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
		ActionProcessor ap = new ActionProcessor();
		ap.loadActionSet("c:\\users\\matthew\\git\\genesis\\corpora\\stories\\matthewFay\\charactermodelling\\Scratch.txt");

		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		Generator generator = Generator.getGenerator();
		generator.setStoryMode();
		generator.flush();

		Entity definition = basicTranslator.translate("Macbeth is a character.").getElement(0);
		definition = basicTranslator.translate("Macbeth is a person.").getElement(0);
		Entity definition2 = basicTranslator.translate("Duncan is a person.").getElement(0);
		Entity event = basicTranslator.translate("Macbeth may kill Duncan.").getElement(0);

		for (Entity action : ap.actionSet) {
			Mark.say("Matching... " + event + ", " + action);
			Mark.say("Result: " + StandardMatcher.getBasicMatcher().match(action, event));
		}
	}
}
