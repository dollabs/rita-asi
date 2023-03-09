package mentalModels;

import frames.entities.Entity;

/*
 * Created on Apr 14, 2016
 * @author phw
 */

public class EntityPair {
	Entity first;

	Entity second;

	public EntityPair(Entity f, Entity s) {
		first = f;
		second = s;
	}

	public Entity getFirst() {
		return first;
	}

	public Entity getSecond() {
		return second;
	}

}
