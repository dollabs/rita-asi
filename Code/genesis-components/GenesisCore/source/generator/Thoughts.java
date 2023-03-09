package generator;

import start.Start;
import utils.Mark;

import constants.Markers;
import dictionary.BundleGenerator;
import frames.entities.Entity;
import frames.entities.Relation;
import mentalModels.MentalModel;

/*
 * Created on Mar 15, 2015
 * @author phw
 */

public class Thoughts extends Rules {

	// public static Relation reason(Entity c, Entity a) {
	// return ISpeak.makeCause(c, a);
	//
	//
	// } public static Relation noteThat(Entity story, Entity consequent) {
	// return ISpeak.makeRoleFrame(story, "indicate", consequent);
	// }

	public static Entity conclude(MentalModel m, Entity consequent) {
		return makeRoleFrame(m, "conclude", consequent);
	}

	public static Entity askMyself(MentalModel m, Entity element) {
		Entity ask = makeRoleFrame(m, "ask", m);
		Relation whether = new Relation(Markers.WHETHER_QUESTION, ask, element);
		// Mark.say("Whether question:", whether);
		// Mark.say("Ask:", ask.toXML());
		return whether;
	}

	public static Relation conclude(MentalModel m, Entity consequent, Entity... entities) {
		return embed(m, "conclude", consequent, entities);
	}

	public static Entity think(MentalModel m, Entity consequent) {
		return makeRoleFrame(m, "think", consequent);
	}

	public static Relation think(MentalModel m, Entity consequent, Entity... entities) {
		return embed(m, "think", consequent, entities);
	}

	public static Entity believe(MentalModel m, Entity consequent) {
		return makeRoleFrame(m, "believe", consequent);
	}

	public static Relation believe(MentalModel m, Entity consequent, Entity... entities) {
		return embed(m, "believe", consequent, entities);
	}

	public static Entity thinkLeadsTo(MentalModel m, Entity consequent, Entity... entities) {
		return leadsTo(m, "think", consequent, entities);
	}

	public static Relation noteThat(MentalModel m, Entity consequent, Entity... entities) {
		return embed(m, "indicate", consequent, entities);
	}

	public static Entity haveTrait(MentalModel m, String trait) {
		Entity result = RoleFrames.makeRoleFrame(m, Markers.PROPERTY_TYPE, Start.makeThing(trait));
		result.addType(Markers.PERSONALITY_TRAIT);
		return result;
	}

	// Auxiliaries

	/**
	 * Note must get bundle from word net so as to match descriptions coming in via English
	 */
	private static Relation embed(MentalModel m, String relation, Entity consequent, Entity... entities) {
		Entity connection = makeRoleFrame(m, relation, consequent);
		Relation cause = makeCause(connection, entities);
		return cause;
	}

	private static Entity leadsTo(MentalModel m, String relation, Entity consequent, Entity... entities) {
		Entity leadsTo = makeLeadsToRule(consequent, entities);
		Entity connection = makeRoleFrame(m, relation, leadsTo);
		return connection;
	}

	public static Entity add(MentalModel m, Entity story, Entity element) {
		Entity result = makeRoleFrame(m, "add", element);
		addRole(result, Markers.TO_MARKER, new Entity(story.getType()));
		return result;
	}

}
