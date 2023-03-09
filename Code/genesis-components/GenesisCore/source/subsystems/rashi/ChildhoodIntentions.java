package subsystems.rashi;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import frames.entities.Entity;
import utils.Mark;

public class ChildhoodIntentions implements IntentionsProcessor{
	boolean debug;
	public static final String CONCEPT_NAME = "Childhood";
	SympathyIntentions sympathyIntentions;
	Map<Entity, Integer> conclusions;
	
	public boolean mostSympathyIsChild; 
	
	
	public ChildhoodIntentions(SympathyIntentions sympathyInput, boolean debugFlag) {
		
		debug = debugFlag;
		sympathyIntentions = sympathyInput;
		conclusions = new HashMap<Entity, Integer>();
		
		mostSympathyIsChild = false;
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
		
		Entity predicate = concept.getElement(0);
		Entity predicateObject = predicate.getObject().getElement(0).getSubject();
		if(debug) Mark.say("childhood object found as:", predicateObject);
		
		int currentCount = conclusions.getOrDefault(predicateObject, 0);
		conclusions.put(predicateObject, currentCount + 1);
		
	}
	
	@Override
	public void getConclusions() {
		// TODO: handle interactions of blame / mitigate. assumes blame object already built.
		
		if(conclusions.keySet().size() < 1) return;
		//Entity mostRetributed = conclusions.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
		
		if(conclusions.keySet().contains(sympathyIntentions.getMostVictimizedEntity())){
			mostSympathyIsChild = true;
		}
		
		
		
		Entity mostVictimized = sympathyIntentions.getMostVictimizedEntity();
		
		
		if(mostSympathyIsChild) {
			Mark.say("the most sympathized is also a child");
		}
		
		
		
	}
	

}
