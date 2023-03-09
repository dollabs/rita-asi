package subsystems.recall;

import java.util.*;

import frames.entities.Sequence;
import utils.Mark;

/*
 * Created on Jul 12, 2010
 * @author phw
 */

public class StoryVectorMatcher {

	public static int ENTITY = 0;

	public static int CONCEPT = 1;

	public static TreeSet<MatchWrapper> findBestMatches(StoryVectorWrapper probe, ArrayList<StoryVectorWrapper> candidates, int mode) {
		if (probe == null || candidates == null) {
			return null;
		}
		TreeSet<MatchWrapper> result = new TreeSet<MatchWrapper>();
		for (StoryVectorWrapper candidate : candidates) {
			if (probe.getTitle() == null || candidate.getTitle() == null || probe.getTitle().equalsIgnoreCase(candidate.getTitle())) {
				// Best match cannot be with self;
				// continue;
			}
			result.add(getMatchWrapper(probe, candidate, mode));
		}
		return result;
	}

	public static MatchWrapper getMatchWrapper(StoryVectorWrapper p1, StoryVectorWrapper p2, int mode) {
		double l1 = 0;
		double sum = 0;
		TreeSet<MatchContribution> contributions = new TreeSet<MatchContribution>();
		for (String s : getKeySet(p1, mode)) {
			int v1 = getValue(s, p1, mode);
			l1 += Math.pow(v1, 2);
		}
		double l2 = 0;
		for (String s : getKeySet(p2, mode)) {
			int v2 = getValue(s, p2, mode);
			l2 += Math.pow(v2, 2);
		}
		double denominator = Math.sqrt(l1 * l2);
		for (String s : getKeySet(p1, mode)) {
			int v1 = getValue(s, p1, mode);
			int v2 = getValue(s, p2, mode);
			double result = 0;
			if (denominator > 0) {
				result = (v1 * v2) / denominator;
			}
			sum += result;
			contributions.add(new MatchContribution(s, result));
		}
		return new MatchWrapper(sum, contributions, p1, p2);
	}

	private static Set<String> getKeySet(StoryVectorWrapper wrapper, int mode) {
		return getMap(wrapper, mode).keySet();
	}

	private static int getValue(String word, StoryVectorWrapper wrapper, int mode) {
		Integer i = getMap(wrapper, mode).get(word);
		if (i == null) {
			return 0;
		}
		else {
			return i;
		}
	}

	private static Map<String, Integer> getMap(StoryVectorWrapper wrapper, int mode) {
		if (mode == CONCEPT) {
			return wrapper.getConceptMap();
		}
		else if (mode == ENTITY) {
			return wrapper.getEntityMap();
		}
		Mark.err("Unrecognized mode");
		return null;
	}

}
