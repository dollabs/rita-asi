package matthewFay.Depricated;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import genesis.Genesis;

import matthewFay.Depricated.PersonaServer.PersonaPublisher;
// import matthewFay.StoryAlignment.Depricated.StoryAligner;

import start.Start;
import storyProcessor.StoryProcessor;
import utils.Mark;
import connections.*;
import connections.Connections.NetWireException;

/*
 * Created on Mar 25, 2010
 * @author phw
 */

@SuppressWarnings({ "serial", "deprecation" })
@Deprecated
public class LocalGenesis extends Genesis {

	public static TeachingProcessor teachingProcessor;

	public static TeachingProcessor getTeachingProcessor() {
		if (teachingProcessor == null) {
			teachingProcessor = new TeachingProcessor();
		}
		return teachingProcessor;
	}

	// private static StoryAligner storyAligner;
	// public static StoryAligner getStoryAligner2() {
	// if (storyAligner == null) {
	// storyAligner = new StoryAligner();
	// }
	// return storyAligner;
	// }

	private static PersonaProcessor personaProcessor = null;

	public static PersonaProcessor getPersonaProcessor() {
		if (personaProcessor == null) personaProcessor = new PersonaProcessor();
		return personaProcessor;
	}

	private static LocalGenesis localGenesis = null;

	public static LocalGenesis localGenesis() {
		return localGenesis;
	}

	public static String wireServer = DefaultSettings.WIRE_SERVER;

	public LocalGenesis() {
		super();
		LocalGenesis.localGenesis = this;
		Mark.say("Running Matthew's constructor");

		boolean ONLINE = true;
		if (ONLINE) {
			try {
				System.out.println("Connecting to Persona");

				WiredBox pub = Connections.subscribe("persona", 2);
				// WiredBox chatterBox = Connections.subscribe("the published Chatterbox instance", -1);

				Connections.getPorts(getPersonaProcessor()).addSignalProcessor("processPersonaSignal");

				// Connections.biwire(pub, getPersonaProcessor());
				Connections.wire(pub, getPersonaProcessor());
				Connections.wire(getPersonaProcessor(), pub);

				// Connections.wire(chatterBox, getMyProcessor());
				// Connections.wire(getMyProcessor(), chatterBox);

				System.out.println("Connected to Persona");

			}
			catch (NetWireException e) {
				ONLINE = false;
				e.printStackTrace();
			}
		}
		if (!ONLINE) {
			System.out.println("Connecting to Persona");

			PersonaServer.PersonaPublisher pub = new PersonaServer.PersonaPublisher();
			try {
				FileInputStream fin = new FileInputStream("persona.dat");
				ObjectInputStream ois = new ObjectInputStream(fin);
				pub = (PersonaPublisher) ois.readObject();
				ois.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			Connections.getPorts(pub).addSignalProcessor("process");

			// WiredBox chatterBox = Connections.subscribe("the published Chatterbox instance", -1);

			Connections.getPorts(getPersonaProcessor()).addSignalProcessor("processPersona");

			Connections.wire(pub, getPersonaProcessor());
			Connections.wire(getPersonaProcessor(), pub);

			System.out.println("Connected to Persona");
		}

		Connections.wire(StoryProcessor.COMPLETE_STORY_EVENTS_PORT, getMentalModel1(), TeachingProcessor.STORY_PORT, getTeachingProcessor());
		Connections.wire(StoryProcessor.COMPLETE_STORY_EVENTS_PORT, getMentalModel2(), TeachingProcessor.STORY_PORT2, getTeachingProcessor());

		// Connections.wire(StoryProcessor.COMPLETE_STORY_PORT, getStoryProcessor1(), StoryAligner.STORY_PORT,
		// getStoryAligner2());
		// Connections.wire(StoryProcessor.COMPLETE_STORY_PORT, getStoryProcessor2(), StoryAligner.STORY_PORT2,
		// getStoryAligner2());
		// Connections.wire(StoryProcessor.REMEMBER_STORY_PORT, getStoryProcessor1(), StoryAligner.REMEMBER_STORY,
		// getStoryAligner2());
		// Connections.wire(StoryProcessor.REMEMBER_STORY_PORT, getStoryProcessor2(), StoryAligner.REMEMBER_STORY,
		// getStoryAligner2());
		// Connections.wire(Start.STAGE_DIRECTION_PORT, getStartParser(), StoryAligner.STAGE_DIRECTION,
		// getStoryAligner2());

		// UNUSED //
		// Connections.wire(StoryProcessor.COMPLETE_STORY, getStoryProcessor2(),
		// MyStoryProcessor.STORY_PORT2, getMyProcessor());
		// ///////////

		Connections.biwire(StoryProcessor.CONCEPTS_VIEWER_PORT, getMentalModel1(), TeachingProcessor.CONCEPT_PORT, getTeachingProcessor());
		Connections.biwire(StoryProcessor.CONCEPTS_VIEWER_PORT, getMentalModel2(), TeachingProcessor.CONCEPT_PORT2, getTeachingProcessor());
		Connections.biwire(StoryProcessor.RULE_PORT, getMentalModel1(), TeachingProcessor.RULE_PORT, getTeachingProcessor());
		Connections.biwire(StoryProcessor.RULE_PORT, getMentalModel2(), TeachingProcessor.RULE_PORT2, getTeachingProcessor());

		// Needs fixing //
		Connections.wire(StoryProcessor.PREDICTION_RULES_PORT, getMentalModel1(), TeachingProcessor.USED_RULES1, getTeachingProcessor());
		Connections.wire(StoryProcessor.PREDICTION_RULES_PORT, getMentalModel2(), TeachingProcessor.USED_RULES2, getTeachingProcessor());
		// ////////////////

		Connections.wire(Start.STAGE_DIRECTION_PORT, getStartParser(), TeachingProcessor.STAGE_DIRECTION, getTeachingProcessor());

		Connections.wire("rule", getTeachingProcessor(), StoryProcessor.INJECT_RULE, getMentalModel1());
		Connections.wire("rule", getTeachingProcessor(), StoryProcessor.INJECT_RULE, getMentalModel2());

		// // New Connections for Persona Stuff
		Connections.biwire(StoryProcessor.RULE_PORT, getMentalModel1(), PersonaProcessor.RULE_PORT, getPersonaProcessor());
		Connections.biwire(StoryProcessor.CONCEPTS_VIEWER_PORT, getMentalModel1(), PersonaProcessor.CONCEPT_PORT, getPersonaProcessor());
		Connections.biwire(StoryProcessor.RULE_PORT, getMentalModel2(), PersonaProcessor.RULE_PORT2, getPersonaProcessor());
		Connections.biwire(StoryProcessor.CONCEPTS_VIEWER_PORT, getMentalModel2(), PersonaProcessor.CONCEPT_PORT2, getPersonaProcessor());
		Connections.wire(Start.PERSONA, getStartParser(), PersonaProcessor.IDIOM, getPersonaProcessor());
		Connections.wire(Start.STAGE_DIRECTION_PORT, getStartParser(), PersonaProcessor.STAGE_DIRECTION, getPersonaProcessor());
	}

	public static void main(String[] args) {
		LocalGenesis myGenesis = new LocalGenesis();
		myGenesis.startInFrame();
	}

}
