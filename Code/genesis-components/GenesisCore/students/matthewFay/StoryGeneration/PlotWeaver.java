package matthewFay.StoryGeneration;

import generator.Generator;

import java.util.*;

import connections.signals.BetterSignal;
import matthewFay.CharacterModeling.CharacterProcessor;
import matthewFay.StoryAlignment.Aligner;
import matthewFay.StoryAlignment.SequenceAlignment;
import matthewFay.StoryAlignment.SortableAlignmentList;
import matthewFay.Utilities.EntityHelper;
import matthewFay.Utilities.HashMatrix;
import matthewFay.Utilities.OrderedHashMatrix;
import matthewFay.representations.BasicCharacterModel;
import matthewFay.viewers.CharacterViewer;
import frames.entities.Entity;
import translator.BasicTranslator;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;

/***
 * PlotWeaver is a class designed to tie together plots of individual characters into a coherent story
 * 
 * Considerations made while designing/developing/researching:
 * - Often characters from the same original story may be difficult to re-weave
 * - Doing a simple greedy approach works fairly well, but not well enough
 * - (mostly removed) Tried grabbing extra generics-bindings during each pass but was complicated without much reward
 * - (unecessary, unless doing the extra bindings stuff) Ordering characters by length can help find optimal matches more easily
 * - (Mostly done) Adding ordered set of testable bindings seems like a good approach
 * - (Todo) Once bindings are perfect, need to work on combing this mechanism with generation
 * 
 * @author Matthew
 *
 */

public class PlotWeaver {
	public static boolean debug_logging = false;
	public static boolean isWeaveCharactersEvent(Entity event) {
		if(event.relationP("weave")) {
			if(event.getSubject().entityP("you")) {
				if(event.getObject().sequenceP("roles")) {
					if(event.getObject().getElement(0).functionP("object")) {
						if(event.getObject().getElement(0).getSubject().entityP("plots")) {
							if(event.getObject().getElement(1).functionP("for")) {
								if(event.getObject().getElement(1).getSubject().entityP("character")) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	private boolean do_gap_filling = true;
	public void setDoGapFilling(boolean value) {
		do_gap_filling = value;
	}
	
	private boolean purge_null_generics = true;
	public void setPurgeNullGenerics(boolean value) {
		purge_null_generics = value;
	}
	
	private List<BasicCharacterModel> characters;
	public boolean isCharacter(Entity e, List<BasicCharacterModel> characters) {
		for(BasicCharacterModel bcm : characters) {
			if(bcm.getEntity().isEqual(e))
				return true;
		}
		return false;
	}
	public BasicCharacterModel getCharacter(Entity e, List<BasicCharacterModel> character) {
		for(BasicCharacterModel bcm : characters) {
			if(bcm.getEntity().isEqual(e))
				return bcm;
		}
		return null;
	}
	
	public List<Entity> filterPlotElts(Entity req_elt, List<Entity> plotElts) {
		List<Entity> filteredPlotElts = new ArrayList<Entity>();
		
		for(Entity plotElt : plotElts) {
			if(EntityHelper.contains(req_elt, plotElt))
				filteredPlotElts.add(plotElt);
		}
		
		return filteredPlotElts;
	}
	
	public LList<PairOfEntities> filterBindingsToConstraints(LList<PairOfEntities> bindings, Entity e) {
		LList<PairOfEntities> constraints = new LList<>();
		
		for(PairOfEntities binding : bindings) {
			if(binding.getDatum() == null || binding.getPattern() == null)
				continue;
			if(binding.getDatum().entityP("null") || binding.getPattern().entityP("null"))
				continue;
			if(binding.getPattern().equals(e))
				continue;
			if(EntityHelper.isGeneric(binding.getDatum()) || EntityHelper.isGeneric(binding.getPattern()))
				constraints = constraints.cons(binding);
		}
		
		return constraints;
	}
	
	public float scoreGenericWithTarget(Entity generic_entity, List<Entity> generic_entitys_plot, BasicCharacterModel target) {
		Aligner aligner = new Aligner();
		
		List<Entity> targets_plot = target.getGeneralizedCharacterStory();
		
		PairOfEntities new_pairing = new PairOfEntities(generic_entity, target.getEntity());
		LList<PairOfEntities> bindings = new LList<>(new_pairing);
		
		SortableAlignmentList sal = aligner.align(generic_entitys_plot,targets_plot,bindings);
		
		if(sal.size() <= 0) {
			return Float.NEGATIVE_INFINITY;
		}
		
		SequenceAlignment alignment = (SequenceAlignment)sal.get(0);
		
		if(alignment.getMatchCount() == 0) {
			return Float.NEGATIVE_INFINITY;
		}
		
		return alignment.score;
	}
	
	public OrderedHashMatrix<Entity, BasicCharacterModel, Float> generateGenericEntitiesScoreMatrix(List<BasicCharacterModel> characters) {
		OrderedHashMatrix<Entity, BasicCharacterModel, Float> matrix = new OrderedHashMatrix<>();
		
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
					
					float score = scoreGenericWithTarget(generic_entity, generics_plot, target_character);
					matrix.put(generic_entity, target_character, score);
				}
			}
		}
		
		return matrix;
	}
	
	public Map<Entity, BasicCharacterModel> findBestGenericTargets(List<BasicCharacterModel> characters, OrderedHashMatrix<Entity, BasicCharacterModel, Float> generic_entity_score_matrix) {
		Map<Entity, BasicCharacterModel> generic_entity_to_target_character = new HashMap<>();
		
		//Use HashMatrix to find best match
		Map<Entity, Float> generic_entity_to_target_score = new HashMap<>();
		for(BasicCharacterModel originating_character : characters) {
			//For a particular originating character, we can only use each target ONCE
			Map<BasicCharacterModel, Entity> target_character_to_generic_entity = new HashMap<>();
			
			boolean satisfied_all_generics = false;
			while(!satisfied_all_generics) {
				satisfied_all_generics = true;
				boolean change_detected = false;
				for(Entity generic_entity : originating_character.getGenericEntities()) {
					for(BasicCharacterModel target_character : generic_entity_score_matrix.keySetCols(generic_entity)) {
						float score = generic_entity_score_matrix.get(generic_entity, target_character);
						if(   score > Float.NEGATIVE_INFINITY // There needs to be a reaon to match generic to this target
								&& ( !generic_entity_to_target_character.containsKey(generic_entity) // either there is no current target
								|| score > generic_entity_to_target_score.get(generic_entity)  ) ) { // or this is better than the current target
							
							if(!target_character_to_generic_entity.containsKey(target_character)) { //case if target already has a 
								if(generic_entity_to_target_character.containsKey(generic_entity)) 
									target_character_to_generic_entity.remove(generic_entity_to_target_character.get(generic_entity));
								generic_entity_to_target_character.put(generic_entity, target_character);
								target_character_to_generic_entity.put(target_character, generic_entity);
								generic_entity_to_target_score.put(generic_entity, score);
								change_detected = true;
							} else {
								Entity conflicting_generic_entity = target_character_to_generic_entity.get(target_character);
								float conflicting_score = generic_entity_score_matrix.get(conflicting_generic_entity, target_character);
								Mark.say(debug_logging, generic_entity+" wants "+target_character+"with score "+score+", but "+conflicting_generic_entity+" has it with score "+conflicting_score);
								
								if(score > conflicting_score) {
									generic_entity_to_target_character.put(generic_entity, target_character);
									target_character_to_generic_entity.put(target_character, generic_entity);
									generic_entity_to_target_score.put(generic_entity, score);
									change_detected = true;
									
									//Mark conflicting_entity as unsolved, find a new target for it later
									generic_entity_to_target_character.remove(conflicting_generic_entity);
									generic_entity_to_target_score.remove(conflicting_generic_entity);
									satisfied_all_generics = false;
								} 
							}
						}
					}
				}
				if(!satisfied_all_generics && !change_detected) {
					Mark.err("infiloop");
					System.exit(1);
				}
			}
		}
		
		return generic_entity_to_target_character;
	}
	
	public List<PairOfEntities> mapToBindingsList(Map<Entity, BasicCharacterModel> generic_entity_to_target_character) {
		List<PairOfEntities> bindings = new ArrayList<>();
		Mark.say(debug_logging, "Generic to Character mappings");
		//Every generic should have a matching character now
		for(Entity e : generic_entity_to_target_character.keySet()) {
			Mark.say(debug_logging, e+" maps to "+generic_entity_to_target_character.get(e).getEntity());
			bindings.add(new PairOfEntities(e, generic_entity_to_target_character.get(e).getEntity()));
		}
		return bindings;
	}
	
	public Map<Entity, BasicCharacterModel> bindingsListToMap(List<PairOfEntities> bindings, List<BasicCharacterModel> characters) {
		Map<Entity, BasicCharacterModel> generic_entity_to_target_character = new HashMap<>();
		
		for(PairOfEntities binding : bindings) {
			Entity generic = binding.getPattern();
			Entity target = binding.getDatum();
			
			for(BasicCharacterModel character : characters) {
				if(character.getEntity().equals(target)) {
					generic_entity_to_target_character.put(generic, character);
					break;
				}
			}
		}
		
		return generic_entity_to_target_character;
	}
	
	public void normalizeScoreMatrixOnGenerics(HashMatrix<Entity, BasicCharacterModel, Float> matrix) {
		List<Entity> generic_entities = new ArrayList<>(matrix.keySetRows());
		for(Entity generic_entity : generic_entities) {
			List<BasicCharacterModel> target_characters = new ArrayList<>(matrix.keySetCols(generic_entity));
			float min = Float.POSITIVE_INFINITY;
			float max = Float.NEGATIVE_INFINITY;
			
			float sumsqs = 0;
			
			for(BasicCharacterModel target_character : target_characters) {
				float score = matrix.get(generic_entity, target_character);
				
				sumsqs += (score*score);
				
				min = min < score ? min : score;
				max = max > score ? max : score;
			}
			float range = max - min;
			
			float norm = (float)Math.sqrt(sumsqs);
			if(norm != 0) {
				for(BasicCharacterModel target_character : target_characters) {
					float score = matrix.get(generic_entity, target_character);
					float normal_score = score/norm;
					matrix.put(generic_entity, target_character, normal_score);
				}
			}
		}
	}
	
	public PlotWeaver(List<BasicCharacterModel> characters) {
		this.characters = characters;
	}
	
	public List<Entity> weavePlots() {
		List<Entity> wovenPlotElts = new ArrayList<>();

		OrderedHashMatrix<Entity, BasicCharacterModel, Float> generic_entity_score_matrix = generateGenericEntitiesScoreMatrix(characters);			
		
//		normalizeScoreMatrixOnGenerics(generic_entity_score_matrix);
		
		Map<Entity, BasicCharacterModel> generic_entity_to_target_character = findBestGenericTargets(characters, generic_entity_score_matrix);
		
		List<PairOfEntities> bindings = mapToBindingsList(generic_entity_to_target_character);
		
		PlotWeavingMatchTree pwmt = new PlotWeavingMatchTree(characters);
		pwmt.generateMatchTree();
		bindings = pwmt.getBestBindings();
//		bindings = pwmt.getActualBindings();
		generic_entity_to_target_character = bindingsListToMap(bindings, characters);
		
		//Debugging Output//
		for(BasicCharacterModel c : characters) {
			String generics = c+"'s generics: ";
			for(Entity g : c.getGenericEntities()) {
				BasicCharacterModel t = generic_entity_to_target_character.get(g);
				generics = generics + "("+g+"=>"+t+"), ";
			}
			Mark.say(generics);
		}
		///////////////////
		
		//Get the plot threads we're going to be working with
		HashMap<BasicCharacterModel, List<Entity>> character_plot_threads = new HashMap<BasicCharacterModel, List<Entity>>();
		for(BasicCharacterModel character : characters) {
			character_plot_threads.put(character, new ArrayList<Entity>());
			List<Entity> plot_elts = character.getGeneralizedCharacterStory();
			for(Entity gen_plot_elt : plot_elts) {
				Entity plot_elt = gen_plot_elt.deepClone(false);
				
				//Here's a good spot to kill null generics, by removing all plot units with a null generic
				boolean null_generic_detected = false;
				List<Entity> entities = EntityHelper.getAllEntities(plot_elt);
				for(Entity entity : entities) {
					if(EntityHelper.isGeneric(entity)) {
						if(!generic_entity_to_target_character.containsKey(entity)) {
							null_generic_detected = true;
						}
					}
				}
				
				if(!purge_null_generics || !null_generic_detected)
					character_plot_threads.get(character).add(plot_elt);
			}
		}
		
		//Replace the generics with specifics in the plots
		for(BasicCharacterModel character : characters) {
			List<Entity> plot_elts = character_plot_threads.get(character);
			for(Entity plot_elt : plot_elts) {
				EntityHelper.findAndReplace(plot_elt, bindings, true);
			}
		}
		//Simplify the binding list
		List<PairOfEntities> characterToSelfBindings = new ArrayList<>();
		for(BasicCharacterModel character : characters) {
			characterToSelfBindings.add(new PairOfEntities(character.getEntity(), character.getEntity()));
		}
		
		//Do Plot Gap Filling if appropriate
		if(do_gap_filling) {
			Aligner aligner = new Aligner();
			
			for(BasicCharacterModel character_left : characters) {
				for(BasicCharacterModel character_right : characters) {
					//Skip if the same
					if(character_left == character_right)
						continue;
					List<Entity> left_plot = character_plot_threads.get(character_left);
					List<Entity> right_plot = character_plot_threads.get(character_right);
					
					SequenceAlignment sa = (SequenceAlignment)aligner.align(left_plot, right_plot, characterToSelfBindings).get(0);
					
//					Mark.say(simpleBindings);
//					Mark.say(sa);
					
					//Add a gap-filler-hack to push kill/eat as far forward in plot as possible
					sa.hackAlignment(character_right.getEntity(), character_left.getEntity());
					
					boolean gaps_filled = sa.selectiveFillGaps(character_right.getEntity(), character_left.getEntity());
					
//					Mark.say(sa);
					
					if(gaps_filled) {
						int i = 0;
						int l_index = 0;
						int r_index = 0;
						while(i < sa.size()) {
							Entity l_aligned_plot_elt = sa.get(i).a;
							Entity r_aligned_plot_elt = sa.get(i).b;
							
							if(l_aligned_plot_elt != null) {
								if(l_index >= left_plot.size() || !left_plot.get(l_index).equals(l_aligned_plot_elt)) {
									left_plot.add(l_index, l_aligned_plot_elt);
									Mark.say("Gap filled "+character_left+": "+l_aligned_plot_elt);
								}
								l_index++;
							}
							if(r_aligned_plot_elt != null) {
								if(r_index >= right_plot.size() || !right_plot.get(r_index).equals(r_aligned_plot_elt)) {
									right_plot.add(r_index, r_aligned_plot_elt);
									Mark.say("Gap filled "+character_right+": "+r_aligned_plot_elt);
								}
								r_index++;
							}
							
							i++;
						}
					}
				}
			}
		}
		
		//Update Character Models with Plots
		for(BasicCharacterModel character : characters) {
			List<Entity> new_plot = character_plot_threads.get(character);
			character.replaceParticipantEvents(new_plot, bindings);
		}
	
		
		//Check that plots are good
		for(BasicCharacterModel character : character_plot_threads.keySet()) {
			List<Entity> plot_elts = character_plot_threads.get(character);
			Mark.say(debug_logging, "Pre-weaving plot for "+character.getEntity());
			for(Entity plot_elt : plot_elts) {
				Mark.say(debug_logging, plot_elt);
			}
		}
	
		//Okay, finally do the actual weaving of character plots together
		//Setup a table for the indexes of the character plot elts
		final LinkedHashMap<BasicCharacterModel, Integer> character_plot_index = new LinkedHashMap<>();
		for(BasicCharacterModel character : characters) {
			character_plot_index.put(character, 0);
		}
		
		//The main loop - go through all the plots and insert elts into the woven story
		//Try to get all the character plots to the end (increment all the indexs past ends of associated plots)
		boolean all_done = false;
		while(!all_done) {
			//Go through each character and add all plots with no blockers
			boolean blocked = true;
			
			int character_index = 0;
			for(BasicCharacterModel cur_character : characters) {
				
				//Track where we are in this character's plot
				int cpi = character_plot_index.get(cur_character);
				if(cpi >= character_plot_threads.get(cur_character).size())
					continue;
				//Track if this character is blocked
				boolean nonblocking = true;
				
				Entity next_plot_elt = character_plot_threads.get(cur_character).get(cpi);
				Set<BasicCharacterModel> inc_characters = prune_to_characters(EntityHelper.getAllEntities(next_plot_elt), characters);
				//Check that there is only the current character in the entity list, if so plot elt is non-blocking
				
				if(inc_characters.size() > 1) {
					nonblocking = false;
				}
				//If non-blockign element found, add to plot and continue
				if(nonblocking) {
					cpi += 1;
					character_plot_index.put(cur_character, cpi);
					wovenPlotElts.add(next_plot_elt);
					blocked = false;
				}
			}
			
			//Strategy1 for unblocking, search through blocked events, check each other blocked character in inc_characters
			//if those characters are all blocked on the same event, add it and increment all cpis
			if(blocked) {
				for(BasicCharacterModel cur_character : characters) {
					int cpi = character_plot_index.get(cur_character);
					if(cpi >= character_plot_threads.get(cur_character).size())
						continue;
					Entity blocked_plot_elt = character_plot_threads.get(cur_character).get(cpi);
					Set<BasicCharacterModel> inc_characters = prune_to_characters(EntityHelper.getAllEntities(blocked_plot_elt), characters);
					int elt_count = 0;
					for(BasicCharacterModel inc_character : inc_characters) {
						int inc_cpi = character_plot_index.get(inc_character);
						if(inc_cpi >= character_plot_threads.get(inc_character).size())
							continue;
						Entity inc_character_plot_elt = character_plot_threads.get(inc_character).get(inc_cpi);
						if(blocked_plot_elt.isDeepEqual(inc_character_plot_elt)) {
							elt_count++;
						}
					}
					if(elt_count == inc_characters.size()) {
						//Unblocked!
						wovenPlotElts.add(blocked_plot_elt);

						for(BasicCharacterModel inc_character : inc_characters) {
							int inc_cpi = character_plot_index.get(inc_character);
							inc_cpi += 1;
							character_plot_index.put(inc_character, inc_cpi);
						}
						blocked = false;
						break;
					}
				}
			}
			
			//Strategy 2 - still blocked, choose an event that is earliest in a character's story and execute it
			boolean use_plotbreaker_strategy = true;
			if(blocked && use_plotbreaker_strategy) {
				List<BasicCharacterModel> sort_by_cpi = new ArrayList<BasicCharacterModel>(characters);
				
				Collections.sort(sort_by_cpi, new Comparator<BasicCharacterModel>() {

					@Override
					public int compare(BasicCharacterModel arg0,
							BasicCharacterModel arg1) {
						int cpi0 = character_plot_index.get(arg0);
						int cpi1 = character_plot_index.get(arg1);
						// TODO Auto-generated method stub
						return -1*Integer.compare(cpi0, cpi1);
					}
					
				});
				
				for(BasicCharacterModel cur_character : sort_by_cpi) {
					int cpi = character_plot_index.get(cur_character);
					if(cpi >= character_plot_threads.get(cur_character).size())
						continue;
					Entity blocked_plot_elt = character_plot_threads.get(cur_character).get(cpi);
					Set<BasicCharacterModel> inc_characters = prune_to_characters(EntityHelper.getAllEntities(blocked_plot_elt), characters);
					int elt_count = 0;
					for(BasicCharacterModel inc_character : inc_characters) {
						int inc_cpi = character_plot_index.get(inc_character);
						if(inc_cpi >= character_plot_threads.get(inc_character).size())
							continue;
						while(inc_cpi < character_plot_threads.get(inc_character).size()) {
							Entity inc_character_plot_elt = character_plot_threads.get(inc_character).get(inc_cpi);
							if(blocked_plot_elt.isDeepEqual(inc_character_plot_elt)) {
								elt_count++;
								break;
							}
							inc_cpi++;
						}
					}
					if(elt_count == inc_characters.size()) {
						//Unblocked!
						wovenPlotElts.add(blocked_plot_elt);

						for(BasicCharacterModel inc_character : inc_characters) {
							int inc_cpi = character_plot_index.get(inc_character);
							while(inc_cpi < character_plot_threads.get(inc_character).size()) {
								Entity inc_character_plot_elt = character_plot_threads.get(inc_character).get(inc_cpi);
								if(blocked_plot_elt.isDeepEqual(inc_character_plot_elt)) {
									character_plot_threads.get(inc_character).remove(inc_cpi);
									break;
								}
								inc_cpi++;
							}
						}
						blocked = false;
						break;
					}
				}
			}
			
			//Temporary, exit loop if everything is blocked
			if(blocked) {
				Mark.say("-----Blocked during plot weaving!");
				Mark.say("-----Blocked on:");
				
				for(BasicCharacterModel blocked_character : character_plot_index.keySet()) {
					Mark.say("------"+blocked_character.getSimpleName()+"'s plot");
					for(Entity plot_elt : character_plot_threads.get(blocked_character)) {
						Mark.say(plot_elt);
					}
					Mark.say("------");
				}
				
				for(BasicCharacterModel blocked_character : character_plot_index.keySet()) {
					int cpi = character_plot_index.get(blocked_character);
					if(cpi >= character_plot_threads.get(blocked_character).size())
						continue;
					Entity next_plot_elt = character_plot_threads.get(blocked_character).get(cpi);
					Mark.say(blocked_character.getEntity());
					Mark.say(next_plot_elt);
					
					
//					for(Entity generic_entity : blocked_character.getGenericEntities()) {
//						Mark.say("From: "+blocked_character+" - "+generic_entity+"=>"
//					            + (generic_entity_to_target_character.containsKey(generic_entity) ? 
//					            		generic_entity_to_target_character.get(generic_entity).getEntity()+"="+generic_entity_score_matrix.get(generic_entity, generic_entity_to_target_character.get(generic_entity)) 
//					            		:  "null")); 
								
//						for(BasicCharacterModel target_character : generic_entity_score_matrix.keySetCols(generic_entity)) {
//							Mark.say(" - "+generic_entity+"=>"+target_character.getEntity()+"="+generic_entity_score_matrix.get(generic_entity, target_character));
//						}
//					}
				}
			
				
				
				Mark.say("-----End of Blocking Error");
				all_done = true;
				break;
			}
			
			//Exit criterion, check indicies
			all_done = true;
			for(BasicCharacterModel character : character_plot_index.keySet()) {
				int cpi = character_plot_index.get(character);
				if(cpi < character_plot_threads.get(character).size()) {
					all_done = false;
					break;
				}
			}
		}
		
		return wovenPlotElts;
	}
	
	private List<BasicCharacterModel> sort_characters_by_plot_length(List<BasicCharacterModel> characters) {
		List<BasicCharacterModel> sorted_characters = new ArrayList<>(characters);
		Collections.sort(sorted_characters, new Comparator<BasicCharacterModel>() {

			@Override
			public int compare(BasicCharacterModel arg0,
					BasicCharacterModel arg1) {
				int s0 = arg0.getParticipantEvents().size();
				int s1 = arg1.getParticipantEvents().size();
				
				int r = s1-s0;
				
				// TODO Auto-generated method stub
				return r;
			}
			
		});
		
		return sorted_characters;
	}
	
	private Set<BasicCharacterModel> prune_to_characters(List<Entity> entities, List<BasicCharacterModel> characters) {
		Set<BasicCharacterModel> just_characters = new HashSet<>();
		for(Entity e : entities) {
			for(BasicCharacterModel c : characters) {
				if(e.isEqual(c.getEntity())) {
					just_characters.add(c);
					break;
				}
			}
		}
		return just_characters;
	}
	
	public static void main(String[] args) throws Exception {		
		String[] plot = new String[] {
			"John is a person.",
			"Mary is a person.",
			"Sally is a person.",
			"The ball is an object.",
			"John controls the ball.",
			"John gives the ball to Mary.",
			"Mary controls the ball.",
			"Mary loves Sally.",
			"Mary hugs Sally.",
			"Mary kicks the ball to Sally.",
			"Sally controls the ball.",
			"Sally punches John.",
			"Sally gives the ball to John.",
			"John controls the ball"
		};
		
		plot = new String[] {
				"Alpha is a person.",
				"Beta is a person.",
				"Omega is a person.",
				"Delta is a person.",
				"Delta hates Omega",
				"Alpha is Omega's wife.",
				"Beta is Delta's wife.",
				"Omega hugs Alpha.",
				"Omega is happy.",
				"Alpha is happy because Omega is happy",
				"Delta hugs Beta."
		};
		
		BasicTranslator trans = BasicTranslator.getTranslator();
		Generator gen = Generator.getGenerator();
		gen.setStoryMode();
		gen.flush();
		
		List<Entity> plotElts = new ArrayList<Entity>();
		for(String sentence : plot) {
			Entity elt = trans.translate(sentence).getElement(0);
			plotElts.add(elt);
		}
		
		CharacterViewer.disableCharacterProcessor.setSelected(false);
		CharacterProcessor cp = new CharacterProcessor();
		for(Entity elt : plotElts) {
			cp.processPlotElement(new BetterSignal(elt));
		}
		cp.processCompleteStory(null);
		
		Map<Entity, BasicCharacterModel> cms = cp.getCharacterLibrary();
		List<BasicCharacterModel> characters = new ArrayList<>(cms.values());
		
		PlotWeaver weaver = new PlotWeaver(characters);
		List<Entity> woven_plot = weaver.weavePlots();
		
		Mark.say("Woven plot:");
		for(Entity plot_elt : woven_plot) {
			Mark.say(plot_elt.toEnglish());
		}
	}
}
