package start.transitionSpace;

import java.util.HashMap;

/*
 * Created on May 15, 2011
 * @author phw
 */

public class Ladder extends HashMap<String, HashMap<String, String>> {

	// Input objects

	public static final String O1 = "Alpha";

	public static final String O2 = "Bravo";

	public static final String O3 = "Charlie";

	// Data objects

	public static final String P1 = "pattern object 1";

	public static final String P2 = "pattern object 2";

	public static final String P3 = "pattern object 3";

	// Pattern rows

	public static final String ML = "moving left";

	public static final String MR = "moving right";

	// Transitions

	public static final String APPEAR = "appear";

	public static final String DISAPPEAR = "disappear";

	public static final String INCREASE = "increase";

	public static final String DECREASE = "decrease";

	public static final String CHANGE = "change";

	public static final String NOT_APPEAR = "not appear";

	public static final String NOT_DISAPPEAR = "not disappear";

	public static final String NOT_INCREASE = "not increase";

	public static final String NOT_DECREASE = "not decrease";

	public static final String NOT_CHANGE = "not change";

	public void addTransition(String object, String label, String change) {
		put(object, label, change);
	}

	private void put(String object, String label, String change) {
		this.getLabelMap(object).put(label, change);
	}

	public String get(String object, String label) {
		return this.getLabelMap(object).get(label);
	}
	
	private HashMap<String, String> getLabelMap(String object) {
		HashMap<String, String> map = this.get(object);
		if (map == null) {
			map = new HashMap<String, String>();
			this.put(object, map);
		}
		return map;
	}
	
	
}
