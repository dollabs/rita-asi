package bryanWilliams.goalAnalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import conceptNet.conceptNetModel.ConceptNetJustification;

/**
 * This class is used to represent a result from goal contribution analysis. A result can either be
 * no contribution, a direct match (an event matches a candidate character goal using traditional matching),
 * or a ConceptNet match (an event contributes to a candidate character goal using knowledge from ConceptNet).
 *
 * @author bryanwilliams
 */
public interface GoalContributionResult {

    public static GoalContributionResult conceptNetMatch(List<ConceptNetJustification> justification) {
        return new ConceptNetMatch(justification);
    }

    public boolean contributes();

    public boolean consultedConceptnet();

    // use a list of justifications because there may be multiple
    public List<ConceptNetJustification> justification();

    public static GoalContributionResult NO_CONTRIBUTION = new GoalContributionResult() {

        @Override
        public boolean contributes() {
            return false;
        }
        
        @Override
        public boolean consultedConceptnet() {
            return false;
        }

        @Override
        public List<ConceptNetJustification> justification() {
            return Collections.emptyList();
        }

    };

    public static GoalContributionResult DIRECT_MATCH = new GoalContributionResult() { 

        @Override
        public boolean contributes() {
            return true;
        }

        @Override
        public boolean consultedConceptnet() {
            return false;
        }

        @Override
        public List<ConceptNetJustification> justification() {
            return Collections.emptyList();
        }

    };

    class ConceptNetMatch implements GoalContributionResult {

        private final List<ConceptNetJustification>  justification;

        ConceptNetMatch(List<ConceptNetJustification> justification) {
            this.justification = new ArrayList<>(justification);
        }

        @Override
        public boolean contributes() {
            return true;
        }

        @Override
        public boolean consultedConceptnet() {
            return true;
        }

        @Override
        public List<ConceptNetJustification> justification() {
            return Collections.unmodifiableList(justification);
        }
    }
}




