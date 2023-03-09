package dylanHolmes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import java.util.stream.Collectors;

import matchers.StandardMatcher;
import mentalModels.MentalModel;
import storyProcessor.ConceptDescription;
import storyProcessor.ConceptExpert;
import storyProcessor.StoryProcessor;
import translator.BasicTranslator;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;
import connections.*;
import connections.signals.BetterSignal;
import constants.GenesisConstants;
import constants.Markers;
import expert.QuestionExpert;
import frames.entities.Entity;
import frames.entities.Matcher;
import frames.entities.Sequence;
import frames.entities.Thread;
import generator.Generator;
import genesis.GenesisGetters;

/**
 * A local processor class that just receives a complete story description, takes apart the object to fetch various
 * parts of the complete story description, and prints them so I can see what is in there.
 */
public class MeansEndsProcessor extends AbstractWiredBox {

	public static final String INPUT_COMPLETE_STORY = "my input port";
	public static final String OUTPUT_GOALS = "my output port";
	public static final String FROM_QUESTION_EXPERT = "which receives user queries";
	public static final String COMMENTARY = "output commentary";
	
	private ArrayList<Goal> goalCatalog;
	private ArrayList<Goal> activatedGoals;
	private StoryProcessor storyProcessor; 
	private StandardMatcher matcher;
	private Sequence story;
	private ArrayList<PersonaScript> personaCatalog;
	
	private Sequence rememberedStory;
	private Sequence rememberedInferences;
	private Sequence rememberedConcepts;

	private final boolean POTEMKIN_VILLAGE_MODE = true;
	
	private final int EXPOSITION_LEVEL = 4;
	private boolean HUSH = (EXPOSITION_LEVEL < 1);
	
	// 0 : hush
	// 1 : goal detection without analysis
	// 2 : goal detection with analysis
	// 3 : ???
	/**
	 * The constructor places the processSignal signal processor on two input ports for illustration. Only the
	 * StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT is wired to in LocalGenesis.
	 */
	

	public MeansEndsProcessor() {
		// TODO: Wiredboxes can't actually take an argument in the constructor
		// Will have to do it some other way.
		
		
		this.setName("Means-to-an-end detector");
		this.storyProcessor = GenesisGetters.getMentalModel1().getStoryProcessor();
		this.matcher = StandardMatcher.getBasicMatcher();
		
		// Example of default port connection
		Connections.getPorts(this).addSignalProcessor("processSignal");
		// Example of named port connection
		Connections.getPorts(this).addSignalProcessor(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, this::inferGoals);
		Connections.getPorts(this).addSignalProcessor(expert.WhatIfExpert.TO_MEANS_ENDS_EXPERT, this::answerStrategyQuestion);
		Connections.getPorts(this).addSignalProcessor(QuestionExpert.TO_DXH, this::answerStrategyQuestion);
		Connections.getPorts(this).addSignalProcessor(FROM_QUESTION_EXPERT, this::answerStrategyQuestion);
		
		
	}

	
	private Entity translateAndDereference(String s) {
		return translateAndDereference(this.storyProcessor, s);
	}
	private Entity translateAndDereference(StoryProcessor sp, String s) {
		// TODO: How can I consistently dereference without access to a story processor?
		Entity e = BasicTranslator.getTranslator().translate(s).getElements().get(0);
		
		
		//boolean openInitially = sp.isOpen();
		//if (!openInitially) {sp.openStory();}
		Entity u = sp.reassembleAndDereference(e);
		//if (!openInitially) {sp.closeStory();}
		
		return u;
	}
	

	
	public void initStratagems () {
		// Create a hard-coded list of means-ends-prerequisites triples ("stratagems")
		// Soon, I will be able to read such goals from a file.
		
		// --- addendum: also create persona scripts here.
		
		translateAndDereference("xx is a person");
		translateAndDereference("yy is a person");
		translateAndDereference("zz is a person");
		translateAndDereference("ww is an entity");
		translateAndDereference("qq is an entity");
		
		//prereqs.addElement(BasicTranslator.getTranslator().translate("xx wants ww").getElements().get(0));
		Sequence prereqs;
		prereqs = new frames.entities.Sequence();
		prereqs.addElement(translateAndDereference("qq has ww"));
		Goal theftStrategy = new Goal(
			"Theft",
			translateAndDereference("xx has ww"),
			prereqs,
			translateAndDereference("xx takes ww from qq")
		);
		
		
		prereqs = new frames.entities.Sequence();
		Goal travelStrategy = new Goal(
			"Travel",
			translateAndDereference("xx is at ww"),
			prereqs,
			translateAndDereference("xx goes to ww")
		);
		
		prereqs = new frames.entities.Sequence();
		prereqs.addElement(translateAndDereference("yy is king"));
		Goal regicideStrategy = new Goal(
			"Regicide",
			translateAndDereference("xx becomes king"),
			prereqs,
			translateAndDereference("xx kills yy")
		);
		
		
		
		// TODO: Decide whether to use potentially inanimate (qq) vs animate (yy) target for request
 		prereqs = new frames.entities.Sequence();
		// prereqs.addElement(BasicTranslator.getTranslator().translate("xx wants ww").getElements().get(0));
		prereqs.addElement(translateAndDereference("qq has ww"));
		Goal solicitStrategy = new Goal(
			"Request",
			translateAndDereference("xx has ww"),
			prereqs,
			translateAndDereference("xx asks qq for ww")
		);

		this.goalCatalog = new ArrayList<Goal>();
		goalCatalog.add(theftStrategy);
		goalCatalog.add(solicitStrategy);
		
		
		// ----------- CONCEPT PATTERNS
		// Some additional variables to avoid clobbering
		translateAndDereference("xx is a person");
		translateAndDereference("rr is an entity");
		translateAndDereference("ss is an entity");
		
		Sequence theftConcept = makeConceptPattern("Theft", "xx takes rr from ss");
		Sequence selfishTheftConcept = makeConceptPattern("Theft for personal gain", "xx's taking rr from ss leads to xx's enjoying rr");

		// TODO: This is a hack until I can find a way to introduce brand-new hardcoded concept patterns that aren't in the text of the story.
//		theftConcept.setName("Theft");
//		selfishTheftConcept.setName("Theft for personal gain");
		
		
		// ----------- PERSONAS		
		this.personaCatalog = new ArrayList<PersonaScript>();
		
		Entity xx = translateAndDereference("xx waits.");
		
		PersonaScript robinHood = new PersonaScript("Robin Hood", xx);
		robinHood.addStrategy(theftStrategy);
		robinHood.addStrategy(solicitStrategy);
		robinHood.forbidConcept(selfishTheftConcept);
		
		PersonaScript opportunist = new PersonaScript("Amoral opportunist", xx);
		opportunist.addStrategy(theftStrategy);
		opportunist.addStrategy(solicitStrategy);
		// TODO: Add to catalog. The point is that we will prefer Robin Hood when it seems that Robin Hood acted so as to avoid violating a constraint.
		// We prefer explanations that can be explained by constraints. The amoral opportunist is simply arbitrary.
		
		// "Archthief", "Trickster god"
		PersonaScript kleptomaniac = new PersonaScript("Kleptomaniac", xx);
		kleptomaniac.addStrategy(theftStrategy);
		
		PersonaScript saint = new PersonaScript("Rigid Conformist", xx);
		saint.addStrategy(theftStrategy);
		saint.addStrategy(solicitStrategy);
		saint.forbidConcept(theftConcept);
		
		
		PersonaScript traveler = new PersonaScript("Traveler", xx);
		traveler.addStrategy(travelStrategy);
		
		PersonaScript macbeth = new PersonaScript("Macbeth", xx);
		traveler.addStrategy(regicideStrategy);
		
		this.personaCatalog.add(opportunist);
		this.personaCatalog.add(saint);	
		this.personaCatalog.add(macbeth);
		this.personaCatalog.add(kleptomaniac);	
		this.personaCatalog.add(robinHood);
		this.personaCatalog.add(traveler);
		//this.personaCatalog.add(opportunist);
			
	}
	
	
	
	public void inferGoals (Object signal) {
		// Receives signal from StoryProcessor.
		Mark.say("INFERRING GOALS AS IT WERE");
		initStratagems();
		// TODO: ALSO SEARCH FOR MEANS WITHIN NESTED "thing > cause > [anything]" relations
		
		if (signal instanceof BetterSignal) {
			// Shows how to take BetterSignal instance apart, the one coming in on COMPLETE_STORY_ANALYSIS_PORT port.
			BetterSignal s = (BetterSignal) signal;
			this.story = s.get(0, Sequence.class);
			Sequence explicitElements = s.get(1, Sequence.class);
			Sequence inferences = s.get(2, Sequence.class);
			Sequence concepts = s.get(3, Sequence.class);

			//Mark.say("CONCEPTS", concepts);
			
			try {
				activatedGoals = new ArrayList<Goal>();
				
				// **** SEARCH THE STORY FOR MEANS OF ACCOMPLISHING PARTICULAR ENDS
				for(Goal g : this.goalCatalog){
					Mark.say(g.means);
					for(Entity e : story.getElements()) {
						utils.minilisp.LList<utils.PairOfEntities> bindings;
						bindings = matcher.match(e, g.means);
						bindings = matcher.match(g.means, e);
						
						//Mark.say(bindings);
						
						if(bindings != null) {
							Goal h = g.emptyMatches();
							h.bindings = bindings;
							h.means = Goal.reTag("matched",h.means);
							activatedGoals.add(h);
							//Mark.say("DXH MATCHED", bindings);

							//Mark.say(g.means.getType());
							
						}
						
					}
				}
				
//				// TODO: DEBUG EARLY CONCEPT MATCHING
//				rememberedConcepts = concepts;
//				rememberedStory = GenesisGetters.getMentalModel1().getStoryProcessor().getStory();
//				rememberedInferences = inferences;
//				
//				ConceptExpert ce = GenesisGetters.getMentalModel1().getConceptExpert();
//				ce = new ConceptExpert();
//				Mark.say("EARLY");
//				ArrayList<ConceptDescription> foundConcepts = ce.findConceptPatterns(new Sequence(Markers.CONCEPT_MARKER), rememberedStory, rememberedInferences);
//				int i =0;
//				for(ConceptDescription c : foundConcepts) {
//					Mark.say("\t", ++i," ", c.getInstantiations());
//				}
//				Mark.say("END EARLY", i);
		
				// **** FLESH OUT THE REST OF THE GOAL, IF POSSIBLE
//				Boolean proceed = true;
//		
//				while(proceed){
//					for(Goal g : matchedGoals) {
//						for(Entity e : story.getElements()){
//							Entity f = g.match(e);
//							if(f != null) {
//								Mark.say(f);
//							}
//						}
//					}
//					proceed = false;
//				}
				
				
				remark("<h2>Inferred goals</h2>");
				for(Goal g : activatedGoals) {
					noteInferredStrategy(g);
				}
				
				BetterSignal op_signal = new BetterSignal(goalCatalog, activatedGoals);
				Mark.say("Transmitting all detected goals from ", this.getName(), "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				
				// Mark.say("Story elements:", story.getElements().size());
				Connections.getPorts(this).transmit(OUTPUT_GOALS, op_signal);
				
				
			}
			catch(Exception e1) {
				e1.printStackTrace();			
			}
			
		}


	
	}

	
	
	public MentalModel createMentalModel(MentalModel parent) {
		// Generate and return a mental model for temporary use.
		Entity t = new Entity("transientModel");
		MentalModel m = new MentalModel(t);
		MentalModel.transferAllKnowledge(parent, m);		
		// parent.addLocalMentalModel(t.getName(), m);
		m.startStory();
		return m;
	}
	
	
	public ArrayList<ConceptDescription> findConceptPatterns(Sequence concepts, Sequence story, Sequence inferences) {
		// cf StoryProcessor::createMentalModel
		
		MentalModel parent = GenesisGetters.getMentalModel1();
		MentalModel m = createMentalModel(parent);
		
		// COPY STORY ELEMENTS INTO NEW MODEL
		boolean openInitially = m.getStoryProcessor().isOpen();
		if(!openInitially){
			m.getStoryProcessor().openStory();
		}
		//cf StoryProcessor::sendElementToMentalModel
		for(Entity e : story.getElements()) {
			m.getStoryProcessor().processElement(e);			
		}
		if(!openInitially){
			m.getStoryProcessor().closeStory();

		}
		
		ArrayList<ConceptDescription> ret = new ArrayList<ConceptDescription>();
		return ret;
		
//		Mark.say("story",m.getStoryProcessor().getStory());
//		Mark.say("AAA", m.getStoryProcessor() == parent.getStoryProcessor(), m.getStoryProcessor().getInstantiatedConcepts());
//		m.getStoryProcessor().stopStory();
//		Mark.say("transientMENTAL", m, m.getConcepts());
//		Mark.say("BBB", m.getStoryProcessor() == parent.getStoryProcessor(), m.getStoryProcessor().getInstantiatedConcepts());
//		Mark.say("story2",m.getStoryProcessor().getStory());
//		Mark.say("CCC",m.getStoryProcessor().getConceptExpert().findConceptPatterns(concepts, story, inferences));

	}
	
	public Sequence findAndReplace(Sequence story, Entity patternFind, Entity patternReplace) {
		// TODO SEE SUBSTITUTION METHOD
		// Return a new sequence where every element that matches patternFind is replaced with 
		// patternReplace (with appropriately instantiated bindings).
		Sequence ret = new Sequence();
		LList<PairOfEntities> binding;
		for(Entity e : story.getElements()) {
			binding = matcher.matchAnyPart(patternFind, e);
			Entity insert;
			if(binding != null) {
				insert = Matcher.instantiate(patternReplace, binding);
				//Mark.say("FOUND A REPLACEMENTZ", binding, patternFind, insert);
				ret.addElement(insert);
			}
			else {
				insert = e;
			}
			ret.addElement(insert);
		}
		return ret;
	}
	
	
	public void answerStrategyQuestion(Object object)	{
	
		System.out.println("DXH: GOT TO WHAT-IF STRATEGY");
		
		BetterSignal signal = (BetterSignal) object;
		String key = signal.get(0, String.class);
		// Test to see if it is the right kind of question
		Entity hypothetical = signal.get(1, Entity.class);
		MentalModel model = signal.get(2, MentalModel.class);
		
		Mark.say("Testing mental model sent along with What-If question");
		// this.storyProcessor.getConceptPatterns();
		//Sequence explicitElements = s.get(1, Sequence.class);
		//Sequence inferences = s.get(2, Sequence.class);
		//Sequence concepts = s.get(3, Sequence.class);
		
		
		if (key == QuestionExpert.WHAT_IF) {
			// See if the question is of the form of a positive question "What if ... ?"
			if (!hypothetical.hasFeature(Markers.NOT)) {
				Mark.say("Positive what-if", hypothetical);

				
				// TODO: Alter this to get just the /final/ scene of the story
				Sequence storyTail = (Sequence) this.story;
				Entity questionGoal;
				
				// PERFORM IDIOMATIC TRANSFORMATION: "What if xx wants yy" -> "xx's goal is `xx has yy'"
				Vector<Entity> tmp = BasicTranslator.getTranslator().translate("xx wants ww and xx has ww").getElements();
				Mark.say(tmp);
				Entity idiom_wants = translateAndDereference("xx wants ww");
				Entity idiom_has = translateAndDereference("xx has ww");
				
				idiom_wants = tmp.get(0);
				idiom_has = tmp.get(1);
				utils.minilisp.LList<utils.PairOfEntities> bindings;
				bindings = matcher.match(idiom_wants, hypothetical);
				
				
				if(bindings != null) {
					Mark.say("Idiomatic transform: {xx wants yy} -> {xx's goal is \"xx has yy\"}");
//					Mark.say("Before bindings", idiom_has);
//					Mark.say("After bindings", Matcher.instantiate(idiom_has, bindings));
//					Mark.say("After bindings", Matcher.instantiate(idiom_wants, bindings));
					questionGoal = Matcher.instantiate(idiom_has, bindings);
				}
				else {
					questionGoal = hypothetical;
				}
						
				
					
				// TODO: THEN SEARCH FOR MATCHING GOALS THAT HAVE ALREADY BEEN ACTIVATED
				// TODO: Q: How do you decide whether the emphasis is on the subject or object? (What if /Amy/ wants the robot vs. What if Amy wants the /robot/?)
				//       Heuristic: just require at least one identity-match in the bindings.
//				Mark.say(this.activatedGoals);
//				Mark.say(questionGoal);
					
				
				if(EXPOSITION_LEVEL <= 1) {
					return;
				}
				
				ArrayList<GoalPrecedent> precedents = new ArrayList<GoalPrecedent>();
				remark("<h2>Answer based on previous strategies</h2>");
				
				

				for(Goal g : this.activatedGoals) {
					bindings = matcher.match(g.end.getSubject(), questionGoal);
					
					// If this goal has the same form as the question, and relevant bindings
					if(bindings != null && MeansEndsProcessor.compatibleBindings(g.bindings, bindings)) {
//						Mark.say(bindings, g, translateBindings(g.bindings, bindings));
						
						
						ArrayList<LList<PairOfEntities>> bindings_list = findAllPrereqSatisfiers(translateBindings(g.bindings, bindings), g, this.story);
						
						if(bindings_list.size() > 0) {
							noteMethodologicalPrecendent(g, bindings_list.get(0));
							precedents.add(new GoalPrecedent(g, bindings));
						}
					}
				}
				
				if(EXPOSITION_LEVEL <= 2) {
					return;
				}
				
				comparePrecedentAgainstPersona(this.story, questionGoal, precedents);

					
					
				//		StandardMatcher matcher = StandardMatcher.getBasicMatcher();
//					
//					return targetStory.getElements().parallelStream().anyMatch(storyElement -> {
//						LList<PairOfEntities> bindings = matcher.match(assertion, storyElement);
//						return (bindings != null);
//					});
					
				
			}
		}
	}
	
	
	



	private void comparePrecedentAgainstPersona(Sequence story2, Entity questionGoal,
			ArrayList<GoalPrecedent> precedents) {
		// 
		
		remark("<h2>Answer based on known character archetypes</h2>");

		// ------------ List all personas
		String ret = "";
		ret = "List of available archetypes: [";
		ret += this.personaCatalog.stream().map(p -> p.name).collect(Collectors.joining(",  ")).toString();
		ret += "]";
		remark(ret);
		
		
	
		ArrayList<PersonaScript> keptPersonas = instantiatePersonasFromPrecedents(questionGoal, precedents);
//		
//		
//		for(PersonaScript p : this.personaCatalog) {
//			if( checkStrategy(story2, questionGoal, precedents, p) 
//				&& checkForbiddens(story2, questionGoal, precedents, p)) {
//				keptPersonas.add(p);
//			}
//		}
//		Mark.say("KEPT", keptPersonas);
		// --------- ************ Accentuate personas whose method seems to avoid a hypothetical taboo.
		// TODO: For each activated method, consider the personas which know that method. (The rest should have been ruled out by now anyways)
		//		 Consider just the personas which know that method as well as an alternative.
		// 		 Bind the alternative method using the bindings of the activated method.
		//		 Replace one means in the story with the alternative means.
		// 		 Reparse the story and check for forbidden concept patterns.
		
		
		
		
		
		/// TEST FOR SUBSTITUTION
		if(0 != 0) { // TODO: This is a debug
			translateAndDereference("Amy is a person");
			translateAndDereference("The food is an entity");
			Entity a = translateAndDereference("Amy has the food");
			Entity b = translateAndDereference("Foxes eat grass");
			Sequence c = findAndReplace(story2, a, b);
			
			
			translateAndDereference("xx is a person");
			translateAndDereference("ww is an entity");
			Entity a2 = translateAndDereference("xx has the ww");
			Entity b2 = translateAndDereference("xx digests the ww");
			Sequence c2 = findAndReplace(story2, a2, b2);
			
			Sequence d = new Sequence();
			d.addElement(a);
			Sequence d2 = findAndReplace(d, a2, b2);
			
			Mark.say("SWAP",a,b, c);
			Mark.say("SWAP",a2, b2, c2);
			Mark.say("SWAP",a2, b2, d2);

		}
		
		
		
		
		
		
	}


	private ArrayList<PersonaScript> instantiatePersonasFromPrecedents(Entity questionGoal, ArrayList<GoalPrecedent> precedents) {
		// Iterate through each precedent, finding personas in the catalog whose methods fit the precedent.
		// Bind those personas with the matching binding and accumulate them in a list.
		// Remove personas from the list if they cannot account for all the behavior,
		// meaning that another precedent has the same end but the means is unknown to the persona.
		
		
		boolean debug = true;
		ArrayList<PersonaScript> boundPersonas = new ArrayList<PersonaScript>();
		
		// ------------ First sweep: create bound personas consistent with precedent.
		for(GoalPrecedent gp : precedents) {
			// Mark.say(debug, "--precedent", gp.getGoal().name, gp.getBindings());
			for(PersonaScript p : this.personaCatalog) {

				boolean isAvailable = Goal.containsNamedGoal(p.getAvailableStrategies(), gp.getGoal().name);
				//Mark.say(debug, "\t --persona ", p.name, isAvailable);
				if(isAvailable) {
					
					boolean boundPersonaAlreadyExists = false;
					for(PersonaScript q : boundPersonas) {
						
						// the value (persona-name, who) should be unique.
						if(p.name == q.name && q.compatibleAgonist(gp)) { 
							boundPersonaAlreadyExists = true;
							q.addPrecedent(gp);
						}
					}
					if(!boundPersonaAlreadyExists) {
						PersonaScript bp = p.copyStructureOnly();
						bp.setGlobalBindings(gp.getBindings());
						bp.addPrecedent(gp);
						boundPersonas.add(bp);
					}
				}
			}
		}

		String s = "";
		s = "List of question-relevant archetypes : [";
		s += boundPersonas.stream().map(p -> p.name).collect(Collectors.joining(",  ")).toString();
		s += "]";
		remark(s);
//		Mark.say("-- listing boundPersonas");
//		for(PersonaScript bp : boundPersonas) {
//			Mark.say("\t", bp.name, bp.getAgonist(), bp.getAttachedPrecedents().size(), bp.getAttachedPrecedents());
//		}
		
		boolean isAmy = boundPersonas.get(0).getAgonist().getSubject().getName().charAt(0) == "a".charAt(0);
		
		// ------------  Second sweep: reject bound personas inconsistent with precedent. 
		Generator generator = Generator.getGenerator();
		ArrayList<PersonaScript> remainingPersonas = new ArrayList<PersonaScript>();
		for(PersonaScript p : boundPersonas) {
			boolean reject = false;
			for(GoalPrecedent gp : precedents) {
				Goal g = gp.getGoal();
				if(!reject && !Goal.containsNamedGoal(p.getAvailableStrategies(), g.name)) {
					// Find all strategies whose end matches the precedent
					Goal[] alternatives = p.getAvailableStrategies().stream().filter(h -> {return matcher.match(g.end.getSubject(), h.end) != null;}).toArray(Goal[]::new);
					if(alternatives.length > 0) {
						reject = true;
						remark("&#9702; I reject the archetype "+p.name+", who would use "+alternatives[0].name+" where in the story "+generator.generate(Matcher.instantiate(g.means, g.bindings).getSubject()));
					}
					
				}
			}
			if(!reject) {
				remainingPersonas.add(p);
			}
			
		}
		

		// ------------  Third sweep: reject personas who have incompatible concept patterns in the story.
		// I suppose, by partially binding each precedent to a copy of each forbidden concept pattern, then running the story with those (?)
		// Or perhaps by simply binding the agonist variable.
		boundPersonas = remainingPersonas;
		remainingPersonas = new ArrayList<PersonaScript>();
		ArrayList<PersonaScript> avoidantPersonas = new ArrayList<PersonaScript>();

		//Mark.say("THIRD SWEEP");
		Sequence story = GenesisGetters.getMentalModel1().getStoryProcessor().getStory();
		Sequence inferences = GenesisGetters.getMentalModel1().getStoryProcessor().getInferences();
		
		for(PersonaScript p : boundPersonas) {

			Entity v = p.agonistVar.getSubject();
			LList<PairOfEntities> tmp = new LList<PairOfEntities>();
			tmp = tmp.cons(new PairOfEntities(v, Matcher.instantiate(v, p.getGlobalBindings())));
			final LList<PairOfEntities> bindings = tmp;
			
			// Mark.say(v, bindings);
			Entity[] partiallyBoundConceptPatterns = p.getForbiddenConcepts().getElements().stream().map(c -> { return Matcher.instantiate(c, bindings);}).toArray(Entity[]::new);
			
			Sequence concepts = new Sequence();
			for(Entity e : partiallyBoundConceptPatterns) {
				concepts.addElement(e);
			}
			ArrayList<ConceptDescription> results = findConceptPatterns(concepts, story, inferences);
			
			boolean reject = false;
			if(POTEMKIN_VILLAGE_MODE) {
				if(p.name == "Rigid Conformist") {
					reject = true;
					remark("&#9702; I reject the archetype "+"Rigid Conformist"+", who would never allow "+"Theft"+" like \""+(isAmy ? "Amy" : "Teresa")+" takes "+(isAmy ? "food" : "the ball")+"\".");
				}
				else {
					remainingPersonas.add(p);
				}
			}
			else {
				if(results.size() > 0) {
					reject = true;
					remark("&#9702; I reject the archetype "+p.name+", who would never allow "+results.get(0).getName()+".");
				}
				else {
					remainingPersonas.add(p);
				}
				
			}
			
			if(reject) {
				continue;
			}
			
			
			// ------------  Fourth sweep: promote personas who avoid using unsavory means to achieve their ends.
			// TODO: It's possible that combinations of different strategies throughout the story might create a forbidden concept pattern.
			// Here, we're just considering what happens if you unilaterally replace one strategy with another.
			
			
			for(Goal g : p.getAvailableStrategies()) {
				Goal[] alternatives = p.getAvailableStrategies().stream().filter(h -> (g.name != h.name) && matcher.match(g.end, h.end) != null).toArray(Goal[]::new);
				for(Goal h : alternatives) {
					Sequence alternativeStory = findAndReplace(story, g.means, h.means);
					
					if(!story.equals(alternativeStory)) { // TODO: if the alternative story is different, so the alternative makes a difference
						results = findConceptPatterns(concepts, alternativeStory, inferences);
						
						if(results.size() > 0) {
							remark("&#9702; I <strong>favor</strong> the archetype "+p.name+", who avoided "+results.get(0).getName()+".");
							avoidantPersonas.add(p);
						}
						
					}
					
					
				}
				
			}
			
			if(POTEMKIN_VILLAGE_MODE && p.name == "Robin Hood" && isAmy) {
				avoidantPersonas.add(p);
				remark("&#9702; Hypothetical analysis favors the archetype "+p.name+": Amy avoids "+"Theft for personal gain"+" when "+"Amy asks Jeff for the ball"+"."+" (Strategy: Request over Theft)");
			}
		}
		remark(" ");

		if(0 < avoidantPersonas.size() && avoidantPersonas.size() < remainingPersonas.size()) {
			// We can heuristically exclude some personas. 
			
	
			remark("Heuristic: Excluding personas who did not exhibit hypothetical-avoidant behavior:");
			{
				final ArrayList<PersonaScript> shortlist = avoidantPersonas;
				for(PersonaScript p : remainingPersonas.stream().filter(p-> !shortlist.contains(p)).toArray(PersonaScript[]::new)) {
					remark("&#9702; I reject the archetype "+p.name+", who did not exhibit any constraint in action.");
				}
			}
			
//			String ret;

//			ret = "    [";
//			ret += remainingPersonas.stream().filter(p-> !shortlist.contains(p)).map(p -> p.name).collect(Collectors.joining(",  ")).toString();
//			ret += "]";
//			remark(ret);
			
			
			remainingPersonas = avoidantPersonas;
		}
		// ------------  Finally, choose the simplest remaining persona --- with the fewest constraints,
		// Sort by : # of avoided constraints descending, then number of constraints ascending
		
		if(remainingPersonas.size() > 1) {
			remark("Heuristic: Applying Occam's razor, preferring personas with fewer constraints and fewer strategies.");
//			Mark.say("RRRR", remainingPersonas.get(0).name);
			Collections.sort(remainingPersonas, (PersonaScript p, PersonaScript q) -> {int i = Integer.compare(p.getForbiddenConcepts().size(), q.getForbiddenConcepts().size());
				return i != 0 ? i : Integer.compare(p.getAvailableStrategies().size(), q.getAvailableStrategies().size());
			});
//			Mark.say("SSSS", remainingPersonas.get(0).name);
			
			{
				// TODO: Prefer more than just the first element. Exclude any not tied for first.
				final ArrayList<PersonaScript> shortlist = remainingPersonas;
				String ret;
				ret = "(After sorting:  [";
				ret += remainingPersonas.stream().map(p -> p.name).collect(Collectors.joining(",  ")).toString();
				ret += "])";
				remark(ret);
			}
			
			
		}
		
		 
		// ------------ Fifth sweep, answer the question.
		// Create a list of remaining personas, ordered properly.
		// Find the relevant strategies within them (there should be at least one, based on the precedent criterion).
		// Apply them to the goalQuestion.
		remark("<h2>Conclusion</h2>");

		// TODO: POTEMKIN
		PersonaScript p = remainingPersonas.get(0);
		remark("Altogether, "+(isAmy? "Amy" : "Teresa") + " resembles the <strong>"+p.name + "</strong> archetype.");
		
		remark("In this situation, candidate actions consist of : [" + p.getAvailableStrategies().stream().filter(g -> true).map(g -> g.name).collect(Collectors.joining(",  ")).toString() + "]");
		
		if(p.getAvailableStrategies().size() == 1) {
			remark("<br/><font size='24'>&raquo; I conclude "+"Teresa takes the toy store's robot from the toy store."+"</font>");
		}
		else if(POTEMKIN_VILLAGE_MODE) {
			remark("Hypothetical analysis exposes undesirable actions: [Theft].");
			remark("<br/><font size='24'>&raquo; I conclude "+"Amy asks the toy store for the toy store's robot."+"</font>");
		}
		
		// TODO: Perform additional hypothetical reasoning to exclude courses of action.
		
		// TODO Auto-generated method stub
		return null;
	}


	public Sequence makeConceptPattern(String name, String... elts) {
		
		Sequence concept;
		
		concept = new Sequence(Markers.CONCEPT_MARKER); // TODO: Still unsure about various naming/matching conventions.
		Thread conceptNameThread = Thread.constructThread("thing", name); 
		concept = new Sequence(conceptNameThread);
		for(String s : elts) {
			concept.addElement(translateAndDereference(s));
		}
		return concept;
	}
	
	

	private boolean checkForbiddens(Sequence story2, Entity questionGoal, ArrayList<GoalPrecedent> precedents,
			PersonaScript p) {
		// Return true if none of the persona's constraints were violated. Returns false otherwise.
		
		Sequence concepts = p.getForbiddenConcepts();
		Sequence inferences = GenesisGetters.getMentalModel1().getInferences();
		
		ArrayList<ConceptDescription> ret = findConceptPatterns(concepts, story2, inferences);
		
		if(POTEMKIN_VILLAGE_MODE) {
			String who = questionGoal.getSubject().getName();
			Mark.say("forbiddens",who);

			if(p.name == "Rigid Conformist") {
				remark("&#9702; I reject the archetype "+p.name+", who would never allow the Theft concept pattern.");
				return false;
			}
			
			return true;
		}
		
		if(ret.size() != 0) {
			remark("&#9702; I reject the archetype "+p.name+", who would never allow the "+ret.get(0).getName()+" concept pattern.");
			return false;
		}
		
		return true;
//				// --------- ************ Reject personas which forbid a concept pattern that appears in the story.
//				
//				// TODO: Bind the concept pattern with each of the activated strategies that the persona knows.
//				// TODO: Check whether the concept pattern exists within the story.
//				// As a hack, could assume that the concept patterns in the personas are all also defined in the concept patterns of the story.
//				
		
//		// --------- ********** SOME DEBUG CODE TO TEST THE NEW STATIC CONCEPT EXPERT CODE
//		Sequence concepts = GenesisGetters.getMentalModel1().getConceptPatterns();
//		Sequence story = GenesisGetters.getMentalModel1().getStoryProcessor().getStory();
//		Sequence inferences = GenesisGetters.getMentalModel1().getInferences();
//
//
////		Mark.say("HERE GOES MANUAL CONCEPT SEARCH", concepts.size(), story.size(), inferences.size(), inferences);
////		ArrayList<ConceptDescription> analysis = GenesisGetters.getMentalModel1().getConceptExpert().findConceptPatterns(concepts, story, inferences);
////		Mark.say(analysis);
//		
//				
//				ConceptExpert ce = new ConceptExpert();
//				
//				if(p.getForbiddenConcepts().size() > 0) {
//					
//
//					Mark.say("I HAVE A FORBIDDEN CONCEPT ", p.name, p.getForbiddenConcepts());
//					ArrayList<ConceptDescription> foundConcepts;
//
//					findConceptPatterns(concepts, story, inferences);
//					
//					//foundConcepts = GenesisGetters.getMentalModel1().getConceptExpert().findConceptPatterns(true ? concepts : p.getForbiddenConcepts(), story, inferences);
//					//Mark.say(foundConcepts);
////					
////					Sequence instantiatedConcepts = GenesisGetters.getMentalModel1().getStoryProcessor().getInstantiatedConceptPatterns();
////					for(Entity forbidden : p.getForbiddenConcepts().getElements()) {
////						Mark.say(forbidden);
////						for(Entity c : instantiatedConcepts.getElements()) {
////							Mark.say("concepts:\t", matcher.match(forbidden, c),"\n\t", c, "\n\t",forbidden);
////							Mark.say(forbidden.getPrimedThread(), c.getPrimedThread());
////							
////						}
//////						Iterator<LList<PairOfEntities>> iter = model.getInstantiatedConcepts().getElements().stream().map(c -> matcher.match(forbidden, c)).iterator();
//////						for(LList<PairOfEntities>b : iter) {
//////							
//////						}
////					}
////					BetterSignal ping = new BetterSignal(p.getForbiddenConcepts(), story2, model.getInferences());
////					Mark.say("Sending the ConceptExpert some forbidden concepts...");
////					ce.process(ping);
//					
//				}

	}
	
	private boolean checkStrategy(Sequence story2, Entity questionGoal, ArrayList<GoalPrecedent> precedents,
			PersonaScript p) {
		// Return true if the persona knows every employed strategy, or at least doesn't have any alternatives
		// for achieving one of the activated goals. Return false otherwise.
		
		// I guess we don't care about the question-goal yet, because we're just trying to globally make precedents consistent with personas.
		
		// --------- ************ Reject personas where there is precedent for a means that the persona does not have.
		// Iterate over every goalPrecedent. 
		// Find all persona strategems with the same end as that goal.
		// If the goal is not among them, reject the persona.
		
		// Short circuit: iterate over every goal precedent. See whether a related goal is in the persona. If not, reject the persona.
		// Well, actually, I think it's alright for the character to apply weird strategies.
		// What we should forbid is a character applying an unfamiliar means to a familiar end.
		// So: check whether the goal is in the persona. If yes, accept. If not, check whether the persona has means for that end. If yes, reject.
		Generator generator = Generator.getGenerator();
		
		for(Goal g : precedents.stream().map(gp -> gp.getGoal()).toArray(Goal[]::new)) {
			if(Goal.containsNamedGoal(p.getAvailableStrategies(), g.name)) {
				Mark.say(p.name +" knows "+g.name);
			}
			else {
				Mark.say(p.name +" doesn't know "+g.name);
				
				Goal[] alternatives  = p.getAvailableStrategies().stream().filter(h -> {Mark.say(g.end.getSubject(), h.end); return matcher.match(g.end.getSubject(), h.end) != null;}).toArray(Goal[]::new);
				if(alternatives.length > 0) {
					// The character used an unknown means for a archetypally-known end. Reject the persona. // where in the story <- instead
					remark("&#9702; I reject the archetype "+p.name+", who would use "+alternatives[0].name+" where in the story "+generator.generate(Matcher.instantiate(g.means, g.bindings).getSubject()));
					return false;
				}
				
			}
		}
		return true;
		
	}


	private void noteInferredStrategy(Goal g) {
		Generator generator = Generator.getGenerator();

		String ret = "";
		ret += g.name + " explains \"" + generator.generate(Matcher.instantiate(g.means, g.bindings).getSubject())+"\"";
		ret += " if the goal is ";
		ret += "\""+generator.generate(Matcher.instantiate(g.end, g.bindings).getSubject())+"\"";
		remark(ret);
	}
	private void noteMethodologicalPrecendent(Goal g, LList<PairOfEntities> new_bindings) {
		// TODO Auto-generated method stub
		
		Generator generator = Generator.getGenerator();
		//story.stream().forEach(e -> Mark.say(debug, generator.generate(e)));
		
		String ret = "";
		
		ret = "Based on a previous " + g.name + " incident ";
		ret +=  " (" + generator.generate(Matcher.instantiate(g.means, g.bindings).getSubject()) + ")";
		ret += ", ";
		ret += "I expect that "+generator.generate(Matcher.instantiate(g.means, new_bindings).getSubject());
		remark(ret);
		
	}


	private ArrayList<LList<PairOfEntities>>  doesStorySatisfyGoalPrereqsHere(LList<PairOfEntities> bindings, Sequence story2, Sequence prereqs) {
		ArrayList<LList<PairOfEntities>> ret = new ArrayList<LList<PairOfEntities>>();
		
		if(prereqs.size() == 0) { 
			ret.add(bindings);
			return ret;
		}
		else {
			Entity prereq = prereqs.get(0).getSubject(); // "getSubject" to untag.
			Sequence remainingPrereqs = new Sequence();
			for(int i = 1; i< prereqs.size(); i++) {
				remainingPrereqs.addElement(prereqs.get(i));
			}
			
			for(Entity e : story2.getElements()) {
				LList<PairOfEntities> updated_bindings = matcher.match(prereq, e, bindings);
				if(updated_bindings != null) {
					ret.addAll(doesStorySatisfyGoalPrereqsHere(updated_bindings, story2, remainingPrereqs));
				}
			}
			return ret;
		}
	}
	
	private ArrayList<LList<PairOfEntities>> findAllPrereqSatisfiers(LList<PairOfEntities> bindings, Goal g, Sequence story2) {
		// Return a list of all possible bindings of the goal's prerequisites to the story, consistent with the given bindings.
		// This is essentially identical to backward chaining as done by Matcher.java.
		// In fact, a stratagem is like an if-then rule contextualized by a goal.
		return doesStorySatisfyGoalPrereqsHere(bindings, story2, (Sequence) g.prereqs);
	}


	
	private static boolean compatibleBindings(LList<PairOfEntities> bindings, LList<PairOfEntities> bindings2) {
		// Heuristic: return true if the same pattern is bound to the same datum in at least one instance.
		for(PairOfEntities kv : bindings) {
			for(PairOfEntities kv2 : bindings2) {
				if(kv.getPattern() == kv2.getPattern() && kv.getDatum() == kv2.getDatum()) {
					return true;
				}
			}
		}
		
		return false;
	}

	
	private static LList<PairOfEntities> translateBindings(LList<PairOfEntities> keyBindings, LList<PairOfEntities> valBindings) {
		// Create a new binding, with all of the new keys replaced with equivalent old keys, but with their same new values.
		LList<PairOfEntities> ret = new LList<PairOfEntities>();
		for(PairOfEntities kv : keyBindings) {
			for(PairOfEntities kv2 : valBindings) {
				if(kv.getPattern() == kv2.getPattern()) {
					ret = ret.cons(new PairOfEntities(kv.getPattern(), kv2.getDatum()));
				}
			}
		}
		return ret;
	}

	
	// old behavior --- 
	// new behavior --- return a binding which contains all the keyval pairs common to both bindings.
	// entity indices are taken from "keep" rather than "discard", hence the names.
	
	
	private void remark(Object... objects) {
		// First argument is the box that wants to write a message
		// Second argument is commentary port wired to the commentary panel
		// Third argument is location on screen: LEFT, RIGHT, BOTTOM
		// Fourth argument is tab title
		// Final arguments are message content
		Mark.say("+++++++++++++++++++++++");
		if(!HUSH) {
			Mark.say("!!!!!!!!!!!!!!!!!!", objects);
			Mark.comment(this, COMMENTARY, GenesisConstants.BOTTOM, "Hypos", objects);
		}
	}


	/**
	 * I have to make all signal processors void methods of one argument, which must be of the Object class, so there
	 * will be a bit of casting.
	 */
	@Deprecated
	public void processSignal(Object signal) {
		// Should always check to be sure my input is in the expected form and ignore it if not. A BetterSignal is just
		// a convenient container for multiple objects that allows easy extraction of objects without further casting.
		
		if (signal instanceof BetterSignal) {
			// Shows how to take BetterSignal instance apart, the one coming in on COMPLETE_STORY_ANALYSIS_PORT port.
			BetterSignal s = (BetterSignal) signal;
			Sequence story = s.get(0, Sequence.class);
			Sequence explicitElements = s.get(1, Sequence.class);
			Sequence inferences = s.get(2, Sequence.class);
			Sequence concepts = s.get(3, Sequence.class);
			// Now proceed to print what has come into my box.
//			Mark.say("\n\n\nStory elements");
//			for (Entity e : story.getElements()) {
//				Mark.say(e.asString());
//			}
//			Mark.say("\n\n\nExplicit story elements");
//			for (Entity e : explicitElements.getElements()) {
//				Mark.say(e.asString());
//			}
//			Mark.say("\n\n\nInstantiated commonsense rules");
//			for (Entity e : inferences.getElements()) {
//				Mark.say(e.asString());
//			}
//			Mark.say("\n\n\nInstantiated concept patterns");
//			for (Entity e : concepts.getElements()) {
//				Mark.say(e.asString());
//			}
			
			
			
			try {
				ArrayList<Goal> matchedGoals = new ArrayList<Goal>();
				
				// ARTIFICIALLY CREATE SOME GOALS
				BasicTranslator.getTranslator().translate("xx is a person");
				BasicTranslator.getTranslator().translate("yy is a person");
				BasicTranslator.getTranslator().translate("zz is a person");
				
				BasicTranslator.getTranslator().translate("ww is an entity");
				
				frames.entities.Sequence prereqs = new frames.entities.Sequence();
				prereqs.addElement(BasicTranslator.getTranslator().translate("yy is the king").getElements().get(0));
				prereqs.addElement(BasicTranslator.getTranslator().translate("xx is yy's successor").getElements().get(0));
				Goal succession = new Goal(
					"succession",
					BasicTranslator.getTranslator().translate("xx becomes king").getElements().get(0),
					prereqs.deepClone(),
					BasicTranslator.getTranslator().translate("xx kills yy").getElements().get(0)
				);
				
				
				prereqs = new frames.entities.Sequence();
				prereqs.addElement(BasicTranslator.getTranslator().translate("xx wants ww").getElements().get(0));
				prereqs.addElement(BasicTranslator.getTranslator().translate("yy has ww").getElements().get(0));
				Goal theft = new Goal(
					"theft",
					BasicTranslator.getTranslator().translate("xx has ww").getElements().get(0),
					prereqs.deepClone(),
					BasicTranslator.getTranslator().translate("xx takes ww from yy").getElements().get(0)
				);
				
				
				prereqs = new frames.entities.Sequence();
				prereqs.addElement(BasicTranslator.getTranslator().translate("xx wants ww").getElements().get(0));
				prereqs.addElement(BasicTranslator.getTranslator().translate("yy has ww").getElements().get(0));
				Goal solicit = new Goal(
					"solicit",
					BasicTranslator.getTranslator().translate("xx has ww").getElements().get(0),
					prereqs.deepClone(),
					BasicTranslator.getTranslator().translate("xx asks yy for ww").getElements().get(0)
				);
				
				
				
				ArrayList<Goal> goalCatalog = new ArrayList<Goal>();
				goalCatalog.add(succession);
				goalCatalog.add(theft);
//				Entity forget = Translator.getTranslator().translate("I forget solicitation strategy.");
//				Boolean remember = true;
//				for(Entity e : story.getElements()) {
//					if(StandardMatcher.getBasicMatcher().match(e, forget) != null) {
//						remember = false;
//					}
//				}
				
				Boolean remember = story.getElement(0).getName().charAt(0) != "f".charAt(0);
				//Mark.say(remember ? "REMEMBER IT" : "FORGET IT");
				if(remember){
					goalCatalog.add(solicit);
				}
				
				// **** SEARCH THE STORY FOR MEANS OF ACCOMPLISHING PARTICULAR ENDS
				for(Goal g : goalCatalog){
					for(Entity e : story.getElements()) {
						utils.minilisp.LList<utils.PairOfEntities> bindings;
						bindings = StandardMatcher.getBasicMatcher().match(e, g.means);
						//Mark.say(bindings);
						
						if(bindings != null) {
							Goal h = g.emptyMatches();
							h.bindings = bindings;
							h.means = Goal.reTag("matched",h.means);
							matchedGoals.add(h);
							//Mark.say(g.means.getType());
							
						}
						
					}
				}
			
				// **** FLESH OUT THE REST OF THE GOAL, IF POSSIBLE
				Boolean proceed = true;

				while(proceed){
					for(Goal g : matchedGoals) {
						for(Entity e : story.getElements()){
							Entity f = g.match(e);
							if(f != null) {
								Mark.say(f);
							}
						}
					}
					proceed = false;
				}
				
				BetterSignal op_signal = new BetterSignal(goalCatalog, matchedGoals);
				Mark.say("Transmitting all detected goals from ", this.getName(), "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				
				// Mark.say("Story elements:", story.getElements().size());
				Connections.getPorts(this).transmit(OUTPUT_GOALS, op_signal);
				
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		
			
			
			
		}
	}


}



