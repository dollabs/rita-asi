package genesis;

import java.awt.event.*;

//import adaTaylor.humor.HumorExpert;
//import carynKrakauer.*;
//import carynKrakauer.controlPanels.GridPanelDisplay;
import connections.*;
import constants.*;
import expert.*;
import gui.*;
import mentalModels.MentalModel;
import start.*;
import storyProcessor.*;
import utils.*;


/**
 * This is intended to be only the additional wires that enable GUI Created on May 8, 2010
 * 
 * @author phw
 */

public class GenesisPlugBoardLower extends GenesisPlugBoardUpper {

	protected void initializeWiring() {
		super.initializeWiring();
		initializeTalker();
		initializePanelSetters();
		initializeButtons();
		initializeBlinkingBoxes();
		initializeMiscellaneous();
//		initializeJessica();
		initializeHumor();
//		initializeCaroline();
	}

	private void initializeJessica() {
		// Moved to initialze initializeQuestionExpert in GenesisPlugBoardUpper
		// Connections.wire(QuestionExpert.TO_JMN, getQuestionggExpert(), JessicasExpert.QUESTION_PORT,
		// getJessicasExpert()); //TODO: this should be in PlugBoardUpper
		Connections.wire(JessicasExpert.DISPLAY_PORT, getJessicasExpert(), JessicasDisplay.FROM_EXPERT, getJessicasDisplay());
		Connections.wire(StoryProcessor.STORY_PROCESSOR_PORT, getMentalModel1(), JessicasExpert.STORY_PORT, getJessicasExpert());
		Connections.wire(StoryProcessor.INCREMENT_PORT_COMPLETE, getMentalModel1(), StoryProcessor.INCREMENT_PORT_COMPLETE, getJessicasExpert());
	}
	
	private void initializeHumor() {
//		Connections.wire(HumorExpert.DISPLAY_PORT, getHumorExpert(), HumorDisplay.FROM_EXPERT, getHumorDisplay());
//		Connections.wire(StoryProcessor.STORY_PROCESSOR_PORT, getMentalModel1(), HumorExpert.STORY_PORT, getHumorExpert());
//		Connections.wire(StoryProcessor.INCREMENT_PORT_COMPLETE, getMentalModel1(), StoryProcessor.INCREMENT_PORT_COMPLETE, getHumorExpert());
	}

//	private void initializeCaroline() {
//		//Connections.wire(QuestionExpert.DID_QUESTION, getQuestionExpert(), CarolinesExpert.QUESTION_PORT, getCarolinesExpert()); //TODO: this should be in PlugBoardUpper
//		//Connections.wire(CarolinesExpert.DISPLAY_PORT, getCarolinesExpert(), CarolinesDisplay.FROM_EXPERT, getCarolinesDisplay());
//		Connections.wire(StoryProcessor.STORY_PROCESSOR_PORT, getMentalModel1(), CarolinesExpert.STORY_PORT, getCarolinesExpert());
//        Connections.wire(StoryProcessor.STORY_PROCESSOR_PORT, getMentalModel2(), CarolinesExpert.STORY_PORT, getCarolinesExpert());
//		Connections.wire(StoryProcessor.INCREMENT_PORT_COMPLETE, getMentalModel1(), StoryProcessor.INCREMENT_PORT_COMPLETE, getCarolinesExpert());
//		Connections.wire(StoryProcessor.INCREMENT_PORT_COMPLETE, getCarolinesExpert(),
//				MentalModel.INJECT_STORY, getMentalModel1());
//		Connections.wire(StoryProcessor.INCREMENT_PORT_COMPLETE, getMentalModel2(), StoryProcessor.INCREMENT_PORT_COMPLETE, getCarolinesExpert());
//		Connections.wire(StoryProcessor.INCREMENT_PORT_COMPLETE, getCarolinesExpert(),
//				MentalModel.INJECT_STORY, getMentalModel2());
////		Connections.wire(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getCarolinesExpert(),
////				MentalModel.INJECT_STORY, getMentalModel2());
//	}

	private void initializeBlinkingBoxes() {
		// getImagineBlinker();

		this.getThreadBlinker();

		this.getRoleBlinker();

		this.getTrajectoryBlinker();
		this.getPathElementBlinker();
		this.getPlaceBlinker();
//		this.getTransitionBlinker();
		this.getTransferBlinker();
		this.getCauseBlinker();

		this.getGoalBlinker();
		this.getPersuationBlinker();
		this.getCoercionBlinker();

		this.getBeliefBlinker();
		this.getIntentionBlinker();
		this.getPredictionBlinker();

		this.getMoodBlinker();
		this.getPartBlinker();
		this.getPersonalityBlinker();
		this.getPropertyBlinker();
		this.getPossessionBlinker();
		this.getJobBlinker();
		this.getSocialBlinker();
		this.getTimeBlinker();
		this.getComparisonBlinker();
//		this.getPictureBlinker();

	}

	private void initializeTalker() {

		// Connections.wire(TabbedTextViewer.TAB, getTalker(), TabbedTextViewer.TAB, getResultContainer());
		Connections.wire(getTalker(), getResultContainer());
		Mark.say("Wired up talker");
	}

	private void initializePanelSetters() {
		Connections.wire(CommandExpert.IMAGINE, getCommandExpert(), SET_BOTTOM_PANEL_TO_IMAGINATION, this);
		Connections.wire(StoryProcessor.STARTING, getMentalModel1(), SET_LEFT_PANEL_TO_ONSET, this);
		// Connections.wire(getTalker(), SET_RIGHT_PANEL_TO_RESULTS, this);
		Connections.wire(PortNames.SET_PANE, StartPreprocessor.getStartPreprocessor(), PortNames.SET_PANE, this);
	}

	private void initializeMiscellaneous() {
		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), Markers.RESET, getTextEntryBox());
		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), Markers.RESET, GenesisGetters.getMentalModelViewer());
		Connections.wire(StartPreprocessor.TO_TEXT_ENTRY_BOX, StartPreprocessor.getStartPreprocessor(), TextEntryBox.PRIMER, getTextEntryBox());
	}

	private void initializeButtons() {
		ActionListener l = new ModeActionListener();
		Radio.tellStoryButton.addActionListener(l);
		Radio.calculateSimilarityButton.addActionListener(l);
		Radio.normalModeButton.addActionListener(l);
		Radio.alignmentButton.addActionListener(l);
	}

	public void setToNormalMode() {
		// Mark.say("Switching to normal mode");
		// this.disconnectStoryTellingWires();
//		disconnectSimilarityComputationWires();
		// setBottomPanel("Elaboration graph");
		// setRightPanel("Sources");
	}

//	public void setToSimilarityComputingMode() {
//		Mark.say("Switching to similarity computing mode");
//		connectSimilarityComputationWires();
//		setBottomPanel("Similarity panel");
//		getSimilarityViewer().getTabbedPane().setSelectedIndex(getSimilarityViewer().getTabbedPane().indexOfTab(SimilarityViewer.CONCEPT_GRID_LABEL));
//		getSimilarityViewer().getGridPanels().getTopPane()
//		        .setSelectedIndex(getSimilarityViewer().getGridPanels().getTopPane().indexOfTab(GridPanelDisplay.DEFINED_GRID));
//	}

	class ModeActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == Radio.normalModeButton) {
				setToNormalMode();
			}
			else if (e.getSource() == Radio.tellStoryButton) {
				setToStoryTellingMode();
			}
//			else if (e.getSource() == Radio.calculateSimilarityButton) {
//				setToSimilarityComputingMode();
//			}
			else if (e.getSource() == Radio.alignmentButton) {
				setToAlignmentMode();
			}

		}
	}

//	private void connectSimilarityComputationWires() {
//		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), SimilarityProcessor.CLEAR, getSimilarityProcessor());
//		Connections.wire(StoryProcessor.COMPLETE_STORY_EVENTS_PORT, getMentalModel1(), getSimilarityProcessor());
//		Connections.wire(ConceptExpert.CONCEPT_ANALYSIS, getMentalModel1(), getSimilarityProcessor());
//	}
//
//	private void disconnectSimilarityComputationWires() {
//		Connections.disconnect(StoryProcessor.COMPLETE_STORY_EVENTS_PORT, getMentalModel1(), getSimilarityProcessor());
//		Connections.disconnect(ConceptExpert.CONCEPT_ANALYSIS, getMentalModel1(), getSimilarityProcessor());
//	}

	public void setToStoryTellingMode() {
		// Mark.say("Switching to story telling mode");
		// this.disconnectSimilarityComputationWires();
		setRightPanel("Sources");
		setBottomPanel("Story");
	}

}
