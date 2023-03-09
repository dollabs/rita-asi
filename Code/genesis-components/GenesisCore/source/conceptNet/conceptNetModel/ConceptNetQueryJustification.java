package conceptNet.conceptNetModel;

import conceptNet.conceptNetNetwork.ConceptNetQueryResult;

/**
 * This class takes in a ConceptNetQueryResult and turns it into a ConceptNetJustification to be used 
 * to justify a connection that Genesis has made.
 * 
 * @author bryanwilliams
 *
 * @param <T> the result type of the query this justification relies on
 */
public class ConceptNetQueryJustification<T> extends ConceptNetJustification {

    private final ConceptNetQueryResult<T> result;
    
    public ConceptNetQueryJustification(ConceptNetQueryResult<T> result, ConceptNetJustification next) {
        super(next);
        this.result = result;
    }
    
    public ConceptNetQueryJustification(ConceptNetQueryResult<T> result) {
        this(result, null);
    }
        
    public String getJustification() {
        String justification = result.getQuery().toResultString(result.getResult());
        if (next != null) {
            return justification + ", " + next.getJustification();
        }
        return justification;
    }

    @Override
    public String toString() {
        return "ConceptNetJustification [result=" + result + ", next=" + next + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
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
        ConceptNetQueryJustification<?> other = (ConceptNetQueryJustification<?>) obj;
        if (result == null) {
            if (other.result != null)
                return false;
        } else if (!result.equals(other.result))
            return false;
        return true;
    }
}
