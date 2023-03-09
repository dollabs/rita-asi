package jakeBarnwell.concept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import frames.entities.Thread;
import jakeBarnwell.Tools;

/**
 * Represents a node in a tree of threads. The root of this tree corresponds
 * to a thread of size 1; children of each node correspond to a thread with 
 * one additional hyponym. The parent of a node corresponds to the hypernymm
 * of the node.
 * 
 * @author jb16
 *
 */
public class Spool {
	/**
	 * For easy debugging
	 */
	private Set<Spool> debugDescendants;
	
	/**
	 * The exact thread this spool represents. Never null.
	 */
	public Thread thread;
	
	/**
	 * Parent node of this spool
	 */
	public Spool parent;
	
	/**
	 * How deep we are down the spool. The root has a depth of 0. There is an
	 * invariant: depth + 1 == threadSize.
	 */
	public int depth;
	
	/**
	 * The charge associated with this exact thread, if applicable. This
	 * charge can only be set if it was assigned directly from a human-supplied
	 * example. For example, if <A B C> was declared to be a positive example,
	 * then the spool for <A B C> has a Charge denoting its positivity; whereas
	 * the spools for <A>, <A B>, <A B C D>, ... all have a null charge.
	 */
	public Charge charge;
	
	/**
	 * Whether or not the exact thread this node represents is permitted by
	 * the lattice learner. Note that if a {@link #charge} is present, its 
	 * value is always consistent with {@link #permissibleP}.
	 */
	public boolean permissibleP = false;
	
	/**
	 * All descendant spools (including this one) that are permitted by the
	 * lattice learner (as set forth by their {@link #permissibleP} fields).
	 * This field is only valid in the root node of the spool. This field
	 * may be empty, but never null.
	 */
	public Set<Spool> permissible = new HashSet<>();
	
	/**
	 * All children of this node. Possibly empty, but never null.
	 */
	public HashMap<Thread, Spool> children = new HashMap<>();
	
	/**
	 * Enforce that no one can call the constructor directly. Please use
	 * the static method {@link Spool#build()} or 
	 * {@link Spool#build(Thread, Charge)} instead.
	 */
	private Spool() {
		;
	}
	
	/**
	 * Builds a new spool from a thread and a charge. The new spool 
	 * is the root node with a thread of size 1, and the descendants
	 * have been created as appropriate. The terminal node represents 
	 * the given thread and charge, and the entire spool has properly 
	 * incorporated the thread and charge.
	 * @param t
	 * @param c
	 * @return
	 */
	public static Spool build(Thread t, Charge c) {
		// TODO for now, build iteratively so easier to debug. Later on
		//  can switch to using .insert(.)
		Spool current = new Spool();
		Spool currentParent = null;
		Thread currentThread = Thread.constructThread(t.get(0));
		int currentDepth = 0;
		while(true) {
			current = new Spool();
			current.thread = currentThread;
			current.parent = currentParent;
			current.depth = currentDepth;
			currentDepth++;
			currentParent = current;
			if(currentDepth >= t.size()) {
				break;
			}
			currentThread = Thread.constructThread(t, 0, currentDepth + 1);
		}
		current.charge = c;
		Spool root = current.getRoot();
		root.incorporate(t, c);
		return root;
	}
	
	/**
	 * Builds a spool with a given string as a label, i.e. as an
	 * atomic, singular thread with one class.
	 * @return
	 */
	public static Spool build(String label) {
		Thread t = Thread.constructThread(label);
		Spool theSpool = new Spool();
		theSpool.thread = t;
		return theSpool;
	}
	
	/**
	 * True if this is the root node (i.e. has no parent)
	 * @return
	 */
	public boolean isRoot() {
		return this.parent == null;
	}
	
	/**
	 * True if this is a terminal node (i.e. has no children)
	 * @return
	 */
	public boolean isTerminal() {
		return this.children.keySet().isEmpty();
	}
	
	/**
	 * Returns an arbitrary terminal of the subtree rooted at this node.
	 * @return
	 */
	public Spool getATerminal() {
		Spool current = this;
		while(!current.children.isEmpty()) {
			current = new ArrayList<>(current.children.values()).get(0);
		}
		
		return current;
	}
	
	/**
	 * Gets the root node of this tree.
	 * @return
	 */
	public Spool getRoot() {
		Spool current = this;
		while(current.parent != null) {
			current = current.parent;
		}
		
		return current;
	}
	
	/**
	 * Returns the descendant of this spool that represents the given thread;
	 * possibly returns itself or null.
	 * @param t
	 * @return
	 */
	public Spool getByThread(Thread t) {
		if(this.thread == null) {
			throw new RuntimeException("Cannot call this method when it's an empty spool!");
		}
		if(t == null || t.size() == 0) {
			return null;
		}
		if(this.thread.equals(t)) {
			return this;
		}
		
		try {
			Spool child = children.get(Thread.constructThread(t, 0, this.depth + 2));
			return child.getByThread(t);
		} catch(RuntimeException e) {
			return null;			
		}
	}
	
	/**
	 * True if this spool is an ancestor (parent-chain) of another spool, or if 
	 * it is the same spool as the other spool.
	 * @param ospool
	 * @return
	 */
	public boolean isAncestorOf(Spool ospool) {
		return ospool.thread.startsWith(this.thread);
	}
	
	/**
	 * Defined to be true iff the other spool is an ancestor of this spool.
	 * @param ospool
	 * @return
	 */
	public boolean isDescendantOf(Spool ospool) {
		return ospool.isAncestorOf(this);
	}
	
	/**
	 * Returns the thread that this spool shares in common with some other
	 * thread. If either thread is null or empty or if the threads have 
	 * no class in common, returns an empty thread.
	 * @param ot
	 * @return the common thread. Never returns null.
	 */
	public Thread inCommon(Thread ot) {
		if(ot == null || this.thread == null || ot.size() == 0 || this.thread.size() == 0) {
			return new Thread();
		}
		
		if(!ot.startsWith(this.thread)) {
			return new Thread();
		}
		
		Spool next;
		Spool current = this;
		while(true) {
			if(ot.size() < current.depth + 2) {
				break;
			}
			next = current.children.get(Thread.constructThread(ot, 0, current.depth + 2));
			if(next == null || !ot.startsWith(next.thread)) {
				break;
			}
			current = next;
		}

		return current.thread == null ? new Thread() : current.thread;
	}
	
	/**
	 * Registers this node as a permissible one in the lattice-learner, and
	 * updates the root's record of permissible nodes as well.
	 * @param isPermissible
	 */
	public void setPermissible(boolean isPermissible) {
		if(isPermissible) {
			getRoot().permissible.add(this);
		} else {
			getRoot().permissible.remove(this);
		}
		permissibleP = isPermissible;
	}
	
	/**
	 * Inserts a new thread into the record of this spool, if it doesn't
	 * already exist. If the thread already exists in this spool, errors
	 * if the charge is different from the one already stored.
	 * 
	 * Note that 
	 * The new terminal node defined by the inserted thread (if not
	 * already extant) has its {@link #charge} set as appropriate. No 
	 * values of {@link #permissibleP} are set.
	 * 
	 * @param t
	 * @param charge
	 * @throws RuntimeException if the thread cannot belong in the subtree
	 * rooted at this node, or if the thread already exists in the tree but
	 * with a different charge recorded.
	 */
	private void insert(Thread t, Charge charge) {
		// If this is a spool, initialize fields properly
		if(thread == null) {
			thread = Thread.constructThread(t.get(0));
			parent = null;
			depth = 0;
		}
		
		// If the new thread is exactly the size for this depth
		if(depth + 1 == t.size()) {
			if(this.thread.equals(t)) {
				// Disallow a terminal from changing its charge, ever.
				if(this.charge != null && this.charge != charge && this.isTerminal()) {
					throw new RuntimeException("Attempt to assign a different example charge to " 
							+ "an already extant terminal thread. What are you thinking?!");
				}
				this.charge = charge;
				return;
			} else {
				throw new RuntimeException("This thread " + t.toString() + " does not belong in this subtree!");
			}
		}
		
		// Errors if new thread is too small for this depth 
		if(depth + 1 > t.size()) {
			throw new RuntimeException("Can't insert thread " + t.toString()
					+ " into this subtree (" + thread.toString() + ", depth " + depth + ").");
		}
		
		// By now, we know the thread is longer than the current depth allows, so look at strict descendants
		Thread childThread = Thread.constructThread(t, 0, this.depth + 2);
		// Checks if this thread lexically can belong in this subtree
		if(!childThread.startsWith(this.thread)) {
			throw new RuntimeException("Attempting to insert child thread " + childThread.toString()
					+ " even though this node ( " + thread.toString() + ") cannot be an ancestor.");
		}
		
		// Recurse to child
		if(children.containsKey(childThread)) {
			// If that child is present, recurse onto it.
			children.get(childThread).insert(t, charge);
		} else {
			// Otherwise, insert child and recurse onto it
			Spool child = new Spool();
			child.thread = childThread;
			child.parent = this;
			child.depth = depth + 1;
			child.insert(t, charge);
			children.put(childThread, child);
		}
	}
	
	/**
	 * Incorporates a new example into the record for this spool, inserting
	 * the thread if necessary and updating all relevant nodes with their
	 * permissibility in the lattice learning matcher.
	 * 
	 * Whenever a new thread is inserted that has something in common with 
	 * the spool, the lowest (most-specific) common class between the 
	 * thread and this spool is chosen as a "pivot." Depending on the charge 
	 * of the new example (thread), different updates may be performed on 
	 * the spools above and/or below the pivot.
	 * 
	 * On a brand new spool with no threads, the entire node ancestry for the
	 * new thread is illegal, except for the terminal thread itself. This
	 * is in line with the definition of a "strict" lattice learning which
	 * only allows things that have been definitively added to the whitelist.
	 * 
	 * If a new example is negative, it is not allowed to over-write the
	 * permissibility of any nodes. Because this is a strict algorithm,
	 * the only this situation might come up would be if there is a common
	 * ancestor node of, say, 3 or more different terminals: in that case,
	 * the common node (if it's marked as permissible) will remain permissible,
	 * but all ancestry down to the negative terminal node will be marked as
	 * impermissible.
	 * 
	 * @requires that the new thread has something in common with the spool
	 * @param t
	 * @param c
	 * @return false if the thread with the given charge was already part of
	 * the spool (i.e. if calling this method was redundant); otherwise true.
	 */
	public boolean incorporate(Thread t, Charge c) {
		assert(t.size() > 0);
		
		// Check if redundant
		Spool existingTerminal = this.getByThread(t);
		if(existingTerminal != null && existingTerminal.charge == c) {
			return false;
		}
		
		// This will throw an error if t is already there but with a different
		//  charge listed.
		this.insert(t, c);
		
		// For now we won't do an incremental algorithm (which I'd prefer because
		// it's easier for the system to explain itself). We'll just recompute
		// everything each time a new example is incorporated.
		this.updatePermissibles();
		
		debugDescendants = getDescendants();
		return true;
	}
	
	/**
	 * Updates (sets) all nodes of this subtree to have a correct permissible
	 * value. Used as a helper function for incorporate.
	 */
	private void updatePermissibles() {
		// If this is a terminal, set it appropriately
		if(this.isTerminal()) {
			this.setPermissible(this.charge == Charge.POSITIVE);
			return;
		}
		
		Set<Spool> terminals = this.getTerminals();
		// If any are negative, set this one impermissible and recurse
		if(terminals.stream().anyMatch(term -> term.charge == Charge.NEGATIVE)) {
			this.setPermissible(false);
			children.values().forEach(child -> child.updatePermissibles());
		} else {
			// If all terminals are positive, set all to permissible if this is
			//  a joint node.
			if(children.values().size() >= 2) {
				this.setPermissible(true);
				this.getDescendants().forEach(desc -> desc.setPermissible(true));
			} else {
				this.setPermissible(false);
				children.values().forEach(child -> child.updatePermissibles());
			}
		}
	}
	
	@Override 
	public boolean equals(Object other) {
		if(other == null || !(other instanceof Spool)) {
			return false;
		}
		
		Spool o = (Spool)other;
		
		return Tools.safeEquals(thread, o.thread) &&
				depth == o.depth &&
				charge == o.charge &&
				permissibleP == o.permissibleP && 
				permissible.equals(o.permissible) &&
				children.equals(o.children);
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash *= Tools.safeHashCode(thread) *
				(depth + 2) * 
				Tools.safeHashCode(charge) * 
				(permissibleP ? 11 : 3 ) * 
				Tools.safeHashCode(permissible) * 
				Tools.safeHashCode(children);
		return hash;
	}
	
	@Override 
	public String toString() {
		if(isRoot()) {
			String permits = "";
			for(Spool c : permissible) {
				permits += String.format("%s,", c.identifier());
			}
			permits = permits.length() > 0 ? permits.substring(0, permits.length() - 1) : permits;
			return String.format("%s[root%s%s>%s%s(%dT)]", 
					this.getClass().getSimpleName(), mark(), identifier(), Symbol.CHECK, permits, getTerminals().size());
		} else {
			return String.format("%s[%s%s(%dT)]", 
					this.getClass().getSimpleName(), mark(), identifier(), getTerminals().size());
		}
		

		
	}
	
	/**
	 * Makes a deep copy of this object
	 * @return
	 */
	public Spool copy() {
		Spool copy = Spool.build("");
		copy.thread = this.thread.copyThread();
		copy.depth = depth;
		copy.charge = charge;
		copy.permissibleP = permissibleP;
		copy.permissible = permissible.stream()
				.map(sp -> sp.copy())
				.collect(Collectors.<Spool>toSet());
		copy.children = new HashMap<Thread, Spool>();
		for(Entry<Thread, Spool> es : children.entrySet()) {
			copy.children.put(es.getKey().copyThread(), es.getValue().copy());
		}
		
		// Re-assign parent pointers
		copy.assignParentPointers(false);
		
		return copy;
	}
	
	/**
	 * Assigns the /parent/ attribute to the children of this object
	 * dynamically.
	 * @param deep if true, recursively assigns parent attributes to ALL
	 * 			descendants, not just the direct children.
	 */
	private void assignParentPointers(boolean deep) {
		for(Spool child : children.values()) {
			child.parent = this;
			if(deep) {
				child.assignParentPointers(true);
			}
		}
	}
	
	/**
	 * Checks if this spool permits a thread in the context of a 
	 * lattice-learner. In other words, checks if the most specific
	 * hypo-class common between the spool and the thread is marked
	 * as permissible. If this object is empty, returns false by 
	 * default.
	 * @param t
	 * @return
	 */
	public boolean permits(Thread t) {
		if(thread == null) {
			return false;
		}
		
		// Get common thread
		Thread common = inCommon(t);
		
		// Get the spool corresponding to that thread
		Spool commonSpool = this.getByThread(common);
		if(commonSpool == null) {
			return false;
		}
		
		// Take the most specific class common and check if it's permissible
		return commonSpool.permissibleP;
	}
	
	/**
	 * Checks if this spool embodies a single string word. In particular,
	 * this method checks if any of the <i>terminal</i> nodes of this 
	 * subtree represents and permits the final type supplied as a
	 * string by the caller.
	 * @param s
	 * @return
	 */
	public boolean embodies(String s) {
		for(Spool term : this.getTerminals()) {
			if(term.thread.getType().equalsIgnoreCase(s) && term.permissibleP) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if this spool embodies a specified thread. In particular,
	 * this method checks if any of the <i>terminal</i> nodes of this 
	 * subtree exactly represents and permits the supplied thread.
	 * @param t
	 * @return
	 */
	public boolean embodies(Thread t) {
		for(Spool term : this.getTerminals()) {
			if(term.thread.equals(t) && term.permissibleP) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gives a simple, human-readable identifying string for this spool
	 * @return
	 */
	public String identifier() {
		return thread.get(thread.size() - 1);
	}
	
	/**
	 * Checks if this spool has any contents or not
	 */
	public boolean isEmpty() {
		return thread == null;
	}
	
	/**
	 * Get the set of all terminal spools in the subtree rooted at this node.
	 * @return
	 */
	public HashSet<Spool> getTerminals() {
		// Set of all nodes in this spool
		HashSet<Spool> allTerminals = new HashSet<>();
		Queue<Spool> agenda = new LinkedList<>();
		agenda.add(this);
		while(!agenda.isEmpty()) {
			Spool now = agenda.poll();
			if(now.isTerminal()) {
				allTerminals.add(now);
			}
			agenda.addAll(now.children.values());
		}
		return allTerminals;
	}
	
	/**
	 * Returns all descendants (including this object) of this subtree
	 * @return
	 */
	public HashSet<Spool> getDescendants() {
		HashSet<Spool> desc = new HashSet<>();
		desc.addAll(children.values());
		for(Spool child : children.values()) {
			desc.addAll(child.getDescendants());
		}
		return desc;
	}
	
	/**
	 * Returns an ordered ancestry of spools starting at the root of this 
	 * tree and ending at this node.
	 * @return
	 */
	public List<Spool> getAncestry() {
		ArrayList<Spool> lineage = new ArrayList<>();
		Spool current = this;
		lineage.add(0, current);
		while(current.parent != null) {
			current = current.parent;
			lineage.add(0, current);
		}
		
		return lineage;
	}
	
	/**
	 * Returns an ordered ancestry of spools starting at a particular
	 * ancestor and ending at this node, inclusive.
	 * @return
	 */
	public List<Spool> getAncestryFrom(Spool from) {
		ArrayList<Spool> lineage = new ArrayList<>();
		Spool current = this;
//		lineage.add(0, current);
		while(true) { // TODO make this not stupid
			lineage.add(0, current);
			current = current.parent;
			if(current.equals(from.parent)) {
				break;
			}
		}
		
		return lineage;
	}
	
	/**
	 * A human-readable mark illustrating the permissibility status
	 * of this node.
	 * @return
	 */
	public String mark() {
		String ch = "?";
		if(charge == Charge.POSITIVE) {
			ch = Symbol.PLUS;
		} else if(charge == Charge.NEGATIVE) {
			ch = Symbol.MINUS;
		} else if(permissibleP) {
			ch = Symbol.CHECK;
		} else if(!permissibleP) {
			ch = Symbol.X;
		}
		return ch;
	}
	
	public String printPermissibles() {
		StringBuilder sb = new StringBuilder(
				String.format("%s[%s] permissibles:", this.getClass().getSimpleName(), identifier()));
		HashSet<Spool> allNodes = this.getTerminals();
		for(Spool node : allNodes) {
			if(node.isTerminal()) {
				sb.append("\n       ");
				for(Spool ll : node.getAncestry()) {
					sb.append(ll.mark() + ll.identifier() + " ");
				}
			}
		}
		sb.append("\n<<<");
		return sb.toString();
	}
	
}
