package jakeBarnwell.treeGui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import frames.entities.Thread;
import utils.Mark;

/**
 * A node in a thread-tree ("spool") that has properties and methods that
 * allow it to easily be used in a graphical situation, e.g. to be drawn. 
 * @author jb16
 *
 */
public class DrawableSpool {	
	/** The {@link Thread} associated with this object */
	public final Thread thread;
	/** The word (i.e. final class of the {@link #thread}) associated with this object */
	public final String word;
	/** The parent of this object */
	public final DrawableSpool parent;
	/** The depth of this node in the tree */
	public final int depth;
	
	/** All children of the node, mapped by thread; may be empty, but never null */
	public HashMap<Thread, DrawableSpool> children = new HashMap<>();
	
	/**
	 * x-coordinate of the center of this node in the domain [0,1]
	 */
	public double x;
	
	/**
	 * y-coordinate of the center of this node in the domain [0,1]
	 */
	public double y;
	
	// Variables used in Reingold-Tilford Algorithm for computing coords
	private double X = 0;
	private double mod = 0;
	private double netX = 0; // Used for temporary counting of X+mod
	
	// How far away other nodes should sit in normalized space
	private static final int SIBLING_SEPARATION = 1;
	
	public DrawableSpool(Thread t, DrawableSpool parent, int depth) {
		this.thread = t;
		this.word = t.getType();
		this.parent = parent;
		this.depth = depth;
	}
	
	public void computeNormalizedDrawCoordinates() {
		// Only allow this method to be called on the root
		assert(parent == null);
		
		// Get the maximum depth of this tree
		int maxDepth = maxDepth();
		
		// Algorithm adapted from https://rachel53461.wordpress.com/2014/04/20/algorithm-for-drawing-trees/
		computeRelativeX(maxDepth);
		
		// Apply mod updates now so that we can accurately deduce contours
		applyMod(mod);
		
		// Final coordinate conversion, normalizes everything to the range of [0, 1]
		double minX = minX();
		double maxX = maxX();
		normalizeCoordinates(minX, maxX, maxDepth);
		tweakCoordinates(minX, maxX, maxDepth);
	}
	
	private double minX() {
		if(children.size() == 0) {
			return X;
		}
		return Math.min(X, children.values().stream().mapToDouble(c -> c.minX()).min().getAsDouble());
	}
	
	private double maxX() {
		if(children.size() == 0) {
			return X;
		}
		return Math.max(X, children.values().stream().mapToDouble(c -> c.maxX()).max().getAsDouble());
	}
	
	private int maxDepth() {
		
		if(children.size() == 0) {
			return depth;
		}

		return Math.max(depth, children.values().stream().mapToInt(c -> c.maxDepth()).max().getAsInt());
	}
	
	private boolean isLeftMost() {
		return parent == null || parent.getChildrenAlphabetical().get(0) == this;
	}
	
	/**
	 * Requires this node to be not the first child
	 * @return
	 */
	private DrawableSpool previousSibling() {
		List<DrawableSpool> orderedSiblings = parent.getChildrenAlphabetical();
		return orderedSiblings.get(orderedSiblings.indexOf(this) - 1);
	}

	private void computeRelativeX(int maxDepth) {
		List<DrawableSpool> orderedChildren = getChildrenAlphabetical();
		for(int ch = 0; ch < orderedChildren.size(); ch++) {
			orderedChildren.get(ch).computeRelativeX(maxDepth);
		}
		
		// Assigning X is super easy when you have no children
		if(children.size() == 0) {
			// if you're the first child, just set X to 0. Otherwise, it's 1+leftSibling.X
			if(isLeftMost()) {
				X = 0;
			} else {
				X = previousSibling().X + SIBLING_SEPARATION;
			}
		} else {
			// When you have children, sit yourself as the average of your children
			double aveX = children.values().stream().mapToDouble(c -> c.X).average().getAsDouble();
			if(isLeftMost()) {
				X = aveX;
			} else {
				X = previousSibling().X + SIBLING_SEPARATION;
				mod = X - aveX;
			}
		}

		// What happens if this children-set overlap with a sibling children-set?
		// Just move this set to the right as necessary; note that the left-most
		// child doesn't need to move.
		if(children.size() != 0 && !isLeftMost()) {
			resolveOverlapConflicts(maxDepth);
		}
	}
	
	/**
	 * Two (or more) subtrees may have overlapping nodes. So what we do is, at each
	 * level here and below, check the min/max X values of nodes at that level and
	 * separate the entire subtrees enough so that there is no overlap.
	 */
	private void resolveOverlapConflicts(int maxDepth) {
		// Note only have to check against the left sibling and move this subtree
		//  to the right as necessary
		DrawableSpool leftSibling = previousSibling();
		
		// Stores the contours of left sibling and me
		HashMap<Integer, Double> prevSiblingContour = new HashMap<>();
		HashMap<Integer, Double> myContour = new HashMap<>();
		leftSibling.computeNetX(0);
		this.computeNetX(0);
		for(int d = depth; d <= maxDepth; d++) {
			prevSiblingContour.put(d, leftSibling.descendantsAtDepth(d)
					.stream().mapToDouble(s -> s.netX).max().orElse(Float.NEGATIVE_INFINITY));
			myContour.put(d, this.descendantsAtDepth(d)
					.stream().mapToDouble(s -> s.netX).min().orElse(Float.POSITIVE_INFINITY));
		}
		
		// Find the maximum overlap and shift this tree by that much, plus 1
		double maxDifference = -1 * SIBLING_SEPARATION;
		for(int d = depth; d <= maxDepth; d++) {
			double difference = prevSiblingContour.get(d) - myContour.get(d);
			if(difference > -1 * SIBLING_SEPARATION) {
				maxDifference = Math.max(maxDifference, difference);
			}
		}
		if(maxDifference > -1 * SIBLING_SEPARATION) {
			double shiftAmount = maxDifference + SIBLING_SEPARATION;
			X += shiftAmount;
			mod += shiftAmount;
		}		
	}
	
	private Set<DrawableSpool> descendantsAtDepth(int d) {
		HashSet<DrawableSpool> set = new HashSet<>();

		// Trivial case; halt if
		if(d == this.depth) {
			set.add(this);
			return set;
		}
		
		// Otherwise, recurse
		for(DrawableSpool child : children.values()) {
			set.addAll(child.descendantsAtDepth(d));
		}
		return set;
		
		//TODO improve efficiency
	}
	
	/**
	 * Applies mod to update the X value for this node and all 
	 * children, then clears the mod value
	 */
	private void applyMod(double modSum) {
		this.X += modSum;
		modSum += this.mod;
		for(DrawableSpool child : children.values()) {
			child.applyMod(modSum);
		}
		this.mod = 0;
	}
	
	/**
	 *  Computes X+mod for every node here and below, without
	 *  overwriting X or mod.
	 */
	private void computeNetX(double modSum) {
		netX = X + modSum;
		modSum += this.mod;
		for(DrawableSpool child : children.values()) {
			child.computeNetX(modSum);
		}
	}
	
	/**
	 * Converts the variables determined in the RT algorithm to 
	 * be within intervals of [0, 1]
	 */
	private void normalizeCoordinates(double minX, double maxX, int maxDepth) {
		// First move x over so it falls after 0
		x = X - minX;
		
		// Normalize it by maxX-minX so it sits in [0,1] (unless maxX==minX)
		if(maxX - minX > 0) {
			x /= maxX - minX;
		}
		
		// Let lowest and highest words sit comfortably within the [0,1]
		//  window, noting that the depth=0 node technically doesn't
		//  exist (given how we use the tree) so ignore it.
		y = depth * (1 / (maxDepth + 1.0));
		
		// Recurse for children
		for(DrawableSpool child : children.values()) {
			child.normalizeCoordinates(minX, maxX, maxDepth);
		}
	}
	
	/**
	 * Applies any hacky tweaks necessary to the coordinates for 
	 * certain special cases, e.g. if the tree has no branching
	 * @param minX
	 * @param maxX
	 * @param maxDepth
	 */
	private void tweakCoordinates(double minX, double maxX, int maxDepth) {
		if(minX == maxX) {
			// Shoves all X-coordinates to 0.5. Sue me.
			normalizeCoordinates(-0.5, 0.5, maxDepth);
		}
	}

	/**
	 * Returns a list of children in alphabetical order by associated {@link #word}.
	 * @return
	 */
	private List<DrawableSpool> getChildrenAlphabetical() {
		return children.values()
				.stream()
				.sorted(Comparator.comparing(spool -> spool.word))
				.collect(Collectors.<DrawableSpool>toList());
	}
	
	@Override
	public String toString() {
		return String.format("WordTreeNode[%s, parent=%s, %d children]", 
				word, parent == null || parent.word == null ? "NIL" : parent.word, children.size());
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null || !(other instanceof DrawableSpool)) {
			return false;
		}
		
		DrawableSpool o = (DrawableSpool)other;
		return word.equals(o.word) 
				&& thread.equals(o.thread) 
				&& depth == o.depth 
				&& children.equals(o.children);
	}
	
	@Override
	public int hashCode() {
		int w = word.hashCode();
		int t = thread.hashCode();
		int c = children.hashCode();
		return w * 13 + t * 73 + c * 97 + depth * 193;
	}
	
	/**
	 * Gets all ancestors of this node. Does not include this
	 * node itself.
	 * @return
	 */
	public HashSet<DrawableSpool> getAncestors() {
		HashSet<DrawableSpool> ancestors = new HashSet<>();
		
		DrawableSpool next = this.parent;
		while(next != null) {
			ancestors.add(next);
			next = next.parent;
		}
		
		return ancestors;
	}
	
	/**
	 * Gets all descendants (children, grandchildren, ...)
	 * of this node. Does not include this node itself.
	 * @return
	 */
	public HashSet<DrawableSpool> getDescendants() {
		HashSet<DrawableSpool> descendants = new HashSet<>();
		ArrayList<DrawableSpool> agenda = new ArrayList<>();
		
		agenda.add(this);
		DrawableSpool now;
		while(!agenda.isEmpty()) {		
			now = agenda.remove(0);
			for(DrawableSpool child : now.children.values()) {
				if(!descendants.contains(child)) {
					agenda.add(child);
					descendants.add(child);
				}
			}
		}
		
		return descendants;
	}

}
