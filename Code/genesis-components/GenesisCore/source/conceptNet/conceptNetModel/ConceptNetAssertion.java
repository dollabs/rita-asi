package conceptNet.conceptNetModel;

import java.io.Serializable;
import java.util.List;

/**
 * The ConceptNetAssertion class  is used to model an assertions in ConceptNet.
 * 
 * Note that this class, along with everything in the conceptNet package, is used to represent ConceptNet data 
 * that does not require contacting the server. The ConceptNetClient class contains methods that query for data about a 
 * ConceptNetAssertion, such as its score in ConceptNet.
 * 
 * @author bryanwilliams
 */
public class ConceptNetAssertion implements Serializable {
    private static final long serialVersionUID = 1L;
    private final ConceptNetConcept concept1;
    private final String relation;
    private final ConceptNetConcept concept2;

    public ConceptNetAssertion(ConceptNetConcept concept1, String relation, ConceptNetConcept concept2) {
        this.concept1 = concept1;
        this.relation = relation;
        this.concept2 = concept2;
    }
    
    public ConceptNetAssertion(String concept1, String relation, String concept2) {
        this(new ConceptNetConcept(concept1), relation, new ConceptNetConcept(concept2));
    }
    
    public ConceptNetAssertion(List<String> assertionList) {
        this(assertionList.get(0), assertionList.get(1), assertionList.get(2));
    }
    
    public ConceptNetConcept getConcept1() {
        return concept1;
    }
    
    public String getConcept1String() {
        return concept1.getConceptString();
    }
    
    public String getRelation() {
        return relation;
    }
    
    public ConceptNetConcept getConcept2() {
        return concept2;
    }
    
    public String getConcept2String() {
        return concept2.getConceptString();
    }
    
    @Override
    public String toString() {
        return "ConceptNetAssertion [concept1=" + concept1 + ", relation=" + relation + ", concept2=" + concept2 + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((relation == null) ? 0 : relation.hashCode());
        result = prime * result + ((concept1 == null) ? 0 : concept1.hashCode());
        result = prime * result + ((concept2 == null) ? 0 : concept2.hashCode());
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
        ConceptNetAssertion other = (ConceptNetAssertion) obj;
        if (relation == null) {
            if (other.relation != null)
                return false;
        } else if (!relation.equals(other.relation))
            return false;
        if (concept1 == null) {
            if (other.concept1 != null)
                return false;
        } else if (!concept1.equals(other.concept1))
            return false;
        if (concept2 == null) {
            if (other.concept2 != null)
                return false;
        } else if (!concept2.equals(other.concept2))
            return false;
        return true;
    }
}
