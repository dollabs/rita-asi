package bryanWilliams.goalAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import bryanWilliams.Util;
import conceptNet.conceptNetModel.ConceptNetAssertion;
import conceptNet.conceptNetModel.ConceptNetFeature;
import conceptNet.conceptNetModel.ConceptNetFeature.FeatureType;
import conceptNet.conceptNetModel.ConceptNetJustification;
import conceptNet.conceptNetModel.ConceptNetQueryJustification;
import conceptNet.conceptNetNetwork.ConceptNetClient;
import conceptNet.conceptNetNetwork.ConceptNetQueryResult;
import conceptNet.conceptNetNetwork.ConceptNetScoredAssertion;
import connections.Connections;
import connections.WiredBox;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Relation.Transitivity;
import matchers.BindingValidator;
import matchers.StandardMatcher;
import matchers.Substitutor;
import storyProcessor.StoryProcessor;
import translator.BasicTranslator;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;
import utils.tools.Predicates;

/**
 * This class analyzes the goals of characters. It's designed to receive the events of the story sequentially,
 * internally propose candidate character goals, and check if later events complete these candidate character goals.
 * 
 * This is the main engine behind the ASPIRE system. For more information, see Chapter 6 of Bryan Williams' M.Eng thesis,
 * available here: http://groups.csail.mit.edu/genesis/papers/2017%20Bryan%20Williams.pdf
 * 
 * @author bryanwilliams
 *
 */
public class AspireEngine implements WiredBox {

    // goals mentioned in the ruleset
    private final Set<Goal> explicitGoals;
    // candidate character goals
    private final Set<CharacterGoal> inProgressGoals;
    // does not use identity matching (can match against rule placeholders)
    private static final StandardMatcher matcher = new StandardMatcher();
    // identity matching useful since in progress goals will have earlier bindings already substituted in
    private static final StandardMatcher identityMatcher = StandardMatcher.getIdentityMatcher();
    private static final BindingValidator bv = new BindingValidator();
    // keeps track of the placeholders mentioned in the goal-related rules
    private final Set<Entity> goalPlaceholders = new HashSet<>();
    // private final static int CHAINING_LIMIT = 2;
    private final static double CONCEPT_NET_SCORE_CUTOFF = 0.3;
    private final boolean debug = false;
        
    public AspireEngine() {
        this.explicitGoals = new HashSet<>();
        this.inProgressGoals = new HashSet<>();
        bv.setPatternMatch(false);
        Connections.getPorts(this).addSignalProcessor(StoryProcessor.STARTING, this::clearState);
        Connections.getPorts(this).addSignalProcessor(StoryProcessor.RULE_PORT, this::makeGoalsFromRules);
    }
    
    @Override
    public String getName() {
        return "goal analyzer";
    }  
    
    /**
     * Clears the internal state of the GoalAnalyzer (e.g. candidate character goals)
     */
    public void clearState(Object o) {
        Mark.say(debug, "GoalAnalyzer clearing state");
        inProgressGoals.clear();
    }
    
    // "want" used to identify a goal
    private static boolean isGoalEntity(Entity e) {
        return e.getType().equals("want");
    }
    
    /**
     * @return true iff this rule describes an event that may cause a character to have a goal (the character "wants" something)
     */
    public static boolean isGoalRule(Entity rule) {
        return rule.relationP() && Predicates.isExplictCauseOrLeadsTo(rule) && isGoalEntity(rule.getObject());
    }
    
    // Scans rules and sees which ones discuss goals
    // Keeps track of the goals they mention, and their ccauses
    private void makeGoalsFromRules(Object o) {
        Mark.say(debug, "Making goals from rules");
        Sequence rules = (Sequence) o;
        // have to standardize all wants so they can be used as keys in the map
        Map<Entity, Goal> standardizedWantToGoal = new HashMap<>();
        for (Entity rule : rules.getElements()) {
            Entity subject = rule.getSubject();
            Entity object = rule.getObject();
            boolean isGoalRule = false;
            if (isGoalEntity(subject)) {
                // If XX wants to harm YY, XX is a bully.
                Entity standardizedWant = Util.withProperNamePlaceholders(subject);
                if (!standardizedWantToGoal.containsKey(standardizedWant)) {
                    Goal goal = new Goal(standardizedWant);
                    standardizedWantToGoal.put(standardizedWant, goal);
                }
                isGoalRule = true;
            }
            if (isGoalEntity(object)) {
                // If XX is a bully, XX may want to harm YY.
                Entity standardizedEnt = Util.withProperNamePlaceholders(rule);
                Entity standardizedWant = standardizedEnt.getObject();
                Entity standardizedCause = standardizedEnt.getSubject();
                if (!standardizedWantToGoal.containsKey(standardizedWant)) {
                    Goal goal = new Goal(standardizedWant);
                    standardizedWantToGoal.put(standardizedWant, goal);
                }
                // bit of a hack - only grabbing first cause from possible sequence
                standardizedWantToGoal.get(standardizedWant).addCause(standardizedCause.getElement(0));
                isGoalRule = true;
            }
            if (isGoalRule) {
                // writes through to underlying rule in RuleMemory, both in its ruleMap and ruleSequence
                rule.addProperty(Markers.GOAL_ANALYSIS, true, true);
            }
        }
        
        Set<Goal> goals = new HashSet<>(standardizedWantToGoal.values());
        goalPlaceholders.clear();
        goals.forEach(goal -> goalPlaceholders.addAll(Util.getAllProperNameComponents(goal.getWant())));
        Mark.say(debug, "Parsed goals", this.explicitGoals);
        explicitGoals.clear();
        explicitGoals.addAll(goals);
    }
    
    // discusses an unbound thing, like "someone" or "something"
    private boolean hasIndefiniteSubject(Entity e) {
        return isIndefinite(e.getSubject());
    }
    
    // entity is an unbound thing, like "someone" or "something"
    private boolean isIndefinite(Entity e) {
        return Util.INDEFINITE_PLACEHOLDERS.stream()
                .anyMatch(str -> str.toEnglish().equalsIgnoreCase(e.toEnglish()));
    }
    
    // Sees what candidate character goals an event in the story causes
    private Set<CharacterGoal> getCausedGoals(Entity event) {
        Set<CharacterGoal> causedGoals = new HashSet<>();
        
        if (CharacterGoal.isAcceptableWant(event)) {
            // goal explicitly stated in the story
            causedGoals.add(new CharacterGoal(null, event));
        }
        
        // check if this event matches any of the causes for goals mentioned
        // in the ruleset
        for (Goal g : explicitGoals) {
            for (Entity cause : g.getCauses()) {
                Mark.say(debug, "Checking if", event, "matches", cause);
                LList<PairOfEntities> match = matcher.match(cause, event);
                if (match != null) {
                    Mark.say(debug, "Match!");
                    Entity want = g.getWant();
                    for (PairOfEntities binding : match) {
                        want = Substitutor.substitute(binding.getDatum(), binding.getPattern(), want);
                    }
                    List<Entity> placeholders = Util.getAllProperNameComponents(want).stream()
                            .distinct()
                            .filter(ent -> goalPlaceholders.contains(ent)).collect(Collectors.toList());
                    if (placeholders.size() == 1) {
                        want = Substitutor.substitute(Util.SOMEONE_PLACEHOLDER, placeholders.get(0), want);
                        goalPlaceholders.add(Util.SOMEONE_PLACEHOLDER);
                    }
                    causedGoals.add(new CharacterGoal(event, want));
                }
            }
        }
        
        // check if this event triggers anything in ConceptNet
        causedGoals.addAll(conceptNetCausedGoals(event));
        
        return causedGoals;
    }
    
    // Sees what candidate character goals an event in the story causes using ConceptNet
    private Set<CharacterGoal> conceptNetCausedGoals(Entity event) {
        Set<CharacterGoal> causedGoals = new HashSet<>();
        
        // applying ConceptNet knowledge requires the event
        // to be about a known person's actions - guards against passive voice
        if (hasIndefiniteSubject(event)) {
            return causedGoals;
        }
        
        if (event.relationP()) {
            Relation eventRel = (Relation) event;
            String verb = eventRel.getType();
            
            BiFunction<Entity, ConceptNetJustification, CharacterGoal> toCharacterGoal = 
                    (want, justification) -> new CharacterGoal(eventRel, want, Arrays.asList(justification));
            boolean hasOnlyProperNameObjects = Util.getTransitiveRelationDirectObjects(eventRel).stream()
                    .allMatch(e -> Util.isProperName(e, true));
            // add goals based on cause verb concept by itself (no verb phrase)
            // only for proper name objects! (e.g. will infer "Matt wants to kiss Jackie" from "Matt loves Jackie", but
            // will not infer just "Matt wants to kiss")
            // (love Causes Desire kiss)
            causedGoals.addAll(conceptNetCausedGoals(verb, eventRel.getSubject(), 
                    Util.getTransitiveRelationProperNameDirectObjects(eventRel), hasOnlyProperNameObjects, toCharacterGoal));
            // add goals based on concept verb phrases 
            // since phrase is used, do NOT give additional objects to pair received goals with
            // e.g. will infer ("Matt wants to think" from "Matt reads book", but not "Matt wants to think book")
            // (read book Causes Desire think)
            getConceptVerbPhrases(eventRel, true).stream()
                .forEach(phrase -> causedGoals.addAll(conceptNetCausedGoals(phrase, eventRel.getSubject(),
                        Collections.emptyList(), true, toCharacterGoal)));
        }
        
        causedGoals.stream()
            .map(CharacterGoal::getWantedAction)
            .forEach(wantedAction -> Mark.say(debug, event.toEnglish(), "causes goal", wantedAction));
        return causedGoals;
    }
    
    // helper method for using ConceptNet to identify what goals a story event may cause
    // if objects is empty and produceStandaloneGoalIfEmpty is true, just produces the goal by itself; 
    // otherwise, produces the goal with each element of objects as a direct object and never by itself
    private Set<CharacterGoal> conceptNetCausedGoals(String causeConcept, Entity goalCharacter, List<Entity> objects,
            boolean produceStandaloneGoalIfEmpty,
            BiFunction<Entity, ConceptNetJustification, CharacterGoal> toCharacterGoal) {
        ConceptNetFeature causeGoalFeature = new ConceptNetFeature(causeConcept, "CausesDesire", FeatureType.LEFT);
        ConceptNetQueryResult<List<ConceptNetScoredAssertion>> unflattenedResult = 
                ConceptNetClient.featureToAssertions(causeGoalFeature);
        // flatten the one query result containing a List of ConceptNetScoredAssertions into a list 
        // of query results, each containing a score for a single ConceptNetAssertion.
        // this way, we can turn each individual query result into its own justification
        List<ConceptNetQueryResult<Double>> flattenedResults = ConceptNetQueryResult.flattenResult(unflattenedResult);
        Set<ConceptNetQueryResult<Double>> goalResults = flattenedResults.stream()
                .filter(result -> result.getResult() >= CONCEPT_NET_SCORE_CUTOFF)
                .collect(Collectors.toSet());
        
        Set<CharacterGoal> goals = new HashSet<>();
        for (ConceptNetQueryResult<Double> result : goalResults) {
            String goalConcept = result.getQuery().getComponentConcepts().get(1).getConceptString();
            List<Optional<Entity>> optionalObjects = objects.stream()
                    .map(Optional::of)
                    .collect(Collectors.toList());
            // if no objects given, form goal with no object
            if (optionalObjects.isEmpty() && produceStandaloneGoalIfEmpty) {
                optionalObjects.add(Optional.empty());
            }
            for (Optional<Entity> optObj : optionalObjects) {
                // try to use START to turn this goal concept, goal character, and optional object into an actual entity
                // use START to do this because constructing the entity by hand is unreliable (START will automatically
                // identify what's an adverb, what's an object, etc.)
                Optional<Entity> optWant = attemptToGenerateWant(goalConcept, goalCharacter, optObj);
                if (optWant.isPresent()) {
                    goals.add(toCharacterGoal.apply(optWant.get(), new ConceptNetQueryJustification<Double>(result)));
                }
            }
        }
        return goals;
    }
    
    // uses START to turn a goal concept, a goal character, and an optional object into an actual entity
    // may not generate into English well - if this is the case, returns an empty optional
    private Optional<Entity> attemptToGenerateWant(String goalConcept, Entity goalCharacter, Optional<Entity> optObj) {
        ArrayList<Object> charInitFeatures = goalCharacter.getFeatures();
        ArrayList<Object> objInitFeatures = new ArrayList<>();
        String goalString = goalCharacter.toEnglish()+" wants to "+goalConcept;
        if (optObj.isPresent()) {
            Entity object = optObj.get();
            goalString += " " + object.toEnglish();
            objInitFeatures = object.getFeatures();
        }
        Entity want = BasicTranslator.getTranslator().translate(goalString).get(0);
        if (want == null) {
            // leave behind any goal concepts that aren't START-parsable
            Mark.err("Above error caused by harmless failure to parse ConceptNet idea with START");
            return Optional.empty();
        }
        
        if (!CharacterGoal.isAcceptableWant(want)) {
            // leave behind any wants that aren't well-formed
            return Optional.empty();
        }
        
        // sometimes Genesis can mistranslate the want and accidentally attach features to the goalCharacter
        // or the object. For instance, if the goal concept is "buy present", the character "Macbeth," and
        // the object "Lady Macbeth," Genesis mistranslates "Macbeth wants to buy present Lady Macbeth" by attaching
        // the present feature to Lady Macbeth. Therefore, we check to make sure no additonal features have been added
        // to the goal character/object during the want translation. If so, we remove these additional features and
        // do not return the want since it's been mistranslated
        ArrayList<Object> charNewFeatures = goalCharacter.getFeatures();
        ArrayList<Object> objNewFeatures = optObj.isPresent() ? optObj.get().getFeatures() : objInitFeatures;
        charNewFeatures.removeAll(charInitFeatures);
        objNewFeatures.removeAll(objInitFeatures);
        if (charNewFeatures.size() > 0 || objNewFeatures.size() > 0) {
            charNewFeatures.stream().forEach(feature -> goalCharacter.removeFeature(feature));
            if (optObj.isPresent()) {
                objNewFeatures.stream().forEach(feature -> optObj.get().removeFeature(feature));
            }
            return Optional.empty();
        }
        
        return Optional.of(want);
    }
    
    // see if an event action (an action from a story event) contributes to a wanted action (the action from a
    // character goal) using ConceptNet
    private GoalContributionResult conceptNetGoalContribution(String wantedActionStr, String eventActionStr) {
        List<ConceptNetAssertion> assertionsToTry = Arrays.asList(
                new ConceptNetAssertion(eventActionStr, "MotivatedByGoal", wantedActionStr),
                new ConceptNetAssertion(eventActionStr, "UsedFor", wantedActionStr),
                new ConceptNetAssertion(wantedActionStr, "HasSubevent", eventActionStr));
        
        List<ConceptNetJustification> justification = new ArrayList<>();
        for (ConceptNetAssertion assertion : assertionsToTry) {
            ConceptNetQueryResult<Double> result = ConceptNetClient.howTrueIs(assertion);
            if (result.getResult() >= CONCEPT_NET_SCORE_CUTOFF) {
                justification.add(new ConceptNetQueryJustification<Double>(result));
            }
        }
        
        if (justification.isEmpty()) {
            return GoalContributionResult.NO_CONTRIBUTION;
        }
        return GoalContributionResult.conceptNetMatch(justification);
    }
    
    // returns a list of the non-proper name direct objects + optionally proper name direct
    // objects + optionally adverbs a verb takes
    private List<Entity> getConceptVerbAdditions(Entity ent, boolean includeProperNames, boolean includeAdverbs) {
        List<Entity> additions = Util.getTransitiveRelationDirectObjects(ent);
        if (includeAdverbs) {
            additions.addAll(Util.getRelationAdverbs(ent));
        }
        return additions.stream()
                .filter(obj -> includeProperNames || !Util.isProperName(obj, false))
                .collect(Collectors.toList());
    }
    
    // returns a list of the verb joined with its non-proper name direct objects and optionally adverbs
    private List<String> getConceptVerbPhrases(Entity ent, boolean includeAdverbs) {
        String verb = ent.getType();
        List<String> phrases = new ArrayList<>();
        for (Entity addition : getConceptVerbAdditions(ent, false, includeAdverbs)) {
            String additionStr = addition.getType();
            phrases.add(verb + " " + additionStr);
            if (verb.equals(Markers.HAVE) || verb.equals(Markers.FEEL)) {
                // special case for possession or feeling - e.g. if Matt has anger, 
                // will extract the concept of anger
                phrases.add(additionStr);
            }
        }
        return phrases;
    }
    
    // e.g. transitiveGoal is "Matt plays baseball", transitive action is "Matt finds a friend"
    // returns first of possibly several contributing phrases it finds
    private GoalContributionResult conceptNetGoalContributionVerbPhrase(Entity transitiveGoal, Entity transitiveAction) {
        return getConceptVerbPhrases(transitiveGoal, false).stream()
                .map(goalConcept -> conceptNetGoalContributionVerbPhrase(goalConcept, transitiveAction, false))
                .filter(GoalContributionResult::contributes)
                .findFirst()
                .orElse(GoalContributionResult.NO_CONTRIBUTION);
    }
    
    // e.g. goalConcept is "relax", transitive action is "Matt watches television"
    // returns first of possibly several contributing phrases it finds
    private GoalContributionResult conceptNetGoalContributionVerbPhrase(String concept, Entity transitiveEnt, boolean entIsGoal) {
        return getConceptVerbPhrases(transitiveEnt, false).stream()
                // order matters
                .map(transitiveConcept -> entIsGoal ? conceptNetGoalContribution(transitiveConcept, concept) : conceptNetGoalContribution(concept, transitiveConcept))
                .filter(GoalContributionResult::contributes)
                .findFirst()
                .orElse(GoalContributionResult.NO_CONTRIBUTION);
    }    
    
    // checks that the identities of the pattern and datum match if they are both people,
    // or that they just normally match if both are not people.
    // allows a person to match a placeholder
    private boolean checkIdentityMatch(Entity pattern, Entity datum) {
        if (datum.equals(pattern)) {
            return true;
        }
        if (identityMatcher.match(pattern, datum) != null) {
            return true;
        }
        if (isIndefinite(pattern) || isIndefinite(datum)) {
            // could be more precise here - e.g. check that, to match "somebody", actually must be a person
            return true;
        }
        if (Util.isProperName(pattern, true) && Util.isProperName(datum, true)) {
            // the only way it's ok for two names to not "strictly" match is for the 
            // pattern to be a placeholder
            return goalPlaceholders.contains(pattern) && !goalPlaceholders.contains(datum);
        }
        // use normal matcher on non-proper name entities - allows for Wordnet thread matches like "hockey" and "sport"
        return matcher.match(pattern, datum) != null;
    }
    
    private boolean subjectsIdentityMatch(Entity ent1, Entity ent2) {
        return checkIdentityMatch(ent1.getSubject(), ent2.getSubject());
    }
    
    // checks if a story event contributes to a candidate character goal
    private GoalContributionResult contributionTowardsCharacterGoal(CharacterGoal inProgressGoal, Entity event) {
        // a specific character's goal cannot match an unspecified person doing something
        if (hasIndefiniteSubject(event) && !hasIndefiniteSubject(inProgressGoal.getWant())) {
            return GoalContributionResult.NO_CONTRIBUTION;
        }
        
        Entity wantedAction = inProgressGoal.getWantedAction();
        // use standard matcher to make sure match exists & bindings are valid
        LList<PairOfEntities> match = matcher.match(wantedAction, event);
        if (match != null && 
                // manually check that all pairs respect identity match, except placeholders or non-people are involved
                match.toList().stream().allMatch(pair -> 
                    checkIdentityMatch(pair.getPattern(), pair.getDatum()))) {
            return GoalContributionResult.DIRECT_MATCH;
        }
                
        if (!(wantedAction.relationP() && event.relationP() && subjectsIdentityMatch(wantedAction, event))) {
            return GoalContributionResult.NO_CONTRIBUTION;
        }
        
        Transitivity wantedActionTransitivity = ((Relation) wantedAction).getTransitivity();
        Transitivity eventTransitivity = ((Relation) event).getTransitivity();
        
        if (wantedActionTransitivity == Transitivity.TRANSITIVE &&
                eventTransitivity == Transitivity.TRANSITIVE) {
            // try event phrases against goal phrases
            // e.g. detect "Matt hits a home run" contributes to "Matt wants to play baseball"  
            // (play baseball Has Subevent hit home run)
            GoalContributionResult result = conceptNetGoalContributionVerbPhrase(wantedAction, event);
            if (result != GoalContributionResult.NO_CONTRIBUTION) {
                return result;
            }
            
            
           /*
            The two matching strategies commented out below are too permissive - they disregard context necessary
            for employing ConceptNet's knowledge.
            For instance, they would match:
            
            America wants to reach an advantage —> America want to gain land
             reach advantage Has Subevent want
             goal phrase to event verb
            America wants to play a sport —> America wins the war
             play sport Has Subevent win
             goal phrase to event verb
            America wants to take care proposal —> America conquers opponent
             conquer opponent Motivated By Goal take
             goal verb to event phrase   
            
            // try event phrases against goal verb
            // e.g. detect "Matt opens his mouth" contributes to "Matt wants to kiss Jackie"
            // (kiss Has Subevent open mouth)
            result = conceptNetGoalContributionVerbPhrase(wantedAction.getType(), event, false);
            if (result != GoalContributionResult.NO_CONTRIBUTION) {
                return result;
            }
            
            // try goal phrases against event verb
            // e.g. detect "Matt pays money" contributes to "Matt wants to buy beer" 
            // (pay Motivated By Goal buy beer)
            result = conceptNetGoalContributionVerbPhrase(event.getType(), wantedAction, true);
            if (result != GoalContributionResult.NO_CONTRIBUTION) {
                return result;
            }
            */
            
            // if named objects (e.g. people) match, try to match event verb and goal verb
            // e.g. detect "Matt punches Josh" contributes to "Matt wants to hurt Josh"
            // (punch Used For hurt)
            for (Entity nameGoalObject : Util.getTransitiveRelationProperNameDirectObjects(wantedAction)) {
                for (Entity nameActionObject : Util.getTransitiveRelationProperNameDirectObjects(event)) {
                    if (checkIdentityMatch(nameGoalObject, nameActionObject)) {
                          result = conceptNetGoalContribution(wantedAction.getType(), event.getType());
                          if (result != GoalContributionResult.NO_CONTRIBUTION) {
                              return result;
                          }
                    }
                }
            }
            
            // give up
            return GoalContributionResult.NO_CONTRIBUTION;
            
        }
        
        // at this point, at most one of {wantedAction, event} is transitive        
        if (wantedActionTransitivity == Transitivity.TRANSITIVE) {
            // event is intrans
            // e.g. detect "Matt sings" contributes to "Matt wants to make music" 
            // (play Has Subevent compete)
            GoalContributionResult result = conceptNetGoalContributionVerbPhrase(event.getType(), wantedAction, true);
            if (result != GoalContributionResult.NO_CONTRIBUTION) {
                return result;
            }
            
            // give up
            return GoalContributionResult.NO_CONTRIBUTION;
        } else if (eventTransitivity == Transitivity.TRANSITIVE) {
            // wanted action is intrans
            // e.g. detect "Matt watches television" contributes to "Matt wants to relax"
            // (watch television Motivated By Goal relax)
            GoalContributionResult result = conceptNetGoalContributionVerbPhrase(wantedAction.getType(), event, false);
            if (result != GoalContributionResult.NO_CONTRIBUTION) {
                return result;
            }
            
            // give up
            return GoalContributionResult.NO_CONTRIBUTION;
        }
        
        // try to match intrans adverbial phrases/standalone verbs
        // e.g. detect "Matt lies down" contributes to "Matt wants to relax" (lie down Motivated By Goal relax)
        // or "Matt sits quietly" contributes to "Matt wants to think" (sit quietly Has Subevent think)
        List<String> wantedActionIntransPhrases = getConceptVerbPhrases(wantedAction, true);
        wantedActionIntransPhrases.add(wantedAction.getType());
        List<String> eventIntransPhrases = getConceptVerbPhrases(event, true);
        eventIntransPhrases.add(event.getType());
        
        for (String wantedActionPhrase : wantedActionIntransPhrases) {
            for (String eventPhrase : eventIntransPhrases) {
               GoalContributionResult result = conceptNetGoalContribution(wantedActionPhrase, eventPhrase);
               if (result != GoalContributionResult.NO_CONTRIBUTION) {
                   return result;
               }
            }
        }
        
        // give up
        return GoalContributionResult.NO_CONTRIBUTION;
    }
        
    /**
     * The main public-facing component of the GoalAnalyzer. Story events should be fed into the GoalAnalyzer sequentially
     * using this method, and the output is a set of goal analysis results describing any detected completed goals. If the set is
     * empty, this event did not result in the detection of any completed goals. 
     */
    public Set<GoalAnalysisResult> processEvent(Entity event) {
        Set<GoalAnalysisResult> results = new HashSet<>();
        Mark.say(debug, "Got event to process", event);
        Mark.say(debug, "Properties", event.getPropertyList());
        if (event.hasProperty(Markers.GOAL_ANALYSIS) && event.isA(Markers.WANT_MARKER)) {
            // this is a goal event that was created because of the goal analyzer - no need to analyze it
            return results;
        }
        for (CharacterGoal inProgressGoal : inProgressGoals) {
            // must use isDeepEqual here because there may be an equivalent goal entity that's not equal because it has a different cause
            // and was created differently
            if (event.hasProperty(Markers.GOAL_ANALYSIS) && event.isDeepEqual(inProgressGoal.getWantedAction())) {
                // this is a goal realization that was created because of the goal analyzer - no need to further apply it
                // to the goal it realizes
                continue;
            }
            Mark.say(debug, "Checking if", event, "contributes to goal", inProgressGoal);
            GoalContributionResult goalContribution = contributionTowardsCharacterGoal(inProgressGoal, event);
            if (goalContribution.contributes()) {
                if (inProgressGoal.hasCause()) {
                    Mark.say(debug, "There is a causal relationship between "+inProgressGoal.getCause().toEnglish()+" and "+event.toEnglish());
                }
                Mark.say(debug, "The goal "+inProgressGoal.getWant().toEnglish()+" is accomplished when "+event.toEnglish());
                Mark.say(debug, "\n");
                inProgressGoal.setConfirmed(true);
                results.add(new GoalAnalysisResult(inProgressGoal, event, goalContribution));
            }
        }
        // an event can both cause and contribute to a goal
        this.inProgressGoals.addAll(getCausedGoals(event));
        return results;
    }
}
