package dylanHolmes;

import java.util.ArrayList;
import java.util.Queue;
import java.util.Vector;

import storyProcessor.ConceptDescription;
import utils.Mark;
import connections.Connections;
import connections.WiredBox;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import frames.entities.Entity;
	

public class ElaborationViewer extends NetworkViewer<EntityBox> {

	public static final String FROM_REFLECTION_BAR = "plot unit input";
	public static final String PROCESS_PATH = "processing path";
	
	// NETWORK METHODS
	
	/**
	 * Perform a linear search through the network's list of vertices for a box with 
	 * the same name as the given Entity. If the box is found, returns it. Otherwise, if createIfNotFound 
	 * is true, creates an EntityBox associated with that entity and returns it. Otherwise, returns null.
	 * 
	 * @param t
	 * @param createIfNotFound
	 * @return
	 */
	public EntityBox getBoxByEntity(Entity e, boolean createIfNotFound){
		EntityBox result = this.getBoxByName(e.getName());
		if(result == null && createIfNotFound){
			return new EntityBox(e);
		}
		return result;
	}
	
	/**
	 * Search for a path from the root EntityBox to the needle EntityBox. If found, return a list of the EntityBoxes in the path. Otherwise, return null.
	 * @param root
	 * @param needle
	 * @return
	 */
	public ArrayList<EntityBox> forward(EntityBox root, Entity needle){
		ArrayList<ArrayList<EntityBox>> pathQueue = new ArrayList<ArrayList<EntityBox>>();
		EntityBox end;
		
		ArrayList<EntityBox> path = new ArrayList<EntityBox>();
		path.add(root);
		pathQueue.add(path);
		
		while(!pathQueue.isEmpty()){
			path = pathQueue.get(0);
			end = path.get(pathQueue.size()-1);
			if(matchEntity(needle, end.entity)){
				return path;
			}
		}
		
		//mpfay: fixing compile error, might not be what you want.
		return null;
	}
	
	// PROPAGATION METHODS
	
	/**
	 * Update (mark as changed) the boxes in this network that 
	 * correspond to the Entities in signal.
	 * @param signal
	 */
	public void updateBoxes(Object signal){
		Vector<Entity> entityList;
		ArrayList<WiredBox> boxes = new ArrayList<WiredBox>();
		
		if(signal instanceof Vector){
			// "process path"
			entityList = (Vector<Entity>) signal;
			Mark.say("Path received for display:", entityList.size(), "elements");
		}
		else if (signal instanceof ConceptDescription){
			// "process plot unit"
			ConceptDescription completion = (ConceptDescription) signal;
			entityList = completion.getStoryElementsInvolved().getElements();
		}
		else {
			Mark.say("Unrecognized signal sent to ElaborationViewer.");
			return;
		}
		

		WiredBox b;
		
		for(Entity e : entityList) {
			b = this.getBoxByEntity(e, false);
			if(b != null){
				boxes.add(b);
			}
			else {
				Mark.say("Found NO box for ", e.asStringWithIndexes());
			}
		}
		
		changed(boxes);	
	}
	
	
	// ENTITY METHODS
	public static boolean matchEntity(Entity x, Entity y){
		return (null != frames.entities.Matcher.match(x, y));
	}
	
	
	public ElaborationViewer(String name) {
		super(name);
		Connections.getPorts(this).addSignalProcessor("process");
		Connections.getPorts(this).addSignalProcessor(FROM_REFLECTION_BAR, "processPlotUnit");
		Connections.getPorts(this).addSignalProcessor(PROCESS_PATH, "processPath");
	}

}

