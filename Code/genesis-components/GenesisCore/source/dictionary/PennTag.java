/*
 * Filename: PennTag.java Author: M. A. Finlayson Format: Java 2 v1.5.GenericTag.SYMBOL Date created: Apr 11, 2007
 */
package dictionary;

import java.util.*;

/**
 * The tags in this enum class are drawn from Marcus, M. P., M. A. Marcinkiewicz, et al. (1993). "Building a large
 * annotated corpus of English: the penn treebank." Computational Linguistics 19(2): 313-330.
 * <p>
 * There is a single error in that article; it mistakenly marks the "possessive pronoun" tag as 'PP' instead of 'PRP$'
 * 
 * @author M.A. Finlayson
 * @since Apr 11, 2007; JDK 1.5.0
 */
public enum PennTag {

	CC("CC", "Coordinating conjuction", null), CD("CD", "Cardinal number", null), DT("DT", "Determiner", null), EX("EX", "Existential there", null), FW(
	        "FW", "Foreign word", null), IN("IN", "Preposition/subordinating conjunction", null), JJ("JJ", "Adjective",
	        edu.mit.jwi.item.POS.ADJECTIVE), JJR("JJR", "Comparative adjective", "Adjective, comparative", edu.mit.jwi.item.POS.ADJECTIVE), JJS(
	        "JJS", "Superlative adjective", "Adjective, superlative", edu.mit.jwi.item.POS.ADJECTIVE), LS("LS", "List item", "List item marker", null), MD(
	        "MD", "Modal", edu.mit.jwi.item.POS.VERB), NN("NN", "Singular noun", "Noun, singular or mass", edu.mit.jwi.item.POS.NOUN), NNS("NNS",
	        "Plural noun", "Noun, plural", edu.mit.jwi.item.POS.NOUN), NNP("NNP", "Singular proper noun", "Proper noun, singular",
	        edu.mit.jwi.item.POS.NOUN), NNPS("NNPS", "Plural  proper noun", "Proper noun, plural", edu.mit.jwi.item.POS.NOUN), PDT("PDT",
	        "Predeterminer", null), POS("POS", "Possessive ending", null), PRP("PRP", "Personal pronoun", null), PRP$("PRP$", "Possessive pronoun",
	        null), // there is an error in (Marcus 1993); this is marked as 'PP'
	RB("RB", "Adverb", edu.mit.jwi.item.POS.ADVERB), RBR("RBR", "Comparative adverb", "Adverb, comparative", edu.mit.jwi.item.POS.ADVERB), RBS("RBS",
	        "Superlative adverb", "Adverb, superlative", edu.mit.jwi.item.POS.ADVERB), RP("RP", "Particle", null), SYM("SYM", "Symbol",
	        "Symbol (mathematical or scientific)", null), TO("TO", "to", null), UH("UH", "Interjection", null), VB("VB", "Verb, base form",
	        edu.mit.jwi.item.POS.VERB), VBD("VBD", "Verb, past tense", edu.mit.jwi.item.POS.VERB), VBG("VBG", "Verb, gerund/present participle",
	        edu.mit.jwi.item.POS.VERB), VBN("VBN", "Verb, past participle", edu.mit.jwi.item.POS.VERB), VBP("VBP",
	        "Verb, non-3rd person singular present", edu.mit.jwi.item.POS.VERB), VBZ("VBZ", "Verb, 3rd person singular present",
	        edu.mit.jwi.item.POS.VERB), WDT("WDT", "wh-determiner", null), WP("WP", "wh-pronoun", null), WP$("WP$", "Possessive wh-pronoun", null), WRB(
	        "WRB", "wh-adverb", null), POUND("#", "Pound sign", null), DOLLAR("$", "Dollar sign", null), PERIOD(".", "Period",
	        "Sentence-final punctuation", null), COMMA(",", "Comma", null), COLON(":", "Colon or semi-colon", null), BRACKET_LEFT("(", "Left parens",
	        "Left bracket character", null), BRACKET_RIGHT(")", "Right parens", "Right bracket character", null), QUOTE_DOUBLE_STRAIGHT("\"",
	        "Straight double quote", null), QUOTE_SINGLE_LEFT("`", "Left single quote", "Left open single quote", null), QUOTE_DOUBLE_LEFT("``",
	        "Left double quote", "Left open double quote", null), QUOTE_SINGLE_RIGHT("'", "Right single quote", "Right close single quote", null), QUOTE_DOUBLE_RIGHT(
	        "''", "Right double quote", "Right close double quote", null), ADJP("ADJP", "Adjective phrase", null), ADVP("ADVP", "Adverb phrase", null), NP(
	        "NP", "Noun phrase", null), PP("PP", "Prepositional phrase", null), S("S", "Simple declarative clause", null), SBAR("SBAR", "S-Bar",
	        "Clause introduced by subordinating conjuction or 0", null), SBARQ("SBARQ", "S-Bar Question",
	        "Direct question introduced by wh-word or wh-phrase", null), SINV("SINV", "S-Inv", "Declaritive sentence with subject-aux inversion",
	        null), SQ("SQ", "SBARQ Subconsituent", "Subconstituent of SBARQ excluding wh-word or wh-phrase", null), VP("VP", "Verb phrase", null), WHADVP(
	        "WHADVP", "wh-adverb phrase", null), WHNP("WHNP", "wh-noun phrase", null), WHPP("WHPP", "wh-prepositional phrase", null), UNKNOWN("X",
	        "Unknown", "Consituent of unknown or uncertain category", null), STAR("*", "Understood subject",
	        "`Understood' subject of infinitive or imperative", null), ZERO("0", "Zero variant", "Zero variant of that in subordinate clauses", null), TRACE(
	        "T", "Trace", "Trace--marks position where moved wh-consituent is interpreted", null), NIL("NIL", "Interpreted preposition location",
	        "Marks position where preposition is interpreted in pied-piping contexts", null);

	private final String fTag, fName, fDesc;

	private final edu.mit.jwi.item.POS fPos;

	final static Map<String, PennTag> tagMap;

	static {
		Map<String, PennTag> hidden = new HashMap<String, PennTag>();
		for (PennTag t : values())
			hidden.put(t.getTag(), t);
		tagMap = Collections.unmodifiableMap(hidden);
	}

	public static edu.mit.jwi.item.POS convert(String tag) {
		PennTag ptag = getTag(tag);
		return ptag == null ? null : ptag.toWordnetPOS();
	}

	public static PennTag getTag(String tag) {
		return tagMap.get(tag);
	}

	private PennTag(String tag, String name, edu.mit.jwi.item.POS wordnetPOS) {
		this(tag, name, name, wordnetPOS);
	}

	private PennTag(String tag, String name, String description, edu.mit.jwi.item.POS wordnetPOS) {
		fTag = tag;
		fName = name;
		fDesc = description;
		fPos = wordnetPOS;
	}

	/* (non-Javadoc) @see edu.mit.parsing.rep.POS.ITag#getName() */
	public String getName() {
		return fName;
	}

	/* (non-Javadoc) @see edu.mit.parsing.rep.POS.ITag#getTag() */
	public String getTag() {
		return fTag;
	}

	/* (non-Javadoc) @see edu.mit.parsing.rep.POS.ITag#getDescription() */
	public String getDescription() {
		return fDesc;
	}

	/* (non-Javadoc) @see edu.mit.parsing.rep.POS.ITag#toPOS() */
	public edu.mit.jwi.item.POS toWordnetPOS() {
		return fPos;
	}

	/* (non-Javadoc) @see java.lang.Object#toString() */
	public String toString() {
		return fName;
	}
}
