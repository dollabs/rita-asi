package matthewFay.Constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import utils.PairOfEntities;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import frames.entities.Entity;

public class BindingSet {
	private Multimap<Entity, Entity> binding_map = HashMultimap.create();
	
	public BindingSet(List<PairOfEntities> bindings) {
		for(PairOfEntities binding : bindings) {
			binding_map.put(binding.getPattern(), binding.getDatum());
		}
	}
	
	public Entity getDatum(Entity pattern) {
		Entity datum = null;
		if(binding_map.containsKey(pattern)) {
			Collection<Entity> datums = binding_map.get(pattern);
			if(datums.size() == 1)
				for(Entity e : datums)
					datum = e;
		}
		return datum;
	}
	
	public Entity getPattern(Entity datum) {		
		List<Entity> patterns = new ArrayList<>();
		for(Entity pattern : binding_map.keySet()) {
			if(binding_map.get(pattern).contains(datum)) {
				patterns.add(pattern);
			}
		}
		if(patterns.size() == 1)
			return patterns.get(0);
		return null;
	}
	
	public static List<PairOfEntities> getInvertedBindings(List<PairOfEntities> bindings) {
		ArrayList<PairOfEntities> inverted_bindings = new ArrayList<>();
		
		for(PairOfEntities binding : bindings) {
			Entity datum = binding.getPattern();
			Entity pattern = binding.getDatum();
			inverted_bindings.add(new PairOfEntities(pattern, datum));
		}
		
		return inverted_bindings;
	}
	
	public List<PairOfEntities> getBindings() {
		ArrayList<PairOfEntities> bindings = new ArrayList<>();
		
		for(Entity pattern : binding_map.keySet()) {
			for(Entity datum : binding_map.get(pattern)) {
				bindings.add(new PairOfEntities(pattern, datum));
			}
		}
		
		return bindings;
	}
	
}
