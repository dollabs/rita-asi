package matthewFay.representations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import frames.entities.Entity;
import utils.Mark;
import matthewFay.CharacterModeling.CharacterProcessor;
import matthewFay.StoryAlignment.Aligner;
import matthewFay.StoryAlignment.Alignment;
import matthewFay.StoryAlignment.SequenceAlignment;
import matthewFay.StoryAlignment.SortableAlignmentList;
import matthewFay.Utilities.EntityHelper;
import matthewFay.Utilities.Generalizer;
import matthewFay.representations.BasicCharacterModel.ObservationMode;

/**
 * Initial attempt at designing a Character Class
 * 
 * Ideally this will represent a character, what the charcater knows,
 * what the character's actions in a story have been, and predictive
 * methods for predicting future behavior of a character
 * 
 * CascadingCharacterModel extends BasicCharacterModel by adding
 * what the character thinks about all other characters (recursively)
 * potential overlap with StoryProcessor functionality
 * 
 * @author Matthew
 *
 */
public class CascadingCharacterModel extends BasicCharacterModel {
	public CascadingCharacterModel(Entity self) {
		super(self);
		init(0);
	}
	
	private CascadingCharacterModel(Entity self, int depth) {
		super(self);
		init(depth);
	}
	
	private void init(int depth) {
		this.depth = depth;
		this.mode = ObservationMode.ONLY_PARTICIPATING;
		this.characterModels = new HashMap<Entity, CascadingCharacterModel>();
		this.activeCharacterModels = new HashMap<>();
	}
	
	////
	//Used to detect similar characters
	public static boolean isSimilarCharacterMarker(Entity event) {
		if(event.relationP("to")) {
			if(event.getSubject().relationP("property")) {
				if(event.getSubject().getObject().entityP("similar")) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public CascadingCharacterModel extractReferentCharacterModel(Entity event) {
		Entity referentEntity = event.getSubject().getSubject();
		if(characterModels.containsKey(referentEntity))
			return characterModels.get(referentEntity);
		return null;
	}
	
	@Override
	public CascadingCharacterModel extractReferenceCharacterModel(Entity event) {
		CascadingCharacterModel referenceCharacterModel = null;
		String referenceName = event.getObject().getType();
		
		for(CascadingCharacterModel model : characterModels.values()) {
			String modelName = model.getEntity().getType();
			if(modelName.equals(referenceName)) {
				referenceCharacterModel = model;
			}
		}
		return referenceCharacterModel;
	}
	////
	
	////
	//Used to detect simulation command
	public static boolean isSimulateCharactersMarker(Entity event) {
		if(event.relationP("simulate")) {
			if(event.getSubject().entityP("you")) {
				if(event.getObject().sequenceP("roles")) {
					if(event.getObject().getElement(0).functionP("object")) {
						if(event.getObject().getElement(0).getSubject().entityP("characters")) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public Entity simulateNextEvent() {
		Entity nextEvent = null;
		List<Entity> predictedEvents = new ArrayList<Entity>();
		//Get predictions from mental models
		for(CascadingCharacterModel characterModel : characterModels.values()) {
			Entity predictedEvent = characterModel.simulateNextEvent();
			if(predictedEvent != null)
				predictedEvents.add(predictedEvent);
		}
		//Get predictions from similar characters
		Aligner aligner = new Aligner();
		for(BasicCharacterModel characterModel : getSimilarCharacters()) {
			SortableAlignmentList alignments = aligner.align(getObservedEvents(), characterModel.getObservedEvents());
			if(alignments.size() > 0) {
				SequenceAlignment alignment = (SequenceAlignment)alignments.get(0);
				//Find first new event
				int eventCount = 0;
				int i = 0;
				while(eventCount < getObservedEvents().size()) {
					if(alignment.get(i).a != null)
						eventCount++;
					i++;
				}
				Entity predictionBase = null;
				while(i<alignment.size()) {
					if(alignment.get(i).b != null) {
						predictionBase = alignment.get(i).b.deepClone(false);
						break;
					}
					i++;
				}
				if(predictionBase != null) {
					Entity predictedEvent = EntityHelper.findAndReplace(predictionBase, alignment.bindings);
					if(predictedEvent != null)
						predictedEvents.add(predictedEvent);
				}
			}
		}
		//Choose event from predictedEvents
		if(predictedEvents.size()>0)
			nextEvent = predictedEvents.get(0);
		return nextEvent;
	}
	////
	
	public enum ObservationMode { ALL, ONLY_PARTICIPATING };
	public ObservationMode mode;
	
	private Map<Entity, CascadingCharacterModel> activeCharacterModels = new HashMap<Entity,CascadingCharacterModel>();
	public Collection<CascadingCharacterModel> getActiveCharacterModels() {
		return activeCharacterModels.values();
	}
	
	/**
	 * Inception: Layer of mental modelling this character exists on
	 * 0 - Reader/Highest Level Character
	 * 1 - Character model in the head of a level 0 character
	 * 2 - etc.
	 */
	private static int maximum_depth = 1;
	private int depth = 0;
	
	//Every character keeps track internally what they know and think
	//about all the other characters that they have observed
	//including themselves, limited by the depth of 
	private Map<Entity, CascadingCharacterModel> characterModels;
	public CascadingCharacterModel createCharacterModel(Entity entity) {
		if(depth >= maximum_depth)
			return null;
		if(!characterModels.containsKey(entity)) {
			// Attempt to create new character model
			// Stop imagining recursive character models at max depth
			CascadingCharacterModel newCharacterModel = new CascadingCharacterModel(entity, depth+1);
			
//			Mark.say("Creating Cascade Character from : "+entity);
			characterModels.put(entity, newCharacterModel);
			activeCharacterModels.put(entity, newCharacterModel);
			
			//Recursively update all character models with this new character
			//And update this new character with models of all the old characters
			for(CascadingCharacterModel characterModel : activeCharacterModels.values()) {
				characterModel.createCharacterModel(entity);
				newCharacterModel.createCharacterModel(characterModel.getEntity());
			}
		}
		return characterModels.get(entity);
	}
	public Collection<CascadingCharacterModel> getAllCharacterModels() {
		return characterModels.values();
	}
	public CascadingCharacterModel getCharacterModel(Entity entity) {
		if(activeCharacterModels.containsKey(entity))
			return activeCharacterModels.get(entity);
		if(characterModels.containsKey(entity))
			return characterModels.get(entity);
		return null;
	}
	
	/**
	 * Archives the current character models for future reference.
	 * Archived characters do not respond to new events
	 */
	public void endStory() {
		activeCharacterModels.clear();
		getObservedEvents().clear();
		getParticipantEvents().clear();
	}
	
	/**
	 * Adds the given event to this character's observed events,
	 * if the character is in the event, adds the character
	 * to this character's participant events
	 * @param event
	 */
	@Override
	public void observeEvent(Entity event) {
		if(isCharacterMarker(event)) {
			//Add a new character model//
			CascadingCharacterModel newCharacterModel = createCharacterModel(CascadingCharacterModel.extractCharacterEntity(event));
			if(newCharacterModel != null) {
				//Update the new character model with previously observed events//
				for(Entity past_event : this.getObservedEvents()) {
					updateCharacterModel(newCharacterModel, past_event);
				}
			}
		}
		
		if(isSimulateCharactersMarker(event)) {
			Mark.say(getSimpleName()+ ": Do simulation!");
			Entity simEvent = simulateNextEvent();
			Mark.say("Next Event: "+simEvent);
			event = simEvent;
		}
		
		if(event==null)
			return;
		
		//Add event to self character models
		if(EntityHelper.containsEntity(event, getEntity())) {
			if(isSimilarCharacterMarker(event)) {
				CascadingCharacterModel referentCharacterModel = extractReferentCharacterModel(event);
				CascadingCharacterModel referenceCharacterModel = extractReferenceCharacterModel(event);
				if(referentCharacterModel != null && referenceCharacterModel != null) {
					referentCharacterModel.getSimilarCharacters().add(referenceCharacterModel);
					Mark.say(getEntity().getType(), ": ", event.toEnglish());
				}
			}
			
			getObservedEvents().add(event);
			getParticipantEvents().add(event);
			
			//Add to generalized event counts
			String generalized_event = Generalizer.generalize(event, getEntity()).toString();
			generalized_event_counts.put(generalized_event, generalized_event_counts.get(generalized_event)+1);
			
			//Add to generalized event counts
			String semi_generalized_event = Generalizer.generalize(event, getEntity(), CharacterProcessor.getCharacterLibrary().keySet()).toString();
			semi_generalized_event_counts.put(generalized_event, generalized_event_counts.get(generalized_event)+1);
		} else if(mode == ObservationMode.ALL) {
			getObservedEvents().add(event);
		}
		
		for(CascadingCharacterModel character : getActiveCharacterModels()) {
			updateCharacterModel(character, event);
		}
	}
	
	/**
	 * Takes a character model and an event and adds it to the character model
	 * if the constraints imposed by the observationmode allow it
	 * @param characterModel
	 * @param event
	 */
	private void updateCharacterModel(CascadingCharacterModel characterModel, Entity event) {
		//Handle updating character models
		if(mode == ObservationMode.ONLY_PARTICIPATING) {
			List<Entity> present_entities = EntityHelper.getAllEntities(event);
			if(present_entities.contains(characterModel.getEntity()))
				characterModel.observeEvent(event);
		}
		
		if(mode == ObservationMode.ALL) {
			characterModel.observeEvent(event);
		}
	}
	
	@Override
	public String toString() {
		String s = "{ \n Character: "+getEntity().getName()+" (d="+depth+")\n";
		s += "Observations: \n";
		for(Entity event : getObservedEvents()) {
			s += event+", \n";
		}
		s += "\nKnown Characters of "+getEntity().getName()+" (d="+depth+")\n";
		for(CascadingCharacterModel character : characterModels.values()) {
			s += "\n"+character+",\n "; 
		}
		s += "\n}\n";
		return s;
	}
}
