package bryanWilliams.Coherence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bryanWilliams.Util;
import frames.entities.Entity;
import frames.entities.Sequence;
import matchers.EntityMatcher;
import matchers.representations.BindingPair;
import matchers.representations.EntityMatchResult;
import utils.Mark;

public class ConflictEvaluator {

    private static final List<String> GOAL_TYPE_KEYWORDS = Arrays.asList(new String[]{
            "goal",
            "desire",
            "want"
    });
    
    private final Sequence seq;
    private final Sequence commonsense;
    // Maps a character's goal to a set of ways of achieving it
    private final Map<Entity, Set<GoalAchievementMethod>> goalMap;
    private final EntityMatcher matcher;
    private final Entity primaryConflict;
    
    private final CauseGraph causeGraph;

    public ConflictEvaluator(Sequence seq, Sequence commonsense, CauseGraph causeGraph) {
        this.seq = seq;
        this.commonsense = commonsense;
        this.goalMap = new HashMap<>();
        this.matcher = new EntityMatcher();
        this.causeGraph = causeGraph;

        populateGoalMap();
        primaryConflict = identifyPrimaryConflict();
    }

    /**
     * Fills the goal map by scanning the narrative for goal events and seeing
     * if commonsense can tell us how that goal can be completed.
     */
    private void populateGoalMap() {
        List<Entity> storySoFar = new ArrayList<>();

        for (Entity event : seq.getElements()) {
            storySoFar.add(event);
            if (isGoalEntity(event)) {
                goalMap.put(event, new HashSet<GoalAchievementMethod>());
                for (Entity commonsenseRule : commonsense.getElements()) {
                    if (commonsenseRule.toString().equals("(rel prediction (seq conjuction (rel force (ent entity-5965) (seq roles (fun object (ent action-5963))))) (fun appear (ent action-5963)))")) {
                        continue;
                    }
                    for (Entity descendant : event.getDescendants()) {
                        if (match(descendant, commonsenseRule.getObject())) {
                            // May get mad if the subject is just an entity, not a sequence. We'll see
                            Set<Entity> requirements = 
                                    new HashSet<>(commonsenseRule.getSubject().getElements());
                            List<GoalAchievementMethod> achievementMethods = generateGoalAchievementMethods(
                                    event, commonsenseRule, descendant, new HashSet<>(), requirements, storySoFar,
                                    new HashSet<>());
                            goalMap.get(event).addAll(achievementMethods);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Identifies the primary conflict by looking at all possible conflicts and choosing
     * the one that causes the most events in the causeGraph. If no possible conflicts are present
     * in the causeGraph, throws a RuntimeException.
     * @return an Entity representing the primary conflict in the story
     */
    private Entity identifyPrimaryConflict() {
        int maxEventsCaused = -1;
        Entity primaryConflict = null;
        for (Entity goal : goalMap.keySet()) {
            CauseNode goalNode = causeGraph.nodeForEntity(goal);
            if (goalNode == null) {
                // not considering conflicts that don't appear in the causation graph
                continue;
            }
            Set<CauseNode> eventsCaused = causeGraph.getDownstreamNodes(goalNode);
            if (eventsCaused.size() > maxEventsCaused) {
                primaryConflict = goal;
                maxEventsCaused = eventsCaused.size();
            }
        }
        if (primaryConflict == null) {
            Mark.say("Possible conflicts", Util.entityCollectionToEnglish(goalMap.keySet()));
            throw new RuntimeException("Error - no conflicts appear in the cause graph. Wow, didn't see that one coming...");
        }
        return primaryConflict;
    }
    
    /**
     * A recursive method that identifies all possible ways of achieving a goal based on events
     * completed in the story.
     * 
     * A descendant of the given goalEvent must match the given commonsenseRule, so
     * this method looks at the requirements the commonsense rule gives to complete the goal
     * and tries to see if some have already been completed. The method does not make any assumptions 
     * about how a goal will be achieved, so every possible method of achieving the goal is returned.
     * The method uses the events that have already occurred in the story (storySoFar) to see what parts
     * of the goal achievement method may have already been completed, and what is currently left
     * uncompleted.
     * 
     * @param goalEvent the Entity describing a goal. a descendant of this Entity should match the
     * object of the commonsense rule
     * @param commonsenseRule the rule that describes how a goal can be fulfilled. Its object is the
     * completion of the goal, and the subjects are the requirements on the completion of the goal
     * @param goalDescendant the descendant of the goalEvent which matches the object of the commonseneRule
     * @param completedRequirements requirements for the completion of the goal that have already been completed
     * (i.e. a match to the requirement has been found earlier in the story)
     * @param uncompletedRequirements requirements for the completion of the goal that have not already been completed
     * @param storySoFar all Entities in the story Sequence up until the goal event
     * @param bindings the current set of bindings involved in matching completed requirements to subjects 
     * of the commonsense rule
     * @return a list of all the ways the goal can be achieved
     */
    private List<GoalAchievementMethod> generateGoalAchievementMethods(Entity goalEvent,
            Entity commonsenseRule, Entity goalDescendant,
            Set<Entity> completedRequirements, Set<Entity> uncompletedRequirements,
            List<Entity> storySoFar, Set<BindingPair> bindings) {
        List<GoalAchievementMethod> methods = new ArrayList<>();

        GoalAchievementMethod baseMethod = new GoalAchievementMethod(goalEvent, commonsenseRule,
                goalDescendant, bindings, completedRequirements, uncompletedRequirements);
        methods.add(baseMethod);

        for (int i = 0; i < storySoFar.size(); i++) {
            Entity event = storySoFar.get(i);
            for (Entity requirement : uncompletedRequirements) {
                EntityMatchResult matchResult = matcher.match(requirement, event);
                if (matchResult.semanticMatch && !bindingsConflict(bindings, matchResult.bindings)) {
                    Set<BindingPair> newBindings = new HashSet<>(bindings);
                    newBindings.addAll(matchResult.bindings);
                    Set<Entity> newCompletedRequirements = new HashSet<>(completedRequirements);
                    newCompletedRequirements.add(requirement);
                    Set<Entity> newUncompletedRequirements = new HashSet<>(uncompletedRequirements);
                    newUncompletedRequirements.remove(requirement);
                    methods.addAll(generateGoalAchievementMethods(goalEvent, commonsenseRule, 
                            goalDescendant, newCompletedRequirements, newUncompletedRequirements,
                            storySoFar.subList(i+1, storySoFar.size()), newBindings));
                }
            }
        }

        return methods;
    }

    /**
     * Returns true if two bindings conflict, meaning that they assign different people
     * to the same commonsense rule role (e.g. "XX"), or they assign the same person to different
     * commonsense rules
     * @param bindings existing set of bindings
     * @param newBindings a proposed set of additional bindings
     * @return true if the bindings conflict, false otherwise
     */
    private boolean bindingsConflict(Set<BindingPair> bindings, List<BindingPair> newBindings) {
        for (BindingPair newBinding : newBindings) {
            for (BindingPair exisitingBinding : bindings) {
                if (newBinding.conflictsWith(exisitingBinding))
                    return true;
            }
        }
        return false;
    }

    /**
     * @param event - the event in the narrative sequence to match 
     * @param commonsensePredicate - the commonsense rule to match
     * @return true if the given event matches the given commonsense predicate, false otherwise.
     * "Match" here means that the predicate describes a way that the narrative event can be completed.
     * e.g. "Macbeth becomes king" and "XX becomes king" match, but "Sue becomes angry" does not match
     * "XX becomes king."
     */
    private boolean match(Entity event, Entity commonsensePredicate) {
        try {
            boolean match = matcher.match(commonsensePredicate, event).semanticMatch;
            return match;
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns a GoalAchievementMethod representing the most likely way for the given goal 
     * to be resolved. Here, maximum likelihood = smallest number of steps needed to achieve that
     * goal = minimum number of uncompleted requirements within the GoalAchievementMethod.
     * Throws a RuntimeException if the given goal is not identified as a goal (not present in the
     * goalMap), and returns null if the evaluator has identified the given goal as a goal, but
     * does not know a way to resolve it.
     * 
     * @param goal - the goal for which the most likely resolution should be sought. Must be present
     * in the goalMap, otherwise throws a RuntimeException.
     * @return a GoalAchievementMethod representing the most likely way for the given goal 
     * to be resolved, or null if the evaluator does not know of any way to resolve the given goal
     */
    private GoalAchievementMethod getMostLikelyResolution(Entity goal) {
        Set<GoalAchievementMethod> resolutionOptions = goalMap.get(goal);
        if (resolutionOptions == null) {
            // goal not in goalMap
            throw new RuntimeException("Error - given goal not in goalMap");
        } else if (resolutionOptions.size() == 0) {
            // don't know a way to achieve goal
            return null;
        }
        int minNumUncompleted = Integer.MAX_VALUE;
        GoalAchievementMethod mostLikelyResolution = null;
        for (GoalAchievementMethod method : resolutionOptions) {
            if (method.getUncompletedRequirements().size() < minNumUncompleted) {
                mostLikelyResolution = method;
                minNumUncompleted = method.getUncompletedRequirements().size();
            }
        }
        return mostLikelyResolution;
    }

    /**
     * @return true if the given event expresses a character's goal, false otherwise
     */
    public static boolean isGoalEntity(Entity e) {
        return e.isA(GOAL_TYPE_KEYWORDS);
    }
    
    public Entity getPrimaryConflict() {
        return primaryConflict;
    }

    /**
     * "Says" all conflicts contained in the goalMap (prints them out using Mark.say)
     */
    public void sayConflicts() {
        for (Entity goal : goalMap.keySet()) {
            Set<GoalAchievementMethod> methods = goalMap.get(goal);
            for (GoalAchievementMethod method : methods) {
                Mark.say(method.toSubstitutedEnglish()+"\n\n");
            }
        }
    }
    
    /**
     * @return a String describing the primary conflict and what steps must be taken
     * for the conflict to be resolved
     */
    public String commentOnPrimaryConflict() {
        String comment = "I think the primary conflict is the following: " + primaryConflict.toEnglish() +"\n";
        comment += "From possible: "+Util.entityCollectionToEnglish(goalMap.keySet()) + "\n";
        
        GoalAchievementMethod resolution = getMostLikelyResolution(primaryConflict);
        if (resolution == null) {
            comment += "I do not know how this goal can be completed.\n";
        } else {
            String uncompletedStr = Util.entityCollectionToEnglish(resolution.getUncompletedRequirements());
            String completedStr = Util.entityCollectionToEnglish(resolution.getCompletedRequirements());
            uncompletedStr = resolution.substituteBindings(uncompletedStr);
            completedStr = resolution.substituteBindings(completedStr);

            comment += "For the goal to be achieved, the following must happen: " + uncompletedStr
                    + "\n";
            comment += "Since: " + completedStr + "\n";
            comment += "This is the fastest way to achieve the goal.\n";
        }
        return comment;
    }
}
