package matchers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import bryanWilliams.Util;
import frames.entities.Entity;

/**
 * A class used to compute structural mappings between two entities.
 * This capability is present in the EntityMatcher, but it's buggy with sequences
 * and structure mapping cannot be cleanly computed alongside traditional mapping (see below). So,
 * I created my own!
 * 
 * The matching in EntityMatcher isn't sufficient because its matching of sequences doesn't backtrack from
 * incorrect choices. It also prioritizes scores instead of structural matches, and the two are hard to combine
 * (i.e. is a structure match with a score of -1 better than just a normal score of -1?). Depending on the use case,
 * a user may want to prioritize score or structural matches, so it's best to separate the two. There also may
 * be many possible structure mappings, whereas traditional matching just returns one result.
 * 
 * @author bryanwilliams
 *
 */
public class StructureMapper {

    private static final EntityMatcher em = new EntityMatcher();
    
    /**
     * Returns the structure mappings between the two entities. Each structure mapping
     * contains the bindings at every level between the two entities.
     */
    public static StructureMappingResult getStructureMappings(Entity e1, Entity e2) {
        // function for adding the mapping between these two top level entities
        UnaryOperator<StructureMapping> putTopLevelMapping = mapping -> mapping.putMapping(e1, e2);
        if (e1.entityP() && e2.entityP()) {
            return new StructureMappingResult(new StructureMapping(e1, e2));
        } else if (e1.functionP() && e2.functionP()) {
            StructureMappingResult subjectMappings = getStructureMappings(e1.getSubject(), e2.getSubject());
            return subjectMappings.adjustMappings(putTopLevelMapping);
        } else if (e1.relationP() && e2.relationP()) {
            StructureMappingResult objectMappings = getStructureMappings(e1.getObject(), e2.getObject());
            StructureMappingResult subjectMappings = getStructureMappings(e1.getSubject(), e2.getSubject());
            StructureMappingResult relationMappings = StructureMappingResult.mergeResults(
                    objectMappings, subjectMappings);
            return relationMappings.adjustMappings(putTopLevelMapping);
        } else if (e1.sequenceP() && e2.sequenceP()) {
            if (e1.getNumberOfChildren() != e2.getNumberOfChildren()) {
                return StructureMappingResult.empty();
            }
            if (e1.getNumberOfChildren() == 0) {
                return new StructureMappingResult(new StructureMapping(e1, e2));
            }
            
            StructureMappingResult comboMappings = getCombinationStructureMappings(
                    e1.getElements(), e2.getElements());
            return comboMappings.adjustMappings(putTopLevelMapping);
        }
        
        // if two entities are not same type, no possible mappings
        return StructureMappingResult.empty();
    }
    
    // used for handling structure mappings between two sequences 
    // requires two lists to be the same size, size > 0
    private static StructureMappingResult getCombinationStructureMappings(
            List<Entity> elements1, List<Entity> elements2) {
        StructureMappingResult allPossibleMappings = StructureMappingResult.empty();
        if (elements1.size() == 0) {
            return allPossibleMappings;
        }
        
        Entity ele1 = elements1.get(0);
        for (Entity ele2 : elements2) {
            StructureMappingResult firstElMappings = getStructureMappings(ele1, ele2);
            if (firstElMappings.isEmpty()) {
                continue;
            }

            List<Entity> newElements1 = new ArrayList<>(elements1);
            newElements1.remove(ele1);
            List<Entity> newElements2 = new ArrayList<>(elements2);
            newElements2.remove(ele2);
            if (newElements1.size() == 0) {
                assert newElements2.size() == 0;
                return firstElMappings;
            }

            StructureMappingResult restMappings = getCombinationStructureMappings(
                    newElements1, newElements2);
            if (restMappings.isEmpty()) {
                continue;
            }
            StructureMappingResult completeMergedMappings = 
                    StructureMappingResult.mergeResults(firstElMappings, restMappings);
            allPossibleMappings = StructureMappingResult.combineResults(
                    allPossibleMappings, completeMergedMappings);
        }
        
        return allPossibleMappings;
    }
    
    
    /**
     * Returns true iff 
     *  1. the two entities are both proper names, or 
     *  2. neither of the entities are proper names AND they match using traditional matching.
     *  
     *  Useful for testing if two entities like "Josh eats an apple" and "Josh wants to eat popcorn" agree
     *  (there is a structure match, but non-people disagree)
     */
    public static boolean boundEntitiesAgree(Entity ent1, Entity ent2) {
        if (Util.isProperName(ent1, true)) {
            return Util.isProperName(ent2, true);
        }
        return em.match(ent1, ent2).semanticMatch;
    }
}
