package subsystems.recall;

/*
 * Created on Jul 16, 2010
 * @author phw
 */

public class MatchContribution implements Comparable {

	String dimension;

	double value;

	public MatchContribution(String dimension, double value) {
		super();
		this.dimension = dimension;
		this.value = value;
	}

	public String getDimension() {
		return dimension;
	}

	public double getValue() {
		return value;
	}
	
	public String toString () {
		return "(" + dimension + ", " + value + ")";
	}

	@Override
	public int compareTo(Object o) {
		return dimension.compareTo(((MatchContribution) o).dimension);
	}

}
