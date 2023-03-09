package subsystems.rashi;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import frames.entities.Entity;
import utils.Mark;

public class RetributionIntentions implements IntentionsProcessor {

	boolean debug;
	public static final String CONCEPT_NAME = "Retribution";
	BlameIntentions blameIntentions;
	Map<Entity, Integer> conclusions;
	
	public boolean blamedIsMostRetributed; 
	public boolean blamedIsRetributedAtAll;
	public boolean retributesOthersThanBlamed;
	
	
	public RetributionIntentions(BlameIntentions blameInput, boolean debugFlag) {
		
		debug = debugFlag;
		blameIntentions = blameInput;
		conclusions = new HashMap<Entity, Integer>();
		
		
		blamedIsMostRetributed = false;
		blamedIsRetributedAtAll = false;
		retributesOthersThanBlamed = false;
	}
	
	@Override
	public boolean isConceptType(Entity concept) {
		// assumes passing in something with bundle....that its a concept...has concept thread.
		//if(debug) Mark.say("checking retribution concept", concept.getBundle().toString());
		Vector<String> conceptThread = concept.getBundle().getThreadContaining("concept");
		return conceptThread.contains(CONCEPT_NAME);
		
	}
	
	@Override
	public void processConcept(Entity concept) {
		
		if (!isConceptType(concept)) return;
		if(debug) Mark.say("Processing concept for", CONCEPT_NAME, concept);
		
		// assumptions: 
		// 	the conclusion is compatible with seq subject and object (also, implicitly assuming conclusion is a relation)
		
		
		Entity conclusion = concept.getElement(1);
		Entity conclusionsObject = conclusion.getObject().getElement(1).getSubject();
		if(debug) Mark.say("retribution object found as:", conclusionsObject);
		
		int currentCount = conclusions.getOrDefault(conclusionsObject, 0);
		conclusions.put(conclusionsObject, currentCount + 1);
		
	}
	
	@Override
	public void getConclusions() {
		// TODO: handle interactions of blame / mitigate. assumes blame object already built.
		
		if(conclusions.keySet().size() < 1) return;
		Entity mostRetributed = conclusions.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
		
		Entity mostBlamed = blameIntentions.getMostBlamedEntity();
		
		
		
		
		
		
		if(mostBlamed.equals(mostRetributed)) {
			Mark.say("the most blamed is the most retributed");
			blamedIsMostRetributed = true;
		}
		
		if(!conclusions.containsKey(mostBlamed) && conclusions.keySet().size() > 0) {
			Mark.say("the author retributes others but not the most blamed");
			retributesOthersThanBlamed = true;
		}
		
		
		if(conclusions.containsKey(mostBlamed)) {
			
			Mark.say("most blamed is retributed at all");
			blamedIsRetributedAtAll = true;
		}
		
		
	}
	
	
	
}
