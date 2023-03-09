package matthewFay.representations;

import java.util.ArrayList;
import java.util.List;

import utils.Mark;

import constants.Markers;
import frames.entities.Entity;

public class StoryGraphNode implements Comparable<StoryGraphNode> {
	private Entity entity;
	public Entity getEntity() {
		return entity;
	}
	private List<StoryGraphEdge> antecedentEdges;
	private List<StoryGraphEdge> consequentEdges;
	
	public int depth = 0;
	public int height = -1;
	public int order = -1;
	
	///////////////////////////
	//These are properties that often come from elaboration graph
	//style representations/rules
	private boolean prediction = false;
	private boolean active = false;
	public void setPrediction(boolean value) {
		prediction = value;
	}
	public boolean getPrediction() {
		return prediction;
	}
	
	public boolean getAssumed() {
		return entity.hasFeature(Markers.ASSUMED);
	}
	
	public boolean getNegated() {
		return entity.hasFeature(Markers.NOT);
	}
	public boolean getActive() {
		return active;
	}
	public void setActive(boolean value) {
		active = value;
	}
	//End of Property Section
	//////////////////////////
	
	void addAntecedentEdge(StoryGraphEdge edge) {
		antecedentEdges.add(edge);
	}
	
	void addConsequentEdge(StoryGraphEdge edge) {
		consequentEdges.add(edge);
	}
	
	public StoryGraphNode(Entity entity) {
		this.entity = entity;
		antecedentEdges = new ArrayList<StoryGraphEdge>();
		consequentEdges = new ArrayList<StoryGraphEdge>();
	}
	
	public List<StoryGraphNode> getAntecedents() {
		List<StoryGraphNode> antecedents = new ArrayList<StoryGraphNode>();
		for(StoryGraphEdge edge : antecedentEdges) {
			antecedents.add(edge.getAntecedent());
		}
		return antecedents;
	}
	
	public List<StoryGraphNode> getConsequents() {
		List<StoryGraphNode> consequents = new ArrayList<StoryGraphNode>();
		for(StoryGraphEdge edge : consequentEdges) {
			consequents.add(edge.getConsequent());
		}
		return consequents;
	}
	
	public List<StoryGraphNode> getAllNeighbors() {
		List<StoryGraphNode> neighbors = new ArrayList<StoryGraphNode>();
		neighbors.addAll(getAntecedents());
		neighbors.addAll(getConsequents());
		return neighbors;
	}
	
	@Override
	public int compareTo(StoryGraphNode o) {
		return height - o.height;
	}
}