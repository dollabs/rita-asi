package matching;

import java.util.*;

import org.junit.*;

import frames.entities.Entity;
import generator.Generator;
import matchers.*;
import matchers.representations.*;
import start.Start;
import translator.BasicTranslator;

public class BindingValidatorTests {

	@Test(timeout = 200000)
	public void testExclusion() {
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		Generator generator = Generator.getGenerator();
		Start.getStart().setMode(Start.STORY_MODE);
		generator.flush();

		String[] sentences1 = new String[] { "Macbeth is a person.", "Duncan is a person.", "Macbeth kills Duncan." };
		String[] sentences2 = new String[] { "Hamlet is a person.", "Claudius is a person.", "Hamlet kills Claudius." };

		List<Entity> story1 = new ArrayList<Entity>();
		for (String sentence : sentences1) {
			try {
				Entity elt = basicTranslator.translate(sentence).getElement(0);
				story1.add(elt);
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				Assert.fail("Parsing Problem!");
			}
		}

		Start.getStart().setMode(Start.STORY_MODE);
		generator.flush();

		List<Entity> story2 = new ArrayList<Entity>();
		for (String sentence : sentences2) {
			try {
				Entity elt = basicTranslator.translate(sentence).getElement(0);
				story2.add(elt);
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				Assert.fail("Parsing Problem!");
			}
		}

		Entity macbeth = null;
		Entity duncan = null;
		Entity hamlet = null;
		Entity claudius = null;
		try {
			macbeth = story1.get(0).getObject();
			duncan = story1.get(1).getObject();
			hamlet = story2.get(0).getObject();
			claudius = story2.get(1).getObject();
		}
		catch (Exception e) {
			Assert.fail("Failed to extract entities, parser may have changed!");
		}

		EntityMatcher em = new EntityMatcher();
		BindingValidator bv = new BindingValidator();

		EntityMatchResult emr = em.match(story1.get(2), story2.get(2));
		Assert.assertNotNull("Binding Validation failed unexpectedly!", bv.validateBindings(emr.bindings));

		BindingPair irreleventExclusion = new BindingPair(false, duncan, hamlet, 1.0);
		emr.bindings.add(irreleventExclusion);
		Assert.assertNotNull("BindingValidator failed to ignore irrelevent exlusion properly!", bv.validateBindings(emr.bindings));

		BindingPair exclusion = new BindingPair(false, macbeth, hamlet, 1.0);
		emr.bindings.add(exclusion);

		Assert.assertNull("BindingValidator failed to apply exlusion properly!", bv.validateBindings(emr.bindings));
	}
}
