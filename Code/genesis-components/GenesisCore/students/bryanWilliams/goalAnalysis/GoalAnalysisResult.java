package bryanWilliams.goalAnalysis;

import frames.entities.Entity;

/**
 * This class represents a result that the ASPIRE Engine produces when it identifies that a story event contributes to
 * a candidate character goal.
 * 
 * @author bryanwilliams
 *
 */
public class GoalAnalysisResult {
    private final CharacterGoal goal;
    private final Entity completion;
    private final GoalContributionResult contributionInfo;
    
    public GoalAnalysisResult(CharacterGoal goal, Entity completion, GoalContributionResult contributionInfo) {
        this.goal = goal;
        this.completion = completion;
        this.contributionInfo = contributionInfo;
    }
    
    public CharacterGoal getGoal() {
        return goal;
    }
    
    public boolean hasCause() {
        return goal.hasCause();
    }
    
    public Entity getCause() {
        return goal.getCause();
    }
    
    // e.g. "Charlies wants to relax"
    public Entity getWant() {
        return goal.getWant();
    }

    // e.g. "Charlie relaxes"
    public Entity getWantedAction() {
        return goal.getWantedAction();
    }

    public Entity getCompletion() {
        return completion;
    }
    
    public GoalContributionResult getContributionInfo() {
        return contributionInfo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((completion == null) ? 0 : completion.hashCode());
        result = prime * result + ((goal == null) ? 0 : goal.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GoalAnalysisResult other = (GoalAnalysisResult) obj;
        if (completion == null) {
            if (other.completion != null)
                return false;
        } else if (!completion.equals(other.completion))
            return false;
        if (goal == null) {
            if (other.goal != null)
                return false;
        } else if (!goal.equals(other.goal))
            return false;
        return true;
    }
}
