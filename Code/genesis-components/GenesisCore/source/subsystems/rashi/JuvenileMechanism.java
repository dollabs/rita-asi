package subsystems.rashi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import frames.entities.Entity;
import matchers.Substitutor;
import start.Start;
import utils.Html;
import translator.Translator;
import utils.Mark;

public class JuvenileMechanism {

	//private final static VALID_PROPERTIES = ArrayList<String>();
	/**
	 * Get common word-net word for all juvenile entities.
	 * @return
	 */
	public static String getCommonWord() {
		return "juvenile";
	}
	
	public static ArrayList<String> getValidProperties(){
		
		return new ArrayList<String>(Arrays.asList("young", "boy", "boys", "girl", "girls"));
		
	}
	
	
	/** 
	 * Used to generate a story about the author's choices/decisions.
	 * Get a sentence (entity) stating that the author refers to the provided entity as a youth.
	 * @param childEntity
	 * @return
	 */
	public static Entity authorRefersToJuvenile(Entity childEntity){
		Translator translator = Translator.getTranslator();
		Entity general = translator.translate("author refers to Bob as a youth");
		//Mark.say(general.get(0).get(1).get(0))
		Entity bob = general.get(0).getObject().get(0).getSubject();
		Entity withBobSubbed = Substitutor.substitute(childEntity, bob, general);
		return withBobSubbed;
		
	}
	
	/**
	 * Get a sentence (entity) stating that the author refers to the provided entity as a victim.
	 * @param childEntity
	 * @return
	 */
	public static Entity authorRefersToJuvenileVictim(Entity childEntity){
		Translator translator = Translator.getTranslator();
		Entity general = translator.translate("author refers to Bob as a victim");
		//Mark.say(general.get(0).get(1).get(0))
		Entity bob = general.get(0).getObject().get(0).getSubject();
		Entity withBobSubbed = Substitutor.substitute(childEntity, bob, general);
		return withBobSubbed;
		
	}
	
	/**
	 * Get an adult entity to replace a youth entity in hypothetical reasoning.
	 * Ensures that the same adult is consistently returned for a given youth.
	 * @param youthToAdult
	 * @param youth
	 * @return
	 */
	public static Entity getAdult(Map<Entity,Entity> youthToAdult, Entity youth){ 
		if(!youthToAdult.containsKey(youth)){
			Translator translator = Translator.getTranslator();
			translator.translateToEntity("Adult is an adult.");
			Entity adult = Start.makeThing("adult");
			Entity newAdult = (Entity) adult.clone();
			
			youthToAdult.put(youth, newAdult);
		}
		
		return youthToAdult.get(youth);
		
		
	}
	
	/**
	 * Get author result for subject when did an action to a child.
	 * @param invokation
	 * @param subject
	 * @param object
	 * @param classification
	 * @return
	 */
	public static Entity getEntityResultYouthObject(String invokation, String subject, String object, String classification){
		String action = ""; //TODO: generalize this whole function
		if(classification.equals("positive")){
			action = "helped";
		}
		if(classification.equals("negative")){
			action = "hurt";
		}
		String sentence = "Author "+ invokation + " for the " + subject +" because "+ subject+ " " +action+ " a " +object +".";
		
		Translator translator = Translator.getTranslator();
		Entity entity = translator.translate(sentence);
		//Mark.say(subject, entity);
		return entity;
		
	}
	
	/**
	 * Process the results of finding which roles are associated with YOUTH
	 * Takes in dictionary with key "juvenile" that contains which
	 *  subjects/objects had juvenile in thread matcher
	 * Takes in Entity --> List of sequences that it's involved with.
	**/ 
	public static Map<Entity, List<Entity>> processJuvenile(Map<String, Map<String, List<Entity>>> juv, 
			Map<Entity, List<Entity>> entityToSequence, ArrayList<Entity> authorStory, 
			Map<Entity, List<Entity>> authorStoryLineToSources, Map<Entity, Entity> variantToMain,
			Boolean swapVariants, Boolean useConceptNet, Boolean printAll){
		//sequence to list of results.
		Map<Entity, List<Entity>> results = new HashMap<Entity, List<Entity>>();
		
		
		List<Entity> youngSubjects = juv.get("juvenile").get("subject");
		if(youngSubjects == null) youngSubjects = new ArrayList<Entity>();
		
		List<Entity> youngObjects = juv.get("juvenile").get("object");
		if(youngObjects == null) youngObjects = new ArrayList<Entity>();
		
		//Mark.say("Young Subjects", youngSubjects);
		//TODO: fix this if its null breaks. do a refactor? look into why it'll be null and if that's OK
		for(Entity subject:youngSubjects){
			
			
			Entity juvenileSubject = subject;
			if(swapVariants) juvenileSubject = variantToMain.getOrDefault(subject, subject);
			
			Entity authorReferJuv = authorRefersToJuvenile(juvenileSubject);
			authorStory.add(authorReferJuv);
			
			for(Entity source : entityToSequence.get(juvenileSubject)) {
				RashiHelpers.putInDictKeytoList(authorStoryLineToSources, authorReferJuv, source);
			}
			
			
			if(printAll) Mark.say(subject, " is a YOUTH and is in the following sequences(subject): ", entityToSequence.get(subject));
			String currentSubject = subject.getName();
			List<Entity> allSequencesWithSubject = entityToSequence.get(subject);
			for(Entity sequenceWithSubjectEntity : allSequencesWithSubject){
				Map<String, Entity> seqSubAndObj = RashiHelpers.getSeqSubjectAndObject(sequenceWithSubjectEntity);
				if(seqSubAndObj.get("subject").equals(subject)){
					List<String> actionThreadString = RashiHelpers.getActionThreadString(sequenceWithSubjectEntity);
					String rating = Sentiment.getPosNegNeutral(actionThreadString, useConceptNet, printAll);
					if(rating.equals("positive")){
						results.putIfAbsent(sequenceWithSubjectEntity, new ArrayList<Entity>());
						results.get(sequenceWithSubjectEntity).add(AuthorStoryGenerators.getEntityResultSubject("invokes admiration", currentSubject, "positive", "subject", printAll));
						if(printAll) Mark.say(subject, " is a YOUTH doing a POSITIVE action,", actionThreadString, " therefore SUPRISE and ADMIRATION are intended");
					}
					if(rating.equals("negative")){
						results.putIfAbsent(sequenceWithSubjectEntity, new ArrayList<Entity>());
						results.get(sequenceWithSubjectEntity).add(AuthorStoryGenerators.getEntityResultSubject("decreases blame", currentSubject, "negative", "subject", printAll));
						if(printAll) Mark.say(subject, " is a YOUTH doing a NEGATIVE action,", actionThreadString, " therefore SUPRISE and LESSENED BLAME are intended ");
					}
				}	
			}
		}
		
		//Mark.say("Young Subjects", youngObjects);
		for(Entity object:youngObjects){
			
			
			Entity juvenileObject = object;
			if(swapVariants) juvenileObject = variantToMain.getOrDefault(object, object);
			authorStory.add(authorRefersToJuvenile(juvenileObject));
			
			if(printAll) Mark.say(object, " is a YOUTH and is in the following sequences(object)", entityToSequence.get(object));
			
			
			String currentObject = object.getName();
			List<Entity> allSequencesWithObject = entityToSequence.get(object);
			//TODO: always when child is involved element of suprise --> add this in as a general rule
			
			
			
			for(Entity sequenceWithObjectEntity : allSequencesWithObject){
				
				Map<String, Entity> seqSubAndObj = RashiHelpers.getSeqSubjectAndObject(sequenceWithObjectEntity);
				if(seqSubAndObj.get("object").equals(object)){
					String currentSubject = seqSubAndObj.get("subject").getName();
					List<String> actionThreadString = RashiHelpers.getActionThreadString(sequenceWithObjectEntity);
					String rating = Sentiment.getPosNegNeutral(actionThreadString, useConceptNet, printAll);
					if(rating.equals("positive")){
						if(printAll) Mark.say(object, " is a YOUTH receiving a POSITIVE action,", actionThreadString ," therefore RESPECT and ADMIRATION are intended");
						results.putIfAbsent(sequenceWithObjectEntity, new ArrayList<Entity>());
						results.get(sequenceWithObjectEntity).add(getEntityResultYouthObject("invokes admiration", currentSubject, currentObject, "positive"));
					}
					if(rating.equals("negative")){
						
						
						authorStory.add(JuvenileMechanism.authorRefersToJuvenileVictim(juvenileObject));
						if(printAll) Mark.say(object, " is a YOUTH receiving a NEGATIVE action,", actionThreadString, " therefore SUPRISE and HEIGHTENED BLAME are intended ");
						results.putIfAbsent(sequenceWithObjectEntity, new ArrayList<Entity>());
						results.get(sequenceWithObjectEntity).add(getEntityResultYouthObject("increases blame", currentSubject, currentObject, "negative"));
						}
					}	
				}
			}
	
		return results;
	}
	
	
	/**
	 * Reason hypothetically regarding an author depicting a specific entity as a youth.
	 * @param entityToSequence
	 * @param juv
	 * @param printAll
	 * @return
	 */
	public static List<Map<Entity, List<Entity>>> hypotheticalReasoningJuvenile(Map<Entity, List<Entity>> entityToSequence, Map<String, List<Entity>> juv, 
			Map<Entity,Entity> variantToMain, Boolean swapVariants, Boolean useConceptNet, Boolean printAll){
		if(printAll) Mark.say("\nHYPOTHETICAL REASONING\n");
		
		Map<Entity, List<Entity>> result = new HashMap<Entity, List<Entity>>();
		Map<Entity, Entity> youthToAdult = new HashMap<Entity, Entity>();
		
		Map<Entity, List<Entity>> newEntityToSequence = new HashMap<Entity, List<Entity>>();
		Map<Entity, Vector<String>> newSubjects = new HashMap<Entity, Vector<String>>();
		Map<Entity, Vector<String>> newObjects = new HashMap<Entity, Vector<String>>();
		Map<Entity, ArrayList<Object>> newEntityToFeatures = new HashMap<Entity, ArrayList<Object>>();
		ArrayList<Entity> newAuthorStory = new ArrayList<Entity>();
		Map<Entity, List<Entity>> newAuthorStoryLineToSources = new HashMap<Entity, List<Entity>>();
		
		//subject 
		
		List<Entity> listJuvenileSubjects = juv.get("subject");
		if(listJuvenileSubjects == null) listJuvenileSubjects = new ArrayList<Entity>();
		
		for(Entity juvenileSubject: listJuvenileSubjects){
			
			if(printAll) Mark.say("Current Juvenile SUBJECT", juvenileSubject);
			
			List<Entity> allSequenceWithSubject = entityToSequence.get(juvenileSubject);
			
			for(Entity seqWithJuvSubject: allSequenceWithSubject){
				result.putIfAbsent(seqWithJuvSubject, new ArrayList<Entity>());
				List<Entity> seqUpdates = result.get(seqWithJuvSubject);
				
				//Mark.say("COMPONENTS", seqWithJuvSubject.relationP(), seqWithJuvSubject.getAllComponents());
				//Mark.say("oldSubject:", juvenileSubject);
				Entity newAdultSubject = JuvenileMechanism.getAdult(youthToAdult, juvenileSubject);
				//Mark.say("newSubject", newAdultSubject); //want to see the ID
				
				Entity withSubjectSubbed = Substitutor.substitute(newAdultSubject, juvenileSubject, seqWithJuvSubject);
				
				RashiHelpers.storeSubjectObject(withSubjectSubbed, newSubjects, newObjects, newEntityToSequence, newEntityToFeatures, printAll);
				
				seqUpdates.add(withSubjectSubbed);
				
				//Mark.say("BEFORE:", seqWithJuvSubject.toEnglish());
				//Mark.say("AFTER:", withSubjectSubbed.toEnglish());
			}
		}
		//object
		List<Entity> listJuvenileObjects = juv.get("object");
		if(listJuvenileObjects == null) listJuvenileObjects = new ArrayList<Entity>();
		
		//Mark.say("JUV OBJECTS", listJuvenileObjects);
		for(Entity juvenileObject: listJuvenileObjects){
			if(printAll){
				Mark.say("Current Juvenile Object", juvenileObject);
			}
			List<Entity> allSequenceWithObject = entityToSequence.get(juvenileObject);
			
			
			for(Entity seqWithJuvObject: allSequenceWithObject){
				result.putIfAbsent(seqWithJuvObject, new ArrayList<Entity>());
				List<Entity> seqUpdates = result.get(seqWithJuvObject);
				
				//Mark.say("COMPONENTS", seqWithJuvObject.relationP(), seqWithJuvObject.getAllComponents());
				//Mark.say("oldObject:", juvenileObject);
			
				Entity newAdultObject = JuvenileMechanism.getAdult(youthToAdult, juvenileObject);
				//Mark.say("newObject", newAdultObject); //want to see the ID
				
				Entity withObjectSubbed = Substitutor.substitute(newAdultObject, juvenileObject, seqWithJuvObject);
				
				RashiHelpers.storeSubjectObject(withObjectSubbed, newSubjects, newObjects, newEntityToSequence, newEntityToFeatures, printAll);
				
				seqUpdates.add(withObjectSubbed);
				
				//Mark.say("BEFORE:", seqWithJuvObject.toEnglish());
				//Mark.say("AFTER:", withObjectSubbed.toEnglish());
				
			}
		}
		if(printAll) Mark.say("\n \n");
		
		List<Map<Entity, List<Entity>>> finalResult = new ArrayList<Map<Entity, List<Entity>>> ();
		Map<String, Map<String, List<Entity>>> newJuv = WordMechanisms.processAspect(newSubjects, newObjects, JuvenileMechanism.getCommonWord(), JuvenileMechanism.getValidProperties());
		
		//Mark.say("NEW JUV", newJuv, "RESULT", result);
		finalResult.add(result);
		Map<Entity, List<Entity>> processResult = processJuvenile(newJuv, newEntityToSequence, newAuthorStory, newAuthorStoryLineToSources, variantToMain, swapVariants, useConceptNet, printAll);
		//Mark.say("Process Result", processResult);
		finalResult.add(processResult);
		return finalResult;
		
	}
	
	
	/**
	 * Generate a readable summary of hypothetical analysis regarding characters' age.
	 * @param hypoJuvResultsForPrint
	 * @param hypoJuvResultsForComparison
	 * @return
	 */
	public static String presentHypotheticalResults(Map<Entity, List<Entity>> hypoJuvResultsForPrint, Map<Entity, List<Entity>> hypoJuvResultsForComparison ){

		String commentary = Html.bold("Hypothetical results, relating to characters' age:\n");

		Mark.say("<-----Hypothetical results, relating to characters' age----->");
		//Mark.say("hypo comparison", hypoJuvResultsForComparison);
		for(Entity seq : hypoJuvResultsForPrint.keySet()){
			commentary += ("  If the original title: " + Html.ital(seq.toEnglish()) + "\n");
			Mark.say("  If the original title: ", seq.toEnglish());
			for(Entity res : hypoJuvResultsForPrint.get(seq)){
				Mark.say("   ...had instead been:", res.toEnglish());
				commentary += (Html.ital("    ...had instead been: ") + Html.red(res.toEnglish()) + "\n");
				int count = 0;
				if(hypoJuvResultsForComparison != null){
					if(hypoJuvResultsForComparison.containsKey(res)){
						commentary += "  The following conclusions would have been reached:\n";
						Mark.say("  The following conclusions would have been reached:");
						count +=1;
						for(Entity con : hypoJuvResultsForComparison.get(res)){
							commentary+= ("    " + Html.blue(con.toEnglish()) + "\n");
							Mark.say("    ", con.toEnglish());
						}
					}
				}
				if(count == 0){
					commentary += Html.blue("    Then no special conclusions would have been reached");
					Mark.say("    Then no special conclusions would have been reached");
				}
				
			}
		}
		return commentary;
	}
	
}
