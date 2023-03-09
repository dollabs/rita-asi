package matchers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import bryanWilliams.Pair;
import bryanWilliams.Util;
import frames.entities.Entity;
import utils.PairOfEntities;
import utils.minilisp.LList;

/**
 * An immutable class representing a mapping between two Entities (including Relations, Sequences, etc.)
 * that are structurally equivalent.
 * 
 * @author bryanwilliams
 */
public class StructureMapping {
    
    // BiMap used because the mapping is directionless
    private final BiMap<Entity, Entity> mapping;
    
    /**
     * Initializes an empty StructureMapping.
     */
    public StructureMapping() {
        this.mapping = HashBiMap.create();
    }
    
    /**
     * Initialized the StructureMapping with a given mapping.
     */
    public StructureMapping(BiMap<Entity, Entity> mapping) {
        this.mapping = HashBiMap.create(mapping);
    }
    
    /**
     * Initialized the StrucutreMapping with a mapping between the two given entities.
     */
    public StructureMapping(Entity e1, Entity e2) {
        this();
        mapping.put(e1, e2);
    }
    
    /**
     * @return the entity to which e is mapped, or null if the 
     * entity does not exist in the mapping.
     */
    public Entity getMapping(Entity e) {
        Entity result = mapping.get(e);
        if (result == null) {
            return mapping.inverse().get(e);
        }
        return result;
    }
    
    /**
     * Return true iff e is contained in this mapping.
     */
    public boolean hasMapping(Entity e) {
        return mapping.get(e) != null || mapping.inverse().get(e) != null;
    }
    
    /**
     * Returns a Map which has the entitiesOfInterest as keys. Each key has its bound entity in the mapping
     * as its value. If an entity of interest is not found, it's mapped to null in the returned map.
     */
    public Map<Entity, Entity> getMappingsFor(Set<Entity> entitiesOfInterest) {
        return entitiesOfInterest.stream()
                .collect(Collectors.toMap(Function.identity(), e -> getMapping(e)));
    }
    
    /**
     * @return the mappings in this structure mapping as a set of pairs of entities
     */
    public Set<Pair<Entity>> getMapping() {
        return mapping.entrySet().stream()
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }
    
    /**
     * A list of entity pairs. Each pair contains an entity that is a component of the original pattern, and
     * the corresponding entity bound to this component. 
     */
    public LList<PairOfEntities> getPatternDatumEntityPairs(Entity pattern) {
        return getMappingsFor(new HashSet<>(Util.getAllDeepComponents(pattern))).entrySet().stream()
                .map(entry -> new PairOfEntities(entry.getKey(), entry.getValue()))
                .reduce(new LList<>(), (llist, pair) -> llist.cons(pair), 
                        (llist1, llist2) -> llist1.append(llist2));
    }
        
    /**
     * Returns true iff the two mappings do not conflict (i.e. map the same
     * entity to two different entities).
     */
    public static boolean mappingsConflict(StructureMapping m1, StructureMapping m2) {
        for (Pair<Entity> binding : m1.getMapping()) {
            Entity e1 = binding.getObjs().get(0);
            Entity e2 = binding.getObjs().get(1);
            Entity m2Binding = m2.getMapping(e1);
            if (m2Binding != null && !e2.equals(m2Binding)) {
                return true;
            }
            m2Binding = m2.getMapping(e2);
            if (m2Binding != null && !e1.equals(m2Binding)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns a new StructureMapping which is the combination of the two mappings. 
     * The behavior is unspecified if the mappings conflict.
     */
    public static StructureMapping mergeMappings(StructureMapping m1, StructureMapping m2) {
        BiMap<Entity, Entity> newMapping = HashBiMap.create();
        newMapping.putAll(m1.mapping);
        newMapping.putAll(m2.mapping);
        return new StructureMapping(newMapping);
    }
    
    /**
     * Returns a new mapping which is the result of adding a binding between the two entities
     * to the existing mapping. The new binding will overwrite any previous mappings for these two entities.
     */
    public StructureMapping putMapping(Entity newEnt1, Entity newEnt2) {
        return StructureMapping.mergeMappings(this, new StructureMapping(newEnt1, newEnt2));
    }
    
    /**
     * Returns a new mapping which is the result of adding a binding between the two entities
     * to the existing mapping. If either entity is already bound, and the two entities are not already bound to each other, 
     * an IllegalArgumentException is thrown to signify the conflict.
     */
    public StructureMapping addMapping(Entity newEnt1, Entity newEnt2) {
        StructureMapping newMapping = new StructureMapping(newEnt1, newEnt2);
        if (StructureMapping.mappingsConflict(this, newMapping)) {
            throw new IllegalArgumentException("Cannot add mapping between "+newEnt1+" and "+newEnt2
                    +" since it conflicts with current mapping");
        }
        return StructureMapping.mergeMappings(this, newMapping);
    }
    
    /**
     * Returns a new StructureMapping which has the binding for the entity e removed.
     * If e is not present in the mapping, this StructureMapping is returned.
     */
    public StructureMapping removeMapping(Entity e) {
        if (mapping.containsKey(e)) {
            BiMap<Entity, Entity> newMapping = HashBiMap.create(mapping);
            newMapping.remove(e);
            return new StructureMapping(newMapping);
        }
        if (mapping.inverse().containsKey(e)) {
            BiMap<Entity, Entity> newMapping = HashBiMap.create(mapping.inverse());
            newMapping.remove(e);
            return new StructureMapping(newMapping);
        }
        return this;
    }
    
}
