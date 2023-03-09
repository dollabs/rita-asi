package conceptNet.conceptNetNetwork;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections15.ListUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Doubles;

import conceptNet.conceptNetModel.ConceptNetAssertion;
import conceptNet.conceptNetModel.ConceptNetConcept;
import conceptNet.conceptNetModel.ConceptNetFeature;


/**
 * ConceptNetQuery is an abstraction used to represent all the information that a ConceptNet 4 query needs to know
 * -- what object type it uses to represent its result, what d4d method it corresponds to, what its arguments are, 
 * how to turn d4d's response into its an instance of its result type, what its default result should be 
 * if there's any sort of error, etc.
 * 
 * Currently ConceptNetQuery just being used for ConceptNet 4, but could presumably be extended to ConceptNet 5 too
 * (although CN5 only supports a subset of CN4 queries)
 * 
 * If you're interested in using this class, and especially if you're interested in *extending* this class, the following guide
 * is hopefully helpful: https://goo.gl/4PpNOL
 * 
 * @author bryanwilliams
 *
 * @param <T> the object type used to represent the result of this query
 */
public interface ConceptNetQuery<T> extends Serializable {

    // the method_name argument in the POST request to send to d4d
    String getMethodName();

    // a list of the arg0, arg1, arg2,... arguments in the POST request to send to d4d
    List<String> getArguments();

    // a user-friendly string representing this query
    String toQueryString();
    
    // a user-friendly string representing this query combined with its result
    String toResultString(T result);
    
    // parses the d4d response into an instance of the expected result type
    // returns null if not parsable
    T parseResult(Object result);

    // what result should be returned if there's any sort of problem
    ConceptNetQueryResult<T> defaultResult();

    // what concepts does this query consists of
    List<ConceptNetConcept> getComponentConcepts();

    // what needs to be true for this query to fire. if this isn't true,
    // the default result is returned
    // default impl - ensure that all component concepts exist in ConceptNet
    default boolean meetsPrerequisites() {
        return getComponentConcepts().stream()
                .map(ConceptNetClient::conceptExists)
                .map(ConceptNetQueryResult::getResult)
                .allMatch(Boolean::booleanValue);
    }

}

// represents any query whose result is a score, which is represented as a double
abstract class ConceptNetScoreQuery implements ConceptNetQuery<Double> {
    protected static final long serialVersionUID = 1L;

    // unfortunately, the response may be an Integer which Jackson (the JSON parsing library) cannot convert to a Double
    // it can also be a String if there has been an error
    // this is why we must manually parse the Object result rather than using Jackson to parse it automatically
    @Override
    public Double parseResult(Object result) {
        if (result instanceof Double) {
            return (double) result;
        } else if (result instanceof Integer) {
            return ((Integer) result).doubleValue();
        } else if (result instanceof String) {
            Double scoreNum = Doubles.tryParse((String) result);
            if (scoreNum != null) {
                return scoreNum.doubleValue();
            }
        }
        return null;
    }
    
    public ConceptNetQueryResult<Double> defaultResult() {
        return new ConceptNetQueryResult<Double>(this, 0.0);
    }
    
    @Override
    public String toResultString(Double result) {
        return toQueryString() + " (" + String.format("%.2f", result) +")";
    }
    
}

// represents any query whose result is a boolean
abstract class ConceptNetBooleanQuery implements ConceptNetQuery<Boolean> {
    private static final long serialVersionUID = 1L;

    @Override
    public Boolean parseResult(Object result) {
        try {
            return (boolean) result;
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public ConceptNetQueryResult<Boolean> defaultResult() {
        return new ConceptNetQueryResult<Boolean>(this, false);
    }
    
    @Override
    public String toResultString(Boolean result) {
        return toQueryString();
    }
}

// these queries return a List of type T rather than a single result
abstract class ConceptNetWideQuery<T> implements ConceptNetQuery<List<T>> {
    protected static final long serialVersionUID = 1L;
    private final Class<T> listElementClass;
    private static final ObjectMapper mapper = new ObjectMapper();

    
    
    protected ConceptNetWideQuery(Class<T> listElementClass) {
        this.listElementClass = listElementClass;
    }
    
    @Override
    public List<T> parseResult(Object result) {
        // Jackson magic! converts the JSON array into a Java list of instances of T 
        return mapper.convertValue(result, new TypeReference<List<T>>() {
            @Override
            public Type getType() {
                return mapper.getTypeFactory().constructCollectionType(List.class, listElementClass);
            }
        });
    }

    @Override
    public ConceptNetQueryResult<List<T>> defaultResult() {
        return new ConceptNetQueryResult<List<T>>(this, Collections.emptyList());
    }
    
    @Override
    public String toResultString(List<T> result) {
        // if ever decide to use this, something else is probably preferable to result.toString()
        return toQueryString() + " = " + result.toString();
    }
}

// queries for the score of an assertion
class ConceptNetAssertionQuery extends ConceptNetScoreQuery {
    private static final long serialVersionUID = 1L;
    private final ConceptNetAssertion assertion;
    private final String methodName;
    
    // useAnalogy space controls whether the "filled in" version of the matrix, 
    // i.e. after SVD processing, is used. can be useful, but sometimes missing info
    // original matrix had
    public ConceptNetAssertionQuery(ConceptNetAssertion assertion, boolean useAnalogySpace) {
        this.assertion = assertion;
        this.methodName = useAnalogySpace ? "how_true_is" : "how_true_is_sparse";
    }
    
    public ConceptNetAssertionQuery(String concept1, String relation, String concept2, boolean useAnalogySpace) {
        this(new ConceptNetAssertion(concept1, relation, concept2), useAnalogySpace);
    }
    
    public ConceptNetAssertionQuery(ConceptNetAssertion assertion) {
        this(assertion, false);
    }
    
    public ConceptNetAssertionQuery(String concept1, String relation, String concept2) {
        this(concept1, relation, concept2, false);
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public List<String> getArguments() {
        return ConceptNetClient.stringify(
                Arrays.asList(assertion.getConcept1String(), assertion.getRelation(), assertion.getConcept2String()));
    }
    
    @Override
    public List<ConceptNetConcept> getComponentConcepts() {
        return Arrays.asList(assertion.getConcept1(), assertion.getConcept2());
    }

    @Override
    public String toQueryString() {
        return assertion.getConcept1String() + " " + assertion.getRelation() + " " + assertion.getConcept2String();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((assertion == null) ? 0 : assertion.hashCode());
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
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
        ConceptNetAssertionQuery other = (ConceptNetAssertionQuery) obj;
        if (assertion == null) {
            if (other.assertion != null)
                return false;
        } else if (!assertion.equals(other.assertion))
            return false;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ConceptNetAssertionQuery [assertion=" + assertion + ", methodName=" + methodName + "]";
    }
}

// queries for similarities of two concepts
class ConceptNetSimilarityQuery extends ConceptNetScoreQuery {
    private static final long serialVersionUID = 1L;
    private final ConceptNetConcept concept1;
    private final ConceptNetConcept concept2;
    
    public ConceptNetSimilarityQuery(ConceptNetConcept concept1, ConceptNetConcept concept2) {
        this.concept1 = concept1;
        this.concept2 = concept2;
    }
    
    public ConceptNetSimilarityQuery(String concept1, String concept2) {
        this(new ConceptNetConcept(concept1), new ConceptNetConcept(concept2));
    }
    
    @Override
    public String getMethodName() {
        return "how_similar_are";
    }

    @Override
    public List<String> getArguments() {
        return ConceptNetClient.stringify(Arrays.asList(concept1.getConceptString(), concept2.getConceptString()));
    }
    
    @Override
    public List<ConceptNetConcept> getComponentConcepts() {
        return Arrays.asList(concept1, concept2);
    }

    @Override
    public String toQueryString() {
        return concept1.getConceptString() +", "+concept2.getConceptString()+" similar";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        ConceptNetSimilarityQuery other = (ConceptNetSimilarityQuery) obj;
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

    @Override
    public String toString() {
        return "ConceptNetSimilarityQuery [concept1=" + concept1 + ", concept2=" + concept2 + "]";
    }
}

// queries that ask whether ConceptNet knows about a certain concept
class ConceptNetConceptExistsQuery extends ConceptNetBooleanQuery {
    private static final long serialVersionUID = 1L;
    private final ConceptNetConcept concept;
    
    public ConceptNetConceptExistsQuery(ConceptNetConcept concept) {
        this.concept = concept;
    }
    
    public ConceptNetConceptExistsQuery(String concept) {
        this(new ConceptNetConcept(concept));
    }
    
    @Override
    public String getMethodName() {
        return "is_concept";
    }

    @Override
    public List<String> getArguments() {
        return ConceptNetClient.stringify(Arrays.asList(concept.getConceptString()));
    }

    @Override
    public String toQueryString() {
        return "is concept? "+concept.getConceptString();
    }
    
    @Override
    public List<ConceptNetConcept> getComponentConcepts() {
        return Arrays.asList(concept);
    }
    
    // no need to check that component concepts exist for this query to fire - that's
    // exactly what this query does!
    @Override
    public boolean meetsPrerequisites() {
        return true;
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
        ConceptNetConceptExistsQuery other = (ConceptNetConceptExistsQuery) obj;
        if (concept == null) {
            if (other.concept != null)
                return false;
        } else if (!concept.equals(other.concept))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ConceptNetConceptExistsQuery [concept=" + concept + "]";
    }
}

// queries that return all the different ways a feature can be completed into a scored assertion
class ConceptNetFeatureQuery extends ConceptNetWideQuery<ConceptNetScoredAssertion> {
    private static final long serialVersionUID = 1L;
    private final ConceptNetFeature feature;
    private final boolean useAnalogySpace;

    public ConceptNetFeatureQuery(ConceptNetFeature feature, boolean useAnalogySpace) {
        super(ConceptNetScoredAssertion.class);
        this.feature = feature;
        this.useAnalogySpace = useAnalogySpace;
    }
    
    public ConceptNetFeatureQuery(ConceptNetFeature feature) {
        this(feature, false);
    }

    @Override
    public String getMethodName() {
        switch (feature.getType()) {
        case LEFT:
            return "concept1_and_relation_to_assertions";
        case RIGHT:
            return "relation_and_concept2_to_assertions";
        default:
            throw new RuntimeException("Unknown method name for feature type "+feature.getType());
        }
    }
    
    @Override
    public List<String> getArguments() {
        List<String> beginningArgs;
        switch (feature.getType()) {
        case LEFT:
            beginningArgs = Arrays.asList(feature.getConcept().getConceptString(), feature.getRelation());
            break;
        case RIGHT:
            beginningArgs = Arrays.asList(feature.getRelation(), feature.getConcept().getConceptString());
            break;
        default:
            throw new RuntimeException("Unknown arguments for feature type "+feature.getType());
        }
        // third arg controls whether just top concept for feature is returned, fourth argument controls
        // whether the sparse matrix (i.e. not analogy space) is used
        List<String> endArgs = Arrays.asList("false", Boolean.valueOf(!useAnalogySpace).toString());
        return ListUtils.union(ConceptNetClient.stringify(beginningArgs), endArgs);
    }
    
    @Override
    public List<ConceptNetConcept> getComponentConcepts() {
        return Arrays.asList(feature.getConcept());
    }

    @Override
    public String toQueryString() {
        switch (feature.getType()) {
        case LEFT:
            return feature.getConcept().getConceptString() + " " + feature.getRelation() + " ___";
        case RIGHT:
            return "___ " + feature.getRelation() + " " + feature.getConcept().getConceptString();
        default:
            throw new RuntimeException("Unknown query string for feature type "+feature.getType());
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((feature == null) ? 0 : feature.hashCode());
        result = prime * result + (useAnalogySpace ? 1231 : 1237);
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
        ConceptNetFeatureQuery other = (ConceptNetFeatureQuery) obj;
        if (feature == null) {
            if (other.feature != null)
                return false;
        } else if (!feature.equals(other.feature))
            return false;
        if (useAnalogySpace != other.useAnalogySpace)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ConceptNetFeatureQuery [feature=" + feature + "]";
    }
}
