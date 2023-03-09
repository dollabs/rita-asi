package basics;

import dictionary.BundleGenerator;
import frames.entities.Bundle;
import frames.entities.Entity;
import generator.Generator;
import translator.Translator;
import utils.Mark;

public class T4_EntityClassification {

	static Translator t = Translator.getTranslator();
	static Generator g = Generator.getGenerator();
	
	public static void main(String[] args) {
		
//		demoGetWordNetClass();
//		demoEstablishWordType();
		demoGeneralization();

	}
	
	public static void demoGetWordNetClass() {
		
		// Use getType() and getPrimedThread() to see the categorization of the frame
		Entity x = t.translateToEntity("George helped Mary with Peter with insights");
		Mark.say("Entity type:", x.getType());
		Mark.say("Primed thread:", x.getPrimedThread());
		x.getPrimedThread().stream().forEachOrdered(c -> Mark.say("It is a:", c));
		
		// Use BundleGenerator.getBundle()
		Bundle bundle = BundleGenerator.getBundle("Dog");
		Mark.say(bundle);
		Mark.say("Empty?", bundle.isEmpty());
		bundle = BundleGenerator.getBundle("Bouvier");
		Mark.say(bundle);
		Mark.say("Empty?", bundle.isEmpty());
		
	}

	public static void demoEstablishWordType() {

		Entity x = t.translateToEntity("A bouvier is small");
		Mark.say("Entity:", x);
		Mark.say("Entity type:", x.getType());
		Mark.say("Primed thread for 'bouvier':", x.getSubject().getPrimedThread());

		// Use "X is a kind of Y" to add the primed thread of Y to that of X
		x = t.translateToEntity("A bouvier is a kind of beast");
		Mark.say("Entity:", x);
		Mark.say("Entity type:", x.getType());
		Mark.say("Primed thread for 'bouvier':", x.getObject().getPrimedThread());
		
		// "X is a Y" works too
		x = t.translateToEntity("John is a person");
		Mark.say("Entity:", x);
		Mark.say("Entity type:", x.getType());
		Mark.say("Primed thread for 'bouvier':", x.getObject().getPrimedThread());
		
	}
	
	public static void demoGeneralization() {
		
		t.internalize("A bouvier is a kind of dog.");
		t.internalize("Sonja is a bouvier.");
		Entity property = t.internalize("Sonja is brave.");
		
		// Look at prime thread, should include "bouvier" but not "brave"
		Entity sonja = property.getSubject();
		Mark.say("Examine", sonja.getPrimedThread());

		// Generalize: delete the last element in the primed thread
		Entity generalizedSonja = sonja.geneneralize(sonja);
		Mark.say("Examine", generalizedSonja.getPrimedThread());

		// Generalize again
		generalizedSonja = generalizedSonja.geneneralize(sonja);
		Mark.say("Examine", generalizedSonja.getPrimedThread());
		
	}
}
