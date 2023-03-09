package hibaAwad;

import genesis.Genesis;
import storyProcessor.*;
import utils.Mark;
import connections.Connections;
import constants.Markers;

/*
 * Created on Mar 25, 2010
 * @author phw
 */

@SuppressWarnings("serial")
public class LocalGenesis extends Genesis {

	EastWestExpert eastWestExpert;
	CoherenceViewer coherenceViewer;

	
		MagicBox magicBox;
	
	public LocalGenesis() {
		super();
		Mark.say("LocalGenesis's constructor");
		
			Connections.wire(Markers.VIEWER, getPersonalityExpert(), new MagicBox(getMentalModel1()));
			
		// Local wiring goes here; example shown
		
		// complete story port for 
		//Connections.wire(StoryProcessor.COMPLETE_STORY_PORT, getMentalModel1(), LocalProcessor.COHERENCE_INPUT_PORT, getLocalProcessor());
		
		//example for final inferences: not really needed
		Connections.wire(StoryProcessor.FINAL_INFERENCES, getMentalModel1(), EastWestExpert.FINAL_INFERENCES, getLocalProcessor());
		
		Connections.wire(ConceptExpert.CONCEPT_ANALYSIS, getMentalModel1(), EastWestExpert.REFLECTION, getLocalProcessor());
		
		//full story analysis port for coherence as well as causal inference model
		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), EastWestExpert.FULL_STORY_INPUT_PORT, getLocalProcessor()); 
		
		// moved this to genesis plugboard:
		//Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), QuestionExpert.LEFT_STORY, getQuestionExpert1());

		getWindowGroupManager().addJComponent(getCoherenceViewer());
	//	getWindowGroupManager().addJComponent(getCausalTextView());
		
		Connections.wire(EastWestExpert.COHERENCE_DATA, getLocalProcessor(), CoherenceViewer.dataPort, getCoherenceViewer());
		Connections.wire(EastWestExpert.COHERENCE_TEXT, getLocalProcessor(), CoherenceViewer.textPort, getCoherenceViewer());
		Connections.wire(EastWestExpert.COHERENCE_LABEL, getLocalProcessor(), CoherenceViewer.labelPort, getCoherenceViewer());
		Connections.wire(EastWestExpert.COHERENCE_AXIS, getLocalProcessor(), CoherenceViewer.axisLabelPort, getCoherenceViewer());
	//	Connections.wire(LocalProcessor.CAUSAL_ANALYSIS, getLocalProcessor(), getCausalTextView());
	}

	public EastWestExpert getLocalProcessor() {
		if (eastWestExpert == null) {
			eastWestExpert = new EastWestExpert();
		}
		return eastWestExpert;
	}
	
	public CoherenceViewer getCoherenceViewer() {
		if (coherenceViewer == null) {
			coherenceViewer = new CoherenceViewer();
			coherenceViewer.setName("Coherence");
		}
		return coherenceViewer;
	}
	


	public static void main(String[] args) {

		LocalGenesis myGenesis = new LocalGenesis();
		myGenesis.startInFrame();
		Connections.getPorts(myGenesis.getLocalProcessor()).transmit(EastWestExpert.COHERENCE_TEXT, CoherenceViewer.CLEAR);
		Connections.getPorts(myGenesis.getLocalProcessor()).transmit(EastWestExpert.COHERENCE_TEXT, "Coherence");
		
		String [] labels = {"Longest chain", "Caused Events", "Number of chains"};
		Connections.getPorts(myGenesis.getLocalProcessor()).transmit(EastWestExpert.COHERENCE_AXIS, labels);

		// JFrame frame = new JFrame();
		// JButton button = new JButton("Test me");
		// frame.getContentPane().add(button);
		// frame.setVisible(true);

	}

}
