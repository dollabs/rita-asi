package bryanWilliams.Coherence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

import frames.entities.Entity;
import frames.entities.Sequence;

public class OrganizationalCoherenceEvaluator {
    // Just the explicit elements in the narrative
    private final Sequence seq;
    // The full narrative - explicit elements + Genesis-supplied entities
    private final Sequence fullSeq;
    private final int seqLength;
    private final CauseGraph causeGraph;
    private final StorySection beginning;
    private final StorySection middle;
    private final StorySection end;
    
    public OrganizationalCoherenceEvaluator(Sequence s, Sequence fullSeq, CauseGraph graph) {
        seq = s;
        this.fullSeq = fullSeq;
        seqLength = s.getElements().size();
        causeGraph = graph;
        
        beginning = identifyBeginning();
        end = identifyEnd();
        middle = new StorySection(beginning.getEndIndex()+1, end.getBeginIndex()-1);
     }

    /**
     * @return a String commenting on the overall organizational coherence
     */
    public String commentOnCoherence() {
        String comment = commentOnMisplacedClassifications();
        comment += "\n";
        comment += commentOnEndConnectivity();
        return comment;
    }
    
    /**
     * @return a String commenting on the misplaced classifications within the sequence. 
     * A misplaced classification is an Entity of type "classification" that
     * does not occur in the beginning of the sequence
     */
    public String commentOnMisplacedClassifications() {
        List<Entity> misplacedClassifications = new ArrayList<>();
        for (int i = beginning.getEndIndex()+1; i < seqLength; i++) {
            Entity curEntity = seq.getElement(i);
            if (isClassificationEntity(curEntity)) {
                misplacedClassifications.add(curEntity);
            }
        }
        
        int numMisplacedClassifications = misplacedClassifications.size();
        String instance = "instances";
        if (numMisplacedClassifications == 1) {
            instance = "instance";
        }
        
        String comment = "I found "+numMisplacedClassifications+" "+ instance+" of a "
                +"classification that should be placed in the beginning of the narrative "
                +"rather than the middle or the end";
        if (numMisplacedClassifications == 0) {
            comment += ".";
        } else {
            comment += ": [";
            for (Entity classification : misplacedClassifications) {
                comment += classification.toEnglish()+", ";
            }
            // remove trailing comma and space
            comment = comment.substring(0, comment.length() - 2);
            comment += "]";
        }
        return comment;
    }
    
    /**
     * @return a String commenting on the connectivity of the end of the sequence.
     * The end of the sequence is expected to all be within the same connected component,
     * and any deviations from this norm are commented on.
     */
    public String commentOnEndConnectivity() {
        List<Entity> unconnectedEndingEntities = new ArrayList<>();
        int curEndIndex = end.getBeginIndex();
        while (!causeGraph.containsEntity(seq.getElement(curEndIndex))) {
            curEndIndex++;
        }
        // Defending against unlikely case that no ending entities appear in the causation graph
        assert curEndIndex < seqLength;
        
        CauseNode earliestEndNode = causeGraph.nodeForEntity(seq.getElement(curEndIndex));
        Set<CauseNode> endComponent = causeGraph.componentContainingNode(earliestEndNode);
        for (int i = curEndIndex; i < seqLength; i++) {
            Entity curEntity = seq.getElement(i);
            CauseNode curNode = causeGraph.nodeForEntity(curEntity);
            if (curNode != null && !endComponent.contains(curNode)) {
                unconnectedEndingEntities.add(curEntity);
            }
        }
        
        int numUnconnectedEndingEntities = unconnectedEndingEntities.size();
        String instance = "instances";
        if (numUnconnectedEndingEntities == 1) {
            instance = "instance";
        }
        
        String comment = "I found "+numUnconnectedEndingEntities+" "+ instance+" of an "
                +"entity in the end of the narrative that is not related to the primary "
                + "conflict";
        if (numUnconnectedEndingEntities == 0) {
            comment += ".";
        } else {
            comment += ": [";
            for (Entity e : unconnectedEndingEntities) {
                comment += e.toEnglish()+", ";
            }
            // remove trailing comma and space
            comment = comment.substring(0, comment.length() - 2);
            comment += "]";
        }
        return comment;
    }
    
    /**
     * @return a StorySection representing the beginning of the sequence. 
     * Beginnings are detected by examining when the chunk of classification 
     * entities end.
     */
    public StorySection identifyBeginning() {
        int curIndex = 0;
        while (belongsToBeginning(seq.getElement(curIndex))) {
            curIndex++;
            if (curIndex == seqLength) {
                // entire narrative is the "beginning"
                break;
            }
        }
        if (curIndex == 0) {
            throw new RuntimeException("Error - did not detect any beginning to the story");
        }
        return new StorySection(0, curIndex - 1);
    }
    
    /**
     * @return true if the given Entity e is in the beginning of the story 
     * (is not an action entity, or is an action entity as well as an attribute entity)
     */
    private boolean belongsToBeginning(Entity e) {
        if (!isActionEntity(e)) {
            return true;
        }
        if (isAttributeEntity(e)) {
            return true;
        }
        return false;
    }
    
    /**
     * @return a StorySection representing the end of the sequence.
     * Ends are detected using the longest chain of events in the sequence.
     * @throws UnsupportedOperationException if there are multiple maximum-length chains
     * and they cannot come to a consensus on the end
     */
    public StorySection identifyEnd() {
        Map<StorySection, Integer> candidateEndToVote = new HashMap<>();
        int numChains = 0;
        for (List<CauseNode> maxLengthChain : causeGraph.getMaxLengthChains()) { 
            StorySection candidateEnd = endFromMaxLengthChain(maxLengthChain);
            candidateEndToVote.putIfAbsent(candidateEnd, 0);
            candidateEndToVote.put(candidateEnd, candidateEndToVote.get(candidateEnd)+1);
            numChains++;
        }

        for (Entry<StorySection, Integer> entry : candidateEndToVote.entrySet()) {
            if (entry.getValue() >= Math.ceil(numChains/2.0)) {
                // StorySection has majority of votes
                return entry.getKey();
            }
        }

        throw new UnsupportedOperationException("Haven't handeled split vote for ending yet!");   
    }
    
    /**
     * @return StorySection representing the end of the sequence according to the 
     * given maximum-length chain
     */
    private StorySection endFromMaxLengthChain(List<CauseNode> maxLengthChain) {
        int begOfEndIndex;
        CauseNode finalNode = maxLengthChain.get(maxLengthChain.size() - 1);
        Entity finalEntity = finalNode.getEvent();
        
        int indexInSequence = seq.indexOf(finalEntity);
        if (indexInSequence == -1) {
            // final entity is in cause graph, but not an explicit element in the story
            int indexInFullSequence = fullSeq.indexOf(finalEntity);
            // search forwards for closest explicit element
            for (int i = indexInFullSequence; i < fullSeq.size(); i++) {
                indexInSequence = seq.indexOf(fullSeq.getElement(i));
                if (indexInSequence != -1) {
                    break;
                }
            }
            if (indexInSequence == -1) {
                // search backwards for closest explicit element
                for (int i = indexInFullSequence; i < fullSeq.size(); i--) {
                    indexInSequence = seq.indexOf(fullSeq.getElement(i));
                    if (indexInSequence != -1) {
                        break;
                    }
                }
            }
        }
        // Defending against unlikely case that finalEntity is in fullSeq and not seq
        // but does not have an explicit Entity afterwards in fullSeq
        assert indexInSequence >= 0;
                    
                    
        if (indexInSequence < (seqLength - 1)) {
            // last Entity in max length chain is not the last Entity in
            // the narrative
            begOfEndIndex = indexInSequence;
        } else {
            // last Entity in max length chain is the last Entity in
            // the narrative
            Set<CauseNode> component = causeGraph.componentContainingNode(finalNode);
            component.removeAll(maxLengthChain);
            int latestConnectedNodeNotInChainIndex = -1;
            for (CauseNode node : component) {
                int curIndex = seq.indexOf(node.getEvent());
                if (curIndex > latestConnectedNodeNotInChainIndex) {
                    latestConnectedNodeNotInChainIndex = curIndex;
                }
            }
            
            int latestBranchingNodeOnChainIndex = -1;
            for (int i = maxLengthChain.size() - 2; i >= 0; i--) {
                CauseNode curNode = maxLengthChain.get(i);
                if (curNode.getChildren().size() > 1 || curNode.getParents().size() > 1) {
                    // found branching node
                    int curIndex = seq.indexOf(curNode.getEvent());
                    if (curIndex > latestBranchingNodeOnChainIndex) {
                        latestBranchingNodeOnChainIndex = curIndex;
                    }
                    break;
                }
            }
            
            begOfEndIndex = Math.max(latestBranchingNodeOnChainIndex, latestConnectedNodeNotInChainIndex);
        }
        return new StorySection(begOfEndIndex, seqLength - 1);
    }
    
    /**
     * @return all Entities of type "classification" in the sequence
     */
    public List<Entity> getClassificationEntities() {
        return seq.stream().filter(e -> isClassificationEntity(e))
                .collect(Collectors.toList());
    }
    
    /**
     * Produces a visual graph displaying classification vs. action entities,
     * with tooltips showing the full list of types each entity possesses.
     */
    public void generateActionGraph() {        
        XYSeries begSeries = new XYSeries("Beginning", false, false);
        XYSeries midSeries = new XYSeries("Middle", false, false);
        XYSeries endSeries = new XYSeries("End", false, false);
        
        for (int i = 0; i < seqLength; i++) {
            double x = i;
            double y;
            if (isActionEntity(seq.getElement(i))) {
                y = 1;
            } else {
                y = 0.2;
            }
            if (i <= beginning.getEndIndex()) {
                begSeries.add(x, y);
                midSeries.add(x, 0);
                endSeries.add(x, 0);
            } else if (i <= middle.getEndIndex()) {
                midSeries.add(x, y);
                begSeries.add(x, 0);
                endSeries.add(x, 0);
            } else {
                endSeries.add(x, y);
                begSeries.add(x, 0);
                midSeries.add(x, 0);
            }
        }
        
        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        dataset.addSeries(begSeries);
        dataset.addSeries(midSeries);
        dataset.addSeries(endSeries);
        
        JFreeChart barChart = ChartFactory.createXYBarChart("Action Entity Distribution Throughout Narrative",
                "Entity Index", false, "Action Entity", dataset, PlotOrientation.VERTICAL, true, true, false);
                
        XYItemRenderer r = ((XYPlot) barChart.getPlot()).getRenderer();
        r.setBaseToolTipGenerator( new XYToolTipGenerator() {
            @Override
            public String generateToolTip(XYDataset dataset, int row, int col) {
                Entity elem = seq.getElement(col);
                return String.valueOf(col)+": "+elem.toEnglish()+
                        elem.getAllTypes().toString();
            }
        });
        
        ChartFrame frame = new ChartFrame(seq.getExplicitName(), barChart);
        frame.pack();
        frame.setVisible(true);
    }
    
    /**
     * @return true iff e is of type type
     */
    public static boolean isOfType(Entity e, String type) {
        return e.isA(type);
    }
    
    /**
     * @return true iff e is of type action
     */
    public static boolean isActionEntity(Entity e) {
        return isOfType(e, "action");
    }
    
    /**
     * @return true iff e is of type attribute
     */
    public static boolean isAttributeEntity(Entity e) {
        return isOfType(e, "attribute");
    }
    
    /**
     * @return true iff e is of type classification
     */
    public static boolean isClassificationEntity(Entity e) {
        return isOfType(e, "classification");
    }
}
