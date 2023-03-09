package subsystems.rashi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import frames.entities.Entity;
import utils.Mark;

public class SympathyIntentions implements IntentionsProcessor {

	
	boolean debug; 
	public static final String CONCEPT_NAME = "Victimhood";
	Map<Entity, Integer> conclusions;
	Map<Entity, List<Entity>> conclusionToCompositionSource;
	Entity mostVictimizedEntity;
	boolean mostVictimizedOnlyVictim;
	public boolean victimizesOtherThanMostSympathy;
	
	boolean sympathyFound;
	
	public SympathyIntentions(boolean debugFlag) {

		debug = debugFlag;
		conclusions = new HashMap<Entity, Integer>();
		conclusionToCompositionSource = new HashMap<Entity, List<Entity>>();
		mostVictimizedOnlyVictim = false;
		victimizesOtherThanMostSympathy = false;
		
		sympathyFound = false;
		
	}
	
	@Override
	public boolean isConceptType(Entity concept) {
		
		// assumes passing in something with bundle....that its a concept...has concept thread.
		
		//if(debug) Mark.say("checking sympathy concept", concept.getBundle().toString());
		
		Vector<String> conceptThread = concept.getBundle().getThreadContaining("concept");

		return conceptThread.contains(CONCEPT_NAME);
	}
	
	@Override
	public void processConcept(Entity concept) {
	
		if (!isConceptType(concept)) return;
		sympathyFound = true;
		if(debug) Mark.say("Processing concept for", CONCEPT_NAME, concept);
		
		// assumptions: 
		// 	the conclusion is compatible with seq subject and object (also, implicitly assuming conclusion is a relation)
		
		Entity conclusion = concept.getElement(1);
		Entity predicate = concept.getElement(0);
		RashiHelpers.putInDictKeytoList(conclusionToCompositionSource, conclusion, predicate);
		
		//Map<String, Entity> predicateSubObject = RashiHelpers.getSeqSubjectAndObject(predicate);
		
		
		Map<String, Entity> conclusionSubjectObject = RashiHelpers.getSeqSubjectAndObject(conclusion);
		Entity conclusionsObject = conclusion.getObject().getElement(1).getSubject();//conclusionSubjectObject.get("object"); 
		int currentCount = conclusions.getOrDefault(conclusionsObject, 0);
		conclusions.put(conclusionsObject, currentCount + 1);
		
		
	}
	@Override
	public void getConclusions() {
		Entity mostVictimized = conclusions.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
		
		mostVictimizedEntity = mostVictimized;
		if(conclusions.keySet().size() == 1) mostVictimizedOnlyVictim = true;
		if(conclusions.keySet().size() > 1) victimizesOtherThanMostSympathy = true;
		
		if(debug) Mark.say("Most sympathized:", mostVictimizedEntity);
		if(debug) Mark.say("Conclusions", conclusions);
		
		
	}
	
	public Map<Entity, List<Entity>> getConclusionsToCompositionSource(){
		return conclusionToCompositionSource;
	}
	
	public Entity getMostVictimizedEntity() {
		
		return mostVictimizedEntity;
	}
}
