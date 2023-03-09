package matching;

import java.util.*;

import org.junit.*;

import frames.entities.Entity;
import generator.Generator;
import matchers.EntityMatcher;
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
public class MatcherFeatureTests {
	public static void testMatching(String[] sentences1, String[] sentences2, int match1, int match2, boolean match, boolean score_match) {
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		Generator generator = Generator.getGenerator();
		Start.getStart().setMode(Start.STORY_MODE);
		generator.flush();

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

		EntityMatcher em = new EntityMatcher();
		if (score_match) em.useScoreMatching();

		Entity elt1 = story1.get(match1);
		Entity elt2 = story2.get(match2);

		EntityMatchResult emr = em.match(elt1, elt2);

		Assert.assertTrue("EntityMatcher Match Result Failure on\n" + elt1 + elt2 + "\n" + em.match(elt1, elt2) + "\n"
		        + elt1.getSubject().getPrimedThread() + "\n" + elt2.getSubject().getPrimedThread() + "\n", em.match(elt1, elt2).isMatch() == match);
	}

	public final static String simple0 = "Sally and Mary are cats.";

	public final static String simple1 = "John and Gary are dogs.";

	public final static String simple2 = "John likes Sally.";

	public final static String simple3 = "Gary likes Mary.";

	public final static String simple4 = "Mary likes Sally.";

	public final static String simple5 = "Tony is a car.";

	public final static String simple6 = "Mary likes Tony.";

	@Test(timeout = 20000)
	public void testSimpleMatching() {
		Mark.say("Testing basic matching...");
		testMatching(new String[] { simple0, simple1, simple2, simple3, simple4, simple5, simple6 }, new String[] { simple0, simple1, simple2,
		        simple3, simple4, simple5, simple6 }, 2, 3, true, false);

		Mark.say("Testing basic match fail...");
		testMatching(new String[] { simple0, simple1, simple2, simple3, simple4, simple5, simple6 }, new String[] { simple0, simple1, simple2,
		        simple3, simple4, simple5, simple6 }, 1, 3, false, false);

		Mark.say("Testing score matching...");
		testMatching(new String[] { simple0, simple1, simple2, simple3, simple4, simple5, simple6 }, new String[] { simple0, simple1, simple2,
		        simple3, simple4, simple5, simple6 }, 3, 4, true, true);

		Mark.say("Testing negative score matching...");
		testMatching(new String[] { simple0, simple1, simple2, simple3, simple4, simple5, simple6 }, new String[] { simple0, simple1, simple2,
		        simple3, simple4, simple5, simple6 }, 3, 6, false, true);
	}
}
