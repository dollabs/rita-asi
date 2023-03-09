// How is story alignment score calculated?
// How is aligning stories similar or different to measuring similarity of stories?

package zhutianYang.School;

import frames.entities.Sequence;
import generator.Generator;
import matthewFay.Demo;
import matthewFay.StoryAlignment.*;
import matthewFay.Utilities.Pair;
import translator.BasicTranslator;
import utils.*;
import utils.minilisp.LList;


/*
 * Created on Apr 22, 2016
 * @author phw
 */

public class TestAlignStories {

	public static void main(String[] args) {

		// Create demo stories.
		/* ApproachStory()
		 * "Start Story titled \"Approach Story\"
			"Paul is a person
			"Jill is a person
			"Paul is far from Jill.
			"Paul approaches Jill.
			"Paul is near Jill.
		 */
		/* CarryStory()
		 * "Start Story titled \"Carry Story\"
			"Matt is a person = Paul is a person
			"Mary is a person = Jill is a person
			"The ball is an object
			"Matt controls the ball.
			"Matt carries the ball.
			"Matt approaches Mary. = Paul approaches Jill.
			"Matt is near Mary. = Paul is near Jill.
			"The ball is near Mary.
		 */
		/* KickStory()
		 * Start Story titled \"Kick Story\"
			Matt is a person
			The ball is an object
			Matt walks towards the ball
			Matt Kicks the ball
		 */
		Sequence seqA = Demo.ApproachStory();
		Sequence seqB = Demo.CarryStory(); // somehow like story A
		Sequence seqC = Demo.KickStory(); // very much like story A
		Sequence seqD = ApproachStory2(); // the same pattern as story A
//		TestAlign(seqA, seqB);
//		return;
//		
		Sequence seqE = goalStory("Amy challenge");
		Sequence seqF = goalStory2("Amy challenge");
//
//		
		TestAlign(seqE, seqF);
		
	}
	
	public static void TestAlign(Sequence seqA, Sequence seqB) {
		// Align two stories
				Aligner aligner = new Aligner();
				SortableAlignmentList sal = aligner.align(seqA, seqB);
				Mark.say("\nsortable: ");
				Mark.say(sal);
				
				// Shows which story entities match. 
				// sal actually has only one element
				SequenceAlignment bestAlignment = (SequenceAlignment) sal.get(0);
				Mark.say("\nBestAlignment\n\n");
				Mark.say(bestAlignment);
				
				LList<PairOfEntities> bestBindings = bestAlignment.bindings;
				Mark.say("\nBest entity binding\n\n");
				for (PairOfEntities p : bestBindings) {
					Mark.say("Entity binding:", p);
				}

				// Shows which story elements match.
				Mark.say("\nBest element bindings\n\n");
				bestAlignment.stream().forEachOrdered(e -> Mark.say("Element binding:\n", ((Pair) e).a, "\n", ((Pair) e).b));
				
				// How is story alignment score calculated?
				Mark.say("\n>>> seqA, seqB"); // the same score as align(seqB, seqA);
				sal.stream().forEachOrdered(a -> Mark.say("Score:", a.score));
				
				return;
				
//				Sequence seqC = seqA;
//				Sequence seqD = seqA;
//				
//				
//				sal = aligner.align(seqA, seqC);
//				Mark.say("\n>>> seqA, seqC");
//				sal.stream().forEachOrdered(a -> Mark.say("Score:", a.score));
//				
//				sal = aligner.align(seqA, seqD); // Score: 0.00999999 = 0
//				Mark.say("\n>>> seqA, seqD");
//				sal.stream().forEachOrdered(a -> Mark.say("Score:", a.score)); 
//
//				sal = aligner.align(seqA, seqA);
//				Mark.say("\n>>> seqA, seqA");
//				sal.stream().forEachOrdered(a -> Mark.say("Score:", a.score));
//				
//				sal = aligner.align(seqD, seqD);
//				Mark.say("\n>>> seqD, seqD");
//				sal.stream().forEachOrdered(a -> Mark.say("Score:", a.score));
//
//				sal = aligner.align(seqB, seqB);
//				Mark.say("\n>>> seqB, seqB");
//				sal.stream().forEachOrdered(a -> Mark.say("Score:", a.score));
		
	}
	
	public static Sequence ApproachStory2() {

		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		Sequence story1 = new Sequence();
		try {
			story1.addElement(basicTranslator.translate("Start Story titled \"Approach Story 2\"").getElement(0));
			story1.addElement(basicTranslator.translate("Jack is a person").getElement(0));
			story1.addElement(basicTranslator.translate("Rose is a person").getElement(0));
			story1.addElement(basicTranslator.translate("Jack is far from Rose.").getElement(0));
			story1.addElement(basicTranslator.translate("Jack runs towards Rose.").getElement(0));
			story1.addElement(basicTranslator.translate("Jack is near Rose.").getElement(0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story1;
	}
	
	public static Sequence RandStory1(String storyname) {

		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		String items[] = storyname.split(" ", 2);
		String person = items[0];
		String object = items[1];
		String[] sents = { "Start Story titled \"Story "+storyname+"\"", 
				person + " is a person",
				person + " eats "+ object+"."
				};
		
		Sequence story1 = new Sequence();
		try {
			for (String s: sents) {
				story1.addElement(basicTranslator.translate(s).getElement(0));				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story1;
	}
	public static Sequence goalStory(String storyname) {

		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		String items[] = storyname.split(" ", 2);
		String person = items[0];
		String object = items[1];
		String[] sents = { "Start Story titled \"Story "+storyname+"\"", 
				person + " is a person",
				person + " faces "+ object+".",
				person + " feels sad."
				};
		
		Sequence story1 = new Sequence();
		try {
			for (String s: sents) {
				story1.addElement(basicTranslator.translate(s).getElement(0));				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story1;
	}
	public static Sequence goalStory2(String storyname) {

		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		String items[] = storyname.split(" ", 2);
		String person = items[0];
		String object = items[1];
		String[] sents = { "Start Story titled \"Story "+storyname+"\"", 
				person + " is a person",
				person + " faces "+ object + ".",
				person + " is hindered by an obstacle.",
				person + " cannot achieve the goal.",
				person + " feels sad."
				};
		
		Sequence story1 = new Sequence();
		try {
			for (String s: sents) {
				story1.addElement(basicTranslator.translate(s).getElement(0));				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story1;
	}

}
