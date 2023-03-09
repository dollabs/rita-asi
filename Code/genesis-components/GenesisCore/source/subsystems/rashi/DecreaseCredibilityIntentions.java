package subsystems.rashi;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import frames.entities.Entity;
import frames.entities.Relation;
import translator.Translator;
import utils.Mark;

public class DecreaseCredibilityIntentions implements IntentionsProcessor{
	
	boolean debug; 
	public static final String CONCEPT_NAME = "Decrease credibility";
	Map<Entity, Integer> conclusions;
	Map<Entity, List<Entity>> conclusionToCompositionSource;
	Entity mostDecreasedCredEntity;
	boolean mostDecreasedCredOnlyDecreasedCred;
	
	boolean decreaseCredFound;
	
	public DecreaseCredibilityIntentions(boolean debugFlag) {

		debug = debugFlag;
		conclusions = new HashMap<Entity, Integer>();
		conclusionToCompositionSource = new HashMap<Entity, List<Entity>>();
		mostDecreasedCredOnlyDecreasedCred = false;
		
		decreaseCredFound = false;
		
	}
	
	@Override
	public boolean isConceptType(Entity concept) {
		
		// assumes passing in something with bundle....that its a concept...has concept thread.
		
		Vector<String> conceptThread = concept.getBundle().getThreadContaining("concept");
	
		return conceptThread.contains(CONCEPT_NAME);
	}
	
	@Override
	public void processConcept(Entity concept) {
	
		if (!isConceptType(concept)) return;
		decreaseCredFound = true;
		if(debug) Mark.say("Processing concept for", CONCEPT_NAME, concept);
		
		// assumptions: 
		// 	the conclusion is compatible with seq subject and object (also, implicitly assuming conclusion is a relation)
		
		Entity conclusion = concept.getElement(1);
		Entity predicate = concept.getElement(0);
		RashiHelpers.putInDictKeytoList(conclusionToCompositionSource, conclusion, predicate);
		
		
		
		Map<String, Entity> conclusionSubjectObject = RashiHelpers.getSeqSubjectAndObject(conclusion);
		Entity conclusionsObject = conclusionSubjectObject.get("object");
		
		Entity untrustworthyAgent = predicate.getObject().getElement(0).getSubject().getSubject();
		int currentCount = conclusions.getOrDefault(untrustworthyAgent, 0);
		conclusions.put(untrustworthyAgent, currentCount + 1);
		
		
	}
	@Override
	public void getConclusions() {
		
		int count = 0;
		Entity somebody = null;
		for(Entity key : conclusions.keySet()) {
			String keyString = key.toEnglish();
			if(!keyString.equals("somebody")) count += conclusions.get(key);
			else somebody = key;
		}
		
		conclusions.put(somebody, conclusions.get(somebody)-count);
		
		List<Entity> toRemove = new ArrayList<Entity>();
		for(Entity key : conclusions.keySet()) {
			if(conclusions.get(key) == 0) {
				toRemove.add(key);
			}
		}
		
		for(Entity i : toRemove) {
			conclusions.remove(i);
		}
		
		Entity mostDecreasedCred = conclusions.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
		
		mostDecreasedCredEntity = mostDecreasedCred;
		if(conclusions.keySet().size() == 1) mostDecreasedCredOnlyDecreasedCred = true;
		
		
		if(debug) Mark.say("Most decreased cred:", mostDecreasedCredEntity);
		if(debug) Mark.say("Conclusions", conclusions);
		
		
	}
	
	public Map<Entity, List<Entity>> getConclusionsToCompositionSource(){
		return conclusionToCompositionSource;
	}
	
	public Entity getMostDecreasedCredEntity() {
		
		return mostDecreasedCredEntity;
	}

}
