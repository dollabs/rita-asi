// use Bundle bundle = BundleGenerator.getBundle("Dog") to know the classes of word
// use Entity.getPrimedThread() to know the classes of entity

package zhutianYang.School;

import java.util.List;

import dictionary.BundleGenerator;
import frames.entities.Bundle;
import frames.entities.Entity;
import generator.Generator;
import generator.RoleFrames;
import matchers.*;
import translator.Translator;
import utils.*;
import utils.minilisp.LList;

/*
 * Purpose Created on 21 April, 2016
 * @author phw
 */

public class TestSpecializationGeneralization {

	public static void main(String[] ignore) throws Exception {
		
		Translator t = Translator.getTranslator();
		Generator g = Generator.getGenerator();
		
		Bundle bundle = BundleGenerator.getBundle("phone");
		Mark.say(!bundle.isEmpty(), "phone in WordNet \n", bundle);
		
//		bundle = BundleGenerator.getBundle("Move");
//		Mark.say(!bundle.isEmpty(), "Move in WordNet \n", bundle);
//		
//		// determine if a noun is in the WordNet
//		bundle = BundleGenerator.getBundle("Dog");
//		Mark.say(!bundle.isEmpty(), "Dog in WordNet \n", bundle);
//		
//		bundle = BundleGenerator.getBundle("Bouvier");
//		Mark.say(!bundle.isEmpty(), "Bouvier in WordNet \n", bundle);
		
		// ------------------------------------
		// 1st example of specification
		t.internalize("Sonja is a bouvier.");
		Entity property = t.internalize("Sonja is brave.");
		Entity sonja = property.getSubject();
		Mark.say("\nBefore Internalize:", sonja.getPrimedThread());
		
		// 2nd example of even more specification
		t.internalize("A bouvier is a kind of dog.");
		t.internalize("Sonja is a bouvier.");
		property = t.internalize("Sonja is brave.");
		sonja = property.getSubject();
		Mark.say("\nAfter Internalize:", sonja.getPrimedThread());

		// ------------------------------------
		// 1st time of generalization
		Entity generalization = sonja.geneneralize(sonja);
		Mark.say("\n1st generalization", generalization.getPrimedThread());

		// 2nd time of generalization
		generalization = generalization.geneneralize(sonja);
		Mark.say("\n2nd generalization", generalization.getPrimedThread());

	}
}
