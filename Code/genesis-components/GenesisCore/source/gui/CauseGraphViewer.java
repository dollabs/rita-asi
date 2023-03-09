package gui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;

import javax.swing.JPanel;

import org.apache.commons.collections15.Transformer;


import bryanWilliams.Util;
import bryanWilliams.Coherence.*;
import connections.*;
import constants.Switch;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import frames.entities.Entity;
import frames.entities.Sequence;
import genesis.StoryAnalysis;
import storyProcessor.StoryProcessor;

public class CauseGraphViewer extends JPanel implements WiredBox {
    
    private static final long serialVersionUID = 1L;

	public static final String MENTAL_MODEL_PORT = "story analysis via mental model";
	
	private CauseGraph curCauseGraph;

    public CauseGraphViewer() {
		Connections.getPorts(this).addSignalProcessor(MENTAL_MODEL_PORT, this::processCompleteStoryAnalysis);
		setLayout(new BorderLayout());
		this.addComponentListener(new ComponentAdapter() {
	        public void componentResized(ComponentEvent e) {
	            if (curCauseGraph != null) {
	                refreshView();
	            }
	        }
		});
    }
    
	/**
	 * Digs story out of complete analysis signal and hands off to Bryan's process method
	 */
	public void processCompleteStoryAnalysis(Object o) {
		// Mark.say("Entering analysis via mental model");
		if (!(o instanceof StoryAnalysis)) {
			return;
		}
		if (Switch.showCausationGraph.isSelected()) {
		    StoryAnalysis storyAnalysis = (StoryAnalysis) o;
		    Sequence story = storyAnalysis.getStory();
		    this.curCauseGraph = getCauseGraphForStory(story);
		    refreshView();
		}
	}
	
	private void refreshView() {
        JPanel view = generateView(this.curCauseGraph);
        this.removeAll();
        view.setBackground(Color.WHITE);
        add(view, BorderLayout.CENTER);
        revalidate();
        repaint();
	}

    public CauseGraph getCauseGraphForStory(Sequence story) {
        CauseGraph fullCauseGraph = new CauseGraph(story);
		if (Switch.showCommonsenseCausationReasoning.isSelected()) {
            return fullCauseGraph;
        }
        
        Sequence explicitElements = StoryProcessor.getExplicitElements(story);
        Set<Entity> explicitEntities = Util.flattenSequence(explicitElements);
        for (Entity entity : CauseGraph.getCausalEntities(story)) {
            Sequence cause = (Sequence) entity.getSubject();
            Set<Entity> causes = new HashSet<Entity>(cause.getElements());
            Entity effect = entity.getObject();
            // If a causal relation is explicitly mentioned in the story,
            // its causes and effect are individually mentioned too
            if (explicitEntities.contains(entity)) {
                explicitEntities.addAll(causes);
                explicitEntities.add(effect);
            }
        }
        
        return fullCauseGraph.compressGraph(explicitEntities::contains);
    }
    
    private JPanel generateView(CauseGraph causeGraph) {
        DirectedGraph<CauseNode, String> digraph = causeGraph.getVisualGraph();
        Layout<CauseNode, String> kklayout = new KKLayout<CauseNode, String>(digraph); //KKLayout is best, FRLayout is also cool
        kklayout.setSize(this.getSize());
        VisualizationViewer<CauseNode,String> v = new VisualizationViewer<CauseNode,String>(kklayout);
        v.setPreferredSize(this.getSize());
        v.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<CauseNode>());
        v.getRenderContext().setEdgeLabelTransformer(new Transformer<String, String>() {
            public String transform(String label) {
                // Edge label will be arbitrary (but unique) edge number, optionally followed by a space and the
                // part of the label that should be displayed
                int spaceIndex = label.indexOf(' ');
                if (spaceIndex >= 0) {
                    return label.substring(spaceIndex);
                }
                return "";
            }
        });
        DefaultModalGraphMouse<CauseNode, String> gm = new DefaultModalGraphMouse<CauseNode, String>();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        v.setGraphMouse(gm);    
        return v;
    }
}
