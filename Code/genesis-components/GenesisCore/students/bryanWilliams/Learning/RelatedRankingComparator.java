package bryanWilliams.Learning;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import conceptNet.conceptNetNetwork.ConceptNetClient;
import conceptNet.conceptNetNetwork.RelatedGroup;
import conceptNet.conceptNetNetwork.RelatedGroupElement;
import frames.entities.Entity;
import utils.Mark;

public class RelatedRankingComparator extends EntitySimilarityComparator {
    private final int relatedRankRequirement;
    private final boolean bidirectional;
    private final Map<String, RelatedGroup> cache = new HashMap<>();
    
    public RelatedRankingComparator(Function<Entity, String> similarityStringExtractor,
            int similarityRankRequirement, boolean bidirectional) {
        super(similarityStringExtractor);
        this.relatedRankRequirement = similarityRankRequirement;
        this.bidirectional = bidirectional;
    }
    
    public boolean areSimilarWithOrdering(String s1, String s2) {
        if (!cache.containsKey(s1)) {
            try {
                Mark.say("getting related group for", s1);
                RelatedGroup s1rGroup = ConceptNetClient.getRelatedGroup(s1);
                cache.put(s1, s1rGroup);
            } catch (IOException e) {
                Mark.err("RelatedRankingComparator could not get similarity group for "+s1);
                e.printStackTrace();
                return false;
            }
        }      
        
        List<RelatedGroupElement> similarWords = cache.get(s1).getSimilarWords();
        System.out.println("similarWords: "+similarWords);
        return similarWords.stream()
                .limit(relatedRankRequirement)
                .anyMatch(el -> s2.equalsIgnoreCase(el.getWord()));
    }

    @Override
    public boolean areSimilar(String s1, String s2) {
        if (bidirectional) {
            return areSimilarWithOrdering(s1, s2) || areSimilarWithOrdering(s2, s1);
        }
        return areSimilarWithOrdering(s1, s2);
    }
}
