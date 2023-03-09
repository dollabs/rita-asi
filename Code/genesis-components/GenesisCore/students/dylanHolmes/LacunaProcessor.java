package dylanHolmes;

import gui.TabbedTextViewer;

import java.util.ArrayList;

import matchers.StandardMatcher;
import storyProcessor.ConceptTranslator;
import storyProcessor.StoryProcessor;
import utils.Html;
import translator.BasicTranslator;
import utils.Mark;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;

/**
 * A local processor class that just receives a complete story description, takes apart the object to fetch various
 * parts of the complete story description, and prints them so I can see what is in there.
 */
public class LacunaProcessor extends AbstractWiredBox {

	public static final String INPUT_COMPLETE_STORY = "my input port";

	public static final String OUTPUT_REDUCED_STORY = "my output port";

	public Sequence explicitElements = new Sequence();

	public Sequence lacunae = new Sequence();

	/**
	 * The constructor places the processSignal signal processor on two input ports for illustration. Only the
	 * StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT is wired to in LocalGenesis.
	 */
	public LacunaProcessor() {
		this.setName("Lacuna processor");
		Connections.getPorts(this).addSignalProcessor(INPUT_COMPLETE_STORY, "replaceStory");
	}

	public void outputReducedStory() {
		// if( this.explicitElements.getElements().size() == 0 ||
		// this.lacunae.getElements().size() == 0){
		// return;
		// }
		Mark.say("outputting reduced story");

		// TODO: Debug code follows (not actually a todo)
		try {
			// this.lacunae.addElement(Translator.getTranslator().translate("Patrick wants the ball").getElement(0));
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}

		Sequence reducedStory = new Sequence();
		for (Entity e : this.explicitElements.getElements()) {
			Boolean excise = false;
			for (Entity lacuna : this.lacunae.getElements()) {
				Mark.say("lacuna: " + lacuna.asString());
				if (!excise) {
					excise |= (null != StandardMatcher.getBasicMatcher().matchAnyPart(lacuna, e));
				}
			}

			// Mark.say(excise ? "1" : "0");Mark.say(e.toString());

			if (!excise) {
				reducedStory.addElement(e);
			}

		}
		BetterSignal op_signal = new BetterSignal(this.lacunae, this.explicitElements, reducedStory);
		Mark.say("Transmitting reduced story from ", this.getName(), "!");

		Connections.getPorts(this).transmit(OUTPUT_REDUCED_STORY, op_signal);

		// Inserted by phw
		Sequence sampleResult = new Sequence();
		// Remove murders
		for (Entity e : explicitElements.getElements()) {
			// Note that this does not remove kill events embedded in cause expressions
			if (!e.isA("kill")) {
				sampleResult.addElement(e);
			}
		}
		// Mark.say("Reduced story has", explicitElements.getElements().size(), "elements");
		// for (Entity e : explicitElements.getElements()) {
		// Mark.say("Element", e);
		// }
		Connections.getPorts(this).transmit(OUTPUT_REDUCED_STORY, sampleResult);

	}

	public void addLacuna(Object signal) {
		if (signal instanceof BetterSignal) {
			BetterSignal s = (BetterSignal) signal;
			Entity lacuna = s.get(0, Entity.class);
			this.lacunae.addElement(lacuna);
			outputReducedStory();
		}
	}

	public void replaceStory(Object signal) {
		// Should always check to be sure my input is in the expected form and ignore it if not. A BetterSignal is just
		// a convenient container for multiple objects that allows easy extraction of objects without further casting.
		if (signal instanceof BetterSignal) {
			Mark.say("Lacuna processor");

			BetterSignal s = (BetterSignal) signal;
			Sequence story = s.get(0, Sequence.class);
			Sequence explicitElements = s.get(1, Sequence.class);
			Sequence inferences = s.get(2, Sequence.class);
			Sequence concepts = s.get(3, Sequence.class);
			// Now proceed to print what has come into my box.
			// Mark.say("\n\n\nStory elements");
			// for (Entity e : story.getElements()) {
			// Mark.say(e.asString());
			// }
			// Mark.say("\n\n\nExplicit story elements");
			// for (Entity e : explicitElements.getElements()) {
			// Mark.say(e.asString());
			// }

			this.explicitElements = explicitElements;
			outputReducedStory();
		}
	}
}