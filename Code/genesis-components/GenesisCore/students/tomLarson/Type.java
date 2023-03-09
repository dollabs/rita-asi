package tomLarson;

public class Type {
	
	/**
	 * A Type is a Thing. Any String within a Thread is a type. 
	 * Types have weights, for the impact that that type has. 
	 * 
	 */
	private String name;
	private double weight;
	
	public Type(String name, double weight) {
		this.name = name.toLowerCase();
		this.weight = weight;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public String getName() {
		return name;
	}
	
	public void setWeight(double newWeight) {
		weight = newWeight;
	}
	
	public String toString() {
		return name + ":" + weight;
	}
	
}
