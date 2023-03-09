package bryanWilliams.Learning;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JOptionPane;

import bryanWilliams.Pair;
import frames.entities.Entity;

public abstract class EntitySimilarityComparator implements Comparator<String> {//Comparator<Entity> {
    private final Function<Entity, String> similarityStringExtractor;
    private final Map<Pair<String>, Boolean> similarityCache = new HashMap<>();
    private static final boolean ASK_USER_PERMISSION = false;

    /**
     * Creates a similarity comparator with the given parameters.
     * @param similarityRankRequirement governs how strict the similarity comparison is. The field of one entity
     * must be in the top n results for similar concepts to the other entity's field, where n=similarityRankRequirement.
     * Larger similarityRankRequirements result in more permissive comparisons.
     * @param similarityStringExtractor defines what string of the entities should be compared for similarity
     */
    public EntitySimilarityComparator(Function<Entity, String> similarityStringExtractor) {
        this.similarityStringExtractor = similarityStringExtractor;
    }
    
    public abstract boolean areSimilar(String s1, String s2);
    
    
    private String formatSimilarityString(String s) {
        return s.replaceAll("(\\s+|-+)", "_");
    }

    /**
     * Considers two entities equal if they have either equal or similar similarity fields, where
     * the similarity field is obtained using the similarityStringExtractor given in the extractor.
     */
    @Override
//    public int compare(Entity e1, Entity e2) {
//        String s1 = formatSimilarityString(similarityStringExtractor.apply(e1));
//        String s2 = formatSimilarityString(similarityStringExtractor.apply(e2));
    public int compare(String s1, String s2) {
        s1 = formatSimilarityString(s1);
        s2 = formatSimilarityString(s2);

        if (s1.equals(s2)) {
            return 0;
        }    
        Pair<String> simPair = new Pair<>(s1, s2);
        if (similarityCache.containsKey(simPair)) {
            return similarityCache.get(simPair)? 0 : -1;
        }

        boolean similar = areSimilar(s1, s2);
        if (similar && ASK_USER_PERMISSION) {
            similar = JOptionPane.showConfirmDialog(
                    null,
                    "Should I match relations "+s1+" and "+ s2+" (i.e. X "+s1+" Y and X "+s2+" Y)?",
                    "Flexible Rule Matching",
                    JOptionPane.YES_NO_OPTION) == 0;
        }
        similarityCache.put(simPair, similar);
        return similar? 0 : -1;
    }
}
