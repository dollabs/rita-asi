// Updated 10 June 2015

package subsystems.rashi;

import java.util.*;

import connections.*;
import connections.signals.BetterSignal;
import constants.*;
import frames.entities.Entity;
import frames.entities.Sequence;
import generator.Generator;
import generator.RoleFrames;
import genesis.GenesisGetters;
import mentalModels.MentalModelDemo;
import start.Start;
import storyProcessor.StoryProcessor;
import suriBandler.LocalGenesisForSuri;
import utils.Html;
import translator.Translator;
import utils.Mark;


/**
 * A local processor class that just receives a complete story description, takes apart the wrapper object to fetch
 * various parts of the complete story description, and prints them so you can see what is in there.
 */
public class RashisExperts extends AbstractWiredBox {

	// Examples of how ports are named, not used here
	public static final String MY_INPUT_PORT = "my input port";
	public static final String MY_OUTPUT_PORT = "my output port";
	
	//need to send author so consistent, to be switched via box
	public static final String GET_AUTHOR_PORT = "get author port";
	
	// Trying to export sources
	public static final String MY_SOURCES_PORT = "my sources port";

	public static final String COMMENTARY = "Rashi commentary";
	
	// new 3/6 trying to receive questions
	public static String QUESTION_PORT = "question port";
	
	//new 4/30/18 for running from question line
	public static String TRIGGER_PORT = "trigger port";
	
	
	boolean printAll = true;
	boolean swapVariants = true;
	boolean useConceptNet = false;
	public Entity author = Translator.getTranslator().translate("author");//new Entity("author");
	
	// Author story
	ArrayList<Entity> authorStory; 
			
	//Map<String, List<Entity>> authorStoryLineToSources = new HashMap<String, List<Entity>>();
	Map<Entity, List<Entity>> authorStoryLineToSources;
		
		
	/**
	 * 
	 */
	public RashisExperts() {

		super("Rashi system");
		// Receives story processor when story has been processed
		Connections.getPorts(this).addSignalProcessor(Start.STAGE_DIRECTION_PORT, this::reset);
		Connections.getPorts(this).addSignalProcessor(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, this::processStoryProcessor);
		Connections.getPorts(this).addSignalProcessor(Start.START_VIEWER_PORT, this::unnamedActors); //Start.START_VIEWER_PORT
		
		Connections.getPorts(this).addSignalProcessor(RashisExperts.GET_AUTHOR_PORT, this::saveAuthor);
		
		// added 3/6 trying to receive questions 
		//Connections.getPorts(this).addSignalProcessor(QUESTION_PORT, this::processQuestion); 
		
		//added 4/30
		Connections.getPorts(this).addSignalProcessor(RashisExperts.TRIGGER_PORT, this::triggerProcessor);
		
		this.authorStory = new ArrayList<Entity>();
		authorStoryLineToSources = new HashMap<Entity, List<Entity>>();
		
		Mark.say("Finished Adding Ports for Suri");
		
	}
	
	public void saveAuthor(Object signal) {
		author = (Entity) signal;
	}
	public void triggerProcessor(Object signal) {
		Mark.say("Trigger processor received", signal);
		Connections.getPorts(this).transmit(MY_SOURCES_PORT, authorStoryLineToSources);
		for(Entity e : authorStory) Connections.getPorts(this).transmit(MY_OUTPUT_PORT, e);
		
	}
	
	/**
	 * Question processing stuff
	 */
	public void processQuestion(Object s) {
        Mark.say("In Suri's Question Processor");

        if (s instanceof BetterSignal) {
            BetterSignal signal = (BetterSignal) s;
            Entity fullQuestion = signal.get(2, Entity.class);
            Entity questionContent = signal.get(0, Entity.class);
            
            Mark.say("Suri's full question: ", fullQuestion);
            Mark.say("Suri's question content:", questionContent.isA("disjuction"));
        }
	}
	
		/**
		 * Commentary panel stuff
		 */
		private void say(Object... objects) {
			// First argument is the box that wants to write a message
			// Second argument is commentary port wired to the commentary panel
			// Third argument is location on screen: LEFT, RIGHT, BOTTOM
			// Fourth argument is tab title
			// Final arguments are message content
			
			// Panel better set in story
		// Mark.comment(this, COMMENTARY, GenesisConstants.BOTTOM, "RASHI Output", objects);
		Mark.comment(this, COMMENTARY, null, "RASHI Output", objects);
		}
		
	/**
	 * You have to make all signal processors void methods of one argument, which must be of the Object class, so there
	 * will be a bit of casting.
	 * <p>
	 * This one writes information extracted from the story processor received on the STORY_PROCESSOR_SNAPSHOT port.
	 */
	public void processStoryProcessor(Object signal) {
		Mark.say("In processStoryProcessor for Suri");
		boolean debug = true;
		// Make sure it is what was expected
		Mark.say("Entering processStoryProcessor");

		if (signal instanceof StoryProcessor) {
			StoryProcessor processor = (StoryProcessor) signal;
			Sequence story = processor.getStory();
			Sequence explicitElements = processor.getExplicitElements();
			Sequence inferences = processor.getInferredElements();
			Sequence concepts = processor.getInstantiatedConceptPatterns();
			Mark.say(debug, "\n\n\nStory elements");
			story.getElements().stream().forEach(f -> Mark.say(debug, f));
			
			//for (String key : processor.conceptMap)
			Mark.say("\n\n\n<====Testing Begin=====>\n");
			//story.getElements().stream().forEach(f -> Mark.say(debug, f.getClass()));
			//story.getElements().stream().forEach(f -> Mark.say(debug, f.getAllComponents()));
			//story.getElements().stream().forEach(f -> Mark.say(debug, f.getChildren()));
			//Set<HashSet<Entity>> children = new HashSet<HashSet<Entity>>();
			Set<Entity> children = new HashSet<Entity>();
			story.getElements().stream().forEach(f -> children.add(f));//.getChildren()));
			String processResult = processElements(children);
			Mark.say("\n<====End=====>\n\n\n");
			
			Mark.say(debug, "\n\n\nExplicit story elements");
			explicitElements.stream().forEach(e -> Mark.say(debug, e));
			Mark.say(debug, "\n\n\nInstantiated commonsense rules");
			inferences.stream().forEach(e -> Mark.say(debug, e));
			Mark.say(debug, "\n\n\nInstantiated concept patterns");
			concepts.stream().forEach(e -> Mark.say(debug, e));
			Mark.say(debug, "\n\n\nAll story elements, in English");
			Generator generator = Generator.getGenerator();
			story.stream().forEach(e -> Mark.say(debug, generator.generate(e)));

			processor.getRuleMemory().getRuleSequence().getElements().stream().filter(r -> r.getProbability() == null ? true : false)
			        .forEach(r -> Mark.say(debug, "Rule:", r.getProbability(), r));

			Mark.say("Recorded stories", GenesisGetters.getMentalModel1().getStoryMemory().size());

			Mark.say("Map size", GenesisGetters.getMentalModel1().getStoryMemory().getMemory().size());

			GenesisGetters.getMentalModel1().getStoryMemory().getMemory().values().stream().forEach(m -> Mark.say("Title", m.getTitle()));
			//say("There are", processor.getStory().getElements().size(), "story elements");
			say(processResult);
		}
	
	
	}
	
	public void combineResultsHelper(Map<Entity, List<Entity>> seqToAllResults, Map<Entity, List<Entity>> currentMapEntityToListEntity){
		
		for(Entity seq : currentMapEntityToListEntity.keySet()){
			if(!seqToAllResults.containsKey(seq)) seqToAllResults.put(seq, new ArrayList<Entity>());
			
			List<Entity> currentSeqResults = seqToAllResults.get(seq);
			
			List<Entity> currentListEntity = currentMapEntityToListEntity.get(seq);
			currentSeqResults.addAll(currentListEntity);
		}
		
	}
	public void combineResults(Map<Entity, List<Entity>> seqToAllResults, Map<Entity, List<Entity>> seqToUncertaintyConclusions, 
			Map<Entity, List<Entity>> juvProcessed,  Map<Entity, Entity> passiveConclusions,
			Map<Entity, List<Entity>> hypoJuvResultsForComparison, Map<Entity, List<Entity>> hypoJuvResultsForPrint){
		
		combineResultsHelper(seqToAllResults, seqToUncertaintyConclusions);	
		combineResultsHelper(seqToAllResults, juvProcessed);
		//combineResultsHelper(seqToAllResults, hypoJuvResultsForComparison);
		
		
		for(Entity seq: passiveConclusions.keySet()){
			if(seqToAllResults.containsKey(seq)){
				seqToAllResults.get(seq).add(passiveConclusions.get(seq));
			}
			else{
				seqToAllResults.put(seq, new ArrayList<Entity>());
				seqToAllResults.get(seq).add(passiveConclusions.get(seq));
			}
		}
		
		
	}
	
	public Map<Entity, Entity> gatherVariantCharacters(Set<Entity> seqs){
		
		Map<Entity, Entity> variantToMain = new HashMap<Entity, Entity>();
		
		
		for(Entity seq: seqs) {
			
			Map<String, Entity> finalizedRoles; 
			
			if(seq.relationP()) {
				finalizedRoles = RashiHelpers.getSeqSubjectAndObjectForRelation(seq);
			}
			else finalizedRoles = RashiHelpers.getSeqSubjectAndObject(seq);
			
			
			Entity seqSubject = finalizedRoles.get("subject");
			Entity seqObject = finalizedRoles.get("object");
			
			if(seq.hasProperty("characterized", true)) {
				if(!variantToMain.containsKey(seqSubject) && seqObject!=null && seqSubject !=null) {
					Mark.say("FOUND VARIANT");
					variantToMain.put(seqSubject, seqObject);
					}
			}
		}
		
		return variantToMain;
	}
	public String processElements(Set<Entity> seqs){
		String commentary = "";
		
		//Generator generator = Generator.getGenerator();	
		Map<Entity, Vector<String>> subjects = new HashMap<Entity, Vector<String>>();
		Map<Entity, Vector<String>> objects = new HashMap<Entity, Vector<String>>();
	
		Map<Entity, List<Entity>> entityToSequence = new HashMap<Entity, List<Entity>>();
		Map<Entity, ArrayList<Object>> entityToFeatures = new HashMap<Entity, ArrayList<Object>>();
		
		Map<String, List<List<Entity>>> wordFinds = new HashMap<String, List<List<Entity>>>();
		Map<Entity, List<Entity>>  seqToUncertaintyConclusions = new HashMap<Entity, List<Entity>>();
		Map<Entity, List<String>> actionThreads = new HashMap<Entity, List<String>>();
		Map<String, Map<String, List<Entity>>> juv;
		Map<Entity, List<Entity>> juvProcessed;
		Map<Entity, List<Entity>> hypoJuvResultsForComparison = null;
		Map<Entity, List<Entity>> hypoJuvResultsForPrint = null;
		
		Map<Entity, List<Entity>> seqToAllResult = new HashMap<Entity, List<Entity>>();
		List<Map<Entity, List<Entity>>> hypotheticalJuvenile;
		
		
		
		
		// Map sequence to passive conclusions
		Map<Entity, Entity> passiveConclusions = new HashMap<Entity, Entity>();
		
		// Author story
		authorStory = new ArrayList<Entity>();
		//Map<String, List<Entity>> authorStoryLineToSources = new HashMap<String, List<Entity>>();
		authorStoryLineToSources = new HashMap<Entity, List<Entity>>();
		
		// Map all variations of same character to one 
		
		Map<Entity, Entity> variantToMain = new HashMap<Entity, Entity>(); 
		if(swapVariants) variantToMain = gatherVariantCharacters(seqs);
		if(swapVariants && printAll) Mark.say("VARIANT CHARACTERS", variantToMain);
		
		//Mark.say("CONFIRM ALLEGED", ConceptNetClient.getSimilarityScore("confirm", "alleged").getResult());
		//Mark.say("PATENT ALLEGED", ConceptNetClient.getSimilarityScore("patent", "alleged").getResult());
		//Mark.say("CONFIRM BELIEVED", ConceptNetClient.getSimilarityScore("confirm", "believed").getResult());
		//Mark.say("CONFIRM ostensible", ConceptNetClient.getSimilarityScore("confirm", "ostensible").getResult());
		//Mark.say("PUPIL CHILD", ConceptNetClient.getSimilarityScore("pupil", "child").getResult());
		
		
		for (Entity seq: seqs){
			//if(seq.getElements().size() > 0 && seq.getElement(seq.getElements().size()-1).getObject() !=null) {
				//List<Entity> seqObjectElements = seq.getElement(seq.getElements().size()-1).getObject().getElements();
				//if(seqObjectElements!=null && seqObjectElements.get(seqObjectElements.size()-1).hasProperty("obviously")) {
				
			if(seq.hasProperty("obviously")) {
					//TODO: This shouldn't only be able to be believe! Should be anything general.
					Entity whatIWant = RoleFrames.makeRoleFrame(author, "believe", seq);
					Mark.say("WHAT I WANT", Generator.getGenerator().generate(whatIWant));
					if(seq.hasProperty("let-through")) {
						whatIWant = seq;
					}
					authorStory.add(whatIWant);
					
				}
			
			// <----- HERE and below, refactor ---->

			if (seq.getBundle()!=null){
				
				List<String> actionThreadString = RashiHelpers.getActionThreadString(seq);
				actionThreads.put(seq, actionThreadString);
				}
			
			if(printAll) Mark.say("    Analyzing Sequence -----> ", seq.toEnglish());//, seq.relationP());
			
			
			//finalizedRoles maps String depicting role i.e, subject --> Entity
			Map<String, Entity> finalizedRoles = RashiHelpers.storeSubjectObject(seq, subjects, objects,entityToSequence, entityToFeatures, printAll);
		
			Entity seqObject = finalizedRoles.get("object");
			Entity seqSubject = finalizedRoles.get("subject");
			
			
			if(printAll) Mark.say("Sequence is passive: ", seq.hasFeature(Markers.PASSIVE));
			
			if(seq.hasFeature(Markers.PASSIVE)) {
				Mark.say("Now analyzing for passive, with author:", author.toEnglish());
				
				Entity passiveSubject = seqSubject;
				Entity passiveObject = seqObject;
				if(swapVariants) passiveSubject = RashiHelpers.getVariant(seqSubject, variantToMain);
				if(printAll) Mark.say("getting passive conclusions for original subject ", seqSubject, "as variant ", passiveSubject );
				PassiveMechanism.getPassiveConclusions(seq, passiveSubject, seqObject, passiveConclusions, authorStory, authorStoryLineToSources, author, printAll);
			}
			//if(printAll && seqSubject != null) Mark.say("CHECKING FOR CHARACTERIZED", seqSubject.hasFeature(Markers.CHARACTERIZED));
			//boolean isARelation = seq.relationP();
			//boolean isEntityClass = seq.entityP("classification") ;
			
			
			//Mark.say(seq.getProperty("characterized"));
			
			
			//TODO: Polarity. for positive: beneficiary, benefactor, etc. 
			String actionClass = Sentiment.getPosNegNeutral(actionThreads.get(seq), useConceptNet, printAll);
			if(seqObject != null){
				
				Entity victimResult = null; 
				
				if(actionClass.equals("negative")) {
					Entity victim = seqObject;
					if(swapVariants) victim = RashiHelpers.getVariant(victim, variantToMain);
					
					Mark.say("VICTIM FOUND:", victim);
					victimResult = AuthorStoryGenerators.victimMetric(victim);
					
//					boolean drawsBlood = Sentiment.actionDrawsBlood(actionThreads.get(seq), useConceptNet, printAll);
//					Entity drawsBloodResult = null;
//					if(drawsBlood) {
//						drawsBloodResult = AuthorStoryGenerators.drawsBloodMetric(seqObject);
//					}
//					
//					if(drawsBloodResult!=null) authorStory.add(drawsBloodResult);
					}
				
				Entity beneficiaryResult = null;
				if(actionClass.equals("positive")) {
					Entity beneficiary = seqObject;
					if(swapVariants) beneficiary = RashiHelpers.getVariant(beneficiary, variantToMain);
					
					beneficiaryResult =  AuthorStoryGenerators.beneficiaryMetric(beneficiary);
					
				}
				
				
				if(victimResult !=null) {
					authorStory.add(victimResult);
					RashiHelpers.putInDictKeytoList(authorStoryLineToSources, victimResult, seq);
				}
				if(beneficiaryResult !=null) {
					authorStory.add(beneficiaryResult);
					RashiHelpers.putInDictKeytoList(authorStoryLineToSources, beneficiaryResult, seq);
				}
				
			}
			if(seqSubject!=null){
				Entity blameResult = null;
				Entity blameSubject = seqSubject;
				
				Entity benefactorResult = null;
				Entity benefactorSubject = seqSubject;
				
				if(actionClass.equals("negative") || actionClass.equals("positive")) {
					
					Vector<String> objectDescriptions = objects.get(seqObject);
					boolean isObjectNegative = Sentiment.isEntityNegative(seqObject, objectDescriptions, useConceptNet);
					boolean isObjectVariantNegative = false; 
					
					if(swapVariants) { 
						Entity variantObject = RashiHelpers.getVariant(seqObject, variantToMain);
						Vector<String> variantDescriptions = objects.get(variantObject);
						isObjectVariantNegative = Sentiment.isEntityNegative(variantObject, variantDescriptions, useConceptNet);
					}
					
					if(actionClass.equals("negative")) {
						if(swapVariants) blameSubject = RashiHelpers.getVariant(blameSubject, variantToMain);
						blameResult = AuthorStoryGenerators.blameMetric(blameSubject, (isObjectNegative || isObjectVariantNegative));
					}
					
					if(actionClass.equals("positive")) {
						if(swapVariants) benefactorSubject = RashiHelpers.getVariant(benefactorSubject, variantToMain);
						benefactorResult = AuthorStoryGenerators.benefactorMetric(benefactorSubject, (isObjectNegative || isObjectVariantNegative));
					}
					
				}
				
				if(blameResult!=null) {
					authorStory.add(blameResult);
					RashiHelpers.putInDictKeytoList(authorStoryLineToSources, blameResult, seq);
				}
				if(benefactorResult!=null) {
					authorStory.add(benefactorResult);
					RashiHelpers.putInDictKeytoList(authorStoryLineToSources, benefactorResult, seq);
				}
				
				
			}
			// <----- HERE and above, refactor ---->
			
			
			Entity uncertaintySubject = RashiHelpers.getVariant(seqSubject, variantToMain);
			Entity uncertaintyObject = RashiHelpers.getVariant(seqObject, variantToMain);
			
			
			
			UncertaintyMechanism.processUncertainties(wordFinds, seq, uncertaintySubject, uncertaintyObject, seqToUncertaintyConclusions, actionThreads, authorStory, authorStoryLineToSources, useConceptNet, printAll);
			
			Mark.say("\n \n \n");
			}
		
		//Mark.say("SUBJECTS\n", subjects);
		//Mark.say("OBJECTS\n", objects);
		
		if(printAll){
			Mark.say("WORD FINDS \n", wordFinds);
			Mark.say("ALLEGED FINDS \n"); 
		for(Entity seq: seqToUncertaintyConclusions.keySet()){
			Mark.say("Alleged found in following sequence ", seq.toEnglish());
			List<Entity> conclusions = seqToUncertaintyConclusions.get(seq);
			Mark.say("Conclusions drawn from previous sequence: ");
			for(Entity con : conclusions){
				Mark.say(con.toEnglish());
				}
			}
		}
		juv = WordMechanisms.processAspect(subjects, objects, JuvenileMechanism.getCommonWord(), JuvenileMechanism.getValidProperties());
		if(printAll){
			Mark.say("SUBJECTS", subjects);
			Mark.say("Objects", objects);
			Mark.say("PROCESS YOUTH", juv);
		}
		//TODO: Update process to better handle the classification of verbs
		juvProcessed = JuvenileMechanism.processJuvenile(juv, entityToSequence, authorStory, authorStoryLineToSources, variantToMain, swapVariants, useConceptNet, printAll);
		
		if(printAll){
		Mark.say("Analyzing for youth:");
		for(Entity key : juvProcessed.keySet()){
			Mark.say("The sequence :", key.toEnglish(), " produced...: ");
			for(Entity res : juvProcessed.get(key)){
				Mark.say(res.toEnglish()); //TODO: make this mirror adjective
				}
			}
		}
		
		
		
		if(printAll) Mark.say("RESULTS OF PROCESSING JUVENILE", juvProcessed);
		if(printAll) Mark.say("hypothetical Results:\n");
		if(!juvProcessed.isEmpty()){ 
			hypotheticalJuvenile = JuvenileMechanism.hypotheticalReasoningJuvenile(entityToSequence, juv.get("juvenile"), variantToMain, swapVariants, useConceptNet, printAll);
			hypoJuvResultsForPrint = hypotheticalJuvenile.get(0);
			hypoJuvResultsForComparison = hypotheticalJuvenile.get(1);
			if(printAll) Mark.say("hypothetical juvenile for comparison", hypoJuvResultsForComparison);
			
			for(Entity seq : hypoJuvResultsForPrint.keySet()){
				if(printAll) Mark.say("The sequence:", seq.toEnglish(), "wondered what the reader would think instead if they said...");
				for(Entity newSeq : hypoJuvResultsForPrint.get(seq)){
					if(printAll) Mark.say(newSeq.toEnglish());
				}
			}
			
		}
		
		if(printAll){
		Mark.say("RESULTS OF PASSIVE");
		for(Entity seq : passiveConclusions.keySet()){
			Mark.say("From the sentence: ", seq.toEnglish(), "it was concluded that: ");
			Mark.say(passiveConclusions.get(seq).toEnglish());
			}
		//Mark.say("ENTITY TO SEQUENCE", entityToSequence);
		//Mark.say("ENTITY TO FEATURES", entityToFeatures);
		//Mark.say("SEQUENCE TO ACTION", actionThreads);
		}
		
		
		combineResults(seqToAllResult, seqToUncertaintyConclusions, juvProcessed, passiveConclusions, hypoJuvResultsForComparison, hypoJuvResultsForPrint);
		
		Mark.say("<------Reporting All Conclusions, by Title (Sentence) ------->");
		for(Entity seq : seqToAllResult.keySet()){
			commentary += (Html.bold("Title: " )+ Html.ital(seq.toEnglish())+"\n");
			Mark.say("Title: ", seq.toEnglish());
			Mark.say("  Conclusions:");
			commentary += Html.bold(("  Conclusions:\n"));
			
			for(Entity conclusion : seqToAllResult.get(seq)){
				Mark.say("         ", conclusion.toEnglish());
				//commentary += ("         " + conclusion.toEnglish() + "\n");
				commentary += (conclusion.toEnglish() + "\n");
			}
			commentary += "\n";
		}
		//commentary +="\n\n";
		if(hypoJuvResultsForPrint!=null){
		commentary += JuvenileMechanism.presentHypotheticalResults(hypoJuvResultsForPrint, hypoJuvResultsForComparison);
		}
		
		HashMap<String, Integer> countAuthorActions = EmphasisMechanism.countRepeatAuthorActions(authorStory);
		
		ArrayList<Entity> emphasisAdditions = new ArrayList<Entity>();
		HashSet<String> emphasisAdditionsString = new HashSet<>();
		
		for(Entity s : authorStory) {
			String authorStoryPart = Generator.getGenerator().generate(s);	
			if(countAuthorActions.get(authorStoryPart) > 1 && !emphasisAdditionsString.contains(authorStoryPart)) {
				emphasisAdditions.add(s);
				emphasisAdditionsString.add(authorStoryPart);
			}
			
		}
		
		authorStory.addAll(EmphasisMechanism.processRepeats(emphasisAdditions, authorStoryLineToSources));
		
		
		 
		Mark.say("< ---- AUTHOR STORY ----- >");
		
		String authorStoryString = "";
		
		String authorStoryEnglish = "";
		for(Entity s : authorStory){
			
			String authorStoryPart = Generator.getGenerator().generate(s);				
			authorStoryString += (authorStoryPart + "\n");
		}
		authorStoryEnglish += authorStoryString;
		Mark.say(authorStoryString);
		Mark.say("< ------------------ >");
		
		
		Mark.say("Transmitting author story from ", this.getName(), "!");	
		
		Connections.getPorts(this).transmit(MY_SOURCES_PORT, authorStoryLineToSources);
		for(Entity e : authorStory) Connections.getPorts(this).transmit(MY_OUTPUT_PORT, e);
		
		
		Mark.say("finished transmitting author story");
		
		return authorStoryEnglish;
		//return commentary;
}
	 
	public void reset(Object signal) {
		//Does nothing right now
		//Mark.say("Reset was called on signal:", signal);
		//Mark.say("Finished reset for Suri, signal:", signal);
	}
	public void unnamedActors(Object signal){
		//Mark.say("unnamedActors called", signal);
		//Mark.say("<------Running Unnamed Actors---------->");
		//Mark.say("SIGNAL", signal);
		
	}
	
	/**
	 * Merely calls main method in LocalGenesis, a shortcut
	 */
	public static void main(String[] args) {
		MentalModelDemo.main(args);
	}
	
}