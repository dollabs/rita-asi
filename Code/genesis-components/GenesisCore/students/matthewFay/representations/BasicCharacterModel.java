package matthewFay.representations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import frames.entities.Entity;
import utils.Mark;
import utils.PairOfEntities;
import matthewFay.CharacterModeling.CharacterProcessor;
import matthewFay.CharacterModeling.representations.Trait;
import matthewFay.StoryAlignment.Aligner;
import matthewFay.StoryAlignment.Alignment;
import matthewFay.StoryAlignment.SequenceAlignment;
import matthewFay.StoryAlignment.SortableAlignmentList;
import matthewFay.Utilities.DefaultHashMap;
import matthewFay.Utilities.EntityHelper;
import matthewFay.Utilities.Generalizer;
import matthewFay.representations.CascadingCharacterModel.ObservationMode;

/**
 * Initial attempt at designing a Character Class
 * 
 * Ideally this will represent a character, what the character knows,
 * what the character's actions in a story have been, and predictive
 * methods for predicting future behavior of a character
 * 
 * @author Matthew
 *
 */
public class BasicCharacterModel {
	public BasicCharacterModel(Entity self) {
		init(self);
	}
	
	private void init(Entity self) {
//		Mark.say("Self:"+self);
		this.self = self;
		this.mode = ObservationMode.ALL;
		participantEvents = new ArrayList<Entity>();
		observedEvents = new ArrayList<Entity>();
	}
	
	private static boolean characterFromPerson = true; 
	private static boolean characterFromCountry = true;
	////
	//Used to detect characters to model
	public static boolean isCharacterMarker(Entity event) {
		if(event.relationP("classification")) {
			if(event.getObject().getName().toLowerCase().startsWith("i-"))
				return false;
			if(event.getSubject().entityP("character"))
				return true;
			if(characterFromPerson && event.getSubject().getName().contains("person"))
				return true;
			if(characterFromCountry && event.getSubject().getName().contains("country"))
				return true;
		}
		return false;
	}
	
	public static boolean isGenericMarker(Entity event) {
		if(event.relationP("property")) {
			if(event.getObject().entityP("generic"))
				return true;
		}
		return false;
	}
	
	public static Entity extractCharacterEntity(Entity event) {
		if(!isCharacterMarker(event))
			return null;
		return event.getObject();
	}
	
	public static Entity extractGenericEntity(Entity event) {
		if(!isGenericMarker(event))
			return null;
		return event.getSubject();
	}
	////
	
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
	
	private List<Trait> positive_traits = new ArrayList<>();
	private List<Trait> negative_traits = new ArrayList<>();
	public void addTrait(Trait trait, boolean positive_example) {
		if(positive_example)
			positive_traits.add(trait);
		else
			negative_traits.add(trait);
	}
	public List<Trait> getTraits(boolean positive_example) {
		if(positive_example)
			return new ArrayList<Trait>(positive_traits);
		return new ArrayList<Trait>(negative_traits);
	}
	
	public BasicCharacterModel extractReferentCharacterModel(Entity event) {
		Entity referentEntity = event.getSubject().getSubject();
		return CharacterProcessor.findBestCharacterModel(referentEntity); 
	}
	
	public BasicCharacterModel extractReferenceCharacterModel(Entity event) {
		Entity referenceEntity = event.getObject();
		return CharacterProcessor.findBestCharacterModel(referenceEntity);
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
		//Get predictions from similar characters
		Aligner aligner = new Aligner();
		for(BasicCharacterModel characterModel : getSimilarCharacters()) {
			// TODO: Add binding of this character to similar character...
			List<PairOfEntities> bindings = new ArrayList<>();
			bindings.add(new PairOfEntities(this.getEntity(),characterModel.getEntity()));
			SortableAlignmentList alignments = aligner.align(this.getParticipantEvents(), characterModel.getParticipantEvents(),bindings);
			if(alignments.size() > 0) {
				SequenceAlignment alignment = (SequenceAlignment)alignments.get(0);
				//Find first new event
				int eventCount = 0;
				int i = 0;
				while(eventCount < this.getParticipantEvents().size()) {
					if(alignment.get(i).a != null)
						eventCount++;
					i++;
					if(i != eventCount) {
						//Temporary debugging code...
						//Mark.err(alignment);
						Mark.say("Skipped event....");
						alignments = aligner.align(this.getParticipantEvents(), characterModel.getParticipantEvents(),bindings);
					}
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
					Entity predictedEvent = EntityHelper.findAndReplace(predictionBase, alignment.bindings, true, true);
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
	
	private Entity self;
	public Entity getEntity() {
		return self;
	}
	
	public enum ObservationMode { ALL, ONLY_PARTICIPATING };
	public ObservationMode mode;
	
	//Events this character participated in
	private List<Entity> participantEvents;
	public List<Entity> getParticipantEvents() {
		return participantEvents;
	}
	
	//Events this character has observed
	private List<Entity> observedEvents;
	public List<Entity> getObservedEvents() {
		return observedEvents;
	}
	
	//The story from observed Events that have had all other characters abstracted out in a consistent manner
	private static Map<Entity, BasicCharacterModel> generic_entity_to_character = new HashMap<>();
	private List<Entity> generalizedCharacterStory = new ArrayList<Entity>();
	private List<Entity> generic_entities = new ArrayList<Entity>();
	public List<Entity> getGeneralizedCharacterStory() {
		if(generalizedCharacterStory.size() != participantEvents.size())
			generateGeneralizedCharacterStory();
		return generalizedCharacterStory;
	}
	
	//Static, global across characters
	public static BasicCharacterModel getOriginatingCharacter(Entity generic_entity) {
		if(generic_entity_to_character.containsKey(generic_entity))
			return generic_entity_to_character.get(generic_entity);
		return null;
	}
	
	//Local to the character
	private Map<Entity, Entity> generic_replacements = new HashMap<>();
	public Entity getReplacedEntity(Entity generic) {
		return generic_replacements.get(generic);
	}
	
	public List<Entity> getGenericEntities() {
		getGeneralizedCharacterStory();
		
		return generic_entities;
	}
	
	//Replace Plot
	public void replaceParticipantEvents(List<Entity> newPlot, List<PairOfEntities> bindings) {
		//First just replace the participant plots
		participantEvents.clear();
		participantEvents.addAll(newPlot);
		
		//Then update the generic bindings
		for(PairOfEntities binding : bindings) {
			Entity generic = binding.getPattern(); 
			if(generic_entities.contains(generic)) {
				Entity newCharacter = binding.getDatum();
				Mark.say("Found: "+binding);
				generic_replacements.put(newCharacter, generic);
			}
		}
	}
	
	private void generateGeneralizedCharacterStory() {
//		for(Entity generic_entity : generic_entities) {
//			generic_entity_to_character.remove(generic_entity);
//		}
//		generic_entities.clear();
		generalizedCharacterStory.clear();
//		generic_replacements.clear();
		Set<Entity> all_entities_from_story = new HashSet<Entity>();
		for(Entity event : participantEvents) {
			all_entities_from_story.addAll(EntityHelper.getAllEntities(event));
		}
		List<PairOfEntities> entitiesToReplace = new ArrayList<>();
		for(Entity entity : all_entities_from_story) {
			if(entity != this.getEntity() && !generic_replacements.values().contains(entity)) {
				if( CharacterProcessor.getCharacterLibrary().keySet().contains(entity) || CharacterProcessor.getGenericsLibrary().contains(entity)) {
					Entity generic_entity = EntityHelper.getGenericEntity();
					entitiesToReplace.add(new PairOfEntities(entity, generic_entity));
					generic_entities.add(generic_entity);
					generic_entity_to_character.put(generic_entity, this);
					generic_replacements.put(generic_entity, entity);
				}
			}
		}
		for(Entity event : participantEvents) {
			Entity new_event = event.deepClone(false);
			new_event = EntityHelper.findAndReplace(new_event, entitiesToReplace, true);
			generalizedCharacterStory.add(new_event);
		}
	}
	
	
	
	//List of Characters Similiar to this one
	private List<BasicCharacterModel> similarCharacters = new ArrayList<BasicCharacterModel>();
	public List<BasicCharacterModel> getSimilarCharacters() {
		return similarCharacters;
	}
	
	//Count of events that have had all entities except this character abstracted away
	protected DefaultHashMap<String, Integer> generalized_event_counts = new DefaultHashMap<>(0);
	public DefaultHashMap<String, Integer> getGeneralizedEventCounts() {
		return generalized_event_counts;
	}
	
	//Count of events that have had all other characters abstracted away
	protected DefaultHashMap<String, Integer> semi_generalized_event_counts = new DefaultHashMap<>(0);
	public DefaultHashMap<String, Integer> getSemiGeneralizedEventCounts() {
		return semi_generalized_event_counts;
	}
	
	/**
	 * Adds the given event to this character's observed events,
	 * if the character is in the event, adds the character
	 * to this character's participant events
	 * @param event
	 */
	public void observeEvent(Entity event) {
		if(event==null)
			return;
		
		if(isSimulateCharactersMarker(event)) {
			Mark.say(event.getAllTypes());
			Mark.say(getSimpleName()+ ": Do simulation!");
			Entity simEvent = null;
			do {
				simEvent = simulateNextEvent();
				Mark.say("Next Event: "+simEvent);
				observeEvent(simEvent);
			} while(simEvent != null);
			
			if(this.similarCharacters.size()==1) {
				Mark.say("Simulation Complete: ");
				Mark.say(this.getSimpleName()+" has "+this.participantEvents.size()+" events.");
				Mark.say(similarCharacters.get(0).getSimpleName()+" has "+similarCharacters.get(0).participantEvents.size()+" events.");
			}
			
			return;
		}
		
		//Add event to self character models
		if(EntityHelper.containsEntity(event, self)) {
			//Only do similarity if participant
			if(isSimilarCharacterMarker(event)) {
				BasicCharacterModel referentCharacterModel = extractReferentCharacterModel(event);
				BasicCharacterModel referenceCharacterModel = extractReferenceCharacterModel(event);
				if(referentCharacterModel != null && referenceCharacterModel != null) {
					referentCharacterModel.getSimilarCharacters().add(referenceCharacterModel);
					Mark.say(getEntity().getType(), ": ", event.toEnglish());
				}
			} else {
			
				getObservedEvents().add(event);
				getParticipantEvents().add(event);
				
				//Add to generalized event counts
				String generalized_event = Generalizer.generalize(event, self).toString();
				generalized_event_counts.put(generalized_event, generalized_event_counts.get(generalized_event)+1);
				
				//Add to generalized event counts
				String semi_generalized_event = Generalizer.generalize(event, self, CharacterProcessor.getCharacterLibrary().keySet()).toString();
				semi_generalized_event_counts.put(generalized_event, generalized_event_counts.get(generalized_event)+1);
			}
		} else if(mode == ObservationMode.ALL) {
			getObservedEvents().add(event);
		}
	}
	
	/**
	 * Just adds an event to this character's participated events
	 * @param event
	 */
	public void addEvent(Entity event) {
		if(EntityHelper.containsEntity(event, self)) {
			participantEvents.add(event);
		}
	}
	
	public String getSimpleName() {
		return getEntity().getType();
	}
	
	@Override
	public String toString() {
		return self.toString().replace("(ent", "(char");
	}
	
	public String toLongString() {
		String s = "{ \n Character: "+self.getName()+")\n";
		s += "Observations: \n";
		for(Entity event : observedEvents) {
			s += event+", \n";
		}
		s += "\nKnown Characters of "+self.getName()+")\n";
		s += "\n}\n";
		return s;
	}
}
