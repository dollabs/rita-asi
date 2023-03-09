package conceptNet.conceptNetNetwork;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RelatedGroupElement implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String word;
    private final double score;
    
    @JsonCreator
    private RelatedGroupElement(@JsonProperty("@id") String wordStr, @JsonProperty("weight") double score) {
        // remove the "/c/en/" prefix
        this.word = wordStr.substring(wordStr.lastIndexOf('/') + 1);
        this.score = score;
    }

    public String getWord() {
        return word;
    }

    public double getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "SimilarityGroupElement [word=" + word + ", score=" + score + "]";
    }
}
