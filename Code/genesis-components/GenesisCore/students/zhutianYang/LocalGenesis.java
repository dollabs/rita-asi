// Updated 30 Oct 2018
// As we have suggested, you should first inspect the book.

package zhutianYang;

import connections.*;
import consciousness.*;
import constants.Markers;
import dictionary.BundleGenerator;
import expert.QuestionExpert;
import genesis.FileSourceReader;
import genesis.Genesis;
import gui.RadioButtonWithDefaultValue;
import start.Start;
import translator.Translator;
import utils.*;


/**
 * This is a personal copy of Genesis I can play with without endangering the code of others. I will also want to look
 * at the main methods in Entity, for examples of how the representational substrate works, and Generator, for examples
 * of how to go from English to Genesis's inner language and back.
 *
 * @author ztyang
 */

@SuppressWarnings("serial")
public class LocalGenesis extends Genesis {

	// Paper demos
	public static String preloadPrincipleDemo = "corpora/demos/Z Principle demo.txt";
	public static String preloadReadDemo = "corpora/demos/Z Read demo.txt";
	public static String preloadDrawDemo = "corpora/zMemory/Draw a speaker_1022_174835.txt";
	public static String preloadTestContainDemo = "corpora/zMemory/Test for Prof Winston.txt";
	public static String preloadBatteryDemo = "corpora/zMemory/Replace cellphone battery_1118_205647.txt";
	public static String preloadDrowningDemo = "corpora/Ensembles/Save drowning people.txt";
	
	// AdaLab demos
	public static String preloadDemoMico = "corpora/zMemory/Learn by mico.txt";
	public static String preloadLearnSalad = "corpora/zMemory/Learn make salad.txt";
	public static String preloadDemoSalad = "corpora/zMemory/Make Salad.txt";
	public static String preloadDemoSalad1 = "corpora/zMemory/Make an apple salad for me_0906_052802.txt";

	public static String preloadDemoRobot = "corpora/zMemory/Learn by robot.txt";
	
	public static String preloadDemoFruitSalad2 = "corpora/zMemory/Make a fruit salad_1124_202902.txt";
	public static String preloadDemoFruitSalad1 = "corpora/zMemory/Make salad for a Thanksgiving Day_1122_160410.txt";
	public static String preloadDemoFruitSalad = "corpora/zMemory/Make a fruit salad_1120_135235.txt";
	
	public static String preloadDemoBreakfastCereal = "corpora/zMemory/Make a breakfast cereal_1127_215752";
	public static String preloadDemoSeasonPasta = "corpora/zMemory/Season the pasta_1128_032936";
	
	public static String preloadLearnFruitSalad = "corpora/zMemory/Learn make fruit salad.txt";
	public static String preloadLearnBreakfastCereal = "corpora/zMemory/Learn make breakfast cereal.txt";
	public static String preloadLearnSeasonPasta = "corpora/zMemory/Learn make pasta.txt";
	public static String preloadLearnChocolateCake = "corpora/zMemory/Learn make chocolate cake.txt";

	// Genesis final demos
	public static String preloadHelpYangPlan = "corpora/zStoriesOfWar/Plan on Help Yang.txt";
	public static String preloadSayGoodbyeStories = "corpora/zMemory/Learn from say goodbye stories.txt";
	public static String preload3HelpYangStoryByGuo = "corpora/zStoriesOfWar/Story on Help Yang by Guo.txt";
	public static String preload3HelpYangStoryBySteve = "corpora/zStoriesOfWar/Story on Help Yang by Steve.txt";

	// for bed time story learner
	public static String preload36KillHuangStory = "corpora/zStoriesOfWar/Story on Kill Yuan.txt";

	// for stratagem expert
	public static String preload36KillHuang = "corpora/zStoriesOfWar/Advise on Kill Yuan.txt";
	public static String preload36KillHuangTaught = "corpora/zStoriesOfWar/Taught on Kill Yuan.txt";
	public static String preload36KillHuangPlan = "corpora/zStoriesOfWar/Plan on Kill Yuan.txt";

	// for war stories
	public static String preloadMilitray = "corpora/zStoriesMilitary/S3_Kill with borrowed knife_Huang.txt";

	// for recipe expert
	public static String preloadDemoStory = "corpora/zMemory/Learn from story.txt";
	public static String preloadDemoCollaborate = "corpora/zMemory/Learn to collaborate.txt";
	public static String preloadDemoConversation = "corpora/zMemory/Learn from conversation.txt";

	public static String preloadDemoReplace = "corpora/zMemory/Learn from replace battery stories.txt";
	public static String preloadDemoMartini = "corpora/zMemory/Learn from martini story.txt";
	public static String preloadDemoNoodles = "corpora/zMemory/Learn from noodles story.txt";

	public static String preloadDemoSingle = "corpora/zMemory/Learn from missing condition.txt";
	public static String preloadLemon = "corpora/zMemory/Replace cellphone battery_0613_215714.txt";
	public static String preloadCheck = "corpora/zMemory/Replace cellphone battery_0614_103854.txt";


	LocalProcessor localProcessor;
	RecipeExpert recipeExpert;
	RecipeLearner recipeLearner;
	StratagemExpert stratagemExpert;
	BedtimeStoryLearner bedtimeStoryLearner;
	JustDoIt justDoIt;
	RobotListener robotListener;
	StoryAligner storyAligner;

	PageHowToLearner howToLearnerPage;
	PageNoviceLearner noviceLearnerPage;
	PageStoryLearner storyLearnerPage;
	PageTranslatorGenerator translatorGeneratorPage;
	PageWordNetGUI wordNetGUIPage;
	PageWordNetGUI wordNetGUIPage2;
	PageStoryAligner storyAlignerPage;

	public LocalGenesis() {
		super();
		Mark.say("Yang's learning machines" + ZPage.ASCIIArt);
		
		getWindowGroupManager().addJComponent(getPageHowToLearner());
		getWindowGroupManager().addJComponent(getPageNoviceLearner());
		getWindowGroupManager().addJComponent(getPageStoryLearner());
//		getWindowGroupManager().addJComponent(getPageTranslatorGenerator());
//		getWindowGroupManager().addJComponent(getPageWordNetGUI());
//		getWindowGroupManager().addJComponent(getPageWordNetGUI2());
		getWindowGroupManager().addJComponent(getPageStoryAligner());
		getTextEntryBox().setEnabled(false);

		// for debugging, put in timers to see where is slow
		NewTimer timer = NewTimer.zTimer;
		timer.initialize();
//		timer.lapTime(true, "1");

		// stratagem expert communicates with interface
		Connections.wire(getTextEntryBox(), StratagemExpert.FROM_TEXT_ENTRY_BOX, getStratagemExpert());
		Connections.wire(QuestionExpert.TO_ZTY_36, getQuestionExpert(), StratagemExpert.FROM_QUESTION_EXPERT, getStratagemExpert());
		Connections.wire(StratagemExpert.TO_CLEAR_TEXT_ENTRY_BOX, getStratagemExpert(), TextEntryBox.CLEAR, getTextEntryBox());
		Connections.wire(StratagemExpert.TO_COMMENTARY, getStratagemExpert(), getCommentaryContainer());

		// bedtime story learner communicates with interface
		Connections.wire(getTextEntryBox(), BedtimeStoryLearner.FROM_TEXT_ENTRY_BOX, getBedtimeStoryLearner());
		Connections.wire(QuestionExpert.TO_ZTY_BTS, getQuestionExpert(), BedtimeStoryLearner.FROM_QUESTION_EXPERT, getBedtimeStoryLearner());
		Connections.wire(BedtimeStoryLearner.TO_CLEAR_TEXT_ENTRY_BOX, getBedtimeStoryLearner(), TextEntryBox.CLEAR, getTextEntryBox());
		Connections.wire(BedtimeStoryLearner.TO_COMMENTARY, getBedtimeStoryLearner(), getCommentaryContainer());

		// recipe expert communicates with interface
//		Connections.wire(getTextEntryBox(), RecipeExpert.FROM_TEXT_ENTRY_BOX, getRecipeExpert());
//		Connections.wire(QuestionExpert.TO_ZTY, getQuestionExpert(), RecipeExpert.FROM_QUESTION_EXPERT, getRecipeExpert());
//		Connections.wire(RecipeExpert.TO_CLEAR_TEXT_ENTRY_BOX, getRecipeExpert(), TextEntryBox.CLEAR, getTextEntryBox());
//		Connections.wire(RecipeExpert.TO_COMMENTARY, getRecipeExpert(), getCommentaryContainer());
		Connections.wire(PageNoviceLearner.TO_RECIPE_EXPERT, getPageNoviceLearner(), RecipeExpert.FROM_NOVICE_PAGE, getRecipeExpert());
		Connections.wire(RecipeExpert.TO_NOVICE_PAGE, getRecipeExpert(), PageNoviceLearner.FROM_RECIPE_EXPERT, getPageNoviceLearner());
		Connections.wire(FileSourceReader.getFileSourceReader(), PageNoviceLearner.FROM_FILE_SOURCE, getPageNoviceLearner());

		// recipe expert communicates with problem solver
//		Connections.wire(ProblemSolver.TO_EXTERNAL_CHECKER, getProblemSolver().getNovice(), RecipeExpert.FROM_PROBLEM_SOLVER, getRecipeExpert());
		timer.lapTime(true, "1");
		Connections.wire(ProblemSolver.COMMANDS, getMentalModel1().getProblemSolver(), CommandList.FROM_PROBLEM_SOLVER, getCommandList());
		timer.lapTime(true, "2");
		Connections.wire(ProblemSolver.COMMANDS, getMentalModel2().getProblemSolver(), CommandList.FROM_PROBLEM_SOLVER, getCommandList());
		timer.lapTime(true, "3");
		Connections.wire(CommandList.TO_RECIPE_EXPERT, getCommandList(), RecipeExpert.FROM_COMMAND_LIST, getRecipeExpert());

		// recipe expert communicates with robotic system
		Connections.wire(RecipeExpert.TO_ROBOT_LISTENER, getRecipeExpert(), RobotListener.FROM_RECIPE_EXPERT, getRobotListener());
		Connections.wire(RobotListener.TO_RECIPE_EXPERT, getRobotListener(), RecipeExpert.FROM_ROBOT_LISTENER, getRecipeExpert());
		timer.lapTime(true, "4");

//		timer.summarize();
	}
	
	public PageStoryAligner getPageStoryAligner() {
		if (storyAlignerPage == null) {
			storyAlignerPage = new PageStoryAligner();
			storyAligner = new StoryAligner();
			storyAlignerPage.setName("Z Story Aligner Gallery");
		}
		return storyAlignerPage;
	}
	
//	public PageWordNetGUI getPageWordNetGUI() {
//		if (wordNetGUIPage == null) {
//			wordNetGUIPage = new PageWordNetGUI();
//			wordNetGUIPage.setName("Z WordNet Lookup");
//		}
//		return wordNetGUIPage;
//	}
//	
//	public PageWordNetGUI getPageWordNetGUI2() {
//		if (wordNetGUIPage2 == null) {
//			wordNetGUIPage2 = new PageWordNetGUI();
//			wordNetGUIPage2.setName("Z WordNet Lookup 2");
//		}
//		return wordNetGUIPage2;
//	}
//
//
//	public PageTranslatorGenerator getPageTranslatorGenerator() {
//		if (translatorGeneratorPage == null) {
//			translatorGeneratorPage = new PageTranslatorGenerator();
//			translatorGeneratorPage.setName("Z Translator/Generator");
//		}
//		return translatorGeneratorPage;
//	}

	public PageHowToLearner getPageHowToLearner() {
		if (howToLearnerPage == null) {
			howToLearnerPage = new PageHowToLearner();
			howToLearnerPage.setName("Learn PS from HowTo Books");
		}
		return howToLearnerPage;
	}

	public PageNoviceLearner getPageNoviceLearner() {
		if (noviceLearnerPage == null) {
			noviceLearnerPage = new PageNoviceLearner();
			noviceLearnerPage.setName("Learn PS from Conversations");
		}
		return noviceLearnerPage;
	}

	public PageStoryLearner getPageStoryLearner() {
		if (storyLearnerPage == null) {
			storyLearnerPage = new PageStoryLearner();
			storyLearnerPage.setName("Learn PS from Stories");
		}
		return storyLearnerPage;
	}

	public RecipeExpert getRecipeExpert() {
		if (recipeExpert == null) {
			recipeExpert = new RecipeExpert();
		}
		return recipeExpert;
	}

	public RecipeLearner getRecipeLearner() {
		if (recipeLearner == null) {
			recipeLearner = new RecipeLearner();
		}
		return recipeLearner;
	}

	public StratagemExpert getStratagemExpert() {
		if (stratagemExpert == null) {
			stratagemExpert = new StratagemExpert();
		}
		return stratagemExpert;
	}

	public BedtimeStoryLearner getBedtimeStoryLearner() {
		if (bedtimeStoryLearner == null) {
			bedtimeStoryLearner = new BedtimeStoryLearner();
		}
		return bedtimeStoryLearner;
	}

	public RobotListener getRobotListener() {
		if (robotListener == null) {
			robotListener = new RobotListener();
		}
		return robotListener;
	}

	public static void setRadioButtonsUsingIdiom(String string) {
		String command = string.substring(Markers.SET_BUTTON_TEXT.length()).trim();
		int index = command.indexOf(Markers.SET_BUTTON_SEPARATOR_TEXT);
		String control = command.substring(0, index);
		String setting = command.substring(index + Markers.SET_BUTTON_SEPARATOR_TEXT.length()).trim();
		RadioButtonWithDefaultValue.getButtons().stream().forEachOrdered(f -> {
			if (control.equalsIgnoreCase(f.getText())) {
				if (setting.equalsIgnoreCase("true")) {
					Mark.say("Setting", f.getText(), "to true");
					// Transition may matter, may be listener
					f.setSelected(false);
					f.doClick();
				} else if (setting.equalsIgnoreCase("false")) {
					// f.setSelected(false);
				} else {
					Mark.say("Bad switch setting command", string);
				}
			}
		});
	}

	public static void main(String[] args) {

		LocalGenesis myGenesis = new LocalGenesis();
		myGenesis.startInFrame();
		myGenesis.GENESIS_FRAME.setBounds(30, 0, 1650, 1080);
		myGenesis.GENESIS_FRAME.setBounds(80, 0, 1650, 1000);
		
		// purge cache in case things mix up
//		Start.purgeStartCache();
//		BundleGenerator.purgeWordnetCache();
//		BundleGenerator.writeWordnetCache();
		
//		setRadioButtonsUsingIdiom("Set expert teaches novice button to true");
//		setRadioButtonsUsingIdiom("Set real robot button to true");
		
		// -------- symposium demo
//		RecipeExpert.runProblemSolving("corpora/Ensembles/Repair a phone.txt");
//		RecipeExpert.runProblemSolving("corpora/Ensembles/Save drowning people.txt");
//		RecipeExpert.runProblemSolving("corpora/Ensembles/Revenge for father.txt");
//		RecipeExpert.runProblemSolving("corpora/Ensembles/Alex reads a memoir.txt");
//		RecipeExpert.runProblemSolving("corpora/Ensembles/I read a memoir.txt");
//		RecipeExpert.runProblemSolving("corpora/Ensembles/Idenify needs.txt");
//		RecipeExpert.runProblemSolving("corpora/Ensembles/Winston sees history repeat.txt");
//		RecipeExpert.runProblemSolving("corpora/Ensembles/Use GAIN.txt");
//		RecipeExpert.runProblemSolving("corpora/Ensembles/Einstein predicts particles in light.txt");
//		RecipeExpert.runProblemSolving("corpora/Ensembles/symbols.txt");
		
		//// -------- paper demo
//		RecipeExpert.runProblemSolving(preloadDrowningDemo);
//		RecipeExpert.runProblemSolving(preloadBatteryDemo);
//		RecipeExpert.runProblemSolving(preloadReadDemo);
//		unsetRadioButtonsUsingIdiom("Set expert teaches novice button to false");
//		RecipeExpert.runProblemSolving(preloadTestContainDemo);
//		RecipeExpert.runProblemSolving(preloadDemoReplace);
//		RecipeExpert.runProblemSolving(preloadDrawDemo);


		//// -------- real robot demo
//		RecipeExpert.runProblemSolving(preloadDemoRobot);          // ------ to connect to robot demos
//		RecipeExpert.runProblemSolving(preloadDemoFruitSalad2);
//		RecipeExpert.runProblemSolving(preloadDemoFruitSalad1);
//		RecipeExpert.runProblemSolving(preloadDemoSalad1);
//		RecipeExpert.runProblemSolving(preloadDemoBreakfastCereal);
//		RecipeExpert.runProblemSolving(preloadDemoSeasonPasta);
		
//		RecipeExpert.runProblemSolving(preloadLearnFruitSalad);
//		RecipeExpert.runProblemSolving(preloadLearnBreakfastCereal);
//		RecipeExpert.runProblemSolving(preloadLearnSeasonPasta);
//		RecipeExpert.runProblemSolving(preloadLearnSalad);
		
		
		//// -------- final demo
//		RecipeExpert.runProblemSolving(preload3HelpYangStoryByGuo);
//		RecipeExpert.runProblemSolving(preload3HelpYangStoryBySteve); // works
//		RecipeExpert.runProblemSolving(preloadHelpYangPlan);  // broken
//		RecipeExpert.runProblemSolving(preloadSayGoodbyeStories);  // broken

		//// -------- development
//		RecipeExpert.runProblemSolving(preload36KillHuangStory);
//		RecipeExpert.runProblemSolving(preload36KillHuangPlan);
//		RecipeExpert.runProblemSolving(preloadHelpYangPlan);

		//// -------- 6.100 demo
//		RecipeExpert.runProblemSolving(preloadDemoStory);
//		RecipeExpert.runProblemSolving(preloadDemoMartini);


	}
}
