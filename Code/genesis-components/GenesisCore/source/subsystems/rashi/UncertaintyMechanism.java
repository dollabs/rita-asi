package subsystems.rashi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import conceptNet.conceptNetNetwork.ConceptNetClient;
import frames.entities.Entity;
import generator.Generator;
import matchers.StandardMatcher;
import matchers.Substitutor;
import start.Start;
import translator.Translator;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;

public class UncertaintyMechanism {

	
	public static ArrayList<String> getUncertaintyVerbs(){
		return new ArrayList<String>(Arrays.asList("allege", "claim", "suspect", "believe"));
	}
	/**
	 * Process if there are uncertainties and save the results.
	 * @param wordFinds
	 * @param seq
	 * @param seqSubject
	 * @param seqObject
	 * @param seqToUncertaintyConclusions
	 * @param actionThreads
	 * @param authorStory
	 */
	public static void processUncertainties(Map<String, List<List<Entity>>> wordFinds, Entity seq, Entity seqSubject, Entity seqObject, 
			Map<Entity, List<Entity>> seqToUncertaintyConclusions, Map<Entity, List<String>> actionThreads, ArrayList<Entity> authorStory,
			Map<Entity, List<Entity>> authorStoryLineToSources,
			Boolean useConceptNet, Boolean printAll){
		
		LList<PairOfEntities> resultAllegeMatcher;
		LList<PairOfEntities> resultBelieveMatcher;
		Translator t = Translator.getTranslator();
		t.translate("xx is a person");
		Entity allegeEntity = Translator.getTranslator().translateToEntity("xx alleges something");
		Entity believeEntity = Translator.getTranslator().translateToEntity("xx believe something");
		
		//ConceptNet 
		List<String> currentActionThread = actionThreads.get(seq);
		Double doubtScore = 0.0;
		String mostRelevantAction = "";
		if(currentActionThread.size()> 0){
			mostRelevantAction = currentActionThread.get(currentActionThread.size()-1);
			if(useConceptNet) {
			doubtScore = ConceptNetClient.getSimilarityScore("believed", mostRelevantAction).getResult();
			}
			if(printAll) Mark.say("ACTION THREAD for", mostRelevantAction, currentActionThread);
			if(printAll) Mark.say("SCORE", doubtScore);
		}
		
		
		resultAllegeMatcher = StandardMatcher.getBasicMatcher().match(seq, allegeEntity);
		resultBelieveMatcher = StandardMatcher.getBasicMatcher().match(seq, believeEntity);
		//TODO: make this more systematic, look into why believed matched with itself is -1.
		//Mark.say("MOST RELEVANT ACTION:", mostRelevantAction);
		if(mostRelevantAction.equalsIgnoreCase("") && printAll){
			Mark.say("NO ACTION FOUND IN", seq);
		}
		
		if (resultAllegeMatcher!=null || resultBelieveMatcher!=null || getUncertaintyVerbs().contains(mostRelevantAction) || doubtScore  > .40){
			
			String doubtWord = currentActionThread.get(currentActionThread.size()-1);
			if(printAll) Mark.say("DOUBT WORD", doubtWord, doubtScore);
			//if(wordFinds.containsKey("allege")){
			if(wordFinds.containsKey(doubtWord)){	
				//wordFinds.get("allege").add(Arrays.asList(seqSubject, seqObject));
				wordFinds.get(doubtWord).add(Arrays.asList(seqSubject, seqObject));
			}
			else{
				//wordFinds.put("allege", new ArrayList<List<Entity>>());
				//wordFinds.get("allege").add(Arrays.asList(seqSubject, seqObject));
				wordFinds.put(doubtWord, new ArrayList<List<Entity>>());
				wordFinds.get(doubtWord).add(Arrays.asList(seqSubject, seqObject));
			}
			
			seqToUncertaintyConclusions.putIfAbsent(seq, new ArrayList<Entity>());
			int endSubjectNameIndex = seqSubject.getName().indexOf("-");
			String subjectName = seqSubject.getName().substring(0, endSubjectNameIndex);
			int endObjectNameIndex = seqObject.getName().indexOf("-");
			String objectName = seqObject.getName().substring(0, endObjectNameIndex);
			
			seqToUncertaintyConclusions.get(seq).add(UncertaintyMechanism.getUncertaintyRoleResult(subjectName, objectName, "alleges", authorStory, printAll));
			getUncertaintyResultGeneral(seq, seqSubject, seqObject, authorStory, authorStoryLineToSources, printAll);
		}
		
		
	}
	
	/**
	 *  Get a statement about uncertainty in the general from: subject ...doubt word... object.
	 *  
	 *  TODO: Streamline it by replacing with consistent word that indicates doubt 
	 *  Currently assumes verb is always allege
	 *  
	 *  TODO: If subject is not author
	 * @param subject
	 * @param object
	 * @param action
	 * @param authorStory
	 * @param printAll
	 * @return
	 */
	public static void getUncertaintyResultGeneral(Entity seq, Entity seqSubject, Entity seqObject, ArrayList<Entity> authorStory,
			Map<Entity, List<Entity>> authorStoryLineToSources, Boolean printAll) {
		
		
		Entity allegeEntity = Translator.getTranslator().translateToEntity("xx alleges something");
		allegeEntity.setSubject(seqSubject);
		//allegeEntity.setObject(seqObject);
		//allegeEntity.setObject(Start.makeThing("something"));
		
		if(printAll) Mark.say("ADDING ALLEGATION TO AUTHOR STORY: ", allegeEntity.toEnglish());
		
		String subName = RashiHelpers.getEntityNameCleaned(seqSubject);
		String objName = RashiHelpers.getEntityNameCleaned(seqObject);
		
		Mark.say("Sequence here is:", seq.toEnglish());
		Mark.say("subject", seq.getSubject());
		Mark.say("SUB NAME", subName);
		Mark.say("object", seq.getObject());
		Mark.say("given object", seqObject);
		Mark.say("OBJ NAME", objName);
		
		
		Entity resultingSeq = (Entity) seq.clone();
		
		if(subName.equals("someone") || subName.equals("somebody")) {
			Entity author = Start.makeThing("Author");
			resultingSeq.setSubject(author);
			Mark.say("TRY ONE:", resultingSeq.toEnglish());
			Entity resultingSeq2 = Substitutor.substitute(seqSubject, author, seq);
			Mark.say("TRY TWO:", resultingSeq2.toEnglish());
		}
		
		
		String tryCompound = "author says that " + subName + " alleges something."; //swapped The author with author
		Mark.say("how would this look", tryCompound);
		Translator translator = Translator.getTranslator();
		Entity triedToCompound = translator.translate(tryCompound);
		Mark.say("TRIED TO COMPOUND AS ENTITY:", triedToCompound.toEnglish());
		
		
		String tryCompoundObj = "author says that " + objName + " is alleged.";
		//Mark.say("how would this look", tryCompoundObj);
		Entity triedToCompoundObj = translator.translate(tryCompoundObj);
		//Mark.say("TRIED TO COMPOUND AS ENTITY:", triedToCompoundObj.toEnglish());
		
		authorStory.add(triedToCompound);
		RashiHelpers.putInDictKeytoList(authorStoryLineToSources, triedToCompound, seq);
		authorStory.add(triedToCompoundObj);
		RashiHelpers.putInDictKeytoList(authorStoryLineToSources, triedToCompoundObj, seq);
		//authorStory.add(seq);
	}
	
	/**
	 * Generates sentence about where doubt is cast regarding role.
	 * @param subject
	 * @param object
	 * @param action
	 * @param authorStory
	 * @param printAll
	 * @return
	 */
	public static Entity getUncertaintyRoleResult(String subject, String object, String action, ArrayList<Entity> authorStory, Boolean printAll){
		if(subject.toLowerCase().contains("someone") || subject.toLowerCase().contains("somebody")){
			subject = "author";
		}
		//"The teacher casts doubt on the child's role because the teacher alleges the child's role.";
		String sentence = "The " +subject +" casts doubt on the " + object + "'s role because the " + subject + " " + action + " the " + object + "'s role.";
		
		Translator translator = Translator.getTranslator();
		Entity entity = translator.translate(sentence);
		if(printAll) Mark.say(subject, entity);
		
		
		String desiredObject = object + "'s role";
		translator = Translator.getTranslator();
		Entity entity2 = translator.translate("Author " + action + " "+ desiredObject);
		//Entity authorCastingDoubtResults = RoleFrames.makeRoleFrame("Author", action, desiredObject);
		if(printAll) {
			//Mark.say("DOUBT AUTHOR RESULTS:", authorCastingDoubtResults.toEnglish());
			//Mark.say(Generator.getGenerator().generate(authorCastingDoubtResults));
			Mark.say("DOUBT AUTHOR TRY 2:", entity2.toEnglish());
			Mark.say(Generator.getGenerator().generate(entity2));
		}
		//authorStory.add(authorCastingDoubtResults);
		//authorStory.add(entity2);
		
		
		return entity;
	}
	
	
}
