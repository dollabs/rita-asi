package storyProcessor;

import java.util.Vector;

import utils.Mark;
import utils.tools.Filters;
import connections.*;
import connections.signals.BetterSignal;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;

/*
 * Created on Apr 26, 2014
 * @author phw
 */

public class TestStoryOutputBox extends AbstractWiredBox {

	private static TestStoryOutputBox box;

	public static TestStoryOutputBox getBox() {
		if (box == null) {
			box = new TestStoryOutputBox();
		}
		return box;
	}

	public TestStoryOutputBox() {
		super("Test story output box");
		Connections.getPorts(this).addSignalProcessor("processStory");
	}

	public void processStory(Object object) {
		if (object instanceof BetterSignal) {
			BetterSignal bs = (BetterSignal) object;
			Sequence story = bs.get(0, Sequence.class);
			Vector<Relation> harms;
			Vector<Relation> helps;

			harms = Filters.findActionsBy("paul", Filters.findHarmingActions(story));
			for (Entity harm : harms) {
				Mark.say("Paul's harms:", harm);
			}

			helps = Filters.findActionsBy("paul", Filters.findHelpingActions(story));
			for (Entity help : helps) {
				Mark.say("Paul's helps:", help);
			}

			harms = Filters.findActionsBy("peter", Filters.findHarmingActions(story));
			for (Entity harm : harms) {
				Mark.say("Peter's harms:", harm);
			}

			helps = Filters.findActionsBy("peter", Filters.findHelpingActions(story));
			for (Entity help : helps) {
				Mark.say("Peter's helps:", help);
			}

		}
	}
}
