package frames;

import connections.*;
import frames.entities.Entity;
import frames.entities.Relation;
import memory.Memory;
import java.util.List;
import java.util.ArrayList;

/**
 * @author ryscheng
 * Created on January 22, 2008
 * 
 * Performs all Memory interactions for force interactions
 */
public class ForceMemory {

	//Copied from ForceInterpreter
	public static final String [] forceWords = ForceInterpreter.forceWords;
	public static final String [] forceThread = ForceInterpreter.forceThread;
	public static final String [] causeWords = ForceInterpreter.causeWords;
	public static final String [] activeWords = ForceInterpreter.activeWords;
	
	/**
	 * Grabs the static memory variable that all of Gauntlet uses
	 */
	public static Memory getMemory(){
		return Memory.getMemory();
	}
	
	/**
	 * 
	 * @param force - a force relation that is ambiguous whether the agonist or antagonist wins
	 * @return true if in memory contains an instance where the same antagonist forces the agonist to do the same thing
	 */
	public static boolean isForceRelation(Relation force) {
		int i,j;
		Entity subject = force.getSubject();
		Entity object = force.getObject();
		ArrayList<Entity> neighbors = new ArrayList<Entity>();
		List<Entity> tempResult;
		Entity currThing;
		Relation currRelation;
		Relation searchRelation = new Relation(ForceMemory.forceThread[0],subject,object);
		searchRelation.removeType("thing");
		
		//Search for neighbors starting with the first two types of the forceThread
		//and extend to the full forceThread
		for (i=1;i<ForceMemory.forceThread.length;i++){
			searchRelation.addType(ForceMemory.forceThread[i]);
			tempResult = getMemory().getNeighbors(searchRelation);
			
			//Add it to the master neighbors list if not already in there
			for (j=1;j<tempResult.size();j++){
				if (!neighbors.contains(tempResult.get(j))) {
					neighbors.add(tempResult.get(j));
				}
			}
		}
		
		Entity agonist = ForceInterpreter.getAgonist(force);
		Entity antagonist = ForceInterpreter.getAntagonist(force);
		
		//For each neighbor, if we find the same agonist, antagonist, and action, then return true
		for (j=0;j<neighbors.size();j++){
			currThing = neighbors.get(j);
			if (currThing instanceof Relation){
				currRelation = (Relation) currThing;
				if ((ForceInterpreter.getAgonist(currRelation).isEqual(agonist)) &&
					(ForceInterpreter.getAntagonist(currRelation).isEqual(antagonist)) &&
					(force.getObject().isA(currRelation.getObject().getType()))){
					return true;
				}
			}
		}
		
		return false;
	}
	
}
