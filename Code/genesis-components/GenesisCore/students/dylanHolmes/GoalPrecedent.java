package dylanHolmes;

import utils.PairOfEntities;
import utils.minilisp.LList;

public class GoalPrecedent {
	// A goal precedent consists of a goal and bindings for that goal's variables
	private Goal goal;
	private LList<PairOfEntities> bindings;
	public GoalPrecedent(Goal goal, LList<PairOfEntities> new_bindings) {
		this.goal = goal;
		this.bindings = new_bindings;
	}
	public Goal getGoal() {
		return goal;
	}
	public void setGoal(Goal goal) {
		this.goal = goal;
	}
	public LList<PairOfEntities> getBindings() {
		return bindings;
	}
	public void setBindings(LList<PairOfEntities> bindings) {
		this.bindings = bindings;
	}
	
	public String toString() {
		return this.getGoal().name;
	}
}
