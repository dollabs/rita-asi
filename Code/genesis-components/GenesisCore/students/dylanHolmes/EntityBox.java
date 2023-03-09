package dylanHolmes;

import connections.WiredBox;
import frames.entities.Entity;

/**	
 * A WiredBox which encapsulates a single Entity.
 * 
 * An EntityBox accrues metadata about its Entity, such 
 * as how the Entity was generated, and what plot elements the
 * Entity belongs to.
 * 
 * @author ocsenave
 *
 */
public class EntityBox implements WiredBox {
	
	private String name;
	Entity entity;
	
	// These EntityBox modes are set based on the 
	// provenance of its Entity.
	
	private boolean mode_explained = false;
	private boolean mode_predicted = false;
	private boolean mode_negated = false;
	private boolean mode_assumed = false;
	
	
	@Override
	public String getName() {return this.name;}
	public Entity getEntity(){return this.entity;}
	
	public EntityBox(Entity e){
		this.entity = e;
	}
	
}