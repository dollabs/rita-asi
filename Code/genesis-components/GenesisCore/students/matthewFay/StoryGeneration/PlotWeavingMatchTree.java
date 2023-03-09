package matthewFay.StoryGeneration;

import java.util.*;

import com.google.common.collect.Iterables;
import com.google.common.collect.MinMaxPriorityQueue;

import frames.entities.Entity;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;
import matthewFay.CharacterModeling.CharacterProcessor;
import matthewFay.Constraints.BindingInferenceUtility;
import matthewFay.Constraints.BindingSet;
import matthewFay.Constraints.ConstraintSet;
import matthewFay.StoryAlignment.Aligner;
import matthewFay.StoryAlignment.NWSequenceAlignmentScorer;
import matthewFay.StoryAlignment.SequenceAlignment;
import matthewFay.StoryAlignment.SortableAlignmentList;
import matthewFay.Utilities.EntityHelper;
import matthewFay.Utilities.HashMatrix;
import matthewFay.Utilities.OrderedHashMatrix;
import matthewFay.Utilities.EntityHelper.MatchNode;
import matthewFay.representations.BasicCharacterModel;

public class PlotWeavingMatchTree {
	private static boolean debug_logging = true;
	
	private List<BasicCharacterModel> characters;
	
	private List<Entity> all_generic_entities = null;
	private List<Entity> all_character_entities = null;
	
	private OrderedHashMatrix<Entity, BasicCharacterModel, Float> generic_score_matrix = null;
	private HashMatrix<Entity, Entity, List<PairOfEntities>> generic_constraint_matrix = null;
	private OrderedHashMatrix<BasicCharacterModel, BasicCharacterModel, Float> score_matrix = null;
	
	private MatchNode root_node;
	private MinMaxPriorityQueue<MatchNode> queue;
	
	private ArrayList<MatchNode> leaf_nodes;
	private boolean leaf_found;
	private float leaf_score;
	
	//Only used for testing/debugging purposes
	private List<PairOfEntities> actual_bindings = new ArrayList<PairOfEntities>();
	public List<PairOfEntities> getActualBindings() {
		return actual_bindings;
	}
	
	public PlotWeavingMatchTree(List<BasicCharacterModel> characters) {
		this.characters = characters;
		
		queue = MinMaxPriorityQueue.create();
		leaf_nodes = new ArrayList<>();
		
		actual_bindings = obtainActualBindings();
	}
	
	private void init() {
		leaf_nodes.clear();
		leaf_found = false;
		leaf_score = Float.NEGATIVE_INFINITY;
		
		queue.clear();
		
		all_generic_entities = new ArrayList<>();
		all_character_entities = new ArrayList<>();
		for(BasicCharacterModel character : characters) {
			for(Entity generic_entity : character.getGenericEntities()) {
				all_generic_entities.add(generic_entity);
			}
			all_character_entities.add(character.getEntity());
		}
		
		generateGenericEntitiesScoreMatrices(characters);
		//New, more important score method
		doAlignmentScore(new ArrayList<PairOfEntities>());
		
		root_node = new MatchNode();
		root_node.score = Float.POSITIVE_INFINITY; 
		root_node.generic_entities = new ArrayList<>(all_generic_entities);
		root_node.bindings = new ArrayList<>();
		
		//Do heuristic pruning here!
		//Anything with only one possible match to be a binding
		for(BasicCharacterModel origin_character : characters) {
			for(Entity generic_entity : origin_character.getGenericEntities()) {
				Set<BasicCharacterModel> characters = generic_score_matrix.keySetCols(generic_entity);
				if(characters.size() == 1) {
					BasicCharacterModel character = Iterables.getOnlyElement(characters);
					root_node.bindings.add(new PairOfEntities(generic_entity, character.getEntity()));
					root_node.generic_entities.remove(generic_entity);
				}
			}
		}
		
		//Prevent double matches for heuristics
		HashMap<BasicCharacterModel, Map<BasicCharacterModel, Integer>> origin_to_target_counts = new HashMap<>();
		for(BasicCharacterModel origin : characters) {
			origin_to_target_counts.put(origin, new HashMap<BasicCharacterModel, Integer>());
			for(BasicCharacterModel target : characters) {
				origin_to_target_counts.get(origin).put(target, 0);
			}
		}
		for(PairOfEntities binding : root_node.bindings) {
			Entity pattern = binding.getPattern();
			Entity datum = binding.getDatum();
			if(pattern != null && datum != null) {
				BasicCharacterModel origin = BasicCharacterModel.getOriginatingCharacter(pattern);
				BasicCharacterModel target = CharacterProcessor.getCharacterModel(datum, false);
				if(origin != null && target != null) {
					origin_to_target_counts.get(origin).put(target, origin_to_target_counts.get(origin).get(target)+1);
				}
			}
		}
		for(BasicCharacterModel origin : characters) {
			for(BasicCharacterModel target : characters) {
				Mark.say(origin+" to "+target+":"+origin_to_target_counts.get(origin).get(target));
				int count = origin_to_target_counts.get(origin).get(target);
				if(count > 1) {
					//Prune them all!
					Entity target_entity = target.getEntity();
					List<PairOfEntities> bindings_copy = new ArrayList<>(root_node.bindings);
					for(PairOfEntities binding : bindings_copy) {
						if(origin.getGenericEntities().contains(binding.getPattern())) {
							if(binding.getDatum() == target_entity) {
								root_node.bindings.remove(binding);
								root_node.generic_entities.add(binding.getPattern());
							}
						}
					}
				}
			}
		}
		
		//Check after bindings TOO
		Mark.say("Heuristic Bindings: \n"+root_node.bindings);
		doAlignmentScore(root_node.bindings);
		
		queue.add(root_node);
		
	}
	
	private List<PairOfEntities> obtainActualBindings() {
		List<PairOfEntities> bindings = new ArrayList<>();
		
		for(BasicCharacterModel character : characters) {
			for(Entity generic_entity : character.getGenericEntities()) {
				bindings.add(new PairOfEntities(generic_entity, character.getReplacedEntity(generic_entity)));
			}
		}
		
		return bindings;
	}
	
	public void generateMatchTree() {
		init();
		
		//Debugging - print out score matrix and constraint matrix
		if(debug_logging) {
			//printGenericsMatrix(generic_score_matrix, characters);
			//printConstraintsMatrix(generic_constraint_matrix, characters);
			//printScoreMatrix(score_matrix,characters);
			
			for(BasicCharacterModel character : characters) {
				String generics = character+"'s generics: ";
				for(Entity generic_entity : character.getGenericEntities()) {
					generics = generics + generic_entity + ", ";
				}
				Mark.say(generics);
			}
			
			Mark.say("Target Bindings:");
			Mark.say(actual_bindings);
			
		}
		
		//For debugging
		List<MatchNode> processed_nodes = new ArrayList<MatchNode>();
		int node_count = 0;
		
		while(!queue.isEmpty()) {
			MatchNode best_node = queue.poll();
			
			processed_nodes.add(best_node);
			node_count++;
			
			//Check if current node is a leaf node
			if(best_node.generic_entities.isEmpty()) {
				if(best_node.score >= leaf_score) {
					if(best_node.score > leaf_score)
						leaf_nodes.clear();
					if(!leaf_nodes.contains(best_node)) {
						leaf_nodes.add(best_node);
						leaf_found = true;
						if(best_node.score == Float.POSITIVE_INFINITY)
							best_node.score = doAlignmentScore(best_node.bindings);
						leaf_score = best_node.score;
						break;
					}
				}
				continue;
			}
			
			if(leaf_found) {
				if(best_node.score <= leaf_score) {
					queue.clear();
					break;
				}
			}
			
			Entity next_generic = best_node.generic_entities.get(0);
			
			//Add in nulls after bugs are fixed
			MatchNode next_node = createNode(best_node, next_generic, new Entity("null"));
			
			if(next_node.score > best_node.score) {
				Mark.err("Scores should never increase!");
				
			}
			
			if(next_node.score > Float.NEGATIVE_INFINITY) {
				queue.add(next_node);
			}
			
			List<BasicCharacterModel> ordered_target_characters = generic_score_matrix.getOrderedColKeySet(next_generic);
			
			for(BasicCharacterModel target_character : ordered_target_characters) {
				if(generic_score_matrix.get(next_generic, target_character) <= Float.NEGATIVE_INFINITY)
					break;
				if(!possibleTargets(next_generic, best_node.bindings).contains(target_character))
					continue;
				
				next_node = createNode(best_node, next_generic, target_character.getEntity());
										
				if(next_node.score > best_node.score) {
					Mark.err("Scores should never increase!");
					
				}
				
				if(next_node.score > Float.NEGATIVE_INFINITY) {
					queue.add(next_node);
				}
			}
			
			
		}
		
		Mark.say("Processed "+node_count+" nodes");
		
		Collections.sort(leaf_nodes);
		Mark.say("Plot Weave Matching Results:");
		for(MatchNode leaf_node : leaf_nodes) {
			Collections.sort(leaf_node.bindings, new Comparator<PairOfEntities>() {

				@Override
				public int compare(PairOfEntities arg0, PairOfEntities arg1) {
					String p0 = "null";
					if(arg0.getPattern() != null) p0 = arg0.getPattern().getType();
					String p1 = "null";
					if(arg1.getPattern() != null) p1 = arg1.getPattern().getType();
					int p = p0.compareTo(p1);
					if(p != 0) return p;
					String d0 = "null";
					if(arg0.getDatum() != null) d0 = arg0.getDatum().getType();
					String d1 = "null";
					if(arg1.getDatum() != null) d1 = arg1.getDatum().getType();
					int d = d0.compareTo(d1);
					return d;
				}
				
			});
			Mark.say("Score: "+leaf_node.score+"=>"+leaf_node.bindings);
			Mark.say("Validation: ");
			Mark.say(doAlignmentScore(leaf_node.bindings));
		}
	}
	
	private MatchNode createNode(MatchNode best_node, Entity next_generic, Entity target) {
		MatchNode next_node = new MatchNode();
		
		next_node.setParent(best_node);
		next_node.generic_entities = new ArrayList<>(best_node.generic_entities);
		next_node.generic_entities.remove(next_generic);
		
		next_node.bindings = new ArrayList<>(best_node.bindings);
		
		PairOfEntities new_binding = new PairOfEntities(next_generic, target);
		next_node.bindings.add(new_binding);		
//		next_node.score = scoreBindings(next_node.bindings);
		next_node.score = doAlignmentScore(next_node.bindings);
		
		return next_node;
	}
	
	private List<BasicCharacterModel> possibleTargets(Entity generic_entity, List<PairOfEntities> bindings) {
		ArrayList<BasicCharacterModel> possible_targets = new ArrayList<>();
		
		BasicCharacterModel originating_character = BasicCharacterModel.getOriginatingCharacter(generic_entity);
		List<Entity> sibling_generics = originating_character.getGenericEntities();
		List<BasicCharacterModel> sibling_targets = new ArrayList<>();
		for(PairOfEntities binding : bindings) {
			Entity pattern = binding.getPattern();
			if(sibling_generics.contains(pattern)) {
				Entity datum = binding.getDatum();
				if(!EntityHelper.isGeneric(datum) && !datum.entityP("null")) {
					BasicCharacterModel target_character = CharacterProcessor.getCharacterModel(datum, false);
					if(target_character != null) 
						sibling_targets.add(target_character);
				}
			}
		}
		for(BasicCharacterModel character : characters) {
			if(character != originating_character) {
				if(!sibling_targets.contains(character)) {
					possible_targets.add(character);
				}
			}
		}
		
		return possible_targets;
	}
	
	private float doAlignmentScore(List<PairOfEntities> bindings) {
		float total_score = 0;
		
		boolean record_results = false;
		if(score_matrix == null) {
			score_matrix = new OrderedHashMatrix<>();
			record_results = true;
		}
		
		BindingInferenceUtility biu = new BindingInferenceUtility(bindings);
		biu.inferTargetsFromChaining();
		biu.inferEquivalentGenerics();
		biu.requireBindingsFor(all_generic_entities);
		biu.addTargets(all_character_entities);
		List<PairOfEntities> all_bindings = biu.getTwoWayBindings();
		
		//AHAH Scoring should ACTUALLY be done on a character-character basis, NOT a generic-character basis!
		for(int i=0;i<characters.size();i++) {
			BasicCharacterModel character_i = characters.get(i);
			for(int j=i+1;j<characters.size();j++) {
				BasicCharacterModel character_j = characters.get(j);
				
				List<Entity> character_i_plot = character_i.getGeneralizedCharacterStory();
				List<Entity> character_j_plot = character_j.getGeneralizedCharacterStory();
				
				//Reeaally fast aligner
				NWSequenceAlignmentScorer scorer = new NWSequenceAlignmentScorer(character_i_plot, character_j_plot);
				
				SequenceAlignment alignment = scorer.align(all_bindings);
				
				total_score += alignment.score;
				
				if(record_results) {
					score_matrix.put(character_i, character_j, alignment.score);
					score_matrix.put(character_j, character_i, alignment.score);
				}
			}
		}
		
		return total_score;
	}
	
	private List<PairOfEntities> resolveBindingChaining(List<PairOfEntities> bindings) {
		List<PairOfEntities> new_bindings = new ArrayList<PairOfEntities>();
		
		BindingSet bs = new BindingSet(bindings);
		
		for(PairOfEntities binding : bindings) {
			//First try forward chaining
			//i.e. if a pattern is generic and points to a character
			//then find
			Entity pattern = binding.getPattern();
			Entity datum = bs.getDatum(pattern);
			new_bindings.add(binding);
			
			if(EntityHelper.isGeneric(pattern)) {
				Entity chained_datum = bs.getDatum(datum);
				while(chained_datum != null) {
					new_bindings.add(new PairOfEntities(pattern, chained_datum));
					chained_datum = bs.getDatum(chained_datum);
				}
			}
		}
		
		return new_bindings;
	}
	
	private List<PairOfEntities> filterBindings(List<PairOfEntities> all_bindings, List<Entity> patterns, List<Entity> datums) {
		List<PairOfEntities> bindings = new ArrayList<>();
		
		for(PairOfEntities binding : all_bindings) {
			if(patterns.contains(binding.getPattern()) || datums.contains(binding.getDatum()))
				bindings.add(binding);
		}
		
		return bindings;
	}
	
	private List<PairOfEntities> getConstraints(Entity generic, Entity target) {
		return getConstraints(generic, target, new ArrayList<PairOfEntities>());
	}
	
	private List<PairOfEntities> getConstraints(Entity generic, Entity target, List<PairOfEntities> checked) {
		//Check if checked
		for(PairOfEntities check : checked) {
			if(check.getPattern().equals(generic) && check.getDatum().equals(target))
				return new ArrayList<>();
		}
		checked.add(new PairOfEntities(generic, target));
		
		List<PairOfEntities> constraints = generic_constraint_matrix.get(generic, target);
		
		List<PairOfEntities> new_constraints = new ArrayList<>( constraints );
		for(PairOfEntities constraint : new_constraints) {
			if(generic_constraint_matrix.contains(constraint.getPattern(), constraint.getDatum())) {
				constraints.addAll(getConstraints(constraint.getPattern(), constraint.getDatum(), checked));
			}
		}
		
		return constraints;
	}
	
	public List<PairOfEntities> getBestBindings() {
		if(leaf_found == false) {
			this.generateMatchTree();
		}
		if(leaf_nodes.size() > 0)
			return leaf_nodes.get(0).bindings;
		Mark.err("Unknown Error; No leaf nodes found!");
		return new ArrayList<PairOfEntities>();
	}

	private BasicCharacterModel getCharacterModel(Entity character_entity) {
		for(BasicCharacterModel character : characters) {
			if(character.getEntity().equals(character_entity))
				return character;
		}
		return null;
	}
	
	private void generateGenericEntitiesScoreMatrices(List<BasicCharacterModel> characters) {
		generic_score_matrix = new OrderedHashMatrix<>();
		generic_constraint_matrix = new HashMatrix<>();
		
		//Old method still important for pruing purposes ONLY!
		Aligner aligner = new Aligner();
		
		//Doing the characters in order, just grab the generics at random
		for(BasicCharacterModel originating_character : characters) 
		{	
			//For each generic from this character
			for(Entity generic_entity : originating_character.getGenericEntities()) {
				List<BasicCharacterModel> possible_characters = new ArrayList<>(characters);
				possible_characters.remove(originating_character);
				
				List<Entity> generics_plot = originating_character.getGeneralizedCharacterStory();
				generics_plot = filterPlotElts(generic_entity, generics_plot);
				
				//For each character
				for(BasicCharacterModel target_character : possible_characters) {
					//Setup a binding of possible character to generic character being matched
					
					float score = 0;					
					List<Entity> targets_plot = target_character.getGeneralizedCharacterStory();
					
					PairOfEntities new_pairing = new PairOfEntities(generic_entity, target_character.getEntity());
					LList<PairOfEntities> bindings = new LList<>(new_pairing);
					
					
					SortableAlignmentList sal = aligner.align(generics_plot,targets_plot,bindings);
					
					if(sal.size() <= 0) {
						score = Float.NEGATIVE_INFINITY;
//						constraint_matrix.put(generic_entity, target_character.getEntity(), new ArrayList<PairOfEntities>());
						
					} else {
						SequenceAlignment alignment = (SequenceAlignment)sal.get(0);
						if(alignment.getMatchCount() == 0)
							alignment.score = Float.NEGATIVE_INFINITY;
						score = alignment.score;
						
						//Add Constraints
						List<PairOfEntities> constraints = filterBindingsToGenericsConstraints(alignment.bindings);
						//Validate that all generic constraints point to either nulls or characters
						for(PairOfEntities constraint : constraints) {
							if(EntityHelper.isGeneric(constraint.getPattern()) && !EntityHelper.isGeneric(constraint.getDatum())) {
								if(getCharacterModel(constraint.getDatum()) == null) {
									score = Float.NEGATIVE_INFINITY;
								}
							}
						}
						
						if(score > Float.NEGATIVE_INFINITY)
							generic_constraint_matrix.put(generic_entity, target_character.getEntity(), constraints);
					}
					
					if(score > Float.NEGATIVE_INFINITY)
						generic_score_matrix.put(generic_entity, target_character, score);
				}
			}
		}
	}
	
	private List<PairOfEntities> filterBindingsToGenericsConstraints(LList<PairOfEntities> bindings) {
		List<PairOfEntities> constraints = new ArrayList<>();
		
		for(PairOfEntities binding : bindings) {
			if(binding.getDatum() == null || binding.getPattern() == null)
				continue;
			if(binding.getDatum().entityP("null") || binding.getPattern().entityP("null"))
				continue;
			if(EntityHelper.isGeneric(binding.getDatum())) {
				constraints.add(new PairOfEntities(binding.getDatum(), binding.getPattern()));
				continue;
			}
			if(EntityHelper.isGeneric(binding.getPattern())) {
				constraints.add(new PairOfEntities(binding.getPattern(), binding.getDatum()));
				continue;
			}
		}
		
		return constraints;
	}
	
	private List<Entity> filterPlotElts(Entity req_elt, List<Entity> plotElts) {
		List<Entity> filteredPlotElts = new ArrayList<Entity>();
		
		for(Entity plotElt : plotElts) {
			if(EntityHelper.contains(req_elt, plotElt))
				filteredPlotElts.add(plotElt);
		}
		
		return filteredPlotElts;
	}
	
	
	//Match nodes are used to construct search tree
	private class MatchNode implements Comparable<MatchNode> {
		private MatchNode parent;
		private int depth = 0;

		private Vector<MatchNode> children;

		public void setParent(MatchNode node) {
			if (parent == node) return;

			if (parent != null) {
				parent.children.remove(this);
			}

			node.children.add(this);
			parent = node;
			depth = parent.depth+1;
		}
		
		public List<PairOfEntities> bindings;
		public List<Entity> generic_entities;
		
		public float score;

		public MatchNode() {
			parent = null;
			children = new Vector<MatchNode>();
//			constraints = new ArrayList<PairOfEntities>();
			generic_entities = new ArrayList<>();
			score = Float.NEGATIVE_INFINITY;
		}

		/**
		 * Used for sorting highest to lowest
		 */
		@Override
		public int compareTo(MatchNode o) {
			if (score > o.score) return -1;
			if (score < o.score) return 1;
			if (generic_entities.size() < o.generic_entities.size()) return -1;
			if (generic_entities.size() > o.generic_entities.size()) return 1;
			return 0;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof MatchNode) {
				MatchNode node = (MatchNode)o;
				if(score != node.score)
					return false;
				if(bindings.size() != node.bindings.size())
					return false;
				for(PairOfEntities binding : bindings) {
					boolean found_match = false;
					for(PairOfEntities constraint_check : node.bindings) {
						if(binding.getDatum().equals(constraint_check.getDatum())
								&& binding.getPattern().equals(constraint_check.getPattern())) {
							found_match = true;
							break;
						}
					}
					if(!found_match)
						return false;
				}
				return true;
			}
			return false;
		}
		
		@Override
		public String toString() {
			return ""+bindings+":"+score;
		}
	}
	
	//Debugging Output etc...
	private static void printGenericsMatrix(HashMatrix<Entity, BasicCharacterModel, Float> matrix, List<BasicCharacterModel> characters) {
		for(BasicCharacterModel character : characters) {
			Mark.say(character.getEntity());
			for(Entity generic_entity : character.getGenericEntities()) {				
				for(BasicCharacterModel target_character : matrix.keySetCols(generic_entity)) {
					Mark.say(" - "+generic_entity+"=>"+target_character.getEntity()+"="+matrix.get(generic_entity, target_character));
				}
			}
		}
	}
	
	private static void printConstraintsMatrix(HashMatrix<Entity, Entity, List<PairOfEntities>> matrix, List<BasicCharacterModel> characters) {
		for(BasicCharacterModel character : characters) {
			Mark.say(character.getEntity());
			for(Entity generic_entity : character.getGenericEntities()) {				
				for(Entity target_character : matrix.keySetCols(generic_entity)) {
					Mark.say(" - "+generic_entity+"=>"+target_character+"= "+matrix.get(generic_entity, target_character));
				}
			}
		}
	}
	
	private static void printScoreMatrix(HashMatrix<BasicCharacterModel, BasicCharacterModel, Float> matrix, List<BasicCharacterModel> characters) {
		for(BasicCharacterModel character : characters) {
			Mark.say(character.getEntity());			
			for(BasicCharacterModel target_character : matrix.keySetCols(character)) {
				Mark.say(" - "+character.getEntity()+":"+target_character.getEntity()+"= "+matrix.get(character, target_character));
			}
		}
		
	}
}
