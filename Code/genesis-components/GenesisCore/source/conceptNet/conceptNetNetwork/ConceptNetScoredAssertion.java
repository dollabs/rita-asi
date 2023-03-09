package conceptNet.conceptNetNetwork;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

import conceptNet.conceptNetModel.ConceptNetAssertion;

/**
 * Represents a ConceptNet assertion and associated score received from ConceptNet. 
 * 
 * **Not intended for creation by users, just used to represent data received from ConceptNet.**
 * 
 * 
 * @author bryanwilliams
 *
 */
public class ConceptNetScoredAssertion extends ConceptNetAssertion implements Serializable {
    private static final long serialVersionUID = 1L;
    private final double score;
    
    
    @SuppressWarnings("unchecked")
    @JsonCreator
    // package protected constructor since only classes in conceptNetNetwork should be able to create instances of this class
    // (see documentation at top)
    ConceptNetScoredAssertion(List<Object> argList) {
        super((List<String>) argList.get(0));
        this.score = (double) argList.get(1);
    }

    public double getScore() {
        return score;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(score);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConceptNetScoredAssertion other = (ConceptNetScoredAssertion) obj;
        if (Double.doubleToLongBits(score) != Double.doubleToLongBits(other.score))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + score +")";
    }
}
