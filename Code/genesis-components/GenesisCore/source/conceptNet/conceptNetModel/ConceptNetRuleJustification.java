package conceptNet.conceptNetModel;

import frames.entities.Entity;

/**
 * Represents a rule so that rules can be used in combination with ConceptNet knowledge in a justification.
 * For instance, in similarity-based matching, a rule + CN similarity knowledge are combined.
 * We want to show the full justification to the user, so both ConceptNetQuery instances and Genesis rules
 * must be able to be turned into ConceptNetJustifications.
 * 
 * @author bryanwilliams
 */
public class ConceptNetRuleJustification extends ConceptNetJustification {

    private final Entity rule;
    
    public ConceptNetRuleJustification(Entity rule, ConceptNetJustification next) {
        super(next);
        this.rule = rule;
    }
    
    public ConceptNetRuleJustification(Entity rule) {
        this(rule, null);
    }

    @Override
    public String getJustification() {
        String justification =  "rule " + rule.toEnglish();
        if (next != null) {
            return justification + ", " + next.getJustification();
        }
        return justification;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rule == null) ? 0 : rule.hashCode());
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
        ConceptNetRuleJustification other = (ConceptNetRuleJustification) obj;
        if (rule == null) {
            if (other.rule != null)
                return false;
        } else if (!rule.equals(other.rule))
            return false;
        return true;
    }
}
