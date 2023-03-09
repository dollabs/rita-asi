package consciousness;

import java.util.*;

import constants.Markers;
import frames.entities.Entity;
import generator.Generator;
import generator.RoleFrames;
import matchers.StandardMatcher;
import translator.Translator;
import utils.*;
import utils.minilisp.LList;

/*
 * Created on Jan 18, 2016
 * @author phw
 */

public class FormRecognizer {

	private static FormRecognizer formRecognizer;

	public static FormRecognizer getSpecial() {
		if (formRecognizer == null) {
			formRecognizer = new FormRecognizer();
		}
		return formRecognizer;
	}

	private FormRecognizer() {
		// TODO Auto-generated constructor stub
	}

	public Entity translate(String s) {
		boolean debug = false;
		try {
			List<String> parts = new ArrayList<>();
			StringBuffer b = new StringBuffer(s);
			Entity xx = null, yy = null, zz = null;
			if (b.indexOf("\"") >= 0) {
				xx = translation(substitute("xx", b));
				Mark.say(debug, "xx", xx);
				if (b.indexOf("\"") >= 0) {
					yy = translation(substitute("yy", b));
					Mark.say(debug, "yy", yy);
					if (b.indexOf("\"") >= 0) {
						zz = translation(substitute("zz", b));
						Mark.say(debug, "zz", zz);
					}
				}
			}
			Entity base = translation(b.toString());
			Mark.say(debug, "Basic translation", base);
			if (xx != null) {
				substitute("xx", xx, base);
			}
			if (yy != null) {
				substitute("yy", yy, base);
			}
			if (xx != null) {
				substitute("zz", zz, base);
			}
			Mark.say(debug, "Revision", base);
			return base;
		}
		catch (Exception e) {
			Mark.err("Bug in special");
		}
		Mark.err("No translation for", s);
		return null;
	}


	Map<String, Entity> patterns;



	public Map<String, Entity> getPatterns() {
		if (patterns == null) {
			patterns = new HashMap<String, Entity>();
			translation("xx is a variable");
			translation("yy is a variable");
			translation("zz is a variable");
			templates.stream().forEach(t -> patterns.put(t, translate(t)));
		}
		return patterns;
	}

	public String generate(Entity e) {
		for (String key : getPatterns().keySet()) {
			Entity value = getPatterns().get(key);
			// Mark.say("\n>>> Trying", key, "\n", value, "\n", e);
			String result = generate(key, value, e);
			if (result != null) {
				return result;
			}
		}
		// Extra argument so won't do endless loop
		return Generator.getGenerator().generate(e, Markers.PRESENT);
	}

	public String generate(String pattern, Entity patternEntity, Entity datumEntity) {
		boolean debug = false;
		try {
			Mark.say(debug, "Pattern", patternEntity);
			Mark.say(debug, "Datum  ", datumEntity);

			LList<PairOfEntities> match = StandardMatcher.getBasicMatcher().match(patternEntity, datumEntity);
			Mark.say(debug, "Match", match);

			StringBuffer b = new StringBuffer(pattern);
			if (match != null) {

				for (PairOfEntities p : match) {
					if (p.getPattern().isA("xx")) {
						substitute(p.getDatum(), "xx", b);
					}
					else if (p.getPattern().isA("yy")) {
						substitute(p.getDatum(), "yy", b);
					}
					else if (p.getPattern().isA("zz")) {
						substitute(p.getDatum(), "zz", b);
					}
				}
				return b.toString();
			}
		}
		catch (Exception e) {
			Mark.say("A bug!");
		}
		return null;
	}

	private void substitute(Entity datum, String variable, StringBuffer b) {
		int index = b.indexOf(variable);
		if (index >= 0) {
			String replacement = "\"" + Generator.stripPeriod(generate(datum)) + "\"";
			b.replace(index, index + 2, replacement);
		}

	}

	public void substitute(String s, Entity e, Entity base) {
		if (base.getSubject() != null) {
			if (base.getSubject().isAPrimed(s)) {
				base.setSubject(e);
			}
			else {
				substitute(s, e, base.getSubject());
			}
		}
		if (base.getObject() != null) {
			if (base.getObject().isAPrimed(s)) {
				base.setObject(e);
			}
			else {
				substitute(s, e, base.getObject());
			}
		}
		if (base.getElements() != null) {
			Vector<Entity> clone = (Vector) (base.getElements().clone());
			for (int i = 0; i < clone.size(); ++i) {
				if (base.getElements().get(i).isAPrimed(s)) {
					base.getElements().set(i, e);
				}
				else {
					substitute(s, e, base.getElements().get(i));
				}
			}
		}
	}


	private String substitute(String string, StringBuffer b) {
		int index1 = b.indexOf("\"");
		int index2 = b.indexOf("\"", index1 + 1);
		String result = b.substring(index1 + 1, index2);
		b.replace(index1, index2 + 1, string);
		return result;
	}

	public Entity translation(String s, String... variables) {
		try {
			for (String v : variables) {
				Entity vv = Translator.getTranslator().translate(v + " is a variable");
				// Mark.say("VV", vv.getObject().toXML());
			}
			return Translator.getTranslator().translate(s);
		}
		catch (Exception e) {
			Mark.say("Unable to translate", s);
		}
		return null;
	}

	List<String> templates = Arrays.asList(

	"Is there a path from xx to yy.",

	"A path leads from xx to yy.",

	"I do not believe xx.",

	"Do I believe xx.",

	"I believe xx",

	"Did the story contain xx",

	"The story contains xx",

	"Did xx.",

	"I found xx.",
	
	"I showed xx.",

	"I check xx.",

	"I solve xx.",

	"I check that xx.",

	// "I want to show xx.",

	"I ask xx.",

	        "I work on xx.",

	        "xx has no explanation"

	);

	public static void main(String[] ignore) throws Exception {

		// Entity e1 = Special.getSpecial().translate("is there a path from xx to yy");
		//
		// Entity e2 = Special.getSpecial().translate("is there a path from \"John loves Mary\" to \"John eats
		// lunch\"");
		//
		// Mark.say("English:", Special.getSpecial().generate(e2));
		//
		Entity d = RoleFrames.makeRoleFrame("path", "lead");

		RoleFrames.addRole(d, "from", Translator.getTranslator().translate("John loves Mary"));

		RoleFrames.addRole(d, "to", Translator.getTranslator().translate("Mary is smart"));

		// Translator.getTranslator().translation("xx is a variable");
		// Translator.getTranslator().translation("yy is a variable");

		// Entity p = Special.getSpecial().translation("a path leads from xx to yy", "xx", "yy");
		//
		// Mark.say("d", d);
		//
		// Mark.say("p", p);

		Entity p = FormRecognizer.getSpecial().translation("i determined that xx", "xx");

		LList<PairOfEntities> match = StandardMatcher.getBasicMatcher().match(p, d);
		Mark.say("Match", match);

	}

}
