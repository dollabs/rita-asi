package consciousness;

import frames.entities.Entity;

/*
 * Created on Feb 15, 2016
 * @author phw
 */

public class Problem {
	Entity problemDescription;

	public Problem(Entity entity) {
		this.problemDescription = entity;
	}

	public Entity getProblemDescription() {
		return problemDescription;
	}
}
