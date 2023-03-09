package matchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import frames.entities.Entity;
import utils.PairOfEntities;
import utils.minilisp.LList;
import matchers.representations.BindingPair;
import matthewFay.representations.*;

public class BindingValidator {
	private boolean enforceUniqueBindings = true;
	//Only check for uniqueness in one direction (pattern/e1 not yet bound)
	private boolean patternMatch = true;
	public void setPatternMatch(boolean value) {
		patternMatch = value;
	}
	
	public BindingValidator() {
		
	}
	
	public List<BindingPair> validateBindings(List<BindingPair> bindings) {
		return validateBindings(bindings, new ArrayList<BindingPair>());
	}
	
	//Shortcut method for converting from LList
	public List<BindingPair> validateBindings(List<BindingPair> bindings, LList<PairOfEntities> constraints) {
		return validateBindings(bindings, BindingValidator.convertFromLList(constraints));
	}
	
	public List<BindingPair> validateBindings(List<BindingPair> bindings, List<BindingPair> constraints) {
		if(bindings == null || bindings.size() < 1)
			return null;
		
		//In case we modify the list, don't propagate changes out of this function
		constraints = new ArrayList<BindingPair>(constraints);
		
		//Create extra constraints from exclusion bindings
		for(BindingPair binding : bindings) {
			if(!binding.getAllowed())
				constraints.add(binding);
		}
		
		HashMap<Entity, List<Entity>> mappings = new HashMap<Entity, List<Entity>>();
		//First add all pairs from the binding set
		for(BindingPair pair : bindings) {
			//Ignore exclusion constraints
			if(!pair.getAllowed())
				continue;
			//Validate against constraints
			if(validateConstraints(pair,constraints)) {
				Entity entity1 = pair.getPattern();
				Entity entity2 = pair.getDatum();
				if(!mappings.containsKey(entity1)) {
					mappings.put(entity1, new ArrayList<Entity>());
				}
				
				//Add the current binding pair to the set of bindings
				List<Entity> matches = mappings.get(entity1);
				if(!matches.contains(entity2)) {
					matches.add(entity2);
				}
				//Validate that we haven't broken the uniqueness constraint
				if(enforceUniqueBindings && matches.size() > 1) {
					return null;
				}
			} else {
				return null;
			}
		}
		//Simplify the list of bindings
		List<BindingPair> simplifiedBindings = new ArrayList<>();
		//Now add all the constraint pairs
		for(BindingPair pair : constraints) {
			if(!simplifiedBindings.contains(pair))
				simplifiedBindings.add(pair);
		}
		
		//Now add any new ones from the mappings
		for(BindingPair pair : bindings) {
			if(!simplifiedBindings.contains(pair))
				simplifiedBindings.add(pair);
		}
		
		return simplifiedBindings;
	}
	
	/**
	 * Verifies that the given constraints allow for the provided pairing
	 * @param pair
	 * @return
	 */
	private boolean validateConstraints(BindingPair pair, List<BindingPair> constraints) {
		Entity entity1 = pair.getPattern();
		Entity entity2 = pair.getDatum();
		
		boolean foundMismatch = false;
		boolean foundMatch = false;
		boolean foundNotAllowed = false;
		
		for(BindingPair constraint : constraints) {
			Entity constraint1 = constraint.getPattern();
			Entity constraint2 = constraint.getDatum();
			if(enforceUniqueBindings && constraint.getAllowed()) {
				if( (constraint1 == entity1 && constraint2 != entity2) ) {
					foundMismatch = true;
				}
				if( (constraint1 != entity1 && constraint2 == entity2) && !patternMatch) {
					foundMismatch = true;
				}
			}
			if( (constraint1 == entity1 && constraint2 == entity2) ) {
				if(constraint.getAllowed())
					foundMatch = true;
				else
					foundNotAllowed = true;
			}
		}
		
		if(foundNotAllowed)
			return false;
		if(!foundMatch && foundMismatch)
			return false;
		
		return true;
	}
	
	public static List<BindingPair> convertFromLList(LList<PairOfEntities> bindings) {
		if(bindings == null)
			return null;
		
		List<BindingPair> newBindings = new ArrayList<>();
		for(PairOfEntities pair : bindings) {
			newBindings.add(new BindingPair(pair));
		}
		return newBindings;
	}
	
	public static LList<PairOfEntities> convertToLList(List<BindingPair> bindings) {
		if(bindings == null || bindings.size() < 1)
			return null;
		
		LList<PairOfEntities> cons = new LList<PairOfEntities>();
		for(BindingPair pair : bindings) {
			cons = cons.cons(pair.toPairOfEntities());
		}
		return cons;
	}
	
	public static boolean equivalencyCheck(LList<PairOfEntities> set1, LList<PairOfEntities> set2) {
		return equivalencyCheck(convertFromLList(set1), convertFromLList(set2));
	}
	
	public static boolean equivalencyCheck(List<BindingPair> set1, List<BindingPair> set2) {
		if(set1 != null && set1.size() == 0)
			set1 = null;
		if(set2 != null && set2.size() == 0)
			set2 = null;
		if(set1 == null && set2 == null)
			return true;
		if((set1 == null) != (set2 == null))
			return false;
		
		if(set1.size() != set2.size()) {
			return false;
		}
		for(BindingPair pair1 : set1) {
			boolean found = false;
			for(BindingPair pair2 : set2) {
				if(pair1.equals(pair2)) {
					found = true;
					break;
				}
			}
			if(!found)
				return false;
		}
		return true;
	}
}
