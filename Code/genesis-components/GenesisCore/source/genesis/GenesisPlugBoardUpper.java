package genesis;

import connections.*;
import consciousness.*;
import constants.*;
import expert.*;
import frames.*;
import generator.Generator;
import gui.*;
import hibaAwad.EastWestExpert;
import matthewFay.ClusterProcessor;
import matthewFay.CharacterModeling.*;
import matthewFay.Exporter.ExperimentExportProcessor;
import matthewFay.StoryAlignment.AlignmentProcessor;
import matthewFay.StoryThreading.*;
import matthewFay.viewers.*;
import memory2.M2;
import mentalModels.*;
//import olgaShestopalova.PredictionExpert;
import silaSayan.StoryTeller;
import silasAast.*;
import start.*;
import expert.StatisticsExpert;
import storyProcessor.*;
import subsystems.rashi.RashisExperts;
import subsystems.rashi.SummarizeIntentions;
import subsystems.recall.StoryRecallExpert;
import subsystems.summarizer.Persuader;
import subsystems.summarizer.Summarizer;

import translator.*;
import utils.Talker;
import utils.Mark;
import zhutianYang.*;

/*
 * Created on May 31, 2013
 * @author phw
 */

public class GenesisPlugBoardUpper extends GenesisGetters {

	protected void initializeWiring() {
		initializeStoryProcessorConnections();
		initializeQuestionExpert(); // Creating commentary container ...
		initializeStoryMemory();
		initializeEscalationExpert();
		initializeOnsetConnections();
		initializeCommandAndImaginationExpert();
		initializeRest();
		initializeStageDirections();
		initializeDownStreamConnections();
		initializeGeneratorTest();
		connectStoryTellingWires();
		// initializeWhatIfExpert();
		initializeSummarizer();
//		intializePrediction();
		intializeStatistics();
		initializeAspireEngine();
		initializeProblemSolvingLearners(); // added by Zhutian for running demonstration files in TheGenesisSystem
		initializeRecipeFinder();
//		initializeRashiConnections();

	}

	private void initializeRecipeFinder() {
		Connections.wire(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getMentalModel1()
		        .getStoryProcessor(), RecipeFinder.FROM_FIRST_PERSPECTIVE, getRecipeFinder());
		Connections.wire(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getMentalModel2()
		        .getStoryProcessor(), RecipeFinder.FROM_SECOND_PERSPECTIVE, getRecipeFinder());
		Connections.wire(RecipeFinder.TO_COMMENTARY, getRecipeFinder(), getCommentaryContainer());
	}

	private void initializeAspireEngine() {
	    Connections.wire(StoryProcessor.STARTING, getMentalModel1().getStoryProcessor(), StoryProcessor.STARTING, getAspireEngine());
	    Connections.wire(StoryProcessor.RULE_PORT, getMentalModel1().getStoryProcessor(), StoryProcessor.RULE_PORT, getAspireEngine());
	}

	private void intializeStatistics() {
		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), StatisticsExpert.COMPUTE, getStatisticsExpert());
		Connections.wire(StatisticsExpert.COMMENTARY, getStatisticsExpert(), getCommentaryContainer());
	}

//	private void intializePrediction() {
//		Connections
//		        .wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getPredictionExpert1());
//		Connections
//		        .wire(StoryProcessor.INCREMENT_PORT_COMPLETE, getMentalModel1(), PredictionExpert.QUIESCENCE_PORT_COMPLETE, getPredictionExpert1());
//		Connections.wire(Markers.NEXT, GenesisGetters.getAnaphoraExpert(), getPredictionExpert1());
//		Connections.wire(PredictionExpert.WHAT_IS_NEXT_PORT, getPredictionExpert1(), getResultContainer());
//		// Connections .wire(StoryProcessor.PERSONALITY_TRAIT_PORT, getMentalModel1().getTraitExpert(),
//		// PredictionExpert.TRAIT_PORT, getPredictionExpert1());
//	}

	public void setToAlignmentMode() {
		if (Radio.alignmentButton.isSelected()) {
			Mark.say("Switching to alignment computing mode");
			setBottomPanel("Alignment Viewer");

			// boolean gapFillingHandle =
			// AlignmentViewer.gapFilling.isSelected();
			// boolean generateNiceOutputHandle =
			// AlignmentViewer.generateNiceOutput.isSelected();

			AlignmentViewer.alignmentPanel.removeAll();

			AlignmentViewer.generateNiceOutput.setSelected(true);
			AlignmentViewer.gapFilling.setSelected(true);
			Radio.alignmentButton.setSelected(true);

			// getSelectionGroup().setGuts(getBottomPanel(), "Dictionary");

			// AlignmentViewer.gapFilling.setSelected(gapFillingHandle);
			// AlignmentViewer.generateNiceOutput.setSelected(generateNiceOutputHandle);
		}
		else {
			Radio.normalModeButton.doClick();
		}
	}

	private void initializeGeneratorTest() {
		Connections.wire(Generator.TEST, Generator.getGenerator(), getResultContainer());
	}

	private void initializeStoryProcessorConnections() {

		Connections.wire(Start.PERSONA, StartPreprocessor.getStartPreprocessor(), Start.PERSONA, getStartParser());
		Connections.wire(Start.MODE, getMentalModel1(), Start.MODE, getStartParser());
		Connections.wire(Start.MODE, getMentalModel2(), Start.MODE, getStartParser());

		// Inputs

		Connections.wire(Markers.NEXT, GenesisGetters.getAnaphoraExpert(), getMentalModel1());
		Connections.wire(Markers.NEXT, GenesisGetters.getAnaphoraExpert(), getMentalModel2());

		Connections.wire(Markers.NEXT, GenesisGetters.getAnaphoraExpert(), getNewDisambiguator());

		Connections.wire(getNewDisambiguator(), getTalker());

		// Connections.wire(CauseExpert.RULE, getCauseExpert(),
		// StoryProcessor.RULE, getStoryProcessor1());
		// Connections.wire(CauseExpert.RULE, getCauseExpert(),
		// StoryProcessor.RULE, getStoryProcessor2());

		// Outputs

		// Connections.wire(StoryProcessor.FINAL_INFERENCES, getMentalModel1(), RuleViewer.FINAL_INFERENCE,
		// getInstantiatedRuleViewer1());
		// Connections.wire(getInstantiatedRuleViewer1(),
		// getElaborationViewer1());

		// Connections.wire(getMentalModel1(), getElaborationViewer1());

		// Connections.wire(StoryProcessor.FINAL_INFERENCES, getMentalModel2(), RuleViewer.FINAL_INFERENCE,
		// getInstantiatedRuleViewer2());

		Connections.wire(Markers.NEXT, getMentalModel1(), getQuestionExpert());
		Connections.wire(Markers.NEXT, getMentalModel2(), getQuestionExpert());

		// Monitoring

		// Connections.wire(StoryProcessor.RULE_PORT, getMentalModel1(), getRuleViewer1());
		// Connections.wire(StoryProcessor.RULE_PORT, getMentalModel2(), getRuleViewer2());

		// Connections.wire(StoryProcessor.RESET_CONCEPTS_PORT, getMentalModel2(),
		// ReflectionBar.CLEAR_PLOT_UNIT_BUTTONS, getPlotUnitBar2());

		// Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), getConceptViewer1());
		// Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), getConceptViewer2());

		// Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(),
		// getInstantiatedConceptViewer1());
		// Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(),
		// getInstantiatedConceptViewer2());

		// Connections.wire(ReflectionExpert.REFLECTION_ANALYSIS,
		// getReflectionExpert1(), getReflectionViewer1());
		// Connections.wire(ReflectionExpert.REFLECTION_ANALYSIS,
		// getReflectionExpert2(), getReflectionViewer2());

		// Connections.wire(ConceptExpert.INSTANTIATED_CONCEPTS, getMentalModel1(), getInstantiatedConceptViewer1());
		// Connections.wire(ConceptExpert.INSTANTIATED_CONCEPTS, getMentalModel2(), getInstantiatedConceptViewer2());

		// Connections.wire(ReflectionExpert.INSTANTIATED_REFLECTIONS, getReflectionExpert1(),
		// StoryProcessor.INCOMING_INSTANTIATIONS, getMentalModel1());
		// Connections
		// .wire(ReflectionExpert.INSTANTIATED_REFLECTIONS, getReflectionExpert2(),
		// StoryProcessor.INCOMING_INSTANTIATIONS, getMentalModel2());

		// Connections.wire(StoryProcessor.INFERENCES, getMentalModel1(), getInstantiatedRuleViewer1());
		// Connections.wire(StoryProcessor.INFERENCES, getMentalModel2(), getInstantiatedRuleViewer2());

		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), ConceptBar.CLEAR_CONCEPT_BUTTONS, getMentalModel1());
		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), ConceptBar.CLEAR_CONCEPT_BUTTONS, getMentalModel2());

		// added by Zhutian for it's easier to take screenshot without statisticsBar
		if(!zhutianYang.StoryAligner.hideStatisticsBar) {
			Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), StatisticsBar.CLEAR_COUNTS, getMentalModel1());
			Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), StatisticsBar.CLEAR_COUNTS, getMentalModel2());
		}
		
		Connections.wire(FileSourceReader.STATUS, getFileSourceReader(), getMentalModel1());
		Connections.wire(FileSourceReader.STATUS, getFileSourceReader(), getMentalModel2());

		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), getElaborationPanel());
		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), getInspectorPanel());

		// Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), getRuleViewerPanel());
		// Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(),
		// getInstantiatedRuleViewerPanel());
		// Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(),
		// getConceptsViewerPanel());
		// Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(),
		// getInstantiatedConceptViewerPanel());

		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), getOnsetPanel());
		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), getRecallPanel());
		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), getWordRecallPanel());

		// Connections.wire(StoryProcessor.CONCEPTS_VIEWER_PORT, getMentalModel1(), getConceptViewer1());
		// Connections.wire(StoryProcessor.CONCEPTS_VIEWER_PORT, getMentalModel2(), getConceptViewer2());

		// Connections.wire(StoryProcessor.STORY_NAME, getMentalModel2(), getStoryName2());

		Connections.wire(StoryProcessor.BRIEFING_PORT, getMentalModel1().getStoryProcessor(), getRealTimeBriefer());

	}

	private void initializeRest() {
		Connections.wire(Start.START_VIEWER_PORT, getStartParser(), getStartProcessingViewer());

		Connections.wire(getTextEntryBox(), StartPreprocessor.getStartPreprocessor());
		Connections.wire(getFileSourceReader(), StartPreprocessor.getStartPreprocessor());

		// These taps experimental only; do not handle idioms properly
		// Connections.wire(getTextEntryBox(), ExperimentalParserTap.DATA, getExperimentalParserTap());
		// Connections.wire(getFileSourceReader(), ExperimentalParserTap.DATA, getExperimentalParserTap());


		Connections.wire(StartPreprocessor.SELF, StartPreprocessor.getStartPreprocessor(), StartPreprocessor.getStartPreprocessor());



		Connections.wire(getTextEntryBox(), getSourceContainer());
		Connections.wire(StartPreprocessor.getStartPreprocessor(), getSourceContainer());

		// Connections.wire(getFileSourceReader(), getSourceContainer());

		// Connections.wire(getNextButton(), FileSourceReader.NEXT, getFileSourceReader());
		// Connections.wire(FileSourceReader.HAS_QUEUE, getFileSourceReader(), JButtonBox.ENABLE, getNextButton());
		// Connections.wire(getRunButton(), FileSourceReader.RUN, getFileSourceReader());
		// Connections.wire(FileSourceReader.HAS_QUEUE, getFileSourceReader(), JButtonBox.ENABLE, getRunButton());

		Connections.wire(getMovieManager(), getSourceContainer());
		Connections.wire(FileSourceReader.STATE, getFileSourceReader(), getStateMaintainer());

		if (MentalModel.USE_ORIGINAL_WIRING) {
			Connections.wire(Start.MODE, StartPreprocessor.getStartPreprocessor(), Start.MODE, getStartParser());
			Connections.wire(StartPreprocessor.getStartPreprocessor(), Start.SENTENCE, getStartParser());
			Connections.wire(Start.PARSE, getStartParser(), BasicTranslator.PROCESS, getNewSemanticTranslator());
			Connections.wire(BasicTranslator.RESULT, getNewSemanticTranslator(), StartPostprocessor.getStartPostprocessor());
		}
		else {
			Connections.wire(Start.MODE, StartPreprocessor.getStartPreprocessor(), ParserTranslator.MODE, getParserTranslator());
			Connections.wire(StartPreprocessor.getStartPreprocessor(), ParserTranslator.SENTENCE_IN, getParserTranslator());
			Connections.wire(ParserTranslator.ENTITY_OUT, getParserTranslator(), StartPostprocessor.getStartPostprocessor());
		}

		// Connections.wire(StartPreprocessor.getStartPreprocessor(), Start.SENTENCE, getStartParser());
		// Connections.wire(Start.TAP, getStartParser(), ExperimentalParserTap.DATA, getExperimentalParserTap());

		Connections.wire(getCombinator(), getLinkDisambiguator());
		Connections.wire(getHardWiredTranslator(), getLinkDisambiguator());
		Connections.wire(getDeriver(), getLinkDisambiguator());
		Connections.wire(getLinkDisambiguator(), getAnaphoraExpert());


		Connections.wire(StartPostprocessor.getStartPostprocessor(), getIdiomExpert());

		Connections.wire(getIdiomExpert(), Switch.disambiguatorSwitch);

		Connections.wire(TabbedTextViewer.TAB, getIdiomExpert(), TabbedTextViewer.TAB, getSourceContainer());
		Connections.wire(TabbedTextViewer.TAB, StartPreprocessor.getStartPreprocessor(), TabbedTextViewer.TAB, getSourceContainer());
		Connections.wire(TabbedTextViewer.TAB, getMovieManager(), TabbedTextViewer.TAB, getSourceContainer());
		StoryProcessor storyProcessor = getMentalModel1().getStoryProcessor();
		Connections.wire(TabbedTextViewer.TAB, storyProcessor, TabbedTextViewer.TAB, getSourceContainer());
		Connections.wire(StoryProcessor.COMPLETE_CONCEPTNET_JUSTIFICATION_STRING_PORT, storyProcessor, getSourceContainer());

		// Video processing
		// Connections.wire(TabbedTextViewer.TAB, getStoryProcessor1(),
		// TabbedTextViewer.TAB, getResultContainer());
		// *************************
		Connections.wire(StoryProcessor.RECORD_PORT, getMentalModel1(), getResultContainer());

		Connections.wire(StoryProcessor.COMMENTARY_PORT, getMentalModel1(), getResultContainer());
		// *****************************
		Connections.wire(StoryProcessor.RECORD_PORT, getMentalModel1(), getTalker());

		Connections.wire(StartPreprocessor.COMMENT_PORT, StartPreprocessor.getStartPreprocessor(), getResultContainer());

		// Alignment

		// Alignment New Connections!

		Connections
		        .wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), AlignmentProcessor.COMPLETE_STORY_ANALYSIS_PORT, getAlignmentProcessor());
		Connections
		        .wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel2(), AlignmentProcessor.COMPLETE_STORY_ANALYSIS_PORT2, getAlignmentProcessor());

		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor
		        .getStartPreprocessor(), AlignmentProcessor.STAGE_DIRECTION_PORT, getAlignmentProcessor());

		Connections.wire(AlignmentProcessor.INSERT_PORT, getAlignmentProcessor(), StoryProcessor.INJECT_ELEMENT, getMentalModel1());

		// Viewer Ports
		Connections.wire(getAlignmentProcessor(), getAlignmentViewer());
		Connections.wire(AlignmentProcessor.GRAPH_PORT_OUTPUT, getAlignmentProcessor(), AlignmentViewer.GRAPH_PORT, getAlignmentViewer());
		Connections
		        .wire(AlignmentProcessor.CONCEPT_ALIGNMENT_OUTPUT, getAlignmentProcessor(), AlignmentViewer.CONCEPT_ALIGNMENT_PORT, getAlignmentViewer());

		// Clustering
		Connections.wire(StoryProcessor.COMPLETE_STORY_EVENTS_PORT, getMentalModel1(), ClusterProcessor.STORY_PORT, getClusterProcessor());
		Connections.wire(StoryProcessor.COMPLETE_STORY_EVENTS_PORT, getMentalModel2(), ClusterProcessor.STORY_PORT, getClusterProcessor());

		Connections.wire(StoryProcessor.CLUSTER_STORY_PORT, getMentalModel1(), ClusterProcessor.CLUSTER_PORT, getClusterProcessor());
		Connections.wire(StoryProcessor.CLUSTER_STORY_PORT, getMentalModel2(), ClusterProcessor.CLUSTER_PORT, getClusterProcessor());

		// Threading

		// Generation
		// Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(),
		// RuleGraphProcessor.getRuleGraphProcessor());

		// Character Modelling
		Connections
		        .wire(StoryProcessor.PLOT_PLAY_BY_PLAY_PORT, getMentalModel1(), CharacterProcessor.PLOT_PLAY_BY_PLAY_PORT, getCharacterProcessor());
		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor
		        .getStartPreprocessor(), CharacterProcessor.STAGE_DIRECTION_PORT, getCharacterProcessor());
		Connections
		        .wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), CharacterProcessor.COMPLETE_STORY_ANALYSIS_PORT, getCharacterProcessor());

		// Trait Learning
		Connections.wire(StartPreprocessor.getStartPreprocessor(), TraitProcessor.getTraitProcessor());
		Connections.wire(Markers.NEXT, getAnaphoraExpert(), TraitProcessor.getTraitProcessor());
		Connections.wire(TraitProcessor.getTraitProcessor(), getTraitViewer());
		Connections.wire(CharacterViewer.TRAIT, getCharacterViewer(), ElaborationView.INSPECTOR, getMentalModel1().getInspectionView());

		// Export Processor
		Connections
		        .wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, ExperimentExportProcessor
		                .getExperimentExportProcessor());

		// Connections.wire(StoryProcessor.COMPLETE_STORY_PORT, getMentalModel1(),
		// StoryThreadingProcessor.STORY_INPUT_PORT, getStoryThreadingProcessor());
		Connections
		        .wire(StoryThreadingProcessor.COMPARISON_PORT, getStoryThreadingProcessor(), StoryThreadingViewer.COMPARISON_PORT, getStoryThreadingViewer());

		Connections.wire(Markers.VIEWER, getCoercionExpert(), getCoerceInterpreter());

//		Connections.wire(Markers.VIEWER, getAnaphoraExpert(), getRachelsPictureExpert());

		// Connect to EntityExpert

		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), EntityExpert.STORY, getEntityExpert());
		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel2(), EntityExpert.STORY, getEntityExpert());

		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), TextEntryBox.CLEAR, getTextEntryBox());

	}

	private void initializeOnsetConnections() {

		// Onset

		// Connections.wire(StoryProcessor.TO_ONSET_DETECTOR, getMentalModel1(), getOnsetDetector1());
		// Connections.wire(StoryProcessor.TO_ONSET_DETECTOR, getMentalModel2(), getOnsetDetector2());

		// Connections.wire(getOnsetDetector1(), ConceptOnsetDetector.DISCOVERY, getOnsetDetector1());
		// Connections.wire(getOnsetDetector2(), ConceptOnsetDetector.DISCOVERY, getOnsetDetector2());

		Connections.wire(StoryProcessor.ONSET_VIEWER_PORT, getMentalModel1().getStoryProcessor(), getOnsetViewer1());
		Connections.wire(StoryProcessor.ONSET_VIEWER_PORT, getMentalModel2().getStoryProcessor(), getOnsetViewer2());

		// Connections.wire(ConceptOnsetDetector.TAB, getOnsetDetector1(), TabbedTextViewer.TAB, getResultContainer());
		// Connections.wire(ConceptOnsetDetector.ALERT, getOnsetDetector1(), getResultContainer());
		// Connections.wire(ConceptOnsetDetector.TAB, getOnsetDetector2(), TabbedTextViewer.TAB, getResultContainer());
		// Connections.wire(ConceptOnsetDetector.ALERT, getOnsetDetector2(), getResultContainer());

		// Completion

		// Connections.wire(StoryProcessor.TO_COMPLETION_DETECTOR, getMentalModel1(), getConceptExpert1());
		// Connections.wire(StoryProcessor.TO_COMPLETION_DETECTOR, getMentalModel2(), getConceptExpert2());

		// Connections.wire(getConceptExpert1(), ConceptExpert.DISCOVERY,
		// getConceptExpert1());
		// Connections.wire(getConceptExpert2(), ConceptExpert.DISCOVERY,
		// getConceptExpert2());

		// **********************
		Connections.wire(ConceptExpert.TAB, getMentalModel1().getConceptExpert(), TabbedTextViewer.TAB, getResultContainer());
		Connections.wire(ConceptExpert.ENGLISH, getMentalModel1().getConceptExpert(), getResultContainer());
		Connections.wire(ConceptExpert.TAB, getMentalModel2().getConceptExpert(), TabbedTextViewer.TAB, getResultContainer());
		Connections.wire(ConceptExpert.ENGLISH, getMentalModel2().getConceptExpert(), getResultContainer());

		// Connections.wire(TabbedTextViewer.TAB, getPlotUnitProcessor(),
		// TabbedTextViewer.TAB, getResultContainer());
		// Connections.wire(getPlotUnitProcessor(), getResultContainer());
		//
		// Connections.wire(TabbedTextViewer.TAB, getPlotUnitProcessor2(),
		// TabbedTextViewer.TAB, getResultContainer());
		// Connections.wire(getPlotUnitProcessor2(), getResultContainer());

		// Connections.wire(ConceptExpert.DEBUG, getMentalModel1().getI().getConceptExpert(), this.getStartViewer());

	}

	private void initializeCommandAndImaginationExpert() {
		Connections.wire(CommandExpert.TELL, getCommandExpert(), JessicasExpert.TELL_PORT, getJessicasExpert());
		Connections.wire(JessicasExpert.COMMENTARY, getJessicasExpert(), getCommentaryContainer());
	}

	private void initializeSummarizer() {
		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), Summarizer.LEFT_INPUT, getSummarizer());
		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel2(), Summarizer.RIGHT_INPUT, getSummarizer());

		Connections.wire(Summarizer.REPORT_OUTPUT, getSummarizer(), getSummaryContainer());
		Connections.wire(TabbedTextViewer.SELECTED_TAB, getSummaryContainer(), Summarizer.SELECTED_TAB, getSummarizer());

		Connections.wire(Summarizer.REPORT_OUTPUT, getPersuader(), getRetellingContainer());

		// Connections.wire(Summarizer.SELECTED_LEFT_DESCRIPTION, getSummarizer(), ElaborationView.SUMMARY,
		// getMentalModel1().getElaborationView());
		// Connections.wire(Summarizer.SELECTED_RIGHT_DESCRIPTION, getSummarizer(), ElaborationView.SUMMARY,
		// getMentalModel2().getElaborationView());

		Connections.wire(Summarizer.SELECTED_LEFT_DESCRIPTION, getSummarizer(), ElaborationView.INSPECTOR, getMentalModel1().getInspectionView());
		Connections.wire(Summarizer.SELECTED_RIGHT_DESCRIPTION, getSummarizer(), ElaborationView.INSPECTOR, getMentalModel2().getInspectionView());

		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), Port.RESET, getSummarizer());

		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), Port.RESET, getMentalModel1().getInspectionView());
		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), Port.RESET, getMentalModel2().getInspectionView());

		Connections.wire(ConceptBar.RESET, getSummarizer(), ConceptBar.RESET, getMentalModel1().getConceptBar());
		Connections.wire(ConceptBar.RESET, getSummarizer(), ConceptBar.RESET, getMentalModel2().getConceptBar());

		Connections.wire(ConceptBar.TO_ELABORATION_VIEWER, getMentalModel1().getConceptBar(), Summarizer.LEFT_TARGET_CONCEPT, getSummarizer());
		Connections.wire(ConceptBar.TO_ELABORATION_VIEWER, getMentalModel2().getConceptBar(), Summarizer.RIGHT_TARGET_CONCEPT, getSummarizer());

		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getPersuader());

		Connections.wire(StartPreprocessor.TO_PERSUADER, StartPreprocessor.getStartPreprocessor(), Persuader.COMMAND, getPersuader());

		Connections.wire(Persuader.TO_SECOND_PERSPECTIVE, getPersuader(), StoryProcessor.INJECT_ELEMENT, getMentalModel2());
		Connections.wire(WhatIfExpert.TO_SECOND_PERSPECTIVE, getWhatIfExpert(), StoryProcessor.INJECT_ELEMENT, getMentalModel2());
		Connections.wire(WhatIfExpert.TO_SECOND_RULES, getWhatIfExpert(), StoryProcessor.INJECT_RULE, getMentalModel2());
		Connections.wire(WhatIfExpert.TO_SECOND_CONCEPTS, getWhatIfExpert(), StoryProcessor.INJECT_CONCEPT, getMentalModel2());

		Connections.wire(WhatIfExpert.HTML, getWhatIfExpert(), getResultContainer());

		// Connections.wire(CommandExpert.PERSUADE, getCommandExpert(), Persuader.COMMAND, getPersuader());

	}

	private void initializeStoryMemory() {
		// Coupling to precedent finder by vectors
		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), StoryRecallExpert.MEMORY_PORT, getStoryRecallExpert1());
		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel2(), StoryRecallExpert.MEMORY_PORT, getStoryRecallExpert2());

		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), StoryRecallExpert.MEMORY_PORT, getStoryRecallExpert2());
		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel2(), StoryRecallExpert.MEMORY_PORT, getStoryRecallExpert1());

		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), StoryRecallExpert.RECALL_PORT, getStoryRecallExpert1());
		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel2(), StoryRecallExpert.RECALL_PORT, getStoryRecallExpert2());

		Connections.wire(StoryRecallExpert.CONCEPTS, getStoryRecallExpert1(), getStoryRecallViewer1());
		Connections.wire(StoryRecallExpert.CONCEPTS, getStoryRecallExpert2(), getStoryRecallViewer2());

		Connections.wire(StoryRecallExpert.ENTITIES, getStoryRecallExpert1(), getStoryWordRecallViewer1());
		Connections.wire(StoryRecallExpert.ENTITIES, getStoryRecallExpert2(), getStoryWordRecallViewer2());

	}

	private void initializeQuestionExpert() {

		Connections.wire(QuestionExpert.SPEECH, getQuestionExpert(), getTalker());

		// Added by phw for introspection and explanation; Commentary port temporary, should go through consciousness mechanism
		Connections.wire(QuestionExpert.COMMENTARY, getQuestionExpert(), getCommentaryContainer());
		Connections.wire(MentalModel.COMMENTARY, getMentalModel1(), getCommentaryContainer());
		Connections.wire(MentalModel.COMMENTARY, getMentalModel2(), getCommentaryContainer());
		Connections.wire(QuestionExpert.EXPLANATION, getQuestionExpert(), getExplanationContainer());

		// added by Hiba
		Connections.wire(EastWestExpert.CAUSAL_ANALYSIS, getQuestionExpert(), getCausalTextView());
		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), Markers.RESET, getCausalTextView());
		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), getQuestionExpert());
		Connections.wire(QuestionExpert.INSERT_PORT, getQuestionExpert(), StoryProcessor.INJECT_ELEMENT, getMentalModel1());

		// New stuff added by phw for problem solving.
		Connections.wire(PortNames.SET_PANE, getQuestionExpert(), PortNames.SET_PANE, this);
		Connections.wire(QuestionExpert.TO_PHW, getQuestionExpert(), ProblemSolver.SOLVER_INPUT_PORT, getProblemSolver());
		Connections.wire(QuestionExpert.TO_JMN, getQuestionExpert(), JessicasExpert.QUESTION_PORT, getJessicasExpert()); // PlugBoardUpper
		Connections.wire(QuestionExpert.TO_DXH, getQuestionExpert(), WhatIfExpert.FROM_QUESTION_EXPERT, getWhatIfExpert());
//		Connections.wire(QuestionExpert.TO_CA, getQuestionExpert(), CarolinesExpert.QUESTION_PORT, getCarolinesExpert());
		Connections.wire(ProblemSolver.COMMENTARY, getMentalModel1().getProblemSolver(), getCommentaryContainer());
		Connections.wire(ProblemSolver.COMMENTARY, getMentalModel2().getProblemSolver(), getCommentaryContainer());

		// This is so PHW's code can call DXH's code
		Connections.wire(QuestionExpert.TO_DXH, getProblemSolver(), WhatIfExpert.FROM_QUESTION_EXPERT, getWhatIfExpert());

	}

	private void initializeEscalationExpert() {
		// Connections.wire(PlotUnitProcessor.MEMORY_PORT,
		// getPlotUnitProcessor(), getEscalationExpert());
		// Connections.wire(PlotUnitProcessor.MEMORY_PORT,
		// getPlotUnitProcessor2(), getEscalationExpert2());

		Connections.wire(ConceptExpert.CONCEPT_ANALYSIS, getMentalModel1(), getEscalationExpert1());
		Connections.wire(ConceptExpert.CONCEPT_ANALYSIS, getMentalModel2(), getEscalationExpert2());

		// Connections.wire(getEscalationExpert1(), TabbedTextViewer.TAB, getResultContainer());
		Connections.wire(getEscalationExpert1(), getResultContainer());
		// Connections.wire(getEscalationExpert2(), TabbedTextViewer.TAB, getResultContainer());
		Connections.wire(getEscalationExpert2(), getResultContainer());
	}

	// Sila. Connecting story teller to main line.
	private void connectStoryTellingWires() {

		// Connections.disconnect(Markers.NEXT, getAnaphoraExpert(),
		// StoryTeller.PLOT, getStoryTeller());

		// Connections.wire(ConceptExpert.CONCEPT_ANALYSIS,
		// getMentalModel1(), StoryTeller.CONCEPT_PORT1,
		// getStoryTeller());
		// Connections.wire(ConceptExpert.CONCEPT_ANALYSIS,
		// getConceptExpert2(), StoryTeller.CONCEPT_PORT2,
		// getStoryTeller());

		Connections.wire(StoryProcessor.INSTANTIATED_CONCEPTS, getMentalModel1(), StoryTeller.CONCEPT_PORT1, getStoryTeller());
		Connections.wire(StoryProcessor.COMPLETE_STORY_EVENTS_PORT, getMentalModel1(), StoryTeller.COMPLETE_STORY, getStoryTeller());
		Connections.wire(ConceptExpert.CONCEPT_ANALYSIS, getMentalModel1(), StoryProcessor.INCOMING_CONCEPT_ANALYSIS, getMentalModel1());
		Connections.wire(ConceptExpert.CONCEPT_ANALYSIS, getMentalModel2(), StoryProcessor.INCOMING_CONCEPT_ANALYSIS, getMentalModel2());

		Connections.wire(StoryProcessor.CONCEPT_ANALYSIS, getMentalModel1(), StoryTeller.CONCEPT_ANALYSIS, getStoryTeller());
		Connections.wire(StoryProcessor.INCREMENT_PORT, getMentalModel1(), StoryTeller.QUIESCENCE_PORT1, getStoryTeller());
		Connections.wire(StoryProcessor.INCREMENT_PORT, getMentalModel2(), StoryTeller.QUIESCENCE_PORT2, getStoryTeller());

		Connections.wire(StoryProcessor.INFERENCES, getMentalModel1(), StoryTeller.TEACHER_INFERENCES, getStoryTeller());
		Connections.wire(StoryProcessor.INFERENCES, getMentalModel2(), StoryTeller.STUDENT_INFERENCES, getStoryTeller());
		Connections.wire(StoryProcessor.NEW_INFERENCE_PORT, getMentalModel1(), StoryTeller.INCREMENT, getStoryTeller());

		Connections.wire(StoryProcessor.FINAL_INPUTS, getMentalModel1(), StoryTeller.EXPLICIT_STORY, getStoryTeller());

		Connections.wire(StoryProcessor.RULE_PORT, getMentalModel1(), StoryTeller.RULE_PORT, getStoryTeller());

		Connections.wire(StoryTeller.TEACH_RULE_PORT, getStoryTeller(), StoryProcessor.LEARNED_RULE_PORT, getMentalModel2());
		Connections.wire(StoryTeller.NEW_RULE_MESSENGER_PORT, getStoryTeller(), StoryProcessor.NEW_RULE_MESSENGER_PORT, getMentalModel2());

		// The following is for Story Viewer tab

		Connections.wire(getStoryTeller(), getStoryContainer());
		// Connections.wire(getSummaryHelper(), getStoryContainer());

		// Connections.wire(SummaryHelper.TO_STORY_TELLER, getSummaryHelper(),
		// StoryTeller.FROM_SUMMARY_HELPER, getStoryTeller());

		// Connections.wire(SummaryHelper.TO_STORY_TELLER, getSummaryHelper(), StoryTeller.FROM_SUMMARY_HELPER,
		// getStoryTeller());

		Connections.wire(StartPreprocessor.STORY_TEXT, StartPreprocessor.getStartPreprocessor(), getStoryContainer());
		Connections.wire(StartPreprocessor.STORY_TEXT, StartPreprocessor.getStartPreprocessor(), StoryTeller.PLOT_PORT, getStoryTeller());

		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), StoryTeller.STAGE_DIRECTION_PORT, getStoryTeller());

		Connections.wire(StoryTeller.CLEAR, getStoryTeller(), TabbedTextViewer.CLEAR, getStoryContainer());
	}

	private void connectNewStoryTellingWires() {

		Connections
		        .wire(StaticAudienceModeler.AUDIENCE_COMMONSENSE_OUT, getStaticAudienceModeler(), StoryPreSimulator.AUDIENCE_COMMONSENSE_IN, getStoryPresimulator());
		Connections
		        .wire(StaticAudienceModeler.AUDIENCE_REFLECTIVE_OUT, getStaticAudienceModeler(), StoryPreSimulator.AUDIENCE_REFLECTIVE_IN, getStoryPresimulator());

	}

	private void initializeStageDirections() {

		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), OnsetViewer.RESET_PORT, getOnsetViewer1());
		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), OnsetViewer.RESET_PORT, getOnsetViewer2());

		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), IDIOM, this);
		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), FileSourceReader.PAUSE, getFileSourceReader());

		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), Start.STAGE_DIRECTION_PORT, getMentalModel1());
		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), Start.STAGE_DIRECTION_PORT, getMentalModel2());

		Connections
		        .wire(StartPreprocessor.RESET_TEXT_DISPLAYS, StartPreprocessor.getStartPreprocessor(), TabbedTextViewer.CLEAR, getResultContainer());
		Connections
		        .wire(StartPreprocessor.RESET_TEXT_DISPLAYS, StartPreprocessor.getStartPreprocessor(), TabbedTextViewer.CLEAR, getSourceContainer());

		Connections.wire(StartPreprocessor.RESET_TEXT_DISPLAYS, StartPreprocessor
		        .getStartPreprocessor(), TabbedTextViewer.CLEAR, getCommentaryContainer());

		Connections.wire(StartPreprocessor.RESET_TEXT_DISPLAYS, StartPreprocessor
		        .getStartPreprocessor(), TabbedTextViewer.CLEAR, getExplanationContainer());

		Connections.wire(StartPreprocessor.RESET_TEXT_DISPLAYS, StartPreprocessor
		        .getStartPreprocessor(), TabbedTextViewer.CLEAR, getExplanationContainer());
		Connections.wire(StartPreprocessor.RESET_TEXT_DISPLAYS, StartPreprocessor.getStartPreprocessor(), TabbedTextViewer.CLEAR, getTalker());

		// Connections.wire(ConceptBar.CONCEPT_BUTTON, getConceptExpert2(), ConceptBar.CONCEPT_BUTTON,
		// getPlotUnitBar2());
		// Connections.wire(ConceptBar.TO_ELABORATION_VIEWER, getPlotUnitBar2(),
		// ElaborationViewer.FROM_CONCEPT_BAR, getElaborationViewer2());
		// Connections.wire(ConceptBar.RESET, getConceptExpert2(), ConceptBar.RESET, getPlotUnitBar2());

		// Wires for ConceptBar in Instantiated Rule Viewer
		Connections.wire(ConceptBar.CONCEPT_BUTTON, getMentalModel1(), ConceptBar.CONCEPT_BUTTON, getInstantiatedRuleConceptBar1());
		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor
		        .getStartPreprocessor(), ConceptBar.CLEAR_CONCEPT_BUTTONS, getInstantiatedRuleConceptBar1());

		Connections.wire(ConceptBar.CONCEPT_BUTTON, getMentalModel2(), ConceptBar.CONCEPT_BUTTON, getInstantiatedRuleConceptBar2());
		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor
		        .getStartPreprocessor(), ConceptBar.CLEAR_CONCEPT_BUTTONS, getInstantiatedRuleConceptBar2());

		// Wires for ConceptBar in simple(raw) Rule Viewer
		Connections.wire(ConceptBar.CONCEPT_BUTTON, getMentalModel1(), ConceptBar.CONCEPT_BUTTON, getRuleConceptBar1());
		Connections
		        .wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), ConceptBar.CLEAR_CONCEPT_BUTTONS, getRuleConceptBar1());

		Connections.wire(ConceptBar.CONCEPT_BUTTON, getMentalModel2(), ConceptBar.CONCEPT_BUTTON, getRuleConceptBar2());
		Connections
		        .wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), ConceptBar.CLEAR_CONCEPT_BUTTONS, getRuleConceptBar2());

		Connections
		        .wire(StartPreprocessor.INCONSISTENCY, StartPreprocessor
		                .getStartPreprocessor(), StartPreprocessor.INCONSISTENCY, InconsistencyDetector.getInconsistencyDetector());

	}

	private void initializeDownStreamConnections() {

		// Connections.wire(IdiomExpert.STORY_PROCESSOR_PORT, getIdiomExpert(), StoryProcessor.CONTROL,
		// getMentalModel1());
		Connections.wire(IdiomExpert.DESCRIBE, getIdiomExpert(), StoryProcessor.DESCRIBE, getMentalModel1());
		// Connections.wire(IdiomExpert.START_PARSER_PART, getIdiomExpert(), Start.MODE, this.getStartParser());

		// Connections.wire(IdiomExpert.STORY_PROCESSOR_PORT, getIdiomExpert(), StoryProcessor.CONTROL,
		// getMentalModel2());
		Connections.wire(IdiomExpert.DESCRIBE, getIdiomExpert(), StoryProcessor.DESCRIBE, getMentalModel2());

		// Connections.wire(Start.SELF, getStartParser(), Start.MODE, getStartParser());

		Connections.wire(Port.UP, Switch.disambiguatorSwitch, getStartDisambiguator());
		Connections.wire(Port.DOWN, Switch.disambiguatorSwitch, getAnaphoraExpert());
		Connections.wire(getStartDisambiguator(), getAnaphoraExpert());

		Connections.wire(Markers.NEXT, getAnaphoraExpert(), getStartViewer());
		Connections.wire(Markers.NEXT, getAnaphoraExpert(), getDistributionBox());

		Connections.wire(Markers.NEXT, getAnaphoraExpert(), getQuestionExpert());
		Connections.wire(Markers.NEXT, getQuestionExpert(), getCommandExpert());
		Connections.wire(Markers.NEXT, getCommandExpert(), getDescribeExpert());
		Connections.wire(Markers.NEXT, getDescribeExpert(), getCauseExpert());
		Connections.wire(Markers.NEXT, getCauseExpert(), getBeliefExpert());
		Connections.wire(Markers.NEXT, getBeliefExpert(), getGoalExpert());
		Connections.wire(Markers.NEXT, getGoalExpert(), getIntentionExpert());
		Connections.wire(Markers.NEXT, getIntentionExpert(), getPersuationExpert());
		Connections.wire(Markers.NEXT, getPersuationExpert(), getCoercionExpert());
		Connections.wire(Markers.NEXT, getCoercionExpert(), getTimeExpert());
		Connections.wire(Markers.NEXT, getTimeExpert(), getRoleExpert());
		Connections.wire(Markers.NEXT, getRoleExpert(), getTrajectoryExpert());
//		Connections.wire(Markers.NEXT, getTrajectoryExpert(), getTransitionExpert());
//		Connections.wire(Markers.NEXT, getTransitionExpert(), getTransferExpert());
		Connections.wire(Markers.NEXT, getTransferExpert(), getSocialExpert());
		Connections.wire(Markers.NEXT, getSocialExpert(), getComparisonExpert());
		Connections.wire(Markers.NEXT, getComparisonExpert(), getStateExpert());
		Connections.wire(Markers.NEXT, getStateExpert(), getMoodExpert());
		Connections.wire(Markers.NEXT, getMoodExpert(), getPersonalityExpert());
		Connections.wire(Markers.NEXT, getPersonalityExpert(), getPropertyExpert());
		Connections.wire(Markers.NEXT, getPropertyExpert(), getPartExpert());
		Connections.wire(Markers.NEXT, getPartExpert(), getPossessionExpert());
		Connections.wire(Markers.NEXT, getPossessionExpert(), getJobExpert());
		Connections.wire(Markers.NEXT, getJobExpert(), getAgentExpert());
		Connections.wire(Markers.NEXT, getAgentExpert(), getThreadExpert());
		Connections.wire(Markers.NEXT, getThreadExpert(), getPredictionExpert());

		Connections.wire(Markers.LOOP, getCauseExpert(), getCauseExpert());
		Connections.wire(Markers.LOOP, getBeliefExpert(), getCauseExpert());
		Connections.wire(Markers.LOOP, getGoalExpert(), getCauseExpert());
		Connections.wire(Markers.LOOP, getPersuationExpert(), getCauseExpert());
		Connections.wire(Markers.LOOP, getCoercionExpert(), getCauseExpert());
		Connections.wire(Markers.LOOP, getTimeExpert(), getCauseExpert());

		Connections.wire(StoryProcessor.NEW_ELEMENT_PORT, getMentalModel1(), CauseExpert.FROM_STORY_PORT, getCauseExpert());

		Connections.wire(Markers.PATH, getTrajectoryExpert(), getPathExpert());
		Connections.wire(Markers.PATH, getPathExpert(), getPathElementExpert());
		Connections.wire(Markers.PATH, getPathElementExpert(), getPlaceExpert());
		// Connections.wire(Markers.NEXT, getPlaceExpert(), getTransitionExpert());

		Connections.wire(Markers.DIRECT, getStateExpert(), getPathElementExpert());

		// Connections.wire(getStartDisambiguator(), getStartViewer());
		Connections.wire(getLinkDisambiguator(), getLinkViewer());
		Connections.wire(M2.PORT_TALKER, getM2(), getLinkDisambiguator());
		Connections.wire(getLinkDisambiguator(), getInternalToEnglishTranslator());
		Connections.wire(SimpleGenerator.DISAMBIGUATED, getLinkDisambiguator(), SimpleGenerator.DISAMBIGUATED, getSimpleGenerator());
		Connections.wire(SimpleGenerator.DISAMBIGUATED, getStartDisambiguator(), SimpleGenerator.DISAMBIGUATED, getSimpleGenerator());

		Connections.wire(getSimpleGenerator(), getTalker());

		Connections.wire(getSimpleGenerator(), this.getRemarksAdapter());

		// Connections.wire(getTalker(),TextViewer.REPLY, getTextViewer());

		// Connections.wire(getTalker(), getTextBox());

		// Connections.wire(TextEntryBox.CLEAR, getTextEntryBox(), Talker.CLEAR,
		// getTalker());

		Connections.wire(getInternalToEnglishTranslator(), getTalkBackViewer());

		// Connections.wire(QuestionProcessor.MEMORY_PORT,
		// getQuestionProcessor(), M2.PORT_STIMULUS, getM2());
		// Connections.wire(M2.PORT_RESPONSE, getM2(),
		// QuestionProcessor.MEMORY_PORT, getQuestionProcessor());

		// connect question expert to question display planel
		// Connections.wire(getQuestionProcessor(), getQuestionViewer());

		// sam's new memory stuff
		Connections.wire(M2.PORT_ENGLISH, getDistributionBox(), memorySwitch);
		Connections.wire(memorySwitch, M2.PORT_ENGLISH, getM2());
		// Connections.wire(M2.PORT_ENGLISH, getDistributionBox(),
		// M2.PORT_ENGLISH, getM2());
		// Connections.wire(M2.PORT_PREDICTIONS, getM2(), Talker.PREDICTION,
		// getTalker());

		// gui output of entire memory contents
		Connections.wire(M2.PORT_CHAINS, getM2(), getM2Viewer());
		// gui output of predictions made
		Connections.wire(M2.PORT_PREDICTIONS, getM2(), getPredictionsViewer());
		Connections.wire(M2.PORT_TALKER, getM2(), SimpleGenerator.EXPECTATION, getSimpleGenerator());

		Connections.wire(SimpleGenerator.EXPECTATION, getSimpleGenerator(), Talker.PREDICTION, getTalker());

		// wire up Rep Circles to memory
		Connections.wire(BlockFrame.FRAMETYPE, getM2(), getRepBlockViewer());
		Connections.wire(CauseFrame.FRAMETYPE, getM2(), getRepCauseViewer());
		Connections.wire(ForceFrame.FRAMETYPE, getM2(), getRepForceViewer());
		Connections.wire(ForceFrame.FRAMETYPE, getM2(), getRepCoerceViewer());
		Connections.wire((String) RecognizedRepresentations.TIME_REPRESENTATION, getM2(), getRepTimeViewer());
		// Connections.wire(RoleFrame.FRAMETYPE, getM2(), getRepRoleViewer());
		Connections.wire(GeometryFrame.FRAMETYPE, getM2(), getRepGeometryViewer());
		Connections.wire((String) RecognizedRepresentations.PATH_THING, getM2(), getRepPathElementViewer());
		Connections.wire(PlaceFrame.FRAMETYPE, getM2(), getRepPlaceViewer());
		// Connections.wire(TrajectoryFrame.FRAMETYPE, getM2(),
		// getRepTrajectoryViewer());
		Connections.wire(TransitionFrame.FRAMETYPE, getM2(), getRepTransitionViewer());
		Connections.wire(TransferFrame.FRAMETYPE, getM2(), getRepTransferViewer());
		Connections.wire(ActionFrame.FRAMETYPE, getM2(), getRepActionViewer());
		Connections.wire((String) RecognizedRepresentations.SOCIAL_REPRESENTATION, getM2(), getRepSocialViewer());
		Connections.wire(MentalStateFrame.FRAMETYPE, getM2(), getRepMoodViewer());

		// wire Rep Circles to Chain display panel for mouse-over effect
		// Connections.wire(getSomMouseMonitor(), getChainViewer()); // single
		// // mouse
		// // listener
		// Connections.wire(getRepBlockViewer(), getSomMouseMonitor());
		// Connections.wire(getRepCauseViewer(), getSomMouseMonitor());
		// Connections.wire(getRepCoerceViewer(), getSomMouseMonitor());<
		// Connections.wire(getRepTimeViewer(), getSomMouseMonitor());
		// Connections.wire(getRepForceViewer(), getSomMouseMonitor());
		// Connections.wire(getRepGeometryViewer(), getSomMouseMonitor());
		// Connections.wire(getRepPathElementViewer(), getSomMouseMonitor());
		// Connections.wire(getRepPlaceViewer(), getSomMouseMonitor());
		// Connections.wire(getRepRoleViewer(), getSomMouseMonitor());
		// Connections.wire(getRepMoodViewer(), getSomMouseMonitor());
		// Connections.wire(getRepTrajectoryViewer(), getSomMouseMonitor());
		// Connections.wire(getRepTransitionViewer(), getSomMouseMonitor());
		// Connections.wire(getRepTransferViewer(), getSomMouseMonitor());
		// Connections.wire(getRepMoodViewer(), getSomMouseMonitor());
		// Connections.wire(getRepActionViewer(), getSomMouseMonitor());
		// Connections.wire(getRepSocialViewer(), getSomMouseMonitor());
		// Connections.wire(getPredictionsViewer(), getLinkViewer());

		// Connections.wire(Markers.VIEWER, getRachelsPictureExpert(), getQueuingPictureBox());
		// Connections.wire(getQueuingPictureBox(), getPictureBlinker());

		Connections.wire(ButtonSwitchBox.OFF, getSyntaxToSemanticsSwitchBox(), HardWiredTranslator.PROCESS, getHardWiredTranslator());

	}
	
	private void initializeProblemSolvingLearners() {
		Connections.wire(PageNoviceLearner.TO_RECIPE_EXPERT, getPageNoviceLearner(), RecipeExpert.FROM_NOVICE_PAGE, getRecipeExpert());
		Connections.wire(RecipeExpert.TO_NOVICE_PAGE, getRecipeExpert(), PageNoviceLearner.FROM_RECIPE_EXPERT, getPageNoviceLearner());
		Connections.wire(FileSourceReader.getFileSourceReader(), PageNoviceLearner.FROM_FILE_SOURCE, getPageNoviceLearner());
	}

	/**
	* 
	*/
	private void initializeRashiConnections() {

		Mark.say("Entering RASHI wiring");

		// This one connects start preprocessor and passes signal when reader encounters "Start experiment."
		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), Start.STAGE_DIRECTION_PORT, getRashiExperts());

		// This one connects a new port (as of 13 Jan 2015) from the story processor to your processor. Delivers the
		// story processor itself.

		Connections.wire(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getMentalModel1()
		        .getStoryProcessor(), StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getRashiExperts());

		Connections.wire(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getMentalModel1()
		        .getStoryProcessor(), StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getMentalModel2());

		// For question triggering "identifying modulation"/ Question expert
		// Connections.wire(QuestionExpert.TO_SB, getQuestionExpert(), LocalProcessorForSuri.TRIGGER_PORT,
		// getLocalProcessor());

		// Suri, look at this
		Connections.wire(RashisExperts.MY_OUTPUT_PORT, getRashiExperts(), StoryProcessor.INJECT_ELEMENT, getMentalModel2());

		// This was added 12/18 to allow for generating new stories based upon missing concepts
		Connections.wire(SummarizeIntentions.MY_OUTPUT_PORT, getSummarizerOfIntentions(), StoryProcessor.INJECT_ELEMENT, getMentalModel1());

		// Connections.wire(SummarizeIntentions.MY_OUTPUT_PORT, getSummarizerOfIntentions(),
		// StoryProcessor.INJECT_ELEMENT, getMentalModel2());

		Connections.wire(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getMentalModel2()
		        .getStoryProcessor(), StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getSummarizerOfIntentions());

		// TODO: 11/09: wire from local processor to input port of mental model 2 in the same format as
		// what it outputs (rules, concepts) .Trivial test case: take output from here (what processor
		// is initially handed (the complete signal) and pass that right back into a mental model
		// to be processed. take it in some mutated way and put it back into mental model 2.
		// define, use a predefined, port from local processor that is the same port that's hooked up
		// to mental model 2. and then send some object onto that port in local processor.

		Connections.wire(RashisExperts.MY_OUTPUT_PORT, getRashiExperts(), SummarizeIntentions.MY_INPUT_PORT, getSummarizerOfIntentions());
		Connections.wire(RashisExperts.MY_OUTPUT_PORT, getRashiExperts(), MentalModel.INJECT_STORY, getMentalModel2());

		Connections.wire(SummarizeIntentions.SEND_AUTHOR_PORT, getSummarizerOfIntentions(), RashisExperts.GET_AUTHOR_PORT, getRashiExperts());

		// (4/10/18) get sources
		Connections.wire(RashisExperts.MY_SOURCES_PORT, getRashiExperts(), SummarizeIntentions.MY_SOURCES_PORT, getSummarizerOfIntentions());

		// Added this
		Connections.wire(Start.START_VIEWER_PORT, Start.getStart(), Start.START_VIEWER_PORT, getRashiExperts());

		// ---- Output to GUI, not console.

		// Connections.getPorts(this).addSignalProcessor(MY_PORT, this::processStoryProcessor);

		Connections.wire(RashisExperts.COMMENTARY, getRashiExperts(), getCommentaryContainer());
		Connections.wire(SummarizeIntentions.COMMENTARY, getSummarizerOfIntentions(), getCommentaryContainer());

		// -----

		Mark.say("Exit RASHI wiring");

	}

}
