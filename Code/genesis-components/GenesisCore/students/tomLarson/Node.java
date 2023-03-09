package tomLarson;

import java.util.HashSet;
import java.util.Set;

/**
 * A Node is an object with one parent and possibly many children.
 * @author Thomas
 *
 */
@SuppressWarnings("rawtypes")
public class Node<T extends Type> {
	
	private Type type;
	private Node parent;
	private Set<Node> children;
	
	public static final int propdist = 10;
	public static final double impactDecreaseFactor = .5;
	public static final double startImpact = 1;
	
	/**
	 * Make a new Node
	 */
	public Node(T type) {
		this.type = type;
		parent = null;
		children = new HashSet<Node>();
	}
	/**
	 * Make a new node without creating a Type first. 
	 * The new Node has a null parent and no children
	 * @param type
	 * @param weight
	 */
	public Node(String name, double weight) {
		this.type = new Type(name, weight);
		parent = null;
		children = new HashSet<Node>();
	}
	
	
	/**
	 * Changes the weight of the current node and the surrounding nodes
	 * according to the values propDist, impactDecreaseFactor, and startImpact
	 *
	 */
	public void propagateImpact() {
		propagateImpact(startImpact, propdist);
	}
	
	//helper
	private void propagateImpact(double impact, int moreLevels) {
		if (moreLevels <= 0) {return;}
		//Impact this node
		setWeight(getWeight() + impact);
		Node parent = getParent();
		if (parent != null) {
			parent.propagateImpact(impact*impactDecreaseFactor, moreLevels - 1);
		}
		for (Node child : getChildren()) {
			child.propagateImpact(impact*impactDecreaseFactor, moreLevels - 1);
		}
	}
	
	/**
	 * Gets the name of this node
	 * @return
	 */
	public String getName() {
		return type.getName();
	}
	
	public boolean equals(Object o) {
		if (o instanceof Node) {
			Node node = (Node) o;
			return (node.getName().equals(type.getName()));
		}
		return false;
	}
	
	public void setWeight(double newWeight) {
		type.setWeight(newWeight);
	}
	/**
	 * Sets the parent of this node. Nodes  have only one parent, which may be null.
	 * @param n
	 */
	public void setParent(Node n) {
		parent = n;
	}
	
	/**
	 * Adds a child to this Node
	 * @param n
	 */
	public void addChild(Node n) {
		children.add(n);
	}
	
	public Node getParent() {
		return parent;
	}
	
	public Set<Node> getChildren() {
		return children;
	}
	
	public double getWeight() {
		return type.getWeight();
	}

	
	public String toString() {
		return type.getName();
	}
	
	public Type getType() {
		return type;
	}

}
