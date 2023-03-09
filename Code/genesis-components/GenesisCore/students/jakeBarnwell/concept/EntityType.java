package jakeBarnwell.concept;

import java.util.HashMap;

import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;

public enum EntityType {
	ENTITY,
	FUNCTION,
	RELATION,
	SEQUENCE;
	
	// I'm uncomfortable using reflection so just do it by hand
	private static final HashMap<Class<? extends Entity>, EntityType> CLASS_TO_ENTITY_TYPE 
		= new HashMap<Class<? extends Entity>, EntityType>() {
		private static final long serialVersionUID = 53L;
		{
			put(Entity.class, ENTITY);
			put(Function.class, FUNCTION);
			put(Relation.class, RELATION);
			put(Sequence.class, SEQUENCE);
		}
	};
	
	public static EntityType of(Entity e) {
		return CLASS_TO_ENTITY_TYPE.get(e.getClass());
	}
	
	public static EntityType of(SemanticTree tree) {
		if(tree.getObject() != null) {
			return RELATION;
		}
		if(tree.getSubject() != null) {
			return FUNCTION;
		}
		if(tree.getElements() != null) {
			return SEQUENCE;
		}
		
		return ENTITY;		
	}
}
