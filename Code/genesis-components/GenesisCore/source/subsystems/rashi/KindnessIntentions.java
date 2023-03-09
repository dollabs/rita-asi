package subsystems.rashi;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import frames.entities.Entity;
import utils.Mark;

public class KindnessIntentions implements IntentionsProcessor{
	
	
	boolean debug;
	public static final String CONCEPT_NAME = "Kindness";
	BlameIntentions blameIntentions;
	Map<Entity, Integer> conclusions;
	
	
	public boolean blamedIsMostKind; 
	public boolean blamedIsKindAtAll;
	public boolean mentionsKindnessOthersThanBlamed;
	
	
	
	public KindnessIntentions(BlameIntentions blameInput, boolean debugFlag) {
		
		debug = debugFlag;
		blameIntentions = blameInput;
		conclusions = new HashMap<Entity, Integer>();
		
		
		blamedIsMostKind = false; 
		blamedIsKindAtAll = false;
		mentionsKindnessOthersThanBlamed = false;
		
		
	}
	
	@Override
	public boolean isConceptType(Entity concept) {
		// assumes passing in something with bundle....that its a concept...has concept thread.
		//if(debug) Mark.say("checking Kindness concept", concept.getBundle().toString());
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
		
		Entity predicate = concept.getElement(0);
		Entity kindnessDoer = RashiHelpers.getSeqSubjectAndObject(predicate).get("object");
		
		if(debug) Mark.say("benefactor found as:", kindnessDoer);
		
		
		int currentCount = conclusions.getOrDefault(kindnessDoer, 0);
		conclusions.put(kindnessDoer, currentCount + 1);
		
	}
	
	@Override
	public void getConclusions() {
		// TODO: handle interactions of blame / mitigate. assumes blame object already built.
		
		if(conclusions.keySet().size() < 1) return;
		Entity mostGooddoer = conclusions.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
		
		
	
		
		
		Entity mostBlamed = blameIntentions.getMostBlamedEntity();
		if(mostBlamed.equals(mostGooddoer)) {
			Mark.say("the most blamed is the kindest");
			blamedIsMostKind = true; 
		}
		
		if(!conclusions.containsKey(mostBlamed) && conclusions.keySet().size() > 0) {
			Mark.say("the author mentions others kindness but not the most blamed");
			mentionsKindnessOthersThanBlamed = true;
		}
		
		
		if(conclusions.containsKey(mostBlamed)) {
			
			Mark.say("most blamed does have some kindess");
			blamedIsKindAtAll = true;
		}
		
		
	}
	

}
