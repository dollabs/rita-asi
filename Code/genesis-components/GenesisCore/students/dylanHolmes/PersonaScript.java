package dylanHolmes;

import java.util.ArrayList;

import frames.entities.Entity;
import frames.entities.Matcher;
import frames.entities.Sequence;
import matchers.StandardMatcher;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;

public class PersonaScript {
	
	public String name;
	public Entity agonistVar; // who is the focus
	private Sequence forbiddenConcepts;
	private ArrayList<Goal> availableStrategies;
	private LList<PairOfEntities> globalBindings;
	private ArrayList<GoalPrecedent> attachedPrecedents;
	
	public PersonaScript (String name, Entity agonist) {
		this.name = name;
		this.agonistVar = agonist;
		this.forbiddenConcepts = new Sequence();
		this.availableStrategies = new ArrayList<Goal>();
		this.globalBindings = new LList<PairOfEntities>();
		this.attachedPrecedents = new ArrayList<GoalPrecedent>();
	}

	public PersonaScript addStrategy(Goal g) {
		this.availableStrategies.add(g);
		return this;
	}
	
	public PersonaScript addPrecedent(GoalPrecedent gp) {
		this.attachedPrecedents.add(gp);
		return this;
	}
	
	public PersonaScript copyStructureOnly() {
		// !! importantly, excludes global bindings.
		PersonaScript ret = new PersonaScript(this.name, this.agonistVar);
		ret.setAvailableStrategies(availableStrategies);
		ret.setForbiddenConcepts(forbiddenConcepts);
		ret.setGlobalBindings(globalBindings);
		return ret;
	}
	
	public boolean aboutTheSameCharacter(GoalPrecedent gp) {
		// Returns true if the value in the gp's binding map
		// and the value in the persona script's global map
		// agree at the key associated with the persona script's "who" value.
		
		// TODO: decide what to do if gp has no binding for who yet.
		
		Entity a = null;
		Entity b = null;
		
		// cf StandardMatcher::getAssignment
		for (PairOfEntities p : this.globalBindings) {
			if (this.agonistVar == p.getPattern()) {
				a = p.getDatum();
			}
		}
		for (PairOfEntities p : gp.getBindings()) {
			if (this.agonistVar == p.getPattern()) {
				b = p.getDatum();
			}
		}
		return a == b;
	}
	
	public LList<PairOfEntities> addBindings(LList<PairOfEntities> additionalBindings) {
		// Add additional bindings to the current global bindings.
		// If this creates a conflict at the global level, set the global bindings to null.
		// TODO: automatic stub.
		return null;
	}
	
	
	public Sequence getForbiddenConcepts() {
		return forbiddenConcepts;
	}

	public void setForbiddenConcepts(Sequence forbiddenConcepts) {
		this.forbiddenConcepts = forbiddenConcepts;
	}

	public ArrayList<Goal> getAvailableStrategies() {
		return availableStrategies;
	}

	public void setAvailableStrategies(ArrayList<Goal> availableStrategies) {
		this.availableStrategies = availableStrategies;
	}

	public LList<PairOfEntities> getGlobalBindings() {
		return globalBindings;
	}

	public void setGlobalBindings(LList<PairOfEntities> globalBindings) {
		this.globalBindings = globalBindings;
	}

	public void forbidConcept(Sequence concept) {
		this.forbiddenConcepts.addElement(concept);
	}

	
	public Entity getAgonist() {
		return  Matcher.instantiate(this.agonistVar, this.getGlobalBindings());
	}
	public ArrayList<GoalPrecedent> getAttachedPrecedents() {
		return attachedPrecedents;
	}

	public void setAttachedPrecedents(ArrayList<GoalPrecedent> attachedPrecedents) {
		this.attachedPrecedents = attachedPrecedents;
	}

	public boolean compatibleAgonist(GoalPrecedent gp) {
		// TODO This is a hack
		return true;
		//Mark.say(Matcher.instantiate(this.agonistVar, gp.getBindings()).getSubject().getName(), Matcher.instantiate(this.agonistVar, gp.getBindings()).getSubject().getName());
		//return Matcher.instantiate(this.agonistVar, gp.getBindings()).getSubject().getName() == Matcher.instantiate(this.agonistVar, gp.getBindings()).getSubject().getName();
		
		//Mark.say("\t\t\t compatible: ", Matcher.instantiate(this.agonistVar, gp.getBindings()).equals(Matcher.instantiate(this.agonistVar, this.getGlobalBindings())), this.name, this.getAgonist().getSubject().getName(), gp.getBindings(), Matcher.instantiate(this.agonistVar, gp.getBindings()), Matcher.instantiate(this.agonistVar, this.getGlobalBindings()));
		//return Matcher.instantiate(this.agonistVar, gp.getBindings()) == Matcher.instantiate(this.agonistVar, this.getGlobalBindings());
	}
	
	
}
