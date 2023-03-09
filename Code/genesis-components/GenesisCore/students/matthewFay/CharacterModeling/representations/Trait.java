package matthewFay.CharacterModeling.representations;

import java.util.ArrayList;
import java.util.List;

import frames.entities.Entity;
import utils.PairOfEntities;
import matthewFay.StoryAlignment.Aligner;
import matthewFay.StoryAlignment.SequenceAlignment;
import matthewFay.Utilities.EntityHelper;
import matthewFay.Utilities.Generalizer;
import matthewFay.Utilities.Pair;
import matthewFay.representations.BasicCharacterModel;
import matthewFay.viewers.TraitViewer;

public class Trait {
	public static boolean isTraitAssignment(Entity element) {
		if(element.relationP("personality_trait")) {
			return true;
		}
		return false;
	}
	
	private List<BasicCharacterModel> positive_examples;
	private List<BasicCharacterModel> negative_examples;
	
	private String name;
	public String getName() {
		return name;
	}
	
	private Entity prime_character_entity;
	private List<Entity> trait_elements;
	private boolean dirty = true;
	public void markDirty() {
		dirty = true;
	}
	
	public Trait(String name) {
		this.name = name;
		
		positive_examples = new ArrayList<>();
		negative_examples = new ArrayList<>();
		trait_elements = new ArrayList<>();
	}
	
	public void addPositiveExample(BasicCharacterModel character) {
		positive_examples.add(character);
		dirty = true;
		if(positive_examples.size() == 1) {
			BasicCharacterModel prime_character = positive_examples.get(0);
			prime_character_entity = prime_character.getEntity();
			trait_elements.addAll(prime_character.getParticipantEvents());
			return;
		}
		
		List<Entity> plot2 = character.getParticipantEvents();
		List<PairOfEntities> bindings = new ArrayList<>();
		bindings.add(new PairOfEntities(prime_character_entity, character.getEntity()));
		
		SequenceAlignment sa = (SequenceAlignment)Aligner.getAligner().align(trait_elements, plot2, bindings).get(0);
		for(Pair<Entity, Entity> pair : sa) {
			if(pair.a != null && pair.b != null) {
				trait_elements.add(pair.a);
			}
		}
	}
	
	public void addNegativeExample(BasicCharacterModel character) {
		negative_examples.add(character);
		dirty = true;
	}
	
	private void infer_trait_elements() {
		if(positive_examples.size() > 0) {
			//Choose a prime character, in this case largest plot
			for(BasicCharacterModel character : positive_examples) {
				if(character.getParticipantEvents().size() > trait_elements.size()) {
					trait_elements.clear();
					BasicCharacterModel prime_character = positive_examples.get(0);
					prime_character_entity = prime_character.getEntity();
					trait_elements.addAll(prime_character.getParticipantEvents());
				}
			}
			//Do the negatives
			for(BasicCharacterModel negative_example : negative_examples) {
				List<Entity> neg_plot = negative_example.getParticipantEvents();
				
				List<PairOfEntities> bindings = new ArrayList<>();
				bindings.add(new PairOfEntities(prime_character_entity, negative_example.getEntity()));
				
				SequenceAlignment sa = (SequenceAlignment)Aligner.getAligner().align(trait_elements, neg_plot, bindings).get(0);
				
				trait_elements.clear();
				for(Pair<Entity, Entity> pair : sa) {
					if(pair.a != null && pair.b == null) {
						trait_elements.add(pair.a);
					}
				}
			}
			//Do the positives
			for(BasicCharacterModel positive_example : positive_examples) {
				List<Entity> pos_plot = positive_example.getParticipantEvents();
				
				List<PairOfEntities> bindings = new ArrayList<>();
				bindings.add(new PairOfEntities(prime_character_entity, positive_example.getEntity()));
				
				SequenceAlignment sa = (SequenceAlignment)Aligner.getAligner().align(trait_elements, pos_plot, bindings).get(0);
				
				trait_elements.clear();
				for(Pair<Entity, Entity> pair : sa) {
					if(pair.a != null && pair.b != null) {
						trait_elements.add(pair.a);
					}
				}
			}

			dirty = false;
		}
	}
	
	public List<Entity> getElements() {
		if(dirty)
			infer_trait_elements();
		if(dirty)
			return new ArrayList<>();
		
		List<Entity> toReturn = new ArrayList<>(trait_elements); 
		
		//Generalize things
		if(TraitViewer.getTraitViewer().generalize_trait_description.isSelected())
			toReturn = EntityHelper.generalizeListOfEntities(trait_elements, prime_character_entity);
		
		return toReturn;
	}
}
