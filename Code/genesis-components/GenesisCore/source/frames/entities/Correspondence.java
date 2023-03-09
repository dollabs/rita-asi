/* Filename: Correspondence.java
 * Author: M. A. Finlayson
 * Format: Java 2 v.1.4.2
 * Date created: Jun 2, 2004
 */
package frames.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

/** Holds the neccessary information to indicate a correspondence 
 * between two trajectory spaces.  An analogy is a type of correspondence.
 * 
 * @author M.A. Finlayson
 * @since Jun 2, 2004; JDK 1.4.2
 */
public class Correspondence {
	
	private Entity source, target;
	private HashMap<String, Entity> nameToThing;
	private HashMap<Entity, Entity> mappings;
	private HashMap<CEntry, Double> scores;
	private double score;
	
	public Correspondence(Entity s, Entity t){
		this(s, t, 0);
	}
	
	public Correspondence(Entity s, Entity t, double score){
		source = s;
		target = t;
		nameToThing = new HashMap<String, Entity>();
		mappings = new HashMap<Entity, Entity>();
		scores = new HashMap<CEntry, Double>();
		setScore(score);
	}
	
	public void setScore(double s){score = s;}
	
	public Entity getSource() {return source;}
	public Entity getTarget() {return target;}
	public Map<Entity, Entity> getMappings(){return mappings;}
	public Map<CEntry, Double> getScores(){return scores;}
	public double getScore(){return score;}
	
	public double getScore(Entity x, Entity y){
		Double result = new Double(-1);
		CEntry c, e = new CEntry(x,y);
		for(Iterator<CEntry> i = scores.keySet().iterator(); i.hasNext(); ){
			c = i.next();
			if(c == e){
				result = scores.get(c);
				break;	
			}
		}
		
		return result.doubleValue();
	}
	
	public Correspondence getSubCorrespondence(Entity s, Entity t){
		if(s == null || t == null){return null;}
		Correspondence result = new Correspondence(s, t);
		Vector<Entity> queue = new Vector<Entity>();
		
		queue.add(s);
		queue.add(t);
		Entity next, match;
		while(!queue.isEmpty()){
			next = queue.remove(0);
			
			match = mappings.get(next);
			if(match != null){
				result.addMatch(next, match);
			}
			
			if(next.functionP()){
				match = next.getSubject();
				queue.add(match);
			} else if (next.relationP()){
				match = next.getSubject();
				queue.add(match);
				match = next.getObject();
				queue.add(match);
			} else if (next.sequenceP()){
				queue.addAll(next.getElements());
			}
				
		}
		
		return result;
	}
	
	public CEntry getMatch(String s, String t){
		Entity source = nameToThing.get(s);
		Entity target = nameToThing.get(t);
		return new CEntry(source, target);
	}
	
	public Set<CEntry> getWithinTreeMatches(CEntry entry){
		HashSet<CEntry> result = new HashSet<CEntry>();
		
		CEntry test;
		Entity entrySource = (Entity)entry.key;
		Entity entryTarget = (Entity)entry.value;
		Entity testSource, testTarget;
		for(Iterator<CEntry> i = scores.keySet().iterator(); i.hasNext(); ){
			test = i.next();
			testSource = (Entity)test.key;
			testTarget = (Entity)test.value;
			if(entrySource.isAncestorOf(testSource) && entryTarget.isAncestorOf(testTarget)){
				result.add(test);		
			}
		}
		
		return result;
	}
	
	public Set<CEntry> getInconsistentMatches(CEntry entry){
		HashSet<CEntry> result = new HashSet<CEntry>();
		
		CEntry test;
		Entity entrySource = (Entity)entry.key;
		Entity entryTarget = (Entity)entry.value;
		Entity testSource, testTarget;
		for(Iterator<CEntry> i = scores.keySet().iterator(); i.hasNext(); ){
			test = i.next();
			testSource = (Entity)test.key;
			testTarget = (Entity)test.value;
			if(!entrySource.isAncestorOf(testSource) && entryTarget.isAncestorOf(testTarget)){
				result.add(test);		
			}
			
			if(entrySource.isAncestorOf(testSource) && !entryTarget.isAncestorOf(testTarget)){
				result.add(test);		
			}
		}
		
		return result;
	}
	
	public Entity getMatchSource(int id){
		Entity result;
		for(Iterator<Entity> i = mappings.keySet().iterator(); i.hasNext(); ){
			result = i.next();
			if(result.getID() == id){
				return result;
			}
		}
		return null;
	}
	
	public Entity getMatchTarget(int id){
		Entity result;
		for(Iterator<Entity> i = mappings.values().iterator(); i.hasNext(); ){
			result = i.next();
			if(result != null){
				if(result.getID() == id){
					return result;
				}
			}
		}
		return null;
	}
	
	public void addMatch(Entity x, Entity y){
		addMatch(x, y, 0);
	}
	
	public void addMatch(Entity x, Entity y, double d){
		mappings.put(x,y);
		nameToThing.put(x.getName(), x);
		nameToThing.put(y.getName(), y);
		addScore(new CEntry(x, y), new Double(d));
	}
	
	public void addScore(CEntry e, Double d){
		scores.put(e, d);
	}
	
	public String getName(){
		String result = "";
		if(getSource() != null){
			result = result + getSource().getName();
		} else {
			result = result + "null";
		}
		
		result = result + " :: ";
		
		if(getTarget() != null){
			result = result + getTarget().getName();
		} else {
			result = result + "null";
		}
		
		return result;
	}
	
	public String toString(){
	    StringBuffer result = new StringBuffer();
	    
	    result.append(getName());
	    result.append("\n");
	    
	    Map.Entry entry;
	    Entity base, target;
	    for(Iterator i = mappings.entrySet().iterator(); i.hasNext(); ){
	        entry = (Map.Entry)i.next();
	        base = (Entity)entry.getKey();
	        target = (Entity)entry.getValue();
	        result.append("<");
	        result.append(base.getName());
	        result.append(" ;; ");
	        result.append(target.getName());
	        result.append(">");
	        if(i.hasNext()) result.append("\n");
	    }
	    return result.toString();
	}
	
	public boolean equals(Object o){
		Correspondence c;
		try{c = (Correspondence)o;} catch (ClassCastException e){return false;}
		
		if(getSource() == null & getTarget() == null){
			if(c.getSource() == null &
			   c.getTarget() == null &
			   getMappings().equals(c.getMappings()) &
			   getScores().equals(c.getScores())){
				return true;
			   } else {
				return false;
			   }
		} else if (getSource() == null){
			if(c.getSource() == null &
			   getTarget().equals(c.getTarget()) &
			   getMappings().equals(c.getMappings()) &
			   getScores().equals(c.getScores())){
				return true;
			   } else {
				return false;
			   }
		} else if (getTarget() == null){
			if(getSource().equals(c.getSource()) &
			   c.getTarget() == null &
			   getMappings().equals(c.getMappings()) &
			   getScores().equals(c.getScores())){
				return true;
			   } else {
				return false;
			   }
		} else {
			if(getSource().equals(c.getSource()) &
			   getTarget().equals(c.getTarget()) &
			   getMappings().equals(c.getMappings()) &
			   getScores().equals(c.getScores())){
				return true;
			   } else {
				return false;
			   }
		}
	}
	
	public int hashCode(){
		if(getSource() == null & getTarget() == null){
			return getMappings().hashCode() + getScores().hashCode();
		} else if (getSource() == null){
			return getTarget().hashCode() + getMappings().hashCode() + getScores().hashCode();			
		} else if(getTarget() == null){
			return getSource().hashCode() + getMappings().hashCode() + getScores().hashCode();
		} else {
			return getSource().hashCode() + getTarget().hashCode() + getMappings().hashCode() + getScores().hashCode();						
		}
	}
	
		
	public class CEntry implements Entry {
		Object key, value;
		
		public CEntry(Object k, Object v){
			key = k;
			value = v;
		}
		
		public Object getKey() {return key;}
		public Object getValue() {return value;}

		public Object setValue(Object v) {
			Object old = value;
			value = v;
			return old;
		}
		
		public boolean equals(Object obj){
			if(obj instanceof CEntry){
				CEntry e = (CEntry)obj;
				if(e.key == key && e.value == value){
					return true;
				}
			}
			return false;
		}
		
		public int hashCode(){
			int result = super.hashCode();
			if(key != null){
				result = result + key.hashCode();
			}
			
			if(value != null){
				result = result + value.hashCode();
			}
			
			return result;
		}
		
	}
}
