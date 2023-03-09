package kevinWhite;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

import frames.entities.Entity;

public class FasterLLConceptTest {

	@Test
	public void testParseSimpleSentencePositive() throws Exception {
		String sentence = "Fish can swim.";
		String expected_noun = "fish";
		String expected_verb = "action travel swim";
		Boolean expected_feature = true;
		HashMap result = FasterLLConcept.parseSimpleSentence(sentence);
		Entity noun_entity = (Entity) result.get("noun");
		String noun = noun_entity.toEnglish();
		String verb = (String) result.get("verb");
		Boolean feature = (Boolean) result.get("feature");
		assertEquals(expected_noun, noun);
		assertEquals(expected_verb, verb);
		assertEquals(expected_feature, feature);
	}

	@Test
	public void testParseSimpleSentenceNegative() throws Exception {
		String sentence = "Fish cannot swim.";
		String expected_noun = "fish";
		String expected_verb = "action travel swim";
		Boolean expected_feature = false;
		HashMap result = FasterLLConcept.parseSimpleSentence(sentence);
		Entity noun_entity = (Entity) result.get("noun");
		String noun = noun_entity.toEnglish();
		String verb = (String) result.get("verb");
		Boolean feature = (Boolean) result.get("feature");
		assertEquals(expected_noun, noun);
		assertEquals(expected_verb, verb);
		assertEquals(expected_feature, feature);
	}
}
