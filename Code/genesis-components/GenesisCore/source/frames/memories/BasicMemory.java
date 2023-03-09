// // Java run

package frames.memories;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Vector;

import connections.Wire;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.NameGenerator;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;
import utils.CollectionUtils;
import utils.logging.Level;
import utils.logging.Logger;

/**
 * Class for storing things. Generally used via wire mechanism, but if you wish,
 * you can call store method directly; for example, if you have a Thing instance
 * t, you can store it in memory m by calling m.store(t). Has read from file and
 * store in file methods. To store the contents of memory m in file my.data, use
 * m.storeInFile("my.data"). To read back that memory, clearing everying in m
 * first, use m.RestoreFromFile("my.data"). Alternatively, you can hook up the
 * wire mechanism and send in the file name via the STORE port, and then cause a
 * reset to the data in the file using the RESTORE port.
 * 
 * @author Patrick Winston and Mark Finlayson
 */
public class BasicMemory extends Observable implements Serializable, Observer, MemoryForget {
	private static BasicMemory staticMemory;

	private HashMap<String, ArrayList<Entity>> conceptHash = new HashMap<String, ArrayList<Entity>>();

	public static BasicMemory getStaticMemory() {
		if (staticMemory == null) {
			staticMemory = new BasicMemory();
			warning("Creating static memory: " + staticMemory);
		}
		return staticMemory;
	}

	public BasicMemory() {
		// Connections.getPorts(this).addSignalProcessor("storeInput");
	}

	// public void storeInput(Object object) {
	// if (object instanceof Thing) {
	// store((Thing) object);
	// }
	// }

	/*
	 * Adds a thing to memory.
	 */
	public void setInput(Object o, Object port) {
		if (!(o instanceof Entity)) {
			return;
		}
		if (Wire.INPUT.equals(port)) {
			store((Entity) o);
		}
		else if (BasicMemory.RECURSIVE.equals(port)) {
			storeRecursively((Entity) o);
		}
		else if (BasicMemory.STORE.equals(port)) {
			if (!(o instanceof String)) {
				Logger.warning(this, "Store port on memory expected a file name");
				return;
			}
			storeInFile((String) o);
		}
		else if (BasicMemory.RESTORE.equals(port)) {
			if (!(o instanceof String)) {
				Logger.warning(this, "Store port on memory expected a file name");
				return;
			}
			restoreFromFile((String) o);
		}
	}

	public BasicMemory(boolean notification) {
		System.out.println("Hello basic memory with argument");
		setNotification(notification);
	}

	/**
	 * Indicates if the state of some thing has been saved in preparation for a
	 * change in state.
	 */
	public static final Object SAVE_STATE_THING = new Object();

	/** Indicates if the state of some thing has changed */
	public static final Object FIRE_NOTIFICATION_THING = new Object();

	/**
	 * Indicates if the state of the memory has been saved in preparation for a
	 * change in state.
	 */
	public static final Object SAVE_STATE = new Object();

	/**
	 * Indicates if the state of the memory has changed by adding or deleting a
	 * Thing.
	 */
	public static final Object FIRE_NOTIFICATION = new Object();

	public static final int ACTION_STORE = 0;

	public static final int ACTION_FORGET = 0;

	Vector<Entity> things = new Vector();

	Hashtable instances = new Hashtable();

	public static Object RECURSIVE = "Recursive";

	public static Object STORE = "Store in file";

	public static Object RESTORE = "Restore from file";

	/*
	 * Dummy.
	 */
	public Object getOutput(Object o) {
		return null;
	}

	/**
	 * Returns a vector of all instances.
	 */
	public Vector<Entity> getThings() {
		return things;
	}

	/**
	 * Returns a vector of all instances of the specified type.
	 */
	public Vector getThings(String type) {
		Vector result = new Vector();
		Vector fodder = getThings();
		for (int i = 0; i < fodder.size(); ++i) {
			Entity thing = (Entity) (fodder.elementAt(i));
			if (thing.isA(type)) {
				result.add(thing);
			}
		}
		return result;
	}

	/**
	 * Returns a collection of things which are of the specified supertype.
	 * 
	 * @author M.A. Finlayson
	 * @since Mar 8, 2004; JDK 1.4.2
	 */
	public Vector getThingsOfSupertype(String supertype) {
		Vector result = new Vector();

		Vector fodder = getThings();
		for (int i = 0; i < fodder.size(); ++i) {
			Entity thing = (Entity) (fodder.elementAt(i));
			if (thing.getSupertype().equals(supertype)) {
				result.add(thing);
			}
		}

		info("Result is: " + result);
		return result;
	}

	/**
	 * Returns a hash table of all instances created.
	 */
	public Hashtable getInstances() {
		return instances;
	}

	/*
	 * Sets instance vector.
	 */
	public void setThings(Vector t) {
		saveState();
		things = t;
		fireNotification();
	}

	/*
	 * Sets instance table.
	 */
	public void setInstances(Hashtable t) {
		saveState();
		instances = t;
		fireNotification();
	}

	/*
	 * Gets state, for writing a serialized file.
	 */
	public Vector getState() {
		Vector v = new Vector();
		v.add(getInstances());
		v.add(getThings());
		return v;
	}

	/**
	 * Sets state, after reading vector from a serialized file.
	 */
	public void setState(Vector v) {
		if (v == null) {
			return;
		}
		saveState();
		try {
			setInstances((Hashtable) (v.get(0)));
			setThings((Vector) (v.get(1)));
		}
		catch (ClassCastException e) {
			warning("State not compatible.");
		}
		fireNotification();
	}

	public void thingModified(Entity t, String oldState, String newState) {
		if (!notification) {
			return;
		}
		// finest("\nThing modified from:\n-- " + oldState + "\n--(to:) " +
		// newState + "\n--");
		fireNotification();
	}

	/** @see java.util.Observer#update(java.util.Observable, java.lang.Object) */
	Entity lastChanged;

	public void update(Observable o, Object arg) {
		// saveState();
		lastChanged = (Entity) o;
		if (arg == Entity.SAVE_STATE) {
			changed(SAVE_STATE_THING);
		}
		else if (arg == Entity.FIRE_NOTIFICATION) {
			changed(FIRE_NOTIFICATION_THING);
		}
		// fireNotification();
	}

	public void changed() {
		changed(null);
	}

	public void changed(Object o) {
		setChanged();
		notifyObservers(o);
	}

	/**
	 * Used to suppress notification.
	 */
	boolean notification = false;

	/**
	 * Sets the notification flag.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 14, 2004; JDK 1.4.2
	 */
	public void setNotification(boolean b) {
		notification = b;
	}

	/**
	 * Sets the notification flag on this object, and all of the object's
	 * descendents, overriding any individual settings.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 15, 2004; JDK 1.4.2
	 */
	public void setNotificationOnDependents(boolean b) {
		setNotification(b);
		Vector v = getThings();
		for (int i = 0; i < v.size(); i++) {
			((Entity) v.get(i)).setNotificationOnDependents(b);
		}
	}

	public boolean getNotification() {
		return notification;
	}

	/**
	 * Saves the state associated with this Thing in string form
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 13, 2004; JDK 1.4.2
	 */
	public void saveState() {
		if (notification) {
			changed(SAVE_STATE);
		}
	}

	protected void fireNotification() {
		if (notification) {
			changed(FIRE_NOTIFICATION);
		}
	}

	protected void fireNotificationStore(Entity t) {
		if (notification) {
			fireNotification();
			changed(new ActionObject(t, ACTION_STORE));
		}
	}

	protected void fireNotificationForget(Entity t) {
		if (notification) {
			fireNotification();
			changed(new ActionObject(t, ACTION_FORGET));
		}
	}

	/**
	 * Stores thing instance.
	 */
	public boolean store(Entity t) {
		if (getInstances().get(t.getNameSuffix()) == null) {

			saveState();

			t.setNotificationOnDependents(getNotification());
			t.addObserver(this);
			getInstances().put(t.getNameSuffix(), t);
			getThings().add(t);

			fireNotificationStore(t);
			return true;
		}
		else {
			fine("Failed to store thing: " + t.getName());
			return false;
		}
	}

	/**
	 * Store thing and all its components.
	 */
	public void storeRecursively(Entity t) {
		fine("Storing thing recursively: " + t.getName());
		Vector superThings = new Vector();
		superThings.addAll(t.getSubjectOf());
		superThings.addAll(t.getObjectOf());
		superThings.addAll(t.getElementOf());
		Entity superThing;
		for (int i = 0; i < superThings.size(); i++) {
			superThing = (Entity) superThings.get(i);
			storeRecursively(superThing);
		}

		if (!store(t)) {
			return;
		}

		if (t.functionP()) {
			storeRecursively(t.getSubject());
		}
		else if (t.relationP()) {
			storeRecursively(t.getSubject());
			storeRecursively(t.getObject());
		}
		else if (t.sequenceP()) {
			Vector v = t.getElements();
			for (int i = 0; i < v.size(); ++i) {
				Entity element = (Entity) (v.get(i));
				storeRecursively(element);
			}
		}
	}

	/**
	 * Removes thing from memory.
	 */
	public boolean forget(Entity t) {
		if (isForgettable(t)) {
			saveState();
			// First, remove by suffix from instances table
			getInstances().remove(t.getNameSuffix());
			// Then, remove by object from things table
			getThings().remove(t);
			t.deleteObserver(this);
			fireNotificationForget(t);
			return true;
		}
		else {
			warning("Thing could not be forgotten:" + t.getName());
			return false;
		}
	}

	/**
	 * Removes the thing from memory and all of its decendants.
	 */
	public boolean forgetRecursively(Entity t) {
		if (t.functionP()) {
			forgetRecursively(t.getSubject());
		}
		else if (t.relationP()) {
			forgetRecursively(t.getSubject());
			forgetRecursively(t.getObject());
		}
		else if (t.sequenceP()) {
			Vector elements = new Vector();
			elements.addAll(t.getElements());
			for (int i = 0; i < elements.size(); i++) {
				forgetRecursively((Entity) elements.get(i));
			}
		}
		return forget(t);
	}

	boolean fascistForgetting = true;

	public void setFascistForgetting(boolean b) {
		fascistForgetting = b;
	}

	/**
	 * Checks to see if a thing can be forgotten. Does this by checking the
	 * subthings in all things in the things vector. Returns <b>true</b> if it
	 * finds a match.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 15, 2004; JDK 1.4.2
	 */
	public boolean isForgettable(Entity thing) {
		if (!fascistForgetting) {
			return true;
		}

		Entity parent;
		for (Iterator i = thing.getParents().iterator(); i.hasNext();) {
			parent = (Entity) i.next();
			if (getInstances().keySet().contains(parent.getNameSuffix())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Looks for a thing recursively in the structure of another thing.
	 * 
	 * @param g
	 *            The thing in which is looked for the other thing.
	 * @param t
	 *            The thing looked for.
	 * @return <code>true</code> if the thing is found, <code>false</code>
	 *         otherwise.
	 * @author M.A. Finlayson
	 * @since Jan 15, 2004; JDK 1.4.2
	 */
	public boolean findThingRecursively(Entity g, Entity t) {
		finest("Looking in and at " + g.getName() + " for " + t.getName());

		if (g == t) {
			finest("Found thing!");
			return true;
		}

		if (g.getClass() == Function.class) {
			return findThingRecursively(((Function) g).getSubject(), t);
		}
		else if (g.getClass() == Relation.class) {
			Relation r = (Relation) g;
			return findThingRecursively(r.getSubject(), t) || findThingRecursively(r.getObject(), t);
		}
		else if (g.getClass() == Sequence.class) {
			Vector elements = ((Sequence) g).getElements();
			for (int i = 0; i < elements.size(); i++) {
				if (findThingRecursively((Entity) elements.get(i), t)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Finds a thing in memory based on its ID number
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 15, 2004; JDK 1.4.2
	 */
	public Entity findThingInMemory(int id) {
		String suffix = "-" + id;
		return (Entity) getInstances().get(suffix);
	}

	/**
	 * Finds thing in memory from type of Thing created from string. Used mainly
	 * when reading stored xml files. ?????
	 */
	public Entity findThingInMemory(Entity thing) {
		String suffix = NameGenerator.extractSuffixFromName(thing.getType());
		if (suffix != null) {
			Object o = getInstances().get(suffix);
			// ... if so, just return it
			if (o != null) {
				return (Entity) o;
			}
		}
		return null;
	}

	/**
	 * Finds thing in memory from suffix of name. ?????
	 */
	public Entity findThingInMemory(String name) {
		String suffix = extractSuffixFromName(name);
		if (suffix != null) {
			Object o = getInstances().get(suffix);
			// ... if so, just return it
			if (o != null) {
				fine("Found in memory: " + name);
				return (Entity) o;
			}
			else {
				warning("Failed to find in memory: " + name);
			}
		}
		return null;
	}

	/**
	 * Returns only things from the Vector which are of the specified type.
	 * 
	 * @author M.A. Finlayson
	 * @since Mar 8, 2004; JDK 1.4.2
	 */
	public Vector getThingsOfType(String type, Vector things) {
		Vector result = new Vector();

		for (int i = 0; i < things.size(); ++i) {
			Entity thing = (Entity) (things.elementAt(i));
			if (thing.getType().equals(type)) {
				result.add(thing);
			}
		}
		return result;
	}

	/**
	 * Returns a collection of things which are of the specified supertype.
	 * 
	 * @author M.A. Finlayson
	 * @since Mar 8, 2004; JDK 1.4.2
	 */
	public List getThingsOfSupertype(String supertype, List things) {
		List result = new Vector();

		String suptype;
		for (int i = 0; i < things.size(); ++i) {
			Entity thing = (Entity) (things.get(i));
			suptype = thing.getSupertype();
			if (suptype != null) {
				if (suptype.equals(supertype)) {
					result.add(thing);
				}
			}
		}
		return result;
	}

	public Entity getReferenceX(Entity thing, String butNot) {
		return findMatchingThingX(thing, butNot);
	}

	/**
	 * Tries to find a reference, but not one belonging to specified class.
	 * Used, for example, in parsers, to find "the big red tree" when confronted
	 * with "the big tree." The exclusion class is need to prevent just finding
	 * the object viewed as a word.
	 */
	public Entity findMatchingThingX(Entity thing, String butNot) {
		fine("Looking for match to " + thing.getName());
		// For now, only look for Thing instances, not instances of other
		// classes...dangerours
		if (!thing.entityP()) {
			fine("...but not thing ");
			return null;
		}
		// Also, for now, better be a tangible thing...
		// No, because won't match objects it doesn't know about
		if (false && !thing.isA("tangibleThing")) {
			fine("...but not tangible thing ");
			return null;
		}
		Collection theseTypes = thing.getAllTypesForFindMatchingThing();
		Vector possibilities = fetchThings(thing.getType());
		possibilities.remove(this);
		if (possibilities.isEmpty()) {
			return null;
		}
		int matches = 0;
		Entity result = null;
		for (int i = 0; i < possibilities.size(); ++i) {
			Entity possibility = (Entity) (possibilities.elementAt(i));
			Collection thoseTypes = possibility.getAllTypesForFindMatchingThing();
			Collection intersection = CollectionUtils.intersection(theseTypes, thoseTypes);
			Collection difference = CollectionUtils.difference(theseTypes, thoseTypes);
			int newMatches = intersection.size();
			if (butNot != null && possibility.isA(butNot)) {
				// System.out.println("Will not match " +
				// possibility.getName());
			}
			// Do not match a clone
			else if (possibility.isA("clone")) {
			}
			else if (possibility instanceof Function || possibility instanceof Sequence) {
				// System.out.println("Will not match " +
				// possibility.getName());
			}
			else {
				if (!difference.isEmpty()) {
					// System.out.println("Imperfect match---following
					// unmatched: " + difference);
				}
				else if (newMatches > matches) {
					result = possibility;
					matches = newMatches;
					// System.out.println("Match improved!");
				}
				else if (newMatches == matches) {
					// System.out.println("Ambiguous match! " + result.getName()
					// + " and " + possibility.getName() + "---use most
					// recent:\n" + possibility);
					result = possibility;
					matches = newMatches;
				}
			}
		}
		if (result != null) {
			fine("Looks like " + thing.getName() + " best matches " + result.getName());
			fine("Current: " + this);
			fine("Antecedant: " + result);
		}
		return result;
	}

	/**
	 * Extends threads of given individual via other threads found in memory.
	 * Enormously hairy, and probably needs to be thought through again
	 * carefully. Example: thing pastVerb flew is extended by thread in memory
	 * thing word verb pastVerb becoming thing word verb pastVerb flew The "via"
	 * argument means that only extensions through threads containing that class
	 * are allowed; example would be extension through "word." All threads are
	 * extended, then all redundant threads are pruned out, where redundant
	 * means that one thread has the same sequence as the bottom of another
	 * thread. Example: thing word verb pastVerb, thing verb pastVerb are
	 * reduced to just thing word verb pastVerb
	 */
	public void extendVia(Entity thing, String via) {
		// System.out.println("Extending " + getName() + " via " + via);
		Thread thread = thing.getPrimedThread();

		// Nothing to do if the thread has no elements
		if (thread.size() == 0) {
			return;
		}

		// Nothing to do if first class on the thread is not the thing class
		// (e.g., may be the 'word' class -- MAF.7.Jan.04)
		if (!((String) (thread.elementAt(0))).equalsIgnoreCase("thing")) {
			return;
		}

		// Nothing to do if the only class on the thread is the thing class
		if (thread.size() == 1) {
			return;
		}

		// Get the hook.
		String hook = null;
		// Hook must be first element
		// (but the following line is redundant, since if the thread.size() == 1
		// we would
		// have returned already via the previous call -- MAF.7.Jan.04)
		if (thread.size() == 1) {
			hook = (String) (thread.elementAt(0));
		}
		// Hook must be second element (in fact, the hook will always be the
		// second element -- MAF.7.Jan.04)
		else {
			hook = (String) (thread.elementAt(1));
		}
		Logger.getLogger("extender").fine("Hook is " + hook);
		Logger.getLogger("extender").fine("Total thing instances: " + getThings().size());

		// Now get all the threads of all things which are a member of the 'via'
		// class,
		// and have 'hook' as the last item of their primed thread. --
		// MAF.7.Jan.04
		Vector goodThings = fetchThings(hook, via);

		Logger.getLogger("extender").fine("Thing instances belonging to via class: " + goodThings.size());
		Vector threads = new Vector();
		Entity goodThing;
		for (int i = 0; i < goodThings.size(); ++i) {
			goodThing = (Entity) goodThings.get(i);
			threads.addAll(goodThing.getBundle());
		}

		// Keep all threads which contain the 'via' class and also
		// start with 'thing'. Seems like you might get some threads which do
		// not contain the 'hook' class, though. May be an error. --
		// MAF.7.Jan.04
		Logger.getLogger("extender").fine("Total threads: " + threads.size());
		Vector goodThreads = new Vector();
		for (int i = 0; i < threads.size(); ++i) {
			Thread candidateThread = (Thread) (threads.elementAt(i));
			Logger.getLogger("extender").fine("Candidate: " + candidateThread);
			// Candidate's first element is 'thing' and candidate contains via
			if (!candidateThread.isEmpty()) {
				if (((String) (candidateThread.firstElement())).equalsIgnoreCase("thing") && candidateThread.contains(via)) {
					goodThreads.add(candidateThread);
				}
			}
		}

		Logger.getLogger("extender").fine("Threads containing via class: " + threads.size());

		// Extend the primary thread in all ways made possible by the threads in
		// the goodThread vector.
		// -- MAF.7.Jan.04
		for (int i = 0; i < goodThreads.size(); ++i) {
			Thread extendingThread = (Thread) (goodThreads.elementAt(i));

			// Clone the primary thread
			Thread newThread = (Thread) (thread.copyThread());

			// Decide where to start adding stuff; depends on whether new thread
			// has thing class or not
			int insertionIndex = 0;
			if (((String) (newThread.elementAt(0))).equalsIgnoreCase("thing")) {
				insertionIndex = 1;
			}

			// Decide where to start adding from; depends on whether extending
			// thread has thing class or not
			int sourceIndex = 0;
			if (((String) (extendingThread.elementAt(0))).equalsIgnoreCase("thing")) {
				sourceIndex = 1;
			}

			// Insert and add to thread bundle
			if (extendingThread.size() - 2 >= sourceIndex) {
				for (int j = extendingThread.size() - 2; j >= sourceIndex; --j) {
					newThread.add(insertionIndex, extendingThread.elementAt(j));
				}
				thing.getBundle().addThread(newThread);
			}
		}

		// Prune redundant threads
		thing.getBundle().prune();
	}

	/**
	 * Fetches things indexed by key. Hack: for now, no hashing.
	 */
	protected Vector<Entity> fetchThings(String key) {
		return fetchThings(key, "thing");
	}

	/**
	 * Fetches things indexed by key inheriting from via class. Hack: for now,
	 * no hashing. ?????
	 */
	protected Vector<Entity> fetchThings(String hook, String via) {
		Vector<Entity> vector = getThings();
		Vector<Entity> result = new Vector<Entity>();
		Bundle threads;
		Thread thread;
		for (int i = 0; i < vector.size(); ++i) {
			Entity thing = (Entity) (vector.elementAt(i));
			// Why do you check only type on primed thread (getType()) but
			// look for via on all threads (isA())? May be mistake. Seems you
			// rather
			// want to find all things which have at least one thread which
			// contains
			// both the key and the via, and furthermore the relationship
			// "... -> via -> ... -> key -> ..." holds. May be an error. --
			// MAF.7.Jan.04
			// Making this function look at all threads for hook class, not just
			// primed
			// if (hook.equalsIgnoreCase(thing.getType()) && thing.isA(via)) {
			// if (thing.getClass().equals(Thing.class)) {
			// result.add(thing);
			// }
			if (!thing.entityP()) {
				continue;
			}
			threads = thing.getBundle();
			for (int j = 0; j < threads.size(); j++) {
				thread = (Thread) threads.get(j);
				if (thread != null && !thread.isEmpty() && hook.equalsIgnoreCase((String) thread.lastElement()) && thing.isA(via)) {
					result.add(thing);
				}
			}
		}
		return result;
	}

	/**
	 * Ported from Thing class. -- MAF.07.Maf.04 Finds thing in memory from type
	 * of Thing created from string. Used mainly when reading stored xml files.
	 */
	public Entity getReference(String name) {
		return findThingInMemory(name);
	}

	/**
	 * This method finds the largest ID number that is being used in Thing
	 * memory.
	 * 
	 * @return the largest ID in the Thing memory
	 * @author Mark Finlayson, Jan 8, 2004
	 */
	public int getLargestID() {
		int result = 0;
		Entity thing;
		for (int i = 0; i < getThings().size(); i++) {
			thing = (Entity) getThings().get(i);
			result = Math.max(result, thing.getID());
		}
		return result;
	}

	/**
	 * Ported from Thing class.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 25, 2004; JDK 1.4.2
	 */
	protected static String extractSuffixFromName(String name) {
		int index = name.lastIndexOf('-');
		if (index >= 0) {
			return (name.substring(index));
		}
		return null;
	}

	/*
	 * Remove all things from memory.
	 */
	public void clear() {
		saveState();
		getInstances().clear();
		getThings().clear();
		NameGenerator.clearNameMemory();
		fireNotification();
	}

	/*
	 * See class comment for explanation; see main for example.
	 */
	public void storeInFile(String file) {
		try {
			FileOutputStream f = new FileOutputStream(file);
			ObjectOutputStream o = new ObjectOutputStream(f);
			o.writeObject(getState());
			o.close();
			f.close();
			Logger.info(this, "Wrote serialized file " + file);
		}
		catch (Exception f) {
			Logger.warning(this, "Encountered exception while writing serialized data file");
			f.printStackTrace();
		}
	}

	/*
	 * See class comment for explanation; see main for example.
	 */
	public void restoreFromFile(String file) {
		try {
			FileInputStream stream = new FileInputStream(file);
			ObjectInputStream o = new ObjectInputStream(stream);
			Vector v = (Vector) (o.readObject());
			setState(v);
			o.close();
			NameGenerator.setNameMemory(getLargestID() + 1);
		}
		catch (Exception f) {
			Logger.warning(this, "Encountered exception while writing serialized data file");
			f.printStackTrace();
		}
	}

	public class ActionObject {

		final Object object;

		final int action;

		public ActionObject(Object o, int a) {
			object = o;
			action = a;
		}

		public Object getObject() {
			return object;
		}

		public int getAction() {
			return action;
		}

	}

	public String fetchAndShow(String key) {
		return showThings(fetchThings(key));
	}

	public String contentsAsStrings() {
		return showThings(getThings());
	}

	public String showThings(Vector<Entity> v) {
		String result = "";
		for (Entity t : v) {
			result += "\nThing: " + t.asString();
		}
		return result;
	}

	public static void main(String arv[]) {

		getLogger().setLevel(Level.All);
		boolean f = false;
		BasicMemory.getStaticMemory().setNotificationOnDependents(f);
		System.out.println("Notification switch:" + f);
		Entity t1 = new Entity("man");
		t1.addType("Patrick");
		Entity t2 = new Entity("boy");
		t2.addType("Mark");
		BasicMemory m = BasicMemory.getStaticMemory();
		EntityMemory m1 = new EntityMemory();

		Relation r1 = new Relation("friend", t1, t2);

		m.storeRecursively(r1);

		System.out.println("--------------Testing Memory/Thing memberships -- MAF.14.Jan.04");
		System.out.println("Thing we are manipulating is as follows: " + r1.asString());
		System.out.println("\nContents of Static Memory: " + m.contentsAsStrings());
		System.out.println("\nContents of First Memory: " + m1.contentsAsStrings());

		System.out.println("Patrick: " + t1);
		System.out.println("Mark: " + t2);

		System.out.println("\nMoving thing from static to first...");
		m1.store(t1);

		System.out.println("\nContents of Static Memory: " + m.contentsAsStrings());
		System.out.println("\nContents of First Memory: " + m1.contentsAsStrings());

		System.out.println("Man:" + m.fetchAndShow("man"));

		System.out.println("Boy:" + m.fetchAndShow("boy"));

		System.out.println("Patrick:" + m.fetchAndShow("Patrick"));

		System.out.println("Mark:" + m.fetchAndShow("Mark"));

		// m.storeInFile("foo.bar");
		// Logger.info("Logger", m.contentsAsStrings());
		// m.clear();
		// Logger.info("Logger", m.contentsAsStrings());
		// m.restoreFromFile("foo.bar");
		// Logger.info("Logger", m.contentsAsStrings());

	}

	// Debugging section
	public static final String LOGGER_GROUP = "memory";

	public static final String LOGGER_INSTANCE = "BasicMemory";

	public static final String LOGGER = LOGGER_GROUP + "." + LOGGER_INSTANCE;

	public static Logger getLogger() {
		return Logger.getLogger(LOGGER);
	}

	protected static void finest(Object s) {
		Logger.getLogger(LOGGER).finest(LOGGER_INSTANCE + ": " + s);
	}

	protected static void finer(Object s) {
		Logger.getLogger(LOGGER).finer(LOGGER_INSTANCE + ": " + s);
	}

	protected static void fine(Object s) {
		Logger.getLogger(LOGGER).fine(LOGGER_INSTANCE + ": " + s);
	}

	protected static void config(Object s) {
		Logger.getLogger(LOGGER).config(LOGGER_INSTANCE + ": " + s);
	}

	protected static void info(Object s) {
		Logger.getLogger(LOGGER).info(LOGGER_INSTANCE + ": " + s);
	}

	protected static void warning(Object s) {
		Logger.getLogger(LOGGER).warning(LOGGER_INSTANCE + ": " + s);
		// throw new RuntimeException("Track this!");
	}

	protected static void severe(Object s) {
		Logger.getLogger(LOGGER).severe(LOGGER_INSTANCE + ": " + s);
	}

	public String getName() {
		return "Basic Memory";
	}

	/**
	 * Store thing and its constituent things.
	 */
	public void storeConcept(Entity t) {
		if (!t.entityP()) {
			store(t);
		}
		ArrayList<Entity> recorded = new ArrayList<Entity>();
		storeConcept(t, t, recorded);
	}

	public void storeConcept(Entity t, Entity parent, ArrayList<Entity> recorded) {
		// Vector superThings = new Vector();
		// superThings.addAll(t.getSubjectOf());
		// superThings.addAll(t.getObjectOf());
		// superThings.addAll(t.getElementOf());
		// Thing superThing;
		// for (int i = 0; i < superThings.size(); i++) {
		// superThing = (Thing) superThings.get(i);
		// storeConcept(superThing, parent);
		// }
		//
		// if (!store(t)) {
		// return;
		// }
		if (recorded.contains(t)) {
			// Ignore
		}
		else if (t.entityP()) {
			hashConcept(t, parent);
			recorded.add(t);
		}
		else if (t.functionP()) {
			storeConcept(t.getSubject(), parent, recorded);
		}
		else if (t.relationP()) {
			storeConcept(t.getSubject(), parent, recorded);
			storeConcept(t.getObject(), parent, recorded);
		}
		else if (t.sequenceP()) {
			Vector<Entity> v = t.getElements();
			for (int i = 0; i < v.size(); ++i) {
				Entity element = (Entity) (v.get(i));
				storeConcept(element, parent, recorded);
			}
		}
	}

	public ArrayList<Entity> retrieveConcept(Entity t) {
		String type = t.getType();
		ArrayList current = conceptHash.get(type);
		if (current == null) {
			current = new ArrayList<Entity>();
			conceptHash.put(type, current);
		}
		return current;
	}
	

	private void hashConcept(Entity t, Entity parent) {
		String type = t.getType();
		ArrayList current = conceptHash.get(type);
		if (current == null) {
			current = new ArrayList<Entity>();
			conceptHash.put(type, current);
		}
		current.add(parent);

	}

	public Set<String> getTypes(){
		return conceptHash.keySet();
	}
	
	public ArrayList<Entity> retrieveConceptByType(String type){
		ArrayList current = conceptHash.get(type);
		if (current == null) {
			current = new ArrayList<Entity>();
			conceptHash.put(type, current);
		}
		return current;
	}
}
