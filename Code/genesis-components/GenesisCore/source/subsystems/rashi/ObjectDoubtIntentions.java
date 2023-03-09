package subsystems.rashi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;

import frames.entities.Entity;
import utils.Mark;

public class ObjectDoubtIntentions implements IntentionsProcessor{
	boolean debug;
	public static final String CONCEPT_NAME = "Object of doubt";
	Map<Entity, Integer> conclusions;
	
	public Set<Entity> objectDoubtList;
	Map<Entity, List<Entity>> sourcesMap;
	Map<Entity, Entity> predicateObjectToPred;
	DecreaseCredibilityIntentions decreaseCredibilityIntentions;
	
	
	public ObjectDoubtIntentions(DecreaseCredibilityIntentions decreaseCredibilityIntentionsInput, Map<Entity, List<Entity>> sourcesMapInput, boolean debugFlag) {
		
		debug = debugFlag;
		conclusions = new HashMap<Entity, Integer>();
		objectDoubtList = new HashSet<Entity>();
		sourcesMap = sourcesMapInput;
		predicateObjectToPred = new HashMap<Entity, Entity>();
		decreaseCredibilityIntentions = decreaseCredibilityIntentionsInput;
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
		Entity predicateObject = predicate.getObject().getElement(0).getSubject().getObject().getElement(0).getSubject();
		
		predicateObjectToPred.put(predicateObject, predicate);
		
		if(debug) Mark.say("childhood object found as:", predicateObject);
		
		int currentCount = conclusions.getOrDefault(predicateObject, 0);
		conclusions.put(predicateObject, currentCount + 1);
		
	}
	
	@Override
	public void getConclusions() {
		for(Entity i: conclusions.keySet()) {
			Entity predicate = predicateObjectToPred.get(i);
			//List<Entity> originalSentence;
			for(Entity opt : sourcesMap.keySet()) {
				if(opt.getElements()!=null) {
					if(opt.getElements().size() > 0) {
						Entity trying = opt.getElement(0);
						String tryString = trying.toEnglish().toString();
						String predicateString = predicate.toEnglish().toString();
						if(trying.equals(predicate) || tryString.equals(predicateString)) {
							Entity obj = trying.getObject();
							List<Entity> sources = sourcesMap.get(opt);
							for(Entity source : sources) {
								if(source.getSubject().toEnglish().equals(decreaseCredibilityIntentions.getMostDecreasedCredEntity().toEnglish())) {
									objectDoubtList.add(i);
								}
							}
						}
					}
				}
			}
			objectDoubtList.add(i);
		}
		
		
		
		
		
	}
	

}
