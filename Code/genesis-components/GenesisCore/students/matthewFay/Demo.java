package matthewFay;

import generator.Generator;

import java.util.*;

import javax.swing.JFrame;

import frames.entities.Entity;
import frames.entities.Sequence;
import matthewFay.Depricated.SequenceAligner;
import matthewFay.StoryAlignment.Alignment;
import matthewFay.StoryAlignment.RankedSequenceAlignmentSet;
import translator.BasicTranslator;
import utils.*;

@SuppressWarnings({ "unused", "deprecation" })
public class Demo {

	public static Sequence ApproachStory() {

		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		Sequence story1 = new Sequence();
		try {
			story1.addElement(basicTranslator.translate("Start Story titled \"Approach Story\"").getElement(0));
			story1.addElement(basicTranslator.translate("Paul is a person").getElement(0));
			story1.addElement(basicTranslator.translate("Jill is a person").getElement(0));
			story1.addElement(basicTranslator.translate("Paul is far from Jill.").getElement(0));
			story1.addElement(basicTranslator.translate("Paul approaches Jill.").getElement(0));
			story1.addElement(basicTranslator.translate("Paul is near Jill.").getElement(0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story1;
		
	}

	public static Sequence CarryStory() {

		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		Sequence story1 = new Sequence();
		try {
			story1.addElement(basicTranslator.translate("Start Story titled \"Carry Story\"").getElement(0));
			story1.addElement(basicTranslator.translate("Matt is a person").getElement(0));
			story1.addElement(basicTranslator.translate("Mary is a person").getElement(0));
			story1.addElement(basicTranslator.translate("The ball is an object").getElement(0));
			story1.addElement(basicTranslator.translate("Matt controls the ball.").getElement(0));
			story1.addElement(basicTranslator.translate("Matt carries the ball.").getElement(0));
			story1.addElement(basicTranslator.translate("Matt approaches Mary.").getElement(0));
			story1.addElement(basicTranslator.translate("Matt is near Mary.").getElement(0));
			story1.addElement(basicTranslator.translate("The ball is near Mary.").getElement(0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story1;
	}

	public static Sequence KickStory() {

		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		Sequence story1 = new Sequence();
		try {
			story1.addElement(basicTranslator.translate("Start Story titled \"Kick Story\"").getElement(0));
			story1.addElement(basicTranslator.translate("Matt is a person").getElement(0));
			story1.addElement(basicTranslator.translate("The ball is an object").getElement(0));
			story1.addElement(basicTranslator.translate("Matt walks towards the ball").getElement(0));
			story1.addElement(basicTranslator.translate("Matt Kicks the ball").getElement(0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story1;
	}

	public static Sequence VerboseGive() {

		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		Sequence story1 = new Sequence();
		try {
			story1.addElement(basicTranslator.translate("Start Story titled \"Verbose Give Story\"").getElement(0));
			story1.addElement(basicTranslator.translate("Matt is a person").getElement(0));
			story1.addElement(basicTranslator.translate("Mary is a person").getElement(0));
			story1.addElement(basicTranslator.translate("Matt controls the ball").getElement(0));
			story1.addElement(basicTranslator.translate("Matt controls the ball").getElement(0));
			story1.addElement(basicTranslator.translate("Matt controls the ball").getElement(0));
			story1.addElement(basicTranslator.translate("Matt controls the ball").getElement(0));
			story1.addElement(basicTranslator.translate("Matt walks toward Mary").getElement(0));
			story1.addElement(basicTranslator.translate("Mary walks toward Matt").getElement(0));
			story1.addElement(basicTranslator.translate("Matt walks toward Mary").getElement(0));
			story1.addElement(basicTranslator.translate("Mary walks toward Matt").getElement(0));
			story1.addElement(basicTranslator.translate("Matt gives the ball to Mary").getElement(0));
			story1.addElement(basicTranslator.translate("Matt gives the ball to Mary").getElement(0));
			story1.addElement(basicTranslator.translate("Mary controls the ball").getElement(0));
			story1.addElement(basicTranslator.translate("Mary controls the ball").getElement(0));
			story1.addElement(basicTranslator.translate("Mary controls the ball").getElement(0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story1;
	}

	public static Sequence ComplexGapStory() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		Sequence story1 = new Sequence();
		try {
			story1.addElement(basicTranslator.translate("Start Story titled \"Complex Gap Story\"").getElement(0));
			story1.addElement(basicTranslator.translate("Matt is a person").getElement(0));
			story1.addElement(basicTranslator.translate("Mary is a person").getElement(0));
			story1.addElement(basicTranslator.translate("Tim is a person").getElement(0));
			story1.addElement(basicTranslator.translate("Mark is a person").getElement(0));
			story1.addElement(basicTranslator.translate("The ball is an object").getElement(0));
			story1.addElement(basicTranslator.translate("Matt controls the ball").getElement(0));
			story1.addElement(basicTranslator.translate("Matt walks toward Mary").getElement(0));
			story1.addElement(basicTranslator.translate("A gap appears").getElement(0));
			story1.addElement(basicTranslator.translate("Mary controls the ball").getElement(0));
			story1.addElement(basicTranslator.translate("Mary walks toward Tim").getElement(0));
			story1.addElement(basicTranslator.translate("A gap appears").getElement(0));
			story1.addElement(basicTranslator.translate("Tim controls the ball").getElement(0));
			story1.addElement(basicTranslator.translate("Mark walks toward Tim").getElement(0));
			story1.addElement(basicTranslator.translate("A gap appears").getElement(0));
			story1.addElement(basicTranslator.translate("Mark controls the ball").getElement(0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();

		return story1;
	}

	public static Sequence ComplexTakeStory() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		Sequence story1 = new Sequence();
		try {
			story1.addElement(basicTranslator.translate("Start Story titled \"Complex Take Story\"").getElement(0));
			story1.addElement(basicTranslator.translate("Mark is a person").getElement(0));
			story1.addElement(basicTranslator.translate("Sally is a person").getElement(0));
			story1.addElement(basicTranslator.translate("The bowl is an object").getElement(0));
			story1.addElement(basicTranslator.translate("Mark controls the bowl").getElement(0));
			story1.addElement(basicTranslator.translate("Sally walks toward Mark").getElement(0));
			story1.addElement(basicTranslator.translate("Sally takes the bowl from Mark").getElement(0));
			story1.addElement(basicTranslator.translate("Sally controls the bowl").getElement(0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();

		return story1;
	}

	public static Sequence ComplexGiveStory() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		Sequence story1 = new Sequence();
		try {
			story1.addElement(basicTranslator.translate("Start Story titled \"Complex Give Story\"").getElement(0));
			story1.addElement(basicTranslator.translate("Mark is a person").getElement(0));
			story1.addElement(basicTranslator.translate("Sally is a person").getElement(0));
			story1.addElement(basicTranslator.translate("The bowl is an object").getElement(0));
			story1.addElement(basicTranslator.translate("Mark controls the bowl").getElement(0));
			story1.addElement(basicTranslator.translate("Mark walks toward Sally").getElement(0));
			story1.addElement(basicTranslator.translate("Mark gives the bowl to Sally").getElement(0));
			story1.addElement(basicTranslator.translate("Sally controls the bowl").getElement(0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();

		return story1;
	}

	public static Sequence GapStory() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();
		Sequence story1 = new Sequence();
		try {
			Entity s1_Title = basicTranslator.translate("Start Story titled \"Gap Story\"").getElement(0);
			Entity s1_A = basicTranslator.translate("Matt is a person").getElement(0);
			Entity s1_B = basicTranslator.translate("Mary is a person").getElement(0);
			Entity s1_B_TIM = basicTranslator.translate("Tim is a person").getElement(0);
			Entity s1_C = basicTranslator.translate("The ball is an object").getElement(0);
			Entity s1_D = basicTranslator.translate("Matt controls the ball").getElement(0);
			Entity s1_E = basicTranslator.translate("Matt gives the ball to Mary").getElement(0);
			Entity s1_E_GAP = basicTranslator.translate("A gap appears").getElement(0);
			Entity s1_F = basicTranslator.translate("Mary controls the ball").getElement(0);
			Entity s1_G_TIM = basicTranslator.translate("A gap appears").getElement(0);
			Entity s1_H_TIM = basicTranslator.translate("Tim controls the ball").getElement(0);

			generator.flush();

			story1.addElement(s1_Title);
			story1.addElement(s1_A);
			story1.addElement(s1_B);
			// story1.addElement(s1_B_TIM);
			story1.addElement(s1_C);
			story1.addElement(s1_D);
			// story1.addElement(s1_E);
			story1.addElement(s1_E_GAP);
			story1.addElement(s1_F);
			// story1.addElement(s1_G_TIM);
			// story1.addElement(s1_H_TIM);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return story1;
	}

	public static Sequence GiveStory() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();
		Sequence story2 = new Sequence();
		try {
			Entity s2_Title = basicTranslator.translate("Start Story titled \"Give Story\"").getElement(0);
			Entity s2_A = basicTranslator.translate("Matt is a person").getElement(0);
			Entity s2_B = basicTranslator.translate("Molly is a person").getElement(0);
			Entity s2_C = basicTranslator.translate("The cup is an object").getElement(0);
			Entity s2_D = basicTranslator.translate("Matt controls the cup").getElement(0);
			Entity s2_E = basicTranslator.translate("Matt gives the cup to Molly").getElement(0);
			Entity s2_F = basicTranslator.translate("Molly controls the cup").getElement(0);

			generator.flush();

			story2.addElement(s2_Title);
			story2.addElement(s2_A);
			story2.addElement(s2_B);
			story2.addElement(s2_C);
			story2.addElement(s2_D);
			story2.addElement(s2_E);
			story2.addElement(s2_F);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return story2;
	}

	public static Sequence GiveStartStory() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();
		Sequence story2 = new Sequence();
		try {
			Entity s2_Title = basicTranslator.translate("Start Story titled \"Give Story\"").getElement(0);
			Entity s2_A = basicTranslator.translate("Mark is a person").getElement(0);
			Entity s2_B = basicTranslator.translate("Sally is a person").getElement(0);
			Entity s2_C = basicTranslator.translate("The cup is an object").getElement(0);
			Entity s2_D = basicTranslator.translate("Mark controls the cup").getElement(0);

			generator.flush();

			story2.addElement(s2_Title);
			story2.addElement(s2_A);
			story2.addElement(s2_B);
			story2.addElement(s2_C);
			story2.addElement(s2_D);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return story2;
	}

	public static Sequence FleeStory() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		Sequence fleeStory = new Sequence();
		try {
			Entity flee_Title = basicTranslator.translate("Start Story titled \"Flee Story\"").getElement(0);
			fleeStory.addElement(flee_Title);
			Entity flee_A = basicTranslator.translate("Mark is a person").getElement(0);
			fleeStory.addElement(flee_A);
			Entity flee_B = basicTranslator.translate("Sally is a person").getElement(0);
			fleeStory.addElement(flee_B);
			Entity flee_C = basicTranslator.translate("Mark is near Sally").getElement(0);
			fleeStory.addElement(flee_C);
			Entity flee_D = basicTranslator.translate("Sally flees from Mark").getElement(0);
			fleeStory.addElement(flee_D);
			Entity flee_E = basicTranslator.translate("Mark is far from Sally").getElement(0);
			fleeStory.addElement(flee_E);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();

		return fleeStory;
	}

	public static Sequence TakeStory() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		Sequence takeStory = new Sequence();
		try {
			Entity take_Title = basicTranslator.translate("Start Story titled \"Take Story\"").getElement(0);
			takeStory.addElement(take_Title);
			Entity take_A = basicTranslator.translate("Mark is a person").getElement(0);
			takeStory.addElement(take_A);
			Entity take_B = basicTranslator.translate("Sally is a person").getElement(0);
			takeStory.addElement(take_B);
			Entity take_C = basicTranslator.translate("The cup is an object").getElement(0);
			takeStory.addElement(take_C);
			Entity take_D = basicTranslator.translate("Mark controls the cup").getElement(0);
			takeStory.addElement(take_D);
			Entity take_E = basicTranslator.translate("Sally takes the cup from Mark").getElement(0);
			takeStory.addElement(take_E);
			Entity take_F = basicTranslator.translate("Sally controls the cup").getElement(0);
			takeStory.addElement(take_F);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();

		return takeStory;
	}

	public static Sequence ThrowCatchStory() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		Sequence throwCatchStory = new Sequence();
		try {
			Entity throw_catch_Title = basicTranslator.translate("Start Story titled \"Throw-Catch Story\"").getElement(0);
			throwCatchStory.addElement(throw_catch_Title);
			Entity throw_catch_A = basicTranslator.translate("Mark is a person").getElement(0);
			throwCatchStory.addElement(throw_catch_A);
			Entity throw_catch_B = basicTranslator.translate("Sally is a person").getElement(0);
			throwCatchStory.addElement(throw_catch_B);
			Entity throw_catch_C = basicTranslator.translate("The cup is an object").getElement(0);
			throwCatchStory.addElement(throw_catch_C);
			Entity throw_catch_D = basicTranslator.translate("Mark controls the cup").getElement(0);
			throwCatchStory.addElement(throw_catch_D);
			Entity throw_catch_E = basicTranslator.translate("Mark throws the cup towards Sally").getElement(0);
			throwCatchStory.addElement(throw_catch_E);
			Entity throw_catch_F = basicTranslator.translate("Sally catches the cup").getElement(0);
			throwCatchStory.addElement(throw_catch_F);
			Entity throw_catch_G = basicTranslator.translate("Sally controls the cup").getElement(0);
			throwCatchStory.addElement(throw_catch_G);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();

		return throwCatchStory;
	}

	public static Sequence FollowStory() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();
		Sequence followStory = new Sequence();
		try {
			Entity follow_Title = basicTranslator.translate("Start Story titled \"Follow Story\"").getElement(0);
			followStory.addElement(follow_Title);
			Entity follow_A = basicTranslator.translate("Mark is a person").getElement(0);
			followStory.addElement(follow_A);
			Entity follow_B = basicTranslator.translate("Sally is a person").getElement(0);
			followStory.addElement(follow_B);
			Entity follow_C = basicTranslator.translate("Mark is near Sally").getElement(0);
			followStory.addElement(follow_C);
			Entity follow_D = basicTranslator.translate("Sally walks away from Mark").getElement(0);
			followStory.addElement(follow_D);
			Entity follow_E = basicTranslator.translate("Mark follows Sally").getElement(0);
			followStory.addElement(follow_E);
			Entity follow_F = basicTranslator.translate("Mark is near Sally").getElement(0);
			followStory.addElement(follow_F);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();

		return followStory;
	}

	public static Sequence ExchangeStory() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		Sequence exchangeStory = new Sequence();
		try {
			Entity exchange_Title = basicTranslator.translate("Start Story titled \"Exchange Story\"").getElement(0);
			exchangeStory.addElement(exchange_Title);
			Entity exchange_A = basicTranslator.translate("Mark is a person").getElement(0);
			exchangeStory.addElement(exchange_A);
			Entity exchange_B = basicTranslator.translate("Sally is a person").getElement(0);
			exchangeStory.addElement(exchange_B);
			Entity exchange_C = basicTranslator.translate("The cup is an object").getElement(0);
			exchangeStory.addElement(exchange_C);
			Entity exchange_D = basicTranslator.translate("The bowl is an object").getElement(0);
			exchangeStory.addElement(exchange_D);
			Entity exchange_E = basicTranslator.translate("Mark controls the cup").getElement(0);
			exchangeStory.addElement(exchange_E);
			Entity exchange_F = basicTranslator.translate("Sally controls the bowl").getElement(0);
			exchangeStory.addElement(exchange_F);
			// //////Not Implemented in Start/Genesis yet
			// Thing exchange_G = translator.translate("Mark exchanges the cup for the bowl with Sally").getElement(0);
			// exchange_story.addElement(exchange_G);
			// //////////////////
			Entity exchange_G_A = basicTranslator.translate("Mark gives the cup to Sally").getElement(0);
			exchangeStory.addElement(exchange_G_A);
			Entity exchange_G_B = basicTranslator.translate("Sally gives the bowl to Mark").getElement(0);
			exchangeStory.addElement(exchange_G_B);
			// Temporarily Reversed for testing
			Entity exchange_H = basicTranslator.translate("Sally controls the cup").getElement(0);
			exchangeStory.addElement(exchange_H);
			Entity exchange_I = basicTranslator.translate("Mark controls the bowl").getElement(0);
			exchangeStory.addElement(exchange_I);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();

		return exchangeStory;
	}

	public static Sequence MediatedExchangeStory() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		Sequence exchangeStory = new Sequence();
		try {
			Entity exchange_Title = basicTranslator.translate("Start Story titled \"Mediated Exchange Story\"").getElement(0);
			exchangeStory.addElement(exchange_Title);
			Entity exchange_A = basicTranslator.translate("Mark is a person").getElement(0);
			exchangeStory.addElement(exchange_A);
			Entity exchange_B = basicTranslator.translate("Sally is a person").getElement(0);
			exchangeStory.addElement(exchange_B);
			Entity exchange_B2 = basicTranslator.translate("Mary is a person").getElement(0);
			exchangeStory.addElement(exchange_B2);
			Entity exchange_C = basicTranslator.translate("The cup is an object").getElement(0);
			exchangeStory.addElement(exchange_C);
			Entity exchange_D = basicTranslator.translate("The bowl is an object").getElement(0);
			exchangeStory.addElement(exchange_D);
			Entity exchange_E = basicTranslator.translate("Mark controls the cup").getElement(0);
			exchangeStory.addElement(exchange_E);
			Entity exchange_F = basicTranslator.translate("Sally controls the bowl").getElement(0);
			exchangeStory.addElement(exchange_F);
			// //////Not Implemented in Start/Genesis yet
			// Thing exchange_G = translator.translate("Mark exchanges the cup for the bowl with Sally").getElement(0);
			// exchange_story.addElement(exchange_G);
			// //////////////////
			Entity exchange_G_A = basicTranslator.translate("Mark gives the cup to Mary").getElement(0);
			exchangeStory.addElement(exchange_G_A);
			Entity exchange_G_B = basicTranslator.translate("Sally gives the bowl to Mary").getElement(0);
			exchangeStory.addElement(exchange_G_B);
			Entity exchange_X = basicTranslator.translate("Mary controls the cup").getElement(0);
			exchangeStory.addElement(exchange_X);
			Entity exchange_Z = basicTranslator.translate("Mary controls the bowl").getElement(0);
			exchangeStory.addElement(exchange_Z);
			Entity exchange_G_C = basicTranslator.translate("Mary gives the bowl to Mark").getElement(0);
			exchangeStory.addElement(exchange_G_C);
			Entity exchange_G_D = basicTranslator.translate("Mary gives the bowl to Sally").getElement(0);
			exchangeStory.addElement(exchange_G_D);
			// Temporarily Reversed for testing
			Entity exchange_H = basicTranslator.translate("Sally controls the cup").getElement(0);
			exchangeStory.addElement(exchange_H);
			Entity exchange_I = basicTranslator.translate("Mark controls the bowl").getElement(0);
			exchangeStory.addElement(exchange_I);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();

		return exchangeStory;
	}

	public static void SequenceAlignmentDemo() {
		Mark.say("Sequence Alignment Demo");
		Mark.say("Doing Parsing...");

		Sequence gapStory = GapStory();
		Sequence giveStory = GiveStory();

		Mark.say("Doing Alignment...");

		SequenceAligner aligner = new SequenceAligner();
		SequenceAligner.outputAlignment(aligner.align(giveStory, gapStory));
	}

	public static void RankedAlignmentDemo() {
		Mark.say("Sequence Match Demo");
		Mark.say("Doing Parsing...");

		Sequence gapStory = GapStory();
		Sequence giveStory = GiveStory();
		Sequence fleeStory = FleeStory();
		Sequence takeStory = TakeStory();
		Sequence throwCatchStory = ThrowCatchStory();
		Sequence followStory = FollowStory();
		Sequence exchangeStory = ExchangeStory();

		List<Sequence> patterns = new ArrayList<Sequence>();
		patterns.add(giveStory);
		// patterns.add(fleeStory);
		patterns.add(takeStory);
		patterns.add(throwCatchStory);
		// patterns.add(followStory);
		patterns.add(exchangeStory);

		SequenceAligner aligner = new SequenceAligner();

		Mark.say("Doing alignment...");

		Mark.say("Best Alignment Found:");
		SequenceAligner.outputAlignment(aligner.findBestAlignments(patterns, gapStory).get(0));

		Mark.say("Ranked Alignments");
		RankedSequenceAlignmentSet<Entity, Entity> alignments = aligner.findBestAlignments(patterns, gapStory);
		int rank = 1;
		for (Alignment<Entity, Entity> alignment : alignments) {
			Mark.say("***");
			Mark.say("Rank ", rank, " with score of", alignment.score, ":");
			SequenceAligner.outputAlignment(alignment);
			rank++;
			Mark.say(" ");
		}

		alignments.globalAlignment();

		JFrame frame = new JFrame("Story Alignment");

		// frame.setContentPane(GapViewer.generateTable(alignments));

		frame.pack();
		frame.setVisible(true);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void pairMatchingTest() {
		try {
			Generator generator = Generator.getGenerator();
			BasicTranslator basicTranslator = BasicTranslator.getTranslator();

			generator.setStoryMode();
			Entity s1_A = basicTranslator.translate("Matt is a person").getElement(0);
			Entity s1_B = basicTranslator.translate("Mary is a person").getElement(0);
			Entity s1_C = basicTranslator.translate("The ball is an object").getElement(0);
			Entity s1_D = basicTranslator.translate("Matt controls the ball").getElement(0);
			Entity s1_E = basicTranslator.translate("Matt gives the ball to Mary").getElement(0);
			Entity s1_F = basicTranslator.translate("Mary controls the ball").getElement(0);

			generator.flush();

			Entity s2_A = basicTranslator.translate("Mark is a person").getElement(0);
			Entity s2_B = basicTranslator.translate("Sally is a person").getElement(0);
			Entity s2_C = basicTranslator.translate("The cup is an object").getElement(0);
			Entity s2_D = basicTranslator.translate("Mark controls the cup").getElement(0);
			Entity s2_E = basicTranslator.translate("Mark gives the cup to Sally").getElement(0);
			Entity s2_F = basicTranslator.translate("Sally controls the cup").getElement(0);

			generator.flush();

			Sequence story1 = new Sequence();
			story1.addElement(s1_A);
			story1.addElement(s1_B);
			story1.addElement(s1_C);
			story1.addElement(s1_D);
			story1.addElement(s1_E);
			story1.addElement(s1_F);

			Sequence story2 = new Sequence();
			story2.addElement(s2_A);
			story2.addElement(s2_B);
			story2.addElement(s2_C);
			story2.addElement(s2_D);
			story2.addElement(s2_E);
			story2.addElement(s2_F);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// OneToOneMatcherDemo();
		// SequenceAlignerDemo();
		// RankedAlignmentDemo();
		// pairMatchingTest();

		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		// Mark.say(translator.translate("If XX is feeling stressed, then XX has history.").getElement(0).asString());
		Entity storystart = basicTranslator.translate("Mark controls the ball. Mark gives the ball to Matt. Matt controls the ball.");
		Mark.say(storystart.asString());
		Mark.say(generator.generate(storystart));
		if (true) return;

		generator.setStoryMode();

		Entity gap = basicTranslator.translate("A gap appears").getElement(0);

		Entity s2_A = basicTranslator.translate("Mark is a person").getElement(0);
		Entity s2_B = basicTranslator.translate("Sally is a person").getElement(0);
		Entity s2_C = basicTranslator.translate("The cup is an object").getElement(0);
		Entity s2_D = basicTranslator.translate("Mark controls the cup").getElement(0);
		Entity s2_E = basicTranslator.translate("Mark gives the cup to Sally").getElement(0);
		Entity s2_F = basicTranslator.translate("Sally controls the cup").getElement(0);

		generator.flush();

		Entity take_A = basicTranslator.translate("Mark is a person").getElement(0);
		Entity take_B = basicTranslator.translate("Sally is a person").getElement(0);
		Entity take_C = basicTranslator.translate("The cup is an object").getElement(0);
		Entity take_D = basicTranslator.translate("Mark controls the cup").getElement(0);
		Entity take_E = basicTranslator.translate("Sally takes the cup from Mark").getElement(0);
		Entity take_F = basicTranslator.translate("Sally controls the cup").getElement(0);

		generator.flush();

		// Mark.say(BasicMatcher.getBasicMatcher().match(take_D, s2_E));

		Mark.say(gap.getType());
		if (gap.getType().equals("appear"))
			if (gap.functionP())
				if (gap.getSubject().isA("gap"))
					Mark.say("Success");
				else
					Mark.say("Fail @ isA gap");
			else
				Mark.say("Fail @ derivativeP");
		else
			Mark.say("Fail @ type != 'appear'");

		Mark.say(gap.asString());

		// Mark.say("Match ",s2_D.getSubject().asString()," and ",take_D.getObject().getElement(0).getSubject().asString(),": ",BasicMatcher.getBasicMatcher().match(s2_D.getSubject(),
		// take_D.getObject().getElement(0).getSubject()));
		// Mark.say("Match ",take_D.getObject().getElement(0).getSubject().asString()," and ",s2_D.getSubject().asString(),": ",BasicMatcher.getBasicMatcher().match(take_D.getObject().getElement(0).getSubject(),
		// s2_D.getSubject()));
		//
		//
		// LList<PairOfEntities> bindings = new LList<PairOfEntities>();
		// // bindings = bindings.cons(new PairOfEntities(s2_D.getSubject(), s2_D.getSubject()));
		// bindings = bindings.cons(new PairOfEntities(s2_D.getSubject(), new Thing()));
		// bindings = bindings.cons(new PairOfEntities(new Thing(), new Thing()));
		//
		// bindings = bindings.cons(new PairOfEntities(s2_D.getObject().getElement(0).getSubject(),
		// take_D.getObject().getElement(0).getSubject()));
		//
		// Mark.say(bindings);
		//
		// bindings = OneToOneMatcher.getOneToOneMatcher().match(s2_D, take_D, bindings);
		// Mark.say(bindings);
		// bindings = BasicMatcher.getBasicMatcher().match(s2_D, take_D, bindings);
		// Mark.say(bindings);
		// bindings = OneToOneMatcher.getOneToOneMatcher().match(s2_D, take_D);
		// Mark.say(bindings);
		//
		// //Outputs (<cup-441, cup-162> <mark-401, mark-54>)
		// //Expected (null)
		// //Is this correct?

	}

}
