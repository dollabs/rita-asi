package jakeBarnwell.concept;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a transformation of a single step, from one tree to another.
 * @author jb16
 *
 */
public final class Transformation {
	public final EntityOperator type;
	public final NodeLocation location;
	
	// These store additional details about the transformation as
	//  necessary. May or may not be null, depending on the transformation type.
	public ConceptTree eleAdded, eleRemoved;
	
	public Transformation(EntityOperator op, NodeLocation tp, Properties properties) {
		type = op;
		location = tp;
		
		for(String key : properties.keys()) {
			Object val = properties.get(key);
			switch(key) {
				case "ELE_ADDED": eleAdded = (ConceptTree)val; break;
				case "ELE_REMOVED": eleRemoved = (ConceptTree)val; break;
				default: throw new RuntimeException("Illegal property name!"); 
			}
		}
	}
	
	@Override
	public String toString() {
		return type.toString();
	}
}