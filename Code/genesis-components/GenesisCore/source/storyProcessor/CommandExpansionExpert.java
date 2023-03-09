package storyProcessor;

import java.util.*;

import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Switch;
import frames.entities.Entity;
import frames.entities.Sequence;
import generator.RoleFrames;
import storyProcessor.StoryProcessor;
import translator.NewRuleSet;
import utils.Mark;

/*
 * Created on Aug 16, 2015
 * @author phw
 */

public class CommandExpansionExpert extends AbstractWiredBox {

	public final static String STORY = "stream of consciousness";

	public CommandExpansionExpert() {
		super("Command expansion expert");
		Connections.getPorts(this).addSignalProcessor(STORY, this::process);
	}

	public void process(Object signal) {

		if (!Switch.allowRepeatedCommands.isSelected()) {
			return;
		}

		boolean debug = false;

		BetterSignal bs = (BetterSignal) signal;
		if (bs.size() != 2) {
			return;
		}
		Entity command = bs.get(0, Entity.class);

		StoryProcessor processor = bs.get(1, StoryProcessor.class);
		Sequence story = processor.getStory();
		// Entity command = story.getElement(story.getElements().size() - 1);

		if (story.isA(Markers.CONCEPT_MARKER)) {
			return;
		}

		// Mark.say("\n>>> Command received", isCompleteCommand(command), command);

		if (!isCompleteCommand(command)) {

			Mark.say(debug, "Incomplete", command);
			// Command is not complete; look for most commands that complete, most recent first

			List<Entity> elements = (List<Entity>) (story.getElements().clone());
			Collections.reverse(elements);

			Entity replacement = null;

			double lowestScore;

			RoleFrameMatcher.MatchDescription bestMatch = null;

			for (Entity e : elements) {

				if (isCompleteCommand(e)) {
					Mark.say(debug, "Found complete command", e);
					// Now, expand incomplete command

					RoleFrameMatcher.MatchDescription match = RoleFrameMatcher.getRoleFrameMatcher().match(command, e);

					if (bestMatch == null) {
						bestMatch = match;
						lowestScore = match.getPenalty();
					}
					else if (bestMatch.getPenalty() > match.getPenalty()) {
						bestMatch = match;
						lowestScore = match.getPenalty();
					}
				}
			}

			if (bestMatch != null) {
				replacement = bestMatch.getCopy();

				Mark.say("In ", command);
				command.setObject(replacement);
				Mark.say("Out", command);

				// story.getElements().remove(command);
				// processor.processElement(replacement);
			}

		}

	}

	public static boolean isCompleteCommand(Entity command) {
		if (!command.relationP(Markers.ADD)) {
			return false;
		}
		Entity role = RoleFrames.getRole(Markers.TO_MARKER, command);
		// Mark.say("Role", role);
		if (role != null) {
			return isPlaceFunction(role);
		}
		return false;

	}

	private static boolean isPlaceFunction(Entity entity) {
		return entity != null && NewRuleSet.placePrepositions.stream().anyMatch(s -> entity.functionP(s));
	}

}
