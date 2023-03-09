package conceptNet.conceptNetNetwork;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the response of the D4D server. The JSON sent from D4D is directly turned into instances of
 * this class using the Jackson JSON parsing library.
 * 
 * @author bryanwilliams
 *
 */
class ConceptNet4Response {

    private final String errorMessage;
    private final Object result;
    
    @JsonCreator
    public ConceptNet4Response(@JsonProperty("error_message") String errorMessage, 
            @JsonProperty("result") Object result) {
        this.errorMessage = errorMessage;
        this.result = result;
    }
    
    public boolean error() {
        return errorMessage.length() > 0;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public Object getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "ConceptNet4Response [errorMessage=" + errorMessage + ", result=" + result + "]";
    }
}
