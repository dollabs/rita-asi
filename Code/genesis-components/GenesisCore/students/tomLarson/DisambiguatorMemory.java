package tomLarson;

import java.util.Map;

import frames.entities.Thread;

import java.util.HashMap;
/**
 * This is where ThreadTrees for all of the verbs seen so far are stored. 
 * @author Thomas
 *
 */
@SuppressWarnings("rawtypes")
public class DisambiguatorMemory {
	
	
	private Map<String, ThreadTree> map; 
	
	/**
	 * Create a new DisambiguatorMemory
	 *
	 */
	public DisambiguatorMemory() {
		map = new HashMap<String, ThreadTree>();
	}
	
	
	public boolean containsVerb(String verb) {
		return map.keySet().contains(verb.toLowerCase());
	}
	
	/**
	 * Adds t to the ThreadTree associated with verb. 
	 * If verb is not in the memory, it is added
	 * @param verb
	 * @param t
	 * @return
	 */
	public void addThread(String verb, Thread t) {
		verb = verb.toLowerCase();
		if (containsVerb(verb)) {
			map.get(verb).addThread(t);
		}
		else {
			map.put(verb, ThreadTree.makeThreadTree(t));
		}
	}
	
	public ThreadTree getThreadTree(String verb) {
		verb = verb.toLowerCase();
		if (containsVerb(verb)) {
			return map.get(verb);
		}
		else { return new ThreadTree();}
	}
	
	
	
	
}
