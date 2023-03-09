package utils;

/*
 * Created on May 7, 2010
 * @author phw
 */

public class StringIntPair {

	String label;

	int value;

	public String getLabel() {
		return label;
	}

	public int getValue() {
		return value;
	}
	
	public String toString() {
		return "<" + label + ", " + value + ">";
	}

	public StringIntPair(String label, int value) {
		super();
		this.label = label;
		this.value = value;
	}

	public void setValue(int i) {
	   value = i;
    }
	
	public void incrementValue() {
		++value;
	}

}
