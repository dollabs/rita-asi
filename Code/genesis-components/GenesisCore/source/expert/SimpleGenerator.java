package expert;

import generator.Generator;
import genesis.Quantum;

import java.util.*;

import utils.Punctuator;
import utils.tools.JFactory;
import utils.Mark;

import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;

/*
 * Created on Sep 28, 2008
 * @author phw
 */

public class SimpleGenerator extends AbstractWiredBox {

	public static final String SAY = "say";

	public static final String EXPECTATION = "expectation";

	public static final String IMAGINE = "imagine";

	public static final String LEARNED = "learned";

	public static final String DISAMBIGUATED = "disambiguated";

	public static int MUTE = 0, ACTIVE = 1;

	private static ArrayList<String> features;

	private int mode = ACTIVE;

	private static HashMap<String, String> translations = new HashMap<String, String>();

	private static Generator generator = Generator.getGenerator();

	public SimpleGenerator() {
		super("Simple generator");
		Connections.getPorts(this).addSignalProcessor("processSay");
		Connections.getPorts(this).addSignalProcessor(EXPECTATION, "processExpectation");
		Connections.getPorts(this).addSignalProcessor(SAY, "processSay");
		Connections.getPorts(this).addSignalProcessor(LEARNED, "processLearning");
		Connections.getPorts(this).addSignalProcessor(IMAGINE, "processImagine");
		Connections.getPorts(this).addSignalProcessor(DISAMBIGUATED, "processDisambiguation");
		features = new ArrayList<String>();
		features.add("dead");
		features.add("alive");
	}

	public void processExpectation(Object object) {
		processAux(EXPECTATION, "I expect that", object);
	}

	public void processSay(Object object) {
		processAux(Port.OUTPUT, "", object);
	}

	public void processDisambiguation(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		Vector<String> types = t.getAllTypes();
		int size = types.size();
		if (size > 1) {
			String last = types.get(size - 1);
			String penultimate = types.get(size - 2);
			transmitIfNotMute(Port.OUTPUT, "This kind of " + last + " is a " + penultimate);
		}

	}

	public void processLearning(Object object) {
		Quantum q = (Quantum) object;
		Entity t = q.getThing();
		String prefix = "Yes, I have learned from experience that ";
		if (!q.isTruth()) {
			prefix = "No, I have not learned from experience that ";
		}
		processAux(Port.OUTPUT, prefix, object);
	}

	public void processImagine(Object object) {
		if (object instanceof Entity) {
			Entity t = (Entity) object;
			if (t.functionP(Markers.IMAGINE)) {
				Mark.say("-->", t.getSubject().asString());
				processAux(Port.OUTPUT, "Ok,  I will imagine ", t.getSubject());
				return;
			}
		}
		processAux(Port.OUTPUT, "", object);
	}

	private void processAux(String port, String prefix, Object object) {
		Mark.say("Working in SimpleGenerator on", object);
		String result = "---no comment---";
		if (object instanceof Quantum) {
			Quantum q = (Quantum) object;
			Entity t = q.getThing();
			result = generate(t, prefix.isEmpty());
		}
		else if (object instanceof String) {
			result = (String) object;
		}
		else if (object instanceof Entity) {
			Entity t = (Entity) object;
			result = generate(t, prefix.isEmpty());
		}
		transmitIfNotMute(port, prefix + result);
	}

	private void transmitIfNotMute(String port, String s) {
		String string = compress(s);
		// Connections.getPorts(this).transmit(string);
		// if (mode != MUTE && port != Port.OUTPUT) {
		if (mode != MUTE) {
			Connections.getPorts(this).transmit(port, string);
		}
	}

	private String compress(String s) {
		StringBuffer buffer = new StringBuffer(s);
		int i = -1;
		while ((i = buffer.indexOf("  ")) >= 0) {
			buffer.delete(i, i + 1);
		}
		return buffer.toString();
	}

	public static String translate(Entity t) {
		return translateAux(t);
	}

	public static String generate(Entity t) {
		return generate(t, true);
	}

	public static String generate(Entity t, boolean matrix) {
		// Mark.say("Trying to translate", t.asString());
		String translation = "No generation";
		try {
			translation = translations.get(t.hashIncludingThings());
			if (translation == null) {
				if (matrix) {
					translation = generator.generateXPeriod(t, Markers.PRESENT);
				}
				else {
					translation = generator.generateXPeriod(t, Markers.PRESENT);
				}

				if (translation != null && !"nil".equalsIgnoreCase(translation)) {
					// Mark.say("Placing hash for", t.hashIncludingThings(), translations.size());
					translations.put(t.hashIncludingThings(), translation);
				}
			}
			if (translation == null || "nil".equalsIgnoreCase(translation)) {
				translation = "|| " + translate(t);
			}
		}
		catch (Exception e) {
			Mark.say("Unable to translate", t.asString());
			return translate(t);
		}
		return translation;
	}

	private static String translateAux(Entity t) {

		if (t.isAPrimed("because") && t.relationP()) {
			Relation r = (Relation) t;
			Entity s = r.getSubject();
			Entity o = r.getObject();
			return translateAux(s) + " because " + translateAux(o);
		}
		else if (t.isAPrimed("cause") && t.relationP()) {
			Relation r = (Relation) t;
			Entity s = r.getSubject();
			Entity o = r.getObject();
			return translateAux(o) + " because " + translateAux(s);
		}
		else if (t.isAPrimed("doesNotLeadTo") && t.relationP()) {
			Relation r = (Relation) t;
			Entity s = r.getSubject();
			Entity o = r.getObject();
			return translateAux(s) + " does not necessarily mean that " + translateAux(o);
		}
		else if (t.isAPrimed(Markers.MENTAL_STATE_MARKER) && t.relationP()) {
			return translateMentalState(t);
		}
		// else if (t.isAPrimed("transfer")) {
		// return translateTransfer(t);
		// }
		else if (t.isAPrimed("trajectory")) {
			return translateTrajectory(t);
		}
		else if (t.isAPrimed("transition")) {
			return translateTransition(t);
		}
		else if (t.isAPrimed(Markers.ACTION_MARKER)) {
			return translateRawAction(t);
		}
		else if (t.isAPrimed("contact")) {
			return translateContact(t);
		}
		else if (t.isAPrimed(Markers.CLASSIFICATION_MARKER)) {
			return translateClassification(t);
		}
		else if (t.isAPrimed(Markers.CONJUNCTION) || t.isAPrimed(Markers.SEMANTIC_INTERPRETATION) || t.isA("eventSpace") || t.isA("transitionLadder")
		        || t.isA("trajectoryLadder") || t.isA("eventLadder")) {
			ArrayList<String> result = new ArrayList<String>();
			for (Entity e : ((Sequence) t).getElements()) {
				result.add(translateAux(e));
			}
			return Punctuator.punctuateAnd(result);
		}
		else if (t.isAPrimed(Markers.ROLE_MARKER) && t.relationP()) {
			return translateBag((Relation) t);
		}
		else if (t.isAPrimed(Markers.MENTAL_STATE)) {
			return t.getType();
		}
		else if (t.isAPrimed(Markers.ACTION_WORD) && t.relationP()) {
			Relation relation = (Relation) t;
			return (translateAux(relation.getSubject()) + " " + relation.getType() + " " + translateAux(relation.getObject()));
		}
		else if (t.isAPrimed(Markers.SOCIAL_MARKER) && t.relationP()) {
			Relation relation = (Relation) t;
			return (translateAux(relation.getSubject()) + " " + relation.getType() + " " + translateAux(relation.getObject()));
		}
		else if (t.relationP()) {
			Relation r = (Relation) t;
			String result = translateAux(r.getSubject());
			String type = t.getType();
			if (type.equals(Markers.PROPERTY_TYPE) && r.hasFeature(Markers.NOT)) {
				result += " is not " + r.getObject().getType();
				return result;
			}
			else if (type.equals(Markers.PROPERTY_TYPE)) {
				result += " is " + r.getObject().getType();
				;
				return result;
			}
			else if (type.equals(Markers.IMAGINE) && r.getSubject().isAPrimed("you")) {
				translateAux(r.getObject());
			}
			else {
				result += " " + type + " ";
			}
			result += translateAux(r.getObject());
			return result;
		}
		else if (t.functionP()) {
			Function d = (Function) t;
			return " " + d.getType() + " " + translateAux(d.getSubject());
		}
		else if (t.sequenceP()) {
			String result = "";
			String first = null;
			for (Entity e : ((Sequence) t).getElements()) {
				if (first == null) {
					first = e.getType();
				}
				result += " " + e.getType();
			}
			if (!t.getType().equals(first)) {
				result = t.getType() + result;
			}
			return result;
		}
		else {
			String result = "";
			if (t.isAPrimed("null")) {
				return "";
				// Do not report null
			}
			else if (t.entityP() && features != null && features.contains(t.getType())) {
			}
			else if (t.getType().equals("contact")) {
			}
			else if (t.getType().equals("false")) {
			}
			else if (t.getType().equals("true")) {
			}
			else if (!t.isA(Markers.NAME) && t.entityP() && "aeiou".indexOf(t.getType().charAt(0)) >= 0) {
				result += " an ";
			}
			else if (!t.isA(Markers.NAME) && t.entityP()) {
				result = " a ";
			}
			else if (t.isA(Markers.NAME) && t.entityP()) {
				String s = t.getType();
				if (s.length() > 0) {
					return s.substring(0, 1).toUpperCase() + s.substring(1);
				}
			}
			return result += t.getType();
		}
	}

	private static String translateBag(Relation t) {
		String s = translateAux(t.getSubject());
		for (Entity e : ((Sequence) t.getObject()).getElements()) {
			s += translateAux(e);
		}
		return s;
	}

	private static String translateMentalState(Entity t) {
		Relation r = (Relation) t;
		String result = translateAux(r.getSubject());
		if (r.getObject().isAPrimed(Markers.POSITIVE)) {
			result += "'s state is +";
		}
		else if (r.getObject().isAPrimed(Markers.NEGATIVE)) {
			result += "'s state is -";
		}
		return result;
	}

	private static String translateTransfer(Entity t) {
		Relation give = (Relation) t;
		Entity subject = give.getSubject();
		Relation move = (Relation) give.getObject();
		Entity mover = move.getSubject();
		Sequence path = (Sequence) move.getObject();
		String result = translateAux(subject);
		result += " " + t.getType() + " ";
		result += translateAux(mover);
		for (Entity element : path.getElements()) {
			if (element.isA("to")) {
				result += translatePathElement(element);
				break;
			}

		}
		return result;
	}

	private static String translateContact(Entity t) {
		String result = " contact between ";
		if (!t.relationP()) {
			return " contact between things ";
		}
		Relation contact = (Relation) t;
		result += translateAux(contact.getSubject());
		result += " and ";
		result += translateAux(contact.getObject());
		return result;
	}

	private static String translateRawAction(Entity t) {
		if (!t.relationP()) {
			return " contact between things ";
		}
		String result = "";
		Relation contact = (Relation) t;
		result += translateAux(contact.getSubject());
		if (t.hasFeature(Markers.NOT)) {
			result += " not " + t.getType() + " ";
		}
		else {
			result += " " + t.getType() + " ";
		}
		result += translateAux(contact.getObject());
		return result;
	}

	private static String translateClassification(Entity t) {
		String result = "";
		if (!t.relationP()) {
			return " something is screwed up ";
		}
		Relation classification = (Relation) t;
		result += translateAux(classification.getObject());
		result += " is ";
		// result += translate(classification.getSubject());
		if (classification.getSubject().entityP()) {
			result += classification.getSubject().getType();
		}
		else {
			result += translateAux(classification.getSubject());
		}
		return result;
	}

	private static String translateTransition(Entity t) {
		String result = "";
		if (t.functionP()) {
			Function d = (Function) t;
			result += translateAux(d.getSubject());
		}
		else {
			return " something changed ";
		}
		// if (t.isAPrimed("appear")) {
		// return result;
		// }
		// else\
		if (t.functionP() && ((Function) t).getSubject().isAPrimed(Markers.MENTAL_STATE_MARKER)) {
		}
		else if (t.isAPrimed("appear")) {
			if (t.functionP() && ((Function) t).getSubject().isAPrimed("contact")) {
				result += " appeared ";
			}
			else {
				result += " appear ";
			}
		}
		else if (t.isAPrimed("disappear")) {
			result += " disappeared ";
		}
		else if (t.isAPrimed("increase")) {
			result += " increased ";
		}
		else if (t.isAPrimed("decrease")) {
			result += " decreased ";
		}
		else {
			result += " changed ";
		}
		return result;
	}

	private static String translateTrajectory(Entity t) {
		String result = "";
		if (!t.relationP()) {
			return " something moved ";
		}
		Relation trajectory = (Relation) t;
		result += translateAux(trajectory.getSubject());
		Entity path = trajectory.getObject();
		if (path.sequenceP()) {
			result += " " + trajectory.getType() + " ";
		}
		else {
			result += " moved ";
		}
		if (path != null) {
			for (Object o : path.getElements()) {
				result += translatePathElement(o);
			}
		}
		return result;
	}

	private static String translatePathElement(Object o) {
		if (!(o instanceof Entity)) {
			System.err.println("Wrong argument type handed to Generator.translatePathElement");
		}
		Entity t = (Entity) o;
		if (!t.functionP()) {
			return " somewhere ";
		}
		Function d = (Function) t;
		return " " + d.getType() + translatePlaceElement(d.getSubject());
	}

	private static String translatePlaceElement(Entity t) {
		if (!t.functionP()) {
			return " someplace ";
		}
		Function d = (Function) t;
		String prep = d.getType();
		if (prep.equals("at")) {
			return translateAux(d.getSubject());
		}
		return " " + d.getType() + translateAux(d.getSubject());
	}

	public static void main(String[] ignore) {
		Entity t = new Entity("man");
		Function transition = new Function("transition", new Entity("contact"));
		transition.addType("appear");

		Sequence path = JFactory.createPath();

		Function house = new Function("at", new Entity("house"));
		Function into = new Function("to", house);
		path.addElement(into);

		Relation trajectory = new Relation("trajectory", t, path);
		trajectory.addType("ran");

		Relation transfer = new Relation("transfer", new Entity("dog"), trajectory);
		transfer.addType("give");

		Relation because = new Relation(Markers.CAUSE_MARKER, transfer, transition);

		Relation classification = new Relation("kill", new Entity("macduff"), new Entity("macbeth"));

		Relation x = new Relation("because", classification, classification);

		// Mark.say("x", new SimpleGenerator().translate(x));

		Mark.say("Translation", classification.asString(), new SimpleGenerator().generate(classification));

		// System.out.println(new
		// SimpleGenerator().translateAux(classification));

		Entity macbeth = new Entity("name");
		macbeth.addType("macbeth");
		System.out.println("macbeth translates to: " + new SimpleGenerator().translateAux(macbeth));
	}

	public void setMode(int mode) {
		this.mode = mode;
	}
}
