package matthewFay.Constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import matthewFay.Utilities.EntityHelper;

import utils.Mark;
import utils.PairOfEntities;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import frames.entities.Entity;

public class BindingInferenceUtility {
	private Multimap<Entity, Entity> binding_map;
	private Multimap<Entity, Entity> inverse_binding_map;
	
	private Set<Entity> known_entities;
	private Set<Entity> required_entities;
	private Set<Entity> all_targets;
	
	public BindingInferenceUtility(Iterable<PairOfEntities> bindings) {
		binding_map = HashMultimap.create();
		inverse_binding_map = HashMultimap.create();
		
		known_entities = new HashSet<Entity>();
		required_entities = new HashSet<Entity>();
		all_targets = new HashSet<Entity>();
		
		add(bindings);
	}
	
	public void add(Iterable<PairOfEntities> bindings) {
		for(PairOfEntities binding : bindings) {
			add(binding);
		}
	}
	
	public void add(PairOfEntities binding) {
		Entity pattern = binding.getPattern();
		Entity datum = binding.getDatum();
		
		known_entities.add(pattern);
		known_entities.add(datum);
		
		binding_map.put(pattern, datum);
		inverse_binding_map.put(datum, pattern);
	}
	
	/**
	 * This function finds generic entities that point to the same target datum
	 * These generics are added as bindings to each other
	 */
	public void inferEquivalentGenerics() {
		List<PairOfEntities> new_bindings = new ArrayList<PairOfEntities>();
		
		for(Entity datum : inverse_binding_map.keySet()) {
			if(!EntityHelper.isGeneric(datum)) {
				List<Entity> generic_entities = new ArrayList<>(inverse_binding_map.get(datum));
				
				for(int i=0; i<generic_entities.size(); i++) {
					Entity generic_datum_i = generic_entities.get(i);
					
					if(EntityHelper.isGeneric(generic_datum_i)) {
						for(int j=i+1; j<generic_entities.size(); j++) {
							Entity generic_datum_j = generic_entities.get(j);
							
							if(EntityHelper.isGeneric(generic_datum_j)) {
								new_bindings.add(new PairOfEntities(generic_datum_i,generic_datum_j));
								new_bindings.add(new PairOfEntities(generic_datum_j,generic_datum_i));
							}
						}
					}
				}
			}
		}
		
		add(new_bindings);
	}
	
	/**
	 * Finds generics which point to a target then chains through bindings
	 * to add this 
	 */
	public void inferTargetsFromChaining() {
		List<PairOfEntities> new_bindings = new ArrayList<PairOfEntities>();
		
		
		for(Entity generic_pattern : binding_map.keySet()) {
			List<Entity> completed_entities = new ArrayList<>();
			completed_entities.add(generic_pattern);
			if(EntityHelper.isGeneric(generic_pattern)) {
				//Find a target
				for(Entity target_datum : binding_map.get(generic_pattern)) {
					if(!EntityHelper.isGeneric(target_datum)) {
						for(Entity generic_datum : binding_map.get(generic_pattern)) {
							if(EntityHelper.isGeneric(generic_datum)) {
								new_bindings.addAll( fillTargetsFromChaining(generic_datum, target_datum, completed_entities) );
							}
						}
					}
				}
			}
		}
		
		add(new_bindings);
	}
	
	private List<PairOfEntities> fillTargetsFromChaining(Entity generic, Entity target, List<Entity> completed_entities) {
		List<PairOfEntities> new_bindings = new ArrayList<>();
		
		if(completed_entities.contains(generic))
			return new_bindings;
		completed_entities.add(generic);
		
		new_bindings.add(new PairOfEntities(generic, target));
		for(Entity datum : binding_map.get(generic)) {
			if( EntityHelper.isGeneric(datum) ) {
				new_bindings.addAll(fillTargetsFromChaining(datum, target, completed_entities));
			} else {
				if(!target.equals(datum)) {
					Mark.err("Binding Conflict!!");
				}
			}
		}
		
		return new_bindings;
	}
	
	public void filterBindings(List<Entity> pattern_filter, List<Entity> datum_filter) {
		List<Entity> pattern_keys = new ArrayList<>(binding_map.keySet());
		List<Entity> datum_keys = new ArrayList<>(inverse_binding_map.keySet());
		
		for(Entity pattern : pattern_keys) {
			if(!pattern_filter.contains(pattern)) {
				//Remove all pairs from binding_map + inverse_binding_map
				List<Entity> datums = new ArrayList<>(binding_map.get(pattern));
				for(Entity datum : datums) {
					binding_map.remove(pattern,datum);
					inverse_binding_map.remove(datum, pattern);
				}
			}
		}
		
		for(Entity datum : datum_keys) {
			if(!datum_filter.contains(datum)) {
				List<Entity> patterns = new ArrayList<>(inverse_binding_map.get(datum));
				for(Entity pattern : patterns) {
					binding_map.remove(pattern, datum);
					inverse_binding_map.remove(datum, pattern);
				}
			}
		}
		
		known_entities.clear();
		for(Entity pattern : binding_map.keySet()) {
			known_entities.add(pattern);
		}
		for(Entity datum : inverse_binding_map.keySet()) {
			known_entities.add(datum);
		}
	}
	
	public void requireBindingsFor(List<Entity> entities) {
		for(Entity entity : entities) {
			requireBindingsFor(entity);
		}
	}
	
	public void requireBindingsFor(Entity entity) {
		required_entities.add(entity);
	}
	
	public void addTargets(List<Entity> targets) {
		for(Entity target : targets) {
			addTarget(target);
		}
	}
	
	public void addTarget(Entity target) {
		all_targets.add(target);
	}
	
	public List<PairOfEntities> getTwoWayBindings() {
		List<PairOfEntities> bindings = getBindings();
		
		List<PairOfEntities> all_bindings = new ArrayList<>();
		
		for(PairOfEntities binding : bindings){
			if(!all_bindings.contains(binding))
				all_bindings.add(binding);
			PairOfEntities inv = new PairOfEntities(binding.getDatum(), binding.getPattern());
			if(!all_bindings.contains(inv))
				all_bindings.add(inv);
		}
		
		Collections.sort(all_bindings, new Comparator<PairOfEntities>() {

			@Override
			public int compare(PairOfEntities o1, PairOfEntities o2) {
				String s1 = o1.toString();
				String s2 = o2.toString();
				
				return s1.compareTo(s2);
			}
			
		});
		
		return all_bindings;
	}
	
	public List<PairOfEntities> getBindings() {
		List<PairOfEntities> bindings = new ArrayList<>();
		
		//First deal with unmatched requirements//
		Set<Entity> all_entities = new HashSet<>();
		all_entities.addAll(known_entities);
		all_entities.addAll(required_entities);
		
		for(Entity required : required_entities) {
			if(!known_entities.contains(required)) {
				//Bind required to all characters+generics
				PairOfEntities binding;
				for(Entity target : all_entities) {
					binding = new PairOfEntities(required, target);
					if(!bindings.contains(binding))
						bindings.add(binding);
					binding = new PairOfEntities(target, required);
					if(!bindings.contains(binding))
						bindings.add(binding);
				}
				for(Entity target : all_targets) {
					binding = new PairOfEntities(required, target);
					if(!bindings.contains(binding))
						bindings.add(binding);
					binding = new PairOfEntities(target, required);
					if(!bindings.contains(binding))
						bindings.add(binding);
				}
			}
		}
		
		for(Entity pattern : binding_map.keySet()) {
			for(Entity datum : binding_map.get(pattern)) {
				if(!pattern.equals(datum)) {
					PairOfEntities binding = new PairOfEntities(pattern, datum);
					if(!bindings.contains(binding)) {
						bindings.add(binding);
					}
				}
			}
		}
		
		Collections.sort(bindings, new Comparator<PairOfEntities>() {

			@Override
			public int compare(PairOfEntities o1, PairOfEntities o2) {
				String s1 = o1.toString();
				String s2 = o2.toString();
				
				return s1.compareTo(s2);
			}
			
		});
		
		return bindings;
	}
	
	public static void main(String[] args) {
		Entity gen0 = EntityHelper.getGenericEntity();
		Entity gen1 = EntityHelper.getGenericEntity();
		Entity gen2 = EntityHelper.getGenericEntity();
		Entity gen3 = EntityHelper.getGenericEntity();
		Entity gen4 = EntityHelper.getGenericEntity();
		Entity gen5 = EntityHelper.getGenericEntity();
		Entity gen6 = EntityHelper.getGenericEntity();
		
		Entity mark = new Entity("mark");
		Entity mary = new Entity("mary");
		Entity sally= new Entity("sally");
		
		List<PairOfEntities> bindings = new ArrayList<>();
		bindings.add(new PairOfEntities(gen0, mark));
		bindings.add(new PairOfEntities(gen1, mark));
		bindings.add(new PairOfEntities(gen2, mary));
		bindings.add(new PairOfEntities(gen3, mary));
		bindings.add(new PairOfEntities(gen3, gen5));
		bindings.add(new PairOfEntities(gen5, gen4));
		bindings.add(new PairOfEntities(gen4, mary));
		
		BindingInferenceUtility biu = new BindingInferenceUtility(bindings);
		biu.inferTargetsFromChaining();
		biu.inferEquivalentGenerics();
		biu.inferTargetsFromChaining();
		biu.inferEquivalentGenerics();
		biu.inferTargetsFromChaining();
		biu.inferEquivalentGenerics();
		biu.requireBindingsFor(gen6);
		Mark.say("Bindings: \n"+bindings);
		Mark.say("Bindings after inference: \n"+biu.getBindings());
		Mark.say("Two Way Bindings after inference: \n"+biu.getTwoWayBindings());
		
		biu.filterBindings(ImmutableList.of(gen0), ImmutableList.of(mark));
		biu.addTarget(sally);
		Mark.say("Bindings after filter: \n"+biu.getBindings());
		Mark.say("Two way Bindings after filter: \n"+biu.getTwoWayBindings());
	}
}
