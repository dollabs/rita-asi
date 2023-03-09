package jakeBarnwell.concept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import frames.entities.Entity;
import frames.entities.Thread;
import utils.Mark;

/**
 * A hierarchical (loosely mirroring an entity) model describing a plausible
 * concept pattern.
 * 
 * @author jb16
 *
 */
public class ConceptModel {
	/** The tree representing the system's model of a valid case */ 
	public ConceptTree conceptTree;
	
	/** All groups of things that must satisfy some structurally-based predicates */
	public Map<Constraint, Set<Group<NodeLocation>>> structuralConstraints = new HashMap<>();

	/** The list of examples supplied to this model */
	public List<Example> examples = new ArrayList<>();

	public ConceptModel() {
		// Initialize all constraints with empty sets
		for(Constraint c : Constraint.values()) {
			structuralConstraints.put(c, new HashSet<Group<NodeLocation>>());
		}
	}
	
	/**
	 * Queries the model to see if this entity is an instance
	 * of the concept.
	 * @param ent
	 * @return
	 */
	public boolean query(Entity ent) {
		EntityTree et = new EntityTree(ent, null);
		// Basically we just try to match this against the concept tree.
		boolean ctMatch = ConceptTree.matches(conceptTree, et);
		boolean constraintsSatisfied = this.constraintsSatisfied(et);
		
		return ctMatch && constraintsSatisfied;
	}
	
	/**
	 * Updates the model given a new example.
	 * 
	 * @param ex the new example
	 * @return the updated model (same object)
	 */
	public void update(Example ex) {
		doUpdate(ex);
		examples.add(ex);
	}

	private void doUpdate(Example ex) {
		Entity exPattern = ex.pattern();
		EntityTree exEntityTree = ex.tree();
		Charge exCharge = ex.charge();

		String sign = exCharge == Charge.POSITIVE ? Symbol.PLUS : Symbol.MINUS;
		Mark.say("  " + sign + " " + exPattern.asString());
		if(conceptTree == null) {
			conceptTree = new ConceptTree(exPattern, null);
		} else {
			// Do special cases if applicable
			boolean applyMacroConstraintsP = applyMacroConstraints(exPattern, exEntityTree, exCharge);
			if(applyMacroConstraintsP) {
				return;
			}
			
			PriorityQueue<TransformationPath> transformPaths = conceptTree.getTransformationPaths(exEntityTree, 1.5);

			// For now, only bother with the best transformation paths:
			TransformationPath bestPath = transformPaths.poll();
			
			// If no valid path, then can't do anything
			if(bestPath == null) {
				Mark.say("No action can be taken.");
				return;
			}
			
			// Go through the transformations and apply the generalizations to the model
			for(int i = 0; i < bestPath.getNumTransformations(); i++) {
				Transformation trans = bestPath.getTransformation(i);
				NodeLocation directions = trans.location;
				EntityOperator op = trans.type;
				ConceptTree operatedNode = (ConceptTree)conceptTree.follow(directions);
				ConceptTree transformedFrom = bestPath.getTreeBeforeTransformation(i);
				ConceptTree transformedTo = bestPath.getTreeAfterTransformation(i);

				if(op == EntityOperator.CHANGE_SPOOL) {
					// Update this concept tree node's belief of what is permissible
					Thread threadToConsider = ((EntityTree)exEntityTree.follow(directions)).getBundle().getPrimedThread();
					operatedNode.getSpool().incorporate(threadToConsider, exCharge);
				} else if(op == EntityOperator.NOT) {
					Mark.say("need to implement NOT change...");
				}// TODO this logic is acceptable but not exactly correct. What about adding a
				//   particular type of preposition, etc. etc.
				else if(op == EntityOperator.REMOVE_ELE) {
					// TODO Implement Charge
					if(exCharge == Charge.POSITIVE) {
						String labelRemoved = trans.eleRemoved.getLabel();
						
						Which optionalEles;
						if(labelRemoved.equals("object")) {
							// Mark the "object" (grammatical object) as optional, i.e. allows 
							//  either an intransitive or transitive verb.
							optionalEles = Which.only("object");
						} else {
							// Mark all elements (except for an "object" element) as optional, i.e.
							//  marks all prepositional phrases as optional.
							optionalEles = Which.ALL.except("object");
						}
						operatedNode.existenceQuantifiers.putIfAbsent(Quantifier.OPTIONAL_ELEMENT, new ArrayList<Which>());
						operatedNode.existenceQuantifiers.get(Quantifier.OPTIONAL_ELEMENT).add(optionalEles);
					} else {
						; // Do nothing, because we are a strict algorithm anyway.
					}
				} else if(op == EntityOperator.ADD_ELE) {
					// TODO Implement Charge
					if(exCharge == Charge.POSITIVE) {
						String labelAdded = trans.eleAdded.getLabel();
						
						Which optionalEles;
						if(labelAdded.equals("object")) {
							// Mark the "object" (grammatical object) as optional, i.e. allows 
							//  either an intransitive or transitive verb.
							optionalEles = Which.only("object");
						} else {
							// Mark all elements (except for an "object" element) as optional, i.e.
							//  marks all prepositional phrases as optional.
							optionalEles = Which.ALL.except("object");
						}
						operatedNode.existenceQuantifiers.putIfAbsent(Quantifier.OPTIONAL_ELEMENT, new ArrayList<Which>());
						operatedNode.existenceQuantifiers.get(Quantifier.OPTIONAL_ELEMENT).add(optionalEles);
					} else {
						; // Do nothing, because we are a strict algorithm anyway.
					}
				} else if(op == EntityOperator.CHANGE_ELE) {
					// TODO Implement Charge
					if(exCharge == Charge.POSITIVE) {
						String labelAdded = trans.eleAdded.getLabel();
						
						Which optionalEles;
						if(labelAdded.equals("object")) {
							// Mark the "object" (grammatical object) as optional, i.e. allows 
							//  either an intransitive or transitive verb.
							optionalEles = Which.only("object");
						} else {
							// Mark all elements (except for an "object" element) as optional, i.e.
							//  marks all prepositional phrases as optional.
							optionalEles = Which.ALL.except("object");
						}
						operatedNode.existenceQuantifiers.putIfAbsent(Quantifier.OPTIONAL_ELEMENT, new ArrayList<Which>());
						operatedNode.existenceQuantifiers.get(Quantifier.OPTIONAL_ELEMENT).add(optionalEles);
					} else {
						; // Do nothing, because we are a strict algorithm anyway.
					}
				} else if(op == EntityOperator.CHANGE_ELE) {
					String labelAdded = trans.eleAdded.getLabel();
					String labelRemoved = trans.eleRemoved.getLabel();
					// TODO not done here....
					
					Mark.say("Here we are! The labels are:", labelAdded, labelRemoved);					
				} else {
					// throw new RuntimeException("Illegal operator type");
					Mark.say("wrong op type", op);
				}
			}
		}
	}
	
	/**
	 * Checks if all macro constraints on this model are satisfied by 
	 * a given input {@link EntityTree}.
	 * @return
	 */
	private boolean constraintsSatisfied(EntityTree et) {
		Set<Group<NodeLocation>> constraintSet;
		for(Constraint c : Constraint.values()) {
			constraintSet = structuralConstraints.get(c);
			for(Group<NodeLocation> groupOfLocs : constraintSet) {
				// For the valid nodes, ensure they obey the constraint		
				if(!c.implore(et, groupOfLocs)) {
					return false;
				}
			}
			
		}
		
		return true;
	}
	
	/**
	 * Attempts to apply special-case model updates, if applicable. If an update
	 * is successfully executed via this method, other updates should probably
	 * not be applied.
	 * @param pattern
	 * @param tree
	 * @param charge
	 * @return true if we applied a special case to the model; else false.
	 */
	private boolean applyMacroConstraints(Entity pattern, EntityTree tree, Charge charge) {
		// TODO for -"x . x" and +"x . x" I'm assuming that implies sometihng
		//  about the macro structure. But these are only true if the prior examples
		//  don't say anything contradictory about it.
		
		// For something that Genesis thinks should match, but we tell it that it shouldn't
		if(charge == Charge.NEGATIVE && ConceptTree.matches(conceptTree, tree)) {
			// Get all groups of nodes that are the same
			Set<Group<NodeLocation>> same = tree.getSame(Constraint.SAMENESS_APPLICABLE, Constraint.SAMENESS_HASH);
			
			// For each group of nodes that are the same, apply a constraint
			//  that dictates that group of nodes should NOT be all the same
			//  (because this is a negative example)
			final Set<Group<NodeLocation>> constraintsNotAllSame = structuralConstraints.get(Constraint.NOT_ALL_SAME);
			same.forEach(sameGroup -> constraintsNotAllSame.add(sameGroup));
			return true;
		}
		
		// Tells Genesis to put two equal nodes as must-be-equal 
		if(charge == Charge.POSITIVE) {
			// Get the same-node groups
			Set<Group<NodeLocation>> same = tree.getSame(Constraint.SAMENESS_APPLICABLE, Constraint.SAMENESS_HASH);
			if(same.size() > 0) {
				// First ensure that no previous example has had these nodes be unequal
				for(Group<NodeLocation> sameGroup : same) {
					for(Example ex : examples) {
						EntityTree exTree = ex.tree();
						if(!Constraint.ALL_SAME.implore(exTree, sameGroup)) {
							break;
						}
//						Group<EntityTree> gg = sameGroup.stream().map(nl -> exTree.follow(nl)).collect(Collectors.toSet());
//						exTree.satisfies(Constraint.ALL_THE_SAME, sameGroup);
					} // TODO get all this logic right
//					Set<ConceptTree> correspondingCTs = 
//							sameGroup.stream().map(nl -> conceptTree.follow(nl)).collect(Collectors.toSet()); // TODO abstract this to another fn
					
				}
			}
			
		}
		
		return false;
	}

}
