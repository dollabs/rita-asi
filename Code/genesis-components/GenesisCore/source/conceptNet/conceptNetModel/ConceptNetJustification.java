package conceptNet.conceptNetModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A ConceptNetJustification is used to turn a piece of ConceptNet knowledge (or chain of pieces of ConceptNet Knowledge)
 * into a justification for why Genesis made a certain causal connection. 
 * 
 * I currently use ConceptNetJustification instances by attaching them as properties to the
 * Genesis entities that represent causal connections that Genesis has made with the help of ConceptNet. There are examples of this
 * in the GoalAnalyzer, StandardMatcher, and RuleEngine classes. Once story processing is complete, 
 * the StoryProcessor class sends a user-friendly String describing all of the justifications to the
 * TabbedTextViewer for the Sources tab so it can be displayed in the "ConceptNet Knowledge" pane.
 * 
 * Currently, lists of ConceptNetJustifications are attached as properties to Genesis entities. This allows for
 * multiple different chains of justifications to all explain a single causal connection.
 * 
 * @author bryanwilliams
 *
 */
public abstract class ConceptNetJustification {
    
    // Pointer to next justification in a chain of reasoning, or null if nothing follows it
    // Not used at the moment, but would be helpful in modeling CN reasoning chains
    protected final ConceptNetJustification next;
    
    public ConceptNetJustification(ConceptNetJustification next) {
        this.next = next;
    }
    
    public static String toJustificationString(List<ConceptNetJustification> justification) {
        return justification.stream()
                .map(ConceptNetJustification::getJustification)
                .collect(Collectors.joining("; "));
    }
            
    public abstract String getJustification();
}
