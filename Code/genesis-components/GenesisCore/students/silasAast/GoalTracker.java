package silasAast;

import java.util.ArrayList;

import matchers.StandardMatcher;
import connections.AbstractWiredBox;
import connections.Connections;
import frames.entities.Entity;
import frames.entities.Sequence;


public class GoalTracker extends AbstractWiredBox {
	
	/**NOTE about matchers in this class... 
	 * There are different scenarios we will entertain: Story-telling modes where GAP-FILLING is most important.
	 * Story-telling modes where CONFLICT-RESOLUTION will be needed.  This can be done "exactly" or "as long as the important 
	 * stuff is there".
	 * So functions: gapMatcher, simpleConflictMatcher, sophisticatedConflictMatcher
	 ***/
	
	// INPUT PORTS
	public static String GOAL_REACTION; // from GoalSpecifier.
	public static String SIMULATED_REACTION;  // from StorySimulator.this signal includes goal and simulated reactions. 
	
	// OUTPUT PORTS
	
	public static String GAP_LIST;  // to StoryModifier. indicates what's missing in simulated audience perspective.
	public static String FINAL_TEXT_OUT; // to StoryPublisher. 
	
	// FIELDS	
	private int MATCH_MODE = 0; //if this is 0: gap-filling, if 1: conflict-resolution 
	private Entity oneBeforeMissing;
	
	public static Sequence goalReaction; // Stores audience reaction narrator desires
	public static Sequence simulatedReaction; // Stores simulated audience reaction
	public ArrayList<ArrayList<Entity>> gapList = new ArrayList<ArrayList<Entity>>(); 
	public static boolean done; // Indicates if narrator is ready to tell story to audience
	public static Sequence finalText; //Story to be told to audience in inner representation
	
	
	public GoalTracker(){
		Connections.getPorts(this).addSignalProcessor(GOAL_REACTION, "storeGoal");
		Connections.getPorts(this).addSignalProcessor(SIMULATED_REACTION, "prepareGoalEvaluation"); //temporary!!!
	}
	
	public void storeGoal(Object o){
		if (o instanceof Sequence){
			goalReaction = (Sequence) o;
		}
	}
	
	public void prepareGoalEvaluation(Object o){
		if (o instanceof Sequence){
			simulatedReaction = (Sequence) o;
			if (goalReaction!=null && MATCH_MODE==0){
				gapMatcher(goalReaction, simulatedReaction);
			}
		}
	}
	
	public void gapMatcher(Sequence goal, Sequence actual){
		StandardMatcher matcher = StandardMatcher.getBasicMatcher();
		int counter = 0;
		
		for (Entity goalElement : goal.getElements()){
			counter++;
			ArrayList<Entity> toAdd = new ArrayList<Entity>();
			for (Entity simElement : actual.getElements()){
				if(matcher.matchAll(goalElement, simElement)==null){
					Entity lastElement = goal.getElement(counter-1);
					toAdd.add(lastElement);
					toAdd.add(goalElement);
					gapList.add(toAdd);
				}
			}
		}
		Connections.getPorts(this).transmit(GAP_LIST, gapList);
		
	}
	

}
