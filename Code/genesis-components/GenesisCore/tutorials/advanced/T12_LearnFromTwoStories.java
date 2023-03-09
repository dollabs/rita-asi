package advanced;

import zhutianYang.RecipeExpert;
import zhutianYang.RecipeLearner;
import utils.Z;
import zhutianYang.ZRelation;

public class T12_LearnFromTwoStories {

	public static void main(String[] args) {
		
		// to suppress non-informative debugging logs
		ZRelation.DEBUG = false;
		RecipeLearner.DEBUG = false;
		RecipeExpert.ROBOT_DEMO = false;
		Z.DEBUG = false;
		Z.suppressPrinting = false;
		
		// input: 	the path to story file that contains two stories, note that the name of the file should be the goal
		// output:	two micro-stories files and two problem solving test files 
		// 				will be generated into the same parent folder as the story file
		Z.learnFromStoryEnsembles("corpora/Ensembles/Repair a phone.txt");

	}

}