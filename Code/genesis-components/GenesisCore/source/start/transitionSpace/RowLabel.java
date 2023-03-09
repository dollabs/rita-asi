package start.transitionSpace;

import utils.Mark;

/*
 * Created on May 15, 2011
 * @author phw
 */

class RowLabel {
	String object;

	String label;

	String key;

	public RowLabel(String object, String label) {
		this.object = object;
		this.label = label;
		this.key = object + " " + label;
	}

	public String toString() {
		return "<" + object + " / " + label + ">";
	}

	public int hashCode() {
		// Mark.betterSay("Hashcode for", this, "-->", key.hashCode());
		return key.hashCode();
	}
}