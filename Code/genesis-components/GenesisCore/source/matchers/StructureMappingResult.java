package matchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * This class represents the result from a structure mapping. It holds all of the structure mappings between
 * the two entities. It can also be empty, in which case there is no structure mapping between the two entities.
 * 
 * @author bryanwilliams
 */
public class StructureMappingResult implements Iterable<StructureMapping> {

    private final List<StructureMapping> mappings;
    
    public static StructureMappingResult empty() {
        return new StructureMappingResult(new ArrayList<>());
    }
    
    public StructureMappingResult(StructureMapping mapping) {
        this.mappings = Arrays.asList(mapping);
    }
    
    public StructureMappingResult(List<StructureMapping> mappings) {
        this.mappings = new ArrayList<>(mappings);
    }
    
    public boolean isEmpty() {
        return mappings.isEmpty();
    }
    
    public List<StructureMapping> getMappings() {
        return Collections.unmodifiableList(mappings);
    }
    
    /**
     * Returns a new StructureMappingResult which is the product of applying the adjustFcn to 
     * each of the structure mappings in this result.
     */
    public StructureMappingResult adjustMappings(UnaryOperator<StructureMapping> adjustFcn) {
        return new StructureMappingResult(mappings.stream()
                .map(adjustFcn)
                .collect(Collectors.toList()));
    }
    
    /**
     * Returns a new StrucutreMappingResult which is the addition of r1 and r2. 
     * The new structure mapping result contains all the structure mappings from each of r1 and r2. 
     * Does not merge the two in any way - if this desired, see StructureMapping.mergeResults()
     */
    public static StructureMappingResult combineResults(StructureMappingResult r1, StructureMappingResult r2) {
        List<StructureMapping> combinedMappings = new ArrayList<>();
        combinedMappings.addAll(r1.mappings);
        combinedMappings.addAll(r2.mappings);
        return new StructureMappingResult(combinedMappings);
    }
    
    /**
     * Returns a new StructureMappingResult which consists of the legal merges of each structure mapping in r1 
     * with each structure mapping in r2. A "legal merge" here means the two mappings do not conflict (i.e. map the same
     * entity to two different entities). 
     */
    public static StructureMappingResult mergeResults(StructureMappingResult r1, StructureMappingResult r2) {
        List<StructureMapping> mergedMappings = new ArrayList<>();
        for (StructureMapping m1 : r1.mappings) {
            for (StructureMapping m2 : r2.mappings) {
                if (!StructureMapping.mappingsConflict(m1, m2)) {
                    mergedMappings.add(StructureMapping.mergeMappings(m1, m2));
                }
            }
        }
        return new StructureMappingResult(mergedMappings);
    }

    @Override
    public Iterator<StructureMapping> iterator() {
        return getMappings().iterator();
    }
}
