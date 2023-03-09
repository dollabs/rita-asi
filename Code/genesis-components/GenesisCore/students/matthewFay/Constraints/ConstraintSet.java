package matthewFay.Constraints;

import java.util.*;

import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;
import matthewFay.Utilities.EntityHelper;
import matthewFay.representations.BasicCharacterModel;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import frames.entities.Entity;

public class ConstraintSet {
	private Multimap<Entity, Entity> all_constraints;
	public Multimap<Entity, Entity> getAllConstraints() {
		return all_constraints;
	}
	
	public ConstraintSet() {
		all_constraints = HashMultimap.create();
	}
	
	public ConstraintSet(LList<PairOfEntities> bindings) {
		all_constraints = HashMultimap.create();
		
		this.addConstraints(bindings);
	}
	
	public ConstraintSet(List<PairOfEntities> bindings) {
		all_constraints = HashMultimap.create();
		
		this.addConstraints(bindings);
	}
	
	public ConstraintSet(ConstraintSet cs) {
		all_constraints = HashMultimap.create();
		
		all_constraints.putAll(cs.getAllConstraints());
	}
	
	public void addConstraint(Entity e, Entity constraint) {
		all_constraints.put(e, constraint);
		if(EntityHelper.isGeneric(e) && EntityHelper.isGeneric(constraint)) {
			all_constraints.put(constraint, e);
		}
	}
	
	public boolean validConstraints() {
		for(Entity entity : all_constraints.keySet()) {
			Collection<Entity> constraints = all_constraints.get(entity);
			
			if(constraints.size() <= 1)
				continue;
			
			//Special Generics Condition
			if(!EntityHelper.isGeneric(entity))
				return false;
			
			Entity target = null;
			for(Entity constraint : constraints) {
				if(!EntityHelper.isGeneric(constraint)) {
					if(target == null)
						target = constraint;
					else
						return false; //Two targets!
				}
			}
			
			if(target == null) {
				//Assume that other generic search will sort this out?
				boolean got_one = false;
				
				for(Entity generic_entity : constraints) {
					if(target == null && !got_one) {
						target = findTarget(generic_entity, new ArrayList<Entity>());
						got_one=true;
					} else {
						Entity next_target = findTarget(generic_entity, new ArrayList<Entity>());
						if( (next_target == null && target == null)
								|| (next_target != null && next_target.equals(target) ) ) {
							continue;
						} else {
							return false;
						}
					}
				}
			} else {			
				generics_checked.clear();
				boolean ret = genericValidation(entity,target);
				if(ret == false)
					return false;
			}
		}
		simplifyConstraints();
		//Now check for double bindings i.e. X's gen1 & gen2 binding to Y
		Multimap<BasicCharacterModel, Entity> double_finder = HashMultimap.create();
		for(Entity e : all_constraints.keySet()) {
			for(Entity target : all_constraints.get(e)) {
				if(!EntityHelper.isGeneric(target)) {
					BasicCharacterModel origin = BasicCharacterModel.getOriginatingCharacter(e);
					if(!double_finder.get(origin).contains(target)) {
						double_finder.put(origin, target);
					} else {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	private Set<Entity> generics_checked = new HashSet<Entity>();
	private boolean genericValidation(Entity generic, Entity target) {
		//Prevent Loops
		if(generics_checked.contains(generic))
			return true;
		generics_checked.add(generic);
		
		boolean ret = true;
		for(Entity other_constraint : all_constraints.get(generic)) {
			if(EntityHelper.isGeneric(other_constraint)) {
				ret = ret && genericValidation(other_constraint, target); 
			} else {
				ret = ret && other_constraint.equals(target);
			}
		}
		return ret;
	}
	
	public List<PairOfEntities> toList() {
		if(!simplifyConstraints())
			Mark.err("Problematic Constraint Set!");
		List<PairOfEntities> llist = new ArrayList<>();
		for(Entity entity : all_constraints.keySet()) {
			for(Entity constraint : all_constraints.get(entity)) {
				llist.add(new PairOfEntities(entity, constraint));
			}
		}
		return llist;
	}
	
	private boolean simplifyConstraints() {
		Map<Entity, Entity> simplified_targets = new HashMap<Entity, Entity>();
		for(Entity entity : all_constraints.keySet()) {
			Entity target = findTarget(entity, new ArrayList<Entity>());
			if(target != null)
				simplified_targets.put(entity, target);
		}
		for(Entity entity : simplified_targets.keySet()) {
			all_constraints.removeAll(entity);
			all_constraints.put(entity, simplified_targets.get(entity));
		}
		return true;
	}
	private Entity findTarget(Entity generic_entity, Collection<Entity> checked_generics) {
		checked_generics.add(generic_entity);
		for(Entity possible_target : all_constraints.get(generic_entity)) {
			if(!EntityHelper.isGeneric(possible_target)) {
				return possible_target;
			} else {
				if(!checked_generics.contains(possible_target)) {
					Entity target = findTarget(possible_target, checked_generics);
					if(target != null)
						return target;
				}
			}
		}
		return null;
	}
	
	public void addConstraints(LList<PairOfEntities> bindings) {
		for(PairOfEntities binding : bindings) {
			this.addConstraint(binding.getPattern(), binding.getDatum());
		}
	}
	
	public void addConstraints(List<PairOfEntities> bindings) {
		for(PairOfEntities binding : bindings) {
			this.addConstraint(binding.getPattern(), binding.getDatum());
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof ConstraintSet) {
			ConstraintSet cs = (ConstraintSet)o;
			Multimap<Entity, Entity> all_constraints2 = cs.getAllConstraints();
			
			if(all_constraints.size() != all_constraints2.size())
				return false;
			
			for(Entity e : all_constraints.keySet()) {
				if(!all_constraints2.containsKey(e))
					return false;
				for(Entity constraint : all_constraints.get(e)) {
					if(!all_constraints2.get(e).contains(constraint))
						return false;
				}
			}
			
			return true;
		}
		return false;
	}
	
	public static void main(String[] args) {
		ConstraintSet cs = new ConstraintSet();
		
		Entity alpha = new Entity("alpha");
		Entity beta = new Entity("beta");
		Entity gamma = new Entity("gamma");
		Entity omega = new Entity("omega");
		
		Entity gen0 = EntityHelper.getGenericEntity();
		Entity gen1 = EntityHelper.getGenericEntity();
		Entity gen2 = EntityHelper.getGenericEntity();
		Entity gen3 = EntityHelper.getGenericEntity();
		Entity gen4 = EntityHelper.getGenericEntity();
		Entity gen5 = EntityHelper.getGenericEntity();
		
		cs.addConstraint(gen0, gen1);
		cs.addConstraint(gen2, gen1);
		cs.addConstraint(gen0, omega);
		cs.addConstraint(gen2, omega);
		cs.addConstraint(gen2, gen3);
//		cs.addConstraint(gen4, gen5);
		cs.addConstraint(gen4, gen5);
//		cs.addConstraint(gen5, omega);
		
		Mark.say("valid?: "+cs.validConstraints());
		Mark.say(cs.toList());
		cs.simplifyConstraints();
		Mark.say(cs.toList());
	}
}
