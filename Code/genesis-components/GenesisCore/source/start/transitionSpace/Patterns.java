package start.transitionSpace;

import java.util.*;

import utils.Mark;

/*
 * Created on May 15, 2011
 * @author phw
 */

public class Patterns extends ArrayList<Pattern> {

	private static Patterns patterns;

	private HashMap<String, String> t1 = new HashMap<String, String>();

	private HashMap<String, String> t2 = new HashMap<String, String>();

	private HashMap<String, String> t3 = new HashMap<String, String>();

	private HashMap<String, String> t4 = new HashMap<String, String>();

	private HashMap<String, String> t5 = new HashMap<String, String>();

	private HashMap<String, String> t6 = new HashMap<String, String>();

	private ArrayList<HashMap> transforms = new ArrayList<HashMap>();

	public static Patterns getPatterns() {
		if (patterns == null) {
			patterns = new Patterns();
		}
		return patterns;
	}

	public Patterns() {

		t1.put(Ladder.P1, Ladder.O1);
		t1.put(Ladder.P2, Ladder.O2);
		t1.put(Ladder.P3, Ladder.O3);

		t2.put(Ladder.P1, Ladder.O1);
		t2.put(Ladder.P2, Ladder.O3);
		t2.put(Ladder.P3, Ladder.O2);

		t3.put(Ladder.P1, Ladder.O2);
		t3.put(Ladder.P2, Ladder.O1);
		t3.put(Ladder.P3, Ladder.O3);

		t4.put(Ladder.P1, Ladder.O2);
		t4.put(Ladder.P2, Ladder.O3);
		t4.put(Ladder.P3, Ladder.O1);

		t5.put(Ladder.O1, Ladder.P3);
		t5.put(Ladder.O2, Ladder.P1);
		t5.put(Ladder.O3, Ladder.P2);

		t6.put(Ladder.O1, Ladder.P3);
		t6.put(Ladder.O2, Ladder.P2);
		t6.put(Ladder.O3, Ladder.P1);

		transforms.add(t1);
		transforms.add(t2);
		transforms.add(t3);
		transforms.add(t4);
		transforms.add(t5);
		transforms.add(t6);

		Pattern pattern = new Pattern("moves left");
		Ladder ladder = new Ladder();
		ladder.addTransition(Ladder.P1, Ladder.ML, Ladder.APPEAR);
		pattern.addLadder(ladder);
		this.addPattern(pattern);

		pattern = new Pattern("moves right");
		ladder = new Ladder();
		ladder.addTransition(Ladder.P1, Ladder.MR, Ladder.APPEAR);
		pattern.addLadder(ladder);
		this.addPattern(pattern);

	}

	public void addPattern(Pattern p) {
		this.add(p);
	}

	public String match(Ladders event) {
		for (Pattern pattern : this) {
			for (HashMap<String, String> t : transforms) {
				if (match(t, event, pattern)) {
					Mark.say("Found pattern:", t.get(Ladder.P1), pattern.getName());
					return t.get(Ladder.P1) + " " + pattern.getName();
				}
			}
		}
		Mark.say("No match");
		return null;
	}

	private boolean match(HashMap<String, String> transform, Ladders event, Pattern pattern) {
		int eventLadderCount = event.size();
		for (int i = 0; i < pattern.size(); ++i) {
			Ladder pLadder = pattern.get(i);
			int eventIndex = eventLadderCount - i - 1;
			if (eventIndex < 0) {
				return false;
			}
			Ladder eLadder = event.get(eventIndex);
			for (String object : pLadder.keySet()) {
				HashMap<String, String> map = pLadder.get(object);
				for (String label : map.keySet()) {
					String patternValue = pLadder.get(object, label);
					String eventValue = eLadder.get(transform.get(object), label);
					if (patternValue != eventValue) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public static void main(String[] ignore) {
		Ladders event = new Ladders();
		Ladder ladder = new Ladder();
		ladder.addTransition(Ladder.O2, Ladder.MR, Ladder.APPEAR);
		event.addLadder(ladder);

		Patterns.getPatterns().match(event);

	}
}
