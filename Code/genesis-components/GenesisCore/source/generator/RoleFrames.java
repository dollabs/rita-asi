package generator;

import java.util.*;


import constants.Markers;
import dictionary.BundleGenerator;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import start.Start;
import translator.Translator;
import utils.Mark;
import utils.Z;

/*
 * Created on Mar 14, 2015
 * @author phw
 */

public class RoleFrames extends Grunts {

	/**
	 * Takes two or more arguments.
	 * <p>
	 * If number of arguments is odd, third argument is the role-frame's object, and the rest are alternating role names
	 * (strings) and role fillers, either strings or entities.
	 * <p>
	 * If number of arguments is even, there is no object, just role names and role fillers.
	 * <p>
	 * If subject or a role filler is given as a string with +n at the end, then the created entity is stored in a hash
	 * table so that subsequent appearances are translated into the same object.
	 */
	public static Entity makeRoleFrame(Object s, String v, Object... args) {
		
		Entity subject = getEntity(s);
		
		Bundle bundle = BundleGenerator.getBundle(v);
		
		Relation relation = makeRelation(v, subject, makeRoles());
		int start = 0;
		if (args.length % 2 == 1) {
			// Odd number of args, must have an object in the first position
			Entity object = getEntity(args[0]);
			// Add object role
			addRole(relation, Markers.OBJECT_MARKER, object);
			// Skip object in next iteration
			start = 1;
		}
		for (int i = start; i < args.length; i = i + 2) {
			// Mark.say("y");
			addRole(relation, (String) args[i], getEntity(args[i + 1]));
		}
		
//		for (Entity ent : Translator.getTranslator().translate(Generator.getGenerator().generate(relation)).getElements()) {
//			Mark.yellow(ent);
//			ent.details();
//		}
		return relation;
	}
	
	/**
	 * A convenience method; use to start over
	 */
	public static void clearCache() {
		Entity.clearCache();
	}

	/**
	 * Another convenience method
	 */
	public static void addFeature(Entity x, Object feature) {
		x.addFeature(feature);
	}

	private static Entity getEntity(Object x) {
		if (x instanceof String) {
			return Entity.getClassifiedThing((String) x);
		}
		return (Entity) x;
	}

	// public static Entity makeRoleFrame(Object subject, String verb) {
	// return makeRoleFrame(Start.makeThing((String) subject), verb);
	// }
	//
	// public static Entity makeRoleFrame(Object subject, String verb, Object object) {
	// Entity s;
	// if (subject instanceof String) {
	// s = Start.makeThing((String) subject);
	// }
	// else {
	// s = (Entity) subject;
	// }
	// Entity o;
	// if (object instanceof String) {
	// o = makeEntity((String) object);
	// }
	// else {
	// o = (Entity) object;
	// }http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=1963651
	// return makeRoleFrame(s, verb, o);
	// }

	public static Entity addRole(Entity frame, String preposition, Object role) {
		Entity o;
		if (role instanceof String) {
			o = Entity.getClassifiedThing((String) role);
		}
		else {
			o = (Entity) role;
		}
		addRole(frame, preposition, o);
		return frame;
	}

	// public static Entity makeRoleFrame(Entity subject, String verb) {
	// Bundle bundle = BundleGenerator.getBundle(verb);
	// Relation relation = makeRelation(verb, subject, makeRoles());
	// if (!bundle.isEmpty()) {http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=1963651
	// relation.setBundle(bundle);
	// }
	// return relation;
	// }
	//
	// public static Entity makeRoleFrame(Entity subject, String verb, Entity object) {
	// Entity frame = makeRoleFrame(subject, verb);
	// addRole(frame, Markers.OBJECT_MARKER, object);
	// return frame;
	// }


	public static Entity addRole(Entity frame, String preposition, Entity role) {
		frame.getObject().addElement(new Function(preposition, role));
		return frame;
	}

	public static Entity removeRole(Entity frame, String preposition) {
		Entity remove = null;
		if (isRoleFrame(frame)) {
			for (Entity e : frame.getObject().getElements()) {
				if (e.isA(preposition)) {
					remove = e;
					break;
				}
			}

			frame.getObject().getElements().remove(remove);

		}
		return frame;
	}

	/**
	 * Get subject role
	 */
	public static Entity getSubject(Entity t) {
		return t.getSubject();
	}

	/**
	 * Get object role.
	 */
	public static Entity getObject(Entity t) {
		return getRole(Markers.OBJECT_MARKER, t);
	}

	/**
	 * Get any role.
	 */
	public static Entity getRole(String marker, Entity t) {
		if (t.relationP() && t.getObject().sequenceP()) {
			for (Entity role : t.getObject().getElements()) {
				if (role.functionP(marker)) {
					return role.getSubject();
				}
			}
		}
		return null;
	}

	/**
	 * Check for role
	 */
	public static boolean hasRole(String marker, Entity t) {
		if (t.relationP() && t.getObject().sequenceP()) {
			for (Entity role : t.getObject().getElements()) {
				if (role.functionP(marker)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check for role
	 */
	public static boolean hasRole(String marker, String filler, Entity t) {
		if (t.relationP() && t.getObject().sequenceP()) {
			for (Entity role : t.getObject().getElements()) {
				if (role.functionP(marker)) {
					if (role.getSubject().isA(filler)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Get all entities marked with given role
	 */
	public static List<Entity> getRoles(String marker, Entity t) {
		List<Entity> result = new ArrayList<>();
		if (t.relationP() && t.getObject().sequenceP()) {
			for (Entity role : t.getObject().getElements()) {
				if (role.functionP(marker)) {
					result.add(role.getSubject());
				}
			}
		}
		return result;
	}

	/**
	 * Get all roles
	 */
	public static List<Entity> getRoles(Entity t) {
		List<Entity> result = new ArrayList<>();
		if (t.relationP() && t.getObject().sequenceP()) {
			for (Entity role : t.getObject().getElements()) {
				result.add(role);
			}
		}
		return result;
	}

	public static Entity getSlot(String marker, Entity t) {
		if (t.relationP() && t.getObject().sequenceP()) {
			for (Entity role : t.getObject().getElements()) {
				if (role.functionP(marker)) {
					return role;
				}
			}
		}
		return null;
	}

	// Auxiliary

	private static Entity makeRoles() {
		Sequence roles = new Sequence(Markers.ROLE_MARKER);
		return roles;
	}

	public static boolean isRoleFrame(Entity x) {
		if (// x.relationP() &&
		x.getObject() != null && x.getObject().sequenceP(Markers.ROLE_MARKER)) {
			return true;
		}
		return false;
	}

	public static Entity makeIntoHowQuestion(Entity x) {
		return new Function("how", x);
	}

	public static Entity makeIntoAdviceQuestion(Entity x) {
		Entity advice = RoleFrames.makeRoleFrame(Markers.i, "need", "advice");
		Entity question = new Function(Markers.QUESTION_MARKER, x);
		question.addType(Markers.HOW_QUESTION);
		Entity result = RoleFrames.addRole(advice, "on", question);
		Mark.say("Here it is:", result);
		return result;
	}

	/**
	 * Doesn't work
	 */
	// public static Entity makeIntoAskQuestion(Entity x) {
	// return new Function("ask", x);
	// }

	public static void main(String[] ignore) {
		// Entity x = makeRoleFrame("John", "kiss", "Mary");
		// Entity y = Translator.getTranslator().translateToEntity("Paul kissed Susan");
		//
		// Mark.say("Result", isRoleFrame(x), x);
		// Mark.say("Result", isRoleFrame(y), y);
		//
		// Entity z = Translator.getTranslator().translateToEntity("A bird flew to a tree");
		// Mark.say("Result", z.asStringSansIndexes());
		//
		// z = Translator.getTranslator().translateToEntity("If xx kills yy, then yy becomes dead.");
		// Mark.say("Result", z.asStringSansIndexes());

		// Entity x = makeRoleFrame("John+4", "kiss", "Mary", "in", "afternoon");
		//
		// Mark.say("Role-frame result", x.asStringWithIndexes());
		//
		// Entity y =Entity.getClassifiedThing("John+4");
		// y.addFeature("tall");
		//
		// Mark.say("Feature result", x.toEnglish());
		//
		// y = makeRoleFrame("House+4", "is", "by", "tree");
		//
		// Mark.say(y);
		//
		// Mark.say(y.toEnglish());

		// Entity y = makeRoleFrame("House+4", "is", "by", "tree+2", "in", "afternoon+7");
		//
		// Mark.say(y);
		//
		// Mark.say(y.toEnglish());
		//
		// y = makeRoleFrame("House+4", "appears", "by", "tree+2", "in", "afternoon+7");
		//
		// Mark.say(y);
		//
		// Mark.say(y.toEnglish());

		 Entity test = makeRoleFrame("window", "is", "in", "room");
		 test = makeRoleFrame("window", "appears", "at", "room");

//		Entity test = makeRoleFrame(Start.makeThing(Markers.YOU), "follow");
//		Mark.say(test.toXML());
		Mark.say(test);

	}

}
