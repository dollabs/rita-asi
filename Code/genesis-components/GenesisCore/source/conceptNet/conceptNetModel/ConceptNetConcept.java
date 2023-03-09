package conceptNet.conceptNetModel;

import java.io.Serializable;


// A very simple class. Mainly built so that the format of concept strings
// can be standardized using the prepareConcept method.

/**
 * The ConceptNetConcept class is used to model concepts in ConceptNet. It is very simple, and its primary purpose
 * is to format concept strings using the prepareConcept method.
 * 
 * Note that this class, along with everything in the conceptNet package, is used to represent ConceptNet data 
 * that does not require contacting the server. Because this class does not contact the server, it's possible to
 * create a ConceptNetConcept instance that represents a concept ConceptNet does not actually know anything about.
 * The ConceptNetClient class contains methods that query for data about a ConceptNetConcept instance, including whether 
 * or not ConceptNet knows anything about the concept a ConceptNetConcept instance represents.
 * 
 * @author bryanwilliams
 */
public class ConceptNetConcept implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String concept;
    
    public ConceptNetConcept(String concept) {
        this.concept =  ConceptNetConcept.prepareConcept(concept);
    }
    
    public static String prepareConcept(String concept) {
        if (concept.contains("_")) {
            concept = concept.replaceAll("_", " ");
        }
        if (concept.contains("-")) {
            concept = concept.replaceAll("-", " ");
        }
        return concept;
    }
    
    public String getConceptString() {
        return concept;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((concept == null) ? 0 : concept.hashCode());
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
        ConceptNetConcept other = (ConceptNetConcept) obj;
        if (concept == null) {
            if (other.concept != null)
                return false;
        } else if (!concept.equals(other.concept))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return concept;
    }
    
}
