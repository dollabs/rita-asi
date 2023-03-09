package memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import constants.RecognizedRepresentations;
import frames.entities.Entity;
import memory.XMem.XMem;
import utils.EntityUtils;

/**
 * Stores Things. Lots of complex linking going on.
 * 
 * @author sglidden
 *
 */
public class EntitySpace {

	private Map<Integer, Entity> things = new HashMap<Integer, Entity>();

	private Map<Entity, Entity> subThings = new HashMap<Entity, Entity>();
	private Map<Entity, Set<Entity>> superThings = new HashMap<Entity, Set<Entity>>();

	private Map<Integer, Set<Entity>> potentials = new HashMap<Integer, Set<Entity>>();

	private XMem xmem = new XMem();

	public synchronized void add(Entity tRaw) {
//		System.out.println("Processing "+tRaw.getID());
		// TODO: resolve this unwrap hack
		Entity t = RepProcessor.unwrap(tRaw);
		things.put(t.getID(), t);
		superThings.put(t, new HashSet<Entity>());
		// store possible subreps of t, linking if known
		for (Integer i : getSubIDs(t)) {
			if (things.containsKey(i)) {
				subThings.put(things.get(i), t);
				superThings.get(t).add(things.get(i));
			}
			else {
				if (potentials.containsKey(i)) {
					potentials.get(i).add(t);
				}
				else {
					Set<Entity> tset = new HashSet<Entity>();
					tset.add(t);
					potentials.put(i, tset);
				}
//				System.out.println("adding potential number: "+i+" for "+t.getID());

			}
		}
		// see if t has a known super-rep
		if (potentials.containsKey(t.getID())) {
			if (potentials.containsKey(t.getID())) {
				for (Entity superRep : potentials.get(t.getID())){
					subThings.put(t, superRep);
					superThings.get(superRep).add(t);
				}
			}
		}
//		// hack for Trajectories/Transitions
//		else if ((EntityUtils.getRepType(t) == RecognizedRepresentations.TRAJECTORY_THING ||
//				EntityUtils.getRepType(t) == RecognizedRepresentations.TRANSITION_THING)) {
//			int id = t.getElement(0).getElement(0).getID();
//			System.out.println("TRAJ/TRAN ID: "+id);
//			if (potentials.containsKey(id)) {
//				for (Thing superRep : potentials.get(id)){
//					System.out.println("[MEMORY] SUPER of "+id+": "+superRep.getID());
//					subThings.put(t, superRep);
//					superThings.get(superRep).add(t);
//				}
//			}
//		}

//		System.out.println("POTENTIALS: "+potentials.keySet());
		//		System.out.println("SUBTHINGS: "+subThings);
//		System.out.println("SUPERTHINGS: "+superThings);

		// see if we should add it to XMem
		// TODO: handle things other than causes
		if (EntityUtils.getRepType(t) == RecognizedRepresentations.CAUSE_THING) {
			processCause(t);
		}
		else if (subThings.get(t) != null &&
				EntityUtils.getRepType(subThings.get(t)) == RecognizedRepresentations.CAUSE_THING) {
			// is child of cause -- try to add everything now
			Entity cause = subThings.get(t);
			processCause(cause);
		}
	}
	
	private void processCause(Entity cause) {
		Set<Entity> subs = superThings.get(cause);
		Entity subject = cause.getSubject();
		Entity object = cause.getObject();
		Entity t1 = null;
		Entity t2 = null;
		for (Entity sub : subs) {
			if (sub.getID() == subject.getID()) {
				t1 = sub;
			}
			else if (sub.getID() == object.getID()) {
				t2 = sub;
			}
		}
		if (t1 != null && t2 != null) {
			xmem.add(t1, t2);
			if (Memory.DEBUG) System.out.println("[MEMORY] Added to XMem");
		}
	}

	/**
	 * Returns true if EntitySpace contains the exact Entity t.
	 * (Duplicates are not OK). Uses the Entity's ID number.
	 * 
	 * @param timer Entity
	 * @return true if EntitySpace contains a Entity t with id t.getID();
	 */
	public synchronized boolean contains(Entity thing) {
		// TODO: I really don't like this unwrap hack...
		Entity t = RepProcessor.unwrap(thing);
		return things.containsKey(t.getID());
	}
	
	public synchronized List<Entity> predict(Entity thing) {
		// tries to predict what would happen following a given Thing
		// TODO: I really don't like this unwrap hack...
		return xmem.predict(RepProcessor.unwrap(thing));
	}

	// gets the ids of all the Things inside t
	private Set<Integer> getSubIDs(Entity t) {
		Set<Integer> ids = new HashSet<Integer>();
		Set<Entity> children = t.getDescendants();
		for (Entity c : children) {
			ids.add(c.getID());
		}
		return ids;
	}
}
