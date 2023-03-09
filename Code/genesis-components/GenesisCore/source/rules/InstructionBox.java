package rules;

import javax.swing.JOptionPane;

import connections.*;
import connections.signals.BetterSignal;
import constants.*;
import frames.entities.Entity;
import frames.entities.Function;
import generator.Generator;
import generator.RoleFrames;
import storyProcessor.StoryProcessor;
import utils.Mark;

/*
 * Created on Apr 16, 2017
 * @author phw
 */

public class InstructionBox extends AbstractWiredBox {

	public InstructionBox() {
		Connections.getPorts(this).addSignalProcessor(this::processElement);
	}

	public void processElement(Object o) {
		boolean debug = false;
		// Here, could instead have user interaction.
		BetterSignal signal = (BetterSignal) o;
		Entity element = signal.get(0, Entity.class);
		StoryProcessor storyProcessor = signal.get(1, StoryProcessor.class);
		

		Mark.say(debug, "Better look for", element.toString());

		Entity instruction = RoleFrames.makeRoleFrame("you", "check");
		RoleFrames.addRole(instruction, Markers.FOR_MARKER, element);
		String english = Generator.getGenerator().generate(instruction);

		Entity popupInstruction = new Function("question", element);
		popupInstruction.addType("did");

		Mark.say(debug, "English command is", popupInstruction);

		String s = Generator.getGenerator().generate(popupInstruction);


		int reply = JOptionPane.showConfirmDialog(null, s, "Instruction", JOptionPane.YES_NO_OPTION);

		if (reply == JOptionPane.YES_OPTION) {
			Mark.say(debug, "At this point, insert confirmed element into story.");
			storyProcessor.processElement(element);

		}
		else {
			Mark.say(debug, "No luck, couldn't affirm element.");
		}

	}

}
