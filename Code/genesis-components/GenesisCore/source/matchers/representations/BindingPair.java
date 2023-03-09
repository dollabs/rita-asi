package matchers.representations;

import java.util.ArrayList;
import java.util.List;

import frames.entities.Entity;
import utils.PairOfEntities;

public class BindingPair {
	private List<Entity> entitySet = new ArrayList<Entity>();
	//If false, this pair is NOT allowed
	private boolean allowed = true;
	private double threadScore = 0;
	public double getScore() {
		return threadScore;
	}
	
	public BindingPair(Entity entity1, Entity entity2, double score) {
		entitySet.add(entity1);
		entitySet.add(entity2);
		this.threadScore = score;
	}
	
	public BindingPair(boolean allowed, Entity entity1, Entity entity2, double score) {
		this.allowed = allowed;
		entitySet.add(entity1);
		entitySet.add(entity2);
		this.threadScore = score;
	}
	
	@Deprecated
	public BindingPair(PairOfEntities pair) {
		entitySet.add(pair.getPattern());
		entitySet.add(pair.getDatum());
	}
	
	@Deprecated
	public PairOfEntities toPairOfEntities() {
		return new PairOfEntities(getPattern(), getDatum());
	}
	
	public Entity getPattern() {
		return entitySet.get(0);
	}
	
	public Entity getDatum() {
		return entitySet.get(1);
	}
	
	public boolean getAllowed() {
		return allowed;
	}
	
	public Entity get(int i) {
		if(this.entitySet.size()>i) {
			return this.entitySet.get(i);
		}
		return null;
	}
	
	public List<Entity> getEntities() {
		return entitySet;
	}
	
	public int size() {
		return entitySet.size();
	}
	
	/**
	 * Returns true if two bindings conflict, meaning that they assign different datum
     * to the same pattern (e.g. "XX"), or they assign the same datum to different
     * patterns
	 */
	public boolean conflictsWith(BindingPair otherPair) {
	    return (getDatum().equals(otherPair.getDatum()) &&
	            !getPattern().equals(otherPair.getPattern())) ||
	            (getPattern().equals(otherPair.getPattern()) &&
	             !getDatum().equals(otherPair.getDatum()));
	}
	
	public boolean equals(Object o) {
		if(o instanceof BindingPair) {
			BindingPair binding = (BindingPair)o;
			if(size() != binding.size())
				return false;
			for(int i=0;i<size();i++) {
				if(!get(i).equals(binding.get(i)))
					return false;
			}
			return true;
		}
		return false;
	}
	
	// Need this to allow correct behavior for sets
	@Override
	public int hashCode() {
	    return entitySet.hashCode();
	}
	
	public String toString() {
		String str = "{ ";
		str += "'binding':[";
		str += "'"+get(0)+"', ";
		str += "'"+get(1)+"'], ";
		str += "'allowed':"+allowed+", ";
		str += threadScore+" ";
		str += "}";
		return str;
	}
}
