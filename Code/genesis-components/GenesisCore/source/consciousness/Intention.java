package consciousness;

import java.util.*;
import java.util.function.Predicate;

import frames.entities.Entity;
import utils.PairOfEntities;
import utils.minilisp.LList;

/*
 * Created on Feb 15, 2016
 * @author phw
 */

public class Intention {

	String name;

	Predicate<Entity> predicate;

	Entity intentionDescription;

	List<Condition> conditions;

	LList<PairOfEntities> bindings;

	public Intention(String name, Entity entity, Predicate<Entity> function, Condition... conditions) {
		this.name = name;
		this.intentionDescription = entity;
		this.predicate = function;
		this.conditions = Arrays.asList(conditions);
	}

	public String getName() {
		return name;
	}

	public Predicate<Entity> getPredicate() {
		return predicate;
	}

	public Entity getIntentionDescription() {
		return intentionDescription;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setBindings(LList<PairOfEntities> match) {
		bindings = match;
	}

	public LList<PairOfEntities> getBindings() {
		return bindings;
	}

	public Entity getInstantiatedIntentionDescription() {
		Entity result = U.substitute(getIntentionDescription(), getBindings());
		// Mark.say("Instantiation bindings", getBindings());
		// Mark.say("Instantiation description", getIntentionDescription());
		// Mark.say("Instantiation result", result);

		return result;
	}
}
