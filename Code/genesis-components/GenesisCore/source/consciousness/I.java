package consciousness;

import connections.Connections;
import connections.signals.BetterSignal;
import constants.*;
import dictionary.BundleGenerator;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.NameGenerator;
import frames.entities.Sequence;
import frames.entities.Thread;
import generator.Generator;
import generator.ISpeak;
import gui.ElaborationView;
import mentalModels.MentalModel;
import utils.Mark;

/*
 * Created on Mar 6, 2015
 * @author phw
 */

public class I extends MentalModel {

	public static final String TEST = "this is a test";

	public static final String COMMENTARY = "introspection";

	public static final String STREAM_OF_CONSCIOUSNESS = "stream of consciousness";

	private MentalModel mentalModel;

	public I(MentalModel model) {
		super(Markers.I, "i.txt");
		// Mark.say("Created I for", model.getName());
		setNameSuffix(NameGenerator.getNewName());
		mentalModel = model;
		Bundle bundle = BundleGenerator.getBundle("person");
		Thread thread = bundle.get(0);
		thread.add("name");
		thread.add("i");
		getBundle().add(thread);
		// May be in testing mode!
		if (mentalModel != null) {
			Connections.wire(I.STREAM_OF_CONSCIOUSNESS, this, ElaborationView.STORY, mentalModel.getInspectionView());
		}
		else {
			Mark.err("No mental model in I constructor");
		}
	}

	/**
	 * For local testing only
	 */
	private I() {
		this(null);
	}

	// Transmission methods

	public void noteThought(Entity x) {
		// Mark.say("Noting thought", x);
		getStoryProcessor().processElement(x);
		Connections.getPorts(this).transmit(STREAM_OF_CONSCIOUSNESS, getStoryProcessor().getStory());
		transmitComposedThought(composeThought(x));
	}

	public void noteThoughtButNotEnglish(Entity x) {
		getStoryProcessor().processElement(x);
		Connections.getPorts(this).transmit(STREAM_OF_CONSCIOUSNESS, getStoryProcessor().getStory());
	}

	// Hack, see caller
	public void notePathElements(Sequence path) {
		String result = "<ul><li>";
		int counter = 0;
		for (Entity e : path.getElements()) {
			if (counter == 0) {
				result += "The path goes from \"" + composeEntity(e);
			}
			else if (counter == path.getElements().size() - 1) {
				result += "\" to \"" + composeEntity(e) + "\".";
			}
			else {
				result += "\" via \"" + composeEntity(e);
			}
			++counter;
		}
		result += "</li></ul>";
		transmitComposedThought(result);
	}

	private String composeThought(Entity x) {
		return "<ul><li>" + composeEntity(x) + "</li></ul>";
	}

	private String composeEntity(Entity x) {
		String result = Generator.getGenerator().generateXPeriod(x);
		if (result.startsWith("(")) {
			result = "&mdash;untranslatable&mdash;";
		}
		return result;
	}

	private void transmitComposedThought(String s) {
		// Mark.say("Transmitting", s);
		Connections.getPorts(getMentalModel())
		        .transmit(MentalModel.COMMENTARY, new BetterSignal(GenesisConstants.RIGHT, Markers.INTROSPECTION_TAB, s));
	}

	//

	public void noteThinkAbout(Entity element) {
		noteThought(ISpeak.askMyself(this, element));
	}

	// Thought constructors

	public void noteQuestionAntecedent(Entity element) {
		noteThought(ISpeak.askMyself(this, element));
	}

	public void noteBelief(String culture, Entity element) {
		noteThought(ISpeak.believe(this, element, ISpeak.haveTrait(this, "asian")));
	}

	public void noteInsertion(Entity element) {
		Entity e = ISpeak.add(this, getStory(), element);
		noteThought(e);
	}

	public void noteFalsePremise(Entity antecedent, Entity consequent) {
		Entity r1 = ISpeak.negate(ISpeak.believe(this, antecedent));
		Entity r2 = ISpeak.negate(ISpeak.believe(this, consequent, antecedent));
		Entity r3 = ISpeak.makeCause(r2, r1);
		noteThought(r3);
	}

	public void noteNoConnection(Entity antecedent, Entity consequent) {
		Entity r1 = ISpeak.negate(ISpeak.thinkLeadsTo(this, consequent, antecedent));
		Entity r2 = ISpeak.negate(ISpeak.believe(this, consequent, antecedent));
		Entity r3 = ISpeak.makeCause(r2, r1);
		noteThought(r3);
	}

	public void noteQuestionCauses(Entity antecedent, Entity consequent) {
		noteThought(ISpeak.askMyself(this, ISpeak.makeCause(consequent, antecedent)));
	}

	public void noteConclusion(Entity consequent, Entity antecedent) {
		noteThought(ISpeak.conclude(this, ISpeak.makeCause(consequent, antecedent)));
	}

	public void noteThink(Entity consequent, Entity antecedent) {
		noteThought(ISpeak.think(this, ISpeak.makeCause(consequent, antecedent)));
	}

	public void noteQuestionLeadsTo(Entity antecedent, Entity consequent) {
		noteThought(ISpeak.askMyself(this, ISpeak.thinkLeadsTo(this, consequent, antecedent)));
	}

	public void noteQuestionAnsweredByLeadsToComplete(Entity antecedent, Entity consequent) {
		Entity question = ISpeak.askMyself(this, ISpeak.makeCause(consequent, antecedent));
		Entity answer = ISpeak.conclude(this, ISpeak.makeCause(consequent, antecedent));
		Entity result = ISpeak.makeCause(answer, question);
		result.addType(Markers.ENTAIL_RULE);
		noteThoughtButNotEnglish(result);
	}

	public void noteQuestionAnsweredByLeadsTo(Entity antecedent, Entity consequent) {
		Entity q = new Entity("question");
		Entity question = ISpeak.makeRoleFrame(this, "ask", q);
		Entity answer = ISpeak.makeRoleFrame(this, "answer", q);
		Entity result = ISpeak.makeCause(answer, question);
		result.addType(Markers.ENTAIL_RULE);
		noteThought(result);
	}

	/**
	 * Temporary hack, want to generate innerese and a sentence, not a list
	 */
	public void notePath(Sequence path) {
		notePathElements(path);
	}

	public void noteLeadsTo(Entity antecedent, Entity consequent) {
		noteThought(ISpeak.thinkLeadsTo(this, consequent, antecedent));

	}

	public void noteStoryElement(Entity antecedent) {
		noteThought(ISpeak.makeRoleFrame(getStory(), "indicate", antecedent));

	}

	private Entity getStory() {
		return getStoryProcessor().getStory();
	}

	public void noteStart(MentalModel m) {
		getStoryProcessor().startStory();

	}

	public void noteTheEnd(MentalModel m) {
		getStoryProcessor().stopStory();
		// getStoryProcessor().getStory().stream().forEachOrdered(e -> Mark.say("Element:", e));

	}

	public static void main(String[] ignore) {
		Mark.say("I = ", new I().toString());
		Mark.say("m = ", new MentalModel("Poo").toString());
		Mark.say("e = ", new Entity().toString());
	}

	public MentalModel getMentalModel() {
		return mentalModel;
	}

}
