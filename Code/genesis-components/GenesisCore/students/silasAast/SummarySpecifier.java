package silasAast;

import matchers.StandardMatcher;
import connections.AbstractWiredBox;
import frames.entities.Entity;
import frames.entities.Sequence;

public class SummarySpecifier extends AbstractWiredBox{

	//INPUT PORTS
	public static String NARRATOR_VIEW_IN;
	public static String INSTANTIATED_REFLECTIONS;  //from StoryProcessor.
	
	// OUTPUT PORTS
	
	// FIELDS
	private static Sequence desiredReaction = new Sequence();
	
	public SummarySpecifier(){
	
	}
	
	public static Sequence setGoal(Sequence narratorView, Sequence reflections){
		StandardMatcher matcher = StandardMatcher.getBasicMatcher();
		
		for (Entity storyElement: narratorView.getElements()){
			for (Entity reflection : reflections.getElements()){
				if (matcher.match(storyElement, reflection)!=null){
					desiredReaction.addElement(storyElement);
				}
			}
		}
		
		return desiredReaction;
	}

}
