package bryanWilliams.Coherence;

import java.util.Set;

import bryanWilliams.Util;
import frames.entities.Entity;
import matchers.representations.BindingPair;

public class GoalAchievementMethod {

    private final Entity goal;
    private final Entity commonsenseRule;
    // The "descendant" of the goal that directly matches the commonsense rule
    private final Entity goalDescendant;
    // Bindings involved in the completedRequirements. Bindings match completed requirements
    // to subjects of the commonsense rule
    private final Set<BindingPair> bindings;
    private final Set<Entity> completedRequirements;
    private final Set<Entity> uncompletedRequirements;
    
    public GoalAchievementMethod(Entity goal, Entity commonsenseRule,
            Entity goalDescendant, Set<BindingPair> bindings, 
            Set<Entity> completedRequirements, Set<Entity> uncompletedRequirements) {
        this.goal = goal;
        this.commonsenseRule = commonsenseRule;
        this.goalDescendant = goalDescendant;
        this.bindings = bindings;
        this.completedRequirements = completedRequirements;
        this.uncompletedRequirements = uncompletedRequirements;
    }
    
    public Entity getGoal() {
        return goal;
    }
    
    public Entity getCommonsenseRule() {
        return commonsenseRule;
    }
    
    /**
     * @return the "descendant" of the goal that directly matches the commonsense rule
     */
    public Entity getGoalDescendant() {
        return goalDescendant;
    }
    
    /**
     * @return the set of completed events required for the goal to be achieved in this way
     */
    public Set<Entity> getCompletedRequirements() {
        return completedRequirements;
    }
    
    /**
     * @return the set of uncompleted events required for the goal to be achieved in this way
     */
    public Set<Entity> getUncompletedRequirements() {
        return uncompletedRequirements;
    }
    
    public Set<BindingPair> getBindings() {
        return bindings;
    }
    
    /**
     * Substitutes all bindings used in this method into the String by replacing every
     * instance of the pattern in English with the datum in English.
     * @param target - the string to substitute bindings into
     * @return a new version of target with all English patterns used by this method replaced
     * with an English version of the datum they correspond to
     */
    public String substituteBindings(String target) {
        // could have a bug - what if two bindings use same placeholder?
        for (BindingPair bp : bindings) {
            target = target.replaceAll(bp.getPattern().toEnglish(), bp.getDatum().toEnglish());
        }
        return target;
    }
    
    /**
     * @return a String representation of the GoalAchievementMethod, but all contained
     * events are described in human-readable English
     */
    public String toEnglish() {
        String s = "Goal: "+goal.toEnglish()+"\n";
        s += "Commonsense Rule: "+commonsenseRule.toEnglish()+"\n";
        s += "Goal Descendant: "+goalDescendant.toEnglish()+"\n";
        s += "Completed Requirements: "+Util.entityCollectionToEnglish(completedRequirements)+"\n";
        s += "Uncompleted Requirements: "+Util.entityCollectionToEnglish(uncompletedRequirements)+"\n";
        s += "Bindings: "+bindings;
        return s;
    }
    
    /**
     * @return a String representation of the GoalAchievementMethod, but all contained
     * events are described in human-readable English. Additionally, the bindings have been
     * substituted into the completed and uncompleted requirements.
     */
    public String toSubstitutedEnglish() {
        return substituteBindings(toEnglish());
    }
}
