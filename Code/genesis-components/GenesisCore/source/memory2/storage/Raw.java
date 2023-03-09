package memory2.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import frames.entities.Entity;
import memory2.datatypes.Chain;
import memory2.datatypes.ImmutableEntity;
import memory2.Methods;

/**
 * Provides storage for input to the memory.
 * No computation, only storage!
 * 
 * We want this to be thread safe.
 * 
 * @author sglidden
 *
 */
public class Raw {
	
	// NOTE you still need to synchronize manually when iterating over these datatypes!
	// see the JavaDocs
	
	// Stores the number of times we've seen some Thing
	private Map<ImmutableEntity, Integer> rawFreq = Collections.synchronizedMap(new HashMap<ImmutableEntity, Integer>());
	
	// Stores, for certain types of Things, all the Things that they are found in.
	// The current policy: as soon as someone does a context search, add the answer to the cache
	// and keep it updated when new Things are added. Never remove an entry from contextCache.
	private Map<ImmutableEntity, Set<ImmutableEntity>> contextCache = Collections.synchronizedMap(new HashMap<ImmutableEntity, Set<ImmutableEntity>>());
	
	
	public void add(Entity in) {
		ImmutableEntity t = new ImmutableEntity(in);
		// add to rawFreq
		if (rawFreq.containsKey(t)) {
			rawFreq.put(t, rawFreq.get(t) + 1);
		}
		else {
			rawFreq.put(t, 1);
		}
		// see if we need to add to contextCache
		List<Entity> subThings = Chain.flattenThing(in);
		for (Entity sub : subThings) {
			ImmutableEntity isub = new ImmutableEntity(sub);
			if (contextCache.containsKey(isub)) {
				contextCache.get(isub).add(t);
			}
		}
	}
	
	/**
	 * @param timer Thing
	 * @return int number of things t has been inputed to Raw
	 */
	public int frequency(Entity in) {
		ImmutableEntity t = new ImmutableEntity(in);
		Integer freq = rawFreq.get(t);
		if (freq == null) {
			return 0;
		}
		return (int) freq;
	}
	
	/**
	 * @param t Thing
	 * @return all Things which contain t
	 */
	public Set<Entity> getContext(Entity t) {
//		System.out.println("getting context of: "+t);
//		System.out.println("rawFreq: "+rawFreq);
		// if we are lucky, the answer is waiting in the contextCache
		ImmutableEntity ithing = new ImmutableEntity(t);
		if (contextCache.containsKey(ithing)) {
			return Methods.convertFromIthingSet(contextCache.get(ithing));
		}
		
		// otherwise, time to do a big painful search! and update contextCache.
		Set<ImmutableEntity> results = new HashSet<ImmutableEntity>();
		synchronized(rawFreq) {
			for (ImmutableEntity item : rawFreq.keySet()) {
				if (Methods.containsIthing(item, ithing)) {
					results.add(item);
				}
			}
		}
		contextCache.put(ithing, results);
		return Methods.convertFromIthingSet(results);
	}
}
