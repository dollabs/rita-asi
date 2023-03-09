//// Java

package frames.memories;

import utils.logging.Logger;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
//import java.util.Map;
//import java.util.Set;
//import java.util.TreeMap;
import java.util.Vector;

import frames.entities.ClassPair;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Thread;

/**
 * @author Patrick Winston
 */

/*
 * Edited on 8 July 2013 by ahd
 */

public class EntityMemory extends BasicMemory {
	
	/** Maintains counts of classes in the memory.  Simple implementation right now.
	 * Would like to make it a dependent probability table in future.
	 * @author M.A. Finlayson
	 * @since Jan 16, 2004; JDK 1.4.2
	 */
	Hashtable classCounts = new Hashtable();
	
	/** Maintains counts of direct subclasses of classes in the memory. 
	 * classDivisionCount is a table of the number of direct subclasses of each class.
	 * If a class is not in the table, it has zero subclasses. 
	 * @author M.A. Finlayson
	 * @since Jan 16, 2004; JDK 1.4.2
	 */
	Hashtable classDivisionCount = new Hashtable();
	
	/** Used to maintain classHomogeneities, so the counter knows when
	 * to completely remove a subclass from the table.
	 * @author M.A. Finlayson
	 * @since Jan 17, 2004; JDK 1.4.2
	 */
	Hashtable classPairCounts = new Hashtable();
	
	double classCountAverage = 0;
	double classCountDeviation = 0;

	public Hashtable getClassCounts(){
		return classCounts;
	}
	
	public Hashtable getClassDivisionCount(){
		return classDivisionCount;
	}
	
	public Hashtable getClassPairCounts(){
		return classPairCounts;
	}
	
	public void setAverage(double d){
		classCountAverage = d;
	}
	
	public double getAverage(){
		if(avgChanged){
			calculateAverage();
		}
		return classCountAverage;
	}
	
	public double getDeviation(){
		if(devChanged){
			calculateDeviation();
		}
		return classCountDeviation;
	}
	
	public void setDeviation(double d){
		classCountDeviation = d;
	}
	
	
	/** Returns the count of the specified class.
	 * @author M.A. Finlayson
	 * @since Jan 16, 2004; JDK 1.4.2
	 */
	public int getClassCount(String c){
		Integer count = (Integer)classCounts.get(c);
		if(count == null){
			return 0;	
		} else {
			return count.intValue();
		}
	}
	
	public void refresh(){
		classCounts = new Hashtable();
		classDivisionCount = new Hashtable();
		classPairCounts = new Hashtable();
		
		Entity thing;
		Vector things = getThings();
		for(int i = 0; i < things.size(); i++){
			thing = (Entity)things.get(i);
			thingModified(thing, null, thing.toString());
		}
		
		calculateAverage();
		calculateDeviation();
	}
	
	boolean avgChanged = false;
	boolean devChanged = false;
	/** Triggers an update of the frequency table.
	 * @param t Thing that has been modified.
	 * @param oldState String representation of old state of thing.
	 * @param newSTate String representation of new state of thing.
	 * @author M.A. Finlayson
	 * @since Jan 16, 2004; JDK 1.4.2
	 * @see frames.memories.BasicMemory#thingModified(frames.entities.Entity, java.lang.String, java.lang.String) thingModified
	 */
	public void thingModified(Entity t, String oldState, String newState){
		avgChanged = true;
		devChanged = true;
		
		super.thingModified(t, oldState, newState);
		Vector remove = Entity.getClassesFromString(oldState);
		Vector add = Entity.getClassesFromString(newState);
		
		Vector intersection = new Vector();
		intersection.addAll(remove);
		intersection.retainAll(add);
		
		remove.removeAll(intersection);
		add.removeAll(intersection);
		incrementClassCounts(add);
		decrementClassCounts(remove);
		
		remove = Thread.getClassPairsFromString(oldState);
		add = Thread.getClassPairsFromString(newState);
		
		intersection.clear();
		intersection.addAll(remove);
		intersection.retainAll(add);
		
		remove.removeAll(intersection);
		add.removeAll(intersection);
		addClassPairs(add);
		removeClassPairs(remove);
	}
	
	/** Updates class pair counts. 
	 * @param v Vector of class pairs to be added.
	 * @author M.A. Finlayson 
	 * @since Jan 17, 2004; JDK 1.4.2
	 */
	public void addClassPairs(Vector v){
		String cp;
		Integer pairCount;
		Integer homoCount;
		Hashtable cpc = getClassPairCounts();
		Hashtable cdc = getClassDivisionCount();
		for(int i = 0; i < v.size(); i++){
			cp = (String)v.get(i);
			pairCount = (Integer)cpc.get(cp);
			homoCount = (Integer)cdc.get(ClassPair.getUpper(cp));
			if(pairCount == null && homoCount == null){
				cdc.put(ClassPair.getUpper(cp), new Integer(1));
				cpc.put(cp, new Integer(1));
			} else if(pairCount == null && homoCount != null){
				cdc.put(ClassPair.getUpper(cp), new Integer(homoCount.intValue() + 1));
				cpc.put(cp, new Integer(1));
			} else {
				cpc.put(cp, new Integer(pairCount.intValue() + 1));
			}
		}
	}
	
	/** Updates class pair counts.
	 * @param v Vector of class pairs to be removed.
	 * @author M.A. Finlayson 
	 * @since Jan 17, 2004; JDK 1.4.2
	 */
	public void removeClassPairs(Vector v){
		String cp;
		Integer pairCount;
		Integer homoCount;
		Hashtable cpc = getClassPairCounts();
		Hashtable cdc = getClassDivisionCount();
		for(int i = 0; i < v.size(); i++){
			cp = (String)v.get(i);
			pairCount = (Integer)cpc.get(cp);
			homoCount = (Integer)cdc.get(ClassPair.getUpper(cp));
			if(pairCount.intValue() == 1 && homoCount.intValue() == 1){
				cdc.remove(ClassPair.getUpper(cp));
				cpc.remove(cp);
			} else if(pairCount.intValue() == 1 && homoCount != null){
				cdc.put(ClassPair.getUpper(cp), new Integer(homoCount.intValue() - 1));
				cpc.remove(cp);
			} else if(pairCount != null && homoCount != null){
				cpc.put(cp, new Integer(pairCount.intValue() - 1));
			} 
		}
	}
	
	/** Takes a vector of strings and increments their counts by one in the 
	 * class count table.  If they don't exist in there, it adds them.
	 * @author M.A. Finlayson
	 * @since Jan 16, 2004; JDK 1.4.2
	 */
	public void incrementClassCounts(Vector v){
		String key;
		Integer count;
		Hashtable cc = getClassCounts();
		for(int i = 0; i < v.size(); i++){
			key = (String)v.get(i);
			count = (Integer)cc.get(key);
			if(count == null){
				classCounts.put(key, new Integer(1));
			} else {
				classCounts.put(key, new Integer(count.intValue() + 1));
			}
		}
	}
	
	/** Takes a vector of strings and decrements their counts by one in the 
	 * class count table.  If the count goes to zero, elminates the class from the table
	 * @author M.A. Finlayson
	 * @since Jan 16, 2004; JDK 1.4.2
	 */
	public void decrementClassCounts(Vector v){
		String key;
		Integer count;
		int c;
		for(int i = 0; i < v.size(); i++){
			key = (String)v.get(i);
			count = (Integer)classCounts.get(key);
			if(count == null){
				// don't do anything if the class isn't in the table
			} else {
				c = count.intValue() - 1;
				if(c == 0){
					classCounts.remove(key);
				} else {
					classCounts.put(key, new Integer(c));
				}
			}
		}
	}
	
	/** Calculates the average class count.
	 * @author M.A. Finlayson
	 * @since Jan 16, 2004; JDK 1.4.2
	 */
	public double calculateAverage(){
		Collection c = getClassCounts().values();
		double sum = 0;
		for(Iterator i = c.iterator(); i.hasNext(); ){
			sum = sum + ((Integer)i.next()).intValue();
		}
		double result = sum/c.size();
		setAverage(result);
		avgChanged = false;
		return result;
	}
	
	/** Calculates the deviation of the class count.
	 * @author M.A. Finlayson
	 * @since Jan 16, 2004; JDK 1.4.2
	 */
	public double calculateDeviation(){
		double average = getAverage();
		Collection c = getClassCounts().values();
		double sum = 0;
		for(Iterator i = c.iterator(); i.hasNext(); ){
			sum = sum + Math.pow((((Integer)i.next()).intValue() - average), 2);
		}
		double variance = sum/c.size();
		double result = Math.sqrt(variance);
		setDeviation(result);
		devChanged = false;
		return result;
	}
	
	
	/** Indictes a change has occurred in the memory.
	 * @author M.A. Finlayson
	 * @since Jan 16, 2004; JDK 1.4.2
	 * @see frames.memories.BasicMemory#fireNotification() fireNotification
	 */
	public void fireNotification(){
		super.fireNotification();
		finest("Memory fireNotification() triggered.");
	}

	/** Same as overriden method except calls thingModified()
	 * @author M.A. Finlayson
	 * @since Jan 16, 2004; JDK 1.4.2
	 * @see frames.memories.BasicMemory#store(frames.entities.Entity) store
	 */
	public boolean store(Entity t){
		boolean b = super.store(t);
		if(b){
			thingModified(t, "", t.toXMLSansName(Entity.defaultToXMLIsCompact));
		}
		return b;
	}
	
	/** Same as overriden method except calls thingModified(); 
	 * @author M.A. Finlayson
	 * @since Jan 16, 2004; JDK 1.4.2
	 * @see frames.memories.BasicMemory#forget(frames.entities.Entity) forget
	 */
	public boolean forget(Entity t){
		boolean b = super.forget(t);
		if(b){
			thingModified(t, t.toXMLSansName(Entity.defaultToXMLIsCompact), "");
		}
		return b;
	}
	
	/** Clears the state of the memory. 
	 * @author M.A. Finlayson
	 * @since Jan 16, 2004; JDK 1.4.2
	 * @see frames.memories.BasicMemory#clear() clear
	 */
	public void clear(){
		super.clear();
		classCounts.clear();
	}

	/** Tests behavior.
	 * @author M.A. Finlayson
	 * @since Jan 16, 2004; JDK 1.4.2
	 * @see frames.memories.BasicMemory#main(java.lang.String[]) main
	 */
	public static void main(String arv[]){
		Entity t1 = new Entity("Mark");
		t1.addType("parking");
		t1.addType("god");
		Entity t2 = new Entity("Steph");
		t2.addType("parking");
		t2.addType("dunce");
		Relation r1 = new Relation("siblings", t1, t2);
		r1.addType("related");
		r1.addType("fraternal");
		r1.addType("zygotes");
		Thread d1 = new Thread();
		d1.addType("related");
		d1.addType("fraternal");
		d1.addType("twins");
		Thread d2 = new Thread();
		d2.addType("features");
		d2.addType("the");
		r1.addThread(d2);
		r1.addThread(d1);
		
		System.out.println("\nRelation is: " + r1);
//		String oldState = r1.toStringSansName(Thing.defaultToStringIsCompact);
//		r1.addType("brother");
//		System.out.println("\nRelation now is: " + r1);
//		String newState = r1.toStringSansName(Thing.defaultToStringIsCompact);
		
		EntityMemory m1 = new EntityMemory();
		m1.store(r1);
		m1.store(t1);
		m1.store(t2);		
		System.out.println("\nClass counts: " + m1.getClassCounts());
		System.out.println("\nPair counts: " + m1.getClassPairCounts());
		System.out.println("\nHomogeneity counts: " + m1.getClassDivisionCount());
		
		m1.forget(t2);
		System.out.println("\n\nClass counts: " + m1.getClassCounts());
		System.out.println("\nPair counts: " + m1.getClassPairCounts());
		System.out.println("\nHomogeneity counts: " + m1.getClassDivisionCount());
//		m1.forget(r1);
//		System.out.println("\n(Forgetting) Class counts: " + m1.getClassCounts());
		
//		System.out.println("\nNow for the real test...");
//		BridgeSpeak parser = new BridgeSpeak();
//		info("Reading background files.");
//		parser.setChangeMode(false);
//		String [] files = {"wordk.txt", "classk.txt"};
//		parser.readFiles(files);
//		parser.setChangeMode(true);
//		
//		System.out.println("\nAdding things to a countable memory...");
//		Memory m1 = new Memory();
//		Vector things = new Vector();
//		things.addAll(Thing.getStaticMemory().getThings());
//		for(int i = 0; i < things.size(); i++){
//			m1.store((Thing)things.get(i));
//		}
//		
//		System.out.println("What do we have here?");
//		System.out.println("Memory size: " + m1.getThings().size());
//		System.out.println("Average of count: " + m1.getAverage());
//		System.out.println("Deviation of count: " + m1.getDeviation());
//		
//		System.out.println("\nSorting entries...");
//		Hashtable counts = m1.getClassCounts();
//		TreeMap sorted = new TreeMap();
//		Set entries = counts.entrySet();
//		Map.Entry me;
//		for(Iterator i = entries.iterator(); i.hasNext(); ){
//			me = (Map.Entry)i.next();
//			sorted.put(me.getKey(), me.getValue());
//		}
//		
//		entries = sorted.entrySet();
//		for(Iterator i = entries.iterator(); i.hasNext(); ){
//			me = (Map.Entry)i.next();
//			System.out.println(me.getKey() + ":" + me.getValue());
//		}
//		
//		System.out.println("\nGetting count of 'thing': " + m1.getClassCount("thing"));
//		System.out.println("Getting count of 'verb': " + m1.getClassCount("verb"));
//		System.out.println("Getting count of 'ugly': " + m1.getClassCount("ugly"));
	}

//	Debugging section
   public static final String LOGGER_GROUP = "memory";
   public static final String LOGGER_INSTANCE = "Memory";
   public static final String LOGGER = LOGGER_GROUP + "." + LOGGER_INSTANCE;
  
   public static Logger getLogger(){
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
   }
   protected static void severe(Object s) {
	Logger.getLogger(LOGGER).severe(LOGGER_INSTANCE + ": " + s);
   }

}
