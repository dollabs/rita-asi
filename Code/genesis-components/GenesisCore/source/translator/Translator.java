package translator;

import java.util.*;


import connections.Connections;
import consciousness.Templates;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.Generator;
import generator.RoleFrames;
import generator.Rules;
import matchers.StandardMatcher;
import start.Start;
import utils.*;
import utils.minilisp.LList;

/**
 * Purpose is to handle sentences with quote marks on translation. Also used by generator. Generator matches Entity to
 * be translated against some standard patterns and translates by parts if a pattern is matched, inserting quotes.
 * <p>
 * Determined to substitute this for the parser-translate combination on 19 Jan 2016, so as to have better control over
 * total parse-translate combination.
 * <p>
 * Created on Jan 18, 2016
 * 
 * @author phw
 */

public class Translator extends Templates {

	public static final String SENTENCE = "sentence input";

	public static final String ENTITY = "entity output";

	private static Translator translator;

	public static Translator getTranslator() {
		if (translator == null) {
			translator = new Translator();
		}
		return translator;
	}

	public Translator() {
		Connections.getPorts(this).addSignalProcessor(SENTENCE, this::process);
	}

	public void process(Object input) {
		if (input instanceof String) {
			Connections.getPorts(this).transmit(ENTITY, translate((String) input));
		}
	}

	public Entity internalize(String s) {
		return translateToEntity(s);
	}

	public Entity translateToEntity(String s) {
		Entity t = generalizedTranslate(s);
		if (t == null) {
			Mark.say("Could not translate, got null, for", s);
			return null;
		}
		else if (!t.sequenceP()) {
			Mark.say("Could not translate, result not a sequence, for", s);
			return null;
		}
		else if (t.getElements().size() != 1) {
			Mark.say("Could not translate", s, "into single entity, got\n", t);
			return null;
		}
		return t.get(0);
	}

	public Entity translate(String s) {
		return generalizedTranslate(s);
	}


	// Z: here is what Translator.translate() calls first
	private Entity generalizedTranslate(String s) {

		boolean debug = false;
		Mark.say(debug, "Entering generalizedTranslate", s);

		// --------------- SPECIAL CASE 1: QUESTIONS
		// 
		// Z: tackle questions that start with "did", "why/when/where/how did", "why", "what happened after/before/when", 
		// 		"you think about/ask/conclude/explain/solve"
		Entity shortcut = specialTranslate(s);
		if (shortcut != null) return shortcut;
		

		// --------------- SPECIAL CASE 2: COMMON SENSE
		// 
		// Z: handle rules that start cannot handle at this time
		if (trapCommonsenseRule(s)) {
			Entity e = processCommonsenseRule(s);
			return e;
		}


		StringBuffer b = new StringBuffer(s.trim());
		

		// --------------- SPECIAL CASE 3: DIRECT SPEECH
		// 
		// Z: tackle direct speech like "John: I think Mary is crazy."
		// 		Turn it into John says "I think Mary is crazy" and then translate
		int index1 = b.indexOf(":");
		int index2 = b.indexOf(" ");
		if (index1 > 0 && index1 == index2 - 1) {
			String replacement = s.substring(0, index1) + " says \"" + Generator.stripPeriod(s.substring(index2 + 1).trim()) + "\".";
			return translate(replacement);
		}
		

		// --------------- SPECIAL CASE 4: QUOTED VARIABLES
		//
		// Z: tackle special story related sentences that contains quotation mark
		Entity xxx = null, yyy = null, zzz = null;
		// Replaces quoted material with variable strings in buffer, then returns value for variable
		Mark.say(debug, "Looking for quote marks", s);
		int index = findQuoteMark(b);
		boolean special = false;
		if (index >= 0) {
			special = true;
		}
		if (s.startsWith(Markers.START_CONCEPT_TEXT) || // Start description of
				s.startsWith(Markers.START_STORY_TEXT) || // Start story titled
		        s.endsWith(Markers.END_PERSONALITY_TEXT) || // is a kind of personality trait
		        s.contains(Markers.EVIDENTLY_TEXT) // triggers \"
		) {
			special = false;
		}
		if (special) {
			// Mark.err("Requires quote handler:", s);
			xxx = translateToEntityWithVariables(substitute("xxx", b));
			Mark.say(debug, "xxx", xxx);
			if (findQuoteMark(b) >= 0) {
			yyy = translateToEntityWithVariables(substitute("yyy", b));
			Mark.say(debug, "yyy", yyy);
				if (findQuoteMark(b) >= 0) {
				zzz = translateToEntityWithVariables(substitute("zzz", b));
				Mark.say(debug, "zzz", zzz);
				}
			}
			Mark.say(debug, "Trying to translate", b.toString());
			// Translate string with variables replacing quoted material
			Entity base = BasicTranslator.getTranslator().translate(b.toString());
			
			// Replace variables with parse of quoted material
			Mark.say(debug, "Basic translation", base, "\n", xxx, "\n", yyy, "\n", zzz);
			if (xxx != null) {
				substitute(xxx, "xxx", base);
			}
			if (yyy != null) {
				substitute(yyy, "yyy", base);
			}
			if (zzz != null) {
				substitute(zzz, "zzz", base);
			}
			Mark.say(debug, "Returning", base);
			return base;
		}
		
		
		
		
		// Z: normal sentences to translate
		else {
			Mark.say(debug, "Starting legacy translation", s);
			Entity e = BasicTranslator.getTranslator().translate(s);
			Mark.say(debug, "Returning legacy translation", e, "for", s);
			return e;
		}
		
		

	}
	
	
	
	
	
	
	// --------------- SPECIAL CASE 2: COMMON SENSE
	/**
	 * Added by phw 1 Oct 2017 to handle rules that start cannot handle at this time.
	 */
	private Sequence processCommonsenseRule(String sentence) {
		Start.getStart().setMode(Start.STORY_MODE);
		Mark.say("Entering processCommonsenseRule", sentence);
		int index1 = "whenever".length();
		int index2 = sentence.indexOf("then");
		String antecedentString = sentence.substring(index1, index2).trim();
		String[] antecedentStrings = antecedentString.split("and");

		List<Entity> antecedents = new ArrayList<>();

		for (int i = 0; i < antecedentStrings.length; ++i) {
			antecedents.add(generalizedTranslate(depunctuate(antecedentStrings[i])).get(0));
		}

		antecedents.stream().forEach(a -> Mark.say("Antecedent", a));


		int index3 = antecedentString.lastIndexOf(',');
		String consequentString = sentence.substring(index2 + 4).trim();

		Entity consequent = generalizedTranslate(depunctuate(consequentString)).get(0);

		Sequence result = new Sequence();
		Relation relation = Rules.makeCause(consequent, antecedents);
		result.addElement(relation);
		Start.getStart().setMode(Start.SENTENCE_MODE);
		return result;
	}

	private String depunctuate(String s) {
		s = s.trim();
		int index = s.length() - 1;
		char c = s.charAt(index);
		if (".,;?".indexOf(c) >= 0) {
			s = s.substring(0, index);
		}
		return s;
	}

	private boolean trapCommonsenseRule(Object input) {
		if (!(input instanceof String)) {
			Mark.say("Argument to trapCommonsense rule is not a string", input);
			return false;
		}
		String s = (String) input;
		if ((s.startsWith("Whenever ") || s.startsWith("whenever ")) && s.contains("then")) {
			return true;
		}
		return false;
	}

	
	// -------- SPECIAL CASE 1: QUESTIONS -----------------------------------------------
	// 
	// Z: detect special sentence patterns and unwrap it to become special sequence frames
	// 
	// ------------------
	
	private Entity specialTranslate(String input) {
		
		boolean debug = false;

		String s = input.toLowerCase();

		Mark.say(debug, "Entering specialTranslate", s);

		List<String> questionStarters = Arrays
		        .asList(

		                "did", "why did", "when did", "where did", "how did",

		                "why",

		                "what happened after", "what happened before", "what happened when",

		                "you think about whether", "you think about",

		                "you ask", "you ask about",

		                "you conclude", "you conclude that",

		                "you look in the story for",

		                "you explain that",

		                "you solve"

		);

		String starter = questionStarters.stream().filter(x -> s.startsWith(x)).findAny().orElse(null);

		if (starter == null) {
			return null;
		}

		// Mark.say("Starter is", starter);

		String core = s.substring(starter.length()).trim();

		Entity e = generalizedTranslate(core);

		if (e != null) {
			Sequence sequence = (Sequence) e;

			if (sequence.size() == 1) {
				// Looks like just one element in there, good news.
				decorate(sequence, starter);
				return sequence;
			}
			else {
				Mark.say(e);
			}
		}
		return null;
	}

	private void decorate(Sequence sequence, String starter) {
		// Ok, I know what to do
		Entity element = sequence.get(0);
		Entity replacement = new Function(Markers.QUESTION_MARKER, element);
		if (starter.equalsIgnoreCase("did")) {
			replacement.addType(Markers.DID_QUESTION);
		}
		else if (starter.equalsIgnoreCase("why did") || starter.equalsIgnoreCase("why")) {
			replacement.addType(Markers.WHY_QUESTION);
		}
		else if (starter.equalsIgnoreCase("when did")) {
			replacement.addType(Markers.WHEN_QUESTION);
		}
		else if (starter.equalsIgnoreCase("where did")) {
			replacement.addType(Markers.WHERE_QUESTION);
		}
		else if (starter.equalsIgnoreCase("how did")) {
			replacement.addType(Markers.HOW_QUESTION);
		}
		else if (starter.equalsIgnoreCase("what happened after")) {
			replacement.addType(Markers.WHAT_QUESTION);
			replacement.setSubject(new Function(Markers.AFTER, replacement.getSubject()));
		}
		else if (starter.equalsIgnoreCase("what happened before")) {
			replacement.addType(Markers.WHAT_QUESTION);
			replacement.setSubject(new Function(Markers.BEFORE, replacement.getSubject()));
		}
		else if (starter.equalsIgnoreCase("what happened when")) {
			replacement.addType(Markers.WHAT_QUESTION);
			replacement.setSubject(new Function(Markers.AFTER, replacement.getSubject()));
		}
		else if (starter.equalsIgnoreCase("you think about whether") || starter.equalsIgnoreCase("you think about")) {
			replacement = RoleFrames.makeRoleFrame("i", "think");
			RoleFrames.addRole(replacement, "about", element);
		}
		else if (starter.equalsIgnoreCase("you ask") || starter.equalsIgnoreCase("you ask about")) {
			replacement = RoleFrames.makeRoleFrame("i", "ask", element);
		}
		else if (starter.equalsIgnoreCase("you solve")) {
			replacement = RoleFrames.makeRoleFrame("i", "ask", element);
		}
		else if (starter.equalsIgnoreCase("you look in the story for")) {
			replacement = RoleFrames.makeRoleFrame("i", "look");
			RoleFrames.addRole(replacement, "in", "story");
			RoleFrames.addRole(replacement, "for", element);
		}
		else if (starter.equalsIgnoreCase("you explain that") || starter.equalsIgnoreCase("you explain")) {
			replacement = RoleFrames.makeRoleFrame("i", "explain", element);
		}
		else if (starter.equalsIgnoreCase("you conclude that") || starter.equalsIgnoreCase("you conclude")) {
			replacement = RoleFrames.makeRoleFrame("i", "conclude", element);
		}
		if (replacement != null) {
			replacement.addProperty(Markers.SPECIAL, true);
			sequence.set(0, replacement);
		}
	}
	
	// ------------------------------------------------------------------------------------------------
	


	// --------------- SPECIAL CASE 4: QUESTIONS ---------------------------------------------------
	//
	private int findQuoteMark(StringBuffer b) {
		int i1 = b.indexOf("\"");
		int i2 = b.indexOf("<");
		if (i1 < 0) {
			return i2;
		}
		else if (i2 < 0) {
			return i1;
		}
		return Math.min(i1, i2);
	}

	public Entity translateToEntityWithVariables(String s, String... variables) {
		if (s.indexOf(' ') < 0) {
			// Mark.say("Just a single word", s);
			return Start.makeThing(s);
		}
		return translateWithVariables(s, variables).get(0);
	}

	private Entity translateWithVariables(String s, String... variables) {
		for (String v : variables) {
			Entity vv = BasicTranslator.getTranslator().translateToEntity(v + " is a variable");
		}
		return generalizedTranslate(s);
	}

	private Map<String, Entity> getPatterns() {
		if (patterns == null) {
			Mark.err("Correct this; true for debugging only");
			patterns = new HashMap<String, Entity>();
			translateToEntityWithVariables("xx is a variable");
			translateToEntityWithVariables("yy is a variable");
			translateToEntityWithVariables("zz is a variable");
			
			templates.stream().forEach(t -> {
				Entity e = translateToEntity(t);
				if (e == null) {
					Mark.err("Could not translate", t);
				}
				else {
					patterns.put(t, e);
					// Mark.say("This is a pattern", e.toXML());
				}
			});
		}
		return patterns;
	}

	public void clearPatterns() {
		// Mark.err("Clearing patterns is a debugging hack!!!");
		// patterns = null;
	}

	private String generate(String pattern, Entity patternEntity, Entity entity) {
		return generateWithSpecifiedQuoteCharacter('\"', pattern, patternEntity, entity);
	}

	private String generateWithSpecifiedQuoteCharacter(char quote, String pattern, Entity patternEntity, Entity entity) {

		boolean debug = false;

		Mark.say(debug, "Entering generation from", entity);
		Mark.say(debug, "\n>>> Trying", pattern, "\n", patternEntity, "\n", entity);

		try {
			// Mark.say(debug, "\n>>> Pattern", patternEntity);
			// Mark.say(debug, "Datum ", entity);

			StringBuffer b = new StringBuffer(pattern);

			LList<PairOfEntities> match = StandardMatcher.getBasicMatcher().match(patternEntity, entity);


			if (match != null) {
				Mark.say(debug, "Generation result before substitution:", b.toString());
				for (PairOfEntities p : match) {
					if (p.getPattern().isA("xx")) {
						String g = generateWithSpecifiedQuoteCharacter(change(quote), p.getDatum());
						substitute(quote, g, "xx", b);
					}
					else if (p.getPattern().isA("yy")) {
						String g = generateWithSpecifiedQuoteCharacter(change(quote), p.getDatum());
						substitute(quote, g, "yy", b);
					}
					else if (p.getPattern().isA("zz")) {
						String g = generateWithSpecifiedQuoteCharacter(change(quote), p.getDatum());
						substitute(quote, g, "zz", b);
					}
				}
				Mark.say(debug, "Match", match);
				Mark.say(debug, "Generation result:", b.toString());
				return b.toString();
			}
			else {
				Mark.say(debug, "Match failed");
			}
		}
		catch (Exception e) {
			Mark.err("A bug attempting to generate English from", entity);
			return entity.toString();
		}
		return null;
	}
	

	public String generate(Entity e) {
		return generateWithSpecifiedQuoteCharacter('\"', e);
	}

	private String generateWithSpecifiedQuoteCharacter(char quote, Entity e) {
		boolean debug = false;
		for (String key : getPatterns().keySet()) {
			Entity value = getPatterns().get(key);
			String result = generateWithSpecifiedQuoteCharacter(quote, key, value, e);
			if (result != null) {
				Mark.say(debug, "Result is", result);
				return result;
			}
		}
		// Extra argument so won't do endless loop
		if (e.relationP()) {
			return Generator.getGenerator().generate(e, Markers.PRESENT);
		}
		else {
			return Generator.getGenerator().generate(e, null);
		}
	}
	

	public void substitute(Entity e, String s, Entity base) {
		if (base.getSubject() != null) {
			if (base.getSubject().isAPrimed(s)) {
				base.setSubject(e);
			}
			else {
				substitute(e, s, base.getSubject());
			}
		}
		if (base.getObject() != null) {
			if (base.getObject().isAPrimed(s)) {
				base.setObject(e);
			}
			else {
				substitute(e, s, base.getObject());
			}
		}
		if (base.getElements() != null) {
			Vector<Entity> clone = (Vector) (base.getElements().clone());
			for (int i = 0; i < clone.size(); ++i) {
				if (base.getElements().get(i).isAPrimed(s)) {
					base.getElements().set(i, e);
				}
				else {
					substitute(e, s, base.getElements().get(i));
				}
			}
		}
	}

	private char change(char quote) {
		if ('\"' == quote) {
			return '\'';
		}
		else {
			return '\"';
		}
	}

	private void substitute(char quote, String x, String variable, StringBuffer b) {
		int index = b.indexOf(variable);
		if (index >= 0) {
			String replacement = quote + Generator.stripPeriod(x) + quote;
			b.replace(index, index + 2, replacement);
		}
	}



	private String substitute(String string, StringBuffer b) {
		int index1 = findQuoteMark(b);
		// Mark.say("Quote mark is", b.substring(index1, index1 + 1));
		int index2 = -1;

		if (b.charAt(index1) == '"') {
			index2 = findMatch('"', b, index1 + 1);
		}
		else if (b.charAt(index1) == '<') {
			index2 = findMatch('>', b, index1);
		}
		if (index2 < 0) {
			Mark.err("Bad nesting of quotes in", b.toString());
		}
		String result = b.substring(index1 + 1, index2);
		b.replace(index1, index2 + 1, string);
		return result;
	}

	private int findMatch(char c, StringBuffer b, int start) {
		int depth = 0;
		for (int i = start; i < b.length(); ++i) {
			char x = b.charAt(i);
			if (x == '<') {
				++depth;
			}
			else if (x == '>') {
				--depth;
			}
			if (c == x && depth == 0) {
				return i;
			}
		}
		return -1;
	}





	private Map<String, Entity> patterns;
	
	public static void test() {
		Translator t = Translator.getTranslator();
		List<String> strings = new ArrayList<>();
		strings.add("I found a path from \"John loves Mary\" to \"Mary is smart.\"");
		strings.add("John loves Mary");
		strings.add("John says \"Mary is crazy.\"");
		strings.add("John: Mary is crazy.");
		strings.add("I believe America is militaristic");
		strings.add("Find a path from xx to yy.");
		strings.add("You put bowl on table");
		strings.add("Macbeth murders Duncan");
		for (String str : strings) {
			Mark.red(str);
			Entity ent = t.translate(str);
			ent.prettyPrint();
			ent.details();
		}
	}

	

	public static void main(String[] ignore) throws Exception {
		
		test();
		
//		Entity p = new Entity();

//		Entity p = Translator.getTranslator().translate("I found a path from \"John loves Mary\" to \"Mary is smart.\"");
//		
//		 Mark.say("Result1 is:", p.prettyPrint());
//		
//		p = BasicTranslator.getTranslator().translate("I found a path from \"John loves Mary\" to \"Mary is smart.\"");
//		
//		 Mark.say("Result2 is:", p.prettyPrint());
//		
//		 p = Translator.getTranslator().translate("I found a path from John loves Mary to Mary is smart.");
//		Mark.say("Result3 is believed bad, legacy:", p.prettyPrint());
//		
//		
//		 p = Translator.getTranslator().translateToEntity("John loves Mary");
////		
//		 Mark.say("Result4 is", p.prettyPrint());
//		 
//		 p = BasicTranslator.getTranslator().translateToEntity("John loves Mary");
////			
//		 Mark.say("Basic translator", p.prettyPrint());
//
		// p = Translator.getTranslator().translateToEntity("John says \"Mary is crazy.\"");
//
//		Mark.say("Result5 is", p.prettyPrint());
//
//		p = Translator.getTranslator().translateToEntity("John: Mary is crazy.");

//		Mark.say("Result6 is", p.prettyPrint());
//
//		// LList<PairOfEntities> match = StandardMatcher.getBasicMatcher().match(p, d);
//		// Mark.say("Match", match);
		
//		Mark.say("Working");
//
//		Entity q = Translator.getTranslator().translateToEntity(" I believe America is militaristic");
//
//		// q.addProperty(Markers.SPECIAL, true);
//
//		q = new Function(Markers.QUESTION, q);
//
//		q.addType(Markers.DID_QUESTION);
//
//		q.addProperty(Markers.TENSE, Markers.FUTURE);
//
//		Mark.say("Q is", q);
//
//		// RoleFrames.addRole(outer, Markers.ABOUT_MARKER, q);
//
//		Mark.say(getTranslator().generate(q));

		// Mark.say(getTranslator().generate(outer));

		// Mark.say(getTranslator().translate("John said \"Mary loves Paul\""));
		//
		// Mark.say("Match",
		// StandardMatcher.getBasicMatcher().match(
		//
		// Translator.getTranslator().translate("Find a path from xx to yy."),
		// Translator.getTranslator().translate("Find a path from xx to yy.")));
		
		// Mark.say(Translator.getTranslator().translate("John loves Mary."));
//		Generator g = Generator.getGenerator();
//
//		Entity e = Translator.getTranslator().translateToEntity("You put bowl on table");
//		e = RoleFrames.makeRoleFrame(new Entity("i"), "did", e);
//		String s = g.generate(e);
//		Mark.say("Result:\n", e, "\n", s);

	}

}
