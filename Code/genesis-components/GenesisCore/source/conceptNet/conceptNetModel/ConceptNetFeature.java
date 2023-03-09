package conceptNet.conceptNetModel;

import java.io.Serializable;

/**
 * A ConceptNetFeature is a ConceptNet assertion with one concept left unspecified. The position
 * of the unspecified concept in the assertion is determined by the type field.
 * 
 * Note that this class, along with everything in the conceptNet package, is used to represent ConceptNet data 
 * that does not require contacting the server. The ConceptNetClient class contains methods that query for data about a 
 * ConceptNetFeature, such as all the ways that a feature can be completed into a fully specified assertion.
 * 
 * @author bryanwilliams
 */
public class ConceptNetFeature implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * A feature type represents the position of the unspecified concept.
     * 
     * A left feature represents concept relation ___, e.g. "relax HasSubevent ___".
     * The feature sits to the LEFT of the unspecified concept.
     * 
     * A right feature represents ___ relation concept, e.g. "___ MotivatedByGoal relax"
     * The feature sits to the RIGHT of the unspecified concept.
     * 
     * @author bryanwilliams
     */
    public enum FeatureType {
        LEFT,
        RIGHT
    };
    
    private final ConceptNetConcept concept;
    private final String relation;
    private final FeatureType type;
    
    public ConceptNetFeature(ConceptNetConcept concept, String relation, FeatureType type) {
        this.concept = concept;
        this.relation = relation;
        this.type = type;
    }
    
    public ConceptNetFeature(String concept, String relation, FeatureType type) {
        this(new ConceptNetConcept(concept), relation.replace(" ", ""), type);
    }

    public ConceptNetConcept getConcept() {
        return concept;
    }
    
    public String getConceptString() {
        return concept.getConceptString();
    }

    public String getRelation() {
        return relation;
    }

    public FeatureType getType() {
        return type;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((concept == null) ? 0 : concept.hashCode());
        result = prime * result + ((relation == null) ? 0 : relation.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        ConceptNetFeature other = (ConceptNetFeature) obj;
        if (concept == null) {
            if (other.concept != null)
                return false;
        } else if (!concept.equals(other.concept))
            return false;
        if (relation == null) {
            if (other.relation != null)
                return false;
        } else if (!relation.equals(other.relation))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ConceptNetFeature [concept=" + concept + ", relation=" + relation + ", type=" + type + "]";
    }
}
