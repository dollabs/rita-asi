package matching;

import java.util.*;

import org.junit.*;

import frames.entities.Entity;
import generator.Generator;
import matchers.EntityMatcher;
import matchers.original.BasicMatcherOriginal;
import matchers.representations.EntityMatchResult;
import start.Start;
import translator.BasicTranslator;
import utils.Mark;

/**
 * A set of tests for testing the consistency between AbstractEntityMatcher and other matchers. Eventually AEM will
 * supplant all matchers where possible.
 * 
 * @author Matthew
 */
public class MatcherConsistencyTests {
	public static void testMatching(String[] sentences1, String[] sentences2, int match1, int match2, boolean match) {
		testMatching(sentences1, sentences2, match1, match2, match, false);
	}

	public static void testMatching(String[] sentences1, String[] sentences2, int match1, int match2, boolean match, boolean old_in_error) {
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		Generator generator = Generator.getGenerator();
		Start.getStart().setMode(Start.STORY_MODE);

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

		// StandardMatcher bm = StandardMatcher.getBasicMatcher();
		BasicMatcherOriginal bm = BasicMatcherOriginal.getBasicMatcher();
		EntityMatcher em = new EntityMatcher();

		Entity elt1 = story1.get(match1);
		Entity elt2 = story2.get(match2);

		EntityMatchResult emr = em.match(elt1, elt2);
		bm.match(elt1, elt2);

		Assert.assertTrue("EntityMatcher Match Result Failure on\n" + elt1 + elt2 + "\n" + em.match(elt1, elt2) + "\n"
		        + elt1.getSubject().getPrimedThread() + "\n" + elt2.getSubject().getPrimedThread() + "\n" + em.match(elt1, elt2).isMatch() + "!="
		        + match, em.match(elt1, elt2).isMatch() == match);
		if (!old_in_error) {
			Assert.assertTrue("BasicMatch NOT EQUIVALENT TO EntityMatcher" + elt1 + elt2 + "\n" + em.match(elt1, elt2) + "\n"
			        + elt1.getSubject().getPrimedThread() + "\n" + elt2.getSubject().getPrimedThread() + "\n" + em.match(elt1, elt2).isMatch() + "!="
			        + (bm.match(elt1, elt2) != null), em.match(elt1, elt2).isMatch() == (bm.match(elt1, elt2) != null));
		}
	}

	public final static String simple0 = "Sally and Mary are cats.";

	public final static String simple1 = "John and Gary are dogs.";

	public final static String simple2 = "John likes Sally.";

	public final static String simple3 = "Gary likes Mary.";

	public final static String simple4 = "Mary likes Sally.";

	@Test(timeout = 20000)
	public void testSimpleMatching() {
		Mark.say("Testing basic matching...");
		testMatching(new String[] { simple0, simple1, simple2, simple3, simple4 }, new String[] { simple0, simple1, simple2, simple3,
		        simple4 }, 2, 3, true);

		Mark.say("Testing basic match fail...");
		testMatching(new String[] { simple0, simple1, simple2, simple3, simple4 }, new String[] { simple0, simple1, simple2, simple3,
		        simple4 }, 1, 3, false);
	}

	@Test(timeout = 20000)
	public void testTypeMismatch() {
		Mark.say("Testing type mismatch...");
		testMatching(new String[] { simple0, simple1, simple2, simple3, simple4 }, new String[] { simple0, simple1, simple2, simple3,
		        simple4 }, 2, 4, false);
	}

	public final static String partialTypeless0 = "Neil and Sarah are people.";

	public final static String partialTypeless1 = "Neil likes Sarah.";

	public final static String partialTypeless2 = "Bob likes Molly.";

	@Test(timeout = 20000)
	public void testPartialTypelessMatch() {
		Mark.say("Testing partial type-less match...");
		testMatching(new String[] { partialTypeless0, partialTypeless1 }, new String[] { partialTypeless2 }, 1, 0, false);
	}

	public final static String typeless0 = "Doug likes Doug.";

	public final static String typeless1 = "Jake likes Jake.";

	@Test(timeout = 20000)
	public void testTypelessMatch() {
		Mark.say("Testing type-less match...");
		testMatching(new String[] { typeless0 }, new String[] { typeless1 }, 0, 0, false);
	}

	public final static String sovereignty0 = "Macbeth, Duncan, Macduff, and George are persons.";

	public final static String sovereignty1 = "Macbeth is not the king.";

	public final static String sovereignty2 = "Duncan is the king.";

	public final static String sovereignty3 = "Macduff is not the king.";

	public final static String sovereignty4 = "George is the king.";

	@Test(timeout = 20000)
	public void testNegationMatching() {
		Mark.say("Testing double positives...");
		testMatching(new String[] { sovereignty0, sovereignty2 }, new String[] { sovereignty0, sovereignty4 }, 1, 1, true);
		Mark.say("Testing double negative...");
		testMatching(new String[] { sovereignty0, sovereignty1 }, new String[] { sovereignty0, sovereignty3 }, 1, 1, true);
		Mark.say("Testing negative-positive...");
		testMatching(new String[] { sovereignty0, sovereignty1 }, new String[] { sovereignty0, sovereignty2 }, 1, 1, false);
		Mark.say("Testing positive-negative...");
		testMatching(new String[] { sovereignty0, sovereignty4 }, new String[] { sovereignty0, sovereignty3 }, 1, 1, false);
	}

	public final static String hansel0 = "Hansel and Duncan are persons.";

	public final static String hansel1 = "The candy_house eats Hansel";

	public final static String hansel2 = "Duncan eats Hansel";

	public final static String hansel3 = "Hansel eats Duncan";

	@Test()
	public void testThreadMatching() {
		Mark.say("Testing thread sensitive matching...");
		testMatching(new String[] { hansel0, hansel1, hansel2, hansel3 }, new String[] { hansel0, hansel1, hansel2, hansel3 }, 2, 3, true);
		testMatching(new String[] { hansel0, hansel1, hansel2, hansel3 }, new String[] { hansel0, hansel1, hansel2, hansel3 }, 3, 2, true);
		testMatching(new String[] { hansel0, hansel1, hansel2, hansel3 }, new String[] { hansel0, hansel1, hansel2, hansel3 }, 1, 2, true, true);
		testMatching(new String[] { hansel0, hansel1, hansel2, hansel3 }, new String[] { hansel0, hansel1, hansel2, hansel3 }, 2, 1, false, true);
	}
}
