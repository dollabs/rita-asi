package memory.patterns;

import frames.entities.Entity;

/**
 * Data structure that describes two Things, and a Relation between them.
 * Immutable.
 * 
 * @author sglidden
 *
 */
public final class Pattern {

	// need to get a more comprehensive list of these
	public enum Relation { 
		causes,
		proceeds,
	}
	
	private Entity t1, t2;
	private Relation r;
	
	public Pattern(Entity t1, Entity t2, Relation r) {
		this.t1 = t1;
		this.t2 = t2;
		this.r = r;
	}
	
	
}
