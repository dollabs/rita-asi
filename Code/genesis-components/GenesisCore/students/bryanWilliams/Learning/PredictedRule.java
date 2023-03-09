package bryanWilliams.Learning;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import bryanWilliams.Pair;
import bryanWilliams.Util;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;
import matchers.StructureMapper;
import matchers.StructureMapping;
import matchers.StructureMappingResult;
import matchers.Substitutor;

public class PredictedRule {    
//    private static final Comparator<String> similarityComparator = new RankingSimilarityComparator(Entity::getType, 5, true);
    
    // the cause explicitly found in the reasoning tree
    private final Entity explicitCause;
    private final ReasoningNode causeNode;
    // proposed additional causes that, along with the explicitCause, could result in the effect
    // any combination of these can be taken together to reach the effect (or none - just the explicit cause)
    private final Set<Entity> bridgeCauses;
    private final Entity effect;
    private final ReasoningNode effectNode;
    
    public PredictedRule(Entity explicitCause, ReasoningNode causeNode, Set<Entity> bridgeCauses, Entity effect, ReasoningNode effectNode) {
        super();
        this.explicitCause = explicitCause;
        this.causeNode = causeNode;
        this.bridgeCauses = new HashSet<>(bridgeCauses);
        this.effect = effect;
        this.effectNode = effectNode;
    }

    public Entity getExplicitCause() {
        return explicitCause;
    }

    public ReasoningNode getCauseNode() {
        return causeNode;
    }

    public Set<Entity> getBridgeCauses() {
        return bridgeCauses;
    }

    public Entity getEffect() {
        return effect;
    }

    public ReasoningNode getEffectNode() {
        return effectNode;
    }
    
    // TODO currently limiting bridge combos to 2
    public Set<Relation> getPossibleRules() {
        Set<Relation> possibleRules = new HashSet<>();
                
        Relation noBridgeRule = new Relation("prediction", explicitCause, effect);
        possibleRules.add(noBridgeRule);
        for (Entity bridgeCause : bridgeCauses) {
            Sequence causeSequence = new Sequence("conjuction");
            causeSequence.addElement(explicitCause);
            causeSequence.addElement(bridgeCause);
            Relation ruleWithBridge = new Relation("prediction", causeSequence, effect);
            possibleRules.add(ruleWithBridge);
        }
        
        return possibleRules;
    }
    
    private static boolean hasSimilarRelations(StructureMapping mapping, Comparator<String> comparator) {
        return mapping.getMapping().stream()
                .map(Pair::getObjs)
                .filter(objs -> objs.get(0).relationP() && objs.get(1).relationP())
                // convert each (rel1, rel2) to (type1, type2)
                .map(objs -> objs.stream().map(Entity::getType).collect(Collectors.toList()))
                .allMatch(objs -> comparator.compare(objs.get(0), objs.get(1)) == 0);
    }
     
    public Map<Relation, Entity> getSimilarRules(Sequence commonsense, int similarityRankRequirement) {
        Set<Relation> possibleRules = getPossibleRules();
        Map<Relation, Entity> similarRules = new HashMap<>();
        
        for (Relation possibleRule : possibleRules) {
            for (Entity csRule : commonsense.getElements()) {
                StructureMappingResult mappings = StructureMapper.getStructureMappings(possibleRule, csRule);
                // strip out outermost relation from mapping because its type won't match
                // commonsense rule relation's type
                StructureMappingResult strippedMappings = mappings.adjustMappings(sm -> sm.removeMapping(possibleRule));
                for (StructureMapping mapping : strippedMappings) {
                    if (hasSimilarRelations(mapping, Util.similarityComparator)) {
                        similarRules.put(possibleRule, csRule);
                        //System.out.println(possibleRule + " is similar to "+csRule);
                        //System.out.println("bindings: "+mapping);
                    }
                }
            }
        }
        
        return similarRules;
    }      
}
