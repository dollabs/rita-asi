package conceptNet.conceptNetNetwork;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RelatedGroup implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String base;
    private final List<RelatedGroupElement> relatedWords;
    
    @JsonCreator
    private RelatedGroup(@JsonProperty("@id") String baseStr, 
            @JsonProperty("related") List<RelatedGroupElement> similarWords) {
        // remove the "/c/en/" prefix
        this.base = baseStr.substring(baseStr.lastIndexOf('/') + 1);
        this.relatedWords = new ArrayList<>(similarWords);
    }

    public String getBase() {
        return base;
    }

    public List<RelatedGroupElement> getSimilarWords() {
        return Collections.unmodifiableList(relatedWords);
    }

    @Override
    public String toString() {
        return "RelatedGroup [base=" + base + ", relatedWords=" + relatedWords + "]";
    }    
}
