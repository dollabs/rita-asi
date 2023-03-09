package subsystems.rashi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import frames.entities.Entity;
import matchers.StandardMatcher;
import utils.Mark;

public class PassivityIntentions implements IntentionsProcessor {

	boolean debug;
	public static final String CONCEPT_NAME = "Passivity";
	public BlameIntentions blameIntentions;
	Map<Entity, List<Entity>> sourcesMap;
	List<Entity> conclusions;
	ArrayList<Entity> passiveAgents;
	ArrayList<Entity> passiveObjects;
	
	
	public PassivityIntentions(boolean debugFlag, BlameIntentions blameInput, Map<Entity, List<Entity>> authorStoryLineToSources) {
		debug = debugFlag;
		blameIntentions = blameInput;
		sourcesMap = authorStoryLineToSources;
		conclusions = new ArrayList<Entity>();
		passiveAgents = new ArrayList<Entity>();
		passiveObjects = new ArrayList<Entity>();
	}
	
	
	@Override
	public boolean isConceptType(Entity concept) {
		// TODO Auto-generated method stub
		if(debug) Mark.say("checking passivity concept", concept.getBundle().toString());
		
		Vector<String> conceptThread = concept.getBundle().getThreadContaining("concept");
		return conceptThread.contains(CONCEPT_NAME);
		
	}


	@Override
	public void processConcept(Entity concept) {
		if(!isConceptType(concept)) return;
		
		
		//Entity conclusion = concept.getElement(1);
		Entity predicate = concept.getElement(0);
		
		conclusions.add(predicate);
		
		Mark.say(concept);
		
	}
	
	public boolean usedPassiveForEntity(Entity entity) {
		return (passiveAgents.contains(entity));
		//for(Entity predicate : conclusions) {
			//if(predicate.getSubject().toString().equals(entity.toString())) return true;
			
		//}
		
		//return false;
	}
	
	public boolean usedPassiveForObject(Entity entity) {
		return (passiveObjects.contains(entity));
		
	}
	
	@Override
	public void getConclusions() {
		
		for(Entity predicate : conclusions) {
			//Entity predicate = concept.getElement(0);
			// all sources for "author uses passivity for xx"
			
			Mark.say("Predicate", predicate.toEnglish());
			Entity passiveAgent = predicate.getObject().getElement(1).getSubject();
			passiveAgents.add(passiveAgent);
			
			Entity passiveObject = predicate.getObject().getElement(0).getSubject();
			passiveObjects.add(passiveObject);
			
			
			/*
			List<Entity> sources = sourcesMap.getOrDefault(predicate.toEnglish(), null);
			Map<Entity, List<Entity>> blameSources = blameIntentions.getConclusionsToCompositionSource();
			
			if(sources != null) {
			for(Entity source : sources) {
				
				//TODO: if associated with BLAME, mitigate lightly
				
				//go through blame results ... 
				// TODO: do passive first then look for this in blame?
				
				for (Map.Entry<Entity,List<Entity>> entry : blameSources.entrySet()) {
					  Entity key = entry.getKey();
					  List<Entity> value = entry.getValue();
					  
					  for(Entity blameSource : value) {
						  Mark.say("Blame source", blameSource.toEnglish());
						  
						  List<Entity> excess = StandardMatcher.getBasicMatcher().matchAndReportExcess(source, blameSource);
						  Mark.say("Result of matchRoleFrames with arguments rfp and rfd", excess);
						  StandardMatcher.getBasicMatcher().match(source, blameSource);
						  
						  List<Entity> originalSources = sourcesMap.getOrDefault(blameSource.toEnglish(), null);
						  
						  if(originalSources != null) {
						  for(Entity originalSource : originalSources) {
							  
								
							  if(source.toEnglish().equals(originalSource.toEnglish())) {
								  Mark.say("author used passive voice here:", source.toEnglish(), "and mitigated blame here:", originalSource, "and", blameSource.toEnglish());
							  }
						  	}
						  }
						  //if(sourcesMap.containsKey(key));
					  }
					  
					  // do stuff
					}
			}
				
				for(Entity authorActionBlame : blameSources.) {
					if(blameSources.get(blameConclusion).contains(source)) {
						Mark.say("blame in", blameConclusion, "mitigated because used passive in", source);
					}
				}
				*/
				
			}
			
			
		}
		
		
	}
	
//}
