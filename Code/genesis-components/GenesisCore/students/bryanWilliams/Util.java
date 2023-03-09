package bryanWilliams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import bryanWilliams.Learning.ScoreSimilarityComparator;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;
import matchers.StandardMatcher;
import matchers.Substitutor;
import translator.BasicTranslator;

public class Util {
    
    // TODO make tighter by creating LazyArray class?
    private static final Entity[] PLACEHOLDERS = new Entity[26];    
    public static final List<Entity> INDEFINITE_PLACEHOLDERS = Arrays.asList("someone", "something", "somebody")
            .stream()
            .map(str -> Util.getAllPersonComponents(
                    BasicTranslator.getTranslator().translateToEntity(
                            str + " is a person")).get(1))
            .collect(Collectors.toList());
    public static final Entity SOMEONE_PLACEHOLDER = INDEFINITE_PLACEHOLDERS.get(0);
    public static final Comparator<String> similarityComparator = new ScoreSimilarityComparator(
            Entity::getType, StandardMatcher.CONCEPTNET_SIMILARITY_SCORE_CUTOFF);
    public static double time = 0;
    
    public static Entity getPlaceholder(int i) {
        if (PLACEHOLDERS[i] == null) {
            char pChar = (char) ('A' + i);
            PLACEHOLDERS[i] = Util.getAllPersonComponents(
                    BasicTranslator.getTranslator().translateToEntity(""+pChar+pChar+" is a person"))
                    .get(1);
        }
        return PLACEHOLDERS[i];
    }
    
    public static int numPlaceholders() {
        return PLACEHOLDERS.length;
    }
    
    @SuppressWarnings("unchecked")
    private static <T extends Entity> T replaceWithPlaceholders(T ent, Collection<Entity> containedEntsToReplace) {
        int index = 0;
        T entWithPlaceholders = ent;
        for (Entity e : containedEntsToReplace) {
            Entity placeholder = Util.getPlaceholder(index++);
            // TODO address casting?
            entWithPlaceholders = (T) Substitutor.substitute(placeholder, 
                    e, entWithPlaceholders);
            
        }
        return entWithPlaceholders;
    }
    
    public static <T extends Entity> T withPlaceholders(T ent) {
        return replaceWithPlaceholders(ent, new HashSet<>(Util.getAllDeepComponents(ent)));
    }
    
    public static <T extends Entity> T withPersonPlaceholders(T ent) {
        return replaceWithPlaceholders(ent, new HashSet<>(Util.getAllPersonComponents(ent)));
    }
    
    public static <T extends Entity> T withProperNamePlaceholders(T ent) {
        return replaceWithPlaceholders(ent, new HashSet<>(Util.getAllProperNameComponents(ent)));
    }        
    /**
     * @return the English representation of the entity followed by 
     * a bracketed innerese representation
     */
    public static String toEnglishAndInnerese(Entity e) {
        if (e == null) {
            return "null";
        }
        return e.toEnglish()+" ["+e.toString()+"]";
    }

    
    /**
     * @return a English representation (String) of the given set of Entities.
     */
    public static String entityCollectionToEnglish(Collection<Entity> entities) {
        String s = "[";
        for (Entity e : entities) {
            s += e.toEnglish()+", ";
        }
        if (entities.size() > 0) {
            // remove trailing comma and space
            s = s.substring(0, s.length() - 2);        
        }
        s += "]";
        return s;
    }
    
    /**
     * @return a list of all "deep" components of e, where a deep component
     * is an entity with no components of its own
     */
    public static List<Entity> getAllDeepComponents(Entity e) {        
        List<Entity> curComponents = e.getAllComponents();
        if (curComponents.isEmpty()) {
            if (e.entityP()) {
                return new ArrayList<>(Arrays.asList(e));
            } else if (e.sequenceP()) {
                return new ArrayList<>();
            } else{
                throw new IllegalArgumentException("Unexpected entity structure: "+e);
            }
        }
        List<Entity> lowestComponents = new ArrayList<>();
        for (Entity curEntity : curComponents) {
            lowestComponents.addAll(getAllDeepComponents(curEntity));
        }
        return lowestComponents;
    }
    
    public static List<Entity> getAllDeepComponents(Entity e, Predicate<Entity> pred) {
        return Util.getAllDeepComponents(e).stream()
                .filter(pred)
                .collect(Collectors.toList());
    }
    
    /**
     * @return all the deep person components (entities of type person) contained in e
     */
    public static List<Entity> getAllPersonComponents(Entity e) {
        return Util.getAllDeepComponents(e, ent -> ent.isA("person"));
    }
    
    public static boolean isProperName(Entity e) {
        return isProperName(e, false);
    }
    
    /**
     * If allowIndefinites is true, indefinite nouns like "someone," "somebody,"
     * or "something" will be considered proper names too.
     */
    public static boolean isProperName(Entity e, boolean allowIndefinites) {
        return (e.isA("name") && e.hasProperty(Markers.PROPER)) 
                || (allowIndefinites && Util.INDEFINITE_PLACEHOLDERS.contains(e));
    }
    
    /**
     * @return all the deep name components (entities that are proper names) contained in e
     */
    public static List<Entity> getAllProperNameComponents(Entity e) {
        return Util.getAllDeepComponents(e, Util::isProperName);
    }
    
    /**
     * "Flattens" the elements in a sequence (some of which may be further sequences)
     * by returning the set of entities contained in the sequence.
     * @param s - the sequence to be flattened
     * @return the set of entities contained in this sequence, regardless of nesting depth
     */
    public static Set<Entity> flattenSequence(Sequence s) {
        Set<Entity> entities =  new HashSet<Entity>();
        for (Entity e : s.getElements()) {
            if (e.sequenceP()) {
                entities.addAll(flattenSequence((Sequence) e)); 
            } else {
                entities.add(e);
            }
        }
        return entities;
    }
        
    private static List<Entity> getRelationObjects(Entity rel, Predicate<Entity> functionFilter) {
        return rel.getObject().stream()
                .filter(Entity::functionP)
                .filter(functionFilter)
                .map(Entity::getSubject)
                .collect(Collectors.toList());
    }
    
    public static List<Entity> getTransitiveRelationDirectObjects(Entity rel) {
        return getRelationObjects(rel, ent -> ent.isA(Markers.OBJECT_MARKER));
    }
    
    public static List<Entity> getRelationAdverbs(Entity rel) {
        return getRelationObjects(rel, ent -> ent.isA(Markers.MANNER_MARKER));
    }
    
    /**
     * Returns indefinite direct objects (e.g. somebody, something) too.
     */
    public static List<Entity> getTransitiveRelationProperNameDirectObjects(Entity rel) {
        return getTransitiveRelationDirectObjects(rel).stream()
                .filter(e -> Util.isProperName(e, true))
                .collect(Collectors.toList());
    }
}
