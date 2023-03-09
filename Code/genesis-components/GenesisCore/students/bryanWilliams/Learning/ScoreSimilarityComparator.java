package bryanWilliams.Learning;

import java.util.function.Function;

import conceptNet.conceptNetNetwork.ConceptNetClient;
import frames.entities.Entity;
import utils.Mark;

public class ScoreSimilarityComparator extends EntitySimilarityComparator {
    private final double cutoffScore;
    
    public ScoreSimilarityComparator(Function<Entity, String> similarityStringExtractor,
            double cutoffScore) {
        super(similarityStringExtractor);
        this.cutoffScore = cutoffScore;
    }    
    @Override
    public boolean areSimilar(String s1, String s2) {
        boolean debug = false;
        Mark.say(debug, "getting similarity score for", s1, s2);
        return ConceptNetClient.getSimilarityScore(s1, s2).getResult() >= cutoffScore;
    }

}
