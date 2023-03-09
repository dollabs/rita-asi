package subsystems.rashi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import frames.entities.Entity;
import frames.entities.Relation;
import translator.Translator;
import utils.Mark;

public class BlameIntentions implements IntentionsProcessor{
	
	boolean debug; 
	public static final String CONCEPT_NAME = "Blame";
	Map<Entity, Integer> conclusions;
	Map<Entity, List<Entity>> conclusionToCompositionSource;
	Entity mostBlamedEntity;
	boolean mostBlamedOnlyBlamed;
	
	boolean blameFound;
	
	public BlameIntentions(boolean debugFlag) {

		debug = debugFlag;
		conclusions = new HashMap<Entity, Integer>();
		conclusionToCompositionSource = new HashMap<Entity, List<Entity>>();
		mostBlamedOnlyBlamed = false;
		
		blameFound = false;
		
	}
	
	@Override
	public boolean isConceptType(Entity concept) {
		
		// assumes passing in something with bundle....that its a concept...has concept thread.
		
		//if(debug) Mark.say("checking blame concept", concept.getBundle().toString());
		
		Vector<String> conceptThread = concept.getBundle().getThreadContaining("concept");
		//Mark.say(conceptThread.get(conceptThread.size()-1));
		//Mark.say("checking", conceptThread.contains("Blame"));
		//return (concept.getBundle().toString().contains("Blame"));
		return conceptThread.contains(CONCEPT_NAME);
	}
	
	@Override
	public void processConcept(Entity concept) {
	
		if (!isConceptType(concept)) return;
		blameFound = true;
		if(debug) Mark.say("Processing concept for", CONCEPT_NAME, concept);
		
		// assumptions: 
		// 	the conclusion is compatible with seq subject and object (also, implicitly assuming conclusion is a relation)
		
		Entity conclusion = concept.getElement(1);
		Entity predicate = concept.getElement(0);
		RashiHelpers.putInDictKeytoList(conclusionToCompositionSource, conclusion, predicate);
		
		
		
		Map<String, Entity> conclusionSubjectObject = RashiHelpers.getSeqSubjectAndObject(conclusion);
		Entity conclusionsObject = conclusionSubjectObject.get("object");
		int currentCount = conclusions.getOrDefault(conclusionsObject, 0);
		conclusions.put(conclusionsObject, currentCount + 1);
		
		
	}
	@Override
	public void getConclusions() {
		Entity mostBlamed = conclusions.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
		
		mostBlamedEntity = mostBlamed;
		if(conclusions.keySet().size() == 1) mostBlamedOnlyBlamed = true;
		
		
		if(debug) Mark.say("Most blamed:", mostBlamed);
		if(debug) Mark.say("Conclusions", conclusions);
		
		
	}
	
	public Map<Entity, List<Entity>> getConclusionsToCompositionSource(){
		return conclusionToCompositionSource;
	}
	
	public Entity getMostBlamedEntity() {
		
		return mostBlamedEntity;
	}

}
