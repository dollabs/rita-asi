package subsystems.rashi;

import java.util.*;


import connections.*;
import constants.GenesisConstants;
import frames.entities.Entity;
import frames.entities.Sequence;
import generator.Generator;
import generator.RoleFrames;
import genesis.GenesisGetters;
import matchers.*;
import storyProcessor.StoryProcessor;
import suriBandler.LocalGenesisForSuri;
import utils.Html;
import utils.minilisp.LList;
import translator.Translator;
import utils.*;


public class SummarizeIntentions extends AbstractWiredBox {
	public static final String MY_INPUT_PORT = "my input port";
	public static final String MY_OUTPUT_PORT = "my output port";
	
	public static final String MY_SOURCES_PORT = "my sources port";
	public static final String SEND_AUTHOR_PORT = "send author port";

	public static final String COMMENTARY = "SumarizeIntentionsPort";
	
	boolean debug = false;
	boolean generatePropoganda = false;
	Map<Entity, List<Entity>> sourcesMap = new HashMap<Entity, List<Entity>>();
	
	Entity author;
	
	public Entity getAuthor() {
		return author;
	}
	
	public SummarizeIntentions(){
		super("Local story processor 2");
		Mark.say("In Summarize Intentions Processor for Suri");
		//Connections.getPorts(this).addSignalProcessor("processAuthorDecisions");
		Connections.getPorts(this).addSignalProcessor(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, this::processSnapshot);
		Connections.getPorts(this).addSignalProcessor(SummarizeIntentions.MY_INPUT_PORT, this::processAuthorDecisions);
		
		//trying to get sources 
		Connections.getPorts(this).addSignalProcessor(MY_SOURCES_PORT, this::saveSources);
	}
	
	public void saveSources(Object sources) {
		if(sources instanceof Map<?, ?>) {
			if(debug) Mark.say("Received Sources for Composition Story");
			sourcesMap = (Map<Entity, List<Entity>>) sources;
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

		// Placement better done in stories
		Mark.comment(this, COMMENTARY, null, "RASHI Summary", objects);
	}
	
	public void processSnapshot(Object snapshot) {
		Mark.say("Received story snapshot", snapshot);

		if (snapshot instanceof StoryProcessor) {
			StoryProcessor processor = (StoryProcessor) snapshot;
			Sequence story = processor.getStory();
			Sequence explicitElements = processor.getExplicitElements();
			Sequence inferences = processor.getInferredElements();
			Sequence concepts = processor.getInstantiatedConceptPatterns();
			
			
			Mark.say(debug, "\n\n\nStory elements");
			story.getElements().stream().forEach(f -> Mark.say(debug, f));
			
			Mark.say("\n\n\n<====Testing Begin=====>\n");
			// Adding this: 
			Set<Entity> allConcepts = new HashSet<Entity>();
			concepts.stream().forEach(e -> allConcepts.add(e));
			processConcepts(allConcepts);
			
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
			
			
			
			if(generatePropoganda){
			Set<Entity> precursors = new HashSet<Entity>();
			
			LList<PairOfEntities> result;
			Set<Entity> removedConsequently = new HashSet<Entity>();
			Map<Entity, LList<PairOfEntities>> elementToBindings = new HashMap<Entity, LList<PairOfEntities>>();
			for (Entity e1 : concepts.getElements()) {
				for (Entity e2 : processor.getConceptPatterns().getElements()) {
					Mark.say("Concept1", e1.getName(), "Concept2", e2.getName());
					List<Entity> e2Elements = e2.getElement(e2.getElements().size()-1).getObject().getElements();
					if(e2Elements.size() > 1 && !removedConsequently.contains(e2)) {
						removedConsequently.add(e2);
						//Mark.say("before removed", e2);
						e2.getElement(e2.getElements().size()-1).getObject().getElements().remove(e2Elements.size()-1);
						//Mark.say("after removed?", e2);
					}
					Entity toMatchData = e1.getElement(e1.getElements().size()-1);
					//Mark.say("Last Element1", toMatchData);
					Entity toMatchPattern = e2.getElement(e2.getElements().size()-1);
					//Mark.say("Last Element2", toMatchPattern);
				
					result = StandardMatcher.getBasicMatcher().match(toMatchPattern, toMatchData);
					
					Mark.say("Matching Results-----", result);
					if (result != null) {
						if(!e1.getType().equals(e2.getType())) {
							for(int i = 0; i < e2.getElements().size()-1; i++) {
								Entity precurser = e2.getElement(i);
								precursors.add(precurser);
								elementToBindings.put(precurser, result);
							}
						}
					}
				}
			}
			Mark.say("Precursers:", precursors);
			
			Set<Entity> needToAdd = new HashSet<Entity>();
			for (Entity pre : precursors) {
				boolean found = false;
				for (Entity comp : story.getElements()) {
					result = StandardMatcher.getBasicMatcher().match(pre, comp);
					if (result != null) {
						found = true;
					}
				}
				if (!found){
					Map<Entity, LList<PairOfEntities>> alsoNeedToAdd = new HashMap<>();
					Entity boundPre = Substitutor.substitute(pre, elementToBindings.get(pre));
					for (Entity e2 : processor.getConceptPatterns().getElements()) {
						Entity toMatchPattern = e2.getElement(e2.getElements().size()-1);
						result = StandardMatcher.getBasicMatcher().match(toMatchPattern, pre);//boundPre
						result = StandardMatcher.getBasicMatcher().match(toMatchPattern, boundPre);
						if(result != null) {
							for(int i = 0; i < e2.getElements().size()-1; i++) {
								Entity precurser = e2.getElement(i);
								alsoNeedToAdd.put(precurser, result);
							}
						}
						
					}
					for (Entity key : alsoNeedToAdd.keySet()) {
						Entity sub = Substitutor.substitute(key, alsoNeedToAdd.get(key));
						sub.addProperty("obviously", "obviously");
						needToAdd.add(sub);
					}
					boundPre.addProperty("obviously", "obviously");
					Mark.say("TRYING TO ADD OBVIOUSLY MANNER", boundPre.toEnglish());
					if(alsoNeedToAdd.keySet().size()==0) needToAdd.add(boundPre);
				}
			}
			Mark.say(needToAdd);
			for(Entity el: needToAdd) {
				Vector<Entity> els = el.getObject().getElements();
				Mark.say("PROPOGANDA ELEMENT", Generator.getGenerator().generate(el));
				Entity attempt = RoleFrames.makeRoleFrame(el.getSubject(), el.getType(), el.getObject());
				Mark.say("PROPOGANDA ELEMENT AGAIN", Generator.getGenerator().generate(attempt));
				String joined = "I did not find anything";
				Entity noAuthorel;
				el.addProperty("obviously", "obviously");
				if(el.relationP() && el.getSubject().toEnglish().toLowerCase().equals("author")) {
					author = el.getSubject();
					Connections.getPorts(this).transmit(SEND_AUTHOR_PORT, author);
					String engSeq = Generator.getGenerator().generate(el);
					String[] splitting = engSeq.split("\\s+");
					joined = String.join(" ", Arrays.copyOfRange(splitting, 2, splitting.length));
					
					//String isThisWhatINeed = elObject.getElement(0).getSubject().toEnglish();
					
					Entity elObject = el.getObject();
					el = elObject.getElement(0).getSubject();
					el.addProperty("obviously", "obviously");
					Connections.getPorts(this).transmit(MY_OUTPUT_PORT, el);
				
					
				}else {
					el.addProperty("let-through", "let-through");
					Connections.getPorts(this).transmit(MY_OUTPUT_PORT, el);
					Entity joinedTranslated = Translator.getTranslator().translateToEntity(joined);
					Mark.say(Generator.getGenerator().generate(joinedTranslated));
				}
			}
			//Connections.getPorts(this).transmit(MY_OUTPUT_PORT, Translator.getTranslator().translate("The End."));
			if(needToAdd.size()>0) {
				StoryProcessor storyProc = GenesisGetters.getMentalModel1().getStoryProcessor();
				Connections.getPorts(storyProc).transmit(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, storyProc);
				}	
			}
			
			
			Mark.say("Recorded stories", GenesisGetters.getMentalModel2().getStoryMemory().size());

			Mark.say("Map size", GenesisGetters.getMentalModel2().getStoryMemory().getMemory().size());

			GenesisGetters.getMentalModel2().getStoryMemory().getMemory().values().stream().forEach(m -> Mark.say("Title", m.getTitle()));
			//say("There are", processor.getStory().getElements().size(), "story elements");
			//say(processResult);
		}

	}
	
	public void processConcepts(Set<Entity> concepts) {
		Mark.say("Processing Concepts");
		
		// ----- BLAME / AGENCY ----- //
		BlameIntentions blameIntentions = new BlameIntentions(debug);
		RetributionIntentions retributionIntentions = new RetributionIntentions(blameIntentions, debug);
		PassivityIntentions passivityIntentions = new PassivityIntentions(debug, blameIntentions, sourcesMap);
		KindnessIntentions kindnessIntentions = new KindnessIntentions(blameIntentions, debug);
		
		/// ----- SYMPATHY ----- // 
		SympathyIntentions sympathyIntentions = new SympathyIntentions(debug);
		ChildhoodIntentions childhoodIntentions = new ChildhoodIntentions(sympathyIntentions, debug);
		
		// ---- DOUBT ---- //
		DecreaseCredibilityIntentions decreaseCredibilityIntentions = new DecreaseCredibilityIntentions(debug);
		//RoleDoubtIntentions roleDoubtIntentions = new RoleDoubtIntentions(debug);
		ObjectDoubtIntentions objectDoubtIntentions = new ObjectDoubtIntentions(decreaseCredibilityIntentions, sourcesMap, debug);
		
		for( Entity concept : concepts) {
			if(debug) Mark.say(concept);
			
			blameIntentions.processConcept(concept);
			retributionIntentions.processConcept(concept);
			passivityIntentions.processConcept(concept);
			kindnessIntentions.processConcept(concept);
			sympathyIntentions.processConcept(concept);
			childhoodIntentions.processConcept(concept);
			decreaseCredibilityIntentions.processConcept(concept);
			//roleDoubtIntentions.processConcept(concept);
			objectDoubtIntentions.processConcept(concept);
			
			
			/*
			Entity predicate = concept.getElement(0);
			Map<String,Entity> subjectObjectPredicate = RashiHelpers.getSeqSubjectAndObject(predicate);
			Entity conclusion = concept.getElement(1);
			Map<String, Entity> subjectObject = RashiHelpers.getSeqSubjectAndObject(conclusion);
			Mark.say("subject", subjectObject.get("subject"));
			Mark.say("object", subjectObject.get("object"));
			Mark.say("default object", conclusion.getObject());
			Mark.say("default subject", conclusion.getSubject());
			Mark.say(concept.getElements());
			Mark.say(concept.toEnglish());
			*/
		}
		
		//TODO: catch null pointers / no elements
		Mark.say("Generating Conclusions");
		if(blameIntentions.blameFound) {
			blameIntentions.getConclusions();
			retributionIntentions.getConclusions();
			passivityIntentions.getConclusions();
			kindnessIntentions.getConclusions();
		
			String summarizedBlameConclusions = gatherConclusions(blameIntentions, retributionIntentions, passivityIntentions, kindnessIntentions);
			Mark.say("OVERALL CONCLUSIONS");
			Mark.say(summarizedBlameConclusions);
			String title = Html.bold("Modulation of Blame ");
			say(title + summarizedBlameConclusions);
		}
		
		
		if(sympathyIntentions.sympathyFound) {
			sympathyIntentions.getConclusions();
			childhoodIntentions.getConclusions();
			passivityIntentions.getConclusions();
			
			String summarizedSympathyConclusions = gatherConclusions(sympathyIntentions, childhoodIntentions, passivityIntentions);
			Mark.say("SYMPATHY CONCLUSIONS");
			Mark.say(summarizedSympathyConclusions);
			String title = Html.bold("Modulation of Symapthy ");
			say(title + summarizedSympathyConclusions);
			
		}
		
		if(decreaseCredibilityIntentions.decreaseCredFound) {
			decreaseCredibilityIntentions.getConclusions();
			objectDoubtIntentions.getConclusions();
			
			String summarizedDoubtConclusions = gatherConclusions(decreaseCredibilityIntentions, objectDoubtIntentions);
			Mark.say("DOUBT CONCLUSION");
			Mark.say(summarizedDoubtConclusions);
			String title = Html.bold("Modulation of Doubt ");
			say(title + summarizedDoubtConclusions);
		}
		
	}
	
	public String gatherConclusions(BlameIntentions blameIntentions, RetributionIntentions retributionIntentions,
			PassivityIntentions passivityIntentions, KindnessIntentions kindnessIntentions) {
		
		// blame conclusions
		
		String conclusion = "\nOverall, the author ";
		if(blameIntentions.mostBlamedOnlyBlamed) conclusion += "unilaterally blames ";
		else conclusion += "directs the majority of blame at ";
		String mostBlamed = blameIntentions.getMostBlamedEntity().toEnglish();
		conclusion += mostBlamed;
		conclusion += ". \n";
		boolean somethingElse = false;
		
		conclusion = Html.ital(conclusion);
		String newAddition = "";
		
		if(retributionIntentions.retributesOthersThanBlamed || kindnessIntentions.mentionsKindnessOthersThanBlamed) {
			somethingElse = true;
			//conclusion += " Not only did the author blame " + mostBlamed + " most frequently, but also the author casts favor on other agents. The author did so by ";
			newAddition = " Not only did the author blame " + mostBlamed + " most frequently, but also the author casts favor on other agents. The author did so by ";
			
			boolean didFirst = false;
			if(retributionIntentions.retributesOthersThanBlamed) {
				newAddition += "excusing other agents' actions by casting their inequities as retribution, while not doing so for " + mostBlamed +".";
				didFirst = true;
						
			}
			conclusion += newAddition;
			newAddition = "";
			if(didFirst) newAddition = "Similarly, the author casts favor on other agents as compared to " + mostBlamed +  " by ";
			newAddition += "referring to other agents as having done good deeds, while not doing so for " + mostBlamed  + ".";
			
		}
		if(!newAddition.equals("")) conclusion += newAddition;
		
		boolean seenRetribution = false;
		newAddition = "";
		if(retributionIntentions.blamedIsMostRetributed) {
			somethingElse = true;
			seenRetribution = true;
			//conclusion += "\n However, the author mitigates this blame by most often casting " + mostBlamed + "'s actions as retribution, rather than as unwarranted.";
			newAddition = "\n However, the author mitigates this blame by most often casting " + mostBlamed + "'s actions as retribution, rather than as unwarranted.";
			
		}
		else if(retributionIntentions.blamedIsRetributedAtAll){
			somethingElse = true;
			seenRetribution = true;
			newAddition = "\n However, the author somewhat mitigates this blame by providing surrounding context and casting a portion of " + mostBlamed + "'s actions as retribution, although not as often as the author does so for other agents";
		}
		if(!newAddition.equals("")) conclusion += newAddition;
		newAddition = "";
		
		if(passivityIntentions.usedPassiveForEntity(blameIntentions.mostBlamedEntity)) {
			// TODO: IF blame instances are passive multiple times or only time, then mitigates via passive voice. Else, ignore.
			somethingElse = true;
			boolean mitigated = true;
			if(seenRetribution) {
				//conclusion += " Similarly, ";
				newAddition = " Similarly, ";
				}
			else{
				int count = 0; 
				for(Entity i : passivityIntentions.passiveAgents) {if(i.equals(blameIntentions.getMostBlamedEntity())) count +=1;}
				if(blameIntentions.mostBlamedOnlyBlamed && blameIntentions.conclusions.get(blameIntentions.getMostBlamedEntity()) > 1) {
					mitigated = false;
					//conclusion += "\n The author's use of passive voice for " + mostBlamed + " does not substantially mitigate the placement of blame because "; 
					//conclusion += "\n the author unilaterally and repeatedly blames " + mostBlamed;
					newAddition = "The author's use of passive voice for " + mostBlamed + " does not substantially mitigate the placement of blame because "; 
					newAddition += "the author unilaterally and repeatedly blames " + mostBlamed;
				}
				else if (blameIntentions.conclusions.get(blameIntentions.getMostBlamedEntity()) > 1 &&  count > 1) {
					newAddition = "However, ";
					newAddition += "the author mitigates this blame by casting " + mostBlamed + "'s action as passive";
				}
				else {
					newAddition = " Although the author uses passive voice for "+ mostBlamed + ", they do so only once and so it cannot be concluded that ";
					mitigated = false;
					newAddition += "the author mitigates this blame by describing " + mostBlamed + "'s action as passive";
				}
			}
			
			
			if(mitigated) newAddition += "thereby decreasing " + mostBlamed + "'s agency and guilt.";
			else newAddition += ".";
		}
		
		if(!newAddition.equals("")) conclusion += newAddition;
	
		
		if(!somethingElse) conclusion += "The author does so in a straightforward manner, neither providing any mitigating context nor describing other agents' actions relative to " + mostBlamed + ".";
		return conclusion;
		
	}
	
	
	
	public String gatherConclusions(SympathyIntentions sympathyIntentions, ChildhoodIntentions childhoodIntentions, PassivityIntentions passivityIntentions) {
		
		String conclusion = "\n Overall, the author ";
		if(sympathyIntentions.mostVictimizedOnlyVictim) conclusion += "unilaterally evokes sympathy for ";
		else conclusion += "evokes the most sympathy for ";
		String mostSympathy = sympathyIntentions.getMostVictimizedEntity().toEnglish();
		conclusion += mostSympathy;
		conclusion += ".";
		conclusion = Html.ital(conclusion);
		
		conclusion += "\nThe author does so by referring to " + mostSympathy + " as a victim or as a recipient of harm.";
		boolean somethingElse = false;
		
		boolean passivityUsed = passivityIntentions.usedPassiveForObject(sympathyIntentions.mostVictimizedEntity);
				//passivityIntentions.usedPassiveForEntity(sympathyIntentions.mostVictimizedEntity);
		
		if(childhoodIntentions.mostSympathyIsChild || passivityUsed) {
			conclusion += " Not only did the author refer to " + mostSympathy + " as a victim most frequently, but also the author emphasizes this sympathy by ";
			
			if(childhoodIntentions.mostSympathyIsChild) {
				somethingElse = true;
				conclusion += "referring to " + mostSympathy + " as a child.";
				
			}
			if(passivityUsed) {
				if(somethingElse) conclusion += " Similarly, but to a lower degree, the author emphasizes sympathy for " + mostSympathy + " by";
				
				conclusion += " using the passive voice when describing " + mostSympathy + " as receiving harm. By doing so, the author increases attention on " + mostSympathy + "'s victimhood.";
				
				
			}
			
		}
		
		return conclusion;
		
	}
	
	
	
	public String gatherConclusions(DecreaseCredibilityIntentions decreaseCredibilityIntentions, ObjectDoubtIntentions objectDoubtIntentions) {
	
		String mostDecCred = decreaseCredibilityIntentions.getMostDecreasedCredEntity().toEnglish();
		String conclusion = "Overall, the author";
		
		if(decreaseCredibilityIntentions.mostDecreasedCredOnlyDecreasedCred) {
			conclusion += " solely questions the credibility of " + mostDecCred + ". ";
		}
		else conclusion += "questions the credibility of " +  mostDecCred + " the most. ";
		
		conclusion = Html.ital(conclusion);
		conclusion += "The author does so by stating that " + mostDecCred + " made qualified claims, such as by 'alleging', 'suspecting', or 'believing' rather than directly claiming or stating.";
		if(objectDoubtIntentions.objectDoubtList.size() > 0) {
			conclusion += " At the same time, because " + mostDecCred + " qualified its remarks, the author casts doubt on " + mostDecCred + "'s claims as well.";// following through " + mostDecCred + "'s qualifying remarks:";
			conclusion += " More specifically, the author casts doubt on ";
			int count = 0;
			for(Entity doubtfulThing : objectDoubtIntentions.objectDoubtList) {
				
				String ownerOwner = null;
				if(doubtfulThing.getPropertyList()!=null && doubtfulThing.getPropertyList().size() > 0) {
					String label = doubtfulThing.getPropertyList().get(0).getLabel();
					if(label.equals("owner")) {
						Entity owner = (Entity) doubtfulThing.getPropertyList().get(0).getValue();
						if(owner.getPropertyList()!= null && owner.getPropertyList().size()>0) {
							if(owner.getPropertyList().get(0).getLabel().equals("owner")) {
								ownerOwner = ((Entity) owner.getPropertyList().get(0).getValue()).toEnglish();
							}
						}
					}
					
				}
				if(ownerOwner!=null) conclusion += (ownerOwner + "'s ");
				conclusion += doubtfulThing.toEnglish(); 
				if(count == objectDoubtIntentions.objectDoubtList.size()-2) conclusion += " and ";
				if (count < objectDoubtIntentions.objectDoubtList.size() - 2) conclusion += ", ";
				count +=1;
			}
			//conclusion = conclusion.substring(0, e)
			//conclusion += " because " + mostDecCred + "";
			conclusion +=".";
		
			
		}
		return conclusion;
	}
	
	public void processAuthorDecisions(Object authorDecisions){
		/*
		Mark.say("Received author decisions: ", authorDecisions);
		Mark.say(authorDecisions.getClass());

		//if (authorDecisions instanceof BetterSignal) {
			//BetterSignal s = (BetterSignal) authorDecisions;
			Sequence authorStorySequence = (Sequence) authorDecisions;//s.get(0, Sequence.class);
			Mark.say("Author story sequence", authorStorySequence);
			
			Vector<Entity> authorStoryElements = authorStorySequence.getElements();
			Mark.say("Author story elements:");
			for(Entity ent : authorStoryElements) {
				Mark.say(ent);
			//}
			Mark.say("finished printing author story elements in summarize intentions");
			
		
		}
		*/
}
}
