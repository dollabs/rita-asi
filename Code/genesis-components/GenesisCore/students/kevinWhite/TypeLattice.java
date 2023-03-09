package kevinWhite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import frames.entities.Thread;

/**
 *@author Michael Klein
 * The actual lattice data structure
 *
 */

public class TypeLattice extends Lattice<String> {

    private Map<String, Set<String>> ancestry = new HashMap<String, Set<String>>();

    /**
     * Constructor 
     * 
     * @param threads
     */
    public TypeLattice(Iterable<Thread> threads) {
    	// First build the graph
    	for (Thread th : threads) {
    		updateAncestry(th);
        }

    	// Then reduce it to the transitive reduction
    	// TODO: fill in if useful / necessary later
    }
    
    /**
     * constructor for additional method testing
     */
    public TypeLattice(){}
    
    
    /**
     * Return the parents of the this node.
     * If the node does not have an ancestry, return new HashSet<String>
     */
    public Set<String> getParents(String node) {
    	return ancestry.containsKey(node) ? ancestry.get(node) : new HashSet<String>();
    }

    /**
     * Perform a comparison whether a node is a children of an ancestor
     */
    public boolean leq(String node, String ancestor) {
    	if (node.equals(ancestor)) {
            return true;
        } else {
        	for (String parent : getParents(node)) {
        		if (leq(parent, ancestor)) {
        			return true;
        		}
        	}
            return false;
        }
    }

    /**
     * Input: a thread
     * Iterate over all nodes in a thread
     * and build up the ancestry data structure 
     * @param th
     */
    public void updateAncestry(Thread th){
        String parent = th.get(0);
        int threadSize = th.size();
        for (int index = 1; index < threadSize; index++) {
        	String child = th.get(index);
        	if (! leq(child, parent)) {
        		// Only add an edge if necessary
        		Set<String> parents = getParents(child);
        		parents.add(parent);
        		ancestry.put(child, parents);
            }
        	parent = child;
        }
    }
    
    /**
     * Perform topological sort
     * @return
     */
    public List<Set<String>> topologicalSort() {
    	// Find all the nodes in the graph
        Set<String> nodes = new HashSet<String>();
        for(String child : ancestry.keySet()) {
        	nodes.add(child);
            for (String parent : getParents(child)) {
            	nodes.add(parent);
            }
        }
        return sort(nodes, new HashSet<String>());
    }

    /**
     * The actual sort functionality
     * 
     * @param nodes
     * @param done
     * @return
     */
    private List<Set<String>> sort(Set<String> nodes, Set<String> done) {
        List<Set<String>> result = new Vector<Set<String>>();

        if (nodes.isEmpty()) {
            return result;
        }

        Set<String> this_level = new HashSet<String>();
        // Iterate through all the nodes
        // For each node
        // If it does not has any ancestor or all of its ancestors have already been sort
        // then it belongs to this levle
        for(String node : nodes) {
        	Set<String> ancestors = new HashSet<String>();
        	ancestors.addAll(getParents(node));
        	ancestors.removeAll(done);
        	
            if (ancestors.isEmpty()) {
                this_level.add(node);
            }
        }

        done.addAll(this_level);
        nodes.removeAll(this_level);

        // First, add this level
        result.add(this_level);
        // Add the remaining
        result.addAll(sort(nodes, done));
        
        return result;
    }    
}
