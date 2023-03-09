package matchers;

import java.util.*;

import frames.entities.Sequence;
import generator.Generator;
import translator.BasicTranslator;

/*
 * Created on Jun 2, 2016
 * @author phw
 */

public class Cases {

	private static List<Sequence> cases;

	public static List<Sequence> getCases() {
		if (cases == null) {
			cases = Arrays.asList(FluStory(), ColdStory(), CardiacStory(), ClotStory());
		}
		return cases;
	}

	public static Sequence patientA() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		generator.setStoryMode();
		Sequence story = new Sequence("A");
		try {
			story.addElement(basicTranslator.translate("Start Story titled \"Flu Story\"").getElement(0));
			story.addElement(basicTranslator.translate("Paul is a person.").getElement(0));
			story.addElement(basicTranslator.translate("Paul is weak.").getElement(0));
			story.addElement(basicTranslator.translate("Paul has headache.").getElement(0));
			story.addElement(basicTranslator.translate("Paul has fever.").getElement(0));

			story.addElement(basicTranslator.translate("Paul is programmer.").getElement(0));

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story;
	}

	public static Sequence patientB() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		generator.setStoryMode();
		Sequence story = new Sequence("B");
		try {
			story.addElement(basicTranslator.translate("Start Story titled \"Flu Story\"").getElement(0));
			story.addElement(basicTranslator.translate("Peter is a person.").getElement(0));
			story.addElement(basicTranslator.translate("Peter sneezes.").getElement(0));
			story.addElement(basicTranslator.translate("Peter sniffles.").getElement(0));

			story.addElement(basicTranslator.translate("Peter is lawyer.").getElement(0));

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story;
	}

	public static Sequence patientC() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		generator.setStoryMode();
		Sequence story = new Sequence("C");
		try {
			story.addElement(basicTranslator.translate("Start Story titled \"Pain story 1\"").getElement(0));
			story.addElement(basicTranslator.translate("Peter is a person.").getElement(0));
			story.addElement(basicTranslator.translate("Peter has angina.").getElement(0));
			story.addElement(basicTranslator.translate("Peter is economist.").getElement(0));

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story;
	}

	public static Sequence patientD() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		generator.setStoryMode();
		Sequence story = new Sequence("D");
		try {
			story.addElement(basicTranslator.translate("Start Story titled \"Pain story 2\"").getElement(0));
			story.addElement(basicTranslator.translate("Peter is a person.").getElement(0));
			story.addElement(basicTranslator.translate("Peter has angina.").getElement(0));
			story.addElement(basicTranslator.translate("Peter traveled.").getElement(0));

			story.addElement(basicTranslator.translate("Peter is economist.").getElement(0));

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story;
	}

	/**
	 * Symptoms in opposite order
	 * 
	 * @return
	 */
	public static Sequence AnotherSymtomStory() {

		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		Sequence story = new Sequence("E");
		try {
			story.addElement(basicTranslator.translate("Start Story titled \"Flu Story\"").getElement(0));
			story.addElement(basicTranslator.translate("Paul is a person.").getElement(0));
			story.addElement(basicTranslator.translate("Paul has fever.").getElement(0));
			story.addElement(basicTranslator.translate("Paul has headache.").getElement(0));
			story.addElement(basicTranslator.translate("Paul is weak.").getElement(0));

			story.addElement(basicTranslator.translate("Paul is professor.").getElement(0));

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story;
	}

	public static Sequence FluStory() {

		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();

		generator.setStoryMode();

		Sequence story = new Sequence("Flu");
		try {
			story.addElement(basicTranslator.translate("Start Story titled \"Flu Story\"").getElement(0));
			story.addElement(basicTranslator.translate("Patient is a person.").getElement(0));
			story.addElement(basicTranslator.translate("Patient has fever.").getElement(0));
			story.addElement(basicTranslator.translate("Patient has headache.").getElement(0));
			story.addElement(basicTranslator.translate("Patient is weak.").getElement(0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story;
	}

	public static Sequence ColdStory() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		generator.setStoryMode();
		Sequence story = new Sequence("Cold");
		try {
			story.addElement(basicTranslator.translate("Start Story titled \"Cold Story\"").getElement(0));
			story.addElement(basicTranslator.translate("Patient is a person.").getElement(0));
			story.addElement(basicTranslator.translate("Patient sniffles.").getElement(0));
			story.addElement(basicTranslator.translate("Patient sneezes.").getElement(0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story;
	}

	public static Sequence CardiacStory() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		generator.setStoryMode();
		Sequence story = new Sequence("Probable cardiac problem");
		try {
			story.addElement(basicTranslator.translate("Start Story titled \"Cardiac Story\"").getElement(0));
			story.addElement(basicTranslator.translate("Patient is a person.").getElement(0));
			story.addElement(basicTranslator.translate("Patient has angina.").getElement(0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story;
	}

	public static Sequence ClotStory() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		generator.setStoryMode();
		Sequence story = new Sequence("Probable cloting problem");
		try {
			story.addElement(basicTranslator.translate("Start Story titled \"Clot Story\"").getElement(0));
			story.addElement(basicTranslator.translate("Patient is a person.").getElement(0));
			story.addElement(basicTranslator.translate("Patient has hypertension.").getElement(0));
			story.addElement(basicTranslator.translate("Patient has angina.").getElement(0));
			story.addElement(basicTranslator.translate("Patient traveled.").getElement(0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story;
	}

	public static Sequence Test1() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		generator.setStoryMode();
		Sequence story = new Sequence("Test1");
		try {
			story.addElement(basicTranslator.translate("Start Story titled \"Test 1\"").getElement(0));
			story.addElement(basicTranslator.translate("George has headache.").getElement(0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story;
	}

	public static Sequence Test2() {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		generator.setStoryMode();
		Sequence story = new Sequence("Test2");
		try {
			story.addElement(basicTranslator.translate("Start Story titled \"Test 2\"").getElement(0));
			story.addElement(basicTranslator.translate("Paul has angina.").getElement(0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		generator.flush();
		return story;
	}

}
