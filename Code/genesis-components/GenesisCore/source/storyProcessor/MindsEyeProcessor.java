package storyProcessor;

import java.util.HashMap;

import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;
import generator.Generator;
import matchers.StandardMatcher;
import translator.*;
import utils.*;
import utils.minilisp.LList;

/*
 * Created on Jan 11, 20
 * @author phw
 */

public class MindsEyeProcessor extends AbstractWiredBox {

	boolean debug = false;

	public static final String COMMENTARY_PORT = "commentary port";

	public static final String LANGUAGE_PORT = "language port";

	public static final String VISION_PORT = "vision port";

	public static final String FRAME_NUMBER_PORT = "frame number port";

	public static final String SYNCHRONIZE_FRAME_NUMBER_PORT = "synchronize frame number port";

	public static final String QUESTION_ANSWER_PORT = "question answer port";

	public static final String REMARK_PORT = "remark port";

	public static final String VIDEO_NAME_PORT = "video name port";

	public static String wireServer = DefaultSettings.WIRE_SERVER;

	public static String server = "co57_test";

	public String currentVideo = "None selected yet!";

	private HashMap<String, String> abbreviations = new HashMap<String, String>();

	private HashMap<String, String> objects = new HashMap<String, String>();

	private static Entity p0;

	private static Entity p1;

	private static Entity p2;

	private static Entity p3;

	private static Entity p4;

	// public static String server = "Co57Stub";

	RPCBox box;

	WiredBox proxy;

	public MindsEyeProcessor() {
		super("Story processor");
		try {

			Translator basicTranslator = Translator.getTranslator();
			basicTranslator.translate("xx is an object");
			basicTranslator.translate("yy is an object");
			basicTranslator.translate("ff is an integer");

			p0 = basicTranslator.translate("Advance video to frame 0").getElement(2);
			p1 = basicTranslator.translate("Is xx approaching yy?").getElement(0);
			p2 = basicTranslator.translate("Is xx larger than yy?").getElement(0);
			p3 = basicTranslator.translate("Is xx bigger than yy?").getElement(0);

			p4 = basicTranslator.translate("Look at video named zz.").getElement(0);

			Mark.say(debug, "po:", p0.asString());
			Mark.say(debug, "p1:", p1.asString());
			Mark.say(debug, "p2:", p2.asString());
			Mark.say(debug, "p3:", p3.asString());
			Mark.say(debug, "p4:", p4.asString());

			Connections.getPorts(this).addSignalProcessor(LANGUAGE_PORT, "processLanguageInput");
			Connections.getPorts(this).addSignalProcessor(VISION_PORT, "processVisionInput");
			Connections.getPorts(this).addSignalProcessor(SYNCHRONIZE_FRAME_NUMBER_PORT, "synchronize");
			Connections.getPorts(this).addSignalProcessor(COMMENTARY_PORT, "comment");
			establishNetworkConnection();
			createAbbreviationMap();
		}
		catch (Exception e) {
			Mark.err("Blew out in MindsEyeProcessor constructor");
			e.printStackTrace();
		}
	}

	public void comment(Object o) throws Exception {
		if (o instanceof Integer) {
			String sentence = "Is the second person approaching the first person";
			Translator basicTranslator = Translator.getTranslator();
			Sequence s = (Sequence) (basicTranslator.translate(sentence));
			Mark.say(debug, o.toString(), s.asString());
			for (Entity t : s.getElements()) {
				processLanguageInput(t);
			}
		}
	}

	private void createAbbreviationMap() {
		abbreviations.put("macbeth", "hamlet");
		abbreviations
		        .put("approach", "MULTIPLE_VERB_02/Approach4_Leave6_Exchange1_A1_C2_Act1_2_Downtown4_MC_EVEN_4761ba00-c5af-11df-ae7f-e80688cb869a.mov");

	}

	private String getAbbreviation(String name) {
		String result = abbreviations.get(name);
		if (result == null) {
			return name;
		}
		return result;
	}

	private void establishNetworkConnection() {
		try {
			// Connections.useWireServer(wireServer);
			proxy = Connections.subscribe(server, 5);

			System.out.println("Done waiting.");
			box = (RPCBox) proxy;
			if (box != null) {
				Mark.say(debug, "Connected to the vision system");
			}
			else {
				Mark.say(debug, "Did not connect to the vision system");
			}
		}
		catch (Exception e) {
			Mark.err("Could not connect to a vision system");
			// e.printStackTrace();
		}
	}

	public void processLanguageInput(Object object) {
		if (object instanceof Entity) {
			Entity t = (Entity) object;
			Mark.say("Mind's eye language port received", t.asString());
			// Commands return true if matched; action on match is continuation
			if (false) {

			}
			else if (testAdvanceCommand(p0, t, "look_at")) {
			}
			else if (testUnaryCommand(p4, t, "choose")) {
			}
			// else if (testUnaryCommand(t,
			// "(r call (t person name m) (r is (t video name l) (t name x)))",
			// "look_at")) {
			// }
			// else if (testFetch(t,
			// "(r about (r tell (t thing name you) (t i)) (t objects))",
			// "get_all_markers")) {
			// }
			else if (testBinaryPredicate(p1, t, "is_moving_toward")) {
			}
			// else if (testBinaryPredicate(t,
			// "(d whether (r move (t object name x) (s path (d toward (d at (t object name y))))))",
			// "is_moving_toward")) {
			// }
			else if (testBinaryPredicate(p2, t, "is_bigger") || testBinaryPredicate(p3, t, "is_bigger")) {
			}
			// else if (testBinaryPredicate(t,
			// "(d whether (r bigger (t object name x) (t object name y)))",
			// "is_bigger")) {
			// }
			// else if (testUnaryPredicate(t,
			// "(d whether (r is (t object name x) (t person)))", "is_person"))
			// {
			// }
			// else {
			// // Mark.say("Mind's eye language port unresponsive");
			// }
		}
	}

	public void synchronize(Object o) {
		issueVoidCommand("look_at", currentVideo, o);
	}

	/*
	 * Advance "The approach video" to frame 80.
	 */
	private boolean testAdvanceCommand(Entity pattern, Entity t, String predicate) {
		// Mark.say("Pattern:", pattern.asString());
		// Mark.say("Datum:  ", t.asString());
		LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(pattern, t);
		// Mark.say("Bindings:", bindings);
		if (bindings != null) {
			String r2 = resolve2("0", bindings);
			int i2 = Integer.parseInt(r2);
			// issueVoidCommand(predicate, r1, i2);
			// Mark.say("Transmitting", i2);
			Mark.say(debug, "Command", predicate);
			Connections.getPorts(this).transmit(FRAME_NUMBER_PORT, i2);
			Connections.getPorts(this).transmit(REMARK_PORT, "Ok.");
			return true;
		}
		return false;
	}

	/*
	 * Look at video named "foo".
	 */
	private boolean testUnaryCommand(Entity pattern, Entity t, String predicate) {
		// Mark.say("Pattern:", pattern.asString());
		// Mark.say("Datum:  ", t.asString());
		LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(pattern, t);
		Mark.say("Bindings:", bindings);
		if (bindings != null) {
			String r1 = resolve2("zz", bindings);
			Mark.say("!!!", predicate, r1);
			issueVoidCommand(predicate, r1);
			Mark.say(debug, "Command", predicate);
			Connections.getPorts(this).transmit(VIDEO_NAME_PORT, r1);
			Connections.getPorts(this).transmit(FRAME_NUMBER_PORT, 0);
			Connections.getPorts(this).transmit(REMARK_PORT, "Ok.");
			return true;
		}
		return false;
	}

	private String checkForAntecedant(String name) {
		if ("video".equals(name)) {
			return currentVideo;
		}
		else {
			String fullName = getAbbreviation(name);
			currentVideo = fullName;
			return fullName;
		}
	}

	// /*
	// * Look at video called "bar".
	// */
	// private boolean testUnaryCommand(Thing t, String x, String predicate) {
	// Thing pattern = Thing.reader(x);
	// LList<PairOfEntities> bindings =
	// BasicMatcher.getBasicMatcher().specialMatch(pattern, t);
	// if (bindings != null) {
	// String r1 = resolve2("x", bindings);
	// r1 = checkForAntecedant(r1);
	// issueVoidCommand(predicate, r1, 0);
	// Mark.say(debug, "Command", predicate);
	// Connections.getPorts(this).transmit(FRAME_NUMBER_PORT, 0);
	// Connections.getPorts(this).transmit(REMARK_PORT, "Ok.");
	// return true;
	// }
	// return false;
	// }
	//
	// private boolean testFetch(Thing t, String x, String command) {
	// Thing pattern = Thing.reader(x);
	// LList<PairOfEntities> bindings =
	// BasicMatcher.getBasicMatcher().specialMatch(pattern, t);
	// if (bindings != null) {
	// Mark.say(debug, "Command", command);
	// issueFetchCommand(command);
	// return true;
	// }
	// return false;
	// }

	// private boolean testUnaryPredicate(Thing t, String x, String predicate) {
	// Thing pattern = Thing.reader(x);
	// LList<PairOfEntities> bindings =
	// BasicMatcher.getBasicMatcher().specialMatch(pattern, t);
	// if (bindings != null) {
	// // Before proceeding, get on same page with Co57
	// Connections.getPorts(this).transmit(SYNCHRONIZE_FRAME_NUMBER_PORT, null);
	// issueFetchCommand("get_all_markers");
	// String r1 = resolve("x", bindings);
	// if (r1 != null) {
	// Mark.say(debug, "Command", predicate);
	// issueValueCommand(t.getSubject(), predicate, r1);
	// return true;
	// }
	// }
	// return false;
	// }

	private boolean testBinaryPredicate(Entity pattern, Entity t, String predicate) {

		LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(pattern, t);

		Mark.say(debug, "Pattern     :", pattern.asString());
		Mark.say(debug, "Input       :", t.asString());
		Mark.say(debug, "Bindings are:", bindings);

		if (bindings != null) {
			// Before proceeding, get on same page with Co57
			Connections.getPorts(this).transmit(SYNCHRONIZE_FRAME_NUMBER_PORT, null);
			issueFetchCommand("get_all_markers");
			String r1 = resolve("xx", bindings);
			String r2 = resolve("yy", bindings);
			Mark.say(debug, "Rs", r1, r2);
			if (r1 != null && r2 != null) {
				Mark.say(debug, "Command", predicate);
				issueValueCommand(t.getSubject(), predicate, r1, r2);
				return true;

			}
		}
		return false;
	}

	private void issueVoidCommand(String command, Object... array) {
		if (box != null) {
			for (Object o : array) {
				Mark.say(debug, "Arg:", o);
			}
			box.rpc(command, array);
			Mark.say(debug, "Void command", command, "apparently succeeded");
		}
		else {
			Mark.err("1 Unable to issue command because not connected to vision system.");
			Connections.getPorts(this).transmit(QUESTION_ANSWER_PORT, "No connection so " + command + "-->" + "don't know");
		}
	}

	private void issueValueCommand(Entity t, String command, Object... array) {
		String args = "";
		for (Object x : array) {
			args += " " + x.toString();
		}
		if (box != null) {
			try {
				Object result = box.rpc(command, array);
				if (result instanceof Object[]) {
					String values = "";
					Mark.say(debug, "Value command", command + args, "apparently succeeded");
					for (Object o : (Object[]) result) {
						Mark.say(debug, "Result returned is", o.toString());
						values += o + " ";
					}
					Connections.getPorts(this).transmit(QUESTION_ANSWER_PORT, command + args + "-->" + values.trim());
				}
				else {
					Mark.say(debug, "Value command", command + args, "apparently succeeded");
					Mark.say(debug, "Result returned is", result.toString());
					if ("false".equals(result.toString())) {
						t.addFeature(Markers.NOT);
					}
					else {
						Mark.say(debug, "Trying to say", t.asString());
						// Connections.getPorts(this).transmit(QUESTION_ANSWER_PORT,
						// Generator.getGenerator().comment(t));
						Connections.getPorts(this).transmit(QUESTION_ANSWER_PORT, Generator.getGenerator().generate(t));
					}
					// Connections.getPorts(this).transmit(QUESTION_ANSWER_PORT,
					// command + " " + args + " " + result.toString());
					// Mark.say(debug, "Generating from", t.asString());
					// Connections.getPorts(this).transmit(QUESTION_ANSWER_PORT,
					// Generator.getGenerator().comment(t));
				}
			}
			catch (Exception e) {
				Mark.say(debug, "Value command", command + args, "apparently failed");
				Mark.say(debug, "is_bigger: " + box.rpc("is_bigger", new Object[] { "hello", "there" }));
			}
		}
		else {
			Mark.err("2 Unable to issue command because not connected to vision system.");
			Connections.getPorts(this).transmit(QUESTION_ANSWER_PORT, "No connection so " + command + args + "-->" + "don't know");
		}
	}

	private void issueFetchCommand(String command) {
		String args = "";
		if (box != null) {
			try {
				Object[] input = new Object[] {};
				Object result = box.rpc(command, input);
				if (result instanceof Object[]) {
					String values = "";
					Mark.say(debug, "Value command", command + args, "apparently succeeded yielding", ((Object[]) result).length, "object(s)");
					Object[] objects = ((Object[]) result);
					if (objects.length == 1) {
						// Connections.getPorts(this).transmit(REMARK_PORT,
						// "Ok.  There is one");
					}
					else {
						// Connections.getPorts(this).transmit(REMARK_PORT,
						// "Ok.  There are " + objects.length);
					}
					for (int i = 0; i < objects.length; ++i) {
						Object o = objects[i];
						putObject(Integer.toString(i), o.toString());
						Mark.say(debug, "Result returned is", o.toString());
						values += o + " ";
					}
					// Connections.getPorts(this).transmit(QUESTION_ANSWER_PORT,
					// command + " " + values.trim());
				}
				else {
					Mark.say(debug, "Value command", command + args, "apparently succeeded yielding object");
					Mark.say(debug, "Result returned is", result.toString());
				}
			}
			catch (Exception e) {
				Mark.say(debug, "Value command", command + args, "apparently failed");
				Mark.say(debug, "is_bigger: " + box.rpc("is_bigger", new Object[] { "hello", "there" }));
			}
		}
		else {
			Mark.err("3 Unable to issue command because not connected to vision system.");
			Connections.getPorts(this).transmit(QUESTION_ANSWER_PORT, "No connection so " + command + args + "-->" + "don't know");
		}
	}

	private String resolve2(String s, LList<PairOfEntities> bindings) {
		for (Object object : bindings) {
			PairOfEntities pair = (PairOfEntities) object;
			Entity d = pair.getDatum();
			Entity p = pair.getPattern();
			if (s.equals(p.getType())) {
				return pair.getDatum().getType();
			}
		}
		return null;
	}

	private String resolve(String s, LList<PairOfEntities> bindings) {
		for (Object object : bindings) {
			PairOfEntities pair = (PairOfEntities) object;
			Entity datum = pair.getDatum();
			Entity pattern = pair.getPattern();
			Mark.say(debug, s, datum.asString(), pattern.asString());
			if (s.equals(pattern.getType()) && datum.getFeatures() != null) {
				if (datum.getFeatures().indexOf("first") >= 0) {
					return getObject("0");
				}
				else if (datum.getFeatures().indexOf("second") >= 0) {
					return getObject("1");
				}
				else if (datum.getFeatures().indexOf("third") >= 0) {
					return getObject("2");
				}
			}
		}
		return null;
	}

	private void putObject(String index, String value) {
		objects.put(index, value);
	}

	private String getObject(String string) {
		return objects.get(string);
	}

	public void processVisionInput(Object object) {
		if (object instanceof Entity) {
			Entity t = (Entity) object;
			Mark.say(debug, "Mind's eye vision port received", t.asString());
		}
	}
}
