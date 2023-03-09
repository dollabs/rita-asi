package consciousness;

import java.util.function.Predicate;

import frames.entities.Entity;
import utils.PairOfEntities;
import utils.minilisp.LList;

/*
 * Created on Feb 15, 2016
 * @author phw
 */

public class Condition {
	Predicate<Entity> predicate;

	Entity conditionDescription;

	LList<PairOfEntities> bindings;

	String name;

	public Condition(String name, Entity entity, Predicate<Entity> function) {
		this.name = name;
		this.conditionDescription = entity;
		this.predicate = function;
	}

	public Predicate<Entity> getPredicate() {
		return predicate;
	}

	public Entity getConditionDescription() {
		return conditionDescription;
	}

	public void setBindings(LList<PairOfEntities> match) {
		bindings = match;
	}

	public LList<PairOfEntities> getBindings() {
		return bindings;
	}

	public Entity getInstantiatedConditionDescription() {
		return U.substitute(getConditionDescription(), getBindings());
	}

	public String getName() {
		return name;
	}
}