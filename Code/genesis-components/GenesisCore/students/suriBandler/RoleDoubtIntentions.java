package suriBandler;

import java.util.*;

import frames.entities.Entity;
import subsystems.rashi.IntentionsProcessor;
import utils.Mark;

public class RoleDoubtIntentions implements IntentionsProcessor{
	boolean debug;
	public static final String CONCEPT_NAME = "Role";
	Map<Entity, Integer> conclusions;
	
	public List<Entity> roleDoubtList; 
	
	
	public RoleDoubtIntentions(boolean debugFlag) {
		
		debug = debugFlag;
		conclusions = new HashMap<Entity, Integer>();
		roleDoubtList = new ArrayList<Entity>();
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
		
		
		
		
		
	}
	

}
