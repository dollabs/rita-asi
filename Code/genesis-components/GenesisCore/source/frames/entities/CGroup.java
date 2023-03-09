/* Filename: CorGroup.java
 * Author: M. A. Finlayson
 * Format: Java 2 v.1.4.2
 * Date created: Jun 10, 2004
 */
package frames.entities;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/** 
 * @author M.A. Finlayson
 * @since Jun 10, 2004; JDK 1.4.2
 */
public class CGroup {
	
	private HashMap<Pair, Correspondence> cors = new HashMap<Pair, Correspondence>();
	
	public Collection<Correspondence> getCorrespondences(){
		HashSet<Correspondence> result = new HashSet<Correspondence>();
		result.addAll(cors.values());
		return result;
	}
	
	public void putCorrespondence(Entity a, Entity b, Correspondence c){
		cors.put(new Pair(a,b), c);
	}
	
	public Set<Correspondence> correspondencess(){
		Set<Correspondence> result = new HashSet<Correspondence>();
		result.addAll(cors.values());
		return result;
	}

	public Correspondence getCorrespondence(Entity a, Entity b){
		return cors.get(new Pair(a, b));
	}
	
	public Correspondence getCorrespondence(int id1, int id2){
		//System.out.println("Getting correspondence: (one,two)=(" + id1 + "," +  id2 + ")");
		Pair pair;
		for(Iterator<Pair> i = cors.keySet().iterator(); i.hasNext(); ){
			pair = i.next();
			if((pair.a.getID() == id1 & pair.b.getID() == id2) | 
			   (pair.a.getID() == id2 & pair.b.getID() == id1)){
			   	return cors.get(pair);
			   }
		}

		return null;
	}
	
	private class Pair {
		Entity a, b;
		public Pair(Entity a, Entity b){
			this.a = a;
			this.b = b;
		}
		
		public boolean equals(Object o){
			if(o.getClass() != Pair.class){
				return false;
			}
			
			if(o.hashCode() == hashCode()){
				return true;
			} else {
				return false;
			}
		}
		
		public int hashCode(){
			return a.hashCode() + b.hashCode();
		}
	}

}
