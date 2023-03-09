package matchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import matchers.representations.ThreadMatchResult;
import matthewFay.Utilities.HashMatrix;
import utils.Mark;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Thread;

public class ThreadMatcher {

	/**
	 * Sets the mode for comparing threads If an Element is set as a template then that Element's threads will not match
	 * if they become longer than the other Element's corresponding thread NoTemplates tries to match threads regardless
	 * of length ExactOnly requires Threads to be identical
	 * 
	 * @author Matthew
	 */
	public enum MatchMode {
		BASIC, SCORE
	}

	private boolean searchAllThreads = true;

	public void searchAllThreads(boolean value) {
		searchAllThreads = value;
	}

	public MatchMode patternMatchMode = MatchMode.BASIC;

	public boolean requireIdentityMatch = false; // From old IdentityMatcher, useful

	public boolean two_way_match = false;

	public float score_cutoff = 0.1f;

	public void useScoreMatching() {
		patternMatchMode = ThreadMatcher.MatchMode.SCORE;
	}

	public void useIdentityMatching() {
		requireIdentityMatch = true;
	}

	public ThreadMatchResult match(Entity element1, Entity element2) {
		if (searchAllThreads) {
			for (Thread t1 : element1.getBundle()) {
				for (Thread t2 : element2.getBundle()) {
					ThreadMatchResult result = match(t1, t2);
					if (result.match) {
						return result;
					}
				}
			}
		}
		return match(element1.getPrimedThread(), element2.getPrimedThread());
	}

	/**
	 * Returns -1 if threads do not match Otherwise returns a value 0 through 1 indicating goodness of match Affected by
	 * ThreadMatchMode Other notes: - 'I' given special treatment, names normally do not need to match
	 * 
	 * @param pattern_thread
	 * @param datum_thread
	 * @return
	 */
	public ThreadMatchResult match(Thread pattern_thread, Thread datum_thread) {
		ThreadMatchResult result = new ThreadMatchResult(pattern_thread, datum_thread);
		// If either thread is NULL, this is bad
		if (pattern_thread == null || datum_thread == null) {
			return result;
		}

		boolean matchedI = false;
		// If only one thread is I, not allowed to match
		if (pattern_thread.getType().equalsIgnoreCase(Markers.i) && !datum_thread.getType().equalsIgnoreCase(Markers.i)) {
			matchedI = true;
			// return result;
		}

		switch (patternMatchMode) {
		case BASIC:
			// Check for anythings
			if (pattern_thread.contains("anything") || datum_thread.contains("anything")) {
				result.score = 1;
				result.match = true;
			}
			// Do subset pattern<datum matching
			else if (subset_match(pattern_thread, datum_thread)) {
				result.score = 1;
				result.match = true;
			}
			else if (two_way_match) {
				if (subset_match(datum_thread, pattern_thread)) {
					result.score = 1;
					result.match = true;
				}
			}
			// Identity Match validation
			boolean pattern_named = (pattern_thread.size() >= 2 && pattern_thread.get(pattern_thread.size() - 2).equals(Markers.NAME));
			boolean datum_named = (datum_thread.size() >= 2 && datum_thread.get(datum_thread.size() - 2).equals(Markers.NAME));
			if (pattern_named && datum_named) {
				if (pattern_thread.get(pattern_thread.size() - 1).equalsIgnoreCase(datum_thread.get(datum_thread.size() - 1))) {
					result.identityMatch = true;
				}
			}
			if (requireIdentityMatch) {
				if (!result.identityMatch && (pattern_named || datum_named)) {
					result.score = -1;
					result.match = false;
				}
			}
			break;
		case SCORE:
			// Used for iteration bounds
			result.minLength = Math.min(pattern_thread.size(), datum_thread.size());
			result.maxLength = Math.max(pattern_thread.size(), datum_thread.size());

			List<String> telts1 = new ArrayList<String>(pattern_thread);
			List<String> telts2 = new ArrayList<String>(datum_thread);

			result.matches = countMatches(pattern_thread, datum_thread);
			if (result.maxLength > 0) {
				result.score = score(pattern_thread, datum_thread);
			}
			if (result.score > score_cutoff) result.match = true;
			break;
		}
		if (matchedI && result.score == 1.0) {
			// Mark.err("Matched", result.score, pattern_thread, datum_thread, "\nchange may have broken code");

		}
		return result;
	}

	// Pattern: entity person name Macbeth
	// Datum: entity name XX
	// ^^^^ Not a match
	// Pattern: entity name XX
	// Datum: entity person name Macbeth
	// ^^^^ MATCHES
	private boolean subset_match(Thread pattern, Thread datum) {
		// Axe the names
		Vector<String> pattern_copy = new Vector<>(pattern);
		Vector<String> datum_copy = new Vector<>(datum);
		if (pattern_copy.contains(Markers.NAME) || datum_copy.contains(Markers.NAME)) {
			pattern_copy.remove(pattern_copy.size() - 1);
			datum_copy.remove(datum_copy.size() - 1);
		}
		while (pattern_copy.contains(Markers.NAME)) {
			pattern_copy.remove(pattern_copy.size() - 1);
		}
		while (datum_copy.contains(Markers.NAME)) {
			datum_copy.remove(datum_copy.size() - 1);
		}
		if (datum_copy.containsAll(pattern_copy)) {
			// Mark.say("Matching!\n", pattern, "\n", datum);
			return true;
		}
		return false;
	}

	private int countMatches(Thread thread1, Thread thread2) {
		int l1 = thread1.size();
		int l2 = thread2.size();
		int matches;
		for (matches = 0; matches < Math.min(l1, l2); matches++) {
			if (thread1.get(matches).equalsIgnoreCase(thread2.get(matches))) {
				continue;
			}
			break;
		}
		return matches;
	}

	// From the intersectTypes function, I like this metric
	private double score(Thread thread1, Thread thread2) {
		int matches = countMatches(thread1, thread2);
		int l1 = thread1.size();
		int l2 = thread2.size();
		return Math.pow(matches, 2) / (l1 * l2);
	}
}
